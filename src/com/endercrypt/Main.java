package com.endercrypt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkArea;
import com.endercrypt.gol.ChunkManager;
import com.endercrypt.gol.processor.GolProcessor;
import com.endercrypt.gol.processor.ProcessingTicket;
import com.endercrypt.gui.AwtWindow;
import com.endercrypt.gui.keyboard.AppKeyListener;
import com.endercrypt.gui.keyboard.Keyboard;
import com.endercrypt.gui.keyboard.Keyboard.BindType;
import com.endercrypt.util.Average;

public class Main
{
	private static final int THREADS = 5;

	private static final int TILE_SIZE = 5;

	private static GolProcessor golProcessor;
	private static ChunkManager chunkManager;
	private static AwtWindow window;

	private static boolean playing = false;
	private static boolean step = true;

	private static int xView = -2;
	private static int yView = -2;

	public static void main(String[] args) throws InterruptedException
	{
		// processor
		golProcessor = new GolProcessor(THREADS);
		golProcessor.start();

		// chunk manager
		chunkManager = new ChunkManager();

		// screen
		int screenSize = TILE_SIZE * Chunk.SIZE * 5;
		window = new AwtWindow("Infinite Game Of Life", new Dimension(screenSize, screenSize), Main::draw);
		window.show();

		setupKeyboard();

		updateSequence();
	}

	private static void setupKeyboard()
	{
		Keyboard keyboard = window.getKeyboard();

		// camera movement
		keyboard.bindKey(KeyEvent.VK_LEFT, BindType.PRESS, new AppKeyListener()
		{
			@Override
			public void keyTriggered(int keycode, BindType bindType)
			{
				xView--;
				window.repaint();
			}
		});
		keyboard.bindKey(KeyEvent.VK_RIGHT, BindType.PRESS, new AppKeyListener()
		{
			@Override
			public void keyTriggered(int keycode, BindType bindType)
			{
				xView++;
				window.repaint();
			}
		});
		keyboard.bindKey(KeyEvent.VK_UP, BindType.PRESS, new AppKeyListener()
		{
			@Override
			public void keyTriggered(int keycode, BindType bindType)
			{
				yView--;
				window.repaint();
			}
		});
		keyboard.bindKey(KeyEvent.VK_DOWN, BindType.PRESS, new AppKeyListener()
		{
			@Override
			public void keyTriggered(int keycode, BindType bindType)
			{
				yView++;
				window.repaint();
			}
		});

		// play controls
		keyboard.bindKey(KeyEvent.VK_SPACE, BindType.PRESS, (keyCode, bindType) -> playing = !playing);
		keyboard.bindKey(KeyEvent.VK_S, BindType.PRESS, (keyCode, bindType) -> step = true);

		// misc
		keyboard.bindKey(KeyEvent.VK_C, BindType.PRESS, new AppKeyListener()
		{
			@Override
			public void keyTriggered(int keycode, BindType bindType)
			{
				final int CHUNK_SIZE = TILE_SIZE * Chunk.SIZE;
				Dimension screenSize = window.screenSize();
				xView = -(screenSize.width / 2) / CHUNK_SIZE;
				yView = -(screenSize.height / 2) / CHUNK_SIZE;
				window.repaint();
			}
		});
	}

	private static void updateSequence() throws InterruptedException
	{
		// update game
		Average average = new Average(100);
		int updates = 0;
		while (true)
		{
			if ((playing == false) && (step == false))
			{
				Thread.sleep(20);
				continue;
			}
			step = false;

			// update
			int updateDelta = update();
			average.add(updateDelta);
			updates++;

			if (updates % 50 == 0)
			{
				System.out.println("Update Time: " + average.get() + " ms");
			}

			if (updates % 200 == 0)
			{
				int gcDelta = chunkManager.garbageCollect();
				System.out.println("GC: collected (took " + gcDelta + " ms)");
			}

			// draw
			window.repaint();
		}
	}

	private synchronized static int update() throws InterruptedException
	{
		// count update time taken
		long time = System.currentTimeMillis();

		// update all
		//System.out.println("Performing update");
		ProcessingTicket updateTicket = golProcessor.newUpdate();
		updateTicket.update(chunkManager);
		updateTicket.finish();

		// 4 threads = 5 ms
		// direct update (this thread) = 13 ms

		// randomize center
		//System.out.println("Randomizing center chunk");
		Chunk centerChunk = chunkManager.getChunk(0, 0);
		for (int i = 0; i < 10; i++)
		{
			int rx = (int) (Math.random() * Chunk.SIZE);
			int ry = (int) (Math.random() * Chunk.SIZE);
			centerChunk.getBackBuffer().set(rx, ry, true);
		}

		// flip all buffers
		//System.out.println("Flipping buffers");
		for (ChunkArea chunkArea : chunkManager)
		{
			for (Chunk chunk : chunkArea)
			{
				chunk.flipBuffers();
			}
		}

		return (int) (System.currentTimeMillis() - time);
	}

	private synchronized static void draw(Graphics2D g2d)
	{
		final int pixelSize = Chunk.SIZE * TILE_SIZE;

		Dimension screenSize = window.screenSize();
		int xAreas = (screenSize.width / pixelSize) + 1;
		int yAreas = (screenSize.height / pixelSize) + 1;

		// draw all chunks
		for (int x = 0; x < xAreas; x++)
		{
			for (int y = 0; y < yAreas; y++)
			{
				chunkManager.getChunk(x + xView, y + yView).draw(g2d, x * pixelSize, y * pixelSize, TILE_SIZE);
				g2d.setColor(Color.BLUE);
				g2d.drawRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
			}
		}

		// mark center
		g2d.setColor(Color.RED);
		g2d.drawRect(-xView * pixelSize, -yView * pixelSize, pixelSize, pixelSize);
	}
}

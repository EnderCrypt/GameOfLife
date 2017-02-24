package com.endercrypt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Random;

import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkArea;
import com.endercrypt.gol.ChunkBuffer;
import com.endercrypt.gol.ChunkManager;
import com.endercrypt.gol.processor.GolProcessor;
import com.endercrypt.gol.processor.ProcessingTicket;
import com.endercrypt.gui.AwtWindow;
import com.endercrypt.gui.keyboard.AppKeyListener;
import com.endercrypt.gui.keyboard.Keyboard;
import com.endercrypt.gui.keyboard.Keyboard.BindType;
import com.endercrypt.setting.Settings;
import com.endercrypt.util.Average;

public class Main
{
	private static final int THREADS = Settings.get().key("Threads").getInteger();

	private static final int TILE_SIZE = Settings.get().key("CellSize").getInteger();

	private static GolProcessor golProcessor;
	private static ChunkManager chunkManager;
	private static AwtWindow window;
	private static Random random;

	private static boolean playing = false;
	private static boolean step = true;

	private static int frame = 0;

	private static int xView, yView = 0;

	public static void main(String[] args) throws InterruptedException
	{
		// random
		random = new Random(Settings.get().key("Seed").getLong());

		// processor
		golProcessor = new GolProcessor(THREADS);
		golProcessor.start();

		// chunk manager
		chunkManager = new ChunkManager();

		// screen
		int screenSize = TILE_SIZE * Chunk.SIZE * 11;
		window = new AwtWindow("Infinite Game Of Life", new Dimension(screenSize, screenSize), Main::draw);
		centerMap();
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
				centerMap();
				window.repaint();
			}
		});
	}

	private static void centerMap()
	{
		final int CHUNK_SIZE = TILE_SIZE * Chunk.SIZE;
		Dimension screenSize = window.screenSize();
		xView = -(screenSize.width / 2) / CHUNK_SIZE;
		yView = -(screenSize.height / 2) / CHUNK_SIZE;
	}

	private static void updateSequence() throws InterruptedException
	{
		// update game

		Average average = new Average(100);
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
			frame++;

			if (frame % 50 == 0)
			{
				System.out.println("Update Time: " + average.get() + " ms");
			}

			// garbage collect
			if (frame % 250 == 0)
			{
				int gcDelta = chunkManager.garbageCollect();
				System.out.println("GC: finished (took " + gcDelta + " ms)");
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
		ProcessingTicket updateTicket = golProcessor.newUpdate();
		updateTicket.update(chunkManager);
		updateTicket.finish();

		// flip all buffers
		for (ChunkArea chunkArea : chunkManager)
		{
			for (Chunk chunk : chunkArea)
			{
				chunk.flipBuffers();
			}
		}

		// randomize center
		randomizeChunk(chunkManager.getChunk(0, 0), Settings.get().key("mirrorMode").getBoolean());

		return (int) (System.currentTimeMillis() - time);
	}

	private static void randomizeChunk(Chunk chunk, boolean mirrorMode)
	{
		ChunkBuffer mainBuffer = chunk.getFrontBuffer();
		if (mirrorMode)
		{
			final int CENTER = Chunk.SIZE / 2;
			for (int i = 0; i < 5; i++)
			{
				int rx = random.nextInt(CENTER);
				int ry = random.nextInt(Chunk.SIZE);
				mainBuffer.set(CENTER - rx, ry, true);
				mainBuffer.set(CENTER + rx, ry, true);
			}
		}
		else
		{
			for (int i = 0; i < 10; i++)
			{
				int rx = random.nextInt(Chunk.SIZE);
				int ry = random.nextInt(Chunk.SIZE);
				mainBuffer.set(rx, ry, true);
			}
		}
	}

	private synchronized static void draw(Graphics2D g2d)
	{
		final int pixelSize = Chunk.SIZE * TILE_SIZE;

		Dimension screenSize = window.screenSize();
		int xAreas = (screenSize.width / pixelSize) + 1;
		int yAreas = (screenSize.height / pixelSize) + 1;

		// draw background
		g2d.setColor(Settings.get().key("BackgroundColor").colorArgs(255));
		g2d.fillRect(0, 0, screenSize.width, screenSize.height);

		// draw all chunks
		for (int x = 0; x < xAreas; x++)
		{
			for (int y = 0; y < yAreas; y++)
			{
				Chunk chunk = chunkManager.unsafeGetChunk(x + xView, y + yView);
				if (chunk != null)
					chunk.draw(g2d, x * pixelSize, y * pixelSize, TILE_SIZE);
			}
		}

		// draw grid
		if (Settings.get().key("Grid").getBoolean())
		{
			g2d.setColor(Settings.get().key("GridColor").colorArgs(255));
			for (int x = 0; x < screenSize.width; x += pixelSize)
			{
				g2d.drawLine(x, 0, x, screenSize.height);
			}
			for (int y = 0; y < screenSize.height; y += pixelSize)
			{
				g2d.drawLine(0, y, screenSize.width, y);
			}
		}

		// mark center
		if (Settings.get().key("CenterSquare").getBoolean())
		{
			g2d.setColor(Settings.get().key("CenterSquareColor").colorArgs(255));
			g2d.drawRect(-xView * pixelSize, -yView * pixelSize, pixelSize, pixelSize);
		}

		// draw hud
		g2d.setColor(Color.WHITE);
		g2d.drawString("Created by EnderCrypt", 5, 16);
		g2d.drawString(thousandMarks(frame), 5, 32);
	}

	private static String thousandMarks(long value)
	{
		// iterator
		Iterator<String> iterator = new Iterator<String>()
		{
			private String stringValue = String.valueOf(value);
			private int lastIndex = stringValue.length();
			private int index = stringValue.length();

			@Override
			public boolean hasNext()
			{
				return (index > 0);
			}

			@Override
			public String next()
			{
				index -= 3;
				String result = stringValue.substring(Math.max(0, index), lastIndex);
				lastIndex = index;
				return result;
			}
		};
		// process
		StringBuilder sb = new StringBuilder();
		while (iterator.hasNext())
		{
			String next = iterator.next();
			sb.insert(0, next);
			if (iterator.hasNext())
				sb.insert(0, "'");
		}
		return sb.toString();
	}
}

package com.endercrypt.gol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.endercrypt.gol.lookup.GolDeadLookup;
import com.endercrypt.gol.lookup.GolLivingLookup;
import com.endercrypt.gol.lookup.GolLookup;

public class Chunk
{
	public static final int SIZE = 10;

	private int x, y;

	private ChunkBuffer frontBuffer;
	private ChunkBuffer backBuffer;

	public Chunk(int x, int y)
	{
		this.x = x;
		this.y = y;

		frontBuffer = new ChunkBuffer();
		backBuffer = new ChunkBuffer();
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Point getPosition()
	{
		return new Point(x, y);
	}

	public void update(ChunkManager chunkManager)
	{
		// prepare lookup
		GolLookup lookup = null;
		if (frontBuffer.haslife())
			lookup = new GolLivingLookup(this, chunkManager);
		else
			lookup = new GolDeadLookup(this, chunkManager);
		// update buffer
		frontBuffer.writeBufferUpdate(backBuffer, lookup);
	}

	public ChunkBuffer getFrontBuffer()
	{
		return frontBuffer;
	}

	public ChunkBuffer getBackBuffer()
	{
		return backBuffer;
	}

	public void flipBuffers()
	{
		ChunkBuffer newFrontBuffer = backBuffer;
		backBuffer = frontBuffer;
		frontBuffer = newFrontBuffer;
	}

	public void draw(Graphics2D g2d, int xPosition, int yPosition, int size)
	{
		if (frontBuffer.haslife())
		{
			for (int dx = 0; dx < Chunk.SIZE; dx++)
			{
				for (int dy = 0; dy < Chunk.SIZE; dy++)
				{
					int xPixel = xPosition + (dx * size);
					int yPixel = yPosition + (dy * size);
					boolean alive = frontBuffer.get(dx, dy);
					g2d.setColor(alive ? Color.WHITE : Color.BLACK);
					g2d.fillRect(xPixel, yPixel, size, size);
				}
			}
		}
		else
		{
			g2d.setColor(Color.BLACK);
			g2d.fillRect(xPosition, yPosition, Chunk.SIZE * size, Chunk.SIZE * size);
		}
	}

	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + ":" + x + ", " + y + "]";
	}
}

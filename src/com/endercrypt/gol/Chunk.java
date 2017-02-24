package com.endercrypt.gol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.endercrypt.gol.lookup.GolBaseLookup;
import com.endercrypt.gol.lookup.GolLookup;
import com.endercrypt.setting.Settings;

public class Chunk
{
	public static final int SIZE = 10;

	public static final Color backgroundColor = Settings.get().key("BackgroundColor").colorArgs(255);
	public static final Color lifeColor = Settings.get().key("LifeColor").colorArgs(255);

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
		GolLookup lookup = GolBaseLookup.retriveLookup(this, chunkManager);
		// update buffer
		frontBuffer.writeBufferUpdate(backBuffer, lookup);
	}

	public ChunkBuffer getFrontBuffer()
	{
		return frontBuffer;
	}

	public void flipBuffers()
	{
		ChunkBuffer newFrontBuffer = backBuffer;
		backBuffer = frontBuffer;
		frontBuffer = newFrontBuffer;
	}

	public boolean isNearEdge()
	{
		int xMod = Math.floorMod(x, Chunk.SIZE);
		if ((xMod == 0) || (xMod == 9))
			return true;

		int yMod = Math.floorMod(y, Chunk.SIZE);
		if ((yMod == 0) || (yMod == 9))
			return true;

		return false;
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
					g2d.setColor(alive ? lifeColor : backgroundColor);
					g2d.fillRect(xPixel, yPixel, size, size);
				}
			}
		}
		else
		{
			g2d.setColor(backgroundColor);
			g2d.fillRect(xPosition, yPosition, Chunk.SIZE * size, Chunk.SIZE * size);
		}
	}

	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + ":" + x + ", " + y + "]";
	}
}

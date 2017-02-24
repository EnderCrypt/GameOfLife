package com.endercrypt.gol;

import com.endercrypt.gol.lookup.GolLookup;

public class ChunkBuffer
{
	private boolean hasLife;
	private boolean[][] buffer;

	public ChunkBuffer()
	{
		clear();
	}

	public boolean haslife()
	{
		return hasLife;
	}

	public void writeBufferUpdate(ChunkBuffer targetBuffer, GolLookup lookup)
	{
		targetBuffer.clear();
		for (int x = 0; x < Chunk.SIZE; x++)
		{
			for (int y = 0; y < Chunk.SIZE; y++)
			{
				int neighbours = lookup.neighbours(x, y);
				boolean living = buffer[x][y];
				if (living)
				{
					if ((neighbours < 2) || (neighbours > 3))
					{
						living = false;
					}
				}
				else
				{
					if (neighbours == 3)
					{
						living = true;
					}
				}
				targetBuffer.set(x, y, living);
			}
		}
	}

	public boolean get(int x, int y)
	{
		return buffer[x][y];
	}

	public void set(int x, int y, boolean live)
	{
		if (live)
			hasLife = true;
		buffer[x][y] = live;
	}

	public void clear()
	{
		hasLife = false;
		buffer = new boolean[Chunk.SIZE][Chunk.SIZE];
	}

	public int countLiving(int limit)
	{
		if (limit <= 0)
			return 0;

		int living = 0;
		for (int x = 0; x < Chunk.SIZE; x++)
		{
			for (int y = 0; y < Chunk.SIZE; y++)
			{
				if (buffer[x][y])
					living++;
				if (living >= limit)
					return living;
			}
		}
		return living;
	}

	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + ":" + (hasLife ? "Living" : "Dead") + "]";
	}
}

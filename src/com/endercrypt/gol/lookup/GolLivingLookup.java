package com.endercrypt.gol.lookup;

import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkBuffer;
import com.endercrypt.gol.ChunkManager;

public class GolLivingLookup extends GolBaseLookup
{
	public GolLivingLookup(Chunk chunk, ChunkManager chunkManager)
	{
		super(chunk, chunkManager);
	}

	@Override
	protected ChunkBuffer internalChunkGet(int x, int y)
	{
		return chunkManager.getChunk(x, y).getFrontBuffer();
	}

	@Override
	public boolean get(int x, int y)
	{
		int xRelChunk = 0;
		int yRelChunk = 0;

		if (x < 0)
		{
			x += Chunk.SIZE;
			xRelChunk--;
		}

		if (x >= Chunk.SIZE)
		{
			x -= Chunk.SIZE;
			xRelChunk++;
		}

		if (y < 0)
		{
			y += Chunk.SIZE;
			yRelChunk--;
		}

		if (y >= Chunk.SIZE)
		{
			y -= Chunk.SIZE;
			yRelChunk++;
		}

		return getRelativeNeighbour(xRelChunk, yRelChunk).get(x, y);
	}
}

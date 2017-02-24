package com.endercrypt.gol.lookup;

import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkBuffer;
import com.endercrypt.gol.ChunkManager;

public class GolDeadLookup extends GolBaseLookup
{
	public GolDeadLookup(Chunk chunk, ChunkManager chunkManager)
	{
		super(chunk, chunkManager);
	}

	@Override
	protected ChunkBuffer internalChunkGet(int x, int y)
	{
		Chunk temp = chunkManager.unsafeGetChunk(x, y);
		if (temp != null)
			return temp.getFrontBuffer();
		return null;
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

		ChunkBuffer targetBuffer = getRelativeNeighbour(xRelChunk, yRelChunk);
		if (targetBuffer == null)
			return false;
		else
			return targetBuffer.get(x, y);
	}
}

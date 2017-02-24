package com.endercrypt.gol.lookup;

import java.awt.Point;

import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkBuffer;
import com.endercrypt.gol.ChunkManager;

public abstract class GolBaseLookup implements GolLookup
{
	protected Chunk chunk;
	protected ChunkBuffer chunkBuffer;

	protected ChunkManager chunkManager;
	protected Point chunkPosition;

	protected ChunkBuffer[][] neighbours;

	public GolBaseLookup(Chunk chunk, ChunkManager chunkManager)
	{
		this.chunk = chunk;
		chunkBuffer = chunk.getFrontBuffer();
		this.chunkManager = chunkManager;
		chunkPosition = chunk.getPosition();

		neighbours = new ChunkBuffer[3][3];
		for (int x = 0; x < 3; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				int xChunk = chunkPosition.x + (x - 1);
				int yChunk = chunkPosition.y + (y - 1);
				neighbours[x][y] = internalChunkGet(xChunk, yChunk);
			}
		}
	}

	protected abstract ChunkBuffer internalChunkGet(int x, int y);

	protected final ChunkBuffer getRelativeNeighbour(int x, int y)
	{
		return neighbours[x + 1][y + 1];
	}

	@Override
	public int neighbours(int x, int y)
	{
		if ((x > 0) && (y > 0) && (x < Chunk.SIZE - 1) && (y < Chunk.SIZE - 1))
		{
			return unsafeNeighbourCheck(x, y);
		}
		int living = 0;
		living += livingValue(x + 1, y);
		living += livingValue(x - 1, y);
		living += livingValue(x, y + 1);
		living += livingValue(x, y - 1);
		living += livingValue(x + 1, y + 1);
		living += livingValue(x - 1, y - 1);
		living += livingValue(x - 1, y + 1);
		living += livingValue(x + 1, y - 1);
		return living;
	}

	/**
	 * this neighbour check is much faster, but WILL throw error if used at a tile that is
	 * at the edge of a Chunk
	 */
	private int unsafeNeighbourCheck(int x, int y)
	{
		int living = 0;
		living += chunkBuffer.get(x + 1, y) ? 1 : 0;
		living += chunkBuffer.get(x - 1, y) ? 1 : 0;
		living += chunkBuffer.get(x, y + 1) ? 1 : 0;
		living += chunkBuffer.get(x, y - 1) ? 1 : 0;
		living += chunkBuffer.get(x + 1, y + 1) ? 1 : 0;
		living += chunkBuffer.get(x - 1, y - 1) ? 1 : 0;
		living += chunkBuffer.get(x - 1, y + 1) ? 1 : 0;
		living += chunkBuffer.get(x + 1, y - 1) ? 1 : 0;
		return living;
	}

	private int livingValue(int x, int y)
	{
		return (get(x, y) ? 1 : 0);
	}
}

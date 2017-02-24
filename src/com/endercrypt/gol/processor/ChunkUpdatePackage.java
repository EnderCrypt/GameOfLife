package com.endercrypt.gol.processor;

import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkManager;

public class ChunkUpdatePackage
{
	private ChunkManager chunkManager;
	private Chunk chunk;
	private volatile boolean finished = false;

	public ChunkUpdatePackage(ChunkManager chunkManager, Chunk chunk)
	{
		this.chunkManager = chunkManager;
		this.chunk = chunk;
	}

	public void update()
	{
		chunk.update(chunkManager);
		finished = true;
	}

	public boolean isFinished()
	{
		return finished;
	}
}

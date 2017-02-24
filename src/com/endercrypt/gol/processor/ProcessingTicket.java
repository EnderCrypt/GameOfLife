package com.endercrypt.gol.processor;

import java.util.ArrayDeque;
import java.util.Deque;
import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkArea;
import com.endercrypt.gol.ChunkManager;

public class ProcessingTicket
{
	private GolProcessor golProcessor;
	private Deque<ChunkUpdatePackage> updateQueue = new ArrayDeque<>();

	public ProcessingTicket(GolProcessor golProcessor)
	{
		this.golProcessor = golProcessor;
	}

	public void update(ChunkManager chunkManager) throws InterruptedException
	{
		for (ChunkArea chunkArea : chunkManager)
		{
			for (Chunk chunk : chunkArea)
			{
				ChunkUpdatePackage chunkUpdatePackage = new ChunkUpdatePackage(chunkManager, chunk);
				golProcessor.blockingQueue.put(chunkUpdatePackage);
				updateQueue.add(chunkUpdatePackage);
			}
		}
	}

	public void finish()
	{
		for (ChunkUpdatePackage chunkUpdatePackage : updateQueue)
		{
			while (chunkUpdatePackage.isFinished() == false)
			{
				// wait
			}
		}
	}
}

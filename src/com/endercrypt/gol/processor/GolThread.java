package com.endercrypt.gol.processor;

import java.util.concurrent.BlockingQueue;

public class GolThread implements Runnable
{
	private BlockingQueue<ChunkUpdatePackage> queue;

	public GolThread(BlockingQueue<ChunkUpdatePackage> queue)
	{
		this.queue = queue;
	}

	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				ChunkUpdatePackage chunkUpdatePackage = queue.take();
				chunkUpdatePackage.update();
			}
		}
		catch (InterruptedException e)
		{
			System.out.println(getClass().getSimpleName() + " (" + Thread.currentThread().getName() + ") gracefully shutting down...");
		}
	}
}

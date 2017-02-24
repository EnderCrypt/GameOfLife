package com.endercrypt.gol.processor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GolProcessor implements Closeable
{
	protected final BlockingQueue<ChunkUpdatePackage> blockingQueue;
	protected InternalThread[] threads;

	public GolProcessor(int threadCount)
	{
		blockingQueue = new ArrayBlockingQueue<>(64);
		threads = new InternalThread[threadCount];
		for (int i = 0; i < threadCount; i++)
		{
			threads[i] = new InternalThread(i);
		}
	}

	public void start()
	{
		for (InternalThread thread : threads)
		{
			thread.start();
		}
	}

	public ProcessingTicket newUpdate()
	{
		return new ProcessingTicket(this);
	}

	@Override
	public void close() throws IOException
	{
		for (InternalThread thread : threads)
		{
			thread.close();
		}
	}

	private class InternalThread implements Closeable
	{
		private Thread thread;
		private GolThread golThread;

		public InternalThread(int id)
		{
			golThread = new GolThread(blockingQueue);
			thread = new Thread(golThread);
			thread.setName("Gol Thread " + id);
		}

		public void start()
		{
			thread.start();
		}

		@Override
		public void close() throws IOException
		{
			thread.interrupt();
		}
	}
}

package com.endercrypt.gol.garbage;

import java.awt.Point;
import java.util.Iterator;

import com.endercrypt.gol.Chunk;
import com.endercrypt.gol.ChunkArea;
import com.endercrypt.gol.ChunkBuffer;
import com.endercrypt.gol.ChunkManager;
import com.endercrypt.gol.lookup.GolDeadLookup;
import com.endercrypt.gol.lookup.GolLookup;

public class WasteCollector
{
	private static final int GC_DISTANCE = 5; // if an area is this far away, it may be garbage collected assuming it also
	private static final int GC_TILES = 10; // has LESS living tiles than this

	private ChunkManager chunkManager;

	private long startTime;
	private long finishTime;

	public WasteCollector(ChunkManager chunkManager)
	{
		this.chunkManager = chunkManager;

		startTime = System.currentTimeMillis();
	}

	public int getTime()
	{
		return (int) (finishTime - startTime);
	}

	/**
	 * performs garbage collection on all chunks in the chunk manager
	 */
	public void process()
	{
		Iterator<ChunkArea> iterator = chunkManager.iterator();
		while (iterator.hasNext())
		{
			ChunkArea chunkArea = iterator.next();
			boolean insignificant = processArea(chunkArea);
			if (insignificant)
				iterator.remove();
		}

		finishTime = System.currentTimeMillis();
	}

	private boolean processArea(ChunkArea chunkArea)
	{
		Point position = chunkArea.getPosition();
		boolean nearby = Math.max(Math.abs(position.x), Math.abs(position.y)) < GC_DISTANCE;
		if (nearby)
		{
			return processNearbyArea(chunkArea);
		}
		else
		{
			return processDistantArea(chunkArea);
		}
	}

	private boolean processNearbyArea(ChunkArea chunkArea)
	{
		boolean insignificant = true;
		Iterator<Chunk> iterator = chunkArea.iterator();
		while (iterator.hasNext())
		{
			Chunk chunk = iterator.next();
			if (chunk.getFrontBuffer().haslife() == false)
			{
				GolLookup lookup = new GolDeadLookup(chunk, chunkManager);
				if (lookup.hasLivingNeighbour() == false)
				{
					iterator.remove();
					continue;
				}
			}
			insignificant = false;
		}
		return insignificant;
	}

	private static boolean processDistantArea(ChunkArea chunkArea)
	{
		int living = 0;
		for (Chunk chunk : chunkArea)
		{
			boolean isNearEdge = chunk.isNearEdge();
			int localLiving = countLiving(chunk.getFrontBuffer(), (GC_TILES - living));

			if (localLiving > 0)
			{
				if (isNearEdge)
				{
					return false;
				}
				living += localLiving;
			}
		}

		if (living < GC_TILES)
		{
			System.out.println("GC: destroying distant area with " + living + " living tiles");
			return true;
		}
		return false;
	}

	private static int countLiving(ChunkBuffer chunkBuffer, int limit)
	{
		if (limit <= 0)
			return 0;

		int living = 0;
		for (int x = 0; x < Chunk.SIZE; x++)
		{
			for (int y = 0; y < Chunk.SIZE; y++)
			{
				if (chunkBuffer.get(x, y))
					living++;
				if (living >= limit)
					return living;
			}
		}
		return living;
	}
}

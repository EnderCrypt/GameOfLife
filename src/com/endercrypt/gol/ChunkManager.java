package com.endercrypt.gol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class ChunkManager implements Iterable<ChunkArea>
{
	private Deque<ChunkArea> chunkAreas = new ArrayDeque<>();

	/**
	 * retrives a valid chunk from x, y
	 * if chunk does not exist at x, y it will be created and returned
	 */
	public Chunk getChunk(int x, int y)
	{
		// calculate chunk container position
		int xArea = (int) Math.floor((double) x / ChunkArea.SIZE);
		int yArea = (int) Math.floor((double) y / ChunkArea.SIZE);

		// get chunk container
		//System.out.println("getChunk " + x + ", " + y + " (area: x: " + xArea + ", " + yArea + ")");
		ChunkArea chunkArea = getArea(xArea, yArea);

		// get chunk
		return chunkArea.get(x, y);
	}

	/**
	 * attempts to retrive a chunk from x and y IF it exists, otherwise returns null
	 */
	public Chunk unsafeGetChunk(int x, int y)
	{
		// calculate chunk container position
		int xArea = (int) Math.floor((double) x / ChunkArea.SIZE);
		int yArea = (int) Math.floor((double) y / ChunkArea.SIZE);

		// get chunk container
		//System.out.println("unsafeGetChunk " + x + ", " + y + " (area: x: " + xArea + ", " + yArea + ")");
		ChunkArea chunkArea = getArea(xArea, yArea);

		// get chunk
		return chunkArea.unsafeGet(x, y);
	}

	private synchronized ChunkArea getArea(int xArea, int yArea)
	{
		// get existing
		for (ChunkArea chunkArea : chunkAreas)
		{
			if (chunkArea.isChunkArea(xArea, yArea))
			{
				return chunkArea;
			}
		}

		// create
		ChunkArea area = new ChunkArea(xArea, yArea);
		chunkAreas.add(area);
		return area;
	}

	/**
	 * built in garbage collector for cleaning up the map of empty areas
	 * returns the time it took
	 */
	public synchronized int garbageCollect()
	{
		long time = System.currentTimeMillis();

		Iterator<ChunkArea> iterator = chunkAreas.iterator();
		while (iterator.hasNext())
		{
			ChunkArea area = iterator.next();
			boolean insignificant = area.garbageCollect();
			if (insignificant)
				iterator.remove();
		}

		return (int) (System.currentTimeMillis() - time);
	}

	@Override
	public Iterator<ChunkArea> iterator()
	{
		return new ArrayDeque<>(chunkAreas).iterator();
	}
}

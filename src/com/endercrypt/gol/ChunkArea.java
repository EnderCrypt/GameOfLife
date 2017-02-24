package com.endercrypt.gol;

import java.awt.Point;
import java.util.Iterator;

public class ChunkArea implements Iterable<Chunk>
{
	public static final int SIZE = 10;

	private int xPosition, yPosition;
	private int xStart, yStart, xStop, yStop;
	private Chunk[][] chunks;

	public ChunkArea(int xArea, int yArea)
	{
		xPosition = xArea;
		yPosition = yArea;
		xStart = xPosition * SIZE;
		yStart = yPosition * SIZE;
		xStop = xStart + SIZE;
		yStop = yStart + SIZE;
		chunks = new Chunk[SIZE][SIZE];
	}

	/**
	 * checks if this is the chunk area position
	 */
	public boolean isChunkArea(int x, int y)
	{
		return ((xPosition == x) && (yPosition == y));
	}

	/**
	 * checks if the chunk position is a valid position whitin this area
	 */
	public boolean isInRange(int x, int y)
	{
		return ((x >= xStart) && (y >= yStart) && (x < xStop) && (y < yStop));
	}

	/**
	 * will always return a chunk from inside the chunk container area
	 * or throw array index out of bounds exception
	 */
	public synchronized Chunk get(int x, int y)
	{
		Chunk chunk = unsafeGet(x, y);
		if (chunk == null)
		{
			chunk = new Chunk(x, y);
			int xRel = x - xStart;
			int yRel = y - yStart;
			chunks[xRel][yRel] = chunk;
		}
		return chunk;
	}

	/**
	 * will return a chunk from inside the chunk container area if it exists, otherwise null
	 * or throw array index out of bounds exception
	 */
	public synchronized Chunk unsafeGet(int x, int y)
	{
		int xRel = x - xStart;
		int yRel = y - yStart;
		Chunk chunk = chunks[xRel][yRel];
		return chunk;
	}

	public Point getPosition()
	{
		return new Point(xPosition, yPosition);
	}

	/**
	 * iterates through all the chunk positions whitin this area, even those who are null
	 */
	private Iterator<Chunk> unsafeIterator()
	{
		return new Iterator<Chunk>()
		{
			private int x = 0;
			private int y = 0;

			@Override
			public boolean hasNext()
			{
				return ((x < SIZE) && (y < SIZE));
			}

			@Override
			public Chunk next()
			{
				Chunk chunk = chunks[x][y];
				x++;
				if (x >= SIZE)
				{
					x = 0;
					y++;
				}
				return chunk;
			}
		};
	}

	/**
	 * iterates through all the existing chunks
	 */
	@Override
	public Iterator<Chunk> iterator()
	{
		return new Iterator<Chunk>()
		{
			private Iterator<Chunk> unsafeIterator = unsafeIterator();
			private Chunk nextChunk = null;
			private int xLast, yLast = -1;

			{
				nextChunk = getNext();
			}

			private Chunk getNext()
			{
				Chunk chunk = null;
				while ((chunk == null) && (unsafeIterator.hasNext()))
				{
					chunk = unsafeIterator.next();
				}
				return chunk;
			}

			@Override
			public boolean hasNext()
			{
				return (nextChunk != null);
			}

			@Override
			public Chunk next()
			{
				Chunk chunk = nextChunk;
				xLast = (chunk.getX() - xStart);
				yLast = (chunk.getY() - yStart);
				nextChunk = getNext();
				return chunk;
			}

			@Override
			public void remove()
			{
				chunks[xLast][yLast] = null;
			}
		};
	}

	/**
	 * built in garbage collector for cleaning up the area of empty chunks (dead ones)
	 * TODO: for future, only clean up areas with no living neighbour (this would improve performance on the update after the garbage collection)
	 * 
	 * returns true if the whole area should be removed
	 */
	public boolean garbageCollect()
	{
		final int GC_DISTANCE = 10; // if an area is this far away, it may be garbage collected assuming it also
		final int GC_TILES = 10; // has LESS living tiles than this

		boolean distant = Math.max(Math.abs(xPosition), Math.abs(yPosition)) > GC_DISTANCE;
		if (distant)
		{
			int living = 0;
			for (Chunk chunk : this)
			{
				boolean isNearEdge = chunk.isNearEdge();
				int localLiving = chunk.getFrontBuffer().countLiving(GC_TILES - living);

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
		else
		{
			boolean insignificant = true;
			Iterator<Chunk> iterator = iterator();
			while (iterator.hasNext())
			{
				Chunk chunk = iterator.next();
				if (chunk.getFrontBuffer().haslife() == false)
				{
					iterator.remove();
					continue;
				}
				insignificant = false;
			}
			return insignificant;
		}
	}
}

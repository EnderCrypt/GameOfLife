package com.endercrypt.gol.lookup;

public interface GolLookup
{
	public boolean get(int x, int y);

	public int neighbours(int x, int y);

	public boolean hasLivingNeighbour();
}

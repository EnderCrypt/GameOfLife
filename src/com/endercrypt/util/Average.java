package com.endercrypt.util;

public class Average
{
	private int size = 0;
	private int index = 0;
	private int total = 0;
	private boolean islooping = false;
	private int[] array;

	public Average(int size)
	{
		array = new int[size];
	}

	public void add(int number)
	{
		total += number;

		int previousValue = array[index];
		array[index] = number;

		if (islooping == false)
			size++;

		if (islooping == true)
		{
			total -= previousValue;
		}

		index++;
		if (index >= array.length)
		{
			index = 0;
			islooping = true;
		}
	}

	public int get()
	{
		return (total / size);
	}
}

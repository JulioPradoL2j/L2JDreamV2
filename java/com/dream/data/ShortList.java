package com.dream.data;

public class ShortList
{
	private static short getShort(String number)
	{
		return Short.parseShort(number);
	}

	private static short[] getShortList(String[] numbers)
	{
		short[] list = new short[numbers.length];
		for (int i = 0; i < list.length; i++)
		{
			list[i] = getShort(numbers[i]);
		}
		return list;
	}

	public static short[] parse(String range)
	{
		if (range.contains("-"))
			return getShortList(range.split("-"));
		else if (range.contains(","))
			return getShortList(range.split(","));

		short[] list =
		{
			getShort(range)
		};
		return list;
	}
}
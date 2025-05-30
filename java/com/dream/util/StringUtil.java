package com.dream.util;

import javolution.text.TextBuilder;

public final class StringUtil
{

	public static void append(final StringBuilder sbString, final String... strings)
	{
		sbString.ensureCapacity(sbString.length() + getLength(strings));

		for (final String string : strings)
		{
			sbString.append(string);
		}
	}

	public static String concat(final String... strings)
	{
		final StringBuilder sbString = new StringBuilder(getLength(strings));

		for (final String string : strings)
		{
			sbString.append(string);
		}

		return sbString.toString();
	}

	private static int getLength(final String[] strings)
	{
		int length = 0;

		for (final String string : strings)
		{
			length += string == null ? 4 : string.length();
		}

		return length;
	}

	public static String getTraceString(StackTraceElement[] trace)
	{
		final TextBuilder sbString = TextBuilder.newInstance();
		for (final StackTraceElement element : trace)
		{
			sbString.append(element.toString()).append("\n");
		}

		String result = sbString.toString();
		TextBuilder.recycle(sbString);
		return result;
	}

	public static String leftPad(final String string, final int length)
	{
		if (string.length() >= length)
			return string;

		final StringBuilder sbString = new StringBuilder(length);

		for (int i = string.length(); i < length; i++)
		{
			sbString.append(' ');
		}

		sbString.append(string);

		return sbString.toString();
	}

	public static String rightPad(final String string, final int length)
	{
		if (string.length() >= length)
			return string;

		final StringBuilder sbString = new StringBuilder(length);
		sbString.append(string);

		for (int i = string.length(); i < length; i++)
		{
			sbString.append(' ');
		}

		return sbString.toString();
	}

	public static StringBuilder startAppend(final int sizeHint, final String... strings)
	{
		final int length = getLength(strings);
		final StringBuilder sbString = new StringBuilder(sizeHint > length ? sizeHint : length);

		for (final String string : strings)
		{
			sbString.append(string);
		}

		return sbString;
	}

	private StringUtil()
	{

	}
}
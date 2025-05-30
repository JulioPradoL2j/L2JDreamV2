package com.dream.util;

import java.lang.reflect.Array;

public final class ArrayUtils
{
	public static final int INDEX_NOT_FOUND = -1;

	public static final long[] EMPTY_LONG_ARRAY = new long[0];

	public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];

	public static final int[] EMPTY_INT_ARRAY = new int[0];

	public static final char[] EMPTY_CHAR_ARRAY = new char[0];

	public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

	public static int[] add(int[] array, int element)
	{
		int arrayLength = array == null ? 1 : array.length + 1;
		int[] newArray = new int[arrayLength];
		if (array != null)
		{
			for (int i = 0; i < array.length; i++)
			{
				newArray[i] = array[i];
			}
		}
		newArray[arrayLength - 1] = element;
		return newArray;
	}

	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	public static <T> T[] add(T[] array, T element)
	{
		Class type = array != null ? array.getClass().getComponentType() : element != null ? element.getClass() : Object.class;
		T[] newArray = (T[]) copyArrayGrow(array, type);
		newArray[newArray.length - 1] = element;
		return newArray;
	}

	public static boolean contains(int[] array, int valueToFind)
	{
		return indexOf(array, valueToFind) != -1;
	}

	public static <T> boolean contains(T[] array, T value)
	{
		if (array == null)
			return false;

		for (T element : array)
			if (value == element)
				return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] copyArrayGrow(T[] array, Class<? extends T> type)
	{
		if (array != null)
		{
			int arrayLength = Array.getLength(array);
			T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
			System.arraycopy(array, 0, newArray, 0, arrayLength);
			return newArray;
		}
		return (T[]) Array.newInstance(type, 1);
	}

	public static int indexOf(int[] array, int valueToFind)
	{
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(int[] array, int valueToFind, int startIndex)
	{
		if (array == null)
			return -1;
		if (startIndex < 0)
		{
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++)
			if (valueToFind == array[i])
				return i;
		return -1;
	}

	public static <T> int indexOf(T[] array, T value, int index)
	{
		if (index < 0 || array.length <= index)
			return INDEX_NOT_FOUND;

		for (int i = index; i < array.length; i++)
			if (value == array[i])
				return i;
		return INDEX_NOT_FOUND;
	}

	public static int[] join(int[][] array)
	{
		if (array == null)
			return null;

		int newSize = 0;
		int idx = 0;

		for (int[] a : array)
		{
			newSize += a.length;
		}

		int[] newArray = (int[]) Array.newInstance(int.class, newSize);

		for (int[] a : array)
		{
			for (int element : a)
			{
				newArray[idx++] = element;
			}
		}

		return newArray;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] join(T[][] array)
	{
		if (array == null)
			return null;

		int newSize = 0;
		int idx = 0;

		for (T[] a : array)
		{
			newSize += a.length;
		}

		T[] newArray = (T[]) Array.newInstance(array[0].getClass().getComponentType(), newSize);

		for (T[] a : array)
		{
			for (T element : a)
			{
				newArray[idx++] = element;
			}
		}

		return newArray;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] remove(T[] array, int index)
	{
		if (array == null)
			return null;

		if (index < 0 || index >= array.length)
			return array;

		int length = array.length;

		T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), length - 1);
		System.arraycopy(array, 0, newArray, 0, index);
		if (index < length - 1)
		{
			System.arraycopy(array, index + 1, newArray, index, length - index - 1);
		}
		return newArray;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] remove(T[] array, T value)
	{
		if (array == null)
			return null;

		int index = indexOf(array, value, 0);
		if (index == INDEX_NOT_FOUND)
			return array;

		int length = array.length;

		T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), length - 1);
		System.arraycopy(array, 0, newArray, 0, index);
		if (index < length - 1)
		{
			System.arraycopy(array, index + 1, newArray, index, length - index - 1);
		}
		return newArray;
	}

	@SuppressWarnings("deprecation")
	public static Character[] toObject(char[] array)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_CHARACTER_OBJECT_ARRAY;
		Character[] result = new Character[array.length];
		for (int i = 0; i < array.length; i++)
		{
			result[i] = new Character(array[i]);
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public static Long[] toObject(long[] array)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_LONG_OBJECT_ARRAY;
		Long[] result = new Long[array.length];
		for (int i = 0; i < array.length; i++)
		{
			result[i] = new Long(array[i]);
		}
		return result;
	}

	public static char[] toPrimitive(Character[] array)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_CHAR_ARRAY;
		char[] result = new char[array.length];
		for (int i = 0; i < array.length; i++)
		{
			result[i] = array[i].charValue();
		}
		return result;
	}

	public static char[] toPrimitive(Character[] array, char valueForNull)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_CHAR_ARRAY;
		char[] result = new char[array.length];
		for (int i = 0; i < array.length; i++)
		{
			Character b = array[i];
			result[i] = b == null ? valueForNull : b.charValue();
		}
		return result;
	}

	public static int[] toPrimitive(Integer[] array)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_INT_ARRAY;
		int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++)
		{
			result[i] = array[i].intValue();
		}
		return result;
	}

	public static int[] toPrimitive(Integer[] array, int valueForNull)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_INT_ARRAY;
		int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++)
		{
			Integer b = array[i];
			result[i] = b == null ? valueForNull : b.intValue();
		}
		return result;
	}

	public static long[] toPrimitive(Long[] array)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_LONG_ARRAY;
		long[] result = new long[array.length];
		for (int i = 0; i < array.length; i++)
		{
			result[i] = array[i].longValue();
		}
		return result;
	}

	public static long[] toPrimitive(Long[] array, long valueForNull)
	{
		if (array == null)
			return null;
		if (array.length == 0)
			return EMPTY_LONG_ARRAY;
		long[] result = new long[array.length];
		for (int i = 0; i < array.length; i++)
		{
			Long b = array[i];
			result[i] = b == null ? valueForNull : b.longValue();
		}
		return result;
	}

	public static <T> T valid(T[] array, int index)
	{
		if (array == null)
			return null;
		if (index < 0 || array.length <= index)
			return null;
		return array[index];
	}

}
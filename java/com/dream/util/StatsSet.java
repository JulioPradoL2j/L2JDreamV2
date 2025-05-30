/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.util;

import java.util.HashMap;
import java.util.Map;

public final class StatsSet
{

	private final Map<String, Object> _set = new HashMap<>();

	public void add(StatsSet newSet)
	{
		Map<String, Object> newMap = newSet.getSet();
		for (String key : newMap.keySet())
		{
			Object value = newMap.get(key);
			_set.put(key, value);
		}
	}

	public void clear()
	{
		_set.clear();
	}

	public boolean getBool(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Boolean value required, but not specified");
		if (val instanceof Boolean)
			return (Boolean) val;
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}

	public boolean getBool(String name, boolean deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Boolean)
			return (Boolean) val;
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}

	public byte getByte(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Byte value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).byteValue();
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}

	public byte getByte(String name, byte deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).byteValue();
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}

	public double getDouble(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Float value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}

	public double getDouble(String name, float deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
		if (enumClass.isInstance(val))
			return (T) val;
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (enumClass.isInstance(val))
			return (T) val;
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
		}
	}

	public float getFloat(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Float value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).floatValue();
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}

	public float getFloat(String name, float deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).floatValue();
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}

	public int getInteger(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).intValue();
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}

	public int getInteger(String name, int deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).intValue();
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}

	public int[] getIntegerArray(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
		{
			int[] result =
			{
				((Number) val).intValue()
			};
			return result;
		}
		int c = 0;
		String[] vals = ((String) val).split(";");
		int[] result = new int[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Integer.parseInt(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Integer value required, but found: " + val);
			}
		}
		return result;
	}

	public long getLong(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).longValue();
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}

	public long getLong(String name, int deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).longValue();
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}

	public final Map<String, Object> getSet()
	{
		return _set;
	}

	public short getShort(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Short value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).shortValue();
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}

	public short getShort(String name, short deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).shortValue();
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}

	public String getString(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			return "";
		return String.valueOf(val);
	}

	public String getString(String name, String deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		return String.valueOf(val);
	}

	public boolean hasValueFor(String name)
	{
		return _set.containsKey(name);
	}

	public void set(String name, boolean value)
	{
		_set.put(name, value);
	}

	public void set(String name, double value)
	{
		_set.put(name, value);
	}

	public void set(String name, Enum<?> value)
	{
		_set.put(name, value);
	}

	public void set(String name, int value)
	{
		_set.put(name, value);
	}

	public void set(String name, long value)
	{
		_set.put(name, value);
	}

	public void set(String name, String value)
	{
		_set.put(name, value);
	}
}
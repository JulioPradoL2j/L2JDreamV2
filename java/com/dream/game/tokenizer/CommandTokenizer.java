package com.dream.game.tokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class CommandTokenizer
{
	private final Map<Integer, String> _tokens = new HashMap<>();
	
	public CommandTokenizer(String str)
	{
		this(str, " ");
	}
	
	public CommandTokenizer(String str, String separator)
	{
		if (str == null || separator == null || str.isEmpty() || separator.isEmpty())
		{
			return;
		}
		
		for (final String s : str.split(separator))
		{
			_tokens.put(_tokens.size(), s);
		}
	}
	
	public final String getToken(int index)
	{
		return _tokens.get(index);
	}
	
	public final String getFirstToken()
	{
		return getToken(0);
	}
	
	public final String getLastToken()
	{
		return _tokens.isEmpty() ? null : _tokens.get(_tokens.size() - 1);
	}
	
	public final byte getAsByte(int index, byte defaultValue)
	{
		final String token = getToken(index);
		if (token == null || !token.matches("-?\\d+"))
		{
			return defaultValue;
		}
		
		return Byte.parseByte(token);
	}
	
	public final int getAsInteger(int index, int defaultValue)
	{
		final String token = getToken(index);
		if (token == null || !token.matches("-?\\d+"))
		{
			return defaultValue;
		}
		
		return Integer.parseInt(token);
	}
	
	public final float getAsFloat(int index, float defaultValue)
	{
		final String token = getToken(index);
		if (token == null || !token.matches("-?\\d+(\\.\\d+)?"))
		{
			return defaultValue;
		}
		
		return Float.parseFloat(token);
	}
	
	public final double getAsDouble(int index, double defaultValue)
	{
		final String token = getToken(index);
		if (token == null || !token.matches("-?\\d+(\\.\\d+)?"))
		{
			return defaultValue;
		}
		
		return Double.parseDouble(token);
	}
	
	public final long getAsLong(int index, long defaultValue)
	{
		final String token = getToken(index);
		if (token == null || !token.matches("-?\\d+"))
		{
			return defaultValue;
		}
		
		return Long.parseLong(token);
	}
	
	public final boolean getAsBoolean(int index)
	{
		final String token = getToken(index);
		return Boolean.parseBoolean(token);
	}
	
	public <E extends Enum<E>> E getAsEnum(int index, final Class<E> enumClass, final E defaultValue)
	{
		final String val = getToken(index);
		if (val != null)
		{
			try
			{
				return Enum.valueOf(enumClass, val);
			}
			catch (IllegalArgumentException e)
			{
				// Ignored
			}
		}
		return defaultValue;
	}
	
	public final int size()
	{
		return _tokens.size();
	}
	
	public final boolean isEmpty()
	{
		return _tokens.isEmpty();
	}
	
	public final String getAsString(int index)
	{
		return getToken(index);
	}
	
	public final String getRemainingTokensFromIndex(int startIndex)
	{
		if (startIndex < 0 || startIndex >= _tokens.size())
		{
			return "";
		}
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = startIndex; i < _tokens.size(); i++)
		{
			joiner.add(getToken(i));
		}
		return joiner.toString();
	}
	
	public final int countTokens()
	{
		return _tokens.size();
	}
	
	public <E extends Enum<E>> E nextEnum(final Class<E> enumClass)
	{
		return getAsEnum(1, enumClass, null);
	}
}

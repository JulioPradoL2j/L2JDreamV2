package com.dream.lang;

import com.dream.util.ObjectPool;

import javolution.text.TextBuilder;

public class L2TextBuilder extends TextBuilder
{
	private static final long serialVersionUID = -4689463504223014731L;
	
	@SuppressWarnings("unused")
	private static final ObjectPool<L2TextBuilder> POOL = new ObjectPool<L2TextBuilder>()
	{
		@Override
		protected L2TextBuilder create()
		{
			return new L2TextBuilder();
		}
		
		@Override
		protected void reset(L2TextBuilder tb)
		{
			tb.clear();
		}
	};
	
	public static L2TextBuilder newInstance()
	{
		return POOL.get();
	}
	
	public static L2TextBuilder newInstance(int capacity)
	{
		return POOL.get();
	}
	
	public static L2TextBuilder newInstance(String str)
	{
		return (L2TextBuilder) POOL.get().append(str);
	}
	
	public static void recycle(L2TextBuilder map)
	{
		POOL.store(map);
	}
	
	public L2TextBuilder()
	{
		super();
	}
	
	public L2TextBuilder(int capacity)
	{
		super(capacity);
	}
	
	public L2TextBuilder(String str)
	{
		super(str);
	}
	
	/**
	 * Returns the String representation of this object and recycles it. This builder is empty when the result is returned.
	 * @return the built String
	 */
	public String moveToString()
	{
		final String value = toString();
		
		L2TextBuilder.recycle(this);
		
		return value;
	}
}

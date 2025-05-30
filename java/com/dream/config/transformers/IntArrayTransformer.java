package com.dream.config.transformers;

import java.lang.reflect.Field;

import com.dream.config.PropertyTransformer;
import com.dream.config.TransformFactory;
import com.dream.config.TransformationException;
import com.dream.util.ArrayUtils;

public class IntArrayTransformer implements PropertyTransformer<int[]>
{
	static
	{
		TransformFactory.registerTransformer(int[].class, new IntArrayTransformer());
	}

	@Override
	public int[] transform(String value, Field field, Object... data) throws TransformationException
	{
		int[] result = {};
		for (String s : value.split(","))
		{
			try
			{
				result = ArrayUtils.add(result, Integer.valueOf(s.trim()));
			}
			catch (NumberFormatException nfe)
			{

			}
		}

		return result;
	}

}
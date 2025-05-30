package com.dream.config.transformers;

import java.lang.reflect.Field;
import java.math.BigInteger;

import com.dream.config.PropertyTransformer;
import com.dream.config.TransformFactory;
import com.dream.config.TransformationException;

public class BigIntegerTransformer implements PropertyTransformer<BigInteger>
{
	static
	{
		TransformFactory.registerTransformer(BigInteger.class, new BigIntegerTransformer());
	}

	@Override
	public BigInteger transform(String value, Field field, Object... data) throws TransformationException
	{
		return new BigInteger(value);
	}

}

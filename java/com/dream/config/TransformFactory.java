package com.dream.config;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.dream.config.transformers.BooleanTransformer;
import com.dream.config.transformers.ByteTransformer;
import com.dream.config.transformers.CharTransformer;
import com.dream.config.transformers.ClassTransformer;
import com.dream.config.transformers.DoubleTransformer;
import com.dream.config.transformers.EnumTransformer;
import com.dream.config.transformers.FileTransformer;
import com.dream.config.transformers.FloatTransformer;
import com.dream.config.transformers.InetSocketAddressTransformer;
import com.dream.config.transformers.IntegerTransformer;
import com.dream.config.transformers.LongTransformer;
import com.dream.config.transformers.ShortTransformer;
import com.dream.config.transformers.StringTransformer;

public class TransformFactory
{
	private static Map<Class<?>, PropertyTransformer<?>> _transformers = new HashMap<>();

	public static PropertyTransformer<?> getTransformer(Field f) throws TransformationException
	{
		Class<?> clazzToTransform = f.getType();
		if (clazzToTransform == Boolean.class || clazzToTransform == Boolean.TYPE)
			return BooleanTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Byte.class || clazzToTransform == Byte.TYPE)
			return ByteTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Character.class || clazzToTransform == Character.TYPE)
			return CharTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Double.class || clazzToTransform == Double.TYPE)
			return DoubleTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Float.class || clazzToTransform == Float.TYPE)
			return FloatTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Integer.class || clazzToTransform == Integer.TYPE)
			return IntegerTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Long.class || clazzToTransform == Long.TYPE)
			return LongTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Short.class || clazzToTransform == Short.TYPE)
			return ShortTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == String.class)
			return StringTransformer.SHARED_INSTANCE;
		else if (clazzToTransform.isEnum())
			return EnumTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == File.class)
			return FileTransformer.SHARED_INSTANCE;
		else if (InetSocketAddress.class.isAssignableFrom(clazzToTransform))
			return InetSocketAddressTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Class.class)
			return ClassTransformer.SHARED_INSTANCE;
		else
		{

			if (_transformers.containsKey(clazzToTransform))
				return _transformers.get(clazzToTransform);
			for (Class<?> clazz : _transformers.keySet())
				if (clazz.isAssignableFrom(clazzToTransform))
					return _transformers.get(clazz);
			throw new TransformationException("No transformer registred for class " + clazzToTransform.getName());
		}

	}

	public static void registerTransformer(Class<?> clazz, PropertyTransformer<?> transformer)
	{
		_transformers.put(clazz, transformer);
	}

}
package com.dream.data;

import com.dream.config.PropertyTransformer;
import com.dream.config.TransformFactory;
import com.dream.util.StatsSet;

import java.lang.reflect.Field;

public class SetInitiated
{
	protected String getTableVal(String val)
	{
		return val;
	}

	@SuppressWarnings("deprecation")
	protected void load(StatsSet set)
	{
		Class<?> cl = getClass();
		while (cl != SetInitiated.class)
		{
			for (Field f : cl.getDeclaredFields())
			{
				String name = f.getName();
				if (name.startsWith("_"))
				{
					name = name.substring(1);
				}
				if (set.hasValueFor(name))
				{
					boolean access = f.isAccessible();
					f.setAccessible(true);
					PropertyTransformer<?> t = TransformFactory.getTransformer(f);
					if (t != null)
					{
						try
						{
							String val = set.getString(name);
							if (val.startsWith("#"))
							{
								val = getTableVal(val);
							}
							f.set(this, t.transform(val, f));
						}
						catch (Exception e)
						{

						}
					}
					f.setAccessible(access);

				}
			}
			cl = cl.getSuperclass();
		}
	}
}
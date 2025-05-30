/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 * aion-emu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-emu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.config.transformers;

import java.lang.reflect.Field;

import com.dream.config.PropertyTransformer;
import com.dream.config.TransformationException;

public class EnumTransformer implements PropertyTransformer<Enum<?>>
{
	public static final EnumTransformer SHARED_INSTANCE = new EnumTransformer();

	@Override
	@SuppressWarnings("unchecked")
	public Enum<?> transform(String value, Field field, Object... data) throws TransformationException
	{
		@SuppressWarnings("rawtypes")
		Class<? extends Enum> clazz = (Class<? extends Enum>) field.getType();

		try
		{
			try
			{
				int val = Integer.parseInt(value);
				for (Enum<?> e : clazz.getEnumConstants())
					if (e.ordinal() == val)
						return e;
			}
			catch (NumberFormatException nfe)
			{

			}
			for (Enum<?> e : clazz.getEnumConstants())
				if (e.name().toUpperCase().equals(value.toUpperCase()))
					return e;
			return null;
		}
		catch (Exception e)
		{
			throw new TransformationException(e);
		}
	}
}
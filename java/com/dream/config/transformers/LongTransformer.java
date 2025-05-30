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

public class LongTransformer implements PropertyTransformer<Long>
{
	public static final LongTransformer SHARED_INSTANCE = new LongTransformer();

	@Override
	public Long transform(String value, Field field, Object... data) throws TransformationException
	{
		try
		{
			return Long.decode(value);
		}
		catch (Exception e)
		{
			throw new TransformationException(e);
		}
	}
}
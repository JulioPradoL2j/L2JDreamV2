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
package com.dream.lang;

import com.dream.util.ObjectPool;

import javolution.text.TextBuilder;

/**
 * @author NB4L1
 */
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

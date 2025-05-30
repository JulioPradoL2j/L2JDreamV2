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
package com.dream.game.model.actor.instance;

public class L2RecipeStatInstance
{
	public static enum statType
	{
		HP,
		MP,
		XP,
		SP,
		GIM
	}

	private statType _type;
	private final int _value;

	public L2RecipeStatInstance(String type, int value)
	{
		try
		{
			_type = Enum.valueOf(statType.class, type);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException();
		}
		_value = value;
	}

	public statType getType()
	{
		return _type;
	}

	public int getValue()
	{
		return _value;
	}
}
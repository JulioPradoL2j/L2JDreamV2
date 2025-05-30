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
package com.dream.game.templates;

import com.dream.util.StatsSet;

public class L2HelperBuff
{
	private int _lowerLevel;

	private int _upperLevel;

	private int _skillID;

	private int _skillLevel;

	private boolean _isMagicClass;

	public L2HelperBuff(StatsSet set)
	{

		_lowerLevel = set.getInteger("lowerLevel");
		_upperLevel = set.getInteger("upperLevel");
		_skillID = set.getInteger("skillID");
		_skillLevel = set.getInteger("skillLevel");

		if ("false".equals(set.getString("isMagicClass")))
		{
			_isMagicClass = false;
		}
		else
		{
			_isMagicClass = true;
		}

	}

	public int getLowerLevel()
	{
		return _lowerLevel;
	}

	public int getSkillID()
	{
		return _skillID;
	}

	public int getSkillLevel()
	{
		return _skillLevel;
	}

	public int getUpperLevel()
	{
		return _upperLevel;
	}

	public boolean isMagicClassBuff()
	{
		return _isMagicClass;
	}

	public void setIsMagicClass(boolean isMagicClass)
	{
		_isMagicClass = isMagicClass;
	}

	public void setLowerLevel(int lowerLevel)
	{
		_lowerLevel = lowerLevel;
	}

	public void setSkillID(int skillID)
	{
		_skillID = skillID;
	}

	public void setSkillLevel(int skillLevel)
	{
		_skillLevel = skillLevel;
	}

	public void setUpperLevel(int upperLevel)
	{
		_upperLevel = upperLevel;
	}
}
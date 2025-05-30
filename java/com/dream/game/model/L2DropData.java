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
package com.dream.game.model;

import java.util.Arrays;

public class L2DropData
{
	public static final int MAX_CHANCE = 1000000;

	private int _itemId;
	private int _minDrop;
	private int _maxDrop;
	private int _chance;
	private String _questID = null;
	private String[] _stateID = null;
	private int _category;

	public void addStates(String[] list)
	{
		_stateID = list;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof L2DropData)
		{
			L2DropData drop = (L2DropData) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}

	public int getCategory()
	{
		return _category;
	}

	public int getChance()
	{
		return _chance;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getMaxDrop()
	{
		return _maxDrop;
	}

	public int getMinDrop()
	{
		return _minDrop;
	}

	public String getQuestID()
	{
		return _questID;
	}

	public String[] getStateIDs()
	{
		return _stateID;
	}

	public boolean isQuestDrop()
	{
		return _questID != null && _stateID != null;
	}

	public void setCategory(int category)
	{
		_category = category;
	}

	public void setChance(int chance)
	{
		_chance = chance;
	}

	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	public void setMaxDrop(int maxDrop)
	{
		_maxDrop = maxDrop;
	}

	public void setMinDrop(int minDrop)
	{
		_minDrop = minDrop;
	}

	public void setQuestID(String questID)
	{
		_questID = questID;
	}

	@Override
	public String toString()
	{
		String out = "ItemID: " + getItemId() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
		if (isQuestDrop())
		{
			out += " QuestID: " + getQuestID() + " StateID's: " + Arrays.toString(getStateIDs());
		}

		return out;
	}
}
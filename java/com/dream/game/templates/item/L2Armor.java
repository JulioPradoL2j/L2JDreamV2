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
package com.dream.game.templates.item;

import com.dream.util.StatsSet;

public final class L2Armor extends L2Equip
{
	private final int _avoidModifier;
	private final int _pDef;
	private final int _mDef;
	private final int _mpBonus;
	private final int _hpBonus;

	public L2Armor(L2ArmorType type, StatsSet set)
	{
		super(type, set);
		_avoidModifier = set.getInteger("avoid_modify");
		_pDef = set.getInteger("p_def");
		_mDef = set.getInteger("m_def");
		_mpBonus = set.getInteger("mp_bonus", 0);
		_hpBonus = set.getInteger("hp_bonus", 0);
	}

	public final int getAvoidModifier()
	{
		return _avoidModifier;
	}

	public final int getHpBonus()
	{
		return _hpBonus;
	}

	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}

	@Override
	public L2ArmorType getItemType()
	{
		return (L2ArmorType) super._type;
	}

	public final int getMDef()
	{
		return _mDef;
	}

	public final int getMpBonus()
	{
		return _mpBonus;
	}

	public final int getPDef()
	{
		return _pDef;
	}
}
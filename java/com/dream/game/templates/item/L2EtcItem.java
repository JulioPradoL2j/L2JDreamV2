package com.dream.game.templates.item;

import com.dream.util.StatsSet;

public final class L2EtcItem extends L2Item
{
	private final String[] _skill;

	public L2EtcItem(L2EtcItemType type, StatsSet set)
	{
		super(type, set);
		_skill = set.getString("skill").split(";");
	}

	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}

	@Override
	public L2EtcItemType getItemType()
	{
		return (L2EtcItemType) super._type;
	}

	public String[] getSkills()
	{
		return _skill;
	}

	@Override
	public final boolean isConsumable()
	{
		return getItemType() == L2EtcItemType.SHOT || getItemType() == L2EtcItemType.POTION;
	}
}
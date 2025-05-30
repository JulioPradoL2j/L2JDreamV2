package com.dream.game.model;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.tools.random.Rnd;

public class L2DropCategory
{
	private final List<L2DropData> _drops;
	private int _categoryChance;
	private int _categoryBalancedChance;
	private final int _categoryType;

	public L2DropCategory(int categoryType)
	{
		_categoryType = categoryType;
		_drops = new ArrayList<>(0);
		_categoryChance = 0;
		_categoryBalancedChance = 0;
	}

	public void addDropData(L2DropData drop, boolean raid)
	{
		boolean found = false;

		if (drop.isQuestDrop())
		{

		}
		else if (!found)
		{
			_drops.add(drop);
			_categoryChance += drop.getChance();
			// for drop selection inside a category: max 100 % chance for getting an item, scaling all values to that.
			_categoryBalancedChance += Math.min(drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS), L2DropData.MAX_CHANCE);
		}
	}

	public void clearAllDrops()
	{
		_drops.clear();
	}

	public synchronized L2DropData dropOne(boolean raid)
	{
		int randomIndex = Rnd.get(getCategoryBalancedChance());
		int sum = 0;
		for (L2DropData drop : getAllDrops())
		{
			sum += Math.min(drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS), L2DropData.MAX_CHANCE);

			if (sum >= randomIndex)
				return drop;
		}
		return null;
	}

	public synchronized L2DropData dropSeedAllowedDropsOnly()
	{
		List<L2DropData> drops = new ArrayList<>();
		int subCatChance = 0;
		for (L2DropData drop : getAllDrops())
			if (drop.getItemId() == 57 || drop.getItemId() == 5575 || drop.getItemId() == 6360 || drop.getItemId() == 6361 || drop.getItemId() == 6362)
			{
				drops.add(drop);
				subCatChance += drop.getChance();
			}

		int randomIndex = Rnd.get(subCatChance);
		int sum = 0;
		for (L2DropData drop : drops)
		{
			sum += drop.getChance();

			if (sum > randomIndex)
			{
				drops.clear();
				drops = null;
				return drop;
			}
		}
		return null;
	}

	public List<L2DropData> getAllDrops()
	{
		return _drops;
	}

	public int getCategoryBalancedChance()
	{
		if (getCategoryType() >= 0)
			return _categoryBalancedChance;

		return L2DropData.MAX_CHANCE;
	}

	public int getCategoryChance()
	{
		if (getCategoryType() >= 0)
			return _categoryChance;

		return L2DropData.MAX_CHANCE;
	}

	public int getCategoryType()
	{
		return _categoryType;
	}

	public boolean isSweep()
	{
		return getCategoryType() == -1;
	}
}
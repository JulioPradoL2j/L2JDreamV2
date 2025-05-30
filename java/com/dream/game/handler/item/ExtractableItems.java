package com.dream.game.handler.item;

import com.dream.game.datatables.xml.ExtractableItemsData;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2ExtractableItem;
import com.dream.game.model.L2ExtractableProductItem;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class ExtractableItems implements IItemHandler
{
	@Override
	public int[] getItemIds()
	{
		return ExtractableItemsData.getInstance().itemIDs();
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		int itemId = item.getItemId();
		L2Skill skill = null;
		L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(itemId);
		for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
		{
			skill = expi.getSkill();
			if (skill != null)
			{
				activeChar.useMagic(skill, false, false);
			}
			return;
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
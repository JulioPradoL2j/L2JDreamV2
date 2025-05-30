package com.dream.game.handler.item;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2FeedableBeastInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class BeastSpice implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6643,
		6644
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		if (!(activeChar.getTarget() instanceof L2FeedableBeastInstance))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		int itemId = item.getItemId();
		if (itemId == 6643)
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(2188, 1), false, false);
		}
		else if (itemId == 6644)
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(2189, 1), false, false);
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
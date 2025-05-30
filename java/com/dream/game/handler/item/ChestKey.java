package com.dream.game.handler.item;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class ChestKey implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;

	private static final int[] ITEM_IDS =
	{
		6665,
		6666,
		6667,
		6668,
		6669,
		6670,
		6671,
		6672
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
		int itemId = item.getItemId();
		L2Skill skill = SkillTable.getInstance().getInfo(2229, itemId - 6664);
		L2Object target = activeChar.getTarget();

		if (!(target instanceof L2ChestInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			L2ChestInstance chest = (L2ChestInstance) target;
			if (chest.isDead() || chest.isInteracted())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			activeChar.useMagic(skill, false, false);
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
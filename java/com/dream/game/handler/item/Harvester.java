package com.dream.game.handler.item;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;

public class Harvester implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5125
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

		if (CastleManorManager.getInstance().isDisabled())
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();

		if (target instanceof L2MonsterInstance && ((L2Character) target).isDead())
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(2098, 1), false, false);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
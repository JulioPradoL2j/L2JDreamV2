package com.dream.game.handler.item;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;

public class Firework implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6403,
		6406,
		6407
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();
		int skillId = -1;

		if (!FloodProtector.tryPerformAction(activeChar, Protected.FIREWORK))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			return;
		}

		if (activeChar.isCastingNow() || activeChar.inObserverMode() || activeChar.isSitting() || activeChar.isConfused() || activeChar.isStunned() || activeChar.isDead() || activeChar.isAlikeDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		switch (itemId)
		{
			case 6403: // Elven Firecracker
				skillId = 2023; // elven_firecracker, xml: 2023
				break;
			case 6406: // Firework
				skillId = 2024; // firework, xml: 2024
				break;
			case 6407: // Large Firework
				skillId = 2025; // large_firework, xml: 2025
				break;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		activeChar.sendPacket(new MagicSkillUse(playable, activeChar, skillId, 1, 1, 0, false));
		activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, skillId, 1, 1, 0, false));

		if (skill != null)
		{
			activeChar.useMagic(skill, false, false);
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
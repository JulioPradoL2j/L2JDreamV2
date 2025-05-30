package com.dream.game.handler.item;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.SystemMessage;

public class EnergyStone implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5589
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
		if (playable instanceof L2PcInstance)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if (playable instanceof L2PetInstance)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		else
			return;

		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}

		if (activeChar.getCharges() == 2 || activeChar.getCharges() > 2)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Skill skill = activeChar.getChargeSkill();
		if (skill == null)
		{
			// Player is not a charger class
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(5589));
		}
		else
		{
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			activeChar.doCast(SkillTable.getInstance().getInfo(2165, 1));
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
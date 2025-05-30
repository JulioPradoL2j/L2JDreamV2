package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SquashInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.templates.chars.L2NpcTemplate;

public class SquashSeed implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6389,
		6390
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
		if (!Config.BIGSQUASH_USE_SEEDS)
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_CANNOT_GROW_PUMPKIN));
			return;
		}
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false))
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}
		L2NpcTemplate squashTemplate = null;
		L2SquashInstance squash = null;
		switch (item.getItemId())
		{
			case 6389:
				squashTemplate = NpcTable.getInstance().getTemplate(12774);
				squash = new L2SquashInstance(IdFactory.getInstance().getNextId(), squashTemplate, activeChar);
				squash.getStatus().setCurrentHpMp(1, 1);
				squash.getStatus().stopHpMpRegeneration();
				squash.setLevel(activeChar.getLevel());
				squash.spawnMe(activeChar.getX() + 5, activeChar.getY() + 5, activeChar.getZ());
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_SQUASH_SUCCESS_GROW));
				break;
			case 6390:
				squashTemplate = NpcTable.getInstance().getTemplate(12777);
				squash = new L2SquashInstance(IdFactory.getInstance().getNextId(), squashTemplate, activeChar);
				squash.getStatus().setCurrentHpMp(1, 1);
				squash.getStatus().stopHpMpRegeneration();
				squash.setLevel(activeChar.getLevel());
				squash.spawnMe(activeChar.getX() + 5, activeChar.getY() + 5, activeChar.getZ());
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_SQUASH_SUCCESS_GROW));
				break;
		}

	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
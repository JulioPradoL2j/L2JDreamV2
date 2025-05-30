package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExAutoSoulShot;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.util.Broadcast;

public class SoulShots implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5789,
		1835,
		1463,
		1464,
		1465,
		1466,
		1467,
		1467,
		1467
	};
	private static final int[] SKILL_IDS =
	{
		2039,
		2150,
		2151,
		2152,
		2153,
		2154,
		2154,
		2154
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		useItem(playable, item, true);
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean animation)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		int itemId = item.getItemId();

		if (weaponInst == null || weaponItem.getSoulShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().containsKey(itemId))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
			}
			return;
		}

		if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			return;

		int weaponGrade = weaponItem.getCrystalType();
		if (weaponGrade == L2Item.CRYSTAL_NONE && itemId != 5789 && itemId != 1835 || weaponGrade == L2Item.CRYSTAL_D && itemId != 1463 || weaponGrade == L2Item.CRYSTAL_C && itemId != 1464 || weaponGrade == L2Item.CRYSTAL_B && itemId != 1465 || weaponGrade == L2Item.CRYSTAL_A && itemId != 1466 || weaponGrade == L2Item.CRYSTAL_S && itemId != 1467 || weaponGrade == L2Item.CRYSTAL_R && itemId != 1467 || weaponGrade == L2Item.CRYSTAL_S80 && itemId != 1467 || weaponGrade == L2Item.CRYSTAL_S84 && itemId != 1467)
		{
			if (!activeChar.getAutoSoulShot().containsKey(itemId))
			{
				activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
			}
			return;
		}
		int saSSCount = (int) activeChar.getStat().calcStat(Stats.SOULSHOT_COUNT, 0, null, null);
		int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;

		if (Config.CONSUME_SPIRIT_SOUL_SHOTS)
			if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
			{
				if (activeChar.getAutoSoulShot().containsKey(itemId))
				{
					activeChar.removeAutoSoulShot(itemId);
					activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItem().getName()));
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
				}
				return;
			}

		if (saSSCount > 0)
		{
			activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_MISER_CONSUME), saSSCount));
		}

		weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT, false);
		if (animation)
		{
			activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
			Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, SKILL_IDS[weaponGrade], 1, 0, 0, false), 360000);
		}
	}
}
package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExAutoSoulShot;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.util.Broadcast;

public class SpiritShot implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5790,
		2509,
		2510,
		2511,
		2512,
		2513,
		2514,
		2514
	};
	private static final int[] SKILL_IDS =
	{
		2061,
		2155,
		2156,
		2157,
		2158,
		2159,
		2159
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

		if (weaponInst == null || weaponItem.getSpiritShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().containsKey(itemId))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
			}
			return;
		}

		if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
			return;

		int weaponGrade = weaponItem.getCrystalType();
		if (weaponGrade == L2Item.CRYSTAL_NONE && itemId != 5790 && itemId != 2509 || weaponGrade == L2Item.CRYSTAL_D && itemId != 2510 || weaponGrade == L2Item.CRYSTAL_C && itemId != 2511 || weaponGrade == L2Item.CRYSTAL_B && itemId != 2512 || weaponGrade == L2Item.CRYSTAL_A && itemId != 2513 || weaponGrade == L2Item.CRYSTAL_S && itemId != 2514 || weaponGrade == L2Item.CRYSTAL_R && itemId != 2514 || weaponGrade == L2Item.CRYSTAL_S80 && itemId != 2514 || weaponGrade == L2Item.CRYSTAL_S84 && itemId != 2514)
		{
			if (!activeChar.getAutoSoulShot().containsKey(itemId))
			{
				activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
			}
			return;
		}

		if (Config.CONSUME_SPIRIT_SOUL_SHOTS)
			if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
			{
				if (activeChar.getAutoSoulShot().containsKey(itemId))
				{
					activeChar.removeAutoSoulShot(itemId);
					activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItem().getName()));
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
				}
				return;
			}

		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
		if (animation)
		{
			activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
			Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, SKILL_IDS[weaponGrade], 1, 0, 0, false), 360000);
		}
	}
}
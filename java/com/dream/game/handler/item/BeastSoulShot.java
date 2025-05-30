package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2BabyPetInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
//import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExAutoSoulShot;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.util.Broadcast;

public class BeastSoulShot implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6645
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
		if (playable == null)
			return;

		L2PcInstance activeOwner = null;
		if (playable instanceof L2Summon)
		{
			activeOwner = ((L2Summon) playable).getOwner();
			activeOwner.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return;
		}
		else if (playable instanceof L2PcInstance)
		{
			activeOwner = (L2PcInstance) playable;
		}

		if (activeOwner == null)
			return;

		L2Summon activePet = activeOwner.getPet();

		if (activePet == null)
		{
			activeOwner.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return;
		}

		if (activePet.isDead())
		{
			activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
			return;
		}

		int itemId = 6645;
		int shotConsumption = 1;
		L2ItemInstance weaponInst = null;
		L2Weapon weaponItem = null;

		if (activePet instanceof L2PetInstance && !(activePet instanceof L2BabyPetInstance))
		{
			weaponInst = activePet.getActiveWeaponInstance();
			weaponItem = activePet.getActiveWeaponItem();

			if (weaponInst == null)
			{
				activeOwner.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
				return;
			}

			if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
				return;

			int shotCount = item.getCount();
			shotConsumption = weaponItem.getSoulShotCount();

			if (shotConsumption == 0)
			{
				activeOwner.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
				return;
			}

			if (!(shotCount > shotConsumption))
			{
				activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
				return;
			}

			weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT, false);
		}
		else
		{
			if (activePet.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
				return;

			activePet.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT);
		}

		if (!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
		{
			if (activeOwner.getAutoSoulShot().containsKey(itemId))
			{
				activeOwner.removeAutoSoulShot(itemId);
				activeOwner.sendPacket(new ExAutoSoulShot(itemId, 0));
				activeOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItem().getName()));
				return;
			}

			activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
			return;
		}
		if (animation)
		{
			activeOwner.sendPacket(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT);

			Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, 2033, 1, 0, 0, false), 360000);
		}
	}
}
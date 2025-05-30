package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2BabyPetInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExAutoSoulShot;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.util.Broadcast;

public class BeastSpiritShot implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6646,
		6647
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

		L2PcInstance activeOwner = playable.getActingPlayer();

		if (item.getItemId() == 6647 && activeOwner.isInOlympiadMode())
		{
			activeOwner.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

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

		int itemId = item.getItemId();
		boolean isBlessed = itemId == 6647;
		int shotConsumption = 1;

		L2ItemInstance weaponInst = null;
		L2Weapon weaponItem = null;

		if (activePet instanceof L2PetInstance && !(activePet instanceof L2BabyPetInstance))
		{
			weaponInst = activePet.getActiveWeaponInstance();
			weaponItem = activePet.getActiveWeaponItem();

			if (weaponInst == null)
			{
				activeOwner.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
				return;
			}
			if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
				return;

			int shotCount = item.getCount();
			shotConsumption = weaponItem.getSpiritShotCount();
			if (shotConsumption == 0)
			{
				activeOwner.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
				return;
			}
			if (!(shotCount > shotConsumption))
			{
				activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET);
				return;
			}
			if (isBlessed)
			{
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			}
			else
			{
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
			}
		}
		else
		{
			if (activePet.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
				return;
			if (isBlessed)
			{
				activePet.setChargedSpiritShot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			}
			else
			{
				activePet.setChargedSpiritShot(L2ItemInstance.CHARGED_SPIRITSHOT);
			}
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
			activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
			return;
		}
		if (animation)
		{
			activeOwner.sendPacket(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT);
			Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, isBlessed ? 2009 : 2008, 1, 0, 0, false), 360000);
		}
	}
}
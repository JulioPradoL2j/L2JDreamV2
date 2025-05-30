package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.handler.ItemHandler;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.PetItemList;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2ArmorType;
import com.dream.game.templates.item.L2Item;

public class RequestPetUseItem extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestPetUseItem.class.getName());

	private synchronized static void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				switch (item.getItem().getBodyPart())
				{
					case L2Item.SLOT_R_HAND:
						pet.setWeapon(0);
						break;
					case L2Item.SLOT_CHEST:
						pet.setArmor(0);
						break;
					case L2Item.SLOT_NECK:
						pet.setJewel(0);
						break;
				}
			}
			else
			{
				pet.getInventory().equipItem(item);
				switch (item.getItem().getBodyPart())
				{
					case L2Item.SLOT_R_HAND:
						pet.setWeapon(item.getItemId());
						break;
					case L2Item.SLOT_CHEST:
						pet.setArmor(item.getItemId());
						break;
					case L2Item.SLOT_NECK:
						pet.setJewel(item.getItemId());
						break;
				}
			}
			activeChar.sendPacket(new PetItemList(pet));
			pet.broadcastFullInfo();
		}
		else
		{
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());

			if (handler == null)
			{
				_log.warn("no itemhandler registered for itemId:" + item.getItemId());
			}
			else
			{
				handler.useItem(pet, item);
				pet.broadcastFullInfo();
			}
		}
	}

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
		activeChar._bbsMultisell = 0;
		L2PetInstance pet = (L2PetInstance) activeChar.getPet();

		if (pet == null)
			return;

		L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

		if (item == null)
			return;

		if (item.isWear())
			return;

		if (activeChar.isAlikeDead() || pet.isDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			return;
		}

		if (!item.isEquipped())
			if (!item.getItem().checkCondition(pet, pet, true))
				return;

		if (item.getItem().getBodyPart() == L2Item.SLOT_NECK)
			if (item.getItem().getItemType() == L2ArmorType.PET)
			{
				useItem(pet, item, activeChar);
				return;
			}

		if (PetDataTable.isWolf(pet.getNpcId()) && item.getItem().isForWolf() || PetDataTable.isHatchling(pet.getNpcId()) && item.getItem().isForHatchling() || PetDataTable.isBaby(pet.getNpcId()) && item.getItem().isForBabyPet() || PetDataTable.isStrider(pet.getNpcId()) && item.getItem().isForStrider() || PetDataTable.isImprovedBaby(pet.getNpcId()) && item.getItem().isForBabyPet())
		{
			useItem(pet, item, activeChar);
			return;
		}

		IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());

		if (handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
		}
	}

}
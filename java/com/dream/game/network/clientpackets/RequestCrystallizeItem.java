package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.PcInventory;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;

public class RequestCrystallizeItem extends L2GameClientPacket
{
	private int _objectId, _count;
	private PcInventory inventory;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_CREATEITEM && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			activeChar.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}

		if (_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to crystalize the subject, crystals < 0!", IllegalPlayerAction.PUNISH_KICK);
			return;
		}

		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		inventory = activeChar.getInventory();
		if (inventory != null)
		{
			L2ItemInstance item = inventory.getItemByObjectId(_objectId);

			if (item == null || item.isWear())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (item.isHeroItem())
				return;
			if (_count > item.getCount())
			{
				_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
			}
		}

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if (itemToRemove == null || itemToRemove.isWear() || itemToRemove.isShadowItem())
			return;

		if (!itemToRemove.getItem().isCrystallizable() || itemToRemove.getItem().getCrystalCount() <= 0 || itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_NONE)
			return;

		boolean canCrystallize = true;
		switch (itemToRemove.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_C:
			{
				if (skillLevel <= 1)
				{
					canCrystallize = false;
				}
				break;
			}
			case L2Item.CRYSTAL_B:
			{
				if (skillLevel <= 2)
				{
					canCrystallize = false;
				}
				break;
			}
			case L2Item.CRYSTAL_A:
			{
				if (skillLevel <= 3)
				{
					canCrystallize = false;
				}
				break;
			}
			case L2Item.CRYSTAL_S:
			{
				if (skillLevel <= 4)
				{
					canCrystallize = false;
				}
				break;
			}
			case L2Item.CRYSTAL_R:
			{
				if (skillLevel <= 4)
				{
					canCrystallize = false;
				}
				break;
			}
			case L2Item.CRYSTAL_S80:
			{
				if (skillLevel <= 4)
				{
					canCrystallize = false;
				}
				break;
			}
			case L2Item.CRYSTAL_S84:
			{
				if (skillLevel <= 4)
				{
					canCrystallize = false;
				}
				break;
			}
		}

		if (!canCrystallize)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.setInCrystallize(true);

		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped = inventory.unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			if (itemToRemove.getEnchantLevel() > 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(itemToRemove.getEnchantLevel()).addItemName(itemToRemove));
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(itemToRemove));
			}
		}

		L2ItemInstance removedItem = inventory.destroyItem("Crystalize", _objectId, _count, activeChar, null);
		if (removedItem == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int crystalId = itemToRemove.getItem().getCrystalItemId();
		int crystalAmount = itemToRemove.getCrystalCount();
		L2ItemInstance createditem = inventory.addItem("Crystalize", crystalId, crystalAmount, activeChar, activeChar);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED).addItemName(removedItem));

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(createditem).addNumber(crystalAmount));
		activeChar.sendPacket(new ItemList(activeChar, false));

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

		activeChar.broadcastUserInfo();

		L2World world = L2World.getInstance();
		world.removeObject(removedItem);

		activeChar.setInCrystallize(false);
	}

}
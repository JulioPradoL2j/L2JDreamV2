package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExVariationCancelResult;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;

public final class RequestRefineCancel extends L2GameClientPacket
{
	private int _targetItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		if (targetItem == null)
		{
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}

		if (!targetItem.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}

		int price = 0;
		switch (targetItem.getItem().getItemGrade())
		{
			case L2Item.CRYSTAL_C:
				if (targetItem.getCrystalCount() < 1720)
				{
					price = 95000;
				}
				else if (targetItem.getCrystalCount() < 2452)
				{
					price = 150000;
				}
				else
				{
					price = 210000;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (targetItem.getCrystalCount() < 1746)
				{
					price = 240000;
				}
				else
				{
					price = 270000;
				}
				break;
			case L2Item.CRYSTAL_A:
				if (targetItem.getCrystalCount() < 2160)
				{
					price = 330000;
				}
				else if (targetItem.getCrystalCount() < 2824)
				{
					price = 390000;
				}
				else
				{
					price = 420000;
				}
				break;
			case L2Item.CRYSTAL_S:
				price = 480000;
				break;
			case L2Item.CRYSTAL_R:
				price = 480000;
				break;
			case L2Item.CRYSTAL_S80:
				price = 480000;
				break;
			case L2Item.CRYSTAL_S84:
				price = 480000;
				break;
			default:
				activeChar.sendPacket(new ExVariationCancelResult(0));
				return;
		}

		if (!activeChar.reduceAdena("RequestRefineCancel", price, null, true))
			return;
		if (targetItem.isEquipped())
		{
			activeChar.disarmWeapons();
		}

		targetItem.removeAugmentation();
		activeChar.sendPacket(new ExVariationCancelResult(1));
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1).addString(targetItem.getItemName()));
	}

}
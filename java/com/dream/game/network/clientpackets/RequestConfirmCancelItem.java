package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExPutItemResultForVariationCancel;
import com.dream.game.templates.item.L2Item;

public final class RequestConfirmCancelItem extends L2GameClientPacket
{
	private int _itemId;

	@Override
	protected void readImpl()
	{
		_itemId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemId);

		if (item == null)
			return;

		if (!item.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}

		int price = 0;
		switch (item.getItem().getItemGrade())
		{
			case L2Item.CRYSTAL_C:
				if (item.getCrystalCount() < 1720)
				{
					price = 95000;
				}
				else if (item.getCrystalCount() < 2452)
				{
					price = 150000;
				}
				else
				{
					price = 210000;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (item.getCrystalCount() < 1746)
				{
					price = 240000;
				}
				else
				{
					price = 270000;
				}
				break;
			case L2Item.CRYSTAL_A:
				if (item.getCrystalCount() < 2160)
				{
					price = 330000;
				}
				else if (item.getCrystalCount() < 2824)
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
				return;
		}
		activeChar.sendPacket(new ExPutItemResultForVariationCancel(_itemId, price));
	}

}
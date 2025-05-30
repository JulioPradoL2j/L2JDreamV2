package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.xml.AugmentationData;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExVariationResult;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.Util;

public final class RequestRefine extends L2GameClientPacket
{
	private static int getLifeStoneGrade(int itemId)
	{
		itemId -= 8723;
		if (itemId < 10)
			return 0;
		if (itemId < 20)
			return 1;
		if (itemId < 30)
			return 2;
		return 3;
	}

	private static int getLifeStoneLevel(int itemId)
	{
		itemId -= 10 * getLifeStoneGrade(itemId);
		itemId -= 8722;
		return itemId;
	}

	private int _targetItemObjId, _refinerItemObjId, _gemstoneItemObjId, _gemstoneCount;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		activeChar._bbsMultisell = 0;
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		L2ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

		if (targetItem == null || refinerItem == null || gemstoneItem == null || targetItem.getOwnerId() != activeChar.getObjectId() || refinerItem.getOwnerId() != activeChar.getObjectId() || gemstoneItem.getOwnerId() != activeChar.getObjectId() || activeChar.getLevel() < 46)
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		if (targetItem.isEquipped())
		{
			activeChar.disarmWeapons();
		}

		if (tryAugmentItem(activeChar, targetItem, refinerItem, gemstoneItem))
		{
			int stat12 = 0x0000FFFF & targetItem.getAugmentation().getAugmentationId();
			int stat34 = targetItem.getAugmentation().getAugmentationId() >> 16;
			activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));
			activeChar.sendPacket(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
		}
		else
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
		}
	}

	private boolean tryAugmentItem(L2PcInstance player, L2ItemInstance targetItem, L2ItemInstance refinerItem, L2ItemInstance gemstoneItem)
	{
		if (targetItem.isAugmented() || targetItem.isWear())
			return false;

		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return false;
		}
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING);
			return false;
		}
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		if (player.getInventory().getItemByObjectId(refinerItem.getObjectId()) == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong LifeStone-id.", Config.DEFAULT_PUNISH);
			return false;
		}
		if (player.getInventory().getItemByObjectId(targetItem.getObjectId()) == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong Weapon-id.", Config.DEFAULT_PUNISH);
			return false;
		}
		if (player.getInventory().getItemByObjectId(gemstoneItem.getObjectId()) == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to refine an item with wrong Gemstone-id.", Config.DEFAULT_PUNISH);
			return false;
		}

		int itemGrade = targetItem.getItem().getItemGrade();
		int lifeStoneId = refinerItem.getItemId();
		int gemstoneItemId = gemstoneItem.getItemId();

		if (lifeStoneId < 8723 || lifeStoneId > 8762)
			return false;

		if (itemGrade < L2Item.CRYSTAL_C || targetItem.getItem().getType2() != L2Item.TYPE2_WEAPON || !targetItem.isDestroyable() || targetItem.isShadowItem())
			return false;

		int modifyGemstoneCount = _gemstoneCount;
		int lifeStoneGrade = 0;
		int lifeStoneLevel;

		lifeStoneLevel = getLifeStoneLevel(lifeStoneId);
		lifeStoneGrade = getLifeStoneGrade(lifeStoneId);

		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
				if (player.getLevel() < 46 || gemstoneItemId != 2130)
					return false;
				modifyGemstoneCount = 20;
				break;
			case L2Item.CRYSTAL_B:
				if (player.getLevel() < 52 || gemstoneItemId != 2130)
					return false;
				modifyGemstoneCount = 30;
				break;
			case L2Item.CRYSTAL_A:
				if (player.getLevel() < 61 || gemstoneItemId != 2131)
					return false;
				modifyGemstoneCount = 20;
				break;
			case L2Item.CRYSTAL_S:
				if (player.getLevel() < 76 || gemstoneItemId != 2131)
					return false;
				modifyGemstoneCount = 25;
				break;
			case L2Item.CRYSTAL_R:
				if (player.getLevel() < 76 || gemstoneItemId != 2131)
					return false;
				modifyGemstoneCount = 25;
				break;
			case L2Item.CRYSTAL_S80:
				if (player.getLevel() < 80 || gemstoneItemId != 2131)
					return false;
				modifyGemstoneCount = 25;
				break;
			case L2Item.CRYSTAL_S84:
				if (player.getLevel() < 80 || gemstoneItemId != 2131)
					return false;
				modifyGemstoneCount = 25;
				break;
		}

		switch (lifeStoneLevel)
		{
			case 1:
				if (player.getLevel() < 46)
					return false;
				break;
			case 2:
				if (player.getLevel() < 49)
					return false;
				break;
			case 3:
				if (player.getLevel() < 52)
					return false;
				break;
			case 4:
				if (player.getLevel() < 55)
					return false;
				break;
			case 5:
				if (player.getLevel() < 58)
					return false;
				break;
			case 6:
				if (player.getLevel() < 61)
					return false;
				break;
			case 7:
				if (player.getLevel() < 64)
					return false;
				break;
			case 8:
				if (player.getLevel() < 67)
					return false;
				break;
			case 9:
				if (player.getLevel() < 70)
					return false;
				break;
			case 10:
				if (player.getLevel() < 76)
					return false;
				break;
		}

		if (gemstoneItem.getCount() - modifyGemstoneCount < 0)
			return false;

		if (!player.destroyItem("RequestRefine", refinerItem, 1, null, false))
			return false;

		if (!player.destroyItem("RequestRefine", gemstoneItem, modifyGemstoneCount, null, false))
			return false;

		targetItem.setAugmentation(AugmentationData.getInstance().generateRandomAugmentation(lifeStoneLevel, lifeStoneGrade));

		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		player.sendPacket(iu);

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		return true;
	}

}
package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExPutItemResultForVariationMake;
import com.dream.game.templates.item.L2Item;

public final class RequestConfirmTargetItem extends L2GameClientPacket
{
	private int _itemObjId;
	private L2ItemInstance item;

	@Override
	protected void readImpl()
	{
		_itemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		item = getClient().getActiveChar().getInventory().getItemByObjectId(_itemObjId);

		if (getClient().getActiveChar() == null)
			return;
		if (item == null)
			return;

		int itemGrade = item.getItem().getItemGrade();
		int itemType = item.getItem().getType1();

		if (item == null)
			return;
		if (getClient().getActiveChar().getLevel() < 46)
		{
			getClient().getActiveChar().sendMessage("You have to have in order to improve the level of 46.");
			return;
		}
		if (item.isAugmented())
		{
			getClient().getActiveChar().sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
			return;
		}
		else if (itemGrade < L2Item.CRYSTAL_C || itemType != L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE || !item.isDestroyable() || item.isShadowItem() || item.getItem().isCommonItem())
		{
			getClient().getActiveChar().sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		if (getClient().getActiveChar().getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			getClient().getActiveChar().sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return;
		}
		if (getClient().getActiveChar().isDead())
		{
			getClient().getActiveChar().sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return;
		}
		if (getClient().getActiveChar().isParalyzed())
		{
			getClient().getActiveChar().sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return;
		}
		if (getClient().getActiveChar().isFishing())
		{
			getClient().getActiveChar().sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return;
		}
		if (getClient().getActiveChar().isSitting())
		{
			getClient().getActiveChar().sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return;
		}
		getClient().getActiveChar().sendPacket(new ExPutItemResultForVariationMake(_itemObjId));
		getClient().getActiveChar().sendPacket(SystemMessageId.SELECT_THE_CATALYST_FOR_AUGMENTATION);
	}

}
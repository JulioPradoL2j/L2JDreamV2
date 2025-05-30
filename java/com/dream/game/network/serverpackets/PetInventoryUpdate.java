package com.dream.game.network.serverpackets;

import com.dream.game.model.ItemInfo;
import com.dream.game.model.actor.instance.L2ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class PetInventoryUpdate extends L2GameServerPacket
{
	private final List<ItemInfo> _items;

	public PetInventoryUpdate()
	{
		this(new ArrayList<>());
	}

	public PetInventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
	}

	public void addItem(L2ItemInstance item)
	{
		_items.add(new ItemInfo(item));
	}

	public void addItems(List<L2ItemInstance> items)
	{
		for (L2ItemInstance item : items)
		{
			_items.add(new ItemInfo(item));
		}
	}

	public void addModifiedItem(L2ItemInstance item)
	{
		_items.add(new ItemInfo(item, 2));
	}

	public void addNewItem(L2ItemInstance item)
	{
		_items.add(new ItemInfo(item, 1));
	}

	public void addRemovedItem(L2ItemInstance item)
	{
		_items.add(new ItemInfo(item, 3));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb3);
		int count = _items.size();
		writeH(count);
		for (ItemInfo item : _items)
		{
			writeH(item.getChange());
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemDisplayId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(item.getEquipped());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(item.getCustomType2());
		}
	}

}
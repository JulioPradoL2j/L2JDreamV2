package com.dream.game.network.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private final int _itemId;

	public ChooseInventoryItem(int Item)
	{
		_itemId = Item;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x6f);
		writeD(_itemId);
	}

}
package com.dream.game.network.serverpackets;

public class ExEnchantSkillInfoDetail extends L2GameServerPacket
{
	private final int _itemId;
	private final int _itemCount;

	public ExEnchantSkillInfoDetail(int itemId, int itemCount)
	{
		_itemId = itemId;
		_itemCount = itemCount;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x5e);

		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(_itemCount);
		writeD(0);
		writeD(_itemId);
		writeD(0);
	}

}
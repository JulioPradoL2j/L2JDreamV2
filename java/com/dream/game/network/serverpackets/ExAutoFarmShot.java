package com.dream.game.network.serverpackets;

public class ExAutoFarmShot extends L2GameServerPacket
{
	private final int _skillId;
	private final int _type;

	public ExAutoFarmShot(int itemId, int type)
	{
		_skillId = itemId;
		_type = type;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x12);
		writeD(_skillId);
		writeD(_type);
	}

}
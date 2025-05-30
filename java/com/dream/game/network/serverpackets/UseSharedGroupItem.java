package com.dream.game.network.serverpackets;

public class UseSharedGroupItem extends L2GameServerPacket
{
	private final int _itemId;
	private final int _grpId;
	private final int _remainedTime;
	private final int _totalTime;

	public UseSharedGroupItem(int itemId, int grpId, int remainedTime, int totalTime)
	{
		_itemId = itemId;
		_grpId = grpId;
		_remainedTime = remainedTime / 1000;
		_totalTime = totalTime / 1000;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(73);

		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainedTime);
		writeD(_totalTime);
	}

}
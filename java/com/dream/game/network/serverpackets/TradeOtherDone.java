package com.dream.game.network.serverpackets;

public class TradeOtherDone extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x82);
	}

}
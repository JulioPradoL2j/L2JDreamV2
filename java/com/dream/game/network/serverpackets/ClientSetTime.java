package com.dream.game.network.serverpackets;

import com.dream.game.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
	public static final ClientSetTime STATIC_PACKET = new ClientSetTime();

	private ClientSetTime()
	{
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xEC);
		writeD(GameTimeController.getGameTime());
		writeD(6);
	}

}
package com.dream.game.network.clientpackets;

public class RequestPledgeExtendedInfo extends L2GameClientPacket
{

	@SuppressWarnings("unused")
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
	}

}
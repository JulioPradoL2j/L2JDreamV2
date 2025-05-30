package com.dream.game.network.clientpackets;

class SuperCmdSummonCmd extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String _summonName;

	@Override
	protected void readImpl()
	{
		_summonName = readS();
	}

	@Override
	protected void runImpl()
	{

	}

}
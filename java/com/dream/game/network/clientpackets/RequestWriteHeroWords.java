package com.dream.game.network.clientpackets;

public class RequestWriteHeroWords extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String _heroWords;

	@Override
	protected void readImpl()
	{
		_heroWords = readS();
	}

	@Override
	protected void runImpl()
	{

	}

}
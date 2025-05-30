package com.dream.game.network.clientpackets;

import com.dream.game.manager.games.fishingChampionship;

public class RequestExFishRanking extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		fishingChampionship.getInstance().showMidResult(getClient().getActiveChar());
	}

}
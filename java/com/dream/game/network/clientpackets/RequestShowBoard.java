package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.communitybbs.CommunityBoard;

public final class RequestShowBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_unknown = readD();
	}

	@Override
	protected void runImpl()
	{
		CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
	}

}
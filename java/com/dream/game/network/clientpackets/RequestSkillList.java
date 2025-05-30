package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class RequestSkillList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();

		if (cha == null)
			return;
		cha.sendSkillList();
	}

}
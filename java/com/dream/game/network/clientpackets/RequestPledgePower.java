package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
	private int _rank, _action, _privs;

	@Override
	protected void readImpl()
	{
		_rank = readD();
		_action = readD();
		if (_action == 2)
		{
			_privs = readD();
		}
		else
		{
			_privs = 0;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (_action == 2)
		{
			if (player.getClan() != null && player.isClanLeader())
			{
				player.getClan().setRankPrivs(_rank, _privs);
			}
		}
		else
		{
			player.sendPacket(new ManagePledgePower(getClient().getActiveChar().getClan(), _action, _rank));
		}
	}

}
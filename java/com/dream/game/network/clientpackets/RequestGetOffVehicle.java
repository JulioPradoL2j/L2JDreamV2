package com.dream.game.network.clientpackets;

import com.dream.game.manager.BoatManager;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.GetOffVehicle;

public class RequestGetOffVehicle extends L2GameClientPacket
{
	private int _id, _x, _y, _z;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		L2BoatInstance boat = BoatManager.getInstance().getBoat(_id);
		activeChar.broadcastPacket(new GetOffVehicle(activeChar, boat, _x, _y, _z));
	}

}
package com.dream.game.network.clientpackets;

import com.dream.game.manager.BoatManager;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.GetOnVehicle;
import com.dream.tools.geometry.Point3D;

public class RequestGetOnVehicle extends L2GameClientPacket
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
		if (boat == null)
			return;

		activeChar.setInBoatPosition(new Point3D(_x, _y, _z));
		activeChar.getPosition().setXYZ(boat.getPosition().getX(), boat.getPosition().getY(), boat.getPosition().getZ());
		activeChar.broadcastPacket(new GetOnVehicle(activeChar, boat, _x, _y, _z));
		activeChar.revalidateZone(true);
	}

}
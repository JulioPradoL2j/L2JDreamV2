package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.StopMoveInVehicle;
import com.dream.tools.geometry.Point3D;

public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _boatId;

	@Override
	protected void readImpl()
	{
		_boatId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (player.isInBoat())
			if (player.getBoat().getObjectId() == _boatId)
			{
				player.setInBoatPosition(new Point3D(_x, _y, _z));
				player.getPosition().setHeading(_heading);
				player.broadcastPacket(new StopMoveInVehicle(player, _boatId));
			}
	}

}
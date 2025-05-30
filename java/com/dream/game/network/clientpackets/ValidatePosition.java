package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.geodata.GeoEngine;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.MoveToLocation;
import com.dream.game.network.serverpackets.PartyMemberPosition;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.network.serverpackets.ValidateLocationInVehicle;
import com.dream.game.util.Broadcast;

public class ValidatePosition extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(ValidatePosition.class);
	private int _x, _y, _z, _heading;
	@SuppressWarnings("unused")
	private int _data;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		_data = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isTeleporting())
			return;
		activeChar.checkSummon();
		int realX = activeChar.getX();
		int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		if (_x == 0 && _y == 0)
			if (realX != 0)
				return;

		activeChar.setHeading(_heading);
		double dx = _x - realX;
		double dy = _y - realY;
		double dz = _z - realZ;

		int finalZ = _z;
		if (Math.abs(dz) <= 200)
		{
			finalZ = realZ;
		}

		double diffSq = dx * dx + dy * dy;
		double speedsq = activeChar.getStat().getMoveSpeed() * activeChar.getStat().getMoveSpeed();
		if (diffSq <= speedsq * 1.5 && dz < 1500)
		{
			activeChar.setLastServerPosition(realX, realY, realZ);
			activeChar.getPosition().setXYZ(_x, _y, _z);
			if (activeChar.getParty() != null)
			{
				activeChar.setLastPartyPosition(_x, _y, _z);
				activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));
			}
			if (activeChar.isInBoat())
			{
				Broadcast.toKnownPlayers(activeChar, new ValidateLocationInVehicle(activeChar));
			}
			else
			{
				Broadcast.toKnownPlayers(activeChar, new ValidateLocation(activeChar));
			}
		}
		// COORD Client<-->Server synchronization
		switch (Config.COORD_SYNCHRONIZE)
		{

			case 1:
			{ // full synchronization Client --> Server
				// only * using this option it is difficult
				// for players to bypass obstacles

				if (!activeChar.isMoving() || !activeChar.validateMovementHeading(_heading)) // Heading changed on client = possible obstacle
				{
					// character is not moving, take coordinates from client
					if (diffSq < 2500)
					{ // 50*50 - attack won't work fluently if even small differences are corrected
						activeChar.getPosition().setXYZ(realX, realY, finalZ);

					}
					else
					{
						activeChar.getPosition().setXYZ(_x, _y, finalZ);
					}
				}
				else
				{
					activeChar.getPosition().setXYZ(realX, realY, finalZ);

				}

				activeChar.setHeading(_heading);

			}
				break;
			case 2:
			{ // full synchronization Server --> Client (bounces for validation)

				if (Config.COORD_SYNCHRONIZE > 0 && (diffSq > 250000 || Math.abs(dz) > 200))
				{
					if (Math.abs(dz) > 200)
					{

						if (Math.abs(finalZ - activeChar.getClientZ()) < 800)
						{
							activeChar.getPosition().setXYZ(realX, realY, finalZ);
						}

					}
					else
					{
						if (!activeChar.isMoving())
						{

							if (activeChar.isInBoat())
								sendPacket(new ValidateLocationInVehicle(activeChar));
							else
								sendPacket(new ValidateLocation(activeChar));

						}
						else if (diffSq > activeChar.getStat().getMoveSpeed())
							activeChar.broadcastPacket(new MoveToLocation(activeChar));

						finalZ = activeChar.getPosition().getZ();
					}

				}

			}
				break;
			case -1:
			{ // just (client-->server) Z coordination

				if (Math.abs(dz) > 200)
				{

					if (Math.abs(_z - activeChar.getClientZ()) < 800)
						activeChar.getPosition().setXYZ(realX, realY, finalZ);

				}
				else
					finalZ = realZ;

			}
				break;
			default:
			case 0:
			{ // no synchronization at all
				// the server has the correct information
				finalZ = realZ;
			}
				break;

		}

		// EXPERIMENTAL fix when players cross the floor adapted By Zeit
		int deltaZ = activeChar.getZ() - _z;
		if (deltaZ > 1024)
		{
			int zLocation = GeoEngine.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), zLocation);
			LOGGER.info("Player " + activeChar.getName() + " has fallen more than 1024 units, returned to last position (" + activeChar.getX() + ", " + activeChar.getY() + ", " + zLocation + ")");
			return;
		}

		// check water
		if (Config.ALLOW_WATER)
			activeChar.checkWaterState();

	}


}
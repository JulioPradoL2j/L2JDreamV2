/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.tools.random.Rnd;

public class Entity
{
	public static Logger _log = Logger.getLogger(Entity.class.getName());

	protected L2Zone _zone;

	public void banishForeigners()
	{
		for (L2PcInstance player : getPlayersInside())
			if (checkBanish(player))
			{
				player.teleToLocation(TeleportWhereType.Town);
			}
	}

	public void broadcastToPlayers(L2GameServerPacket gsp)
	{
		for (L2PcInstance player : getPlayersInside())
		{
			player.sendPacket(gsp);
		}
	}

	public void broadcastToPlayers(String message)
	{
		SystemMessage msg = SystemMessage.sendString(message);
		for (L2PcInstance player : getPlayersInside())
		{
			player.sendPacket(msg);
		}
	}

	protected boolean checkBanish(L2PcInstance cha)
	{
		return true;
	}

	public boolean checkIfInZone(int x, int y)
	{
		if (_zone != null)
			return _zone.isInsideZone(x, y);

		_log.error(getClassName() + " has no zone defined");
		return false;
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		if (_zone != null)
			return _zone.isInsideZone(x, y, z);

		_log.error(getClassName() + " has no zone defined");
		return false;
	}

	public boolean checkIfInZone(L2Object cha)
	{
		if (_zone != null)
			return _zone.isInsideZone(cha);

		_log.error(getClassName() + " has no zone defined");
		return false;
	}

	public int getCastleId()
	{
		if (_zone != null)
			return _zone.getCastleId();

		_log.error(getClassName() + " has no zone defined");
		return 1;
	}

	public String getClassName()
	{
		String[] parts = this.getClass().toString().split("\\.");
		return parts[parts.length - 1];
	}

	public double getDistanceToZone(int x, int y)
	{
		if (_zone != null)
			return _zone.getDistanceToZone(x, y);

		_log.error(getClassName() + " has no zone defined");
		return Double.MAX_VALUE;
	}

	public int getFortId()
	{
		if (_zone != null)
			return _zone.getFortId();

		_log.error(getClassName() + " has no zone defined");
		return 0;
	}

	public List<L2PcInstance> getPlayersInside()
	{
		List<L2PcInstance> lst = new ArrayList<>();
		for (L2Character cha : getZone().getCharactersInside().values())
			if (cha instanceof L2PcInstance)
			{
				lst.add((L2PcInstance) cha);
			}
		return lst;
	}

	public L2PcInstance getRandomPlayer()
	{
		List<L2PcInstance> lst = getPlayersInside();
		if (!lst.isEmpty())
			return lst.get(Rnd.get(lst.size()));

		return null;
	}

	public int getTownId()
	{
		if (_zone != null)
			return _zone.getTownId();

		_log.error(getClassName() + " has no zone defined");
		return 0;
	}

	public L2Zone getZone()
	{
		return _zone;
	}

	public void registerZone(L2Zone zone)
	{
		_zone = zone;
	}
}
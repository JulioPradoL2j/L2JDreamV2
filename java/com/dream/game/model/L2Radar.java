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
package com.dream.game.model;

import java.util.Vector;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.CTF.CTF;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.RadarControl;

public final class L2Radar
{
	public class RadarMarker
	{
		public int _type, _x, _y, _z;

		public RadarMarker(int x, int y, int z)
		{
			_type = 1;
			_x = x;
			_y = y;
			_z = z;
		}

		public RadarMarker(int type, int x, int y, int z)
		{
			_type = type;
			_x = x;
			_y = y;
			_z = z;
		}

		@Override
		public boolean equals(Object obj)
		{
			try
			{
				RadarMarker temp = (RadarMarker) obj;

				return temp._x == _x && temp._y == _y && temp._z == _z && temp._type == _type;
			}
			catch (Exception e)
			{
				return false;
			}
		}
	}

	public class RadarOnPlayer implements Runnable
	{
		private final L2PcInstance _myTarget, _me;

		public RadarOnPlayer(L2PcInstance target, L2PcInstance me)
		{
			_me = me;
			_myTarget = target;
		}

		@Override
		public void run()
		{
			try
			{
				if (_me == null || _me.isOnline() == 0)
					return;

				_me.sendPacket(new RadarControl(1, 1, _me.getX(), _me.getY(), _me.getZ()));
				if (_myTarget == null || _myTarget.isOnline() == 0 || _myTarget.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) == null || CTF.hasFlag(_myTarget))
					return;

				_me.sendPacket(new RadarControl(0, 1, _myTarget.getX(), _myTarget.getY(), _myTarget.getZ()));
				ThreadPoolManager.getInstance().scheduleGeneral(new RadarOnPlayer(_myTarget, _me), 15000);
			}
			catch (Throwable t)
			{
			}
		}
	}

	private final L2PcInstance _player;

	private final Vector<RadarMarker> _markers;

	public L2Radar(L2PcInstance player)
	{
		_player = player;
		_markers = new Vector<>();
	}

	public void addMarker(int x, int y, int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);

		_markers.add(newMarker);
		_player.sendPacket(new RadarControl(2, 2, x, y, z));
		_player.sendPacket(new RadarControl(0, 1, x, y, z));
	}

	public void loadMarkers()
	{
		_player.sendPacket(new RadarControl(2, 2, _player.getX(), _player.getY(), _player.getZ()));
		for (RadarMarker tempMarker : _markers)
		{
			_player.sendPacket(new RadarControl(0, 1, tempMarker._x, tempMarker._y, tempMarker._z));
		}
	}

	public void removeAllMarkers()
	{
		for (RadarMarker tempMarker : _markers)
		{
			_player.sendPacket(new RadarControl(2, 2, tempMarker._x, tempMarker._y, tempMarker._z));
		}

		_markers.removeAllElements();
	}

	public void removeMarker(int x, int y, int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);

		_markers.remove(newMarker);
		_player.sendPacket(new RadarControl(1, 1, x, y, z));
	}
}
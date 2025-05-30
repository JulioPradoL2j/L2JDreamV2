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
package com.dream.game.network.serverpackets;

import com.dream.L2DatabaseFactory;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FriendList extends L2GameServerPacket
{
	private static class FriendStatus
	{
		private final int _charId;
		private final int _id;
		private final String _name;
		private final boolean _online;

		public FriendStatus(int charId, int id, String name, boolean online)
		{
			_charId = charId;
			_id = id;
			_name = name;
			_online = online;
		}

		public int getCharId()
		{
			return _charId;
		}

		public int getId()
		{
			return _id;
		}

		public String getName()
		{
			return _name;
		}

		public boolean isOnline()
		{
			return _online;
		}
	}

	public static Logger _log = Logger.getLogger(FriendList.class.getName());
	private final List<FriendStatus> _friends = new ArrayList<>();
	private final L2PcInstance _activeChar;

	private Connection con;

	public FriendList(L2PcInstance character)
	{
		_activeChar = character;
		getFriendList();
	}

	
	private void getFriendList()
	{
		con = null;

		try
		{
			String sqlQuery = "SELECT friendId, friend_name FROM character_friends WHERE " + "charId=" + _activeChar.getObjectId() + " ORDER BY friend_name ASC";

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(sqlQuery);
			ResultSet rset = statement.executeQuery(sqlQuery);

			int friendId;
			String friendName;
			while (rset.next())
			{
				friendId = rset.getInt("friendId");
				friendName = rset.getString("friend_name");

				if (friendId == _activeChar.getObjectId())
				{
					continue;
				}

				L2PcInstance friend = L2World.getInstance().getPlayer(friendName);

				_friends.add(new FriendStatus(0x00030b7a, friendId, friendName, friend != null));
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error found in " + _activeChar.getName() + "'s FriendList: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfa);
		writeD(_friends.size());
		for (FriendStatus fs : _friends)
		{
			writeD(fs.getCharId());
			writeS(fs.getName());
			writeD(fs.isOnline() ? 0x01 : 0x00);
			writeD(fs.isOnline() ? fs.getId() : 0x00);
		}
	}

}
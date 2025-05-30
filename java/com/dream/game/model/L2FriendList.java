package com.dream.game.model;

import com.dream.L2DatabaseFactory;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.FriendList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class L2FriendList
{
	private final static Logger _log = Logger.getLogger(L2FriendList.class.getName());

	private static final String RESTORE_FRIENDLIST = "SELECT friendId,friend_name FROM character_friends WHERE charId=?";

	private static final String RESTORE_FRIEND_ID = "SELECT friendId FROM character_friends WHERE charId=? AND friend_name=?";

	private static final String DELETE_FROM_FRIENDLIST = "DELETE FROM character_friends WHERE (charId=? AND friendId=?) OR (charId=? AND friendId=?)";

	private static final String ADD_TO_FRIENDLIST = "INSERT INTO character_friends (charId, friendId, friend_name) VALUES (?, ?, ?),(?, ?, ?)";

	private static final String UPDATE_FDRIEND_NAME = "UPDATE character_friends SET friend_name=? WHERE friendId=?";

	public static void addToFriendList(L2PcInstance requestor, L2PcInstance character)
	{
		requestor.getFriendList().addToFriendList(character);
		requestor.sendPacket(new FriendList(requestor));
		character.sendPacket(new FriendList(character));
	}

	public static Map<Integer, String> getFriendList(L2PcInstance requestor)
	{
		return requestor.getFriendList().getFriendList();
	}

	public static String[] getFriendListNames(L2PcInstance requestor)
	{
		return requestor.getFriendList().getFriendListNames();
	}

	public static boolean isInFriendList(L2PcInstance requestor, L2PcInstance character)
	{
		return requestor.getFriendList().isInFriendList(character);
	}

	public static boolean isInFriendList(L2PcInstance requestor, String _character)
	{
		return requestor.getFriendList().isInFriendList(_character);
	}

	public static void removeFromFriendList(L2PcInstance requestor, L2PcInstance character)
	{
		requestor.getFriendList().removeFromFriendList(character);

		requestor.sendPacket(new FriendList(requestor));
		character.sendPacket(new FriendList(character));
	}

	public static void removeFromFriendList(L2PcInstance requestor, String character)
	{
		requestor.getFriendList().removeFromFriendList(character);
		requestor.sendPacket(new FriendList(requestor));
	}

	
	public static void updateFriendName(L2PcInstance player)
	{
		if (player == null)
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_FDRIEND_NAME);
			statement.setString(1, player.getName());
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("L2FriendList:  Could not change friend name...");
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private final Map<Integer, String> friendlist;

	private final L2PcInstance listOwner;

	public L2FriendList(L2PcInstance character)
	{
		friendlist = new HashMap<>();
		listOwner = character;
		loadFriendList();
	}

	
	private void addToFriendList(L2PcInstance character)
	{
		if (character != null && !isInFriendList(character))
		{
			Connection con = null;

			try
			{

				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement;
				statement = con.prepareStatement(ADD_TO_FRIENDLIST);
				statement.setInt(1, listOwner.getObjectId());
				statement.setInt(2, character.getObjectId());
				statement.setString(3, character.getName());
				statement.setInt(4, character.getObjectId());
				statement.setInt(5, listOwner.getObjectId());
				statement.setString(6, listOwner.getName());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not insert friend data:" + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			friendlist.put(character.getObjectId(), character.getName());
			character.getFriendList().getFriendList().put(listOwner.getObjectId(), listOwner.getName());
		}
	}

	private Map<Integer, String> getFriendList()
	{
		return friendlist;
	}

	private String[] getFriendListNames()
	{
		return friendlist.values().toArray(new String[friendlist.size()]);
	}

	private boolean isInFriendList(L2PcInstance character)
	{
		return friendlist.containsKey(character.getObjectId());
	}

	private boolean isInFriendList(String _character)
	{
		return friendlist.containsValue(_character);
	}

	
	private void loadFriendList()
	{
		if (listOwner != null)
		{
			Connection con = null;

			try
			{

				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement;
				statement = con.prepareStatement(RESTORE_FRIENDLIST);
				statement.setInt(1, listOwner.getObjectId());
				ResultSet rset = statement.executeQuery();

				while (rset.next())
				{
					friendlist.put(rset.getInt("friendId"), rset.getString("friend_name"));
				}

				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error restoring friend data.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	
	private void removeFromFriendList(L2PcInstance character)
	{
		Connection con = null;

		try
		{

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement(DELETE_FROM_FRIENDLIST);
			statement.setInt(1, listOwner.getObjectId());
			statement.setInt(2, character.getObjectId());
			statement.setInt(3, character.getObjectId());
			statement.setInt(4, listOwner.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Could not delete friend data:" + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		friendlist.remove(character.getObjectId());
		character.getFriendList().getFriendList().remove(listOwner.getObjectId());
	}

	
	private void removeFromFriendList(String _character)
	{
		if (isInFriendList(_character))
		{
			Connection con = null;

			int _friendId = restoreFriendId(_character);

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement;
				statement = con.prepareStatement(DELETE_FROM_FRIENDLIST);
				statement.setInt(1, listOwner.getObjectId());
				statement.setInt(2, _friendId);
				statement.setInt(3, _friendId);
				statement.setInt(4, listOwner.getObjectId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not delete friend data:" + e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			friendlist.remove(_friendId);
		}
	}

	
	private int restoreFriendId(String _character)
	{
		int _friendId = 0;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement(RESTORE_FRIEND_ID);
			statement.setInt(1, listOwner.getObjectId());
			statement.setString(2, _character);
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				_friendId = rset.getInt("friendId");
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Could not get friend id:" + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return _friendId;
	}
}
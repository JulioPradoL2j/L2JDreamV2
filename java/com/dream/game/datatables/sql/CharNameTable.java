package com.dream.game.datatables.sql;

import com.dream.L2DatabaseFactory;
import com.dream.lang.L2Integer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public final class CharNameTable
{
	private class CharacterInfo
	{
		public final Integer _objectId;
		public String _name;
		private String _oldName;

		public CharacterInfo(int objectId)
		{
			_objectId = L2Integer.valueOf(objectId);

			CharacterInfo characterInfo = _mapByObjectId.put(_objectId, this);
			if (characterInfo != null)
			{
				_log.warn("CharNameTable: Duplicated objectId: [" + this + "] - [" + characterInfo + "]");
			}
		}

		@Override
		public String toString()
		{
			return "objectId: " + _objectId + ", name: " + _name;
		}

		public void updateName(String name)
		{
			_name = name.intern();
			if (_name != null)
			{
				_mapByName.remove(_name.toLowerCase());
			}
			CharacterInfo characterInfo = _mapByName.put(_name.toLowerCase(), this);
			if (characterInfo != null)
			{
				_log.warn("CharNameTable: Duplicated hashName: [" + this + "] - [" + characterInfo + "]");
			}
		}

		public void updateName(String name, String oldName)
		{
			_name = name.intern();
			_oldName = oldName.intern();
			if (_oldName != null)
			{
				_mapByName.remove(_oldName.toLowerCase());
			}
			if (_name != null)
			{
				_mapByName.remove(_name.toLowerCase());
			}
			CharacterInfo characterInfo = _mapByName.put(_name.toLowerCase(), this);
			if (characterInfo != null)
			{
				_log.warn("CharNameTable: Duplicated hashName: [" + this + "] - [" + characterInfo + "]");
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final CharNameTable _instance = new CharNameTable();
	}

	static final Logger _log = Logger.getLogger(CharNameTable.class);

	
	public static int accountCharNumber(String account)
	{
		int number = 0;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM characters WHERE account_name = ?");
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				number = rset.getInt(1);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return number;
	}

	public static final CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public final Map<Integer, CharacterInfo> _mapByObjectId = new HashMap<>();

	public final Map<String, CharacterInfo> _mapByName = new HashMap<>();

	
	public CharNameTable()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT charId, char_name FROM characters");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				update(rset.getInt("charId"), rset.getString("char_name"));
			}

			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_log.info("CharName Data: Loaded " + _mapByObjectId.size() + " character names.");
	}

	public boolean doesCharNameExist(String name)
	{
		return getByName(name) != null;
	}

	public Integer getByName(String name)
	{
		CharacterInfo characterInfo = _mapByName.get(name.toLowerCase());

		return characterInfo == null ? null : characterInfo._objectId;
	}

	public String getByObjectId(Integer objectId)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);

		return characterInfo == null ? null : characterInfo._name;
	}

	public void update(int objectId, String name)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		if (characterInfo == null)
		{
			characterInfo = new CharacterInfo(objectId);
		}

		characterInfo.updateName(name);
	}

	public void update(int objectId, String name, String oldName)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		if (characterInfo == null)
		{
			characterInfo = new CharacterInfo(objectId);
		}

		characterInfo.updateName(name, oldName);
	}
}
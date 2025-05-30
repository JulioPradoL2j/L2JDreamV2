package com.dream.game.model.entity;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.manager.RaidBossInfoManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GrandBossState
{
	public static enum StateEnum
	{
		NOTSPAWN,
		ALIVE,
		DEAD,
		INTERVAL,
		SLEEP,
		UNKNOWN
	}

	private int _bossId;
	private long _respawnDate;
	private StateEnum _state;

	public GrandBossState()
	{
	}

	public GrandBossState(int bossId)
	{
		_bossId = bossId;
		load();
	}

	public GrandBossState(int bossId, boolean isDoLoad)
	{
		_bossId = bossId;
		if (isDoLoad)
		{
			load();
		}
	}

	public int getBossId()
	{
		return _bossId;
	}

	public long getInterval()
	{
		long interval = _respawnDate - System.currentTimeMillis();

		if (interval < 0)
			return 0;

		return interval;
	}

	public long getRespawnDate()
	{
		return _respawnDate;
	}

	public StateEnum getState()
	{
		if (_state == null)
		{
			_state = StateEnum.UNKNOWN;
		}
		return _state;
	}

	
	public void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("SELECT * FROM grandboss_intervallist WHERE boss_id = ?");
			statement.setInt(1, _bossId);
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				_respawnDate = rset.getLong("respawn_time");
				_state = StateEnum.values()[rset.getInt("state")];
			}
			rset.close();
			statement.close();

			if (Config.LIST_RAID_BOSS_IDS.contains(_bossId))
				RaidBossInfoManager.getInstance().updateRaidBossInfo(_bossId, _respawnDate);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	public void save()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("INSERT INTO grandboss_intervallist (boss_id,respawn_time,state) VALUES(?,?,?)");
			statement.setInt(1, _bossId);
			statement.setLong(2, _respawnDate);
			statement.setInt(3, _state.ordinal());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (Config.LIST_RAID_BOSS_IDS.contains(_bossId))
			RaidBossInfoManager.getInstance().updateRaidBossInfo(_bossId, _respawnDate);
	}

	public void setBossId(int newId)
	{
		_bossId = newId;
	}

	public void setNextRespawnDate(long newRespawnDate)
	{
		_respawnDate = newRespawnDate;
	}

	public void setRespawnDate(long interval)
	{
		_respawnDate = interval + System.currentTimeMillis();
	}

	public void setState(StateEnum newState)
	{
		_state = newState;
		if (_bossId != 0)
		{
			update();
		}
	}

	
	public void update()
	{
		Connection con = null;
		boolean needInsert = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE grandboss_intervallist SET respawn_time = ?,state = ? WHERE boss_id = ?");
			statement.setLong(1, _respawnDate);
			statement.setInt(2, _state.ordinal());
			statement.setInt(3, _bossId);
			needInsert = statement.executeUpdate() == 0;
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		if (needInsert)
		{
			save();
		}

		if (Config.LIST_RAID_BOSS_IDS.contains(_bossId))
			RaidBossInfoManager.getInstance().updateRaidBossInfo(_bossId, _respawnDate);
	}
}
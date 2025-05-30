package com.dream.game.manager;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class SiegeGuardManager
{
	private final static Logger _log = Logger.getLogger(SiegeGuardManager.class.getName());
	private final List<L2Spawn> _siegeGuardSpawn = new ArrayList<>();
	private final String _castleName;
	private final int _castleId;
	private final int _castleOwnerId;

	public SiegeGuardManager(String name, int id, int ownerId)
	{
		_castleName = name;
		_castleId = id;
		_castleOwnerId = ownerId;
	}

	public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 0);
	}

	public void addSiegeGuard(L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
			return;

		addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}

	public final List<L2Spawn> getSiegeGuardSpawn()
	{
		return _siegeGuardSpawn;
	}

	public void hireMerc(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 1);
	}

	public void hireMerc(L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
			return;

		hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}

	
	private void loadSiegeGuard()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_siege_guards WHERE castleId = ? AND isHired = ?");
			statement.setInt(1, _castleId);
			if (_castleOwnerId > 0 && _castleId <= 9)
			{
				statement.setInt(2, 1);
			}
			else
			{
				statement.setInt(2, 0);
			}
			ResultSet rs = statement.executeQuery();

			L2Spawn spawn;
			L2NpcTemplate template;

			while (rs.next())
			{
				template = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if (template != null)
				{
					spawn = new L2Spawn(template);
					spawn.setId(rs.getInt("id"));
					spawn.setAmount(1);
					spawn.setLocx(rs.getInt("x"));
					spawn.setLocy(rs.getInt("y"));
					spawn.setLocz(rs.getInt("z"));
					spawn.setHeading(rs.getInt("heading"));
					spawn.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn.setLocation(0);
					_siegeGuardSpawn.add(spawn);
				}
				else
				{
					_log.warn("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warn("Error loading siege guard for castle " + _castleName + ":" + e1);
		}
	}

	
	public void removeMerc(int npcId, int x, int y, int z)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM castle_siege_guards WHERE npcId = ? AND x = ? AND y = ? AND z = ? AND isHired = 1");
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error deleting hired siege guard at " + x + ',' + y + ',' + z + ":" + e);
		}
	}

	
	public void removeMercs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM castle_siege_guards WHERE castleId = ? AND isHired = 1");
			statement.setInt(1, _castleId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error deleting hired siege guard for castle " + _castleName + ":" + e);
		}
	}

	
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, _castleId);
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			statement.setInt(7, 600);
			statement.setInt(8, isHire);
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warn("Error adding siege guard for castle " + _castleName + ":" + e1);
		}
	}

	public void spawnSiegeGuard()
	{
		try
		{
			int hiredCount = 0;
			boolean isHired = _castleOwnerId > 0;

			if (Config.SPAWN_SIEGE_GUARD)
			{
				loadSiegeGuard();
				for (L2Spawn spawn : getSiegeGuardSpawn())
					if (spawn != null)
					{
						if (isHired)
						{
							spawn.setRespawnDelay(600);
						}

						spawn.doSpawn(true);

						if (isHired)
						{
							if (++hiredCount > MercTicketManager.getInstance().getMaxAllowedMerc(_castleId))
								return;

							if (hiredCount > Config.MAX_GUARD_COUNT_FOR_CASTLE)
								return;
						}
					}
			}
		}
		catch (Throwable t)
		{
			_log.warn("Error spawning siege guards for castle " + _castleName + ":" + t.toString());
		}
	}

	public void unspawnSiegeGuard()
	{
		for (L2Spawn spawn : getSiegeGuardSpawn())
		{
			if (spawn == null)
			{
				continue;
			}

			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
			{
				spawn.getLastSpawn().doDie(spawn.getLastSpawn());
			}
		}
		getSiegeGuardSpawn().clear();
	}
}
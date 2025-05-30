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

package com.dream.game.manager;

import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FortSpawnManager
{
	// Task for unspawn the npc
	private class endSpawnTime implements Runnable
	{
		// Object to unspawn
		L2Spawn _spawn;

		protected endSpawnTime(L2Spawn spawn)
		{
			_spawn = spawn;
		}

		@Override
		public void run()
		{
			_spawn.stopRespawn();
			_spawn.getLastSpawn().deleteMe();
		}
	}

	/* Subclass for manage special propertiess for this spawns
	 * @param Spawn
	 * @param IsInSiege
	 * @param SpawnTime */
	public class ManagerSpawn
	{
		// Data
		private final L2Spawn _spawn;
		private final String _spawnType;
		private int _spawnTime;
		private boolean _forSiege;

		// Constructor
		public ManagerSpawn(L2Spawn spawn, String spawnType, int spawnTime)
		{
			_spawn = spawn;
			_spawnTime = spawnTime;
			_spawnType = spawnType;
			_forSiege = false;
		}

		public void doReSpawn()
		{
			if (!getSpawnType().equalsIgnoreCase("doorman"))
			{

				_spawn.init();
				_spawn.getLastSpawn().spawnMe(_spawn.getLastSpawn().getX(), _spawn.getLastSpawn().getY(), _spawn.getLastSpawn().getZ());

				// Spawn the npc only after siege at certain time
				if (_forSiege && getSpawnType().equalsIgnoreCase("Special_envoy"))
				{
					if (_spawnTime <= 0)
					{
						_spawnTime = 3600;// Special Envoy must be spawned for a
					}
					// 1 hour
					ThreadPoolManager.getInstance().scheduleGeneral(new endSpawnTime(_spawn), _spawnTime * 1000);
				}
				else if (_spawnTime > 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new endSpawnTime(_spawn), _spawnTime * 1000);
				}
			}
		}

		public void doSpawn()
		{
			if (getSpawnType().equalsIgnoreCase("Special_envoy"))
				return;

			_spawn.init();
			_spawn.getLastSpawn().spawnMe(_spawn.getLastSpawn().getX(), _spawn.getLastSpawn().getY(), _spawn.getLastSpawn().getZ());

			if (_spawnTime > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new endSpawnTime(_spawn), _spawnTime * 1000);
			}
		}

		// Functions
		public L2Spawn getSpawn()
		{
			return _spawn;
		}

		public String getSpawnType()
		{
			return _spawnType;
		}

		public int getTime()
		{
			return _spawnTime;
		}

		public void unSpawn(boolean forSiege)
		{
			_forSiege = forSiege;

			// Only Unspawn if the npc not stay in the siege
			if (getSpawnType().equalsIgnoreCase("Doorman"))
				return;

			_spawn.stopRespawn();
			_spawn.getLastSpawn().deleteMe();
		}
	}

	protected static final Logger _log = Logger.getLogger(FortSpawnManager.class.getName());

	// Data
	private final Fort _fort;

	// Functions.

	private final List<ManagerSpawn> _ManagersSpawn = new ArrayList<>();

	// Constructor
	public FortSpawnManager(Fort fort)
	{
		_fort = fort;
		loadManagers();
	}

	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		addNewSpawn(spawn, "none", 0, storeInDb);
	}

	public void addNewSpawn(L2Spawn spawn, int spawnTime, boolean storeInDb)
	{
		addNewSpawn(spawn, "none", spawnTime, storeInDb);
	}

	public void addNewSpawn(L2Spawn spawn, String spawnType, boolean storeInDb)
	{
		addNewSpawn(spawn, spawnType, 0, storeInDb);
	}

	
	public void addNewSpawn(L2Spawn spawn, String spawnType, int spawnTime, boolean storeInDb)
	{
		ManagerSpawn mspawn = new ManagerSpawn(spawn, spawnType, spawnTime);
		_ManagersSpawn.add(mspawn);
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("INSERT INTO fort_spawnlist(fort_Id,id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id,spawnType,spawnTime) values(?,?,?,?,?,?,?,?,?,?,?,?)");
				statement.setInt(1, getFort().getFortId());
				statement.setInt(2, spawn.getDbId());
				statement.setInt(3, spawn.getAmount());
				statement.setInt(4, spawn.getNpcId());
				statement.setInt(5, spawn.getLocx());
				statement.setInt(6, spawn.getLocy());
				statement.setInt(7, spawn.getLocz());
				statement.setInt(8, spawn.getHeading());
				statement.setInt(9, spawn.getRespawnDelay() / 1000);
				statement.setInt(10, spawn.getLocation());
				statement.setString(11, spawnType);
				statement.setInt(12, spawnTime / 1000);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				// problem with storing spawn
				_log.warn("FortSpawnManager: Could not store spawn in the DB:" + e);
			}
		}
	}

	// Properties
	public final Fort getFort()
	{
		return _fort;
	}

	public final List<ManagerSpawn> getManagerSpawn()
	{
		return _ManagersSpawn;
	}

	public String getSpawnType(int npcId)
	{
		for (ManagerSpawn spawn : _ManagersSpawn)
			if (spawn.getSpawn().getNpcId() == npcId)
				return spawn.getSpawnType();
		return null;
	}

	
	private void loadManagers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fort_Id = ? ");
			statement.setInt(1, getFort().getFortId());
			ResultSet rs = statement.executeQuery();

			L2Spawn spawn1;
			L2NpcTemplate template1;
			ManagerSpawn mspawn;
			while (rs.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rs.getInt("npc_templateId"));
				if (template1 != null)
				{
					spawn1 = new L2Spawn(template1);
					spawn1.setId(rs.getInt("id"));
					spawn1.setAmount(rs.getInt("count"));
					spawn1.setLocx(rs.getInt("locx"));
					spawn1.setLocy(rs.getInt("locy"));
					spawn1.setLocz(rs.getInt("locz"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawn_delay"));
					spawn1.setLocation(rs.getInt("loc_id"));
					String spawnType = rs.getString("spawnType");
					int spawnTime = rs.getInt("spawnTime");
					mspawn = new ManagerSpawn(spawn1, spawnType, spawnTime);
					_ManagersSpawn.add(mspawn);
				}
				else
				{
					_log.warn("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warn("Error loading managers for fort " + getFort().getName() + ":" + e1);
		}
	}

	// Repawn Managers after siege
	public void respawnManagers()
	{
		try
		{
			for (ManagerSpawn spawn : getManagerSpawn())
				if (spawn != null)
				{
					spawn.doReSpawn();
				}
		}
		catch (Throwable t)
		{
			_log.warn("reSpawnManagers: Error spawning managers for fort " + getFort().getName() + ":" + t.toString());
		}
	}

	// Spawn Managers
	public void spawnManagers()
	{
		try
		{
			for (ManagerSpawn spawn : getManagerSpawn())
				if (spawn != null)
				{
					spawn.doSpawn();
				}
		}
		catch (Throwable t)
		{
			_log.warn("Error spawning managers for fort " + getFort().getName() + ":" + t.toString());
		}
	}

	// Unspawn Mnagers for siege
	public void unspawnForSiege()
	{
		try
		{
			for (ManagerSpawn spawn : getManagerSpawn())
				if (spawn != null)
					if (!spawn.getSpawnType().equalsIgnoreCase("doorman"))
					{
						spawn.unSpawn(true);
					}
		}
		catch (Throwable t)
		{
			_log.warn("UnSpawnFoSiege:Error unspawning managers for fort " + getFort().getName() + ":" + t.toString());
		}
	}

	// unspawn all
	public void unspawnManagers()
	{
		try
		{
			for (ManagerSpawn spawn : getManagerSpawn())
				if (spawn != null)
				{
					spawn.unSpawn(false);
				}
		}
		catch (Throwable t)
		{
			_log.warn("UnSpawnManagers:Error unspawning managers for fort " + getFort().getName() + ":" + t.toString());
		}
	}
}
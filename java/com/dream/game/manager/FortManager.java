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
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class FortManager
{
	protected static final Logger _log = Logger.getLogger(FortManager.class.getName());

	private static FortManager _instance;

	public static final FortManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new FortManager();
		}
		return _instance;
	}

	protected Map<Integer, Integer> _envoyCastles = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> _npcCommanders = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> _siegeNpcs = new HashMap<>();
	protected Map<Integer, List<L2Spawn>> _specialEnvoys = new HashMap<>();
	protected List<L2Spawn> _npcCommandersSpawns;
	protected List<L2Spawn> _siegeNpcsSpawns;
	protected List<L2Spawn> _specialEnvoysSpawns;

	protected int _respawnTime;

	private Fort _fort;
	private List<Fort> _forts;

	public FortManager()
	{
		load();
	}

	public FortManager(Fort fort)
	{
		_fort = fort;
		initNpcs();
		initSiegeNpcs();
		spawnSuspiciousMerchant();
		initNpcCommanders();
		spawnNpcCommanders();
		initSpecialEnvoys();
		if (_fort.getOwnerClan() != null && _fort.getFortState() == 0)
		{
			spawnSpecialEnvoys();
			ThreadPoolManager.getInstance().scheduleGeneral(_fort.new ScheduleSpecialEnvoysDeSpawn(_fort), 1 * 60 * 60 * 1000);
		}
	}

	public void despawnNpcCommanders()
	{
		List<L2Spawn> monsterList = _npcCommanders.get(getFort().getFortId());
		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				spawnDat.stopRespawn();
				spawnDat.getLastSpawn().deleteMe();
			}
		}
	}

	public void despawnSpecialEnvoys()
	{
		List<L2Spawn> monsterList = _specialEnvoys.get(getFort().getFortId());
		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				spawnDat.stopRespawn();
				spawnDat.getLastSpawn().deleteMe();
			}
		}
	}

	public void despawnSuspiciousMerchant()
	{
		List<L2Spawn> monsterList = _siegeNpcs.get(getFort().getFortId());
		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				spawnDat.stopRespawn();
				spawnDat.getLastSpawn().deleteMe();
			}
		}
	}

	public final int findNearestFortIndex(L2Object obj)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double closestDistance = Double.MAX_VALUE;
			double distance;

			for (Fort fort : getForts())
			{
				if (fort == null)
				{
					continue;
				}
				distance = fort.getDistanceToZone(obj.getX(), obj.getY());
				if (closestDistance > distance)
				{
					closestDistance = distance;
					index = getFortIndex(fort.getFortId());
				}
			}
		}
		return index;
	}

	public int getEnvoyCastle(int npcId)
	{
		return _envoyCastles.get(npcId);
	}

	public final Fort getFort()
	{
		return _fort;
	}

	public final Fort getFort(int x, int y, int z)
	{
		for (Fort f : getForts())
			if (f.checkIfInZone(x, y, z))
				return f;
		return null;
	}

	public final Fort getFort(L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Fort getFort(String name)
	{
		for (Fort f : getForts())
			if (f.getName().equalsIgnoreCase(name.trim()))
				return f;
		return null;
	}

	public final Fort getFortById(int fortId)
	{
		for (Fort f : getForts())
			if (f.getFortId() == fortId)
				return f;
		return null;
	}

	public final Fort getFortByOwner(L2Clan clan)
	{
		for (Fort f : getForts())
			if (f.getOwnerClan() == clan)
				return f;
		return null;
	}

	public final int getFortIndex(int fortId)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.getFortId() == fortId)
				return i;
		}
		return -1;
	}

	public final int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for (int i = 0; i < getForts().size(); i++)
		{
			fort = getForts().get(i);
			if (fort != null && fort.checkIfInZone(x, y, z))
				return i;
		}
		return -1;
	}

	public final int getFortIndex(L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final List<Fort> getForts()
	{
		if (_forts == null)
		{
			_forts = new ArrayList<>();
		}
		return _forts;
	}

	
	private void initNpcCommanders()
	{
		_npcCommanders.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct fortId FROM fort_spawnlist WHERE spawnType = ? ORDER BY fortId");

			statement1.setInt(1, 1);
			ResultSet rset1 = statement1.executeQuery();

			while (rset1.next())
			{
				int fortId = rset1.getInt("fortId");
				PreparedStatement statement2 = con.prepareStatement("SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id");
				statement2.setInt(1, getFort().getFortId());
				statement2.setInt(2, 1);
				ResultSet rset2 = statement2.executeQuery();

				L2Spawn spawnDat;
				L2NpcTemplate template1;
				_npcCommandersSpawns = new ArrayList<>();
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npcId"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(1);
						spawnDat.setLocx(rset2.getInt("x"));
						spawnDat.setLocy(rset2.getInt("y"));
						spawnDat.setLocz(rset2.getInt("z"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(60);
						_npcCommandersSpawns.add(spawnDat);
					}
					else
					{
						_log.warn("FortManager.initNpcCommanders: Data missing in NPC table for ID: " + rset2.getInt("npcId") + ".");
					}
				}
				rset2.close();
				statement2.close();
				_npcCommanders.put(fortId, _npcCommandersSpawns);
			}
			rset1.close();
			statement1.close();
		}
		catch (Exception e)
		{
			_log.warn("FortManager.initNpcCommanders: Spawn could not be initialized: " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	private void initNpcs()
	{

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ");
			statement.setInt(1, getFort().getFortId());
			statement.setInt(2, 0);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npcId"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(1);
					spawnDat.setLocx(rset.getInt("x"));
					spawnDat.setLocy(rset.getInt("y"));
					spawnDat.setLocz(rset.getInt("z"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(60);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					spawnDat.doSpawn();
					spawnDat.startRespawn();
				}
				else
				{
					_log.warn("FortManager.initNpcs: Data missing in NPC table for ID: " + rset.getInt("npcId") + ".");
				}
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("FortManager.initNpcs: Spawn could not be initialized: " + e.getMessage());
			e.printStackTrace();
		}

	}

	
	private void initSiegeNpcs()
	{
		_siegeNpcs.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct fortId FROM fort_spawnlist WHERE spawnType = ? ORDER BY fortId");

			statement1.setInt(1, 2);
			ResultSet rset1 = statement1.executeQuery();

			while (rset1.next())
			{
				int fortId = rset1.getInt("fortId");
				PreparedStatement statement2 = con.prepareStatement("SELECT id, npcId, x, y, z, heading FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id");
				statement2.setInt(1, getFort().getFortId());
				statement2.setInt(2, 2);
				ResultSet rset2 = statement2.executeQuery();

				L2Spawn spawnDat;
				L2NpcTemplate template1;
				_siegeNpcsSpawns = new ArrayList<>();
				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npcId"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(1);
						spawnDat.setLocx(rset2.getInt("x"));
						spawnDat.setLocy(rset2.getInt("y"));
						spawnDat.setLocz(rset2.getInt("z"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(60);
						_siegeNpcsSpawns.add(spawnDat);
					}
					else
					{
						_log.warn("FortManager.initSiegeNpcs: Data missing in NPC table for ID: " + rset2.getInt("npcId") + ".");
					}
				}
				rset2.close();
				statement2.close();
				_siegeNpcs.put(fortId, _siegeNpcsSpawns);
			}
			rset1.close();
			statement1.close();
		}
		catch (Exception e)
		{
			_log.warn("FortManager.initSiegeNpcs: Spawn could not be initialized: " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	private void initSpecialEnvoys()
	{
		_specialEnvoys.clear();
		_envoyCastles.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement1 = con.prepareStatement("SELECT Distinct fortId FROM fort_spawnlist WHERE spawnType = ? ORDER BY fortId");

			statement1.setInt(1, 3);
			ResultSet rset1 = statement1.executeQuery();

			while (rset1.next())
			{
				int fortId = rset1.getInt("fortId");
				PreparedStatement statement2 = con.prepareStatement("SELECT id, npcId, x, y, z, heading, castleId FROM fort_spawnlist WHERE fortId = ? AND spawnType = ? ORDER BY id");
				statement2.setInt(1, getFort().getFortId());
				statement2.setInt(2, 3);
				ResultSet rset2 = statement2.executeQuery();

				L2Spawn spawnDat;
				L2NpcTemplate template1;
				_specialEnvoysSpawns = new ArrayList<>();
				while (rset2.next())
				{
					int castleId = rset2.getInt("castleId");
					int npcId = rset2.getInt("npcId");
					template1 = NpcTable.getInstance().getTemplate(npcId);
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(1);
						spawnDat.setLocx(rset2.getInt("x"));
						spawnDat.setLocy(rset2.getInt("y"));
						spawnDat.setLocz(rset2.getInt("z"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(60);
						_specialEnvoysSpawns.add(spawnDat);
						_envoyCastles.put(npcId, castleId);
					}
					else
					{
						_log.warn("FortManager.initSpecialEnvoys: Data missing in NPC table for ID: " + rset2.getInt("npcId") + ".");
					}
				}
				rset2.close();
				statement2.close();
				_specialEnvoys.put(fortId, _specialEnvoysSpawns);
			}
			rset1.close();
			statement1.close();
		}
		catch (Exception e)
		{
			_log.warn("FortManager.initSpecialEnvoys: Spawn could not be initialized: " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	private final void load()
	{
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			Connection con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT id FROM fort ORDER BY id");
			rs = statement.executeQuery();

			while (rs.next())
			{
				getForts().add(new Fort(rs.getInt("id")));
			}

			rs.close();
			statement.close();

			_log.info("Loaded: " + getForts().size() + " fortress");
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadFortData(): " + e.getMessage());
		}
	}

	public void spawnNpcCommanders()
	{
		List<L2Spawn> monsterList = _npcCommanders.get(getFort().getFortId());
		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
		}
	}

	public void spawnSpecialEnvoys()
	{
		List<L2Spawn> monsterList = _specialEnvoys.get(getFort().getFortId());
		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
		}
	}

	public void spawnSuspiciousMerchant()
	{
		List<L2Spawn> monsterList = _siegeNpcs.get(getFort().getFortId());
		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
		}
	}
}
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
import com.dream.game.templates.chars.L2NpcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class FortSiegeGuardManager
{
	protected static final Logger _log = Logger.getLogger(FortSiegeGuardManager.class.getName());

	private final Fort _fort;
	protected Map<Integer, List<L2Spawn>> _siegeGuards = new HashMap<>();
	protected List<L2Spawn> _siegeGuardsSpawns;

	public FortSiegeGuardManager(Fort fort)
	{
		_fort = fort;
	}

	public final Fort getFort()
	{
		return _fort;
	}

	public final Map<Integer, List<L2Spawn>> getSiegeGuardSpawn()
	{
		return _siegeGuards;
	}

	public boolean isSiegeGuard(L2Spawn par)
	{
		return _siegeGuards.get(getFort().getFortId()).contains(par);
	}

	
	void loadSiegeGuard()
	{
		_siegeGuards.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_siege_guards WHERE fortId = ? ");
			statement.setInt(1, getFort().getFortId());
			ResultSet rs = statement.executeQuery();

			L2Spawn spawn1;
			L2NpcTemplate template1;

			_siegeGuardsSpawns = new ArrayList<>();
			while (rs.next())
			{
				int fortId = rs.getInt("fortId");
				template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if (template1 != null)
				{
					spawn1 = new L2Spawn(template1);
					spawn1.setId(rs.getInt("id"));
					spawn1.setAmount(1);
					spawn1.setLocx(rs.getInt("x"));
					spawn1.setLocy(rs.getInt("y"));
					spawn1.setLocz(rs.getInt("z"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn1.setLocation(0);

					_siegeGuardsSpawns.add(spawn1);
				}
				else
				{
					_log.warn("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}

				_siegeGuards.put(fortId, _siegeGuardsSpawns);
			}
			rs.close();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warn("Error loading siege guard for fort " + getFort().getName() + ":" + e1);
			e1.printStackTrace();
		}
	}

	public void spawnSiegeGuard()
	{
		try
		{
			List<L2Spawn> monsterList = getSiegeGuardSpawn().get(getFort().getFortId());
			if (monsterList != null)
			{
				for (L2Spawn spawnDat : monsterList)
				{
					spawnDat.doSpawn();
					spawnDat.startRespawn();
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Error spawning siege guards for fort " + getFort().getName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void unspawnSiegeGuard()
	{
		try
		{
			List<L2Spawn> monsterList = getSiegeGuardSpawn().get(getFort().getFortId());
			if (monsterList != null)
			{
				for (L2Spawn spawnDat : monsterList)
				{
					spawnDat.stopRespawn();
					spawnDat.getLastSpawn().doDie(spawnDat.getLastSpawn());
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Error unspawning siege guards for fort " + getFort().getName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}
}
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

import com.dream.Config;
import com.dream.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


/**
 * @author rapfersan92
 */
public class RaidBossInfoManager
{
	private static final Logger _log = Logger.getLogger(RaidBossInfoManager.class.getName());
	
	private final Map<Integer, Long> _raidBosses;
	
	public static RaidBossInfoManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected RaidBossInfoManager()
	{
		_raidBosses = new ConcurrentHashMap<>();
		load();
	}
	
	
	public void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			
			statement = con.prepareStatement("SELECT boss_id, respawn_time FROM grandboss_intervallist UNION SELECT boss_id, respawn_time FROM raidboss_spawnlist ORDER BY boss_id");
			rs = statement.executeQuery();
			while (rs.next())
			{
				int bossId = rs.getInt("boss_id");
				if (Config.LIST_RAID_BOSS_IDS.contains(bossId))
					_raidBosses.put(bossId, rs.getLong("respawn_time"));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: RaidBossInfoManager load: " + e);
		}
		
		_log.info("RaidBossInfoManager: Loaded " + _raidBosses.size() + " instances.");
	}
	
	public void updateRaidBossInfo(int bossId, long respawnTime)
	{
		_raidBosses.put(bossId, respawnTime);
	}
	
	public long getRaidBossRespawnTime(int bossId)
	{
		return _raidBosses.get(bossId);
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossInfoManager _instance = new RaidBossInfoManager();
	}
}
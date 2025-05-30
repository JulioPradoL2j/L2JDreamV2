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
import com.dream.ConfigFiles;
import com.dream.L2DatabaseFactory;
import com.dream.annotations.L2Properties;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ArtefactInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.TowerSpawn;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class SiegeManager
{
	public class SiegeSpawn
	{
		Location _location;

		private final int _npcId;

		private final int _heading;
		private List<Integer> _zoneList;

		private final int _castleId;
		private int _upgradeLevel;
		private int _hp;

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
		}

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_hp = hp;
		}

		public int getCastleId()
		{
			return _castleId;
		}

		public int getHeading()
		{
			return _heading;
		}

		public int getHp()
		{
			return _hp;
		}

		public Location getLocation()
		{
			return _location;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getUpgradeLevel()
		{
			return _upgradeLevel;
		}

		public List<Integer> getZoneList()
		{
			return _zoneList;
		}

		public void setUpgradeLevel(int level)
		{
			_upgradeLevel = level;
		}
	}

	protected static Logger _log = Logger.getLogger(SiegeManager.class.getName());

	private static SiegeManager _instance;

	public final static void addSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(character.isNoble()))
		{
			character.addSkill(sk, false);
		}
	}

	public final static boolean checkIfInZone(int x, int y, int z)
	{
		return getSiege(x, y, z) != null;
	}

	public final static boolean checkIfInZone(L2Object obj)
	{
		return getSiege(obj) != null;
	}

	public static boolean checkIfOkToPlaceFlag(L2PcInstance player, boolean isCheckOnly)
	{
		L2Clan clan = player.getClan();
		Siege siege = SiegeManager.getSiege(player);
		Castle castle = siege == null ? null : siege.getCastle();

		SystemMessageId sm = null;

		if (siege == null || !siege.getIsInProgress())
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE;
		}
		else if (clan == null || clan.getLeaderId() != player.getObjectId() || siege.getAttackerClan(clan) == null)
		{
			sm = SystemMessageId.CANNOT_USE_ON_YOURSELF;
		}
		else if (castle == null || !castle.checkIfInZoneHeadQuarters(player))
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE;
		}
		else if (castle.getSiege().getAttackerClan(clan).getNumFlags() >= Config.SIEGE_FLAG_MAX_COUNT)
		{
			sm = SystemMessageId.NOT_ANOTHER_HEADQUARTERS;
		}
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	public final static boolean checkIfOkToSummon(L2PcInstance player, boolean isCheckOnly)
	{
		Siege siege = SiegeManager.getSiege(player);

		SystemMessageId sm = null;

		if (siege == null)
		{
			sm = SystemMessageId.YOU_ARE_NOT_IN_SIEGE;
		}
		else if (!siege.getIsInProgress())
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE;
		}
		else if (player.getClanId() != 0 && siege.getAttackerClan(player.getClanId()) == null)
		{
			sm = SystemMessageId.CANNOT_USE_ON_YOURSELF;
		}
		else if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN && siege.getCastle().getOwnerId() > 0)
		{
			sm = SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING;
		}
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	public static boolean checkIfOkToUseStriderSiegeAssault(L2PcInstance player, boolean isCheckOnly)
	{
		Siege siege = SiegeManager.getSiege(player);

		SystemMessageId sm = null;

		if (siege == null)
		{
			sm = SystemMessageId.YOU_ARE_NOT_IN_SIEGE;
		}
		else if (!siege.getIsInProgress())
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE;
		}
		else if (!(player.getTarget() instanceof L2DoorInstance))
		{
			sm = SystemMessageId.TARGET_IS_INCORRECT;
		}
		else if (!player.isRidingStrider() && !player.isRidingRedStrider())
		{
			sm = SystemMessageId.CANNOT_USE_ON_YOURSELF;
		}
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	public static final SiegeManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new SiegeManager();
		}
		return _instance;
	}

	public final static Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles().values())
			if (castle.getSiege().checkIfInZone(x, y, z))
				return castle.getSiege();
		return null;
	}

	public final static Siege getSiege(L2Clan clan)
	{
		if (clan == null)
			return null;
		for (Castle castle : CastleManager.getInstance().getCastles().values())
		{
			Siege siege = castle.getSiege();
			if (siege.getIsInProgress() && (siege.checkIsAttacker(clan) || siege.checkIsDefender(clan)))
				return siege;
		}
		return null;
	}

	public final static Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final static List<Siege> getSieges()
	{
		List<Siege> sieges = new ArrayList<>();
		for (Castle castle : CastleManager.getInstance().getCastles().values())
		{
			sieges.add(castle.getSiege());
		}
		return sieges;
	}

	public final static void removeSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(character.isNoble()))
		{
			character.removeSkill(sk);
		}
	}

	private Map<Integer, List<SiegeSpawn>> _artefactSpawnList;
	private final Map<Integer, List<TowerSpawn>> _flameTowers = new HashMap<>();
	private Map<Integer, List<SiegeSpawn>> _controlTowerSpawnList;

	private final Map<Integer, List<Integer>> _registredClans = new HashMap<>();

	public boolean checkIfOkToCastSealOfRule(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessageId sm = null;
		L2PcInstance player = (L2PcInstance) activeChar;

		if (castle == null || castle.getCastleId() <= 0 || castle.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm = SystemMessageId.YOU_ARE_NOT_IN_SIEGE;
		}
		else if (player.getTarget() == null && !(player.getTarget() instanceof L2ArtefactInstance))
		{
			sm = SystemMessageId.TARGET_IS_INCORRECT;
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE;
		}
		else if (!Util.checkIfInRange(200, player, player.getTarget(), true))
		{
			sm = SystemMessageId.TARGET_TOO_FAR;
		}
		else
		{
			if (!isCheckOnly)
			{
				castle.getSiege().announceToOpponent(SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING), player.getClan());
			}
			return true;
		}
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}

		return false;
	}

	public final boolean checkIsRegistered(L2Clan clan, int castleid)
	{
		if (clan == null)
			return false;

		if (clan.getHasCastle() > 0)
			return true;

		List<Integer> lst = _registredClans.get(castleid);
		if (lst == null)
			return false;
		return lst.contains(clan.getClanId());

	}

	public final List<SiegeSpawn> getArtefactSpawnList(int _castleId)
	{
		if (_artefactSpawnList.containsKey(_castleId))
			return _artefactSpawnList.get(_castleId);
		return null;
	}

	public final List<SiegeSpawn> getControlTowerSpawnList(int _castleId)
	{
		if (_controlTowerSpawnList.containsKey(_castleId))
			return _controlTowerSpawnList.get(_castleId);
		return null;
	}

	public List<TowerSpawn> getFlameTowers(int castleId)
	{
		return _flameTowers.get(castleId);
	}

	
	public void load()
	{
		loadTowerArtefacts();
		_registredClans.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("SELECT castle_id,clan_id FROM siege_clans ORDER BY castle_id");
			ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				List<Integer> lst = _registredClans.get(rs.getInt("castle_id"));
				if (lst == null)
				{
					lst = new ArrayList<>();
					_registredClans.put(rs.getInt("castle_id"), lst);
				}
				lst.add(rs.getInt("clan_id"));
			}
			rs.close();
			stm.close();
		}
		catch (SQLException e)
		{

		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_log.info("Siege Manager: Loaded " + _registredClans.size() + " registred siege(s)");
	}

	private final void loadTowerArtefacts()
	{
		try
		{
			Properties siegeSettings = new L2Properties(ConfigFiles.SIEGE_CONFIGURATION_FILE).setLog(false);

			_controlTowerSpawnList = new HashMap<>();
			_artefactSpawnList = new HashMap<>();

			for (Castle castle : CastleManager.getInstance().getCastles().values())
			{
				List<SiegeSpawn> _controlTowersSpawns = new ArrayList<>();

				for (int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + Integer.toString(i), "");

					if (_spawnParams.length() == 0)
					{
						break;
					}

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());
						int hp = Integer.parseInt(st.nextToken());

						_controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, npc_id, hp));
					}
					catch (Exception e)
					{
						_log.error("Error while loading control tower(s) for " + castle.getName() + " castle.", e);
					}
				}

				List<SiegeSpawn> _artefactSpawns = new ArrayList<>();

				for (int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + Integer.toString(i), "");

					if (_spawnParams.length() == 0)
					{
						break;
					}

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int heading = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());

						_artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
					}
					catch (Exception e)
					{
						_log.error("Error while loading artefact(s) for " + castle.getName() + " castle.", e);
					}
				}

				final List<TowerSpawn> flameTowers = new ArrayList<>();
				for (int i = 1; i < 0xFF; i++)
				{
					final String parameters = siegeSettings.getProperty(castle.getName() + "FlameTower" + Integer.toString(i), "");

					if (parameters.isEmpty())
					{
						break;
					}

					final StringTokenizer st = new StringTokenizer(parameters.trim(), ",");

					try
					{
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int npcId = Integer.parseInt(st.nextToken());

						final List<Integer> zoneList = new ArrayList<>();

						while (st.hasMoreTokens())
						{
							zoneList.add(Integer.parseInt(st.nextToken()));
						}

						flameTowers.add(new TowerSpawn(npcId, new Location(x, y, z), zoneList));
					}
					catch (Exception e)
					{
						_log.warn("Error while loading flame tower(s) for " + castle.getName() + " castle.");
					}
				}

				_controlTowerSpawnList.put(castle.getCastleId(), _controlTowersSpawns);
				_artefactSpawnList.put(castle.getCastleId(), _artefactSpawns);
				_flameTowers.put(castle.getCastleId(), flameTowers);

				loadTrapUpgrade(castle.getCastleId());
			}
			_log.info("SiegeManager: Loaded " + _controlTowerSpawnList.size() + " Control Towers & " + _flameTowers.size() + " Flame Towers.");
		}
		catch (Exception e)
		{
			_log.error("Error while loading siege data.", e);
		}
	}

	
	private void loadTrapUpgrade(int castleId)
	{
		if (castleId <= 0)
			return;

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_trapupgrade WHERE castleId=?");
			statement.setInt(1, castleId);
			ResultSet rs = statement.executeQuery();

			while (rs.next())
			{
				_flameTowers.get(castleId).get(rs.getInt("towerIndex")).setUpgradeLevel(rs.getInt("level"));
			}

			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadTrapUpgrade(): " + e);
		}
	}

	public void registerClan(int fortid, L2Clan clan)
	{
		List<Integer> lst = _registredClans.get(fortid);
		if (lst == null)
		{
			lst = new ArrayList<>();
			_registredClans.put(fortid, lst);
		}
		if (!lst.contains(clan.getClanId()))
		{
			lst.add(clan.getClanId());
		}
	}

	public final void reload()
	{
		_artefactSpawnList.clear();
		_controlTowerSpawnList.clear();
		Config.loadSiegeConfig();
		loadTowerArtefacts();
	}

	public void removeClan(int fortid, int clan)
	{
		List<Integer> lst = _registredClans.get(fortid);
		if (lst == null)
			return;
		if (lst.contains(clan))
		{
			try
			{
				lst.remove((Object) clan);
			}
			catch (Exception e)
			{

			}
		}
	}

}
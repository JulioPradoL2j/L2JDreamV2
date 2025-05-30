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
import com.dream.game.model.CombatFlag;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

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

public class FortSiegeManager
{
	public class SiegeSpawn
	{
		Location _location;
		private final int _npcId;
		private final int _heading;
		private final int _fortId;
		private final int _id;

		public SiegeSpawn(int fort_id, int x, int y, int z, int heading, int npc_id, int id)
		{
			_fortId = fort_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_id = id;
		}

		public int getFortId()
		{
			return _fortId;
		}

		public int getHeading()
		{
			return _heading;
		}

		public int getId()
		{
			return _id;
		}

		public Location getLocation()
		{
			return _location;
		}

		public int getNpcId()
		{
			return _npcId;
		}
	}

	protected static final Logger _log = Logger.getLogger(FortSiegeManager.class.getName());

	private static FortSiegeManager _instance;

	public final static void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
	}

	public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		L2PcInstance player = (L2PcInstance) activeChar;

		FortSiege siege = FortSiegeManager.getSiege(player);

		if (siege == null)
		{
			sm.addString("You must be on fort ground to place a flag");
		}
		else if (!siege.getIsInProgress())
		{
			sm.addString("You can only place a flag during a siege.");
		}
		else if (siege.getAttackerClan(player.getClan()) == null)
		{
			sm.addString("You must be an attacker to place a flag");
		}
		else if (player.getClan() == null || !player.isClanLeader())
		{
			sm.addString("You must be a clan leader to place a flag");
		}
		else if (siege.getAttackerClan(player.getClan()).getNumFlags() >= Config.FORTSIEGE_FLAG_MAX_COUNT)
		{
			sm.addString("You have already placed the maximum number of flags possible");
		}
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}

		return false;
	}

	public final static boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		L2PcInstance player = (L2PcInstance) activeChar;
		Fort fort = FortManager.getInstance().getFort(player);

		if (fort == null || fort.getFortId() <= 0)
		{
			sm.addString("You must be on fort ground to summon this");
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			sm.addString("You can only summon this during a siege.");
		}
		else if (player.getClanId() != 0 && fort.getSiege().getAttackerClan(player.getClanId()) == null)
		{
			sm.addString("You can only summon this as a registered attacker.");
		}
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}

		return false;
	}

	public static boolean checkIfOkToUseStriderSiegeAssault(L2Character activeChar, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		L2PcInstance player = (L2PcInstance) activeChar;

		FortSiege siege = FortSiegeManager.getSiege(player);

		if (siege == null)
		{
			sm.addString("You must be on fort ground to use strider siege assault");
		}
		else if (!siege.getIsInProgress())
		{
			sm.addString("You can only use strider siege assault during a siege.");
		}
		else if (!(player.getTarget() instanceof L2DoorInstance))
		{
			sm.addString("You can only use strider siege assault on doors and walls.");
		}
		else if (!player.isRidingStrider() && !player.isRidingRedStrider())
		{
			sm.addString("You can only use strider siege assault when on strider.");
		}
		else
			return true;

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}

	public static final FortSiegeManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new FortSiegeManager();
		}
		return _instance;
	}

	public final static FortSiege getSiege(int x, int y, int z)
	{
		for (Fort fort : FortManager.getInstance().getForts())
			if (fort.getSiege().checkIfInZone(x, y, z))
				return fort.getSiege();
		return null;
	}

	public final static FortSiege getSiege(L2Clan clan)
	{
		if (clan == null)
			return null;
		for (Fort fort : FortManager.getInstance().getForts())
		{
			FortSiege siege = fort.getSiege();
			if (siege.getIsInProgress() && (siege.checkIsAttacker(clan) || siege.checkIsDefender(clan)))
				return siege;
		}
		return null;
	}

	public final static FortSiege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final static void removeSiegeSkills(L2PcInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}

	private Map<Integer, List<SiegeSpawn>> _commanderSpawnList;

	private Map<Integer, List<CombatFlag>> _flagList;

	private final Map<Integer, List<Integer>> _registredClans = new HashMap<>();

	private List<FortSiege> _sieges;

	private FortSiegeManager()
	{
		for (Fort fort : FortManager.getInstance().getForts())
		{
			addSiege(fort.getSiege());
			fort.getSiege().getSiegeGuardManager().loadSiegeGuard();
		}
	}

	public boolean activateCombatFlag(L2PcInstance player, L2ItemInstance item)
	{
		if (!checkIfCanPickup(player))
			return false;

		Fort fort = FortManager.getInstance().getFort(player);

		List<CombatFlag> fcf = _flagList.get(fort.getFortId());
		for (CombatFlag cf : fcf)
			if (cf.itemInstance == item)
			{
				cf.activate(player, item);
			}
		return true;
	}

	public final void addSiege(FortSiege fortSiege)
	{
		if (_sieges == null)
		{
			_sieges = new ArrayList<>();
		}
		_sieges.add(fortSiege);
	}

	public boolean checkIfCanPickup(L2PcInstance player)
	{
		if (player.isCombatFlagEquipped())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("The battle for Fort completed"));
			return false;
		}

		Fort fort = FortManager.getInstance().getFort(player);

		if (fort == null || fort.getFortId() <= 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("The battle for Fort completed"));
			return false;
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("The battle for Fort completed"));
			return false;
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("The battle for Fort completed"));
			return false;
		}
		return true;
	}

	public final boolean checkIsRegistered(L2Clan clan, int fortid)
	{
		if (clan == null)
			return false;
		List<Integer> lst = _registredClans.get(fortid);
		if (lst == null)
			return false;
		return lst.contains(clan.getClanId());
	}

	public void dropCombatFlag(L2PcInstance player)
	{
		Fort fort = FortManager.getInstance().getFort(player);
		List<CombatFlag> fcf = _flagList.get(fort.getFortId());
		for (CombatFlag cf : fcf)
			if (cf.playerId == player.getObjectId())
			{
				cf.dropIt();
				if (fort.getSiege().getIsInProgress())
				{
					cf.spawnMe();
				}
			}
	}

	public final List<SiegeSpawn> getCommanderSpawnList(int _fortId)
	{
		if (_commanderSpawnList.containsKey(_fortId))
			return _commanderSpawnList.get(_fortId);

		return null;
	}

	public final List<CombatFlag> getFlagList(int _fortId)
	{
		if (_flagList.containsKey(_fortId))
			return _flagList.get(_fortId);

		return null;
	}

	public final List<FortSiege> getSieges()
	{
		if (_sieges == null)
		{
			_sieges = new ArrayList<>();
		}
		return _sieges;
	}

	public boolean isCombat(int itemId)
	{
		return itemId == Config.FORTSIEGE_COMBAT_FLAG_ID;
	}

	
	public void load()
	{
		loadCommandersFlags();
		_registredClans.clear();
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("SELECT fort_id,clan_id FROM fortsiege_clans ORDER BY fort_id");
			ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				List<Integer> lst = _registredClans.get(rs.getInt("fort_id"));
				if (lst == null)
				{
					lst = new ArrayList<>();
					_registredClans.put(rs.getInt("fort_id"), lst);
				}
				lst.add(rs.getInt("clan_id"));
			}
			rs.close();
			stm.close();
		}
		catch (SQLException e)
		{

		}

		_log.info("Fort Siege Data: Loaded " + _sieges.size() + " siege(s)");
	}

	private final void loadCommandersFlags()
	{
		try
		{
			Properties siegeSettings = new L2Properties(ConfigFiles.SIEGE_CONFIGURATION_FILE).setLog(false);

			_commanderSpawnList = new HashMap<>();
			_flagList = new HashMap<>();

			for (Fort fort : FortManager.getInstance().getForts())
			{
				List<SiegeSpawn> _commanderSpawns = new ArrayList<>();
				List<CombatFlag> _flagSpawns = new ArrayList<>();

				for (int i = 1; i < 5; i++)
				{
					String _spawnParams = siegeSettings.getProperty(fort.getName() + "Commander" + Integer.toString(i), "");

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

						_commanderSpawns.add(new SiegeSpawn(fort.getFortId(), x, y, z, heading, npc_id, i));
					}
					catch (Exception e)
					{
						_log.warn("Error while loading commander(s) for " + fort.getName() + " fort.");
					}
				}

				_commanderSpawnList.put(fort.getFortId(), _commanderSpawns);

				int flag_id = Config.FORTSIEGE_COMBAT_FLAG_ID;
				for (int i = 1; i < 4; i++)
				{
					String _spawnParams = siegeSettings.getProperty(fort.getName() + "Flag" + Integer.toString(i), "");

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

						_flagSpawns.add(new CombatFlag(fort.getFortId(), x, y, z, 0, flag_id));
					}
					catch (Exception e)
					{
						_log.warn("Error while loading flag(s) for " + fort.getName() + " fort.");
					}
				}
				_flagList.put(fort.getFortId(), _flagSpawns);
			}
		}
		catch (Exception e)
		{
			_log.error("Error while loading fortsiege data.", e);
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
		_flagList.clear();
		_commanderSpawnList.clear();
		Config.loadSiegeConfig();
		loadCommandersFlags();
	}

	public void removeClan(int fortid, int clan)
	{
		List<Integer> lst = _registredClans.get(fortid);
		if (lst == null)
			return;
		if (lst.contains(clan))
		{
			lst.remove(clan);
		}
	}
}
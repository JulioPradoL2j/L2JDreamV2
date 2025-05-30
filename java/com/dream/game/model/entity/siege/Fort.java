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
package com.dream.game.model.entity.siege;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.AuctionManager;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortUpdater;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ArtefactInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.Auction;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Fort extends Siegeable
{
	private class endFortressSiege implements Runnable
	{
		private final Fort _f;
		private final L2Clan _clan;

		public endFortressSiege(Fort f, L2Clan clan)
		{
			_f = f;
			_clan = clan;
		}

		@Override
		public void run()
		{
			try
			{
				_f.Engrave(_clan);
			}
			catch (Exception e)
			{
				_log.warn("Exception in endFortressSiege " + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	public class FortFunction
	{
		private class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				_cwh = cwh;
			}

			@Override
			public void run()
			{
				try
				{
					if (getOwnerClan() == null)
						return;
					if (getOwnerClan().getWarehouse().getAdena() >= _fee || !_cwh)
					{
						int fee = _fee;
						boolean newfc = true;
						if (getEndTime() == 0 || getEndTime() == -1)
						{
							if (getEndTime() == -1)
							{
								newfc = false;
								fee = _tempFee;
							}
						}
						else
						{
							newfc = false;
						}
						setEndTime(System.currentTimeMillis() + getRate());
						dbSave(newfc);
						if (_cwh)
						{
							getOwnerClan().getWarehouse().destroyItemByItemId("CS_function_fee", 57, fee, null, null);
							if (_log.isDebugEnabled())
							{
								_log.warn("deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
							}
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		private final int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		private final long _rate;
		private long _endDate;
		protected boolean _inDebt;
		public boolean _cwh;

		private Connection con;

		public FortFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask(cwh);
		}

		
		public void dbSave(boolean newFunction)
		{
			con = null;
			try
			{
				PreparedStatement statement;

				con = L2DatabaseFactory.getInstance().getConnection(con);
				if (newFunction)
				{
					statement = con.prepareStatement("REPLACE INTO fort_functions (fortId, type, lvl, lease, rate, endTime) VALUES (?, ?, ?, ?, ?, ?)");
					statement.setInt(1, getFortId());
					statement.setInt(2, getType());
					statement.setInt(3, getLvl());
					statement.setInt(4, getLease());
					statement.setLong(5, getRate());
					statement.setLong(6, getEndTime());
				}
				else
				{
					statement = con.prepareStatement("UPDATE fort_functions SET lvl = ?, lease = ?, endTime = ? WHERE fortId = ? AND type = ?");
					statement.setInt(1, getLvl());
					statement.setInt(2, getLease());
					statement.setLong(3, getEndTime());
					statement.setInt(4, getFortId());
					statement.setInt(5, getType());
				}
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				if (newFunction)
				{
					_log.error("Can not insert fort functions. Error: " + e.getMessage(), e);
				}
				else
				{
					_log.error("Can not update fort functions. Error: " + e.getMessage(), e);
				}
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		public long getEndTime()
		{
			return _endDate;
		}

		public int getLease()
		{
			return _fee;
		}

		public int getLvl()
		{
			return _lvl;
		}

		public long getRate()
		{
			return _rate;
		}

		public int getType()
		{
			return _type;
		}

		private void initializeTask(boolean cwh)
		{
			if (getOwnerClan() == null)
				return;
			long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), _endDate - currentTime);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0);
			}
		}

		public void setEndTime(long time)
		{
			_endDate = time;
		}

		public void setLease(int lease)
		{
			_fee = lease;
		}

		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}
	}

	public class ScheduleSpecialEnvoysDeSpawn implements Runnable
	{
		private final Fort _fortInst;

		public ScheduleSpecialEnvoysDeSpawn(Fort pFort)
		{
			_fortInst = pFort;
		}

		@Override
		public void run()
		{
			try
			{
				if (_fortInst.getFortState() == 0)
				{
					_fortInst.setFortState(1, 0);
				}
				_fortInst.getSpawnManager().despawnSpecialEnvoys();
			}
			catch (Exception e)
			{
				_log.warn("Exception: ScheduleSpecialEnvoysSpawn() for Fort: " + _fortInst.getName() + " " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	protected static final Logger _log = Logger.getLogger(Fort.class.getName());
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;
	private int _fortId = 0;
	private final List<L2DoorInstance> _doors = new ArrayList<>();
	private L2ArtefactInstance _flagPole = null;
	private L2Npc _flag = null;

	private final List<String> _doorDefault = new ArrayList<>();
	private FortSiege _siege = null;

	private Calendar _siegeDate;
	private long _lastOwnedTime;
	private final FortManager _spawnManager;
	private L2Clan _fortOwner = null;
	private int _fortType = 0;
	private int _state = 0;

	private int _castleId = 0;

	private final Map<Integer, FortFunction> _function;

	private final List<L2Skill> _residentialSkills = new ArrayList<>();

	private Connection con;

	public Fort(int fortId)
	{
		_fortId = fortId;
		loadFlagPoles();
		loadDoor();
		load();

		_function = new HashMap<>();
		if (getOwnerClan() != null)
		{
			setVisibleFlag(true);
			loadFunctions();
		}
		_spawnManager = new FortManager(this);
	}

	public void closeDoor(int doorId)
	{
		openCloseDoor(doorId, false);
	}

	public void endOfSiege(L2Clan clan)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);

	}

	public void Engrave(L2Clan clan)
	{
		setOwner(clan, true);
	}

	@Override
	public final int getCastleId()
	{
		return _castleId;
	}

	public final int getCastleIdFromEnvoy(int npcId)
	{
		return getSpawnManager().getEnvoyCastle(npcId);
	}

	public final L2DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
			return null;

		for (L2DoorInstance door : getDoors())
			if (door.getDoorId() == doorId)
				return door;
		return null;
	}

	public final List<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	public final L2ArtefactInstance getFlagPole()
	{
		return _flagPole;
	}

	@Override
	public final int getFortId()
	{
		return _fortId;
	}

	public final int getFortSize()
	{
		return getFortType() == 0 ? 3 : 5;
	}

	public final int getFortState()
	{
		return _state;
	}

	public final int getFortType()
	{
		return _fortType;
	}

	public FortFunction getFunction(int type)
	{
		return _function.get(type);
	}

	public final int getOwnedTime()
	{
		if (_lastOwnedTime == 0)
			return 0;

		return (int) ((System.currentTimeMillis() - _lastOwnedTime) / 1000);
	}

	public final L2Clan getOwnerClan()
	{
		return _fortOwner;
	}

	public List<L2Skill> getResidentialSkills()
	{
		return _residentialSkills;
	}

	public final FortSiege getSiege()
	{
		if (_siege == null)
		{
			_siege = new FortSiege(new Fort[]
			{
				this
			});
		}

		return _siege;
	}

	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public FortManager getSpawnManager()
	{
		return _spawnManager;
	}

	public void giveResidentialSkills(L2PcInstance player)
	{
		if (_residentialSkills != null && _residentialSkills.size() > 0)
		{
			for (L2Skill sk : _residentialSkills)
			{
				player.addSkill(sk, false);
			}
		}
	}

	
	private void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			statement = con.prepareStatement("SELECT * FROM fort WHERE id = ?");
			statement.setInt(1, getFortId());
			rs = statement.executeQuery();

			while (rs.next())
			{
				_name = rs.getString("name");
				_siegeDate = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				_lastOwnedTime = rs.getLong("lastOwnedTime");
				_ownerId = rs.getInt("owner");
				_fortType = rs.getInt("fortType");
				_state = rs.getInt("state");
				_castleId = rs.getInt("castleId");
			}

			rs.close();
			statement.close();

			if (_ownerId > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(_ownerId);
				if (clan != null)
				{
					setOwnerClan(clan);
					clan.setHasFort(getFortId());
					ThreadPoolManager.getInstance().scheduleGeneral(new FortUpdater(clan, 1), 3600000);
				}
				else
				{
					setOwnerClan(null);
				}
			}
			else
			{
				setOwnerClan(null);
			}

			if (_state == 2 && _castleId != 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(_castleId);
				if (castle != null)
				{
					castle.addContractFort(getFortId());
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadFortData(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void loadDoor()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_staticobjects WHERE fortId = ? AND objectType = ?");
			statement.setInt(1, getFortId());
			statement.setInt(2, 0);
			ResultSet rs = statement.executeQuery();

			while (rs.next())
			{
				_doorDefault.add(rs.getString("name") + ";" + rs.getInt("id") + ";" + rs.getInt("x") + ";" + rs.getInt("y") + ";" + rs.getInt("z") + ";" + rs.getInt("range_xmin") + ";" + rs.getInt("range_ymin") + ";" + rs.getInt("range_zmin") + ";" + rs.getInt("range_xmax") + ";" + rs.getInt("range_ymax") + ";" + rs.getInt("range_zmax") + ";"

				+ rs.getInt("hp") + ";" + rs.getInt("pDef") + ";" + rs.getInt("mDef") + ";" + rs.getString("openType") + ";" + rs.getString("commanderDoor"));
				L2DoorInstance door;
				_doors.add(door = DoorTable.parseLine(_doorDefault.get(_doorDefault.size() - 1)));
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				DoorTable.getInstance().putDoor(door);
			}

			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadFortDoor(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void loadFlagPoles()
	{
		con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_staticobjects WHERE fortId = ? AND objectType = ?");
			statement.setInt(1, getFortId());
			statement.setInt(2, 1);
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				_flagPole = new L2ArtefactInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(32027));
				_flagPole.setName(getName());
				_flagPole.spawnMe(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
				_flagPole.setIsInvul(true);
				_flagPole.setFort(this);
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: loadFlagPoles(): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private void loadFunctions()
	{
		con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("SELECT * FROM fort_functions WHERE fortId = ?");
			statement.setInt(1, getFortId());
			rs = statement.executeQuery();
			while (rs.next())
			{
				_function.put(rs.getInt("type"), new FortFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Exception: Fort.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void openCloseDoor(int doorId, boolean open)
	{
		L2DoorInstance door = getDoor(doorId);
		if (door != null)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
		else
		{
			_log.info("Door " + doorId + " not found at fort " + _fortId + "!");
		}
	}

	public void openDoor(int doorId)
	{
		openCloseDoor(doorId, true);
	}

	
	public void removeFunction(int functionType)
	{
		_function.remove(functionType);
		con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("DELETE FROM fort_functions WHERE fortId = ? AND type = ?");
			statement.setInt(1, getFortId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Exception: Fort.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void removeOwner(boolean updateDB)
	{
		setFortState(0, 0);
		L2Clan clan = getOwnerClan();

		if (clan != null)
		{
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				removeResidentialSkills(member);
				member.sendSkillList();
			}

			clan.setHasFort(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			setOwnerClan(null);
			if (updateDB)
			{
				updateOwnerInDB();
			}
		}
	}

	public void removeResidentialSkills(L2PcInstance player)
	{
		if (_residentialSkills != null && _residentialSkills.size() > 0)
		{
			for (L2Skill sk : _residentialSkills)
			{
				player.removeSkill(sk, false);
			}
		}
	}

	public void resetDoors()
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			door.closeMe();
			if (door.getStatus().getCurrentHp() <= 0)
			{
				door.doRevive();
			}
			if (door.getStatus().getCurrentHp() < door.getMaxHp())
			{
				door.getStatus().setCurrentHp(door.getMaxHp());
			}
		}
	}

	
	public final void setFortState(int state, int castleId)
	{
		if (_castleId != 0)
		{
			Castle castle = CastleManager.getInstance().getCastleById(_castleId);
			if (castle != null)
			{
				castle.removeContractFort(getFortId());
			}
		}
		if (state == 2 && castleId != 0)
		{
			Castle castle = CastleManager.getInstance().getCastleById(castleId);
			if (castle != null)
			{
				castle.addContractFort(getFortId());
			}
		}
		_state = state;
		_castleId = castleId;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE fort SET state=?, castleId=? WHERE id = ?");
			statement.setInt(1, getFortState());
			statement.setInt(2, getCastleId());
			statement.setInt(3, getFortId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@SuppressWarnings("null")
	public boolean setOwner(L2Clan clan, boolean updateClansReputation)
	{
		ClanHall hall = ClanHallManager.getInstance().getClanHallByOwner(clan);
		if (hall != null)
		{
			clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("You lost your Clan Hall " + hall.getName()));
			ClanHallManager.getInstance().setFree(hall.getId());
			AuctionManager.getInstance().initNPC(hall.getId());
		}
		if (clan.getAuctionBiddedAt() > 0)
		{
			Auction a = AuctionManager.getInstance().getAuction(clan.getAuctionBiddedAt());
			if (a != null)
			{
				a.cancelAuction();
				clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Your participation in the auction canceled"));
			}
		}

		if (updateClansReputation)
		{
			updateClansReputation(clan, false);
		}

		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			giveResidentialSkills(member);
			member.sendSkillList();
		}

		if (getOwnerClan() != null && clan != null && clan != getOwnerClan())
		{
			updateClansReputation(clan, true);
			L2PcInstance oldLord = getOwnerClan().getLeader().getPlayerInstance();
			if (oldLord != null && oldLord.getMountType() == 2)
			{
				oldLord.dismount();
			}
			removeOwner(true);
		}
		setFortState(0, 0);
		if (clan.getHasCastle() > 0)
		{
			getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Fort again belongs to NPC"), 0, false);
			return false;
		}
		getSpawnManager().spawnSpecialEnvoys();
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(this), 1 * 60 * 60 * 1000);

		if (clan.getHasFort() > 0)
		{
			FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
		}

		setOwnerClan(clan);
		_ownerId = clan.getClanId();
		updateOwnerInDB();

		if (getSiege().getIsInProgress())
		{
			if (Config.FORTSIEGE_REWARD_ID > 0)
			{
				clan.getWarehouse().addItem("Siege", Config.FORTSIEGE_REWARD_ID, Config.FORTSIEGE_REWARD_COUNT, null, null);
				if (clan.getLeader().getPlayerInstance() != null)
				{
					clan.getLeader().getPlayerInstance().sendMessage("Your clan obtain " + Config.FORTSIEGE_REWARD_COUNT + " " + ItemTable.getInstance().getItemName(Config.FORTSIEGE_REWARD_ID));
				}
			}

			getSiege().endSiege();
		}
		return true;
	}

	public final void setOwnerClan(L2Clan clan)
	{
		setVisibleFlag(clan != null ? true : false);
		_fortOwner = clan;
	}

	public final void setSiegeDate(Calendar siegeDate)
	{
		_siegeDate = siegeDate;
	}

	public void setVisibleFlag(boolean val)
	{
		if (val)
		{
			if (_flag == null)
			{
				_flag = new L2Npc(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
				_flag.spawnMe(_flagPole.getX(), _flagPole.getY(), _flagPole.getZ());
				_flag.setIsInvul(true);
			}

		}
		else if (_flag != null)
		{
			_flag.deleteMe();
			_flag = null;
		}
	}

	public void updateClansReputation(L2Clan owner, boolean removePoints)
	{
		if (owner != null)
		{
			if (removePoints)
			{
				owner.setReputationScore(owner.getReputationScore() - 400, true);
			}
			else
			{
				owner.setReputationScore(owner.getReputationScore() + 200, true);
			}
			owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
		}
	}

	public boolean updateFunctions(L2PcInstance player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (player == null)
			return false;
		if (_log.isDebugEnabled())
		{
			_log.warn("Called Fort.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());
		}
		if (lease > 0)
			if (!player.destroyItemByItemId("Consume", 57, lease, null, true))
				return false;
		if (addNew)
		{
			_function.put(type, new FortFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else if (lvl == 0 && lease == 0)
		{
			removeFunction(type);
		}
		else
		{
			int diffLease = lease - _function.get(type).getLease();
			if (_log.isDebugEnabled())
			{
				_log.warn("Called Fort.updateFunctions diffLease : " + diffLease);
			}
			if (diffLease > 0)
			{
				_function.remove(type);
				_function.put(type, new FortFunction(type, lvl, lease, 0, rate, -1, false));
			}
			else
			{
				_function.get(type).setLease(lease);
				_function.get(type).setLvl(lvl);
				_function.get(type).dbSave(false);
			}
		}
		return true;
	}

	
	private void updateOwnerInDB()
	{
		L2Clan clan = getOwnerClan();
		int clanId = 0;

		if (clan != null)
		{
			clanId = clan.getClanId();
			_lastOwnedTime = System.currentTimeMillis();
		}
		else
		{
			_lastOwnedTime = 0;
		}

		con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE fort SET owner = ?, lastOwnedTime = ?, state = ?, castleId = ? WHERE id = ?");
			statement.setInt(1, clanId);
			statement.setLong(2, _lastOwnedTime);
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setInt(5, getFortId());
			statement.execute();
			statement.close();

			if (clan != null)
			{
				clan.setHasFort(getFortId());
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
				for (L2PcInstance player : pls)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString(clan.getName() + " win in the battle of Fort " + getName()));
				}

				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				ThreadPoolManager.getInstance().scheduleGeneral(new FortUpdater(clan, 1), 3600000); // Schedule owner
				// tasks to start
				// running
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

}
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
package com.dream.game.model.entity;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.manager.AuctionManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanHall extends Entity
{
	public class ClanHallFunction
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
					if (_isFree)
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
							getOwnerClan().getWarehouse().destroyItemByItemId("CH_function_fee", 57, fee, null, null);
							if (_log.isDebugEnabled() || Config.DEBUG)
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
					_log.error(e.getMessage(), e);
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

		public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeFunctionTask(cwh);
		}

		
		public void dbSave(boolean newFunction)
		{
			Connection con = null;
			try
			{
				PreparedStatement statement;

				con = L2DatabaseFactory.getInstance().getConnection(con);
				if (newFunction)
				{
					statement = con.prepareStatement("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
					statement.setInt(1, getId());
					statement.setInt(2, getType());
					statement.setInt(3, getLvl());
					statement.setInt(4, getLease());
					statement.setLong(5, getRate());
					statement.setLong(6, getEndTime());
				}
				else
				{
					statement = con.prepareStatement("UPDATE clanhall_functions SET lvl=?, lease=?, endTime=? WHERE hall_id=? AND type=?");
					statement.setInt(1, getLvl());
					statement.setInt(2, getLease());
					statement.setLong(3, getEndTime());
					statement.setInt(4, getId());
					statement.setInt(5, getType());
				}
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.fatal("Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
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

		private void initializeFunctionTask(boolean cwh)
		{
			if (_isFree)
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

	private class FeeTask implements Runnable
	{
		public FeeTask()
		{
		}

		@Override
		public void run()
		{
			try
			{
				if (_isFree)
					return;
				L2Clan Clan = getOwnerClan();
				if (getOwnerClan().getWarehouse().getAdena() >= getLease())
				{
					if (_paidUntil != 0)
					{
						while (_paidUntil < System.currentTimeMillis())
						{
							_paidUntil += _chRate;
						}
					}
					else
					{
						_paidUntil = System.currentTimeMillis() + _chRate;
					}
					_paidDayTime = _paidUntil;
					getOwnerClan().getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
					if (_log.isDebugEnabled() || Config.DEBUG)
					{
						_log.warn("deducted " + getLease() + " adena from " + getName() + " owner's cwh for ClanHall _paidUntil" + _paidUntil);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - System.currentTimeMillis());
					_paid = true;
					updateDb();
				}
				else
				{
					_paid = false;
					if (System.currentTimeMillis() > _paidUntil + _chRate)
					{
						if (ClanHallManager.loaded())
						{
							AuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							Clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
							_log.info("ClanHall: " + getName() + " set to FREE. Old Clan owner : " + Clan.getName() + ". Adena count in CWH : " + Clan.getWarehouse().getAdena());
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 3000);
						}
					}
					else
					{
						Clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_TOMORROW));
						if (System.currentTimeMillis() + 1000 * 60 * 60 * 24 <= _paidUntil + _chRate)
						{
							_paidDayTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
						}
						else
						{
							_paidDayTime = _paidUntil + _chRate;
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidDayTime - System.currentTimeMillis());
						updateDb();
					}
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;
	private final int _clanHallId;
	private List<L2DoorInstance> _doors;
	private final List<String> _doorDefault;
	private final String _name;
	private int _ownerId;
	private L2Clan _ownerClan;
	private final int _lease;

	private final String _desc;
	private final String _location;
	protected long _paidUntil;
	protected long _paidDayTime;
	private final int _grade;
	protected final int _chRate = 604800000;
	protected boolean _isFree = true;
	protected boolean _isUnderSiege = false;

	private final Map<Integer, ClanHallFunction> _functions;

	protected boolean _paid;

	public ClanHall(int clanHallId, String name, int ownerId, int lease, String desc, String location, long paidUntil, long paidDayTime, int Grade, boolean paid)
	{
		_clanHallId = clanHallId;
		_name = name;
		_ownerId = ownerId;

		_lease = lease;
		_desc = desc;
		_location = location;
		_paidUntil = paidUntil;
		_paidDayTime = paidDayTime;
		_grade = Grade;
		_paid = paid;
		_doorDefault = new ArrayList<>();
		_functions = new HashMap<>();
		if (ownerId != 0)
		{
			_isFree = false;
			initializeTask(false);
			loadFunctions();
		}
	}

	@Override
	public boolean checkBanish(L2PcInstance cha)
	{
		return cha.getClanId() != getOwnerId();
	}

	public void free()
	{
		_ownerId = 0;
		_ownerClan = null;
		_isFree = true;
		for (Map.Entry<Integer, ClanHallFunction> fc : _functions.entrySet())
		{
			removeFunction(fc.getKey());
		}
		_functions.clear();
		_paidUntil = 0;
		_paidDayTime = 0;
		updateDb();
	}

	public final String getDesc()
	{
		return _desc;
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
		if (_doors == null)
		{
			_doors = new ArrayList<>();
		}
		return _doors;
	}

	public ClanHallFunction getFunction(int type)
	{
		if (_functions.get(type) != null)
			return _functions.get(type);
		return null;
	}

	public final int getGrade()
	{
		return _grade;
	}

	public final int getId()
	{
		return _clanHallId;
	}

	public final int getLease()
	{
		return _lease;
	}

	public final String getLocation()
	{
		return _location;
	}

	public final String getName()
	{
		return _name;
	}

	public L2Clan getOwnerClan()
	{
		if (_ownerId == 0)
			return null;

		if (_ownerClan == null)
		{
			_ownerClan = ClanTable.getInstance().getClan(getOwnerId());
		}

		return _ownerClan;
	}

	public final int getOwnerId()
	{
		return _ownerId;
	}

	public final boolean getPaid()
	{
		return _paid;
	}

	public final long getPaidUntil()
	{
		return _paidUntil;
	}

	private void initializeTask(boolean forced)
	{
		long currentTime = System.currentTimeMillis();
		if (_paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - currentTime);
		}
		else if (!_paid && !forced)
		{
			if (_paidDayTime > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidDayTime - currentTime);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
		}
	}

	public final boolean isUnderSiege()
	{
		return _isUnderSiege;
	}

	
	private void loadFunctions()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("SELECT * FROM clanhall_functions WHERE hall_id = ?");
			statement.setInt(1, getId());
			rs = statement.executeQuery();
			while (rs.next())
			{
				_functions.put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
			}
			statement.close();
		}
		catch (SQLException e)
		{
			_log.fatal("Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}

	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
		if (door != null)
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
	}

	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoor(doorId, open);
		}
	}

	public void openCloseDoors(boolean open)
	{
		for (L2DoorInstance door : getDoors())
			if (door != null)
				if (open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
	}

	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoors(open);
		}
	}

	
	public void removeFunction(int functionType)
	{
		_functions.remove(functionType);
		Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
			statement.setInt(1, getId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.fatal("Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setOwner(L2Clan clan)
	{
		if (_ownerId > 0 || clan == null)
			return;

		_ownerId = clan.getClanId();
		_ownerClan = null;
		_isFree = false;
		_paidUntil = System.currentTimeMillis();
		initializeTask(true);
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}

	public void setUnderSiege(boolean par)
	{
		_isUnderSiege = par;
	}

	public void spawnDoor()
	{
		spawnDoor(false);
	}

	public void spawnDoor(boolean isDoorWeak)
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if (door.getStatus().getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseLine(_doorDefault.get(i));
				if (isDoorWeak)
				{
					door.getStatus().setCurrentHp(door.getMaxHp() / 2);
				}
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (door.getOpen())
			{
				door.closeMe();
			}
		}
	}

	
	public void updateDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paidDayTime=?, paid=? WHERE id=?");
			statement.setInt(1, _ownerId);
			statement.setLong(2, _paidUntil);
			statement.setLong(3, _paidDayTime);
			statement.setInt(4, _paid ? 1 : 0);
			statement.setInt(5, _clanHallId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean updateFunctions(L2PcInstance player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (player == null)
			return false;

		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.warn("Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());
		}

		if (lease > 0)
			if (!player.destroyItemByItemId("Consume", 57, lease, null, true))
				return false;
		if (addNew)
		{
			_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else if (lvl == 0 && lease == 0)
		{
			removeFunction(type);
		}
		else
		{
			int diffLease = lease - _functions.get(type).getLease();
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.warn("Called ClanHall.updateFunctions diffLease : " + diffLease);
			}
			if (diffLease > 0)
			{
				_functions.remove(type);
				_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, -1, false));
			}
			else
			{
				_functions.get(type).setLease(lease);
				_functions.get(type).setLvl(lvl);
				_functions.get(type).dbSave(false);
			}
		}
		return true;
	}
}
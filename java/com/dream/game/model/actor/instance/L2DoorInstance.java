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
package com.dream.game.model.actor.instance;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2DoorAI;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.knownlist.DoorKnownList;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.actor.stat.DoorStat;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ConfirmDlg;
import com.dream.game.network.serverpackets.DoorInfo;
import com.dream.game.network.serverpackets.DoorStatusUpdate;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.StaticObject;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2CharTemplate;
import com.dream.game.templates.item.L2Weapon;

public class L2DoorInstance extends L2Character
{
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		@Override
		public void doAttack(L2Character target)
		{
		}

		@Override
		public void doCast(L2Skill skill)
		{
		}

		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}

		@Override
		public boolean moveTo(int x, int y, int z)
		{
			return false;
		}

		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}

		@Override
		public void stopMove(L2CharPosition pos)
		{
		}
	}

	class AutoOpenClose implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!getOpen())
				{
					openMe();
				}
				else
				{
					closeMe();
				}
			}
			catch (Exception e)
			{
				_log.warn("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
				e.printStackTrace();
			}
		}
	}

	class CloseTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				onClose();
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	private int _castleIndex = -2;

	private Castle _castle;

	private int _fortId = -2;
	private Fort _fort;
	private L2MapRegion _mapRegion = null;
	protected final int _doorId;
	protected final String _name;
	private boolean _open;

	private boolean _isCommanderDoor;
	private final boolean _unlockable;
	public boolean _isCHDoor = false;
	private int _rangeXMin = 0;
	private int _rangeYMin = 0;
	private int _rangeZMin = 0;

	private int _rangeXMax = 0;
	private int _rangeYMax = 0;
	private int _rangeZMax = 0;
	private int _A = 0;

	private int _B = 0;

	private int _C = 0;
	private int _D = 0;

	private ClanHall _clanHall;

	protected int _autoActionDelay = -1;

	private ScheduledFuture<?> _autoActionTask;

	private int _upgradeHpRatio = 1;

	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastPacket(new StaticObject(this));
		broadcastPacket(new DoorStatusUpdate(this));
	}

	@Override
	public final void broadcastStatusUpdateImpl()
	{
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if (knownPlayers == null || knownPlayers.isEmpty())
			return;

		DoorInfo su = new DoorInfo(this, false);
		DoorStatusUpdate dsu = new DoorStatusUpdate(this);

		for (L2PcInstance player : knownPlayers)
		{
			if (getCastle() != null && getCastle().getCastleId() > 0 || getFort() != null && getFort().getFortId() > 0 && !getIsCommanderDoor())
			{
				su = new DoorInfo(this, true);
			}

			player.sendPacket(su);
			player.sendPacket(dsu);
		}
	}

	public final void closeMe()
	{
		setOpen(false);
		broadcastFullInfo();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (super.doDie(killer))
		{
			if (getCastle() != null && getCastle().getSiege().getIsInProgress())
			{
				getCastle().getSiege().announceToParticipants(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_GATE_BROKEN_DOWN));
			}
			return true;
		}
		return false;
	}

	public int getA()
	{
		return _A;
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2DoorAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	public int getB()
	{
		return _B;
	}

	public int getC()
	{
		return _C;
	}

	public final Castle getCastle()
	{
		if (_castle == null)
		{
			Castle castle = null;

			if (_castleIndex < 0)
			{
				castle = CastleManager.getInstance().getCastle(this);
				if (castle != null)
				{
					_castleIndex = castle.getCastleId();
				}
			}
			if (_castleIndex > 0)
			{
				castle = CastleManager.getInstance().getCastleById(_castleIndex);
			}
			_castle = castle;
		}
		return _castle;
	}

	public ClanHall getClanHall()
	{
		return _clanHall;
	}

	public int getD()
	{
		return _D;
	}

	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getStatus().getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6)
			return 6;
		if (dmg < 0)
			return 0;
		return dmg;
	}

	public int getDistanceToForgetObject(L2Object object)
	{
		if (!(object instanceof L2PcInstance))
			return 0;

		return 4000;
	}

	public int getDistanceToWatchObject(L2Object object)
	{
		if (!(object instanceof L2PcInstance))
			return 0;
		return 3000;
	}

	public int getDoorId()
	{
		return _doorId;
	}

	public String getDoorName()
	{
		return _name;
	}

	public final Fort getFort()
	{
		if (_fort == null)
		{
			Fort fort = null;

			if (_fortId < 0)
			{
				fort = FortManager.getInstance().getFort(this);
				if (fort != null)
				{
					_fortId = fort.getCastleId();
				}
			}
			if (_fortId > 0)
			{
				fort = FortManager.getInstance().getFortById(_fortId);
			}
			_fort = fort;
		}
		return _fort;
	}

	public boolean getIsCommanderDoor()
	{
		return _isCommanderDoor;
	}

	@Override
	public final DoorKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new DoorKnownList(this);
		}

		return (DoorKnownList) _knownList;
	}

	@Override
	public final int getLevel()
	{
		return 1;
	}

	public L2MapRegion getMapRegion()
	{
		return _mapRegion;
	}

	@Override
	public int getMaxHp()
	{
		return super.getMaxHp() * _upgradeHpRatio;
	}

	public boolean getOpen()
	{
		return _open;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public DoorStat getStat()
	{
		if (_stat == null)
		{
			_stat = new DoorStat(this);
		}

		return (DoorStat) _stat;
	}

	public int getUpgradeHpRatio()
	{
		return _upgradeHpRatio;
	}

	public int getXMax()
	{
		return _rangeXMax;
	}

	public int getXMin()
	{
		return _rangeXMin;
	}

	public int getYMax()
	{
		return _rangeYMax;
	}

	public int getYMin()
	{
		return _rangeYMin;
	}

	public int getZMax()
	{
		return _rangeZMax;
	}

	public int getZMin()
	{
		return _rangeZMin;
	}

	public boolean isAttackable(L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isUnlockable() && getFort() == null)
			return true;

		if (!(attacker instanceof L2Playable))
			return false;

		boolean isCastle = getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
		boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress() && !getIsCommanderDoor();

		if (_isCHDoor && (DevastatedCastleSiege.getInstance().getIsInProgress() || FortressOfDeadSiege.getInstance().getIsInProgress()))
			return true;
		else if (getClanHall() != null)
			return false;
		if (isFort)
		{
			if (attacker instanceof L2SummonInstance)
			{
				L2Clan clan = ((L2SummonInstance) attacker).getOwner().getClan();
				if (clan != null && clan == getFort().getOwnerClan())
					return false;
			}
			else if (attacker instanceof L2PcInstance)
			{
				L2Clan clan = ((L2PcInstance) attacker).getClan();
				if (clan != null && clan == getFort().getOwnerClan())
					return false;
			}
		}
		else if (isCastle)
			if (attacker instanceof L2SummonInstance)
			{
				L2Clan clan = ((L2SummonInstance) attacker).getOwner().getClan();
				if (clan != null && clan.getClanId() == getCastle().getOwnerId())
					return false;
			}
			else if (attacker instanceof L2PcInstance)
			{
				L2Clan clan = ((L2PcInstance) attacker).getClan();
				if (clan != null && clan.getClanId() == getCastle().getOwnerId())
					return false;
			}

		return isCastle || isFort;
	}

	public boolean isEnemy()
	{
		if (getCastle() != null && getCastle().getSiege().getIsInProgress())
			return true;
		if (getFort() != null && getFort().getSiege().getIsInProgress() && !getIsCommanderDoor())
			return true;
		if (_isCHDoor && (DevastatedCastleSiege.getInstance().getIsInProgress() || FortressOfDeadSiege.getInstance().getIsInProgress()))
			return true;
		return false;
	}

	public boolean isEnemyOf(L2PcInstance activeChar)
	{
		return false;
	}

	public final boolean isUnlockable()
	{
		return _unlockable;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null)
			return;

		if (Config.SIEGE_ONLY_REGISTERED)
		{
			boolean opp = false;
			Siege siege = SiegeManager.getSiege(player);
			L2Clan oppClan = player.getClan();
			if (siege != null && siege.getIsInProgress())
			{
				if (oppClan != null)
				{
					for (L2SiegeClan clan : siege.getAttackerClans())
					{
						L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

						if (cl == oppClan || cl.getAllyId() == player.getAllyId())
						{
							opp = true;
							break;
						}
					}

					for (L2SiegeClan clan : siege.getDefenderClans())
					{
						L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

						if (cl == oppClan || cl.getAllyId() == player.getAllyId())
						{
							opp = true;
							break;
						}
					}
				}
			}
			else if (_isCHDoor && (DevastatedCastleSiege.getInstance().getIsInProgress() || FortressOfDeadSiege.getInstance().getIsInProgress()))
			{
				opp = true;
			}
			else
			{
				opp = true;
			}

			if (!opp)
				return;
		}

		if (this != player.getTarget())
		{
			player.setTarget(this);

			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			player.sendPacket(new DoorStatusUpdate(this));

			player.sendPacket(new ValidateLocation(this));
		}
		else if (isAutoAttackable(player))
		{
			if (Math.abs(player.getZ() - getZ()) < 400)
			{
				player.getAI().setIntention(CtrlIntention.ATTACK, this);
			}
		}
		else if (player.getClan() != null && getClanHall() != null && player.getClanId() == getClanHall().getOwnerId())
		{
			if (!isInsideRadius(player, L2Npc.INTERACTION_DISTANCE, false, false))
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				player.gatesRequest(this);
				if (!getOpen())
				{
					player.sendPacket(new ConfirmDlg(1140));
				}
				else
				{
					player.sendPacket(new ConfirmDlg(1141));
				}
			}
		}
		else if (player.getClan() != null && getFort() != null && player.getClanId() == getFort().getOwnerId() && isUnlockable() && !getFort().getSiege().getIsInProgress())
			if (!isInsideRadius(player, L2Npc.INTERACTION_DISTANCE, false, false))
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				player.gatesRequest(this);
				if (!getOpen())
				{
					player.sendPacket(new ConfirmDlg(1140));
				}
				else
				{
					player.sendPacket(new ConfirmDlg(1141));
				}
			}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player.isGM())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			player.sendPacket(new DoorStatusUpdate(this));

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder html1 = new StringBuilder("<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td>Current HP  " + getStatus().getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>Max HP      " + getMaxHp() + "</td></tr>");
			html1.append("<tr><td>Max X       " + getXMax() + "</td></tr>");
			html1.append("<tr><td>Max Y       " + getYMax() + "</td></tr>");
			html1.append("<tr><td>Max Z       " + getZMax() + "</td></tr>");
			html1.append("<tr><td>Min X       " + getXMin() + "</td></tr>");
			html1.append("<tr><td>Min Y       " + getYMin() + "</td></tr>");
			html1.append("<tr><td>Min Z       " + getZMin() + "</td></tr>");
			html1.append("<tr><td>Object ID:  " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Door ID: <br>" + getDoorId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table>");

			html1.append("<table><tr>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
			html1.append("</tr></table></body></html>");

			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else
		{

		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onClose()
	{
		closeMe();
	}

	public void onOpen()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
	}

	public final void openMe()
	{
		setOpen(true);
		broadcastFullInfo();
	}

	public void setAutoActionDelay(int actionDelay)
	{
		if (_autoActionDelay == actionDelay)
			return;

		if (actionDelay > -1)
		{
			AutoOpenClose ao = new AutoOpenClose();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ao, actionDelay, actionDelay);
		}
		else if (_autoActionTask != null)
		{
			_autoActionTask.cancel(false);
		}

		_autoActionDelay = actionDelay;
	}

	public void setCHDoor(boolean par)
	{
		_isCHDoor = par;
	}

	public void setClanHall(ClanHall clanHall)
	{
		_clanHall = clanHall;
	}

	public void setIsCommanderDoor(boolean val)
	{
		_isCommanderDoor = val;
	}

	public void setMapRegion(L2MapRegion region)
	{
		_mapRegion = region;
	}

	public void setOpen(boolean open)
	{
		_open = open;
	}

	public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
	{
		_rangeXMin = xMin;
		_rangeYMin = yMin;
		_rangeZMin = zMin;

		_rangeXMax = xMax;
		_rangeYMax = yMax;
		_rangeZMax = zMax;

		_A = _rangeYMax * (_rangeZMax - _rangeZMin) + _rangeYMin * (_rangeZMin - _rangeZMax);
		_B = _rangeZMin * (_rangeXMax - _rangeXMin) + _rangeZMax * (_rangeXMin - _rangeXMax);
		_C = _rangeXMin * (_rangeYMax - _rangeYMin) + _rangeXMin * (_rangeYMin - _rangeYMax);
		_D = -1 * (_rangeXMin * (_rangeYMax * _rangeZMax - _rangeYMin * _rangeZMax) + _rangeXMax * (_rangeYMin * _rangeZMin - _rangeYMin * _rangeZMax) + _rangeXMin * (_rangeYMin * _rangeZMax - _rangeYMax * _rangeZMin));
	}

	public void setUpgradeHpRatio(int hpRatio)
	{
		_upgradeHpRatio = hpRatio;
	}

	@Override
	public String toString()
	{
		return "door " + _doorId;
	}

	@Override
	public void updateAbnormalEffect()
	{

	}
}
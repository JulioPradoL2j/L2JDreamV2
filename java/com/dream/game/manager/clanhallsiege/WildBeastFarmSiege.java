package com.dream.game.manager.clanhallsiege;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.datatables.xml.ZoneTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.ClanHallSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2ClanhallZone;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.game.templates.chars.L2NpcTemplate;

public class WildBeastFarmSiege extends ClanHallSiege
{
	public class clanPlayersInfo
	{
		public String _clanName;
		public L2SiegeFlagInstance _flag = null;
		public L2MonsterInstance _mob = null;
		public List<String> _players = new ArrayList<>();
	}

	public class midSiegeStep implements Runnable
	{
		@Override
		public void run()
		{
			_mobControlTask.cancel();
			L2Clan winner = checkHaveWinner();
			if (winner != null)
			{
				if (clanhall.getOwnerClan() == null)
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
					anonce("Attention !!! Clan Hall, Beast Farm clan was conquered " + winner.getName(), 2);
					endSiege(false);
				}
				else
				{
					startSecondStep(winner);
				}
			}
			else
			{
				endSiege(true);
			}
		}
	}

	public class startFirstStep implements Runnable
	{
		@Override
		public void run()
		{
			teleportPlayers();
			gateControl(2);
			int mobCounter = 1;
			for (String clanName : getRegisteredClans())
			{
				L2NpcTemplate template;
				L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
				template = NpcTable.getInstance().getTemplate(35617 + mobCounter);
				template.setServerSideTitle(true);
				template.setTitle(clan.getName());
				L2MonsterInstance questMob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), template);
				questMob.setHeading(100);
				questMob.getStatus().setCurrentHpMp(questMob.getMaxHp(), questMob.getMaxMp());
				if (mobCounter == 1)
				{
					questMob.spawnMe(57069, -91797, -1360);
				}
				else if (mobCounter == 2)
				{
					questMob.spawnMe(58838, -92232, -1354);
				}
				else if (mobCounter == 3)
				{
					questMob.spawnMe(57327, -93373, -1365);
				}
				else if (mobCounter == 4)
				{
					questMob.spawnMe(57820, -91740, -1354);
				}
				else if (mobCounter == 5)
				{
					questMob.spawnMe(58728, -93487, -1360);
				}
				clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._mob = questMob;
				mobCounter++;
			}
			_mobControlTask.schedule(3000);
			anonce("The battle has started. Kill the NPC enemy", 1);
		}
	}

	protected static Logger _log = Logger.getLogger(WildBeastFarmSiege.class.getName());

	private static WildBeastFarmSiege _instance;

	public static final WildBeastFarmSiege getInstance()
	{
		if (_instance == null)
		{
			_instance = new WildBeastFarmSiege();
		}
		return _instance;
	}

	public static final WildBeastFarmSiege load()
	{
		_log.info("Clan Hall Siege: Wild Beats Farm Initialized.");
		if (_instance == null)
		{
			_instance = new WildBeastFarmSiege();
		}
		return _instance;
	}

	private boolean _registrationPeriod = false;
	private int _clanCounter = 0;
	public final Map<Integer, clanPlayersInfo> _clansInfo = new ConcurrentHashMap<>();

	private final L2Zone zone = ZoneTable.getInstance().getZone(L2Zone.ZoneType.Clanhall, "Beast Farm");

	public ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(63);

	public final clanPlayersInfo _ownerClanInfo = new clanPlayersInfo();

	public boolean _finalStage = false;

	public ScheduledFuture<?> _midTimer;

	private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (getIsInProgress())
			{
				cancel();
				return;
			}
			Calendar siegeStart = Calendar.getInstance();
			siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
			final long registerTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			siegeStart.add(Calendar.HOUR, 1);
			final long siegeTimeRemaining = siegeStart.getTimeInMillis() - System.currentTimeMillis();
			long remaining = registerTimeRemaining;
			if (registerTimeRemaining <= 0)
				if (!isRegistrationPeriod())
				{
					if (clanhall.getOwnerClan() != null)
					{
						_ownerClanInfo._clanName = clanhall.getOwnerClan().getName();
					}
					else
					{
						_ownerClanInfo._clanName = "";
					}
					setRegistrationPeriod(true);
					anonce("Attention! Registration period began the siege of Clan Hall, Beast Farm.", 2);
					remaining = siegeTimeRemaining;
				}
			if (siegeTimeRemaining <= 0)
			{
				startSiege();
				cancel();
				return;
			}
			schedule(remaining);
		}
	};

	public final ExclusiveTask _endSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (!getIsInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - System.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(true);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};

	public final ExclusiveTask _mobControlTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			int mobCount = 0;
			synchronized (_clansInfo)
			{
				for (Integer key : _clansInfo.keySet())
				{
					clanPlayersInfo cl = _clansInfo.get(key);
					if (cl._mob.isDead())
					{
						L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
						unRegisterClan(clan);
					}
					else
					{
						mobCount++;
					}
				}
			}
			teleportPlayers();
			if (mobCount < 2)
			{
				if (_finalStage)
				{
					_siegeEndDate = Calendar.getInstance();
					_endSiegeTask.cancel();
					_endSiegeTask.schedule(5000);
				}
				else
				{
					_midTimer.cancel(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(), 5000);
				}
			}
			else
			{
				schedule(3000);
			}
		}
	};

	private WildBeastFarmSiege()
	{
		long siegeDate = restoreSiegeDate(63);
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 63, 22);
		_startSiegeTask.schedule(1000);
	}

	public void addPlayer(L2Clan playerClan, String playerName)
	{
		if (playerClan == clanhall.getOwnerClan())
			if (_ownerClanInfo._players.size() < 18)
				if (!_ownerClanInfo._players.contains(playerName))
				{
					_ownerClanInfo._players.add(playerName);
					return;
				}
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.size() < 18)
				if (!regPlayers._players.contains(playerName))
				{
					regPlayers._players.add(playerName);
				}
	}

	public void anonce(String text, int type)
	{
		if (type == 1)
		{
			for (String clanName : getRegisteredClans())
			{
				L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
				for (String playerName : getRegisteredPlayers(clan))
				{
					L2PcInstance cha = L2World.getInstance().getPlayer(playerName);
					if (cha != null)
					{
						cha.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Shout, "Journal", text));
					}
				}
			}
		}
		else
		{
			L2MapRegion region = MapRegionTable.getInstance().getRegion(53508, -93776, -1584);
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				if (region == MapRegionTable.getInstance().getRegion(player.getX(), player.getY(), player.getZ()))
				{
					player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Shout, "Journal", text));
				}
		}
	}

	public L2Clan checkHaveWinner()
	{
		L2Clan res = null;
		int questMobCount = 0;
		for (String clanName : getRegisteredClans())
		{
			L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (getQuestMob(clan) != null)
			{
				res = clan;
				questMobCount++;
			}
		}
		if (questMobCount > 1)
			return null;
		return res;
	}

	public void endSiege(boolean par)
	{
		_mobControlTask.cancel();
		_finalStage = false;
		if (par)
		{
			L2Clan winner = checkHaveWinner();
			if (winner != null)
			{
				ClanHallManager.getInstance().setOwner(clanhall.getId(), winner);
				anonce("Attention! Clan Hall, Beast Farm clan was conquered " + winner.getName(), 2);
			}
			else
			{
				anonce("Attention! Clan Hall, Beast Farm has not received a new owner", 2);
			}
		}
		setIsInProgress(false);
		clanhall.setUnderSiege(false);
		((L2ClanhallZone) zone).updateSiegeStatus();
		unSpawnAll();
		_clansInfo.clear();
		_clanCounter = 0;
		teleportPlayers();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(), 63, 22);
		_startSiegeTask.schedule(1000);
	}

	public void gateControl(int val)
	{
		if (val == 1)
		{
			DoorTable.getInstance().getDoor(21150003).openMe();
			DoorTable.getInstance().getDoor(21150004).openMe();
			DoorTable.getInstance().getDoor(21150001).closeMe();
			DoorTable.getInstance().getDoor(21150002).closeMe();
		}
		else if (val == 2)
		{
			DoorTable.getInstance().getDoor(21150001).closeMe();
			DoorTable.getInstance().getDoor(21150002).closeMe();
			DoorTable.getInstance().getDoor(21150003).closeMe();
			DoorTable.getInstance().getDoor(21150004).closeMe();
		}
	}

	public int getPlayersCount(String playerClan)
	{
		for (clanPlayersInfo a : _clansInfo.values())
			if (a._clanName == playerClan)
				return a._players.size();
		return 0;
	}

	public L2MonsterInstance getQuestMob(L2Clan clan)
	{
		clanPlayersInfo clanInfo = _clansInfo.get(clan.getClanId());
		if (clanInfo != null)
			return clanInfo._mob;
		return null;
	}

	public List<String> getRegisteredClans()
	{
		List<String> clans = new ArrayList<>();
		for (clanPlayersInfo a : _clansInfo.values())
		{
			clans.add(a._clanName);
		}
		return clans;
	}

	public List<String> getRegisteredPlayers(L2Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
			return _ownerClanInfo._players;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			return regPlayers._players;
		return null;
	}

	public L2SiegeFlagInstance getSiegeFlag(L2Clan playerClan)
	{
		clanPlayersInfo clanInfo = _clansInfo.get(playerClan.getClanId());
		if (clanInfo != null)
			return clanInfo._flag;
		return null;
	}

	public boolean isClanOnSiege(L2Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
			return true;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers == null)
			return false;
		return true;
	}

	public boolean isPlayerRegister(L2Clan playerClan, String playerName)
	{
		if (playerClan == null)
			return false;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
				return true;
		return false;
	}

	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}

	public synchronized int registerClanOnSiege(L2PcInstance player, L2Clan playerClan)
	{
		if (_clanCounter == 5)
			return 2;
		L2ItemInstance item = player.getInventory().getItemByItemId(8293);
		if (item != null && player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false))
		{
			_clanCounter++;
			clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
			if (regPlayers == null)
			{
				regPlayers = new clanPlayersInfo();
				regPlayers._clanName = playerClan.getName();
				_clansInfo.put(playerClan.getClanId(), regPlayers);
			}
		}
		else
			return 1;
		return 0;
	}

	public void removePlayer(L2Clan playerClan, String playerName)
	{
		if (playerClan == clanhall.getOwnerClan())
			if (_ownerClanInfo._players.contains(playerName))
			{
				_ownerClanInfo._players.remove(playerName);
				return;
			}
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
			if (regPlayers._players.contains(playerName))
			{
				regPlayers._players.remove(playerName);
			}
	}

	public void setRegistrationPeriod(boolean par)
	{
		_registrationPeriod = par;
	}

	public void spawnFlags()
	{
		int flagCounter = 1;
		for (String clanName : getRegisteredClans())
		{
			L2NpcTemplate template;
			L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (clan == clanhall.getOwnerClan())
			{
				template = NpcTable.getInstance().getTemplate(35422);
			}
			else
			{
				template = NpcTable.getInstance().getTemplate(35422 + flagCounter);
			}
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(null, IdFactory.getInstance().getNextId(), template, false, true, clan);
			flag.setTitle(clan.getName());
			flag.setHeading(100);
			flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			if (clan == clanhall.getOwnerClan())
			{
				flag.spawnMe(58782, -93180, -1354);
			}
			else if (flagCounter == 1)
			{
				flag.spawnMe(56769, -92097, -1360);
			}
			else if (flagCounter == 2)
			{
				flag.spawnMe(59138, -92532, -1354);
			}
			else if (flagCounter == 3)
			{
				flag.spawnMe(57027, -93673, -1365);
			}
			else if (flagCounter == 4)
			{
				flag.spawnMe(58120, -91440, -1354);
			}
			else if (flagCounter == 5)
			{
				flag.spawnMe(58428, -93787, -1360);
			}
			clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
			regPlayers._flag = flag;
			flagCounter++;
		}
	}

	public void startSecondStep(L2Clan winner)
	{
		List<String> winPlayers = WildBeastFarmSiege.getInstance().getRegisteredPlayers(winner);
		unSpawnAll();
		_clansInfo.clear();
		clanPlayersInfo regPlayers = new clanPlayersInfo();
		regPlayers._clanName = winner.getName();
		regPlayers._players = winPlayers;
		_clansInfo.put(winner.getClanId(), regPlayers);
		_clansInfo.put(clanhall.getOwnerClan().getClanId(), _ownerClanInfo);
		spawnFlags();
		gateControl(1);
		_finalStage = true;
		anonce("Take place from their siege headquarters.", 1);
		ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5 * 60000);
	}

	public void startSiege()
	{
		setRegistrationPeriod(false);
		if (_clansInfo.size() == 0)
		{
			endSiege(false);
			return;
		}
		if (_clansInfo.size() == 1 && clanhall.getOwnerClan() == null)
		{
			endSiege(false);
			return;
		}
		if (_clansInfo.size() == 1 && clanhall.getOwnerClan() != null)
		{
			L2Clan clan = null;
			for (clanPlayersInfo a : _clansInfo.values())
			{
				clan = ClanTable.getInstance().getClanByName(a._clanName);
			}
			setIsInProgress(true);
			((L2ClanhallZone) zone).updateSiegeStatus();
			startSecondStep(clan);
			anonce("Take place from their siege headquarters.", 1);
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 30);
			_endSiegeTask.schedule(1000);
			return;
		}
		setIsInProgress(true);
		clanhall.setUnderSiege(true);
		((L2ClanhallZone) zone).updateSiegeStatus();
		spawnFlags();
		gateControl(1);
		anonce("Take place from their siege headquarters.", 1);
		ThreadPoolManager.getInstance().scheduleGeneral(new startFirstStep(), 5 * 60000);
		_midTimer = ThreadPoolManager.getInstance().scheduleGeneral(new midSiegeStep(), 25 * 60000);
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 60);
		_endSiegeTask.schedule(1000);
	}

	public void teleportPlayers()
	{
		for (L2Character cha : zone.getCharactersInside().values())
			if (cha instanceof L2PcInstance)
			{
				L2Clan clan = ((L2PcInstance) cha).getClan();
				if (!isPlayerRegister(clan, cha.getName()))
				{
					cha.teleToLocation(53468, -94092, -1634);
				}
			}
	}

	public boolean unRegisterClan(L2Clan playerClan)
	{
		if (_clansInfo.remove(playerClan.getClanId()) != null)
		{
			_clanCounter--;
			return true;
		}
		return false;
	}

	public void unSpawnAll()
	{
		for (String clanName : getRegisteredClans())
		{
			L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
			L2MonsterInstance mob = getQuestMob(clan);
			L2SiegeFlagInstance flag = getSiegeFlag(clan);
			if (mob != null)
			{
				mob.deleteMe();
			}
			if (flag != null)
			{
				flag.deleteMe();
			}
		}
	}
}
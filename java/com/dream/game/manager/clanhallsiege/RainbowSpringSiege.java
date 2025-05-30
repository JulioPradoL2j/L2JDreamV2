package com.dream.game.manager.clanhallsiege;

import com.dream.Message;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.ClanHallSiege;
import com.dream.game.manager.EventsDropManager;
import com.dream.game.manager.EventsDropManager.ruleType;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2HotSpringSquashInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class RainbowSpringSiege extends ClanHallSiege
{
	public final class ChestsSpawn implements Runnable
	{
		@Override
		public void run()
		{
			if (arenaChestsCnt[0] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(153129 + Rnd.get(-400, 400), -125337 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();
				arena1chests.add(newChest);
				arenaChestsCnt[0]++;
			}
			if (arenaChestsCnt[1] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(153884 + Rnd.get(-400, 400), -127534 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();
				arena2chests.add(newChest);
				arenaChestsCnt[1]++;
			}
			if (arenaChestsCnt[2] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(151560 + Rnd.get(-400, 400), -127075 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();
				arena3chests.add(newChest);
				arenaChestsCnt[2]++;
			}
			if (arenaChestsCnt[3] < 4)
			{
				L2NpcTemplate template;
				template = NpcTable.getInstance().getTemplate(35593);
				L2ChestInstance newChest = new L2ChestInstance(IdFactory.getInstance().getNextId(), template);
				newChest.getStatus().setCurrentHpMp(newChest.getMaxHp(), newChest.getMaxMp());
				newChest.spawnMe(155657 + Rnd.get(-400, 400), -125753 + Rnd.get(-400, 400), -2221);
				newChest.setSpecialDrop();
				arena4chests.add(newChest);
				arenaChestsCnt[3]++;
			}

		}
	}

	public class clanPlayersInfo
	{
		public String _clanName;
		public int _decreeCnt;
		public int _arenaNumber;
	}

	public final class EndSiegeTaks implements Runnable
	{
		@Override
		public void run()
		{
			endSiege(true);
		}
	}

	protected static Logger _log = Logger.getLogger(RainbowSpringSiege.class.getName());

	private static RainbowSpringSiege _instance;

	public static final RainbowSpringSiege getInstance()
	{
		if (_instance == null)
		{
			_instance = new RainbowSpringSiege();
		}
		return _instance;
	}

	public static final RainbowSpringSiege load()
	{
		_log.info("Clan Hall Siege: Rainbow Springs Chateau Initialized.");
		if (_instance == null)
		{
			_instance = new RainbowSpringSiege();
		}
		return _instance;
	}

	private boolean _registrationPeriod = false;
	public ClanHall _clanhall = ClanHallManager.getInstance().getClanHallById(62);
	public final Map<Integer, clanPlayersInfo> _clansInfo = new HashMap<>();
	private final L2Npc[] eti = new L2Npc[]
	{
		null,
		null,
		null,
		null
	};
	private final int[] potionsApply =
	{
		0,
		0,
		0,
		0
	};
	private List<Integer> potionsDefault = new ArrayList<>();
	private final L2HotSpringSquashInstance[] squash = new L2HotSpringSquashInstance[]
	{
		null,
		null,
		null,
		null
	};
	public final int[] arenaChestsCnt =
	{
		0,
		0,
		0,
		0
	};

	private int currArena;
	private List<Integer> _playersOnArena = new ArrayList<>();
	private L2Npc teleporter;
	private ScheduledFuture<?> _chestsSpawnTask;

	private int teamWiner = -1;
	public final List<L2ChestInstance> arena1chests = new ArrayList<>();

	public final List<L2ChestInstance> arena2chests = new ArrayList<>();

	public final List<L2ChestInstance> arena3chests = new ArrayList<>();

	public final List<L2ChestInstance> arena4chests = new ArrayList<>();

	private final int _skillsId[] =
	{
		1086,
		1204,
		1059,
		1085,
		1078,
		1068,
		1240,
		1077,
		1242,
		1062
	};

	private final int _skillsLvl[] =
	{
		2,
		2,
		3,
		3,
		6,
		3,
		3,
		3,
		3,
		2
	};

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
					setRegistrationPeriod(true);
					if (_clanhall.getOwnerId() != 0)
					{
						clanPlayersInfo regPlayers = _clansInfo.get(_clanhall.getOwnerId());
						if (regPlayers == null)
						{
							regPlayers = new clanPlayersInfo();
							regPlayers._clanName = _clanhall.getOwnerClan().getName();
							regPlayers._decreeCnt = 0;
							_clansInfo.put(_clanhall.getOwnerId(), regPlayers);
						}
					}
					anonce("Attention! Registration period began the siege of Clan Hall, Palace of Rainbow Springs.");
					anonce("Attention! Battle Clan Hall, Palace of Rainbow Springs will begin in an hour.");
					remaining = siegeTimeRemaining;
				}
			if (siegeTimeRemaining <= 0)
			{
				setRegistrationPeriod(false);
				startSiege();
				cancel();
				return;
			}
			schedule(remaining);
		}
	};

	private final ExclusiveTask _endSiegeTask = new ExclusiveTask()
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
				endSiege(false);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};

	private final ExclusiveTask _firstStepSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			startFirstStep();
		}
	};

	private RainbowSpringSiege()
	{
		long siegeDate = restoreSiegeDate(62);
		Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 62, 22);
		_startSiegeTask.schedule(1000);
	}

	public void anonce(String text)
	{
		L2MapRegion region = MapRegionTable.getInstance().getRegion(143944, -119196, -2136);
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (region == MapRegionTable.getInstance().getRegion(player.getX(), player.getY(), player.getZ()))
			{
				player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Shout, "Journal", text));
			}
	}

	public void chestDie(L2Character killer, L2ChestInstance chest)
	{
		if (arena1chests.contains(chest))
		{
			arenaChestsCnt[0]--;
			arena1chests.remove(chest);
		}
		if (arena2chests.contains(chest))
		{
			arenaChestsCnt[1]--;
			arena2chests.remove(chest);
		}
		if (arena3chests.contains(chest))
		{
			arenaChestsCnt[2]--;
			arena3chests.remove(chest);
		}
		if (arena4chests.contains(chest))
		{
			arenaChestsCnt[3]--;
			arena4chests.remove(chest);
		}
	}

	public void endSiege(boolean par)
	{
		if (!par)
		{
			setIsInProgress(false);
			unspawnQusetNPC();
			anonce("Siege Clan Hall: " + _clanhall.getName() + " is over.");
			anonce("Clan Hall owner remains the same");
		}
		else
		{
			for (clanPlayersInfo ci : _clansInfo.values())
				if (ci != null)
					if (ci._arenaNumber == teamWiner)
					{
						L2Clan clan = ClanTable.getInstance().getClanByName(ci._clanName);
						if (clan != null)
						{
							ClanHallManager.getInstance().setOwner(_clanhall.getId(), clan);
							anonce("Siege Clan Hall: " + _clanhall.getName() + " is over");
							anonce("Clan Hall became owner " + clan.getName());
						}
					}
		}
		_clansInfo.clear();
		for (int id : _playersOnArena)
		{
			L2PcInstance pl = L2World.getInstance().findPlayer(id);
			if (pl != null)
			{
				pl.teleToLocation(150717, -124818, -2355);
			}
		}
		_playersOnArena = new ArrayList<>();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(), 62, 22);
		_startSiegeTask.schedule(1000);
	}

	public synchronized boolean enterOnArena(L2PcInstance pl)
	{
		L2Clan clan = pl.getClan();
		L2Party party = pl.getParty();
		if (clan == null || party == null)
			return false;
		if (!isClanOnSiege(clan) || !getIsInProgress() || currArena > 3 || !pl.isClanLeader() || party.getMemberCount() < 5)
			return false;

		clanPlayersInfo ci = _clansInfo.get(clan.getClanId());
		if (ci == null)
			return false;
		for (L2PcInstance pm : party.getPartyMembers())
			if (pm == null || pm.getRangeToTarget(teleporter) > 500)
				return false;

		ci._arenaNumber = currArena;
		currArena++;

		for (L2PcInstance pm : party.getPartyMembers())
		{
			if (pm.getPet() != null)
			{
				pm.getPet().unSummon(pm);
			}
			_playersOnArena.add(pm.getObjectId());

			switch (ci._arenaNumber)
			{
				case 0:
					pm.teleToLocation(153129 + Rnd.get(-400, 400), -125337 + Rnd.get(-400, 400), -2221);
					break;
				case 1:
					pm.teleToLocation(153884 + Rnd.get(-400, 400), -127534 + Rnd.get(-400, 400), -2221);
					break;
				case 2:
					pm.teleToLocation(151560 + Rnd.get(-400, 400), -127075 + Rnd.get(-400, 400), -2221);
					break;
				case 3:
					pm.teleToLocation(155657 + Rnd.get(-400, 400), -125753 + Rnd.get(-400, 400), -2221);
					break;
			}
		}
		return true;
	}

	public void exchangeItem(L2PcInstance player, int val)
	{
		if (val == 1)
			if (player.destroyItemByItemId("Quest", 8054, 1, player, true) && player.destroyItemByItemId("Quest", 8035, 1, player, true) && player.destroyItemByItemId("Quest", 8052, 1, player, true) && player.destroyItemByItemId("Quest", 8039, 1, player, true) && player.destroyItemByItemId("Quest", 8050, 1, player, true) && player.destroyItemByItemId("Quest", 8051, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8032, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_QUEST_ITEMS));
				return;
			}
		if (val == 2)
			if (player.destroyItemByItemId("Quest", 8054, 1, player, true) && player.destroyItemByItemId("Quest", 8035, 1, player, true) && player.destroyItemByItemId("Quest", 8052, 1, player, true) && player.destroyItemByItemId("Quest", 8039, 1, player, true) && player.destroyItemByItemId("Quest", 8050, 1, player, true) && player.destroyItemByItemId("Quest", 8051, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8031, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_QUEST_ITEMS));
				return;
			}

		if (val == 3)
			if (player.destroyItemByItemId("Quest", 8047, 1, player, true) && player.destroyItemByItemId("Quest", 8039, 1, player, true) && player.destroyItemByItemId("Quest", 8037, 1, player, true) && player.destroyItemByItemId("Quest", 8052, 1, player, true) && player.destroyItemByItemId("Quest", 8035, 1, player, true) && player.destroyItemByItemId("Quest", 8050, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8030, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_QUEST_ITEMS));
				return;
			}
		if (val == 4)
			if (player.destroyItemByItemId("Quest", 8051, 1, player, true) && player.destroyItemByItemId("Quest", 8053, 2, player, true) && player.destroyItemByItemId("Quest", 8046, 1, player, true) && player.destroyItemByItemId("Quest", 8040, 1, player, true) && player.destroyItemByItemId("Quest", 8050, 1, player, true))
			{
				L2ItemInstance item = player.getInventory().addItem("Quest", 8033, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NO_QUEST_ITEMS));
				return;
			}
	}

	public boolean isClanOnSiege(L2Clan playerClan)
	{
		if (playerClan == _clanhall.getOwnerClan())
			return true;
		clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers == null)
			return false;
		return true;
	}

	public boolean isPlayerInArena(L2PcInstance pl)
	{
		if (_playersOnArena.contains(pl.getObjectId()))
			return true;
		return false;
	}

	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}

	public synchronized void onDieSquash(L2HotSpringSquashInstance par)
	{
		if (!getIsInProgress())
			return;
		for (int x = 0; x < squash.length; x++)
			if (squash[x] == par)
			{
				teamWiner = x;
			}
		if (teamWiner >= 0)
		{
			anonce("Attention! One of the participants of the competition, successfully coped with the ordeal.");
			anonce("On the results of the competition for the possession of the Hot Springs Clan Hall will be announced after 2 minutes.");
			setIsInProgress(false);
			unspawnQusetNPC();
			_endSiegeTask.cancel();
			ThreadPoolManager.getInstance().scheduleGeneral(new EndSiegeTaks(), 1000 * 60 * 2);
		}
	}

	public synchronized int registerClanOnSiege(L2PcInstance player, L2Clan playerClan)
	{
		L2ItemInstance item = player.getInventory().getItemByItemId(8034);
		int itemCnt = 0;
		if (item != null)
		{
			itemCnt = item.getCount();
			if (player.destroyItem("RegOnSiege", item.getObjectId(), itemCnt, player, true))
			{
				_log.info("Rainbow Springs Chateau: registered clan " + playerClan.getName() + " get: " + itemCnt + " decree.");
				clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
				if (regPlayers == null)
				{
					regPlayers = new clanPlayersInfo();
					regPlayers._clanName = playerClan.getName();
					regPlayers._decreeCnt = itemCnt;
					_clansInfo.put(playerClan.getClanId(), regPlayers);
				}
			}
		}
		else
			return 0;
		return itemCnt;
	}

	public void removeFromArena(L2PcInstance pl)
	{
		if (_playersOnArena.contains(pl.getObjectId()))
		{
			pl.teleToLocation(150717, -124818, -2355);
		}
	}

	public void setRegistrationPeriod(boolean par)
	{
		_registrationPeriod = par;
	}

	public void shutdown()
	{
		if (isRegistrationPeriod())
		{
			for (clanPlayersInfo cl : _clansInfo.values())
			{
				L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
				if (clan != null && cl._decreeCnt > 0)
				{
					L2PcInstance pl = L2World.getInstance().getPlayer(clan.getLeaderName());
					if (pl != null)
					{
						pl.sendMessage("The storage Clan returned Certificate of Participation in the War Clan Hall Hot Springs");
					}
					clan.getWarehouse().addItem("revert", 8034, cl._decreeCnt, null, null);
				}
			}
		}
		for (int id : _playersOnArena)
		{
			L2PcInstance pl = L2World.getInstance().findPlayer(id);
			if (pl != null)
			{
				pl.teleToLocation(150717, -124818, -2355);
			}
		}
	}

	private void skillsControl(L2PcInstance pl)
	{
		if (pl == null)
			return;
		int x = Rnd.get(0, _skillsId.length - 1);
		L2Skill skill = SkillTable.getInstance().getInfo(_skillsId[x], _skillsLvl[x]);
		if (skill != null)
		{
			skill.getEffects(pl, pl);
		}
	}

	public void startFirstStep()
	{
		potionsDefault = new ArrayList<>();
		L2NpcTemplate template;
		template = NpcTable.getInstance().getTemplate(35596);

		for (int x = 0; x <= 3; x++)
		{
			eti[x] = new L2Npc(IdFactory.getInstance().getNextId(), template);
			eti[x].getStatus().setCurrentHpMp(eti[x].getMaxHp(), eti[x].getMaxMp());
			potionsDefault.add(x + 1);
		}
		eti[0].spawnMe(153129, -125337, -2221);
		eti[1].spawnMe(153884, -127534, -2221);
		eti[2].spawnMe(151560, -127075, -2221);
		eti[3].spawnMe(156657, -125753, -2221);
		template = NpcTable.getInstance().getTemplate(35588);
		for (int x = 3; x >= 0; x--)
		{
			squash[x] = new L2HotSpringSquashInstance(IdFactory.getInstance().getNextId(), template);
			squash[x].getStatus().setCurrentHpMp(squash[x].getMaxHp(), squash[x].getMaxMp());
			potionsApply[x] = potionsDefault.remove(Rnd.get(0, x));
		}
		squash[0].spawnMe(153129 + 50, -125337 + 50, -2221);
		squash[1].spawnMe(153884 + 50, -127534 + 50, -2221);
		squash[2].spawnMe(151560 + 50, -127075 + 50, -2221);
		squash[3].spawnMe(156657 + 50, -125753 + 50, -2221);

		int mobs[] =
		{
			35593
		};
		int item[] =
		{
			8035,
			8037,
			8039,
			8040,
			8046,
			8047,
			8050,
			8051,
			8052,
			8053,
			8054
		};
		int cnt[] =
		{
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1
		};
		int chance[] =
		{
			400,
			400,
			400,
			400,
			400,
			400,
			400,
			400,
			400,
			400,
			400
		};
		EventsDropManager.getInstance().addRule("RainbowSpring", ruleType.BY_NPCID, mobs, item, cnt, chance, false);
		_chestsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ChestsSpawn(), 5000, 5000);
	}

	@SuppressWarnings("unlikely-arg-type")
	public void startSiege()
	{
		if (_startSiegeTask.isScheduled())
		{
			_startSiegeTask.cancel();
		}
		if (_clansInfo.size() == 0)
		{
			endSiege(false);
			return;
		}
		if (_clansInfo.size() > 4)
		{
			for (int x = 1; x < _clansInfo.size() - 4; x++)
			{
				clanPlayersInfo minClan = null;
				int minVal = Integer.MAX_VALUE;
				for (clanPlayersInfo cl : _clansInfo.values())
					if (cl._decreeCnt < minVal)
					{
						minVal = cl._decreeCnt;
						minClan = cl;
					}
				_clansInfo.remove(minClan);
			}
		}
		else if (_clansInfo.size() < 2)
		{
			shutdown();
			anonce("Attention! Clan Hall, Palace of Rainbow Springs has not received a new owner");
			endSiege(false);
			return;
		}
		for (L2Spawn sp : SpawnTable.getInstance().getAllTemplates().values())
			if (sp.getTemplate().getNpcId() == 35603)
			{
				teleporter = sp.getLastSpawn();
			}
		teamWiner = -1;
		currArena = 0;
		setIsInProgress(true);
		anonce("Attention! Competition Clan Hall, Palace of Rainbow Springs will start in 5 minutes.");
		anonce("Attention! members of the clan, you must log on to the arena.");
		for (clanPlayersInfo cl : _clansInfo.values())
		{
			L2Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
			L2PcInstance clanLeader = clan.getLeader().getPlayerInstance();
			if (clanLeader != null)
			{
				clanLeader.sendMessage(Message.getMessage(clanLeader, Message.MessageId.MSG_RAINBOW_GO_TO_ARENA));
			}
		}

		_firstStepSiegeTask.schedule(60000 * 5);
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 65);
		_endSiegeTask.schedule(1000);
	}

	public synchronized boolean unRegisterClan(L2PcInstance player)
	{
		L2Clan playerClan = player.getClan();
		if (_clansInfo.containsKey(playerClan.getClanId()))
		{
			int decreeCnt = _clansInfo.get(playerClan.getClanId())._decreeCnt / 2;
			if (decreeCnt > 0)
			{
				L2ItemInstance item = player.getInventory().addItem("UnRegOnSiege", 8034, decreeCnt, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			return true;
		}
		return false;
	}

	private void unspawnQusetNPC()
	{
		if (_chestsSpawnTask != null)
		{
			_chestsSpawnTask.cancel(true);
		}
		for (L2ChestInstance ch : arena1chests)
		{
			ch.deleteMe();
		}
		for (L2ChestInstance ch : arena2chests)
		{
			ch.deleteMe();
		}
		for (L2ChestInstance ch : arena3chests)
		{
			ch.deleteMe();
		}
		for (L2ChestInstance ch : arena4chests)
		{
			ch.deleteMe();
		}
		for (int x = 0; x < 4; x++)
			if (squash[x] != null)
			{
				squash[x].deleteMe();
			}
	}

	public boolean usePotion(L2Playable activeChar, int potionId)
	{
		if (activeChar instanceof L2PcInstance && isPlayerInArena((L2PcInstance) activeChar) && activeChar.getTarget() instanceof L2Npc && ((L2Npc) activeChar.getTarget()).getTemplate().getIdTemplate() == 35596)
		{
			int action = 0;
			switch (potionId)
			{
				case 8030:
					action = potionsApply[0];
					break;
				case 8031:
					action = potionsApply[1];
					break;
				case 8032:
					action = potionsApply[2];
					break;
				case 8033:
					action = potionsApply[3];
					break;
			}
			if (action == 0)
				return false;
			L2Clan plClan = ((L2PcInstance) activeChar).getClan();
			if (plClan == null)
				return false;
			int playerArena = -1;
			for (clanPlayersInfo cl : _clansInfo.values())
				if (plClan.getName().equalsIgnoreCase(cl._clanName))
				{
					playerArena = cl._arenaNumber;
				}
			if (playerArena == -1)
				return false;
			L2PcInstance player = (L2PcInstance) activeChar;
			if (action == 1)
			{
				double damage = squash[playerArena].getMaxHp() / 100 * Rnd.get(5, 15);
				squash[playerArena].reduceCurrentHp(damage, activeChar);
				activeChar.sendMessage(Message.getMessage(player, Message.MessageId.MSG_RAINBOW_GIVE_DAMMAGE));
			}
			else if (action == 2)
			{
				double hp = squash[playerArena].getMaxHp() / 100 * Rnd.get(5, 15);
				squash[playerArena].getStatus().increaseHp(hp);
				activeChar.sendMessage(Message.getMessage(player, Message.MessageId.MSG_RAINBOW_HEAl));
			}
			else if (action == 3)
			{
				int rndArena = Rnd.get(0, _clansInfo.size() - 1);
				String clName = "";
				if (rndArena == playerArena)
				{
					rndArena++;
				}
				if (rndArena > _clansInfo.size() - 1)
				{
					rndArena = 0;
				}
				for (clanPlayersInfo cl : _clansInfo.values())
					if (cl._arenaNumber == rndArena)
					{
						clName = cl._clanName;
					}
				for (int id : _playersOnArena)
				{
					L2PcInstance pl = L2World.getInstance().findPlayer(id);
					if (pl != null && pl.getClan().getName().equalsIgnoreCase(clName))
					{
						skillsControl(pl);
					}
				}
				activeChar.sendMessage(Message.getMessage(player, Message.MessageId.MSG_RAINBOW_DEBAFF));
			}
			else if (action == 3)
			{
				int rndArena = Rnd.get(0, _clansInfo.size() - 1);
				if (rndArena == playerArena)
				{
					rndArena++;
				}
				if (rndArena > _clansInfo.size() - 1)
				{
					rndArena = 0;
				}
				double hp = squash[rndArena].getMaxHp() / 100 * Rnd.get(5, 15);
				squash[rndArena].getStatus().increaseHp(hp);
				activeChar.sendMessage(Message.getMessage(player, Message.MessageId.MSG_RAINBOW_HEAL_OTHER));
			}
			return true;
		}
		return false;
	}
}
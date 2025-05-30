/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.manager.lastimperialtomb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.manager.grandbosses.BossLair;
import com.dream.game.manager.grandbosses.FrintezzaManager;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.tools.random.Rnd;

public class LastImperialTombManager extends BossLair
{
	private class AnnouncementRegstrationInfo implements Runnable
	{
		private L2Npc _npc = null;
		private final int _remaining;

		public AnnouncementRegstrationInfo(L2Npc npc, int remaining)
		{
			_npc = npc;
			_remaining = remaining;
		}

		@Override
		public void run()
		{
			doAnnouncementRegstrationInfo(_npc, _remaining);
		}
	}

	private class CheckTimeUp implements Runnable
	{
		private final int _remaining;

		public CheckTimeUp(int remaining)
		{
			_remaining = remaining;
		}

		@Override
		public void run()
		{
			doCheckTimeUp(_remaining);
		}
	}

	public class Invade implements Runnable
	{
		@Override
		public void run()
		{
			doInvade();
		}
	}

	public class OpenRoom2InsideDoors implements Runnable
	{
		@Override
		public void run()
		{
			closeRoom2OutsideDoors();
			openRoom2InsideDoors();
		}
	}

	public class SpawnRoom1Mobs1st implements Runnable
	{
		@Override
		public void run()
		{
			L2Npc mob;
			for (L2Spawn spawn : LastImperialTombSpawnlist.getInstance().getRoom1SpawnList1st())
				if (spawn.getNpcid() == 18328)
				{
					mob = spawn.doSpawn();
					mob.getSpawn().stopRespawn();
					_hallAlarmDevices.add(mob);
				}
				else
				{
					mob = spawn.doSpawn();
					mob.getSpawn().stopRespawn();
					_room1Monsters.add(mob);
				}
		}
	}

	public class SpawnRoom1Mobs2nd implements Runnable
	{
		@Override
		public void run()
		{
			L2Npc mob;
			for (L2Spawn spawn : LastImperialTombSpawnlist.getInstance().getRoom1SpawnList2nd())
			{
				mob = spawn.doSpawn();
				mob.getSpawn().stopRespawn();
				_room1Monsters.add(mob);
			}
		}
	}

	public class SpawnRoom1Mobs3rd implements Runnable
	{
		@Override
		public void run()
		{
			L2Npc mob;
			for (L2Spawn spawn : LastImperialTombSpawnlist.getInstance().getRoom1SpawnList3rd())
			{
				mob = spawn.doSpawn();
				mob.getSpawn().stopRespawn();
				_room1Monsters.add(mob);
			}
		}
	}

	public class SpawnRoom1Mobs4th implements Runnable
	{
		@Override
		public void run()
		{
			L2Npc mob;
			for (L2Spawn spawn : LastImperialTombSpawnlist.getInstance().getRoom1SpawnList4th())
			{
				mob = spawn.doSpawn();
				mob.getSpawn().stopRespawn();
				_room1Monsters.add(mob);
			}
		}
	}

	public class SpawnRoom2OutsideMobs implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Spawn spawn : LastImperialTombSpawnlist.getInstance().getRoom2OutsideSpawnList())
				if (spawn.getNpcid() == 18334)
				{
					L2Npc mob = spawn.doSpawn();
					mob.getSpawn().stopRespawn();
					_darkChoirCaptains.add(mob);
				}
				else
				{
					L2Npc mob = spawn.doSpawn();
					mob.getSpawn().startRespawn();
					_room2OutsideMonsters.add(mob);
				}
		}
	}

	public class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			cleanUpTomb();
		}
	}

	private static LastImperialTombManager _instance = null;

	private static boolean _isInvaded = false;

	protected static List<L2Npc> _hallAlarmDevices = new ArrayList<>();

	protected static List<L2Npc> _darkChoirPlayers = new ArrayList<>();

	protected static List<L2Npc> _darkChoirCaptains = new ArrayList<>();

	protected static List<L2Npc> _room1Monsters = new ArrayList<>();

	protected static List<L2Npc> _room2InsideMonsters = new ArrayList<>();

	protected static List<L2Npc> _room2OutsideMonsters = new ArrayList<>();

	protected static L2Npc _messenger;

	protected static List<L2DoorInstance> _room1Doors = new ArrayList<>();
	protected static List<L2DoorInstance> _room2InsideDoors = new ArrayList<>();
	protected static List<L2DoorInstance> _room2OutsideDoors = new ArrayList<>();
	protected static L2DoorInstance _room3Door = null;
	protected static List<L2PcInstance> _partyLeaders = new ArrayList<>();
	protected static List<L2PcInstance> _registedPlayers = new ArrayList<>();

	protected static L2PcInstance _commander = null;

	// delete all mobs from tomb.
	private static void cleanUpMobs()
	{
		for (L2Npc mob : _hallAlarmDevices)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_hallAlarmDevices.clear();

		for (L2Npc mob : _darkChoirPlayers)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_darkChoirPlayers.clear();

		for (L2Npc mob : _darkChoirCaptains)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_darkChoirCaptains.clear();

		for (L2Npc mob : _room1Monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_room1Monsters.clear();

		for (L2Npc mob : _room2InsideMonsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_room2InsideMonsters.clear();

		for (L2Npc mob : _room2OutsideMonsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_room2OutsideMonsters.clear();
	}

	private static void cleanUpRegister()
	{
		_commander = null;
		_partyLeaders.clear();
		_registedPlayers.clear();
	}

	// instance.
	public static LastImperialTombManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new LastImperialTombManager();
		}

		return _instance;
	}

	// setting list of doors and close doors.
	private static void initDoors()
	{
		_room1Doors.clear();
		_room1Doors.add(DoorTable.getInstance().getDoor(25150042));

		for (int i = 25150051; i <= 25150058; i++)
		{
			_room1Doors.add(DoorTable.getInstance().getDoor(i));
		}

		_room2InsideDoors.clear();
		for (int i = 25150061; i <= 25150070; i++)
		{
			_room2InsideDoors.add(DoorTable.getInstance().getDoor(i));
		}

		_room2OutsideDoors.clear();
		_room2OutsideDoors.add(DoorTable.getInstance().getDoor(25150043));
		_room2OutsideDoors.add(DoorTable.getInstance().getDoor(25150045));

		_room3Door = DoorTable.getInstance().getDoor(25150046);

		for (L2DoorInstance door : _room1Doors)
		{
			door.closeMe();
		}

		for (L2DoorInstance door : _room2InsideDoors)
		{
			door.closeMe();
		}

		for (L2DoorInstance door : _room2OutsideDoors)
		{
			door.closeMe();
		}

		_room3Door.closeMe();
	}

	private static void openRoom1Doors()
	{
		for (L2Npc npc : _hallAlarmDevices)
		{
			npc.deleteMe();
			npc.getSpawn().stopRespawn();
		}

		for (L2Npc npc : _room1Monsters)
		{
			npc.deleteMe();
			npc.getSpawn().stopRespawn();
		}

		for (L2DoorInstance door : _room1Doors)
		{
			door.openMe();
		}
	}

	private static void spawnRoom2InsideMob()
	{
		L2Npc mob;
		for (L2Spawn spawn : LastImperialTombSpawnlist.getInstance().getRoom2InsideSpawnList())
		{
			mob = spawn.doSpawn();
			mob.getSpawn().stopRespawn();
			_room2InsideMonsters.add(mob);
		}
	}

	private final int SCROLL = 8073;

	private boolean _isReachToHall = false;

	private final int[][] _invadeLoc =
	{
		{
			173235,
			-76884,
			-5107
		},
		{
			175003,
			-76933,
			-5107
		},
		{
			174196,
			-76190,
			-5107
		},
		{
			174013,
			-76120,
			-5107
		},
		{
			173263,
			-75161,
			-5107
		}
	};

	// task
	protected ScheduledFuture<?> _InvadeTask = null;

	protected ScheduledFuture<?> _RegistrationTimeInfoTask = null;

	protected ScheduledFuture<?> _Room1SpawnTask = null;

	protected ScheduledFuture<?> _Room2InsideDoorOpenTask = null;

	protected ScheduledFuture<?> _Room2OutsideSpawnTask = null;

	protected ScheduledFuture<?> _CheckTimeUpTask = null;

	// Constructor
	public LastImperialTombManager()
	{
	}

	// When the party is annihilated, they are banished.
	@Override
	public void checkAnnihilated()
	{
		if (isPlayersAnnihilated())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					cleanUpTomb();
				}
			}, 5000);
		}
	}

	protected void cleanUpTomb()
	{
		initDoors();
		cleanUpMobs();
		banishForeigners();
		cleanUpRegister();
		_isInvaded = false;
		_isReachToHall = false;

		if (_InvadeTask != null)
		{
			_InvadeTask.cancel(true);
		}
		if (_RegistrationTimeInfoTask != null)
		{
			_RegistrationTimeInfoTask.cancel(true);
		}
		if (_Room1SpawnTask != null)
		{
			_Room1SpawnTask.cancel(true);
		}
		if (_Room2InsideDoorOpenTask != null)
		{
			_Room2InsideDoorOpenTask.cancel(true);
		}
		if (_Room2OutsideSpawnTask != null)
		{
			_Room2OutsideSpawnTask.cancel(true);
		}
		if (_CheckTimeUpTask != null)
		{
			_CheckTimeUpTask.cancel(true);
		}

		_InvadeTask = null;
		_RegistrationTimeInfoTask = null;
		_Room1SpawnTask = null;
		_Room2InsideDoorOpenTask = null;
		_Room2OutsideSpawnTask = null;
		_CheckTimeUpTask = null;
	}

	protected void closeRoom2OutsideDoors()
	{
		for (L2DoorInstance door : _room2OutsideDoors)
		{
			door.closeMe();
		}
		_room3Door.closeMe();
	}

	// announcement of remaining time of registration to players.
	protected void doAnnouncementRegstrationInfo(L2Npc npc, int remaining)
	{
		if (remaining == Config.LIT_REGISTRATION_TIME * 60000)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "Entrance is now possible."));
		}

		if (remaining >= 10000)
		{
			if (remaining > 60000)
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), remaining / 60000 + " minute(s) left for entrance."));
				remaining = remaining - 60000;

				switch (Config.LIT_REGISTRATION_MODE)
				{
					case 1:
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "For entrance, at least " + Config.LIT_MIN_PARTY_CNT + " parties are needed."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), Config.LIT_MAX_PARTY_CNT + " is the maximum party count."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "The current number of registered parties is " + _partyLeaders.size() + "."));
						break;
					case 2:
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "For entrance, at least " + Config.LIT_MIN_PLAYER_CNT + " people are needed."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), Config.LIT_MAX_PLAYER_CNT + " is the capacity."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), _registedPlayers.size() + " people are currently registered."));
						break;
				}

				if (_RegistrationTimeInfoTask != null)
				{
					_RegistrationTimeInfoTask.cancel(true);
				}
				_RegistrationTimeInfoTask = ThreadPoolManager.getInstance().scheduleGeneral(new AnnouncementRegstrationInfo(npc, remaining), 60000);
			}
			else
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), remaining / 60000 + " minute(s) left for entrance."));
				remaining = remaining - 10000;

				switch (Config.LIT_REGISTRATION_MODE)
				{
					case 1:
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "For entrance, at least " + Config.LIT_MIN_PARTY_CNT + " parties are needed."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), Config.LIT_MAX_PARTY_CNT + " is the maximum party count."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "The current number of registered parties is " + _partyLeaders.size() + "."));
						break;
					case 2:
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "For entrance, at least " + Config.LIT_MIN_PLAYER_CNT + " people are needed."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), Config.LIT_MAX_PLAYER_CNT + " is the capacity."));
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), _registedPlayers.size() + " people are currently registered."));
						break;
				}

				if (_RegistrationTimeInfoTask != null)
				{
					_RegistrationTimeInfoTask.cancel(true);
				}
				_RegistrationTimeInfoTask = ThreadPoolManager.getInstance().scheduleGeneral(new AnnouncementRegstrationInfo(npc, remaining), 10000);
			}
		}
		else
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "Entrance period ended."));

			switch (Config.LIT_REGISTRATION_MODE)
			{
				case 1:
					if (_partyLeaders.size() < Config.LIT_MIN_PARTY_CNT || _partyLeaders.size() > Config.LIT_MAX_PARTY_CNT)
					{
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "Since the conditions were not met, the entrance was refused."));
						cleanUpTomb();
						return;
					}
					break;
				case 2:
					if (_registedPlayers.size() < Config.LIT_MIN_PLAYER_CNT || _registedPlayers.size() > Config.LIT_MAX_PLAYER_CNT)
					{
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "Since the conditions were not met, the entrance was refused."));
						cleanUpTomb();
						return;
					}
					break;
			}

			if (_RegistrationTimeInfoTask != null)
			{
				_RegistrationTimeInfoTask.cancel(true);
			}

			if (_InvadeTask != null)
			{
				_InvadeTask.cancel(true);
			}
			_InvadeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Invade(), 10000);
		}
	}

	protected void doCheckTimeUp(int remaining)
	{
		if (_isReachToHall)
			return;

		CreatureSay cs = null;
		int timeLeft;
		int interval;

		if (remaining > 300000)
		{
			timeLeft = remaining / 60000;
			interval = 300000;
			cs = new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Notice", timeLeft + " minutes left.");
			remaining = remaining - 300000;
		}
		else if (remaining > 60000)
		{
			timeLeft = remaining / 60000;
			interval = 60000;
			cs = new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Notice", timeLeft + " minutes left.");
			remaining = remaining - 60000;
		}
		else if (remaining > 30000)
		{
			timeLeft = remaining / 1000;
			interval = 30000;
			cs = new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			cs = new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 10000;
		}

		for (L2PcInstance pc : getPlayersInside())
		{
			pc.sendPacket(cs);
		}

		if (_CheckTimeUpTask != null)
		{
			_CheckTimeUpTask.cancel(true);
		}
		if (remaining >= 10000)
		{
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval);
		}
		else
		{
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
		}
	}

	// invade to tomb.
	public void doInvade()
	{
		initDoors();

		switch (Config.LIT_REGISTRATION_MODE)
		{
			case 0:
				doInvadeCc();
				break;
			case 1:
				doInvadePt();
				break;
			case 2:
				doInvadePc();
				break;
			default:
				_log.warn("LastImperialTombManager: Invalid Registration Mode!");
				return;
		}

		_isInvaded = true;

		if (_Room1SpawnTask != null)
		{
			_Room1SpawnTask.cancel(true);
		}
		_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs1st(), 15000);

		if (_CheckTimeUpTask != null)
		{
			_CheckTimeUpTask.cancel(true);
		}
		_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(Config.LIT_TIME_LIMIT * 60000), 15000);
	}

	// invade to tomb. when registration mode is command channel.
	private void doInvadeCc()
	{
		int locId = 0;

		if (_commander.getInventory().getInventoryItemCount(SCROLL, -1) < 1)
		{
			_commander.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			_commander.sendMessage(Message.getMessage(_commander, Message.MessageId.MSG_CONDITIONS_NOT_MET));

			return;
		}

		_commander.destroyItemByItemId("Quest", SCROLL, 1, _commander, true);

		for (L2Party pt : _commander.getParty().getCommandChannel().getPartys())
		{
			if (locId >= 5)
			{
				locId = 0;
			}

			for (L2PcInstance pc : pt.getPartyMembers())
				if (!pc.isAlikeDead())
				{
					if (_messenger != null)
						if (!pc.isInsideRadius(_messenger, 1000, false, false))
						{
							pc.leaveParty();
							continue;
						}
					pc.teleToLocation(_invadeLoc[locId][0] + Rnd.get(50), _invadeLoc[locId][1] + Rnd.get(50), _invadeLoc[locId][2]);
				}

			locId++;
		}
	}

	// invade to tomb. when registration mode is single.
	private void doInvadePc()
	{
		int locId = 0;
		boolean isReadyToInvade = true;

		for (L2PcInstance pc : _registedPlayers)
			if (pc.getInventory().getInventoryItemCount(SCROLL, -1) < 1)
			{
				pc.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				pc.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Since the conditions were not met, the entrance was refused."));
				isReadyToInvade = false;
			}

		if (!isReadyToInvade)
		{
			for (L2PcInstance pc : _registedPlayers)
			{
				pc.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Since the conditions were not met, the entrance was refused."));
			}

			return;
		}

		for (L2PcInstance pc : _registedPlayers)
		{
			pc.destroyItemByItemId("Quest", SCROLL, 1, _commander, true);
		}

		for (L2PcInstance pc : _registedPlayers)
		{
			if (locId >= 5)
			{
				locId = 0;
			}

			pc.teleToLocation(_invadeLoc[locId][0] + Rnd.get(50), _invadeLoc[locId][1] + Rnd.get(50), _invadeLoc[locId][2]);

			locId++;
		}
	}

	// invade to tomb. when registration mode is party.
	private void doInvadePt()
	{
		int locId = 0;
		boolean isReadyToInvade = true;

		for (L2PcInstance ptl : _partyLeaders)
			if (ptl.getInventory().getInventoryItemCount(SCROLL, -1) < 1)
			{
				ptl.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				ptl.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Since the conditions were not met, the entrance was refused."));

				isReadyToInvade = false;
			}

		if (!isReadyToInvade)
		{
			for (L2PcInstance ptl : _partyLeaders)
			{
				ptl.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("Since the conditions were not met, the entrance was refused."));
			}

			return;
		}

		for (L2PcInstance ptl : _partyLeaders)
		{
			ptl.destroyItemByItemId("Quest", SCROLL, 1, _commander, true);
		}

		for (L2PcInstance ptl : _partyLeaders)
		{
			if (locId >= 5)
			{
				locId = 0;
			}

			for (L2PcInstance pc : ptl.getParty().getPartyMembers())
			{
				pc.teleToLocation(_invadeLoc[locId][0] + Rnd.get(50), _invadeLoc[locId][1] + Rnd.get(50), _invadeLoc[locId][2]);
			}

			locId++;
		}
	}

	public int getRandomRespawnDate()
	{
		return 0;
	}

	// load monsters and close doors.
	@Override
	public void init()
	{
		LastImperialTombSpawnlist.getInstance().clear();
		LastImperialTombSpawnlist.getInstance().fill();
		initDoors();

		_log.info("Last Imperial Tomb: Initializing The Last Imperial Tomb.");
	}

	// return true,tomb was already invaded by players.
	public boolean isInvaded()
	{
		return _isInvaded;
	}

	public boolean isReachToHall()
	{
		return _isReachToHall;
	}

	// Is the door of outside of room2 in confirmation to open.
	public void onKillDarkChoirCaptain()
	{
		int killCnt = 0;

		for (L2Npc DarkChoirCaptain : _darkChoirCaptains)
			if (DarkChoirCaptain.isDead())
			{
				killCnt++;
			}

		if (_darkChoirCaptains.size() <= killCnt)
		{
			openRoom2OutsideDoors();

			for (L2Npc mob : _room2OutsideMonsters)
			{
				mob.deleteMe();
				mob.getSpawn().stopRespawn();
			}

			for (L2Npc DarkChoirCaptain : _darkChoirCaptains)
			{
				DarkChoirCaptain.deleteMe();
				DarkChoirCaptain.getSpawn().stopRespawn();
			}
		}
	}

	// Is the door of inside of room2 in confirmation to open.
	public void onKillDarkChoirPlayer()
	{
		int killCnt = 0;

		for (L2Npc DarkChoirPlayer : _room2InsideMonsters)
			if (DarkChoirPlayer.isDead())
			{
				killCnt++;
			}

		if (_room2InsideMonsters.size() <= killCnt)
		{
			if (_Room2InsideDoorOpenTask != null)
			{
				_Room2InsideDoorOpenTask.cancel(true);
			}
			if (_Room2OutsideSpawnTask != null)
			{
				_Room2OutsideSpawnTask.cancel(true);
			}

			_Room2InsideDoorOpenTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenRoom2InsideDoors(), 3000);
			_Room2OutsideSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom2OutsideMobs(), 4000);
		}
	}

	// Is the door of room1 in confirmation to open.
	public void onKillHallAlarmDevice()
	{
		int killCnt = 0;

		for (L2Npc HallAlarmDevice : _hallAlarmDevices)
			if (HallAlarmDevice.isDead())
			{
				killCnt++;
			}

		switch (killCnt)
		{
			case 1:
				if (Rnd.get(100) < 10)
				{
					openRoom1Doors();
					openRoom2OutsideDoors();
					spawnRoom2InsideMob();
				}
				else
				{
					if (_Room1SpawnTask != null)
					{
						_Room1SpawnTask.cancel(true);
					}

					_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs2nd(), 3000);
				}
				break;
			case 2:
				if (Rnd.get(100) < 20)
				{
					openRoom1Doors();
					openRoom2OutsideDoors();
					spawnRoom2InsideMob();
				}
				else
				{
					if (_Room1SpawnTask != null)
					{
						_Room1SpawnTask.cancel(true);
					}

					_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs3rd(), 3000);
				}
				break;
			case 3:
				if (Rnd.get(100) < 30)
				{
					openRoom1Doors();
					openRoom2OutsideDoors();
					spawnRoom2InsideMob();
				}
				else
				{
					if (_Room1SpawnTask != null)
					{
						_Room1SpawnTask.cancel(true);
					}

					_Room1SpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRoom1Mobs4th(), 3000);
				}
				break;
			case 4:
				openRoom1Doors();
				openRoom2OutsideDoors();
				spawnRoom2InsideMob();
				break;
			default:
				break;
		}
	}

	protected void openRoom2InsideDoors()
	{
		for (L2DoorInstance door : _room2InsideDoors)
		{
			door.openMe();
		}
	}

	protected void openRoom2OutsideDoors()
	{
		for (L2DoorInstance door : _room2OutsideDoors)
		{
			door.openMe();
		}
		_room3Door.openMe();
	}

	// registration to enter to tomb.
	public synchronized void registration(L2PcInstance pc, L2Npc npc)
	{
		_messenger = npc;
		switch (Config.LIT_REGISTRATION_MODE)
		{
			case 0:
				if (_commander != null)
					return;
				_commander = pc;
				if (_InvadeTask != null)
				{
					_InvadeTask.cancel(true);
				}
				_InvadeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Invade(), 10000);
				break;
			case 1:
				if (_partyLeaders.contains(pc))
					return;
				_partyLeaders.add(pc);

				if (_partyLeaders.size() == 1)
				{
					_RegistrationTimeInfoTask = ThreadPoolManager.getInstance().scheduleGeneral(new AnnouncementRegstrationInfo(npc, Config.LIT_REGISTRATION_TIME * 60000), 1000);
				}
				break;
			case 2:
				if (_registedPlayers.contains(pc))
					return;
				_registedPlayers.add(pc);
				if (_registedPlayers.size() == 1)
				{
					_RegistrationTimeInfoTask = ThreadPoolManager.getInstance().scheduleGeneral(new AnnouncementRegstrationInfo(npc, Config.LIT_REGISTRATION_TIME * 60000), 1000);
				}
				break;
			default:
				_log.warn("LastImperialTombManager: Invalid Registration Mode!");
		}
	}

	public void setReachToHall()
	{
		_isReachToHall = true;
	}

	@Override
	public void setRespawn()
	{
	}

	@Override
	public void setUnspawn()
	{
	}

	// RegistrationMode = command channel.
	public boolean tryRegistrationCc(L2PcInstance pc)
	{
		if (!FrintezzaManager.getInstance().isEnableEnterToLair())
		{
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_CONDITIONS_NOT_MET));
			return false;
		}

		if (isInvaded())
		{
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_LIT_OTHER_GROUP_INSIDE));
			return false;
		}

		if (_commander == null)
			if (pc.getParty() != null)
				if (pc.getParty().getCommandChannel() != null)
					if (pc.getParty().getCommandChannel().getChannelLeader() == pc && pc.getParty().getCommandChannel().getPartys().size() >= Config.LIT_MIN_PARTY_CNT && pc.getParty().getCommandChannel().getPartys().size() <= Config.LIT_MAX_PARTY_CNT && pc.getInventory().getInventoryItemCount(SCROLL, -1) >= 1)
						return true;

		pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_LIT_NOT_COMMANDER_OR_NO_SCROOL));
		return false;
	}

	// RegistrationMode = single.
	public boolean tryRegistrationPc(L2PcInstance pc)
	{
		if (!FrintezzaManager.getInstance().isEnableEnterToLair())
		{
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_CONDITIONS_NOT_MET));
			return false;
		}

		if (_registedPlayers.contains(pc))
		{
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_ALREADY_REGISTERD));
			return false;
		}

		if (isInvaded())
		{
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_LIT_OTHER_GROUP_INSIDE));
			return false;
		}

		if (_registedPlayers.size() < Config.LIT_MAX_PLAYER_CNT)
			if (pc.getInventory().getInventoryItemCount(SCROLL, -1) >= 1)
				return true;

		pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_LIT_NO_SCROOL));
		return false;
	}

	// RegistrationMode = party.
	public boolean tryRegistrationPt(L2PcInstance pc)
	{
		if (!FrintezzaManager.getInstance().isEnableEnterToLair())
		{
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_CONDITIONS_NOT_MET));
			return false;
		}

		if (isInvaded())
		{
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_LIT_OTHER_GROUP_INSIDE));
			return false;
		}

		if (pc.getParty() == null)
		{
			pc.sendMessage("SVR: You aren't in party!");
			return false;
		}

		/* if (pc.getParty().getCommandChannel() != null) { pc.sendMessage("SVR: Registration using channel command is not currently enabled. Please dissolve the channel and try again in party!"); return false; } */

		if (_partyLeaders.size() < Config.LIT_MAX_PARTY_CNT)
			if (pc.getParty() != null)
				if (pc.getParty().getLeader() == pc && pc.getInventory().getInventoryItemCount(SCROLL, -1) >= 1 && pc.getParty().getCommandChannel() != null)
					return true;

		pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_LIT_NOT_COMMANDER_OR_NO_SCROOL));
		return false;
	}

	public void unregisterPc(L2PcInstance pc)
	{
		if (_registedPlayers.contains(pc))
		{
			_registedPlayers.remove(pc);
			pc.sendMessage(Message.getMessage(pc, Message.MessageId.MSG_LIT_UNREGISTERED));
		}
	}

	public void unregisterPt(L2Party pt)
	{
		if (_partyLeaders.contains(pt.getLeader()))
		{
			_partyLeaders.remove(pt.getLeader());
			pt.getLeader().sendMessage(Message.getMessage(pt.getLeader(), Message.MessageId.MSG_LIT_UNREGISTERED));
		}
	}

	public void unregisterCc(L2Party pt)
	{
		if (_partyLeaders.contains(pt.getLeader()))
		{
			_partyLeaders.remove(pt.getLeader());
			pt.getLeader().sendMessage(Message.getMessage(pt.getLeader(), Message.MessageId.MSG_LIT_UNREGISTERED));
		}
	}
}
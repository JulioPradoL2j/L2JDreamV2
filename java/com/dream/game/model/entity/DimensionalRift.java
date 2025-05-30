package com.dream.game.model.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.dream.Config;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.DimensionalRiftManager.DimensionalRiftRoom;
import com.dream.game.manager.DimensionalRiftManager.DimensionalRiftRoom.SpawnInfo;
import com.dream.game.manager.QuestManager;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RiftBossInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.tools.random.Rnd;

public class DimensionalRift
{
	private static final long seconds_5 = 5000L;
	protected List<Byte> _completedRooms = new ArrayList<>();
	protected List<L2PcInstance> deadPlayers = new ArrayList<>();
	protected List<L2PcInstance> revivedInWaitingRoom = new ArrayList<>();
	private boolean _hasJumped = false;
	public final Map<Byte, List<L2Spawn>> _spawns = new HashMap<>();
	public boolean isBossRoom = false;
	protected byte jumps_current = 0;
	protected byte _choosenRoom = -1;
	private ExclusiveTask teleporterTimerTask;
	private Timer spawnTimer;
	private TimerTask spawnTimerTask;
	protected byte _roomType;
	protected L2Party _party;
	protected long _jumpTime;

	public DimensionalRift(L2Party party, byte roomType, byte roomId)
	{
		_roomType = roomType;
		_party = party;
		_choosenRoom = roomId;
		int[] coords = getRoomCoord(roomId);
		party.setDimensionalRift(this);

		Quest riftQuest = QuestManager.getInstance().getQuest(635);
		for (L2PcInstance p : party.getPartyMembers())
		{
			if (riftQuest != null)
			{
				QuestState qs = p.getQuestState(riftQuest.getName());
				if (qs == null)
				{
					qs = riftQuest.newQuestState(p);
				}
				if (qs.getInt("cond") != 1)
				{
					qs.set("cond", "1");
				}
			}
			p.teleToLocation(coords[0], coords[1], coords[2]);
		}
		createSpawnTimer(_choosenRoom);
	}

	private long calcTimeToNextJump()
	{
		int time = Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000;
		checkBossRoom(_choosenRoom);
		if (isBossRoom)
		{
			time *= Config.RIFT_BOSS_ROOM_TIME_MUTIPLY;
		}
		return time;
	}

	public void checkBossRoom(byte roomId)
	{
		isBossRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, roomId).isBossRoom();
	}

	public void createSpawnTimer(final byte room)
	{
		if (spawnTimerTask != null)
		{
			spawnTimerTask.cancel();
			spawnTimerTask = null;
		}

		if (spawnTimer != null)
		{
			spawnTimer.cancel();
			spawnTimer = null;
		}

		final DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room);
		final DimensionalRift DR = this;
		riftRoom.setUsed();
		spawnTimer = new Timer();
		spawnTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				List<L2Spawn> _roomspawn = _spawns.get(room);
				if (_roomspawn == null)
				{
					_roomspawn = new ArrayList<>();
					_spawns.put(room, _roomspawn);
				}
				for (SpawnInfo info : riftRoom.getSpawnsInfo())
				{
					L2Spawn spawn = new L2Spawn(info._template);
					spawn.setLocx(info.x);
					spawn.setLocy(info.y);
					spawn.setLocz(info.z);
					spawn.setAmount(1);
					spawn.setRespawnDelay(info.delay);
					spawn.setHeading(-1);
					_roomspawn.add(spawn);
					L2Npc mob = spawn.doSpawn();

					if (mob instanceof L2RiftBossInstance)
					{
						isBossRoom = true;
						((L2RiftBossInstance) mob).setDimensionalRift(DR);
					}
					else
					{
						spawn.startRespawn();
					}

				}
				createTeleporterTimer(true);
			}
		};

		spawnTimer.schedule(spawnTimerTask, Config.RIFT_SPAWN_DELAY);
	}

	protected void createTeleporterTimer(final boolean reasonTP)
	{
		if (teleporterTimerTask != null)
		{
			teleporterTimerTask.cancel();
			teleporterTimerTask = null;
		}

		teleporterTimerTask = new ExclusiveTask()
		{
			@Override
			public void onElapsed()
			{
				if (_jumpTime > 0)
				{
					_jumpTime -= 1000;
					schedule(1000);
					return;
				}

				if (_choosenRoom > -1)
				{
					unspawnRoom(_choosenRoom);
				}

				if (reasonTP && jumps_current < getMaxJumps() && _party.getMemberCount() > deadPlayers.size())
				{
					jumps_current++;

					_completedRooms.add(_choosenRoom);
					_choosenRoom = -1;

					for (L2PcInstance p : _party.getPartyMembers())
						if (!revivedInWaitingRoom.contains(p))
						{
							teleportToNextRoom(p);
						}
					createSpawnTimer(_choosenRoom);

				}
				else
				{
					for (L2PcInstance p : _party.getPartyMembers())
						if (!revivedInWaitingRoom.contains(p))
						{
							teleportToWaitingRoom(p);
						}
					killRift();
					cancel();
				}
			}
		};

		_jumpTime = calcTimeToNextJump();
		if (reasonTP)
		{
			teleporterTimerTask.schedule(1000);
		}
		else
		{
			_jumpTime = 0;
			teleporterTimerTask.schedule(seconds_5);
		}
	}

	public byte getCurrentRoom()
	{
		return _choosenRoom;
	}

	public List<L2PcInstance> getDeadMemberList()
	{
		return deadPlayers;
	}

	public byte getMaxJumps()
	{
		if (Config.RIFT_MAX_JUMPS <= 8 && Config.RIFT_MAX_JUMPS >= 1)
			return (byte) Config.RIFT_MAX_JUMPS;

		return 4;
	}

	public List<L2PcInstance> getRevivedAtWaitingRoom()
	{
		return revivedInWaitingRoom;
	}

	public int[] getRoomCoord(byte roomId)
	{
		return DimensionalRiftManager.getInstance().getRoom(_roomType, roomId).getTeleportCoords();
	}

	public Timer getSpawnTimer()
	{
		return spawnTimer;
	}

	public TimerTask getSpawnTimerTask()
	{
		return spawnTimerTask;
	}

	public ExclusiveTask getTeleportTimerTask()
	{
		return teleporterTimerTask;
	}

	public byte getType()
	{
		return _roomType;
	}

	public void killRift()
	{
		_completedRooms = null;

		if (_party != null)
		{
			_party.setDimensionalRift(null);
		}

		_party = null;
		revivedInWaitingRoom = null;
		deadPlayers = null;
		unspawnRoom(_choosenRoom);
		DimensionalRiftManager.getInstance().killRift(this);
	}

	public void manualExitRift(L2PcInstance player, L2Npc npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
			return;

		if (player.getObjectId() != player.getParty().getPartyLeaderOID())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}

		for (L2PcInstance p : player.getParty().getPartyMembers())
		{
			teleportToWaitingRoom(p);
		}
		killRift();
	}

	public void manualTeleport(L2PcInstance player, L2Npc npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
			return;

		if (player.getObjectId() != player.getParty().getPartyLeaderOID())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}

		if (_hasJumped)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
			return;
		}

		_hasJumped = true;

		unspawnRoom(_choosenRoom);
		_completedRooms.add(_choosenRoom);
		_choosenRoom = -1;

		for (L2PcInstance p : _party.getPartyMembers())
		{
			teleportToNextRoom(p);
		}

		createSpawnTimer(_choosenRoom);
	}

	public void memberDead(L2PcInstance player)
	{
		if (player == null || _party == null)
			return;

		if (!deadPlayers.contains(player))
		{
			deadPlayers.add(player);
		}

		if (_party.getMemberCount() == deadPlayers.size())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					for (L2PcInstance p : _party.getPartyMembers())
						if (!revivedInWaitingRoom.contains(p))
						{
							teleportToWaitingRoom(p);
						}
					killRift();
				}
			}, 5000);
		}
	}

	public void memberRessurected(L2PcInstance player)
	{
		if (deadPlayers.contains(player))
		{
			deadPlayers.remove(player);
		}
	}

	public void partyMemberExited(L2PcInstance player)
	{
		if (deadPlayers.contains(player))
		{
			deadPlayers.remove(player);
		}

		if (revivedInWaitingRoom.contains(player))
		{
			revivedInWaitingRoom.remove(player);
		}

		if (_party.getMemberCount() < Config.RIFT_MIN_PARTY_SIZE || _party.getMemberCount() == 1)
		{
			for (L2PcInstance p : _party.getPartyMembers())
			{
				teleportToWaitingRoom(p);
			}
			killRift();
		}
	}

	public void partyMemberInvited()
	{
		createTeleporterTimer(false);
	}

	public void setSpawnTimer(Timer t)
	{
		spawnTimer = t;
	}

	public void setSpawnTimerTask(TimerTask st)
	{
		spawnTimerTask = st;
	}

	public void setTeleportTimerTask(ExclusiveTask tt)
	{
		teleporterTimerTask = tt;
	}

	protected synchronized void teleportToNextRoom(L2PcInstance player)
	{
		if (_choosenRoom == -1)
		{
			do
			{
				_choosenRoom = (byte) Rnd.get(1, 9);
			}
			while (_completedRooms.contains(_choosenRoom));
		}

		checkBossRoom(_choosenRoom);
		int[] coords = getRoomCoord(_choosenRoom);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	protected void teleportToWaitingRoom(L2PcInstance player)
	{
		DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
		Quest riftQuest = QuestManager.getInstance().getQuest(635);
		if (riftQuest != null)
		{
			QuestState qs = player.getQuestState(riftQuest.getName());
			if (qs != null && qs.getInt("cond") == 1)
			{
				qs.set("cond", "0");
			}
		}
	}

	public void unspawnRoom(byte room)
	{
		List<L2Spawn> _roomSpawn = _spawns.get(room);
		if (_roomSpawn != null)
		{
			for (L2Spawn spawn : _roomSpawn)
			{
				spawn.stopRespawn();
				if (spawn.getLastSpawn() != null)
				{
					spawn.getLastSpawn().deleteMe();
				}
			}
			_roomSpawn.clear();
		}
	}

	public void usedTeleport(L2PcInstance player)
	{
		if (player == null)
			return;

		if (!revivedInWaitingRoom.contains(player))
		{
			revivedInWaitingRoom.add(player);
		}

		if (!deadPlayers.contains(player))
		{
			deadPlayers.add(player);
		}

		if (_party == null)
			return;

		if (_party.getMemberCount() - revivedInWaitingRoom.size() < Config.RIFT_MIN_PARTY_SIZE)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					if (_party != null)
					{
						for (L2PcInstance p : _party.getPartyMembers())
							if (p != null)
								if (!revivedInWaitingRoom.contains(p))
								{
									teleportToWaitingRoom(p);
								}
					}
					killRift();
				}
			}, 5000);
		}
	}
}
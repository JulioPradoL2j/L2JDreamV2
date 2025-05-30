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
package com.dream.game.manager.grandbosses;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.dream.Config;
import com.dream.annotations.L2Properties;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.pack.ai.Valakas;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class ValakasManager extends BossLair
{
	// at end of activity time.
	public class ActivityTimeEnd implements Runnable
	{
		@Override
		public void run()
		{
			setUnspawn();
		}
	}

	// do spawn teleport cube.
	public class CubeSpawn implements Runnable
	{
		@Override
		public void run()
		{
			spawnCube();
		}
	}

	public class IntervalEnd implements Runnable
	{
		@Override
		public void run()
		{
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
			_state.update();
			_log.info("ValakasManager: State of Valakas is " + _state.getState());
		}
	}

	// Move at random on after Valakas appears.
	private class MoveAtRandom implements Runnable
	{
		private final L2Npc _npc;
		private final L2CharPosition _pos;

		public MoveAtRandom(L2Npc npc, L2CharPosition pos)
		{
			_npc = npc;
			_pos = pos;
		}

		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.MOVE_TO, _pos);
		}
	}

	// action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private final L2GrandBossInstance _boss;

		public SetMobilised(L2GrandBossInstance boss)
		{
			_boss = boss;
		}

		@Override
		public void run()
		{
			_boss.setIsImmobilized(false);
			_boss.setIsInSocialAction(false);
		}
	}

	private class ValakasSpawn implements Runnable
	{
		private final int _distance = 6502500;
		private int _taskId;

		ValakasSpawn(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			_monsterSpawnTask = null;
			long nextTime = -1;
			switch (_taskId)
			{
				case 1:
					_valakas = (L2GrandBossInstance) _bossSpawn.doSpawn();
					_monsters.add(_valakas);
					_valakas.setIsImmobilized(true);
					_valakas.setCanReturnToSpawnPoint(false);
					_valakas.setIsInSocialAction(true);
					_state.setState(GrandBossState.StateEnum.ALIVE);
					_state.update();

					nextTime = 16;
					break;

				case 2:
					_valakas.broadcastPacket(new SocialAction(_valakas, 1));

					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1800, 180, -1, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 1500;

					break;

				case 3:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1300, 180, -5, 3000, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 3300;

					break;

				case 4:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 500, 180, -8, 600, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 1300;

					break;

				case 5:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200, 180, -5, 300, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 1600;

					break;

				case 6:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2800, 250, 70, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 200;

					break;

				case 7:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2600, 30, 60, 3400, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 5700;

					break;

				case 8:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 700, 150, -65, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					nextTime = 1400;

					break;

				case 9:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200, 150, -55, 2900, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 6700;

					break;

				case 10:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 750, 170, -10, 1700, 5700);
						}
						else
						{
							pc.leaveMovieMode();
						}

					nextTime = 3700;
					break;

				case 11:
					for (L2PcInstance pc : getPlayersInside())
						if (pc.getPlanDistanceSq(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 840, 170, -5, 1200, 2000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					nextTime = 2000;

					break;

				case 12:
					for (L2PcInstance pc : getPlayersInside())
					{
						pc.leaveMovieMode();
					}

					_state.setState(StateEnum.ALIVE);
					_state.update();
					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_valakas), 16);
					L2CharPosition pos = new L2CharPosition(Rnd.get(211080, 214909), Rnd.get(-115841, -112822), -1662, 0);
					_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_valakas, pos), 32);
					_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new ActivityTimeEnd(), ACTIVITY_TIME * 60000);

					break;
			}

			if (nextTime > 0)
			{
				_taskId++;
				ThreadPoolManager.getInstance().scheduleGeneral(this, nextTime);
			}
		}
	}

	private static ValakasManager _instance;

	public static ValakasManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ValakasManager();
		}
		return _instance;
	}

	// location of teleport cube.
	private final int _teleportCubeId = 31759;
	private final int _teleportCubeLocation[][] =
	{
		{
			214880,
			-116144,
			-1644,
			0
		},
		{
			213696,
			-116592,
			-1644,
			0
		},
		{
			212112,
			-116688,
			-1644,
			0
		},
		{
			211184,
			-115472,
			-1664,
			0
		},
		{
			210336,
			-114592,
			-1644,
			0
		},
		{
			211360,
			-113904,
			-1644,
			0
		},
		{
			213152,
			-112352,
			-1644,
			0
		},
		{
			214032,
			-113232,
			-1644,
			0
		},
		{
			214752,
			-114592,
			-1644,
			0
		},
		{
			209824,
			-115568,
			-1421,
			0
		},
		{
			210528,
			-112192,
			-1403,
			0
		},
		{
			213120,
			-111136,
			-1408,
			0
		},
		{
			215184,
			-111504,
			-1392,
			0
		},
		{
			215456,
			-117328,
			-1392,
			0
		},
		{
			213200,
			-118160,
			-1424,
			0
		}

	};
	protected List<L2Spawn> _teleportCubeSpawn = new ArrayList<>();
	protected List<L2Npc> _teleportCube = new ArrayList<>();
	protected List<L2Npc> _monsters = new ArrayList<>();
	protected ScheduledFuture<?> _cubeSpawnTask = null;
	protected ScheduledFuture<?> _monsterSpawnTask = null;
	protected ScheduledFuture<?> _intervalEndTask = null;
	protected ScheduledFuture<?> _activityTimeEndTask = null;
	protected ScheduledFuture<?> _mobiliseTask = null;
	protected ScheduledFuture<?> _moveAtRandomTask = null;
	protected ScheduledFuture<?> _respawnValakasTask = null;
	private long MIN_RESPAWN;
	private long MAX_RESPAWN;

	public long ACTIVITY_TIME;

	private long ARRIVED_TIME;

	private int LAIR_CAPACITY;

	private long MIN_SLEEP;

	private long MAX_SLEEP;

	public L2GrandBossInstance _valakas = null;

	private boolean ENABLED;

	public ValakasManager()
	{

		super();
		_state = new GrandBossState(29028);
		try
		{

			L2Properties p = new L2Properties("./config/main/bosses.properties");
			ENABLED = Boolean.parseBoolean(p.getProperty("ValakasEnabled", "true"));
			if (!ENABLED)
				return;

			MIN_RESPAWN = Integer.parseInt(p.getProperty("ValakasMinRespawn", "1440"));
			MAX_RESPAWN = Integer.parseInt(p.getProperty("ValakasMaxRespawn", "2880"));
			ACTIVITY_TIME = Integer.parseInt(p.getProperty("ValakasActiveTime", "50"));
			ARRIVED_TIME = Integer.parseInt(p.getProperty("ValakasArrivedTime", "5"));
			LAIR_CAPACITY = Integer.parseInt(p.getProperty("ValakasLairCapacity", "200"));
			MIN_SLEEP = Integer.parseInt(p.getProperty("ValakasMinSleepTime", "120"));
			MAX_SLEEP = Integer.parseInt(p.getProperty("ValakasMaxSleepTime", "240"));

		}
		catch (Exception e)
		{
			_log.error("ValakasManager: Error while reading config", e);
			return;
		}

	}

	@Override
	public void init()
	{
		if (!ENABLED)
			return;

		new Valakas();
		_questName = Valakas.QUEST;
		try
		{
			L2NpcTemplate template1;

			template1 = NpcTable.getInstance().getTemplate(29028);
			_bossSpawn = new L2Spawn(template1);
			_bossSpawn.setLocx(212852);
			_bossSpawn.setLocy(-114842);
			_bossSpawn.setLocz(-1632);
			_bossSpawn.setHeading(833);
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for (int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		switch (_state.getState())
		{
			case DEAD:
				_state.setRespawnDate(Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000);
				_state.setState(StateEnum.INTERVAL);
			case SLEEP:
			case INTERVAL:
				setIntervalEndTask();
				break;
			case UNKNOWN:
			case ALIVE:
				_state.setState(StateEnum.NOTSPAWN);
				break;
		}
		_log.info("Valakas: State is " + _state.getState() + ".");
	}

	@Override
	public boolean isEnableEnterToLair()
	{
		return getPlayersInside().size() < LAIR_CAPACITY && super.isEnableEnterToLair();
	}

	@Override
	public void onEnter(L2Character cha)
	{
		L2PcInstance player = cha.getActingPlayer();
		if (player != null && !player.isGM() && Config.EPIC_REQUIRE_QUEST)
		{
			QuestState qs = player.getQuestState(_questName);
			if (qs == null)
			{
				player.teleToLocation(TeleportWhereType.Town);
			}
		}

	}

	public void setCubeSpawn()
	{
		long interval = Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000;
		_state.setRespawnDate(interval);
		_state.setState(GrandBossState.StateEnum.INTERVAL);
		_activityTimeEndTask.cancel(true);
		setIntervalEndTask();
		_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(), 10000);
	}

	public void setIntervalEndTask()
	{
		_intervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	@Override
	public void setRespawn()
	{

	}

	// clean Valakas's lair.
	@Override
	public void setUnspawn()
	{

		clearLair();
		for (L2Npc mob : _monsters)
		{
			mob.deleteMe();
		}
		_monsters.clear();

		// delete teleport cube.
		for (L2Npc cube : _teleportCube)
		{
			cube.deleteMe();
		}
		_teleportCube.clear();
		if (_state.getState() != StateEnum.DEAD)
		{
			_state.setState(StateEnum.SLEEP);
			long interval = (MIN_SLEEP + Rnd.get((int) (MAX_SLEEP - MIN_SLEEP))) * 60000;
			_state.setRespawnDate(interval);
		}
		else
		{
			_state.setState(StateEnum.INTERVAL);
			long interval = (MIN_RESPAWN + Rnd.get((int) (MAX_RESPAWN - MIN_RESPAWN))) * 60000;
			_state.setRespawnDate(interval);
		}
		_state.update();
		setIntervalEndTask();
		_log.info("ValakasManager: State of Valakas is " + _state.getState());
	}

	public void setValakasSpawnTask()
	{
		if (_monsterSpawnTask == null)
		{
			_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(1), ARRIVED_TIME * 60000);
		}
	}

	public void spawnCube()
	{
		for (L2Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
		_state.setState(StateEnum.DEAD);
		_state.update();
	}
}

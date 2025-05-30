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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import com.dream.Config;
import com.dream.annotations.L2Properties;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.model.quest.pack.ai.Baium;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.Earthquake;
import com.dream.game.network.serverpackets.NpcSay;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class BaiumManager extends BossLair
{
	// at end of activity time.
	public class ActivityTimeEnd implements Runnable
	{
		@Override
		public void run()
		{
			setUnspawn();
			_log.info("BaiumManager: Tempo Limite da sala chegou ao fim. State -> " + _state.getState());
		}
	}

	public class CallArchAngel implements Runnable
	{
		@Override
		public void run()
		{
			spawnArchangels();
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

	public class unCubeSpawn implements Runnable
	{
		@Override
		public void run()
		{
			unspawnCube();
		}
	}

	// at end of interval.
	public class IntervalEnd implements Runnable
	{
		@Override
		public void run()
		{
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
			_state.update();
			if (_statue == null)
			{
			_statue = _statueSpawn.doSpawn();
			}
		}
	}

	public class KillPc implements Runnable
	{
		@Override
		public void run()
		{
			L2Skill skill = SkillTable.getInstance().getInfo(4136, 1);
			if (_waker != null && skill != null)
			{
				_baium.broadcastPacket(new NpcSay(_baium.getObjectId(), 1, _baium.getNpcId(), _words));
				_baium.setTarget(_waker);
				_baium.doCast(skill);
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						_waker.reduceCurrentHp(_waker.getCurrentHp() + 1, _baium);
					}
				}, 2000);

			}
		}
	}

	// Move at random on after Baium appears.
	private class MoveAtRandom implements Runnable
	{
		private final L2CharPosition _pos;

		public MoveAtRandom(L2CharPosition pos)
		{
			_pos = pos;
		}

		@Override
		public void run()
		{
			_baium.getAI().setIntention(CtrlIntention.MOVE_TO, _pos);
		}
	}

	// action is enabled the boss.
	public class SetMobilised implements Runnable
	{
		@Override
		public void run()
		{
			_baium.setIsImmobilized(false);
			_baium.setIsInSocialAction(false);

		}
	}

	// do social.
	private class Social implements Runnable
	{
		private final int _action;

		public Social(int actionId)
		{
			_action = actionId;
		}

		@Override
		public void run()
		{
			_baium.broadcastPacket(new SocialAction(_baium, _action));
		}
	}

	private static BaiumManager _instance;

	public final static int BAIUM_NPC = 29025;
	public final static int BAIUM = 29020;

	public final static int ARCHANGEL = 29021;
	public final static int TELEPORT_CUBE = 29055;
	public final static int STATUE_LOCATION[] =
	{
		116025,
		17455,
		10109,
		40233
	};
	public final static int ANGEL_LOCATION[][] =
	{
		{
			113004,
			16209,
			10076,
			60242
		},
		{
			114053,
			16642,
			10076,
			4411
		},
		{
			114563,
			17184,
			10076,
			49241
		},
		{
			116356,
			16402,
			10076,
			31109
		},
		{
			115015,
			16393,
			10076,
			32760
		},
		{
			115481,
			15335,
			10076,
			16241
		},
		{
			114680,
			15407,
			10051,
			32485
		},
		{
			114886,
			14437,
			10076,
			16868
		},
		{
			115391,
			17593,
			10076,
			55346
		},
		{
			115245,
			17558,
			10076,
			35536
		}
	};
	public final static int CUBE_LOCATION[] =
	{
		115203,
		16620,
		10078,
		0
	};

	public static BaiumManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new BaiumManager();
		}
		return _instance;
	}

	public L2Spawn _statueSpawn = null;
	protected List<L2Spawn> _angelSpawns = new ArrayList<>();

	public L2Npc _statue;
	public L2GrandBossInstance _baium;
	protected L2Spawn _teleportCubeSpawn = null;
	protected L2Npc _teleportCube = null;
	protected L2Npc _npcBaium;
	protected Map<Integer, L2Spawn> _monsterSpawn = new HashMap<>();

	protected List<L2Npc> _monsters = new ArrayList<>();

	protected ScheduledFuture<?> _activityTimeEndTask = null;
	protected String _words = "Don't obstruct my sleep! Die!";

	private long MIN_RESPAWN;

	private long MAX_RESPAWN;

	private long ACTIVITY_TIME;

	public long NO_ATTACK_TIME;

	public long _lastAttackTime;

	public long BAIUMCUBEUNSPAWN;

	private boolean ENABLED;

	private Future<?> _activityTask;

	private final ExclusiveTask _baumCheskTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (_baium != null && !_baium.isDead() && _state.getState() != StateEnum.NOTSPAWN)
			{
				if (System.currentTimeMillis() - _lastAttackTime > NO_ATTACK_TIME)
				{
					setUnspawn();
					_log.info("BaiumManager: O Baium passou o tempo limite sem ser atacado e foi resetado. State -> " + _state.getState());
					return;
				}
				schedule(60000);
			}
		}

	};

	public L2PcInstance _waker;

	public BaiumManager()
	{
		super();
		_state = new GrandBossState(BAIUM);
		try
		{
			L2Properties p = new L2Properties("./config/main/bosses.properties");
			ENABLED = Boolean.parseBoolean(p.getProperty("BaiumEnabled", "true"));
			if (!ENABLED)
				return;

			MIN_RESPAWN = Integer.parseInt(p.getProperty("BaiumMinRespawn", "1440"));
			MAX_RESPAWN = Integer.parseInt(p.getProperty("BaiumMaxRespawn", "2880"));
			ACTIVITY_TIME = Integer.parseInt(p.getProperty("BaiumActiveTime", "50"));
			NO_ATTACK_TIME = Integer.parseInt(p.getProperty("BaiumNoAttackTime", "20")) * 60000;
			BAIUMCUBEUNSPAWN = Integer.parseInt(p.getProperty("BaiumUnspawnCube", "120")) * 1000;
		}
		catch (Exception e)
		{
			_log.error("BaiumManager: Error while reading config", e);
		}
	}

	// Archangel ascension.
	public void deleteArchangels()
	{
		for (L2Npc npc : _monsters)
			if (!npc.isDead())
			{
				npc.deleteMe();
			}
		_monsters.clear();
	}

	@Override
	public void init()
	{
		if (!ENABLED)
			return;

		// setting spawn data of monsters.
		new Baium();
		_questName = Baium.QUEST;
		try
		{
			L2NpcTemplate template1;

			// Statue of Baium
			template1 = NpcTable.getInstance().getTemplate(BAIUM_NPC);
			_statueSpawn = new L2Spawn(template1);
			_statueSpawn.setAmount(1);
			_statueSpawn.setLocx(STATUE_LOCATION[0]);
			_statueSpawn.setLocy(STATUE_LOCATION[1]);
			_statueSpawn.setLocz(STATUE_LOCATION[2]);
			_statueSpawn.setHeading(STATUE_LOCATION[3]);

			// Baium.
			template1 = NpcTable.getInstance().getTemplate(BAIUM);
			_bossSpawn = new L2Spawn(template1);
			_bossSpawn.setLocx(STATUE_LOCATION[0]);
			_bossSpawn.setLocy(STATUE_LOCATION[1]);
			_bossSpawn.setLocz(STATUE_LOCATION[2]);
			_bossSpawn.setHeading(STATUE_LOCATION[3]);

		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		// setting spawn data of teleport cube.
		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(TELEPORT_CUBE);
			_teleportCubeSpawn = new L2Spawn(Cube);
			_teleportCubeSpawn.setAmount(1);
			_teleportCubeSpawn.setLocx(CUBE_LOCATION[0]);
			_teleportCubeSpawn.setLocy(CUBE_LOCATION[1]);
			_teleportCubeSpawn.setLocz(CUBE_LOCATION[2]);
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}

		// setting spawn data of archangels.
		try
		{
			L2NpcTemplate angel = NpcTable.getInstance().getTemplate(ARCHANGEL);
			L2Spawn spawnDat;
			List<Integer> random = new ArrayList<>();
			for (int i = 0; i < 5; i++)
			{
				int r = -1;
				while (r == -1 || random.contains(r))
				{
					r = Rnd.get(10);
				}
				random.add(r);
			}

			for (int i : random)
			{
				spawnDat = new L2Spawn(angel);
				spawnDat.setAmount(1);
				spawnDat.setLocx(ANGEL_LOCATION[i][0]);
				spawnDat.setLocy(ANGEL_LOCATION[i][1]);
				spawnDat.setLocz(ANGEL_LOCATION[i][2]);
				spawnDat.setHeading(ANGEL_LOCATION[i][3]);
				_angelSpawns.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}
		switch (_state.getState())
		{
			case DEAD:
			case SLEEP:
			case INTERVAL:
				setIntervalEndTask();
				break;
			case ALIVE:
			case NOTSPAWN:
			case UNKNOWN:
				_state.setState(StateEnum.NOTSPAWN);
				_statue = _statueSpawn.doSpawn();
				break;
		}
		_log.info("Baium: State is " + _state.getState() + ".");
	}

	@Override
	public void onEnter(L2Character cha)
	{
		L2PcInstance pc = cha.getActingPlayer();
		if (pc != null && !pc.isGM() && Config.EPIC_REQUIRE_QUEST)
			if (pc.getQuestState(_questName) == null)
			{
				pc.teleToLocation(TeleportWhereType.Town);
			}
	}

	// setting teleport cube spawn task.
	public void setCubeSpawn()
	{
		// deleteArchangels();
		ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(), 10000);
	}

	public void setIntervalEndTask()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	@Override
	public void setRespawn()
	{
	}

	// clean Baium's lair.
	@Override
	public void setUnspawn()
	{
		if (_activityTask != null)
		{
			_activityTask.cancel(true);
			_activityTask = null;
		}
		clearLair();
		deleteArchangels();
		if (_state.getState() != StateEnum.DEAD && _state.getState() != StateEnum.INTERVAL)
		{
			if (_baium != null)
			{
				_baium.deleteMe();
				_statue = _statueSpawn.doSpawn();
			}
			_state.setState(StateEnum.NOTSPAWN);
			// _log.info("BaiumManager: O Baium esta pronto para ser sumonado. State -> " + _state.getState());
		}
		// else
		// {
		// _state.setState(StateEnum.INTERVAL);
			// long interval = Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000;
			// _state.setRespawnDate(interval);
		// }
		if (_teleportCube != null)
		{
			_teleportCube.deleteMe();
			_teleportCube = null;
		}
		_state.update();
		setIntervalEndTask();
		/* if (_state.getState() != StateEnum.DEAD && _state.getState() != StateEnum.INTERVAL) { _log.info("BaiumManager: Baium nao foi morto a tempo, logo voltou ao estado " + _state.getState()); } */
	}

	protected void spawnArchangels()
	{
		for (L2Spawn spawn : _angelSpawns)
		{
			L2Attackable angel = (L2Attackable) spawn.doSpawn();
			angel.addDamageHate(_baium, 10, 2000);
			_monsters.add(angel);
		}
	}

	// do spawn Baium.
	public void spawnBaium()
	{

		if (_statue == null)
			return;
		_statue.deleteMe();
		_statue = null;
		_baium = (L2GrandBossInstance) _bossSpawn.doSpawn();
		_state.setState(StateEnum.ALIVE);
		_state.update();
		_baium.setIsImmobilized(true);
		_baium.setIsInSocialAction(true);
		_baium.setCanReturnToSpawnPoint(false);
		ThreadPoolManager.getInstance().scheduleGeneral(new Social(2), 100);

		ThreadPoolManager.getInstance().scheduleGeneral(new Social(3), 15000);

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				_baium.broadcastPacket(new Earthquake(_baium.getX(), _baium.getY(), _baium.getZ(), 40, 5));
			}
		}, 25000);

		ThreadPoolManager.getInstance().scheduleGeneral(new Social(1), 25000);

		ThreadPoolManager.getInstance().scheduleGeneral(new KillPc(), 26000);

		ThreadPoolManager.getInstance().scheduleGeneral(new CallArchAngel(), 35000);

		ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(), 35500);

		L2CharPosition pos = new L2CharPosition(Rnd.get(112826, 116241), Rnd.get(15575, 16375), 10078, 0);
		ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(pos), 36000);

		// set delete task.
		_activityTask = ThreadPoolManager.getInstance().scheduleGeneral(new ActivityTimeEnd(), ACTIVITY_TIME * 60000);
		_lastAttackTime = System.currentTimeMillis();
		_baumCheskTask.schedule(60000);
		_log.info("BaiumManager: O Baium foi sumonado. State -> " + _state.getState());
	}

	// do spawn teleport cube.
	public void spawnCube()
	{
		_teleportCube = _teleportCubeSpawn.doSpawn();
		_baium = null;
		long interval = Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000;
		_state.setRespawnDate(interval);
		_state.setState(GrandBossState.StateEnum.DEAD);
		// setIntervalEndTask();
		deleteArchangels();
		ThreadPoolManager.getInstance().scheduleGeneral(new unCubeSpawn(), BAIUMCUBEUNSPAWN);
		_log.info("BaiumManager: O Baium foi eliminado. State -> " + _state.getState());
	}

	public void unspawnCube()
	{
		if (_teleportCube != null)
		{
			_teleportCube.deleteMe();
			_teleportCube = null;
		}
		clearLair();
		_state.setState(StateEnum.INTERVAL);
		_log.info("BaiumManager: Players Teleportados para cidade. State -> " + _state.getState());
	}


	public void wakeBaium(L2PcInstance waker)
	{
		_waker = waker;
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				spawnBaium();
			}
		}, 2000);
	}

}
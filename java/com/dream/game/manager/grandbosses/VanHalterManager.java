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

import com.dream.L2DatabaseFactory;
import com.dream.annotations.L2Properties;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.model.quest.pack.ai.Vanhalter;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.tools.random.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class VanHalterManager extends BossLair
{
	public class Bleeding implements Runnable
	{
		@Override
		public void run()
		{
			addBleeding();

			if (_setBleedTask != null)
			{
				_setBleedTask.cancel(false);
			}
			_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);
		}
	}

	public class CallRoyalGuardHelper implements Runnable
	{
		@Override
		public void run()
		{
			spawnRoyalGuardHepler();

			if (_royalGuardHepler.size() <= ROYAL_GUARD_COUNT && !_vanHalter.isDead())
			{
				if (_callRoyalGuardHelperTask != null)
				{
					_callRoyalGuardHelperTask.cancel(false);
				}
				_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), ROYAL_GUARD_INTERVAL);
			}
			else
			{
				if (_callRoyalGuardHelperTask != null)
				{
					_callRoyalGuardHelperTask.cancel(false);
				}
				_callRoyalGuardHelperTask = null;
			}
		}
	}

	public class CloseDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			closeDoorOfAltar(true);
		}
	}

	public class HalterEscape implements Runnable
	{
		@Override
		public void run()
		{
			if (_royalGuardHepler.size() <= ROYAL_GUARD_COUNT && !_vanHalter.isDead())
			{
				if (_vanHalter.isAfraid())
				{
					_vanHalter.stopFear(null);
				}
				else
				{
					_vanHalter.startFear();
					if (_vanHalter.getZ() >= -10476)
					{
						L2CharPosition pos = new L2CharPosition(-16397, -53308, -10448, 0);
						if (_vanHalter.getX() == pos.x && _vanHalter.getY() == pos.y)
						{
							_vanHalter.stopFear(null);
						}
						else
						{
							_vanHalter.getAI().setIntention(CtrlIntention.MOVE_TO, pos);
						}
					}
					else if (_vanHalter.getX() >= -16397)
					{
						L2CharPosition pos = new L2CharPosition(-15548, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.MOVE_TO, pos);
					}
					else
					{
						L2CharPosition pos = new L2CharPosition(-17248, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.MOVE_TO, pos);
					}
				}
				if (_halterEscapeTask != null)
				{
					_halterEscapeTask.cancel(false);
				}
				_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 5000);
			}
			else
			{
				_vanHalter.stopFear(null);
				if (_halterEscapeTask != null)
				{
					_halterEscapeTask.cancel(false);
				}
				_halterEscapeTask = null;
			}
		}
	}

	// interval.
	public class Interval implements Runnable
	{
		@Override
		public void run()
		{
			setupAltar();
		}
	}

	public class LockUpDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			closeDoorOfAltar(false);
			_isLocked = true;
			_lockUpDoorOfAltarTask = null;
		}
	}

	// appearance movie.
	private class Movie implements Runnable
	{
		private final int _distance = 6502500;
		private final int _taskId;
		private final List<L2PcInstance> _players = getPlayersInside();

		public Movie(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			_vanHalter.setHeading(16384);
			_vanHalter.setTarget(_ritualOffering);

			switch (_taskId)
			{
				case 1:
					_state.setState(GrandBossState.StateEnum.ALIVE);
					_state.update();

					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50, 90, 0, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(2), 16);

					break;

				case 2:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(5), 1842, 100, -3, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(3), 1);

					break;

				case 3:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(5), 1861, 97, -10, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(4), 1500);

					break;

				case 4:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(4), 1876, 97, 12, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(5), 1);

					break;

				case 5:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(4), 1839, 94, 0, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(6), 1500);

					break;

				case 6:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(3), 1872, 94, 15, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(7), 1);

					break;

				case 7:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(3), 1839, 92, 0, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(8), 1500);

					break;

				case 8:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(2), 1872, 92, 15, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(9), 1);

					break;

				case 9:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(2), 1839, 90, 5, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(10), 1500);

					break;

				case 10:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(1), 1872, 90, 5, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(11), 1);

					break;

				case 11:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(1), 2002, 90, 2, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(12), 2000);

					break;

				case 12:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50, 90, 10, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(13), 1000);

					break;

				case 13:
					// High Priestess van Halter uses the skill to kill Ritual
					// Offering.
					L2Skill skill = SkillTable.getInstance().getInfo(1168, 7);
					_ritualOffering.setIsInvul(false);
					_vanHalter.setTarget(_ritualOffering);
					_vanHalter.setIsImmobilized(false);
					_vanHalter.doCast(skill);
					_vanHalter.setIsImmobilized(true);

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(14), 4700);

					break;

				case 14:
					_ritualOffering.setIsInvul(false);
					_ritualOffering.reduceCurrentHp(_ritualOffering.getMaxHp() + 1, _vanHalter);

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(15), 4300);

					break;

				case 15:
					spawnRitualSacrifice();
					deleteRitualOffering();

					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 100, 90, 15, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(16), 2000);

					break;

				case 16:
					// set camera.
					for (L2PcInstance pc : _players)
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 5200, 90, -10, 9500, 6000);
						}
						else
						{
							pc.leaveMovieMode();
						}

					// set next task.
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(17), 6000);

					break;

				case 17:
					// reset camera.
					for (L2PcInstance pc : _players)
					{
						pc.leaveMovieMode();
					}
					deleteRitualSacrifice();
					deleteCameraMarker();
					_vanHalter.setIsImmobilized(false);
					_vanHalter.setIsInvul(false);

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(18), 1000);

					break;

				case 18:
					combatBeginning();
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
			}
		}
	}

	public class OpenDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			openDoorOfAltar(true);
		}
	}

	// time up.
	public class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			enterInterval();
		}
	}

	private static VanHalterManager _instance;

	public static VanHalterManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new VanHalterManager();
		}
		return _instance;
	}

	// list of intruders.
	protected Map<Integer, List<L2PcInstance>> _bleedingPlayers = new HashMap<>();
	// spawn data of monsters.
	protected Map<Integer, L2Spawn> _monsterSpawn = new HashMap<>();

	protected List<L2Spawn> _royalGuardSpawn = new ArrayList<>();
	protected List<L2Spawn> _royalGuardCaptainSpawn = new ArrayList<>();
	protected List<L2Spawn> _royalGuardHelperSpawn = new ArrayList<>();
	protected List<L2Spawn> _triolRevelationSpawn = new ArrayList<>();
	protected List<L2Spawn> _triolRevelationAlive = new ArrayList<>();
	protected List<L2Spawn> _guardOfAltarSpawn = new ArrayList<>();
	protected Map<Integer, L2Spawn> _cameraMarkerSpawn = new HashMap<>();
	protected L2Spawn _ritualOfferingSpawn = null;
	protected L2Spawn _ritualSacrificeSpawn = null;
	protected L2Spawn _vanHalterSpawn = null;
	// instance of monsters.
	protected List<L2Npc> _monsters = new ArrayList<>();
	protected List<L2Npc> _royalGuard = new ArrayList<>();

	protected List<L2Npc> _royalGuardCaptain = new ArrayList<>();
	protected List<L2Npc> _royalGuardHepler = new ArrayList<>();
	protected List<L2Npc> _triolRevelation = new ArrayList<>();
	protected List<L2Npc> _guardOfAltar = new ArrayList<>();
	protected Map<Integer, L2Npc> _cameraMarker = new HashMap<>();
	protected List<L2DoorInstance> _doorOfAltar = new ArrayList<>();
	protected List<L2DoorInstance> _doorOfSacrifice = new ArrayList<>();
	protected L2Npc _ritualOffering = null;
	protected L2Npc _ritualSacrifice = null;

	protected L2RaidBossInstance _vanHalter = null;
	// Task
	protected ScheduledFuture<?> _movieTask = null;
	protected ScheduledFuture<?> _closeDoorOfAltarTask = null;
	protected ScheduledFuture<?> _openDoorOfAltarTask = null;
	protected ScheduledFuture<?> _lockUpDoorOfAltarTask = null;
	protected ScheduledFuture<?> _callRoyalGuardHelperTask = null;
	protected ScheduledFuture<?> _timeUpTask = null;
	protected ScheduledFuture<?> _intervalTask = null;
	protected ScheduledFuture<?> _halterEscapeTask = null;
	protected ScheduledFuture<?> _setBleedTask = null;
	// state of High Priestess van Halter
	boolean _isLocked = false;
	boolean _isHalterSpawned = false;
	boolean _isSacrificeSpawned = false;
	boolean _isCaptainSpawned = false;

	boolean _isHelperCalled = false;

	private long ACTIVITY_TIME;

	private long TIME_OF_LOCK_UPDOOROFALTAR;

	private long INTERVAL_OF_DOOR;

	private long APPERANCE_TIME;

	private long FIGHT_TIME;

	private long MIN_RESPAWN;

	private long MAX_RESPAWN;

	public int ROYAL_GUARD_COUNT;

	public long ROYAL_GUARD_INTERVAL;

	public VanHalterManager()
	{
		super();
		new Vanhalter();
		_questName = null;
		_state = new GrandBossState(29062);
		try
		{
			L2Properties p = new L2Properties("./config/main/bosses.properties");
			ACTIVITY_TIME = Integer.parseInt(p.getProperty("VanHalterActiveTime", "360")) * 60000;
			TIME_OF_LOCK_UPDOOROFALTAR = Integer.parseInt(p.getProperty("VanHalterTimeOfLockUpDoorOfAltar", "3")) * 60000;
			INTERVAL_OF_DOOR = Integer.parseInt(p.getProperty("VanHalterIntervalOfDoorOfAlter", "90")) * 60000;
			APPERANCE_TIME = Integer.parseInt(p.getProperty("VanHalterAppearanceTime", "1")) * 60000;
			FIGHT_TIME = Integer.parseInt(p.getProperty("VanHalterFightTime", "120")) * 60000;
			MIN_RESPAWN = Integer.parseInt(p.getProperty("VanHalterMinRespawn", "2880")) * 60000;
			MAX_RESPAWN = Integer.parseInt(p.getProperty("VanHalterMaxRespawn", "3600")) * 60000;
			ROYAL_GUARD_COUNT = 6;
			ROYAL_GUARD_INTERVAL = 1 * 60000;

		}
		catch (Exception e)
		{
			_log.error("VanHalterManager: Error while reading config", e);
		}

	}

	// check bleeding player.
	protected void addBleeding()
	{
		L2Skill bleed = SkillTable.getInstance().getInfo(4615, 12);

		for (L2Npc tr : _triolRevelation)
		{
			if (!tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).iterator().hasNext() || tr.isDead())
			{
				continue;
			}

			List<L2PcInstance> bpc = new ArrayList<>();

			for (L2PcInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc);
					tr.broadcastPacket(new MagicSkillUse(tr, pc, bleed.getId(), 12, 1, 1, false));
				}

				bpc.add(pc);
			}
			_bleedingPlayers.remove(tr.getNpcId());
			_bleedingPlayers.put(tr.getNpcId(), bpc);
		}
	}

	// call Royal Guard Helper and escape from player.
	public void callRoyalGuardHelper()
	{
		if (!_isHelperCalled)
		{
			_isHelperCalled = true;
			_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 500);
			_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), 1000);
		}
	}

	public void checkRoyalGuardCaptainDestroy()
	{
		if (!_isHalterSpawned)
			return;

		deleteRoyalGuard();
		deleteRoyalGuardCaptain();
		spawnGuardOfAltar();
		openDoorOfSacrifice();

		for (L2PcInstance pc : getPlayersInside())
		{
			pc.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Altar's Gatekeeper", "The door of the 3rd floor in the altar was opened."));
		}

		_vanHalter.setIsImmobilized(false);
		_vanHalter.setIsInvul(false);
		spawnCameraMarker();

		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;

		_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1), APPERANCE_TIME);
	}

	// event
	public void checkTriolRevelationDestroy()
	{
		if (_isCaptainSpawned)
			return;

		boolean isTriolRevelationDestroyed = true;
		for (L2Spawn tra : _triolRevelationAlive)
			if (!tra.getLastSpawn().isDead())
			{
				isTriolRevelationDestroyed = false;
			}

		if (isTriolRevelationDestroyed)
		{
			spawnRoyalGuardCaptain();
		}
	}

	protected void closeDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _doorOfAltar)
		{
			door.closeMe();
		}

		if (loop)
		{
			if (_openDoorOfAltarTask != null)
			{
				_openDoorOfAltarTask.cancel(false);
			}
			_openDoorOfAltarTask = null;
			_openDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(), INTERVAL_OF_DOOR);
		}
		else
		{
			if (_openDoorOfAltarTask != null)
			{
				_openDoorOfAltarTask.cancel(false);
			}
			_openDoorOfAltarTask = null;
		}
	}

	protected void closeDoorOfSacrifice()
	{
		for (L2DoorInstance door : _doorOfSacrifice)
		{
			try
			{
				door.closeMe();
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	// start fight against High Priestess van Halter.
	protected void combatBeginning()
	{
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), FIGHT_TIME);

		Map<Integer, L2PcInstance> _targets = new HashMap<>();
		int i = 0;

		for (L2PcInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
		{
			i++;
			_targets.put(i, pc);
		}

		_vanHalter.reduceCurrentHp(1, _targets.get(Rnd.get(1, i)));
	}

	protected void deleteCameraMarker()
	{
		if (_cameraMarker.isEmpty())
			return;

		for (int i = 1; i <= _cameraMarker.size(); i++)
		{
			_cameraMarker.get(i).deleteMe();
		}
		_cameraMarker.clear();
	}

	protected void deleteGuardOfAltar()
	{
		for (L2Npc tr : _guardOfAltar)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}

		_guardOfAltar.clear();
	}

	protected void deleteRitualOffering()
	{
		_ritualOffering.setIsImmobilized(false);
		_ritualOffering.setIsInvul(false);
		_ritualOffering.setIsParalyzed(false);
		_ritualOffering.getSpawn().stopRespawn();
		_ritualOffering.deleteMe();
	}

	protected void deleteRitualSacrifice()
	{
		if (!_isSacrificeSpawned)
			return;

		_ritualSacrifice.getSpawn().stopRespawn();
		_ritualSacrifice.deleteMe();
		_isSacrificeSpawned = false;
	}

	protected void deleteRoyalGuard()
	{
		for (L2Npc rg : _royalGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}

		_royalGuard.clear();
	}

	protected void deleteRoyalGuardCaptain()
	{
		for (L2Npc tr : _royalGuardCaptain)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}

		_royalGuardCaptain.clear();
	}

	protected void deleteRoyalGuardHepler()
	{
		for (L2Npc tr : _royalGuardHepler)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_royalGuardHepler.clear();
	}

	protected void deleteTriolRevelation()
	{
		for (L2Npc tr : _triolRevelation)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_triolRevelation.clear();
		_bleedingPlayers.clear();
	}

	protected void deleteVanHalter()
	{
		_vanHalter.setIsImmobilized(false);
		_vanHalter.setIsInvul(false);
		_vanHalter.getSpawn().stopRespawn();
		_vanHalter.deleteMe();
	}

	// High Priestess van Halter dead or time up.
	public void enterInterval()
	{
		// cancel all task
		if (_callRoyalGuardHelperTask != null)
		{
			_callRoyalGuardHelperTask.cancel(false);
		}
		_callRoyalGuardHelperTask = null;

		if (_closeDoorOfAltarTask != null)
		{
			_closeDoorOfAltarTask.cancel(false);
		}
		_closeDoorOfAltarTask = null;

		if (_halterEscapeTask != null)
		{
			_halterEscapeTask.cancel(false);
		}
		_halterEscapeTask = null;

		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}
		_intervalTask = null;

		if (_lockUpDoorOfAltarTask != null)
		{
			_lockUpDoorOfAltarTask.cancel(false);
		}
		_lockUpDoorOfAltarTask = null;

		if (_movieTask != null)
		{
			_movieTask.cancel(false);
		}
		_movieTask = null;

		if (_openDoorOfAltarTask != null)
		{
			_openDoorOfAltarTask.cancel(false);
		}
		_openDoorOfAltarTask = null;

		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;

		// delete monsters
		if (_vanHalter.isDead())
		{
			_vanHalter.getSpawn().stopRespawn();
		}
		else
		{
			deleteVanHalter();
		}
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualOffering();
		deleteRitualSacrifice();
		deleteGuardOfAltar();

		// set interval end.
		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}

		if (!_state.getState().equals(GrandBossState.StateEnum.INTERVAL))
		{
			_state.setRespawnDate(Rnd.get(MIN_RESPAWN, MAX_RESPAWN));
			_state.setState(GrandBossState.StateEnum.INTERVAL);
			_state.update();
		}

		_intervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(), _state.getInterval());
	}

	// initialize
	@Override
	public void init()
	{
		// clear flag.
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;

		// setting door state.
		_doorOfAltar.add(DoorTable.getInstance().getDoor(19160014));
		_doorOfAltar.add(DoorTable.getInstance().getDoor(19160015));
		openDoorOfAltar(true);
		_doorOfSacrifice.add(DoorTable.getInstance().getDoor(19160016));
		_doorOfSacrifice.add(DoorTable.getInstance().getDoor(19160017));
		closeDoorOfSacrifice();

		// load spawn data of monsters.
		loadRoyalGuard();
		loadTriolRevelation();
		loadRoyalGuardCaptain();
		loadRoyalGuardHelper();
		loadGuardOfAltar();
		loadVanHalter();
		loadRitualOffering();
		loadRitualSacrifice();

		// spawn monsters.
		spawnRoyalGuard();
		spawnTriolRevelation();
		spawnVanHalter();
		spawnRitualOffering();

		// setting spawn data of Dummy camera marker.
		_cameraMarkerSpawn.clear();
		try
		{
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(13014); // Dummy
			// npc
			L2Spawn tempSpawn;

			// Dummy camera marker.
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10449);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			_cameraMarkerSpawn.put(1, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10051);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			_cameraMarkerSpawn.put(2, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9741);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			_cameraMarkerSpawn.put(3, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9394);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			_cameraMarkerSpawn.put(4, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55197);
			tempSpawn.setLocz(-8739);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			_cameraMarkerSpawn.put(5, tempSpawn);
		}
		catch (Exception e)
		{
			_log.warn("VanHalterManager : " + e.getMessage());
		}

		// set time up.
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), ACTIVITY_TIME);

		// set bleeding to palyers.
		if (_setBleedTask != null)
		{
			_setBleedTask.cancel(false);
		}
		_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);
		if (_state.getState() == StateEnum.DEAD)
		{
			_state.setRespawnDate(Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000);
			_state.setState(StateEnum.INTERVAL);
		}

		// check state of High Priestess van Halter.
		if (_state.getState().equals(GrandBossState.StateEnum.INTERVAL) || _state.getState().equals(GrandBossState.StateEnum.DEAD))
		{
			enterInterval();
		}
		else
		{
			_state.setState(GrandBossState.StateEnum.ALIVE);
		}
		_log.info("Van Halter : State is " + _state.getState() + ".");

	}

	public void intruderDetection(L2PcInstance intruder)
	{
		if (_lockUpDoorOfAltarTask == null && !_isLocked && _isCaptainSpawned)
		{
			_lockUpDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockUpDoorOfAltar(), TIME_OF_LOCK_UPDOOROFALTAR);
		}
	}

	// load Guard Of Altar
	
	protected void loadGuardOfAltar()
	{
		_guardOfAltarSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32051);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_guardOfAltarSpawn.add(spawnDat);
				}
				else
				{
					_log.warn("VanHalterManager : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded " +
			// _guardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager : Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// load Ritual Offering.
	
	protected void loadRitualOffering()
	{
		_ritualOfferingSpawn = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32038);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_ritualOfferingSpawn = spawnDat;
				}
				else
				{
					_log.warn("VanHalterManager : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded Ritual Offering spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// Load Ritual Sacrifice.
	
	protected void loadRitualSacrifice()
	{
		_ritualSacrificeSpawn = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22195);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_ritualSacrificeSpawn = spawnDat;
				}
				else
				{
					_log.warn("VanHalterManager : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded Ritual Sacrifice spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager : Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// load Royal Guard.
	
	protected void loadRoyalGuard()
	{
		_royalGuardSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 22175);
			statement.setInt(2, 22176);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_royalGuardSpawn.add(spawnDat);
				}
				else
				{
					_log.warn("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded " + _royalGuardSpawn.size()
			// + " Royal Guard spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager : Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// load Royal Guard Captain.
	
	protected void loadRoyalGuardCaptain()
	{
		_royalGuardCaptainSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22188);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_royalGuardCaptainSpawn.add(spawnDat);
				}
				else
				{
					_log.warn("VanHalterManager : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded " +
			// _royalGuardCaptainSpawn.size() +
			// " Royal Guard Captain spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager : Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// load Royal Guard Helper.
	
	protected void loadRoyalGuardHelper()
	{
		_royalGuardHelperSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22191);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_royalGuardHelperSpawn.add(spawnDat);
				}
				else
				{
					_log.warn("VanHalterManager : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded " +
			// _royalGuardHelperSpawn.size() +
			// " Royal Guard Helper spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager. : Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// load Triol's Revelation.
	
	protected void loadTriolRevelation()
	{
		_triolRevelationSpawn.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 32058);
			statement.setInt(2, 32068);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_triolRevelationSpawn.add(spawnDat);
				}
				else
				{
					_log.warn("VanHalterManager : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded " +
			// _triolRevelationSpawn.size() +
			// " Triol's Revelation spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager : Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// load High Priestess van Halter.
	
	protected void loadVanHalter()
	{
		_vanHalterSpawn = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(null);
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 29062);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					_vanHalterSpawn = spawnDat;
					_bossSpawn = _vanHalterSpawn;
				}
				else
				{
					_log.warn("VanHalterManager : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			// _log.info("VanHalterManager : Loaded High Priestess van Halter spawn locations.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warn("VanHalterManager : Spawn could not be initialized: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	protected void openDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _doorOfAltar)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}

		if (loop)
		{
			_isLocked = false;

			if (_closeDoorOfAltarTask != null)
			{
				_closeDoorOfAltarTask.cancel(false);
			}
			_closeDoorOfAltarTask = null;
			_closeDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(), INTERVAL_OF_DOOR);
		}
		else
		{
			if (_closeDoorOfAltarTask != null)
			{
				_closeDoorOfAltarTask.cancel(false);
			}
			_closeDoorOfAltarTask = null;
		}
	}

	protected void openDoorOfSacrifice()
	{
		for (L2DoorInstance door : _doorOfSacrifice)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public void removeBleeding(int npcId)
	{
		if (_bleedingPlayers.get(npcId) == null)
			return;
		for (L2PcInstance pc : _bleedingPlayers.get(npcId))
			if (pc.getFirstEffect(L2EffectType.DMG_OVER_TIME) != null)
			{
				pc.stopEffects(L2EffectType.DMG_OVER_TIME);
			}
		_bleedingPlayers.remove(npcId);
	}

	@Override
	public void setRespawn()
	{
	}

	@Override
	public void setUnspawn()
	{
	}

	// interval end.
	public void setupAltar()
	{
		// cancel all task
		if (_callRoyalGuardHelperTask != null)
		{
			_callRoyalGuardHelperTask.cancel(false);
		}
		_callRoyalGuardHelperTask = null;

		if (_closeDoorOfAltarTask != null)
		{
			_closeDoorOfAltarTask.cancel(false);
		}
		_closeDoorOfAltarTask = null;

		if (_halterEscapeTask != null)
		{
			_halterEscapeTask.cancel(false);
		}
		_halterEscapeTask = null;

		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}
		_intervalTask = null;

		if (_lockUpDoorOfAltarTask != null)
		{
			_lockUpDoorOfAltarTask.cancel(false);
		}
		_lockUpDoorOfAltarTask = null;

		if (_movieTask != null)
		{
			_movieTask.cancel(false);
		}
		_movieTask = null;

		if (_openDoorOfAltarTask != null)
		{
			_openDoorOfAltarTask.cancel(false);
		}
		_openDoorOfAltarTask = null;

		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;

		// delete all monsters
		deleteVanHalter();
		deleteTriolRevelation();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualSacrifice();
		deleteRitualOffering();
		deleteGuardOfAltar();
		deleteCameraMarker();

		// clear flag.
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;

		// set door state
		closeDoorOfSacrifice();
		openDoorOfAltar(true);

		// respawn monsters.
		spawnTriolRevelation();
		spawnRoyalGuard();
		spawnRitualOffering();
		spawnVanHalter();

		_state.setState(GrandBossState.StateEnum.NOTSPAWN);
		_state.update();

		// set time up.
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), ACTIVITY_TIME);
	}

	protected void spawnCameraMarker()
	{
		_cameraMarker.clear();
		for (int i = 1; i <= _cameraMarkerSpawn.size(); i++)
		{
			_cameraMarker.put(i, _cameraMarkerSpawn.get(i).doSpawn());
			_cameraMarker.get(i).getSpawn().stopRespawn();
			_cameraMarker.get(i).setIsImmobilized(true);
		}
	}

	protected void spawnGuardOfAltar()
	{
		if (!_guardOfAltar.isEmpty())
		{
			deleteGuardOfAltar();
		}

		for (L2Spawn trs : _guardOfAltarSpawn)
		{
			trs.startRespawn();
			_guardOfAltar.add(trs.doSpawn());
		}
	}

	protected void spawnRitualOffering()
	{
		_ritualOffering = _ritualOfferingSpawn.doSpawn();
		_ritualOffering.setIsImmobilized(true);
		_ritualOffering.setIsInvul(true);
		_ritualOffering.setIsParalyzed(true);
	}

	protected void spawnRitualSacrifice()
	{
		_ritualSacrifice = _ritualSacrificeSpawn.doSpawn();
		_ritualSacrifice.setIsImmobilized(true);
		_ritualSacrifice.setIsInvul(true);
		_isSacrificeSpawned = true;
	}

	protected void spawnRoyalGuard()
	{
		if (!_royalGuard.isEmpty())
		{
			deleteRoyalGuard();
		}

		for (L2Spawn rgs : _royalGuardSpawn)
		{
			rgs.startRespawn();
			_royalGuard.add(rgs.doSpawn());
		}
	}

	protected void spawnRoyalGuardCaptain()
	{
		if (!_royalGuardCaptain.isEmpty())
		{
			deleteRoyalGuardCaptain();
		}

		for (L2Spawn trs : _royalGuardCaptainSpawn)
		{
			trs.startRespawn();
			_royalGuardCaptain.add(trs.doSpawn());
		}
		_isCaptainSpawned = true;
	}

	protected void spawnRoyalGuardHepler()
	{
		for (L2Spawn trs : _royalGuardHelperSpawn)
		{
			trs.startRespawn();
			_royalGuardHepler.add(trs.doSpawn());
		}
	}

	protected void spawnTriolRevelation()
	{
		if (!_triolRevelation.isEmpty())
		{
			deleteTriolRevelation();
		}

		for (L2Spawn trs : _triolRevelationSpawn)
		{
			trs.startRespawn();
			_triolRevelation.add(trs.doSpawn());
			if (trs.getNpcid() != 32067 && trs.getNpcid() != 32068)
			{
				_triolRevelationAlive.add(trs);
			}
		}
	}

	protected void spawnVanHalter()
	{
		_vanHalter = (L2RaidBossInstance) _vanHalterSpawn.doSpawn();
		_vanHalter.setIsImmobilized(false);
		_vanHalter.setIsInvul(false);
		_isHalterSpawned = true;
	}
}
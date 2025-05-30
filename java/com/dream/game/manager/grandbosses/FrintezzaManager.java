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
import java.util.concurrent.Future;

import com.dream.annotations.L2Properties;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2FrintezzaBossInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.Earthquake;
import com.dream.game.network.serverpackets.ExShowScreenMessage;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.skills.AbnormalEffect;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;
import com.dream.util.ArrayUtils;

public class FrintezzaManager extends BossLair
{
	private class ActionTask implements Runnable
	{

		private int _taskId;

		public ActionTask(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			switch (_taskId)
			{
				case 2:
					showSocialActionMovie(_frintezza, 1000, 90, 30, 0, 5000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(setTask(200), 3000);
					break;
				case 200:
					showSocialActionMovie(_frintezza, 1000, 90, 30, 0, 5000, 0);
					ThreadPoolManager.getInstance().scheduleGeneral(setTask(3), 3000);
					break;
				case 3:
					showSocialActionMovie(_frintezza, 140, 90, 0, 6000, 6000, 2);
					ThreadPoolManager.getInstance().scheduleGeneral(setTask(5), 5990);
					break;
				case 5:
					showSocialActionMovie(_frintezza, 240, 90, 3, 22000, 6000, 3);
					ThreadPoolManager.getInstance().scheduleGeneral(setTask(6), 5800);
					break;
				case 6:
					showSocialActionMovie(_frintezza, 240, 90, 3, 300, 6000, 0);
					for (L2PcInstance pc : getPlayersInside())
					{
						pc.sendPacket(new MagicSkillUse(_frintezza, _frintezza, 5006, 1, _intervalOfFrintezzaSongs, 0, false));
					}
					_scarlet = (L2FrintezzaBossInstance) _weakScarlet.doSpawn();
					for (L2Spawn spawn : _portraits)
					{
						L2Npc npc = spawn.doSpawn();
						npc.setIsImmobilized(true);
					}
					for (L2Spawn spawn : _deamons)
					{
						spawn.startRespawn();
						spawn.doSpawn();
					}
					for (L2Spawn spawn : _minions)
					{
						spawn.doSpawn();
					}
					for (L2PcInstance pc : getPlayersInside())
					{
						pc.sendPacket(new Earthquake(_scarlet.getX(), _scarlet.getY(), _scarlet.getZ(), 50, 6));
					}
					ThreadPoolManager.getInstance().scheduleGeneral(setTask(7), 5800);
					break;
				case 7:
					for (L2PcInstance pc : getPlayersInside())
					{
						pc.leaveMovieMode();
						pc.enableAllSkills();
						pc.setIsImmobilized(false);
					}
					_frintezza.setIsImmobilized(false);
					DoorTable.getInstance().getDoor(25150046).closeMe();
					ThreadPoolManager.getInstance().scheduleGeneral(new MusicTask(Rnd.get(1, 4)), Rnd.get(_intervalOfFrintezzaSongs, _intervalOfFrintezzaSongs * 2));
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							stopActivity();
						}
					}, ACTIVITY_TIME * 60000);

					break;
			}

		}

		private ActionTask setTask(int taskId)
		{
			_taskId = taskId;
			return this;
		}

	}

	public class MusicTask implements Runnable
	{

		private int _musicId;

		public MusicTask(int musicId)
		{
			_musicId = musicId;
		}

		public L2Character[] getMusicTarget()
		{
			L2Character[] result = new L2Character[] {};
			switch (_musicId)
			{
				case 1:
				case 5:
					for (L2PcInstance pc : getPlayersInside())
						if (!pc.isDead() && pc.isInsideRadius(_frintezza, 5000, false, false))
						{
							result = ArrayUtils.add(result, pc);
						}
					break;
				case 2:
					if (_scarlet != null && !_scarlet.isDead())
					{
						result = ArrayUtils.add(result, _scarlet);
					}
					break;
				case 3:
				case 4:
					if (_scarlet != null && !_scarlet.isDead())
					{
						result = ArrayUtils.add(result, _scarlet);
					}
					for (L2Spawn s : _deamons)
						if (!s.getLastSpawn().isDead())
						{
							result = ArrayUtils.add(result, s.getLastSpawn());
						}
					break;
			}
			return result;
		}


		@Override
		public void run()
		{
			if (!_canPlayMusic || _frintezza == null)
				return;
			if (!_skipMusic)
			{
				for (L2PcInstance pc : getPlayersInside())
				{
					pc.sendPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, 1, _intervalOfFrintezzaSongs, 0, false));

				}
				L2Skill skill = null;
				switch (_musicId)
				{
					case 2:
						for (L2PcInstance pc : getPlayersInside())
						{
							pc.sendPacket(new ExShowScreenMessage("Frintezza's Rampaging Opus", 10000));
							pc.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Shout, "Frintezza", "Frintezza's Rampaging Opus"));
						}
						skill = SkillTable.getInstance().getInfo(1217, 33);
						break;
					case 3:
						for (L2PcInstance pc : getPlayersInside())
						{
							pc.sendPacket(new ExShowScreenMessage("Frintezza's Power Concerto", 10000));
							pc.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Shout, "Frintezza", "Frintezza's Power Concerto"));
						}
						skill = SkillTable.getInstance().getInfo(1204, 2);
						break;
					case 4:
						for (L2PcInstance pc : getPlayersInside())
						{
							pc.sendPacket(new ExShowScreenMessage("Frintezza's Plagued Concerto", 10000));
							pc.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Shout, "Frintezza", "Frintezza's Plagued Concerto"));
						}
						skill = SkillTable.getInstance().getInfo(1086, 2);
						break;
					case 5:
						for (L2PcInstance pc : getPlayersInside())
						{
							pc.sendPacket(new ExShowScreenMessage("Frintezza's  Psycho Symphony", 10000));
							pc.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Shout, "Frintezza", "Frintezza's Psycho Symphony"));
						}
						skill = SkillTable.getInstance().getInfo(5008, 5);
						break;
				}
				if (skill != null)
				{
					_frintezza.callSkill(skill, getMusicTarget());
				}
				if (_musicId == 5)
				{
					for (L2Character ch : getMusicTarget())
					{
						setIdle(ch);
						ch.startAbnormalEffect(AbnormalEffect.DANCE_STUNNED);
					}

					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							for (L2PcInstance pc : getPlayersInside())
							{
								pc.stopSkillEffects(5008);
								pc.setIsImmobilized(false);
								pc.enableAllSkills();
								pc.stopAbnormalEffect(AbnormalEffect.DANCE_STUNNED);
							}

						}
					}, Rnd.get(_intervalOfFrintezzaSongs / 2, _intervalOfFrintezzaSongs));
				}
			}
			_skipMusic = false;

			ThreadPoolManager.getInstance().scheduleGeneral(setMusic(Rnd.get(1, _isWeakScarlet ? 4 : 5)), Rnd.get(_intervalOfFrintezzaSongs, _intervalOfFrintezzaSongs * 2));
		}

		public MusicTask setMusic(int musicId)
		{
			_musicId = musicId;
			return this;
		}

		public String onSkillSee(L2Npc npc, L2Skill skill)
		{
				if (skill != null)
				{
				if ((npc.getNpcId() == 29045) && (skill.getId() == SOUL_BREAKING_ARROW_SKILL_ID))
					{
					skipMusic();
					}
				}
			return null;
		}

	}

	private static FrintezzaManager _instance;

	private static L2Spawn createNewSpawn(int templateId, int x, int y, int z, int heading, int respawnDelay)
	{
		L2NpcTemplate template1;
		template1 = NpcTable.getInstance().getTemplate(templateId);
		L2Spawn tempSpawn = new L2Spawn(template1);
		tempSpawn.setLocx(x);
		tempSpawn.setLocy(y);
		tempSpawn.setLocz(z);
		tempSpawn.setHeading(heading);
		tempSpawn.setAmount(1);
		tempSpawn.setRespawnDelay(respawnDelay);
		return tempSpawn;
	}

	public static FrintezzaManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new FrintezzaManager();
		}

		return _instance;
	}

	private boolean ENABLED;
	public long ACTIVITY_TIME;
	long MIN_RESPAWN;
	long MAX_RESPAWN;
	private final int _intervalOfFrintezzaSongs = 20000;
	public L2Spawn _weakScarlet;
	public L2FrintezzaBossInstance _frintezza;
	public L2FrintezzaBossInstance _scarlet;
	private L2Spawn _strongScarlet;
	public L2Spawn _cubeSpawn;
	public final List<L2Spawn> _portraits = new ArrayList<>();
	public final List<L2Spawn> _deamons = new ArrayList<>();
	public final List<L2Spawn> _minions = new ArrayList<>();
	public boolean _isWeakScarlet;
	public boolean _canPlayMusic;
	private static final int SOUL_BREAKING_ARROW_SKILL_ID = 2234;
	public boolean _skipMusic;

	public Future<?> _startTask;
	public Future<?> _tocadj;

	public FrintezzaManager()
	{
		super();
		_questName = "frintezza";
		_state = new GrandBossState(29045);
		try
		{
			L2Properties p = new L2Properties("./config/main/bosses.properties");
			ENABLED = Boolean.parseBoolean(p.getProperty("FrintezzaEnabled", "true"));
			ACTIVITY_TIME = Integer.parseInt(p.getProperty("FrintezzaActiveTime", "60"));
			MIN_RESPAWN = Integer.parseInt(p.getProperty("FrintezzaMinRespawn", "1440"));
			MAX_RESPAWN = Integer.parseInt(p.getProperty("FrintezzaMaxRespawn", "2880"));

		}
		catch (Exception e)
		{
			_log.error("FrintezzaManager: Error while reading config", e);
			return;
		}

	}

	public synchronized void callAssist(L2Character attacker)
	{
		if (!ENABLED)
			return;

		for (L2Spawn s : _deamons)
		{
			L2Attackable m = (L2Attackable) s.getLastSpawn();
			if (m != null && !m.isDead() && !m.isInCombat())
			{
				m.addDamageHate(attacker, 1, 100);
			}
		}
	}

	public void finish()
	{
		if (!ENABLED)
			return;

		if (_scarlet != null)
		{
			_scarlet.deleteMe();
			_scarlet = null;
		}
		if (_frintezza != null)
		{
			_frintezza.deleteMe();
			_frintezza = null;
		}
		for (L2Spawn spawn : _portraits)
		{
			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
			{
				spawn.getLastSpawn().deleteMe();
			}
		}
		for (L2Spawn spawn : _deamons)
		{
			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
			{
				spawn.getLastSpawn().deleteMe();
			}
		}

		for (L2Spawn spawn : _minions)
		{
			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
			{
				spawn.getLastSpawn().deleteMe();
			}
		}

		if (_state.getState() == StateEnum.DEAD)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					_cubeSpawn.doSpawn();
				}
			}, 2000);
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					stopActivity();
				}
			}, 180000);
		}
	}
	
	@Override
	public void init()
	{
		if (!ENABLED)
			return;
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(29045);
		_bossSpawn = new L2Spawn(template);
		_bossSpawn.setLocx(174240);
		_bossSpawn.setLocy(-89805);
		_bossSpawn.setLocz(-5022);
		_bossSpawn.setHeading(16048);

		template = NpcTable.getInstance().getTemplate(29046);
		_weakScarlet = new L2Spawn(template);
		_weakScarlet.setLocx(174234);
		_weakScarlet.setLocy(-88015);
		_weakScarlet.setLocz(-5116);
		_weakScarlet.setHeading(48028);
		template = NpcTable.getInstance().getTemplate(29047);
		_strongScarlet = new L2Spawn(template);

		template = NpcTable.getInstance().getTemplate(31759);
		_cubeSpawn = new L2Spawn(template);
		_cubeSpawn.setLocx(174234);
		_cubeSpawn.setLocy(-88015);
		_cubeSpawn.setLocz(-5116);

		_portraits.add(createNewSpawn(29048, 175833, -87165, -4972, 35048, 0));
		_portraits.add(createNewSpawn(29049, 175876, -88713, -4972, 28205, 0));
		_portraits.add(createNewSpawn(29048, 172608, -88702, -4972, 64817, 0));
		_portraits.add(createNewSpawn(29049, 172634, -87165, -4972, 57730, 0));

		_deamons.add(createNewSpawn(29050, 175833, -87165, -4972, 35048, 180));
		_deamons.add(createNewSpawn(29051, 175876, -88713, -4972, 28205, 180));
		_deamons.add(createNewSpawn(29051, 172608, -88702, -4972, 64817, 180));
		_deamons.add(createNewSpawn(29050, 172634, -87165, -4972, 57730, 180));

		_minions.add(createNewSpawn(29050, 174240, -87759, -5144, 48028, 180));
		_minions.add(createNewSpawn(29051, 174438, -88082, -5115, 48028, 180));
		_minions.add(createNewSpawn(29050, 174233, -88232, -5115, 48028, 180));
		_minions.add(createNewSpawn(29051, 174025, -88019, -5115, 48028, 180));

		switch (_state.getState())
		{
			case DEAD:
				// _state.setRespawnDate(Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000);
				_state.setState(StateEnum.INTERVAL);
			case SLEEP:
			case INTERVAL:
				setIntervalTask();
				break;
			case UNKNOWN:
			case ALIVE:
				_state.setState(StateEnum.NOTSPAWN);
				break;
		}
		_log.info("Frintezza: State is " + _state.getState());

	}

	@Override
	public void onEnter(L2Character cha)
	{
		if (!ENABLED)
			return;
		if (cha instanceof L2PcInstance && _state.getState() == StateEnum.NOTSPAWN && _startTask == null)
		{
			_startTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					start();
					_startTask = null;
				}
			}, 60000);
		}
	}

	public synchronized void onKill(L2Npc npc)
	{
		if (npc.getNpcId() == 29048 || npc.getNpcId() == 29049)
		{
			int index = _portraits.indexOf(npc.getSpawn());
			_deamons.get(index).stopRespawn();
		}
		else if (npc == _scarlet)
		{
			_scarlet = null;
			_state.setState(StateEnum.DEAD);
			finish();
			long interval = Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000;
			_state.setRespawnDate(interval);
			_state.update();
		}
		for (L2Spawn spawn : _portraits)
		{
			spawn.stopRespawn();

				spawn.getLastSpawn().deleteMe();

		}
		for (L2Spawn spawn : _deamons)
		{
			spawn.stopRespawn();
				spawn.getLastSpawn().deleteMe();
		}

		for (L2Spawn spawn : _minions)
		{
			spawn.stopRespawn();
				spawn.getLastSpawn().deleteMe();

		}
		_log.info("FrintezzaManager: State of Frintezza " + _state.getState());
	}

	public synchronized void respawnScarlet()
	{
		if (!ENABLED)
			return;

		if (!_isWeakScarlet)
			return;
		Location loc = _scarlet.getLoc();
		L2Character victim = _scarlet.getMostHated();
		_scarlet.deleteMe();
		_isWeakScarlet = false;
		_strongScarlet.setLocx(loc.getX());
		_strongScarlet.setLocy(loc.getY());
		_strongScarlet.setLocz(loc.getZ());
		_strongScarlet.setHeading(loc.getHeading());

		for (L2Spawn spawn : _minions)
		{
			spawn.doSpawn();
		}

		_scarlet = (L2FrintezzaBossInstance) _strongScarlet.doSpawn();

		for (L2PcInstance pc : getPlayersInside())
		{
			pc.sendPacket(new Earthquake(_scarlet.getX(), _scarlet.getY(), _scarlet.getZ(), 50, 6));
		}
		if (victim != null)
		{
			_scarlet.addDamageHate(victim, 1, 100);
		}
	}

	public void setIdle(L2Character target)
	{
		target.getAI().setIntention(CtrlIntention.IDLE);
		target.abortAttack();
		target.abortCast();
		// target.setIsImmobilized(true);
		target.disableAllSkills();
	}

	private void setIntervalTask()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				_state.setState(StateEnum.NOTSPAWN);
				_state.update();
				_log.info("FrintezzaManager: State of Frintezza " + _state.getState());
			}
		}, _state.getInterval());
	}

	@Override
	public void setRespawn()
	{

	}

	@Override
	public void setUnspawn()
	{

	}

	public void showSocialActionMovie(L2Npc target, int dist, int yaw, int pitch, int time, int duration, int socialAction)
	{
		if (target == null)
			return;
		for (L2PcInstance pc : getPlayersInside())
		{
			setIdle(pc);
			pc.setTarget(null);
			if (pc.getPlanDistanceSq(target) <= 6502500)
			{
				pc.enterMovieMode();
				pc.specialCamera(target, dist, yaw, pitch, time, duration);
			}
			else
			{
				pc.leaveMovieMode();
			}
		}
		// do social.
		if (socialAction > 0 && socialAction < 5)
		{
			target.broadcastPacket(new SocialAction(target, socialAction));
		}
	}

	public synchronized void skipMusic()
	{
		_skipMusic = true;
	}

	public void start()
	{
		if (!ENABLED)
			return;

		_frintezza = (L2FrintezzaBossInstance) _bossSpawn.doSpawn();
		_frintezza.disableAllSkills();
		_frintezza.setIsImmobilized(true);
		_frintezza.setIsInvul(true);
		_state.setState(StateEnum.ALIVE);
		_canPlayMusic = true;
		_skipMusic = false;
		_isWeakScarlet = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ActionTask(2), 1000);
	}

	public void stopActivity()
	{
		if (!ENABLED)
			return;
		DoorTable.getInstance().getDoor(25150046).openMe();
		clearLair();
		finish();
		if (_cubeSpawn.getLastSpawn() != null)
		{
			_cubeSpawn.getLastSpawn().deleteMe();
		}
		if (_state.getState() != StateEnum.DEAD && _state.getState() != StateEnum.INTERVAL)
		{
			_state.setState(StateEnum.NOTSPAWN);
		}
		if (_state.getState() == StateEnum.DEAD)
		{
			_state.setState(StateEnum.INTERVAL);
		}
		_state.update();
		_log.info("FrintezzaManager: State of Frintezza is " + _state.getState());
	}
}
package com.dream.game.manager.grandbosses;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dream.annotations.L2Properties;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class ZakenManager extends BossLair
{
	public class IntervalEnd implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("ZakenManager : State of Zaken is " + _state.getState() + ".");
			spawnZaken();
		}
	}

	public class RunAway implements Runnable
	{
		@Override
		public void run()
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					if (_zaken != null && _zaken.getCurrentMp() > 200)
					{
						if (!_zaken.isDead())
						{
							_zaken.doCast(SkillTable.getInstance().getInfo(4222, 1));
						}
						else
						{
							_runAway.cancel(false);
						}
					}
					else
					{
						_runAway.cancel(false);
					}
				}
			}, Rnd.get(5) * 60000);
		}
	}

	protected final static Logger _log = Logger.getLogger(ZakenManager.class.getName());

	public static long MIN_RESPAWN;
	public static long MAX_RESPAWN;
	public static int MAX_LVL;
	public static boolean isLoaded = false;
	public static List<Integer> OPEN_DOOR_TIME_HOUR = new ArrayList<>();
	public static int DOOR_OPEN_TIME;
	public static boolean CLOSED_ZAKEN_DOORS;
	private static ZakenManager _instance;

	public static ZakenManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ZakenManager();
		}
		return _instance;
	}

	public GrandBossState _state;

	public L2GrandBossInstance _zaken;

	private boolean ENABLED;

	public Future<?> _runAway;

	public ZakenManager()
	{
		super();
		_state = new GrandBossState(29022);
		try
		{
			L2Properties p = new L2Properties("./config/main/bosses.properties");
			ENABLED = Boolean.parseBoolean(p.getProperty("ZakenEnabled", "true"));
			if (!ENABLED)
				return;

			CLOSED_ZAKEN_DOORS = Boolean.parseBoolean(p.getProperty("ZakenDoorClosedDefault", "true"));
			MIN_RESPAWN = Integer.parseInt(p.getProperty("ZakenMinRespawn", "1440"));
			MAX_RESPAWN = Integer.parseInt(p.getProperty("ZakenMaxRespawn", "2880"));
			DOOR_OPEN_TIME = Integer.parseInt(p.getProperty("ZakenDoorOpenTime", "5"));
			MAX_LVL = Integer.parseInt(p.getProperty("ZakenMaxLevelInZone", "69"));

			for (String s : p.getProperty("ZakenDoorOpenHour", "0").split(" "))
			{
				try
				{
					OPEN_DOOR_TIME_HOUR.add(Integer.parseInt(s));
				}
				catch (NumberFormatException e)
				{
				}
			}
			isLoaded = true;
		}
		catch (Exception e)
		{
			MIN_RESPAWN = 1440;
			MAX_RESPAWN = 2880;
		}
	}

	@Override
	public void init()
	{
		if (!ENABLED)
			return;
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(29022);
		_bossSpawn = new L2Spawn(template);
		_bossSpawn.setLocx(55312);
		_bossSpawn.setLocy(219168);
		_bossSpawn.setLocz(-3223);

		if (ZakenManager.CLOSED_ZAKEN_DOORS)
		{
			DoorTable.getInstance().getDoor(21240006).closeMe();
		}
		if (!ZakenManager.CLOSED_ZAKEN_DOORS)
		{
			DoorTable.getInstance().getDoor(21240006).openMe();
		}


		switch (_state.getState())
		{
			case DEAD:
				_state.setRespawnDate(Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000);
				_state.setState(StateEnum.INTERVAL);
			case SLEEP:
			case INTERVAL:
				ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
				break;
			case UNKNOWN:
				_state.setState(StateEnum.ALIVE);
			case ALIVE:
			case NOTSPAWN:
				_state.setState(StateEnum.NOTSPAWN);
				spawnZaken();
				break;

		}
		_log.info("Zaken: State is " + _state.getState() + ".");

	}

	@Override
	public void onEnter(L2Character cha)
	{
		if (cha.getActingPlayer() != null)
			if (MAX_LVL > 0 && cha.getLevel() > MAX_LVL && !cha.getActingPlayer().isGM())
			{
				cha.teleToLocation(TeleportWhereType.Town);
			}

	}

	@Override
	public void setRespawn()
	{
	}

	@Override
	public void setUnspawn()
	{
		_state.setState(StateEnum.INTERVAL);
		long interval = Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000;
		_state.setRespawnDate(interval);
		_state.update();
		_zaken = null;
		_log.info("ZakenManager : State of Zaken is " + _state.getState() + ".");
		ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	public void spawnZaken()
	{
		_zaken = (L2GrandBossInstance) _bossSpawn.doSpawn();
		_zaken._lair = this;
		_zaken.getStatus().setCurrentHpMp(_zaken.getMaxHp(), _zaken.getMaxMp());
		_state.setState(GrandBossState.StateEnum.ALIVE);
		_state.update();
		_runAway = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RunAway(), 20 * 60000, 20 * 60000);

	}
}
package com.dream.game.manager.grandbosses;

import com.dream.annotations.L2Properties;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.model.quest.pack.ai.Core;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class CoreManager extends BossLair
{

	public static long MIN_RESPAWN;

	public static long MAX_RESPAWN;
	public static int MAX_GUARDS;
	private static CoreManager _instance;

	public static CoreManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new CoreManager();
		}
		return _instance;
	}

	private boolean _loaded;

	private boolean ENABLED;

	private CoreManager()
	{
		_state = new GrandBossState(29006);
		_state.load();
		try
		{
			L2Properties p = new L2Properties("./config/main/bosses.properties");
			ENABLED = Boolean.parseBoolean(p.getProperty("CoreEnabled", "true"));
			if (!ENABLED)
				return;
			MIN_RESPAWN = Integer.parseInt(p.getProperty("CoreMinRespawn", "1440"));
			MAX_RESPAWN = Integer.parseInt(p.getProperty("CoreMaxRespawn", "2880"));
			MAX_GUARDS = Integer.parseInt(p.getProperty("CoreNumberOfGuards", "4"));

			if (MAX_GUARDS < 2)
			{
				MAX_GUARDS = 2;
			}
			_loaded = true;
		}
		catch (Exception e)
		{
			_log.error("CoreManager: Error while reading config", e);
			_loaded = false;
			return;
		}

	}

	public void doSpawn()
	{
		L2Boss core = (L2Boss) _bossSpawn.doSpawn();
		core._lair = this;
		_state.setState(StateEnum.ALIVE);
		_state.update();
	}

	@Override
	public void init()
	{
		if (!ENABLED)
			return;
		if (!_loaded)
			return;
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(29006);
		_bossSpawn = new L2Spawn(template);
		_bossSpawn.setLocx(17726);
		_bossSpawn.setLocy(108915);
		_bossSpawn.setLocz(-6480);
		new Core();
		switch (_state.getState())
		{
			case DEAD:
				_state.setRespawnDate(Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000);
				_state.setState(StateEnum.INTERVAL);
			case SLEEP:
			case INTERVAL:
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						doSpawn();
					}
				}, _state.getInterval());
				break;
			case UNKNOWN:
				_state.setState(StateEnum.ALIVE);
			case ALIVE:
			case NOTSPAWN:
				doSpawn();
				break;
		}
		_log.info("Core: State is " + _state.getState() + ".");
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
		_log.info("CoreManager: State of Core is " + _state.getState());
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				doSpawn();
			}
		}, _state.getInterval());
	}

}

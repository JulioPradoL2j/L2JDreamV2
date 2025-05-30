package com.dream.game.manager.grandbosses;

import com.dream.annotations.L2Properties;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.model.quest.pack.ai.QueenAnt;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class QueenAntManager extends BossLair
{

	public static long MIN_RESPAWN;

	public static long MAX_RESPAWN;
	public static int SAFE_LEVEL;
	public static int MAX_NURSES;
	public static int MAX_GUARDS;
	public static boolean AK_ENABLE;
	private static QueenAntManager _instance;

	public static QueenAntManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new QueenAntManager();
		}
		return _instance;
	}

	private boolean _loaded = false;

	private QueenAntManager()
	{
		super();
		try
		{
			L2Properties p = new L2Properties("./config/main/bosses.properties");
			MIN_RESPAWN = Integer.parseInt(p.getProperty("QueenAntMinRespawn", "1440"));
			MAX_RESPAWN = Integer.parseInt(p.getProperty("QueenAntMaxRespawn", "2880"));
			SAFE_LEVEL = Integer.parseInt(p.getProperty("QueenAntMaxSafeLevel", "48"));
			MAX_NURSES = Integer.parseInt(p.getProperty("QueenAntNumberOfNurses", "6"));
			MAX_GUARDS = Integer.parseInt(p.getProperty("QueenAntNumberOfGuards", "8"));
			AK_ENABLE = Boolean.parseBoolean(p.getProperty("QueenAntEnabled", "true"));

			if (AK_ENABLE)
			{
				_loaded = true;
				new QueenAnt();
			}

		}
		catch (Exception e)
		{
			_log.error("QuuenAntManager: Error while reading config", e);
			_loaded = false;
			return;
		}

	}

	public void doSpawn()
	{
		L2GrandBossInstance ak = (L2GrandBossInstance) _bossSpawn.doSpawn();
		ak._lair = this;
		_state.setState(StateEnum.ALIVE);
		_state.update();
	}

	@Override
	public void init()
	{
		if (!_loaded)
			return;
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(29001);
		_bossSpawn = new L2Spawn(template);
		_bossSpawn.setLocx(-21610);
		_bossSpawn.setLocy(181594);
		_bossSpawn.setLocz(-5734);
		_bossSpawn.stopRespawn();
		_state = new GrandBossState(29001);
		switch (_state.getState())
		{
			case DEAD:
				_state.setRespawnDate(Rnd.get(MIN_RESPAWN, MAX_RESPAWN) * 60000);
				_state.setState(StateEnum.INTERVAL);
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
		_log.info("Queen Ant: State is " + _state.getState() + ".");

	}

	@Override
	public void onExit(L2Character cha)
	{
		if (cha instanceof L2Attackable)
		{
			int npcId = ((L2Attackable) cha).getNpcId();
			if (npcId == QueenAnt.GUARD || npcId == QueenAnt.NURSE || npcId == QueenAnt.ROYAL || npcId == QueenAnt.QUEEN)
			{
				cha.teleToLocation(_bossSpawn.getLocx(), _bossSpawn.getLocy(), _bossSpawn.getLocz());
			}
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
		_log.info("QueenAntManager: State of Queen Ant is " + _state.getState());
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

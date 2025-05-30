package com.dream.game.manager.grandbosses;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.Entity;
import com.dream.game.model.entity.GrandBossState;
import com.dream.game.model.entity.GrandBossState.StateEnum;
import com.dream.game.model.quest.QuestState;
import com.dream.game.network.ThreadPoolManager;

public abstract class BossLair extends Entity
{
	protected final static Logger _log = Logger.getLogger(BossLair.class.getName());

	private static List<BossLair> _lairs = new ArrayList<>();

	public static List<BossLair> getLairs()
	{
		return _lairs;
	}

	protected GrandBossState _state;

	protected String _questName;

	public L2Spawn _bossSpawn;

	public BossLair()
	{
		_lairs.add(this);
	}

	public void checkAnnihilated()
	{
		if (isPlayersAnnihilated())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					setUnspawn();
				}
			}, 5000);
		}
	}

	public void clearLair()
	{
		for (L2PcInstance pc : getPlayersInside())
		{
			if (_questName != null)
			{
				QuestState qs = pc.getQuestState(_questName);
				if (qs != null)
				{
					qs.exitQuest(true);
				}
			}
			pc.teleToLocation(TeleportWhereType.Town);
		}
	}

	public String getEpicName()
	{
		return getClass().getSimpleName().replace("Manager", "");
	}

	public String getManagerName()
	{
		return getClass().getSimpleName();
	}

	public GrandBossState.StateEnum getState()
	{
		if (_state == null)
			return StateEnum.UNKNOWN;
		return _state.getState();
	}

	public abstract void init();

	public boolean isEnableEnterToLair()
	{
		return _state.getState() == GrandBossState.StateEnum.NOTSPAWN;
	}

	public synchronized boolean isPlayersAnnihilated()
	{
		for (L2PcInstance pc : getPlayersInside())
			if (!pc.isDead())
				return false;
		return true;
	}

	public void onEnter(L2Character cha)
	{

	}

	public void onExit(L2Character cha)
	{

	}

	protected void printState()
	{
		String info = getManagerName() + ": State of " + getEpicName() + " is " + _state.getState();
		if (_state.getState() == StateEnum.INTERVAL && _state.getRespawnDate() > System.currentTimeMillis())
		{
			info += ", respawn: " + new Date(_state.getRespawnDate());
		}
		info += ".";
		_log.info(info);
	}

	public abstract void setRespawn();

	public abstract void setUnspawn();
}
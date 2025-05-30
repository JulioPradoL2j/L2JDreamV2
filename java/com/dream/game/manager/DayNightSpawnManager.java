package com.dream.game.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.game.GameTimeController;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class DayNightSpawnManager
{

	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager _instance = new DayNightSpawnManager();
	}

	private final static Logger _log = Logger.getLogger(DayNightSpawnManager.class.getName());

	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static void handleHellmans(L2Boss boss, int mode)
	{
		switch (mode)
		{
			case 0:
				boss.deleteMe();
				break;
			case 1:
				boss.spawnMe();
				break;
		}
	}

	private static void ShadowSenseMsg(int mode)
	{
		final L2Skill skill = SkillTable.getInstance().getInfo(294, 1);
		if (skill == null)
			return;

		final SystemMessageId msg = mode == 1 ? SystemMessageId.S1_NIGHT_EFFECT_APPLIES : SystemMessageId.S1_NIGHT_EFFECT_DISAPPEARS;
		final Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
		for (L2PcInstance onlinePlayer : pls)
			if (onlinePlayer.getRace().ordinal() == 2 && onlinePlayer.getSkillLevel(294) > 0)
			{
				onlinePlayer.sendPacket(SystemMessage.getSystemMessage(msg).addSkillName(294));
			}
	}

	private static void spawnCreatures(List<L2Spawn> unSpawnCreatures, List<L2Spawn> spawnCreatures, String UnspawnLogInfo, String SpawnLogInfo)
	{
		try
		{
			if (!unSpawnCreatures.isEmpty())
			{
				int i = 0;
				for (L2Spawn spawn : unSpawnCreatures)
				{
					if (spawn == null)
					{
						continue;
					}

					spawn.stopRespawn();
					L2Npc last = spawn.getLastSpawn();
					if (last != null)
					{
						last.deleteMe();
						i++;
					}
				}
				_log.info("DayNightSpawnManager: Removed " + i + " " + UnspawnLogInfo + " creatures");
			}

			int i = 0;
			for (L2Spawn spawnDat : spawnCreatures)
			{
				if (spawnDat == null)
				{
					continue;
				}
				spawnDat.startRespawn();
				spawnDat.doSpawn();
				i++;
			}

			_log.info("DayNightSpawnManager: Spawned " + i + " " + SpawnLogInfo + " creatures");
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
	}

	private final List<L2Spawn> _dayCreatures;

	private final List<L2Spawn> _nightCreatures;

	private final Map<L2Spawn, L2RaidBossInstance> _bosses;

	protected DayNightSpawnManager()
	{
		_dayCreatures = new ArrayList<>();
		_nightCreatures = new ArrayList<>();
		_bosses = new HashMap<>();

		_log.info("Day Night Spawn's: Handler initialized.");
	}

	public void addDayCreature(L2Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}

	public void addNightCreature(L2Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}

	private void changeMode(int mode)
	{
		if (_nightCreatures.size() == 0 && _dayCreatures.size() == 0)
			return;

		switch (mode)
		{
			case 0:
				spawnDayCreatures();
				specialNightBoss(0);
				ShadowSenseMsg(0);
				break;
			case 1:
				spawnNightCreatures();
				specialNightBoss(1);
				ShadowSenseMsg(1);
				break;
			default:
				_log.warn("DayNightSpawnManager: Wrong mode sent");
				break;
		}
	}

	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}

	public L2Boss handleBoss(L2Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
			return _bosses.get(spawnDat);

		if (GameTimeController.isNowNight())
		{
			L2RaidBossInstance raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			return raidboss;
		}
		_bosses.put(spawnDat, null);
		return null;
	}

	public void notifyChangeMode()
	{
		try
		{
			if (GameTimeController.isNowNight())
			{
				changeMode(1);
			}
			else
			{
				changeMode(0);
			}
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
	}

	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}

	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}

	private void specialNightBoss(int mode)
	{
		try
		{
			for (L2Spawn spawn : _bosses.keySet())
			{
				L2RaidBossInstance boss = _bosses.get(spawn);

				if (boss == null)
				{
					if (mode == 1)
					{
						boss = (L2RaidBossInstance) spawn.doSpawn();
						RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
						_bosses.remove(spawn);
						_bosses.put(spawn, boss);
					}
					continue;
				}

				if (boss.getNpcId() == 25328 && boss.getRaidStatus().equals(BossSpawnManager.StatusEnum.ALIVE))
				{
					handleHellmans(boss, mode);
				}
				return;
			}
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
	}

	public DayNightSpawnManager trim()
	{
		((ArrayList<?>) _nightCreatures).trimToSize();
		((ArrayList<?>) _dayCreatures).trimToSize();
		return this;
	}
}
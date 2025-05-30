package com.dream.game.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dream.Config;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.actor.instance.L2MinionInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;
import com.dream.util.SingletonList;
import com.dream.util.SingletonMap;

public class MinionList
{
	private final List<L2MinionInstance> minionReferences = new SingletonList<>();
	private final Map<Long, Integer> _respawnTasks = new SingletonMap<Long, Integer>().setShared();
	private final L2MonsterInstance master;

	public MinionList(L2MonsterInstance pMaster)
	{
		master = pMaster;
	}

	public void addSpawnedMinion(L2MinionInstance minion)
	{
		minionReferences.add(minion);
	}

	public void clearRespawnList()
	{
		_respawnTasks.clear();
	}

	public int countSpawnedMinions()
	{
		return minionReferences.size();
	}

	private int countSpawnedMinionsById(int minionId)
	{
		int count = 0;
		for (L2MinionInstance minion : getSpawnedMinions())
			if (minion.getNpcId() == minionId)
			{
				count++;
			}

		return count;
	}

	public List<L2MinionInstance> getSpawnedMinions()
	{
		return minionReferences;
	}

	public boolean hasMinions()
	{
		return !getSpawnedMinions().isEmpty();
	}

	public int lazyCountSpawnedMinionsGroups()
	{
		Set<Integer> seenGroups = new HashSet<>();
		for (L2MinionInstance minion : getSpawnedMinions())
		{
			seenGroups.add(minion.getNpcId());
		}

		return seenGroups.size();
	}

	public void maintainMinions()
	{
		if (master == null || master.isAlikeDead())
			return;

		Long current = System.currentTimeMillis();
		if (_respawnTasks != null)
		{
			for (long deathTime : _respawnTasks.keySet())
			{
				double delay = Config.RAID_MINION_RESPAWN_TIMER;
				if (current - deathTime > delay)
				{
					spawnSingleMinion(_respawnTasks.get(deathTime));
					_respawnTasks.remove(deathTime);
				}
			}
		}
	}

	public void moveMinionToRespawnList(L2MinionInstance minion)
	{
		Long current = System.currentTimeMillis();
		synchronized (minionReferences)
		{
			minionReferences.remove(minion);
			if (_respawnTasks.get(current) == null)
			{
				_respawnTasks.put(current, minion.getNpcId());
			}
			else
			{
				for (int i = 1; i < 30; i++)
					if (_respawnTasks.get(current + i) == null)
					{
						_respawnTasks.put(current + i, minion.getNpcId());
						break;
					}
			}
		}
	}

	public void spawnMinions()
	{
		if (master == null || master.isAlikeDead())
			return;

		List<L2MinionData> minions = master.getTemplate().getMinionData();
		if (minions == null)
			return;

		synchronized (minionReferences)
		{
			int minionCount, minionId, minionsToSpawn;
			for (L2MinionData minion : minions)
			{
				minionCount = minion.getAmount();
				minionId = minion.getMinionId();

				minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);

				for (int i = 0; i < minionsToSpawn; i++)
				{
					spawnSingleMinion(minionId);
				}
			}
		}
	}

	private void spawnSingleMinion(int minionid)
	{
		L2NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionid);

		L2MinionInstance monster = new L2MinionInstance(IdFactory.getInstance().getNextId(), minionTemplate);

		if (Config.CHAMPION_ENABLE && Config.CHAMPION_MINIONS && master.isChampion())
		{
			monster.setChampion(true);
		}

		monster.getStatus().setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
		monster.setHeading(master.getHeading());

		monster.setLeader(master);

		int spawnConstant;
		int randSpawnLim = 170;
		int randPlusMin = 1;
		spawnConstant = Rnd.nextInt(randSpawnLim);

		randPlusMin = Rnd.nextInt(2);
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		int newX = master.getX() + Math.round(spawnConstant);
		spawnConstant = Rnd.nextInt(randSpawnLim);

		randPlusMin = Rnd.nextInt(2);
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		int newY = master.getY() + Math.round(spawnConstant);

		monster.spawnMe(newX, newY, master.getZ());
	}
}
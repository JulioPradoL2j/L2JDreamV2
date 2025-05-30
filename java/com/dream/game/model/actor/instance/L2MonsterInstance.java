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
package com.dream.game.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.dream.game.model.MinionList;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.knownlist.MonsterKnownList;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2MonsterInstance extends L2Attackable
{
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;

	public final MinionList _minionList;

	protected ScheduledFuture<?> _minionMaintainTask = null;

	private boolean _isKillable = true;
	private boolean _questDropable = true;

	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();
		_minionList = new MinionList(this);
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker.isAutoAttackable(this))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	public void callMinions(boolean turnBackToStartLocation)
	{
		if (_minionList.hasMinions())
		{
			for (L2MinionInstance minion : _minionList.getSpawnedMinions())
				if (!isInsideRadius(minion, 200, false, false))
				{
					int masterX = getX();
					int masterY = getY();
					int masterZ = getZ();

					int minionX = masterX + Rnd.nextInt(401) - 200;
					int minionY = masterY + Rnd.nextInt(401) - 200;
					int minionZ = masterZ;
					while (minionX != masterX + 30 && minionX != masterX - 30 || minionY != masterY + 30 && minionY != masterY - 30)
					{
						minionX = masterX + Rnd.nextInt(401) - 200;
						minionY = masterY + Rnd.nextInt(401) - 200;
					}

					if (minion != null && !minion.isInCombat() && !minion.isDead() && !minion.isMovementDisabled())
						if (turnBackToStartLocation && !minion.isDead())
						{
							minion.teleToLocation(minionX, minionY, minionZ);
						}
						else
						{
							minion.moveToLocation(minionX, minionY, minionZ, 0);
						}
				}
		}
	}

	public void callMinionsToAssist(L2Character attacker)
	{
		if (_minionList.hasMinions())
		{
			List<L2MinionInstance> spawnedMinions = _minionList.getSpawnedMinions();
			if (spawnedMinions != null && !spawnedMinions.isEmpty())
			{
				Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
				if (itr != null)
				{
					L2MinionInstance minion;
					while (itr.hasNext())
					{
						minion = itr.next();
						if (minion != null && !minion.isDead() && !minion.isInCombat())
							if (isRaidBoss() && !isRaidMinion())
							{
								minion.addDamage(attacker, 100, null);
							}
							else
							{
								minion.addDamage(attacker, 1, null);
							}
					}
				}
			}
		}
	}

	@Override
	public void deleteMe()
	{
		if (hasMinions())
		{
			if (_minionMaintainTask != null)
			{
				_minionMaintainTask.cancel(true);
			}

			deleteSpawnedMinions();
		}

		super.deleteMe();
	}

	public void deleteSpawnedMinions()
	{
		for (L2MinionInstance minion : getSpawnedMinions())
		{
			if (minion == null)
			{
				continue;
			}

			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
			getSpawnedMinions().remove(minion);
		}

		_minionList.clearRespawnList();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!_isKillable)
			return false;

		if (!super.doDie(killer))
			return false;

		if (_minionMaintainTask != null)
		{
			_minionMaintainTask.cancel(true);
		}

		if (isRaidBoss())
		{
			deleteSpawnedMinions();
		}
		return true;
	}

	@Override
	public void firstSpawn()
	{
		super.firstSpawn();

		if (getTemplate().getMinionData() != null)
			if (getSpawnedMinions() != null)
			{
				for (L2MinionInstance minion : getSpawnedMinions())
				{
					if (minion == null)
					{
						continue;
					}

					getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				_minionList.clearRespawnList();

				manageMinions();
			}
	}

	public boolean getKillable()
	{
		return _isKillable;
	}

	@Override
	public final MonsterKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new MonsterKnownList(this);
		}

		return (MonsterKnownList) _knownList;
	}

	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}

	public boolean getQuestDropable()
	{
		return _questDropable;
	}

	public List<L2MinionInstance> getSpawnedMinions()
	{
		return _minionList.getSpawnedMinions();
	}

	public int getTotalSpawnedMinionsGroups()
	{
		return _minionList.lazyCountSpawnedMinionsGroups();
	}

	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList.countSpawnedMinions();
	}

	public boolean hasMinions()
	{
		return _minionList.hasMinions();
	}

	@Override
	public boolean isAggressive()
	{
		return getTemplate().getAggroRange() > 0;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;

		return true;
	}

	protected void manageMinions()
	{
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				_minionList.spawnMinions();
			}
		}, getMaintenanceInterval());
	}

	public void notifyMinionDied(L2MinionInstance minion)
	{
		_minionList.moveMinionToRespawnList(minion);
	}

	public void notifyMinionSpawned(L2MinionInstance minion)
	{
		_minionList.addSpawnedMinion(minion);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		if (getTemplate().getMinionData() != null)
			if (getSpawnedMinions() != null)
			{
				for (L2MinionInstance minion : getSpawnedMinions())
				{
					if (minion == null)
					{
						continue;
					}

					getSpawnedMinions().remove(minion);
					minion.deleteMe();
				}
				_minionList.clearRespawnList();

				manageMinions();
			}
	}

	public void setKillable(boolean b)
	{
		_isKillable = b;
	}

	public void setQuestDropable(boolean b)
	{
		_questDropable = b;
	}
}
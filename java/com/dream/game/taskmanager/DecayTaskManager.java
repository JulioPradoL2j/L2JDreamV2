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
package com.dream.game.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dream.game.model.L2Boss;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PetInstance;

public final class DecayTaskManager extends AbstractPeriodicTaskManager
{
	public static final int RAID_BOSS_DECAY_TIME = 30000;

	public static final int ATTACKABLE_DECAY_TIME = 8500;

	private static DecayTaskManager _instance;

	private static int getDecayTime0(L2Character actor)
	{
		if (actor instanceof L2MonsterInstance)
		{
			switch (((L2MonsterInstance) actor).getNpcId())
			{
				case 29019:
				case 29066:
				case 29067:
				case 29068:
					return 12000;
				case 29028:
					return 18000;
				case 29014:
				case 29001:
					return 150000;
				case 29045:
					return 9500;
				case 29046:
					return 2000;
				case 29047:
					return 7500;
			}
		}

		if (actor instanceof L2Boss)
			return RAID_BOSS_DECAY_TIME;

		if (actor instanceof L2PetInstance)
			return 86400000;

		return ATTACKABLE_DECAY_TIME;
	}

	public static DecayTaskManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new DecayTaskManager();
		}

		return _instance;
	}

	private final Map<L2Character, Long> _decayTasks = new ConcurrentHashMap<>();

	private DecayTaskManager()
	{
		super(1000);
	}

	public void addDecayTask(L2Character actor)
	{
		synchronized (_decayTasks)
		{
			long decay = System.currentTimeMillis() + getDecayTime0(actor);
			_decayTasks.put(actor, decay);
		}
	}

	public void cancelDecayTask(L2Character actor)
	{
		synchronized (_decayTasks)
		{
			_decayTasks.remove(actor);
		}
	}

	public double getRemainingDecayTime(L2Character actor)
	{
		readLock();
		try
		{
			double remaining = _decayTasks.get(actor) - System.currentTimeMillis();

			return remaining / getDecayTime0(actor);
		}
		finally
		{
			readUnlock();
		}
	}

	public String getStats()
	{
		readLock();
		try
		{
			final StringBuilder sb = new StringBuilder();
			sb.append("============= DecayTask Manager Report ============").append("\r\n");
			sb.append("Tasks count: ").append(_decayTasks.size()).append("\r\n");
			sb.append("Tasks dump:").append("\r\n");

			for (L2Character actor : _decayTasks.keySet())
			{
				sb.append("(").append(_decayTasks.get(actor) - System.currentTimeMillis()).append(") - ");
				sb.append(actor.getClass().getSimpleName()).append("/").append(actor.getName()).append("\r\n");
			}

			return sb.toString();
		}
		finally
		{
			readUnlock();
		}
	}

	public boolean hasDecayTask(L2Character actor)
	{
		readLock();
		try
		{
			return _decayTasks.containsKey(actor);
		}
		finally
		{
			readUnlock();
		}
	}

	@Override
	public void run()
	{
		synchronized (_decayTasks)
		{
			long now = System.currentTimeMillis();
			for (Map.Entry<L2Character, Long> entry : _decayTasks.entrySet())
				if (now > entry.getValue())
				{
					final L2Character actor = entry.getKey();
					actor.onDecay();

					_decayTasks.remove(actor);
				}
		}
	}
}

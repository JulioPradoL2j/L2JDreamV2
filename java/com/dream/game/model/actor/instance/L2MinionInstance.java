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

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.templates.chars.L2NpcTemplate;

public final class L2MinionInstance extends L2MonsterInstance
{
	private L2MonsterInstance _master;

	public L2MinionInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean doDie(L2Character killer)
	{

		if (!super.doDie(killer))
			return false;
		if (_master != null)
		{
			_master.notifyMinionDied(this);
		}
		return true;
	}

	public L2MonsterInstance getLeader()
	{
		return _master;
	}

	@Override
	public void onSpawn()
	{
		_master.notifyMinionSpawned(this);
		if (getLeader().isRaid())
		{
			setIsRaidMinion(true);
		}

		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if (region != null && !region.isActive())
		{
			getAI().stopAITask();
		}
		super.onSpawn();
	}

	public void setLeader(L2MonsterInstance leader)
	{
		_master = leader;
	}
}
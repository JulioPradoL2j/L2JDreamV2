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
package com.dream.game.skills.conditions;

import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.skills.Env;

public class ConditionTargetNpcId extends Condition
{

	private final String[] _npcIds;

	public ConditionTargetNpcId(String[] ids)
	{
		_npcIds = ids;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if (env.target == null)
			return false;
		boolean mt;
		for (String _npcId : _npcIds)
		{
			mt = env.target instanceof L2Attackable && ((L2Attackable) env.target).getNpcId() == Integer.valueOf(_npcId) || env.target instanceof L2Npc && ((L2Npc) env.target).getNpcId() == Integer.valueOf(_npcId);
			if (mt)
				return true;
		}
		return false;
	}
}

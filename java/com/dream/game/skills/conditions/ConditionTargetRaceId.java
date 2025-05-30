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

import java.util.Arrays;
import java.util.List;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.skills.Env;
import com.dream.util.ArrayUtils;

final class ConditionTargetRaceId extends Condition
{
	private final int[] _raceIds;

	public ConditionTargetRaceId(List<Integer> raceId)
	{
		_raceIds = ArrayUtils.toPrimitive(raceId.toArray(new Integer[raceId.size()]), 0);

		Arrays.sort(_raceIds);
	}

	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.target instanceof L2Npc))
			return false;

		return Arrays.binarySearch(_raceIds, ((L2Npc) env.target).getTemplate().getRace().ordinal()) >= 0;
	}
}

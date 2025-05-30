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

import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.knownlist.FriendlyMobKnownList;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FriendlyMobInstance extends L2Attackable
{
	public L2FriendlyMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();
	}

	@Override
	public final FriendlyMobKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new FriendlyMobKnownList(this);
		}

		return (FriendlyMobKnownList) _knownList;
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2PcInstance)
			return ((L2PcInstance) attacker).getKarma() > 0;
		return false;
	}
}
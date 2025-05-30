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
package com.dream.game.model.actor.knownlist;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class AttackableKnownList extends NpcKnownList
{
	public AttackableKnownList(L2Attackable activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (getActiveChar().getAggroListRP() != null)
			if (getActiveChar().getAggroListRP().get(object) != null)
				return 3000;
		return Math.min(2200, 2 * getDistanceToWatchObject(object));
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2NpcInstance || !(object instanceof L2Character))
			return 0;

		if (object instanceof L2Playable)
			return 1500;

		if (getActiveChar().getAggroRange() > getActiveChar().getFactionRange())
			return getActiveChar().getAggroRange();

		if (getActiveChar().getFactionRange() > 200)
			return getActiveChar().getFactionRange();

		return 200;
	}

	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;

		if (object instanceof L2Character)
		{
			getActiveChar().getAggroList().remove(object);
		}

		if (getActiveChar().hasAI() && getKnownType(L2PcInstance.class).isEmpty())
		{
			getActiveChar().getAI().setIntention(CtrlIntention.IDLE, null);
		}

		return true;
	}

}
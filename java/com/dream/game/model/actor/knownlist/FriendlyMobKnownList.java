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

import com.dream.game.ai.CtrlEvent;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2FriendlyMobInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class FriendlyMobKnownList extends AttackableKnownList
{
	public FriendlyMobKnownList(L2FriendlyMobInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if (getKnownObjects().containsKey(object.getObjectId()))
			return true;

		if (!super.addKnownObject(object, dropper))
			return false;

		if (object instanceof L2PcInstance && getActiveChar().getAI().getIntention() == CtrlIntention.IDLE)
		{
			getActiveChar().getAI().setIntention(CtrlIntention.ACTIVE, null);
		}

		return true;
	}

	@Override
	public final L2FriendlyMobInstance getActiveChar()
	{
		return (L2FriendlyMobInstance) _activeChar;
	}

	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;

		if (!(object instanceof L2Character))
			return true;

		if (getActiveChar().hasAI())
		{
			L2Character temp = (L2Character) object;
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
			if (getActiveChar().getTarget() == temp)
			{
				getActiveChar().setTarget(null);
			}
		}

		if (getActiveChar().isVisible() && getKnownPlayers().isEmpty())
		{
			getActiveChar().clearAggroList();
			if (getActiveChar().hasAI())
			{
				getActiveChar().getAI().setIntention(CtrlIntention.IDLE, null);
			}
		}

		return true;
	}
}
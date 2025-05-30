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

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2GuardInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public class GuardKnownList extends AttackableKnownList
{
	private final static Logger _log = Logger.getLogger(GuardKnownList.class.getName());

	public GuardKnownList(L2GuardInstance activeChar)
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

		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;

			if (player.getKarma() > 0)
			{
				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug(getActiveChar().getObjectId() + ": PK " + player.getObjectId() + " entered scan range");
				}

				if (getActiveChar().getAI().getIntention() == CtrlIntention.IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.ACTIVE, null);
				}
			}
		}
		else if (Config.ALLOW_GUARDS && getActiveChar().isInActiveRegion() && object instanceof L2MonsterInstance)
		{
			L2MonsterInstance mob = (L2MonsterInstance) object;

			if (mob.isAggressive())
			{
				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.debug(getActiveChar().getObjectId() + ": Aggressive mob " + mob.getObjectId() + " entered scan range");
				}

				if (getActiveChar().getAI().getIntention() == CtrlIntention.IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.ACTIVE, null);
				}
			}
		}

		return true;
	}

	@Override
	public final L2GuardInstance getActiveChar()
	{
		return (L2GuardInstance) _activeChar;
	}

	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;

		if (getActiveChar().noTarget())
		{
			L2CharacterAI ai = getActiveChar().getAI();
			if (ai != null)
			{
				ai.setIntention(CtrlIntention.IDLE, null);
			}
		}

		return true;
	}
}
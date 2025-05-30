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
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeGuardInstance;
import com.dream.game.model.entity.siege.Castle;

public class SiegeGuardKnownList extends AttackableKnownList
{
	public SiegeGuardKnownList(L2SiegeGuardInstance activeChar)
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

		Castle castle = getActiveChar().getCastle();
		if (castle != null && castle.getSiege().getIsInProgress())
		{
			L2PcInstance player = null;
			if (object instanceof L2PcInstance)
			{
				player = (L2PcInstance) object;
			}
			else if (object instanceof L2Summon)
			{
				player = ((L2Summon) object).getOwner();
			}

			if (player != null && (player.getClan() == null || castle.getSiege().getAttackerClan(player.getClan()) != null))
				if (getActiveChar().getAI().getIntention() == CtrlIntention.IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.ACTIVE, null);
				}
		}
		else if (DevastatedCastleSiege.getInstance().getIsInProgress())
		{
			L2PcInstance player = null;
			if (object instanceof L2PcInstance)
			{
				player = (L2PcInstance) object;
			}
			else if (object instanceof L2Summon)
			{
				player = ((L2Summon) object).getOwner();
			}
			if (player != null && (player.getClan() == null || DevastatedCastleSiege.getInstance().checkIsRegistered(player.getClan())))
				if (getActiveChar().getAI().getIntention() == CtrlIntention.IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.ACTIVE, null);
				}
		}
		else if (FortressOfDeadSiege.getInstance().getIsInProgress())
		{
			L2PcInstance player = null;
			if (object instanceof L2PcInstance)
			{
				player = (L2PcInstance) object;
			}
			else if (object instanceof L2Summon)
			{
				player = ((L2Summon) object).getOwner();
			}
			if (player != null && (player.getClan() == null || FortressOfDeadSiege.getInstance().checkIsRegistered(player.getClan())))
				if (getActiveChar().getAI().getIntention() == CtrlIntention.IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.ACTIVE, null);
				}
		}
		return true;
	}

	@Override
	public final L2SiegeGuardInstance getActiveChar()
	{
		return (L2SiegeGuardInstance) _activeChar;
	}
}
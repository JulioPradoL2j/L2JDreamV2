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
package com.dream.game.model.actor.status;

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;

public class NpcStatus extends CharStatus
{
	public NpcStatus(L2Npc activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) _activeChar;
	}

	@Override
	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT)
	{
		getActiveChar().addAttackerToAttackByList(attacker);
		super.reduceHp0(value, attacker, awake, isDOT);
	}
}
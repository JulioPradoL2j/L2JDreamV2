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
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public final class SummonStatus extends CharStatus
{
	public SummonStatus(L2SummonInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2SummonInstance getActiveChar()
	{
		return (L2SummonInstance) super.getActiveChar();
	}

	@Override
	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT)
	{
		super.reduceHp0(value, attacker, awake, isDOT);

		getActiveChar().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1).addCharName(attacker).addNumber((int) value));
	}
}
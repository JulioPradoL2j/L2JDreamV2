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
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.util.Util;

public final class PcStatus extends CharStatus
{
	public PcStatus(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) _activeChar;
	}

	@Override
	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT)
	{
		double realValue = value;

		if (attacker != null && attacker != getActiveChar())
		{
			L2Summon summon = getActiveChar().getPet();


			if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, getActiveChar(), summon, true))
			{
				int tDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

				if (summon.getStatus().getCurrentHp() < tDmg)
				{
					tDmg = (int) summon.getStatus().getCurrentHp() - 1;
				}

				if (summon.getStatus().getCurrentHp() <= 20)
				{
					summon.getOwner().getFirstEffect(1262).stopEffectTask();
				}

				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker, null);
					value -= tDmg;
					realValue = value;
				}
			}

			if (attacker instanceof L2Playable)
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value);
					value = 0;
				}
				else
				{
					value -= getCurrentCp();
					setCurrentCp(0);
				}
		}

		super.reduceHp0(value, attacker, awake, isDOT);

		if (!getActiveChar().isDead() && getActiveChar().isSitting())
		{
			getActiveChar().standUp();
		}

		if (getActiveChar().isFakeDeath())
		{
			getActiveChar().stopFakeDeath(null);
		}

		if (attacker != getActiveChar() && realValue > 0)
		{
			getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addCharName(attacker).addNumber((int) realValue));
		}
		if (attacker != null)
			attacker.broadcastFullInfoImpl();
	}

	public void restoreHpMp()
	{
		setCurrentHpMp(_activeChar.getMaxHp(), _activeChar.getMaxMp());

	}

}
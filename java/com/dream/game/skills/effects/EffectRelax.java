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
package com.dream.game.skills.effects;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectRelax extends L2Effect
{
	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RELAXING;
	}

	@Override
	public boolean onActionTime()
	{
		boolean retval = true;
		if (getEffected().isDead())
		{
			retval = false;
		}

		if (getEffected() instanceof L2PcInstance)
			if (!((L2PcInstance) getEffected()).isSitting())
			{
				retval = false;
			}

		if (getEffected().getStatus().getCurrentHp() + 1 > getEffected().getMaxHp())
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_DEACTIVATED_HP_FULL);
				retval = false;
			}

		double manaDam = calc();

		if (manaDam > getEffected().getStatus().getCurrentMp())
			if (getSkill().isToggle())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				getEffected().sendPacket(sm);
				retval = false;
			}

		if (!retval)
		{
			setRelax(retval);
		}
		else
		{
			getEffected().reduceCurrentMp(manaDam);
		}

		return retval;
	}

	@Override
	public void onExit()
	{
		setRelax(false);
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			setRelax(true);
			((L2PcInstance) getEffected()).sitDown();
		}
		else
		{
			getEffected().getAI().setIntention(CtrlIntention.REST);
		}

		return true;
	}

	private void setRelax(boolean val)
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).setRelax(val);
		}
	}
}
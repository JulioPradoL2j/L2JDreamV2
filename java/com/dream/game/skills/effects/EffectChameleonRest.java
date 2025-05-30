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
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.templates.skills.L2SkillType;

public final class EffectChameleonRest extends L2Effect
{
	public EffectChameleonRest(Env env, EffectTemplate template)
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
		L2Character effected = getEffected();
		boolean retval = true;

		if (effected.isDead())
		{
			retval = false;
		}

		if (getSkill().getSkillType() != L2SkillType.CONT)
			return false;

		if (effected instanceof L2PcInstance)
			if (!((L2PcInstance) effected).isSitting())
			{
				retval = false;
			}

		double manaDam = calc();

		if (manaDam > effected.getStatus().getCurrentMp())
		{
			effected.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}

		if (!retval)
		{
			setChameleon(retval);
		}
		else
		{
			effected.reduceCurrentMp(manaDam);
		}

		return retval;
	}

	@Override
	public void onExit()
	{
		setChameleon(false);

		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setSilentMoving(false);
		}
	}

	@Override
	public boolean onStart()
	{
		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			setChameleon(true);
			((L2PcInstance) effected).setSilentMoving(true);
			((L2PcInstance) effected).sitDown();
		}
		else
		{
			effected.getAI().setIntention(CtrlIntention.REST);
		}
		return true;
	}

	private void setChameleon(boolean val)
	{
		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setRelax(val);
		}
	}
}
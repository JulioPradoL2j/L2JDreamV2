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
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectRemoveTarget extends L2Effect
{
	public EffectRemoveTarget(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.REMOVE_TARGET;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{

	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2RaidBossInstance || getEffected() instanceof L2GrandBossInstance)
			return true;

		getEffected().setTarget(null);
		getEffected().abortAttack();
		getEffected().abortCast();
		getEffected().getAI().setIntention(CtrlIntention.IDLE, getEffector());
		return true;
	}
}

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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.tools.random.Rnd;

public final class EffectConfusion extends L2Effect
{

	private static final Logger _log = Logger.getLogger(EffectConfusion.class);

	public EffectConfusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CONFUSION;
	}

	@Override
	public boolean onActionTime()
	{
		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.debug(getEffected());
		}
		List<L2Character> targetList = new ArrayList<>();

		for (L2Object obj : getEffected().getKnownList().getKnownObjects().values())
			if (obj instanceof L2Character && obj != getEffected())
			{
				targetList.add((L2Character) obj);
			}

		if (targetList.size() == 0)
			return true;

		int nextTargetIdx = Rnd.nextInt(targetList.size());
		L2Object target = targetList.get(nextTargetIdx);

		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.ATTACK, target);

		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopConfused(this);
	}

	@Override
	public boolean onStart()
	{
		getEffected().startConfused();
		onActionTime();
		return true;
	}
}
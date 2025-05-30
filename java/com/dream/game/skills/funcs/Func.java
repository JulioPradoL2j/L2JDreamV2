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
package com.dream.game.skills.funcs;

import com.dream.game.model.L2Skill;
import com.dream.game.skills.Env;
import com.dream.game.skills.Stats;
import com.dream.game.skills.conditions.Condition;

public abstract class Func
{
	public static final Func[] EMPTY_ARRAY = new Func[0];

	public final Stats stat;

	public final int order;

	public final FuncOwner funcOwner;

	public final Condition condition;

	protected Func(Stats pStat, int pOrder, FuncOwner pFuncOwner)
	{
		this(pStat, pOrder, pFuncOwner, null);
	}

	protected Func(Stats pStat, int pOrder, FuncOwner pFuncOwner, Condition pCondition)
	{
		stat = pStat;
		order = pOrder;
		funcOwner = pFuncOwner;
		condition = pCondition;
	}

	protected abstract void calc(Env env);

	public final void calcIfAllowed(Env env)
	{
		if (isAllowed(env))
		{
			calc(env);
		}
	}

	public final boolean isAllowed(Env env)
	{
		if (env.player != null && funcOwner != null)
		{
			final L2Skill skill = funcOwner.getFuncOwnerSkill();

			if (skill != null && skill.ownedFuncShouldBeDisabled(env.player))
				return false;
		}

		if (condition != null && !condition.test(env))
			return false;

		return true;
	}
}
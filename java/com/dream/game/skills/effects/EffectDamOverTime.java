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

import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.network.SystemMessageId;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.templates.skills.L2SkillType;

public final class EffectDamOverTime extends L2Effect
{
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DMG_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;

		double damage = calc();
		if (getSkill().getId() < 2000)
		{
			if (getSkill().getSkillType() == L2SkillType.POISON)
			{
				damage = damage * 2;
			}
			else if (getSkill().getSkillType() == L2SkillType.BLEED)
			{
				damage = damage * 2;
			}
			if (damage > 300)
			{
				damage = 300;
			}
		}
		if (damage >= getEffected().getStatus().getCurrentHp() - 1)
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP);
				return false;
			}

			if (!getSkill().killByDOT())
			{
				if (getEffected().getStatus().getCurrentHp() <= 1)
					return true;
				damage = getEffected().getStatus().getCurrentHp() - 1;
			}
		}

		boolean awake = !(getEffected() instanceof L2Attackable) && !(getSkill().getTargetType() == SkillTargetType.TARGET_SELF && getSkill().isToggle());

		getEffected().reduceCurrentHpByDamOverTime(damage, getEffector(), awake, getPeriod());

		return true;
	}
}
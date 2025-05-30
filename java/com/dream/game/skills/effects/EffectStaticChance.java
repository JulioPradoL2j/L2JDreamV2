package com.dream.game.skills.effects;

import com.dream.game.model.ChanceCondition;
import com.dream.game.model.ChanceSkillList;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

final class EffectStaticChance extends L2Effect
{
	public EffectStaticChance(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		L2Skill skill = getSkill();
		if (skill != null)
		{
			L2Character cha = getEffected();
			if (cha != null)
			{
				ChanceSkillList chList = cha.getChanceSkills();
				if (chList != null)
				{
					ChanceCondition cc = chList.get(skill);
					if (cc != null)
					{
						cc.isReady = true;
					}
				}
			}
		}
	}

	@Override
	public boolean onStart()
	{
		L2Skill skill = getSkill();
		if (skill != null)
		{
			L2Character cha = getEffected();
			if (cha != null)
			{
				ChanceSkillList chList = cha.getChanceSkills();
				if (chList != null)
				{
					ChanceCondition cc = chList.get(skill);
					if (cc != null)
					{
						cc.isReady = false;
					}
				}
			}
		}
		return true;
	}
}
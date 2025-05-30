package com.dream.game.skills.effects;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

final class EffectUpSkillLevel extends L2Effect
{
	public EffectUpSkillLevel(Env env, EffectTemplate template)
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
	}

	@Override
	public boolean onStart()
	{
		L2Skill tmpSkill = SkillTable.getInstance().getInfo(getSkill().getId(), getSkill().getLevel() + 1);
		if (tmpSkill != null)
		{
			getEffected().addSkill(tmpSkill);
		}
		return true;
	}
}
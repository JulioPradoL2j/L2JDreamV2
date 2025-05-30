package com.dream.game.skills.effects;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectChanceSkillTrigger extends L2Effect
{
	public EffectChanceSkillTrigger(Env env, EffectTemplate template)
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
		getEffected().onExitChanceEffect();
		getEffected().removeChanceEffect(_triggeredId);
		super.onExit();
	}

	@Override
	public boolean onStart()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(_triggeredId, _triggeredLevel);
		if (skill != null)
		{
			getEffected().addChanceEffect(skill);
		}

		return super.onStart();
	}
}
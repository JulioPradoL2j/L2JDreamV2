package com.dream.game.skills.effects;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public class EffectFusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;
	L2Skill skill;

	public EffectFusion(Env env, EffectTemplate template)
	{
		super(env, template);
		_effect = getSkill().getLevel();
		_maxEffect = 10;
	}

	public void decreaseForce()
	{
		_effect--;
		if (_effect < 1)
		{
			exit();
		}
		else
		{
			updateBuff();
		}
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FUSION;
	}

	public void increaseEffect()
	{
		if (_effect < _maxEffect)
		{
			_effect++;
			updateBuff();
		}
	}

	@Override
	public boolean onActionTime()
	{
		return true;
	}

	private void updateBuff()
	{

		exit();

		if (getSkill() != null)
		{
			skill = SkillTable.getInstance().getInfo(getSkill().getId(), _effect);
		}

		if (skill != null)
		{
			skill.getEffects(getEffector(), getEffected());
		}
	}
}
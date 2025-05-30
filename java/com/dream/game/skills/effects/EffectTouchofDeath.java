package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.tools.random.Rnd;



public final class EffectTouchofDeath extends L2Effect
{
	public EffectTouchofDeath(Env env, EffectTemplate template)
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
		return cancel(getEffector(), getEffected(), this);
	}
	
	private static boolean cancel(L2Character caster, L2Character target, L2Effect effect)
	{
		if (!(target instanceof L2PcInstance) || target.isDead())
			return false;
		
		final int cancelLvl = effect.getSkill().getMagicLevel();
		int count = effect.getSkill().getMaxNegatedEffects();
		
		double rate = effect.getLevel();
		
		// Resistance/vulnerability
		final double res = Formulas.calcSkillVulnerability(target, effect.getSkill(), effect.getSkill().getSkillType());
		rate *= res;
		
		L2Effect eff;
		int lastCanceledSkillId = 0;
		final L2Effect[] effects = target.getAllEffects();
		for (int i = effects.length; --i >= 0;)
		{
			eff = effects[i];
			if (eff == null)
				continue;
			
			// first pass - dances/songs only
			if (!eff.getSkill().isDance())
				continue;
			
			if (eff.getSkill().getId() == lastCanceledSkillId)
			{
				eff.exit(); // this skill already canceled
				continue;
			}
			
			if (!calcCancelSuccess(eff, cancelLvl, (int) rate))
				continue;
			
			lastCanceledSkillId = eff.getSkill().getId();
			eff.exit();
			count--;
			
			if (count == 0)
				break;
		}
		
		if (count != 0)
		{
			lastCanceledSkillId = 0;
			for (int i = effects.length; --i >= 0;)
			{
				eff = effects[i];
				if (eff == null)
					continue;
				
				// second pass - all except dances/songs
				if (eff.getSkill().isDance())
					continue;
				
				if (eff.getSkill().getId() == lastCanceledSkillId)
				{
					eff.exit(); // this skill already canceled
					continue;
				}
				
				if (!calcCancelSuccess(eff, cancelLvl, (int) rate))
					continue;
				
				lastCanceledSkillId = eff.getSkill().getId();
				eff.exit();
				count--;
				
				if (count == 0)
					break;
			}
		}
		return true;
	}
	
	private static boolean calcCancelSuccess(L2Effect effect, int cancelLvl, int baseRate)
	{
		int rate = 2 * (cancelLvl - effect.getSkill().getMagicLevel());
		rate += effect.getPeriod() / 120;
		rate += baseRate;
		
		if (rate < 25)
			rate = 25;
		else if (rate > 75)
			rate = 75;
		
		return Rnd.get(100) < rate;
	}}


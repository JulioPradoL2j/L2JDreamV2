package com.dream.game.model.zone;

import com.dream.game.model.actor.L2Character;

public class L2DamageZone extends L2DynamicZone
{
	@Override
	protected void checkForDamage(L2Character character)
	{
		if (_hpDamage > 0)
		{
			character.reduceCurrentHp(_hpDamage, character);
		}
		if (_mpDamage > 0)
		{
			character.reduceCurrentMp(_mpDamage);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(this, FLAG_DANGER, true);
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(this, FLAG_DANGER, false);
		super.onExit(character);
	}
}
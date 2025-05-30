package com.dream.game.model;

import com.dream.game.model.actor.L2Character;

public interface IEffector
{
	public void onEffectFinished(L2Character effected, L2Skill skill);
}

package com.dream.game.skills;

import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;

public final class Env
{
	public L2Character player;
	public L2CubicInstance cubic;
	public L2Character target;
	public L2ItemInstance item;
	public L2Skill skill;
	public L2Effect effect;
	public Object object;
	public double value;
	public double baseValue;
	public boolean skillMastery = false;
}
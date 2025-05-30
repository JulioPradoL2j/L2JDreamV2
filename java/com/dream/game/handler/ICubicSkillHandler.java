package com.dream.game.handler;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2CubicInstance;

public interface ICubicSkillHandler extends ISkillHandler
{
	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Character... targets);

}
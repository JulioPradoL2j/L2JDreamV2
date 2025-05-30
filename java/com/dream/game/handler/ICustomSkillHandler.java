package com.dream.game.handler;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;

public interface ICustomSkillHandler
{
	public int[] getSkills();

	public void useSkill(L2Character caster, L2Skill skill, L2Character... targets);

}
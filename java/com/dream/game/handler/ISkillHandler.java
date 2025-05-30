package com.dream.game.handler;

import org.apache.log4j.Logger;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.templates.skills.L2SkillType;

public interface ISkillHandler
{
	public static final Logger _log = Logger.getLogger(ISkillHandler.class);

	public L2SkillType[] getSkillIds();

	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets);

}
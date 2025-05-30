package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.templates.skills.L2SkillType;

public class GiveSp implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GIVE_SP
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			int spToAdd = (int) skill.getPower();
			target.addExpAndSp(0, spToAdd);
		}
	}
}
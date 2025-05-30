package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.templates.skills.L2SkillType;

public class Dummy implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.DUMMY,
		L2SkillType.BEAST_FEED
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{

	}

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		if (skill.getSkillType() == L2SkillType.BEAST_FEED)
		{
			L2Object[] targetList = skill.getTargetList(activeChar);
			if (targetList == null)
				return;
		}
	}
}
package com.dream.game.skills.l2skills;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.util.StatsSet;

public class L2SkillBattleForce extends L2Skill
{
	int _symbolType;
	int _symbolRadius;

	public L2SkillBattleForce(StatsSet set)
	{
		super(set);

		_symbolType = set.getInteger("1", getLevel());
		_symbolRadius = set.getInteger("SymbolRadius");
	}

	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

	}
}
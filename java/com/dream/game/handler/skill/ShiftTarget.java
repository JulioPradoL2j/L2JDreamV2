package com.dream.game.handler.skill;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.templates.skills.L2SkillType;

public class ShiftTarget implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SHIFT_TARGET
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		L2Attackable attackerChar = null;
		L2Npc attacker = null;
		L2PcInstance targetChar = null;

		boolean targetShifted = false;

		for (L2Object target : targets)
			if (target instanceof L2PcInstance)
			{
				targetChar = (L2PcInstance) target;
				break;
			}

		for (L2Object nearby : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
			if (!targetShifted)
				if (nearby instanceof L2Attackable)
				{
					attackerChar = (L2Attackable) nearby;
					targetShifted = true;
					break;
				}

		if (targetShifted && attackerChar != null && targetChar != null)
		{
			attacker = attackerChar;
			int aggro = attackerChar.getHating(activeChar);

			if (aggro == 0)
			{
				if (targetChar.isRunning())
				{
					attacker.setRunning();
				}
				attackerChar.addDamageHate(targetChar, 0, 1);
				attacker.setTarget(targetChar);
				attackerChar.getAI().setIntention(CtrlIntention.ATTACK, targetChar);
			}
			else
			{
				attackerChar.stopHating(activeChar);
				if (targetChar.isRunning())
				{
					attacker.setRunning();
				}
				attackerChar.addDamageHate(targetChar, 0, aggro);
				attacker.setTarget(targetChar);
				attackerChar.getAI().setIntention(CtrlIntention.ATTACK, targetChar);
			}
		}
	}
}
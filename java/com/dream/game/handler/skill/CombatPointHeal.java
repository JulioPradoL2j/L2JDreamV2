package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;

public class CombatPointHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.COMBATPOINTHEAL
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character actChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF).useSkill(actChar, skill, targets);

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			double cp = skill.getPower();
			int msg1 = (int) cp;

			if (target.getStatus().getCurrentCp() + cp >= target.getMaxCp())
			{
				cp = target.getMaxCp() - target.getStatus().getCurrentCp();
			}

			target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber(msg1));
			target.getStatus().setCurrentCp(cp + target.getStatus().getCurrentCp());
			StatusUpdate sump = new StatusUpdate(target);
			sump.addAttribute(StatusUpdate.CUR_CP, (int) target.getStatus().getCurrentCp());
			target.sendPacket(sump);
		}
	}
}
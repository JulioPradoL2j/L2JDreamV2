package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;

public class CPperHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.COMBATPOINTPERCENTHEAL
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

			double perCp = target.getMaxCp() * skill.getPower();
			double newCp = target.getStatus().getCurrentCp() + perCp;
			int msg1 = (int) perCp;

			if (newCp > target.getMaxCp())
			{
				perCp = target.getMaxCp() - target.getStatus().getCurrentCp();
			}

			target.getStatus().setCurrentCp(target.getStatus().getCurrentCp() + perCp);
			StatusUpdate sucp = new StatusUpdate(target);
			sucp.addAttribute(StatusUpdate.CUR_CP, (int) target.getStatus().getCurrentCp());
			target.sendPacket(sucp);
			target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber(msg1));
		}
	}
}
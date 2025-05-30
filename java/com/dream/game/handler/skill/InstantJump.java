package com.dream.game.handler.skill;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.FlyToLocation;
import com.dream.game.network.serverpackets.FlyToLocation.FlyType;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;

public class InstantJump implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.INSTANT_JUMP
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (targets.length == 0 || targets[0] == null)
			return;

		if (activeChar.isRooted())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
			return;
		}

		L2Character target = targets[0];

		int x = 0, y = 0, z = 0;

		int px = target.getX();
		int py = target.getY();
		double ph = Util.convertHeadingToDegree(target.getHeading());

		ph += 180;

		if (ph > 360)
		{
			ph -= 360;
		}

		ph = Math.PI * ph / 180;

		x = (int) (px + 25 * Math.cos(ph));
		y = (int) (py + 25 * Math.sin(ph));
		z = target.getZ();

		activeChar.getAI().setIntention(CtrlIntention.IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, x, y, z, FlyType.DUMMY));
		activeChar.abortAttack();
		activeChar.abortCast();

		activeChar.getPosition().setXYZ(x, y, z);
		activeChar.broadcastPacket(new ValidateLocation(activeChar));

		if (skill.hasEffects())
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
			{
				activeChar.stopSkillEffects(skill.getId());
				skill.getEffects(target, activeChar);
			}
			else
			{
				target.stopSkillEffects(skill.getId());
				if (Formulas.calcSkillSuccess(activeChar, target, skill, false, false, false))
				{
					skill.getEffects(activeChar, target);
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
				}
			}
	}
}
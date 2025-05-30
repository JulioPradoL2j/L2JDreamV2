package com.dream.game.handler.skill;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.skills.Formulas;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.templates.skills.L2SkillType;

public class Resurrect implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.RESURRECT
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}

		L2PcInstance targetPlayer;
		List<L2Character> targetToRes = new ArrayList<>();

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}
			if (Math.abs(target.getZ() - activeChar.getZ()) > 300)
			{
				continue;
			}

			if (target instanceof L2PcInstance)
			{
				targetPlayer = (L2PcInstance) target;

				if (targetPlayer.isInOlympiadMode())
				{
					continue;
				}
				if (skill.getTargetType() == SkillTargetType.TARGET_CORPSE_CLAN)
					if (player != null && player.getClanId() != targetPlayer.getClanId())
					{
						continue;
					}

			}

			targetToRes.add(target);
		}

		for (L2Character cha : targetToRes)
			if (activeChar instanceof L2PcInstance)
			{
				if (cha instanceof L2PcInstance)
				{
					((L2PcInstance) cha).reviveRequest((L2PcInstance) activeChar, skill);
				}
				else if (cha instanceof L2PetInstance)
				{
					if (((L2PetInstance) cha).getOwner() == activeChar)
					{
						cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
					}
					else
					{
						((L2PetInstance) cha).getOwner().revivePetRequest((L2PcInstance) activeChar, skill);
					}
				}
				else
				{
					cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
				}
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(cha);
				cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
	}
}
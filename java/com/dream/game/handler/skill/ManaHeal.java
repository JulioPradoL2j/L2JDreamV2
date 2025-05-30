package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.templates.skills.L2SkillType;

public class ManaHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MANAHEAL,
		L2SkillType.MANARECHARGE,
		L2SkillType.MANAHEAL_PERCENT
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

			double mp = skill.getPower();
			if (skill.getSkillType() == L2SkillType.MANAHEAL_PERCENT)
			{
				mp = target.getMaxMp() * mp / 100.0;
			}
			else
			{
				mp = skill.getSkillType() == L2SkillType.MANARECHARGE ? target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null) : mp;
			}

			if (actChar.getLevel() != target.getLevel())
				if (actChar.getLevel() + 3 >= target.getLevel())
				{
					mp = mp * 1;
				}
				else if (actChar.getLevel() + 5 <= target.getLevel())
				{
					mp = mp * 0.6;
				}
				else if (actChar.getLevel() + 7 <= target.getLevel())
				{
					mp = mp * 0.4;
				}
				else if (actChar.getLevel() + 9 <= target.getLevel())
				{
					mp = mp * 0.3;
				}
				else if (actChar.getLevel() + 10 <= target.getLevel())
				{
					mp = mp * 0.1;
				}

			int msg1 = (int) mp;
			if (target.getStatus().getCurrentMp() + mp >= target.getMaxMp())
			{
				mp = target.getMaxMp() - target.getStatus().getCurrentMp();
			}

			target.setLastHealAmount((int) mp);
			target.getStatus().setCurrentMp(mp + target.getStatus().getCurrentMp());
			StatusUpdate sump = new StatusUpdate(target);
			sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getStatus().getCurrentMp());
			target.sendPacket(sump);

			if (actChar instanceof L2PcInstance && actChar != target)
			{
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1).addString(actChar.getName()).addNumber(msg1));
			}
			else
			{
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber(msg1));
			}
		}
	}
}
package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.templates.skills.L2SkillType;

public class BalanceLife implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BALANCE_LIFE
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF).useSkill(activeChar, skill, targets);

		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}

		double fullHP = 0;
		double currentHPs = 0;

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			if (target.isDead())
			{
				continue;
			}

			if (target != activeChar)
				if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
				{
					continue;
				}
				else if (player != null && player.isCursedWeaponEquipped())
				{
					continue;
				}

			fullHP += target.getMaxHp();
			currentHPs += target.getStatus().getCurrentHp();
		}

		double percentHP = currentHPs / fullHP;

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			double newHP = target.getMaxHp() * percentHP;
			double totalHeal = newHP - target.getStatus().getCurrentHp();

			target.getStatus().increaseHp(totalHeal);

			if (totalHeal > 0)
			{
				target.setLastHealAmount((int) totalHeal);
			}

			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getStatus().getCurrentHp());
			target.sendPacket(su);
		}
	}
}
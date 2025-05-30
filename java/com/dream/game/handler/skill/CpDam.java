package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2SkillType;

public class CpDam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CPDAM
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean sps = false;
		boolean bss = false;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
				}
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
				}
			}
			else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
			}
		}
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;

			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2Npc)
		{
			bss = ((L2Npc) activeChar).isUsingShot(false);
			ss = ((L2Npc) activeChar).isUsingShot(true);
		}

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isDead())
			{
				continue;
			}

			if (!Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
				return;

			int damage = (int) (target.getStatus().getCurrentCp() * (1 - skill.getPower()));

			if (Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
			skill.getEffects(activeChar, target);
			activeChar.sendDamageMessage(target, damage, false, false, false);
			target.getStatus().setCurrentCp(target.getStatus().getCurrentCp() - damage);
		}
	}
}
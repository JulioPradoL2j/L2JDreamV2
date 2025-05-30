package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2SkillType;

public class StrSiegeAssault implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STRSIEGEASSAULT
	};

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if (SiegeManager.checkIfOkToUseStriderSiegeAssault(player, false) || FortSiegeManager.checkIfOkToUseStriderSiegeAssault(player, false))
		{
			int damage = 0;

			for (L2Character target : targets)
			{
				if (target == null)
				{
					continue;
				}

				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
				{
					target.stopFakeDeath(null);
				}
				else if (target.isDead())
				{
					continue;
				}

				boolean dual = activeChar.isUsingDualWeapon();
				byte shld = Formulas.calcShldUse(activeChar, target);
				boolean crit = Formulas.calcCrit(activeChar, target, activeChar.getCriticalHit(target, skill));
				boolean soul = weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER;

				if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
				{
					damage = 0;
				}
				else
				{
					damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
				}

				if (damage > 0)
				{
					target.reduceCurrentHp(damage, activeChar, skill);
					if (soul && weapon != null)
					{
						weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, true);
					}
					activeChar.sendDamageMessage(target, damage, false, false, false);
				}
				else
				{
					activeChar.sendMessage(skill.getName() + " failed.");
				}
			}
		}
	}
}
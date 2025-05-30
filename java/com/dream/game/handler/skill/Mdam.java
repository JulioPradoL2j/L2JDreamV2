package com.dream.game.handler.skill;

import com.dream.game.handler.ICubicSkillHandler;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2SkillType;

public class Mdam implements ICubicSkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MDAM,
		L2SkillType.DEATHLINK
	};

	protected int calcAdditionalDamage(int baseDamage, L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		int addDamage = 0;
		if (skill.getId() == 1439)
		{
			addDamage += baseDamage * 0.1 * targets[0].getBuffCount();
		}
		return addDamage;
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Character... targets)
	{
		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			if (target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));
			int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit);

			if ((Formulas.calcSkillReflect(target, skill) & Formulas.SKILL_REFLECT_SUCCEED) > 0)
			{
				damage = 0;
			}

			if (damage > 0)
			{
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);

				if (skill.hasEffects())
				{
					target.stopSkillEffects(skill.getId());
					if (target.getFirstEffect(skill) != null)
					{
						target.removeEffect(target.getFirstEffect(skill));
					}
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						skill.getEffects(activeCubic, target);
					}
				}

				target.reduceCurrentHp(damage, activeCubic.getOwner(), skill);
			}
		}
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean bss = false;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
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
			if (target instanceof L2Boss)
				if (Math.abs(target.getZ() - activeChar.getZ()) > 50)
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

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
			int damage = (int) Formulas.calcMagicDam(activeChar, target, skill, ss, bss, mcrit);
			final byte reflect = Formulas.calcSkillReflect(target, skill);

			if (skill.getCancelId() > 0)
			{
				L2Effect[] effects = target.getAllEffects();
				for (L2Effect e : effects)
					if (e.getSkill().getId() == skill.getCancelId())
					{
						e.exit();
					}
			}

			damage += calcAdditionalDamage(damage, activeChar, skill, targets);

			if (damage < 1)
			{
				damage = 1;
			}

			if (damage > 0)
			{
				if (Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (skill.hasEffects())
					if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
					else if (Formulas.calcSkillSuccess(activeChar, target, skill, false, ss, bss))
					{
						target.stopSkillEffects(skill.getId());
						skill.getEffects(activeChar, target);
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
					}
				target.reduceCurrentHp(damage, activeChar, skill);
				if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
				{
					activeChar.reduceCurrentHp(damage, target, skill);
				}
			}
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);

		if (skill.isSuicideAttack())
		{
			L2Character target = null;
			for (L2Character tmp : targets)
				if (tmp != null && !(tmp instanceof L2Playable))
				{
					target = tmp;
					break;
				}
			activeChar.doDie(target);
		}
	}
}
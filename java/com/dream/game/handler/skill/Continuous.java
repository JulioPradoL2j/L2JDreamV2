package com.dream.game.handler.skill;

import com.dream.game.ai.CtrlEvent;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.ICubicSkillHandler;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ClanHallManagerInstance;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.templates.skills.L2SkillType;


public class Continuous implements ICubicSkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BUFF,
		L2SkillType.DEBUFF,
		L2SkillType.DOT,
		L2SkillType.MDOT,
		L2SkillType.POISON,
		L2SkillType.BLEED,
		L2SkillType.HOT,
		L2SkillType.CPHOT,
		L2SkillType.MPHOT,
		L2SkillType.FEAR,
		L2SkillType.CONT,
		L2SkillType.WEAKNESS,
		L2SkillType.REFLECT,
		L2SkillType.UNDEAD_DEFENSE,
		L2SkillType.AGGDEBUFF,
		L2SkillType.FUSION,
		L2SkillType.BAD_BUFF
	};

	private L2Skill _skill;

	private L2Summon activeSummon;

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

			if (skill.isOffensive())
			{
				boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill);
				if (!acted)
				{
					activeCubic.getOwner().sendPacket(SystemMessageId.ATTACK_FAILED);
					continue;
				}
			}
			skill.getEffects(activeCubic, target);
		}
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		boolean acted = true;
		boolean consumeSoul = true;
		boolean ss = false;
		boolean sps = false;
		boolean bss = false;

		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}

		if (skill.getEffectId() != 0)
		{
			int skillLevel = (int) skill.getEffectLvl();
			int skillEffectId = skill.getEffectId();

			if (skillLevel == 0)
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, 1);
			}
			else
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
			}

			if (_skill != null)
			{
				skill = _skill;
			}
		}

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			if ((target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance) && skill.getSkillType() == L2SkillType.FEAR)
			{
				continue;
			}
			if (target instanceof L2Summon && target.getActingPlayer() == activeChar)
				if (skill.getSkillType() == L2SkillType.FEAR)
				{
					continue;
				}
			switch (skill.getSkillType())
			{
				case BUFF:
					if (activeChar instanceof L2Playable && target != activeChar && target.getFirstEffect(L2EffectType.BLOCK_BUFF) != null)
						continue;

					if (Math.abs(target.getZ() - activeChar.getZ()) > 200)
					{
						continue;
					}
				case HOT:
				case CPHOT:
				case MPHOT:
				case AGGDEBUFF:
				case CONT:
				case UNDEAD_DEFENSE:
				case BAD_BUFF:
					break;
				default:
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}
					break;
			}

			if (target.isPreventedFromReceivingBuffs())
			{
				continue;
			}

			if (skill.getSkillType() == L2SkillType.BUFF && !(activeChar instanceof L2ClanHallManagerInstance))
				if (target != activeChar)
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
					{
						continue;
					}
					else if (player != null && player.isCursedWeaponEquipped())
					{
						continue;
					}

			if (skill.isDebuff() && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) != null)
				continue;

			if (skill.isOffensive() || skill.isDebuff())
			{
				if (player != null)
				{
					L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
					if (weaponInst != null && consumeSoul)
						if (skill.isMagic())
						{
							if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
							{
								bss = true;
								if (skill.getId() != 1020)
								{
									weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
									consumeSoul = false;
								}
							}
							else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
							{
								sps = true;
								if (skill.getId() != 1020)
								{
									weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
									consumeSoul = false;
								}
							}
						}
						else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
						{
							ss = true;
							if (skill.getId() != 1020)
							{
								weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, true);
								consumeSoul = false;
							}
						}
				}
				else if (activeChar instanceof L2Summon)
				{
					activeSummon = (L2Summon) activeChar;
					if (activeSummon != null && consumeSoul)
						if (skill.isMagic())
						{
							if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
							{
								bss = true;
								activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
								consumeSoul = false;
							}
							else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
							{
								sps = true;
								activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
								consumeSoul = false;
							}
						}
						else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
						{
							ss = true;
							activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
							consumeSoul = false;
						}
				}
				else if (activeChar instanceof L2Npc)
				{
					bss = ((L2Npc) activeChar).isUsingShot(false);
					ss = ((L2Npc) activeChar).isUsingShot(true);
				}

				acted = Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss);
			}

			if (acted)
			{
				if (skill.isToggle())
				{
					L2Effect[] effects = target.getAllEffects();
					if (effects != null)
					{
						for (L2Effect e : effects)
							if (e != null)
								if (e.getSkill().getId() == skill.getId())
								{
									e.exit();
									return;
								}
					}
				}
				skill.getEffects(activeChar, target);

				if (skill.getSkillType() == L2SkillType.AGGDEBUFF)
					if (target instanceof L2Attackable)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					}
					else if (target instanceof L2Playable)
						if (target.getTarget() == activeChar)
						{
							target.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
						}
						else
						{
							target.setTarget(activeChar);
						}

				if (target.isDead() && skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB && target instanceof L2Npc)
				{
					((L2Npc) target).endDecayTask();
				}

				if (Formulas.calcLethalHit(activeChar, target, skill))
					if (skill.getSkillType() == L2SkillType.FEAR && skill.getLevel() >= 301 && skill.getLevel() <= 330)
					{
						switch (skill.getId())
						{
							case 1400:
								target.reduceCurrentHp(skill.getLevel(), activeChar, skill);
								break;
							case 450:
								target.reduceCurrentHp(skill.getLevel(), activeChar, skill);
								break;
							case 405:
								target.reduceCurrentHp(skill.getLevel(), activeChar, skill);
								break;
							default:
								break;
						}
					}
			}
			else if (activeChar instanceof L2PcInstance)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(skill.getId()));
			}
		}

		if (activeChar instanceof L2PcInstance && skill.getGiveCharges() > 0)
		{
			((L2PcInstance) activeChar).increaseCharges(skill.getGiveCharges(), skill.getMaxCharges());
		}

		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);
	}
}
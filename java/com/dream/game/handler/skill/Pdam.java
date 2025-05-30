package com.dream.game.handler.skill;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.BaseStats;
import com.dream.game.skills.Formulas;
import com.dream.game.skills.Stats;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2SkillType;

public class Pdam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.PDAM,
		L2SkillType.FATALCOUNTER
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

		L2ItemInstance weapon = null;
		boolean soul = false;
		int damage = 0;

		weapon = activeChar.getActiveWeaponInstance();
		if (weapon != null)
			if (weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER)
			{
				soul = true;
				weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, true);
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

			boolean dual = activeChar.isUsingDualWeapon();
			byte shld;
			boolean crit = false;
			if (skill.getBaseCritRate() > 0)
			{
				crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar));
			}

			if (skill.ignoreShld())
			{
				shld = 0;
			}
			else
			{
				shld = Formulas.calcShldUse(activeChar, target);
			}

			if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
			{
				damage = 0;
			}
			else
			{
				damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
			}

			if (crit)
			{
				damage *= 2;
			}

			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			final byte reflect = Formulas.calcSkillReflect(target, skill);

			if (!skillIsEvaded)
			{
				if (damage > 0)
				{
					activeChar.sendDamageMessage(target, damage, false, crit, false);

					if (skill.hasEffects())
					{
						if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
						{
							activeChar.stopSkillEffects(skill.getId());
							skill.getEffects(target, activeChar);
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
						}
						else if (Formulas.calcSkillSuccess(activeChar, target, skill, false, false, false))
						{
							skill.getEffects(activeChar, target);
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
						}

						if (damage > 5000 && activeChar instanceof L2PcInstance)
						{
							String name = "";
							if (target instanceof L2RaidBossInstance)
							{
								name = "RaidBoss ";
							}
							if (target instanceof L2Npc)
							{
								name += target.getName() + "(" + ((L2Npc) target).getTemplate().getNpcId() + ")";
							}
							if (target instanceof L2PcInstance)
							{
								name = target.getName() + "(" + target.getObjectId() + ") ";
							}
							name += target.getLevel() + " lvl";
							if (_log.isDebugEnabled())
							{
								_log.info(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name);
							}
						}
					}

					boolean lethal = Formulas.calcLethalHit(activeChar, target, skill);

					if (!lethal && skill.getDmgDirectlyToHP())
					{
						final L2Character[] ts =
						{
							target,
							activeChar
						};

						for (L2Character targ : ts)
						{
							if (target instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance) targ;
								if (!player.isInvul())
									if (damage >= player.getStatus().getCurrentHp())
									{
										if (player.isInDuel())
										{
											player.getStatus().setCurrentHp(1);
										}
										else
										{
											player.getStatus().setCurrentHp(0);
											if (player.isInOlympiadMode())
											{
												player.abortAttack();
												player.abortCast();
												player.getStatus().stopHpMpRegeneration();
												player.setIsDead(true);
												player.setIsPendingRevive(true);
												if (player.getPet() != null)
												{
													player.getPet().getAI().setIntention(CtrlIntention.IDLE, null);
												}
											}
											else
											{
												player.doDie(activeChar);
											}
										}
									}
									else
									{
										player.getStatus().setCurrentHp(player.getStatus().getCurrentHp() - damage);
									}

							}
							else
							{
								target.reduceCurrentHp(damage, activeChar, skill);
							}
							if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) == 0)
							{
								break;
							}
						}
					}
					else
					{
						target.reduceCurrentHp(damage, activeChar, skill);
						if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
						{
							int relectedDamage = (int) target.calcStat(Stats.VENGEANCE_SKILL_VALUE, damage, activeChar, skill);
							if (relectedDamage > 0)
							{
								activeChar.reduceCurrentHp(relectedDamage, target, skill);
								target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(relectedDamage));
							}
						}
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
				}
			}
			else
			{
				if (activeChar instanceof L2PcInstance)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
				}
				if (target instanceof L2PcInstance)
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(activeChar));
				}

				Formulas.calcLethalHit(activeChar, target, skill);
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
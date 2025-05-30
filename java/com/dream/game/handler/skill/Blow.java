package com.dream.game.handler.skill;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.BaseStats;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.skills.Stats;
import com.dream.game.skills.funcs.Func;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;

public class Blow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BLOW
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

		boolean soul = false;
		L2ItemInstance weapon = null;

		weapon = activeChar.getActiveWeaponInstance();
		if (weapon != null)
		{
			soul = weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() == L2WeaponType.DAGGER;
			if (soul)
			{
				weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, true);
			}
		}

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			if (target.isAlikeDead())
			{
				continue;
			}

			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			byte _successChance = Config.BLOW_SIDE;

			if (activeChar.isBehindTarget())
			{
				_successChance = Config.BLOW_BEHIND;
			}
			else if (activeChar.isInFrontOfTarget())
			{
				_successChance = Config.BLOW_FRONT;
			}

			boolean success = true;
			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
			{
				success = _successChance == Config.BLOW_BEHIND;
			}
			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
			{
				success = success && Formulas.calcBlow(activeChar, target, _successChance);
				activeChar.sendPacket(new PlaySound("skillsound.critical_hit_02"));
			}
			if (!skillIsEvaded && success)
			{
				final byte reflect = Formulas.calcSkillReflect(target, skill);

				if (skill.hasEffects())
					if (reflect == Formulas.SKILL_REFLECT_SUCCEED)
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
				byte shld = Formulas.calcShldUse(activeChar, target);
				boolean crit = false;
				if (Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar)))
				{
					crit = true;
				}

				double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, soul);

				L2Effect Tpain = target.getFirstEffect(1262);

				if (crit)
				{
					damage *= 2;
					L2Effect vicious = activeChar.getFirstEffect(312);
					if (vicious != null && damage > 1)
					{
						for (Func func : vicious.getStatFuncs())
						{
							Env env = new Env();
							env.player = activeChar;
							env.target = target;
							env.skill = skill;
							env.value = damage;
							func.calcIfAllowed(env);
							damage = (int) env.value;
						}
					}
				}

				if (skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
				{
					final L2Character[] ts =
					{
						target,
						activeChar
					};
					for (L2Character targ : ts)
					{

						L2PcInstance player = (L2PcInstance) targ;
						if (!player.isInvul() && !player.isPetrified())
						{
							L2Summon summon = player.getPet();
							if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, player, summon, true))
							{
								int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

								if (summon.getStatus().getCurrentHp() < tDmg)
								{
									tDmg = (int) summon.getStatus().getCurrentHp() - 1;
								}
								if (tDmg > 0)
								{
									summon.reduceCurrentHp(tDmg, activeChar, skill);
									damage -= tDmg;
								}
							}

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
								if (player.isSleeping())
								{
									player.stopSleeping(null);
								}
								if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isInOlympiadMode())
								{
									((L2PcInstance) activeChar).addOlyDamage((int) damage);
								}
							}
						}
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addPcName((L2PcInstance) activeChar).addNumber((int) damage));
						if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) == 0)
						{
							break;
						}
					}
				}
				else
				{
					target.reduceCurrentHp(damage, activeChar, true, skill);
					if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
					{
						int relectedDamage = (int) target.calcStat(Stats.VENGEANCE_SKILL_VALUE, damage, activeChar, skill);
						if (relectedDamage > 0)
						{
							activeChar.reduceCurrentHp(relectedDamage, target, skill);
						}
					}
				}

				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				if (activeChar instanceof L2PcInstance && Tpain == null)
				{
					L2PcInstance activePlayer = (L2PcInstance) activeChar;
					activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
					activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber((int) damage));
					if (activePlayer.isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() && ((L2PcInstance) target).getOlympiadGameId() == activePlayer.getOlympiadGameId())
					{
						Olympiad.notifyCompetitorDamage(activePlayer, (int) damage, activePlayer.getOlympiadGameId());
					}
				}
				if (Tpain != null && activeChar instanceof L2PcInstance)
					if (target.getPet() == null || target.getPet().isDead())
					{
						L2PcInstance activePlayer = (L2PcInstance) activeChar;
						activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
						activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber((int) damage));
					}
					else if (target.getPet().getNpcId() == 12077 || target.getPet().getNpcId() == 12311 || target.getPet().getNpcId() == 12312 || target.getPet().getNpcId() == 12313 || target.getPet().getNpcId() == 12526 || target.getPet().getNpcId() == 12527 || target.getPet().getNpcId() == 12528 || target.getPet().getNpcId() == 12564 || target.getPet().getNpcId() == 12621 || target.getPet().getNpcId() == 12780 || target.getPet().getNpcId() == 12781 || target.getPet().getNpcId() == 12782)
					{
						L2PcInstance activePlayer = (L2PcInstance) activeChar;
						activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
						activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber((int) damage));
					}
					else if (target.getPet().getNpcId() != 12077 & target.getPet().getNpcId() != 12311 & target.getPet().getNpcId() != 12312 & target.getPet().getNpcId() != 12313 & target.getPet().getNpcId() != 12526 & target.getPet().getNpcId() != 12527 & target.getPet().getNpcId() != 12528 & target.getPet().getNpcId() != 12564 & target.getPet().getNpcId() != 12621 & target.getPet().getNpcId() != 12780 & target.getPet().getNpcId() != 12781 & target.getPet().getNpcId() != 12782 & target.getPet() != null & !target.getPet().isDead())
					{
						if (Tpain.getSkill().getLevel() == 1)
						{
							L2PcInstance activePlayer = (L2PcInstance) activeChar;
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(((int) damage / 10) * 9).addNumber((int) damage / 10));
						}
						if (Tpain.getSkill().getLevel() == 2)
						{
							L2PcInstance activePlayer = (L2PcInstance) activeChar;
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(((int) damage / 10) * 8).addNumber((int) damage / 5));
						}
						if (Tpain.getSkill().getLevel() == 3)
						{
							L2PcInstance activePlayer = (L2PcInstance) activeChar;
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(((int) damage / 10) * 7).addNumber(((int) damage / 10) * 3));
						}
						if (Tpain.getSkill().getLevel() == 4)
						{
							L2PcInstance activePlayer = (L2PcInstance) activeChar;
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(((int) damage / 10) * 6).addNumber(((int) damage / 10) * 4));
						}
						if (Tpain.getSkill().getLevel() == 5)
						{
							L2PcInstance activePlayer = (L2PcInstance) activeChar;
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber((int) damage / 2).addNumber((int) damage / 2));
						}
					}
			}

			if (skillIsEvaded)
			{
				if (activeChar instanceof L2PcInstance)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
				}
				if (target instanceof L2PcInstance)
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(activeChar));
				}
			}

			Formulas.calcLethalHit(activeChar, target, skill);

			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
			{
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
			skill.getEffects(activeChar, target);
		}
	}
}
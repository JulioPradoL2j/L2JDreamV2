package com.dream.game.handler.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.ai.CtrlEvent;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2AttackableAI;
import com.dream.game.datatables.HeroSkillTable;
import com.dream.game.datatables.NobleSkillTable;
import com.dream.game.handler.ICubicSkillHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2GrandBossInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RaidBossInstance;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.skills.Stats;
import com.dream.game.skills.effects.EffectBuff;
import com.dream.game.taskmanager.tasks.CustomCancelTask;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.tools.random.Rnd;

public class Disablers implements ICubicSkillHandler
{
	static class UnSummon implements Runnable
	{
		L2Summon summonPet;
		L2PcInstance summonOwner;

		public UnSummon(L2Summon sum, L2PcInstance pc)
		{
			summonPet = sum;
			summonOwner = pc;
		}

		@Override
		public void run()
		{
			summonPet.unSummon(summonOwner);
		}
	}

	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STUN,
		L2SkillType.ROOT,
		L2SkillType.SLEEP,
		L2SkillType.CONFUSION,
		L2SkillType.AGGDAMAGE,
		L2SkillType.AGGREDUCE,
		L2SkillType.AGGREDUCE_CHAR,
		L2SkillType.AGGREMOVE,
		L2SkillType.MUTE,
		L2SkillType.FAKE_DEATH,
		L2SkillType.CONFUSE_MOB_ONLY,
		L2SkillType.NEGATE,
		L2SkillType.CANCEL,
		L2SkillType.CANCEL_DEBUFF,
		L2SkillType.PARALYZE,
		L2SkillType.UNSUMMON_ENEMY_PET,
		L2SkillType.BETRAY,
		L2SkillType.CANCEL_TARGET,
		L2SkillType.ERASE,
		L2SkillType.MAGE_BANE,
		L2SkillType.WARRIOR_BANE,
		L2SkillType.STEAL_BUFF
	};

	private static int negateEffect(L2Character target, L2SkillType type, double negateLvl, int maxRemoved)
	{
		return negateEffect(target, type, negateLvl, 0, maxRemoved);
	}

	private static int negateEffect(L2Character target, L2SkillType type, double negateLvl, int skillId, int maxRemoved)
	{
		L2Effect[] effects = target.getAllEffects();
		int count = maxRemoved <= 0 ? -2 : 0;
		for (L2Effect e : effects)
			if (negateLvl == -1)
			{
				if (e.getSkill().getSkillType() == type || e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type)
					if (skillId != 0)
					{
						if (skillId == e.getSkill().getId() && count < maxRemoved)
						{
							e.exit();
							if (count > -1)
							{
								count++;
							}
						}
					}
					else if (count < maxRemoved)
					{
						e.exit();
						if (count > -1)
						{
							count++;
						}
					}
			}
			else
			{
				boolean cancel = false;
				if (e.getSkill().getEffectType() != null && e.getSkill().getEffectAbnormalLvl() >= 0)
				{
					if (e.getSkill().getEffectType() == type && e.getSkill().getEffectAbnormalLvl() <= negateLvl)
					{
						cancel = true;
					}
				}
				else if (e.getSkill().getSkillType() == type && e.getSkill().getAbnormalLvl() <= negateLvl)
				{
					cancel = true;
				}
				if (cancel)
					if (skillId != 0)
					{
						if (skillId == e.getSkill().getId() && count < maxRemoved)
						{
							e.exit();
							if (count > -1)
							{
								count++;
							}
						}
					}
					else if (count < maxRemoved)
					{
						e.exit();
						if (count > -1)
						{
							count++;
						}
					}
			}

		return maxRemoved <= 0 ? count + 2 : count;
	}

	private static L2Effect[] sortEffects(L2Effect[] initial)
	{
		int min, index = 0;
		L2Effect pom;
		for (int i = 0; i < initial.length; i++)
		{
			min = initial[i].getSkill().getMagicLevel();
			for (int j = i; j < initial.length; j++)
				if (initial[j].getSkill().getMagicLevel() <= min)
				{
					min = initial[j].getSkill().getMagicLevel();
					index = j;
				}
			pom = initial[i];
			initial[i] = initial[index];
			initial[index] = pom;
		}
		return initial;
	}

	private static void stealEffects(L2Character stealer, L2Character stolen, List<L2Effect> stolenEffects)
	{
		for (L2Effect eff : stolenEffects)
		{
			if (eff.getPeriod() - eff.getTime() < 1)
			{
				continue;
			}

			Env env = new Env();
			env.player = stolen;
			env.target = stealer;
			env.skill = eff.getSkill();
			L2Effect e = eff.getEffectTemplate().getStolenEffect(env, eff);

			if (stealer instanceof L2PcInstance && e != null)
			{
				stealer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(eff));
			}
			eff.exit();
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Character... targets)
	{

		L2SkillType type = skill.getSkillType();

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

			switch (type)
			{
				case STUN:
				case PARALYZE:
				case ROOT:
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						skill.getEffects(activeCubic, target);
					}
					break;
				case CANCEL_DEBUFF:
					L2Effect[] effects = target.getAllEffects();

					if (effects.length == 0)
					{
						break;
					}

					int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
					for (L2Effect e : effects)
						if (e.getSkill().isDebuff() && count < skill.getMaxNegatedEffects())
							if (e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4082 && e.getSkill().getId() != 5660)
							{
								e.exit();
								if (count > -1)
								{
									count++;
								}
							}
					break;
				case AGGDAMAGE:
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						if (target instanceof L2Attackable)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeCubic.getOwner(), (int) (150 * skill.getPower() / (target.getLevel() + 7)));
						}
						skill.getEffects(activeCubic, target);
					}
					else
					{
					}
					break;
			}
		}
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		L2SkillType type = skill.getSkillType();

		boolean ss = false;
		boolean sps = false;
		boolean bss = false;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if (activeChar instanceof L2PcInstance)
			if (weaponInst == null && skill.isOffensive())
			{
				((L2PcInstance) activeChar).sendMessage(Message.getMessage((L2PcInstance) activeChar, Message.MessageId.MSG_CANNOT_REMOVE_WEAPON));
				return;
			}

		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
					if (skill.getId() != 1020)
					{
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					}
				}
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
					if (skill.getId() != 1020)
					{
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					}
				}
			}
			else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
				if (skill.getId() != 1020)
				{
					weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, true);
				}
			}
		}
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;

			if (skill.isMagic())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				}
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				}
			}
			else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
				activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
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

			if (target.isDead() || target.isInvul() || target.isPetrified())
			{
				continue;
			}

			if ((target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance) && skill.getSkillType() != L2SkillType.AGGDAMAGE)
			{
				continue;
			}
			if (activeChar.getActingPlayer() != null)
				if (target == activeChar.getActingPlayer().getPet() && type != L2SkillType.NEGATE)
				{
					continue;
				}

			if (target.isPreventedFromReceivingBuffs())
			{
				continue;
			}

			switch (type)
			{
				case CANCEL_TARGET:
				{
					if (target instanceof L2Npc)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, 50);
					}

					target.getAI().setIntention(CtrlIntention.IDLE);
					target.breakAttack();
					target.breakCast();
					target.abortAttack();
					target.abortCast();
					target.setTarget(null);
					if (activeChar instanceof L2PcInstance && Rnd.get(100) < skill.getLandingPercent())
					{
						skill.getEffects(activeChar, target);
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
					}
					break;
				}
				case BETRAY:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
					}
					break;
				}
				case UNSUMMON_ENEMY_PET:
				{
					if (target instanceof L2Summon && Rnd.get(100) < skill.getLandingPercent())
					{
						L2PcInstance targetOwner = null;
						targetOwner = ((L2Summon) target).getOwner();
						L2Summon Pet = null;
						Pet = targetOwner.getPet();
						Pet.unSummon(targetOwner);
					}
					break;
				}
				case FAKE_DEATH:
				{
					skill.getEffects(activeChar, target);
					break;
				}
				case ROOT:
				case STUN:
				{
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else if (activeChar instanceof L2PcInstance)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
					}
					break;
				}
				case SLEEP:
				case PARALYZE:
				{
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if (target instanceof L2Npc)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, 50);
					}
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else if (activeChar instanceof L2PcInstance)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
					}
					break;
				}
				case CONFUSION:
				case MUTE:
				{
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if (target instanceof L2Npc)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, 50);
					}
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						L2Effect[] effects = target.getAllEffects();
						for (L2Effect e : effects)
							if (e.getSkill().getSkillType() == type)
							{
								e.exit();
							}
						skill.getEffects(activeChar, target);
					}
					else if (activeChar instanceof L2PcInstance)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
					}
					break;
				}
				case CONFUSE_MOB_ONLY:
				{
					if (target instanceof L2Attackable)
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							L2Effect[] effects = target.getAllEffects();
							for (L2Effect e : effects)
								if (e.getSkill().getSkillType() == type)
								{
									e.exit();
								}
							skill.getEffects(activeChar, target);
						}
						else if (activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
						}
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					}
					break;
				}
				case AGGDAMAGE:
				{
					if (skill.getId() == 51 && target instanceof L2PcInstance)
					{
						target.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						break;
					}
					if (target instanceof L2GrandBossInstance)
					{
						double power = skill.getPower() / 100;
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) power);
						break;
					}
					if (target instanceof L2PcInstance && Rnd.get(100) < 75)
					{
						L2PcInstance pc = (L2PcInstance) target;
						if (pc.getPvpFlag() != 0 || pc.isInOlympiadMode() || pc.isInCombat() || pc.isInsideZone(L2Zone.FLAG_PVP))
						{
							pc.setTarget(activeChar);
							pc.abortAttack();
							pc.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
						}
					}
					if (target instanceof L2Attackable && skill.getId() != 368)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
						break;
					}
					if (target instanceof L2Attackable)
						if (skill.getId() == 368)
						{
							if (target instanceof L2PcInstance)
							{
								L2PcInstance pc = (L2PcInstance) target;
								if (pc.getPvpFlag() != 0 || pc.isInOlympiadMode() || pc.isInCombat() || pc.isInsideZone(L2Zone.FLAG_PVP))
								{
									target.setTarget(activeChar);
									target.getAI().setAutoAttacking(true);
									if (target instanceof L2PcInstance)
									{
										target.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
									}
								}
							}
							target.setTarget(activeChar);
							activeChar.stopSkillEffects(skill.getId());
							skill.getEffects(activeChar, activeChar);
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) (150 * skill.getPower() / (target.getLevel() + 7)));
						}
					break;
				}
				case AGGREDUCE:
				{
					if (target instanceof L2Attackable)
					{
						skill.getEffects(activeChar, target);

						double aggdiff = ((L2Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2Attackable) target).getHating(activeChar), target, skill);

						if (skill.getPower() > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) skill.getPower());
						}
						else if (aggdiff > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) aggdiff);
						}
					}
					break;
				}
				case AGGREDUCE_CHAR:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (target instanceof L2Attackable)
						{
							L2Attackable targ = (L2Attackable) target;
							targ.stopHating(activeChar);
							if (targ.getMostHated() == null)
							{
								if (targ.getAI() instanceof L2AttackableAI)
								{
									((L2AttackableAI) targ.getAI()).setGlobalAggro(-25);
								}
								targ.clearAggroList();
								targ.getAI().setIntention(CtrlIntention.IDLE);
								targ.getAI().setIntention(CtrlIntention.ACTIVE);
								targ.setWalking();
							}
						}
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(skill.getId()));
						}
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					}
					break;
				}
				case AGGREMOVE:
				{
					if (target instanceof L2Attackable && !target.isRaid())
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
							}
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
						}
					}
					else
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					}
					break;
				}
				case ERASE:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss) && !(target instanceof L2SiegeSummonInstance))
					{
						L2PcInstance summonOwner = null;
						L2Summon summonPet = null;
						summonOwner = ((L2Summon) target).getOwner();
						summonPet = summonOwner.getPet();
						if (summonPet != null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new UnSummon(summonPet, summonOwner), 1);
							summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
						}
					}
					else if (activeChar instanceof L2PcInstance)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
					}

					break;
				}
				case MAGE_BANE:
				{
					Vector<L2Skill> cancelledBuffs = new Vector<>();
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if (!Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
						}
						continue;
					}

					L2Effect[] effects = target.getAllEffects();
					for (L2Effect e : effects)
						if (e.getStackType() == "casting_time_down" || e.getStackType() == "ma_up")
						{
							if (Config.ALLOW_CUSTOM_CANCEL)
							{
								if (!cancelledBuffs.contains(e.getSkill()))
								{
									cancelledBuffs.add(e.getSkill());
								}
							}
							e.exit();
						}
					if (Config.ALLOW_CUSTOM_CANCEL)
					{

						if (cancelledBuffs.size() > 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new CustomCancelTask((L2PcInstance) target, cancelledBuffs), Config.CUSTOM_CANCEL_SECONDS * 1000);
						}
					}
					break;
				}

				case WARRIOR_BANE:
				{
					Vector<L2Skill> cancelledBuffs = new Vector<>();
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if (!Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
						}
						continue;
					}

					L2Effect[] effects = target.getAllEffects();
					for (L2Effect e : effects)
						if (e.getStackType() == "speed_up" || e.getStackType() == "attack_time_down")
						{
							if (Config.ALLOW_CUSTOM_CANCEL)
							{
								if (!cancelledBuffs.contains(e.getSkill()))
								{
									cancelledBuffs.add(e.getSkill());
								}
							}
							e.exit();
						}
					if (Config.ALLOW_CUSTOM_CANCEL)
					{

						if (cancelledBuffs.size() > 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new CustomCancelTask((L2PcInstance) target, cancelledBuffs), Config.CUSTOM_CANCEL_SECONDS * 1000);
						}
					}
					break;

				}
				case CANCEL_DEBUFF:
				{
					L2Effect[] effects = target.getAllEffects();

					if (effects.length == 0)
					{
						break;
					}

					int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
					for (L2Effect e : effects)
						if (e.getSkill().isDebuff() && count < skill.getMaxNegatedEffects())
							if (e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4082 && e.getSkill().getId() != 5660)
							{
								e.exit();
								if (count > -1)
								{
									count++;
								}
							}
					break;
				}
				case STEAL_BUFF:
				{
					if (!(target instanceof L2Playable))
						return;

					L2Effect[] effects = target.getAllEffects();

					if (effects == null || effects.length < 1)
						return;

					List<L2Effect> list = Arrays.asList(effects);
					Collections.reverse(list);
					list.toArray(effects);

					List<L2Effect> toSteal = new ArrayList<>();
					int count = 0;
					int lastSkill = 0;

					for (L2Effect e : effects)
					{
						if (e == null)
						{
							continue;
						}
						if (!(e instanceof EffectBuff))
						{
							continue;
						}
						if (e.getSkill().getSkillType() == L2SkillType.HEAL || e.getSkill().isToggle() || e.getSkill().isDebuff() || e.getSkill().isPotion() || e.isHerbEffect())
						{
							continue;
						}
						if (HeroSkillTable.isHeroSkill(e.getSkill().getId()) || NobleSkillTable.isNobleSkill(e.getSkill().getId()))
						{
							continue;
						}
						if (e.getSkill().getId() == lastSkill)
						{
							if (count == 0)
							{
								count = 1;
							}
							toSteal.add(e);
						}
						else if (count < skill.getPower())
						{
							toSteal.add(e);
							count++;
						}
						else
						{
							break;
						}
					}
					if (!toSteal.isEmpty())
					{
						stealEffects(activeChar, target, toSteal);
					}
					break;
				}
				case CANCEL:
				{
					boolean reflect = false;
					Vector<L2Skill> cancelledBuffs = new Vector<>();
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
						reflect = true;
					}

					if (target.getActingPlayer() == activeChar && !reflect)
					{
						continue;
					}
					if (Config.OLD_CANCEL_MODE)
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							L2Effect[] effects = target.getAllEffects();

							double max = skill.getMaxNegatedEffects();
							if (max == 0)
							{
								max = 24;
							}

							if (effects.length >= max)
							{
								effects = sortEffects(effects);
							}

							double count = 0;

							for (L2Effect e : effects)
							{
								switch (e.getSkill().getId())
								{
									case 110:
									case 111:
									case 1323:
									case 1325:
									case 4082:
									case 4215:
									case 4515:
									case 5182:
										continue;
								}

								switch (e.getSkill().getSkillType())
								{
									case BUFF:
									case HEAL_PERCENT:
									case REFLECT:
									case COMBATPOINTHEAL:
										count += 1;
										double rate = 1 - count / max;
										if (rate < 0.33)
										{
											rate = 0.33;
										}
										else if (rate > 0.95)
										{
											rate = 0.95;
										}
										if (Rnd.get(1000) < rate * 1000)
										{
											if (Config.ALLOW_CUSTOM_CANCEL)
											{
												if (!cancelledBuffs.contains(e.getSkill()))
												{
													cancelledBuffs.add(e.getSkill());
												}
											}
											e.exit();
										}
								}
								if (count == max)
								{
									break;
								}
							}
							if (Config.ALLOW_CUSTOM_CANCEL)
							{

								if (cancelledBuffs.size() > 0)
								{
									ThreadPoolManager.getInstance().scheduleGeneral(new CustomCancelTask((L2PcInstance) target, cancelledBuffs), Config.CUSTOM_CANCEL_SECONDS * 1000);
								}
							}
						}
						else if (activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
						}

					}
					else
					{
						L2Effect[] effects = target.getAllEffects();
						int max = skill.getMaxNegatedEffects();
						if (max == 0)
						{
							max = 24;
						}
						else
						{
							max = Rnd.get(skill.getMaxNegatedEffects());
							if (max == 0)
							{
								max = 1;
							}
						}
						if (effects.length < max)
						{
							max = effects.length;
						}
						for (int i = effects.length - 1; i >= 0; i--)
						{
							L2Effect e = effects[i];
							if (!e.isBuff())
							{
								continue;
							}
							switch (e.getSkill().getId())
							{
								case 110:
								case 111:
								case 1323:
								case 1325:
								case 4082:
								case 4215:
								case 4515:
								case 5182:
									continue;
							}
							if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
							{
								e.exit();
								max--;
							}
							if (max == 0)
							{
								break;
							}

						}
					}
					break;

				}
				case NEGATE:
				{
					if (skill.getNegateId() > 0)
					{
						L2Effect[] effects = target.getAllEffects();
						for (L2Effect e : effects)
							if (e.getSkill().getId() == skill.getNegateId())
							{
								e.exit();
							}
					}
					else if (skill.getId() == 2275)
					{
						negateEffect(target, L2SkillType.BUFF, skill.getNegateLvl(), skill.getNegateId(), -1);
						break;
					}
					else
					{
						int removedBuffs = skill.getMaxNegatedEffects() > 0 ? 0 : -2;

						for (String stat : skill.getNegateStats())
						{
							if (removedBuffs > skill.getMaxNegatedEffects())
							{
								break;
							}

							if (stat == "buff" || stat == "heal_percent")
							{
								int lvlmodifier = 52 + skill.getMagicLevel() * 2;
								if (skill.getMagicLevel() == 12)
								{
									lvlmodifier = Experience.MAX_LEVEL - 1;
								}
								int landrate = 90;
								if (target.getLevel() - lvlmodifier > 0)
								{
									landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
								}

								landrate = (int) activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);

								if (Rnd.get(100) < landrate)
								{
									removedBuffs += negateEffect(target, L2SkillType.BUFF, -1, skill.getMaxNegatedEffects());
								}
							}

							else if (stat == "debuff" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.DEBUFF, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "weakness" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.WEAKNESS, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "stun" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.STUN, -1, skill.getMaxNegatedEffects());
							}
							if (stat == "sleep" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.SLEEP, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "confusion" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.CONFUSION, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "mute" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.MUTE, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "fear" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.FEAR, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "poison" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.POISON, skill.getNegateLvl(), skill.getMaxNegatedEffects());
							}
							else if (stat == "bleed" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.BLEED, skill.getNegateLvl(), skill.getMaxNegatedEffects());
							}
							else if (stat == "paralyze" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.PARALYZE, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "root" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.ROOT, -1, skill.getMaxNegatedEffects());
							}
							else if (stat == "death_mark" && removedBuffs < skill.getMaxNegatedEffects())
							{
								removedBuffs += negateEffect(target, L2SkillType.DEATH_MARK, skill.getNegateLvl(), skill.getMaxNegatedEffects());
							}
							else if (stat == "heal" && removedBuffs < skill.getMaxNegatedEffects())
							{
								SkillHandler.getInstance().getSkillHandler(L2SkillType.HEAL).useSkill(activeChar, skill, target);
							}
						}
					}
				}
				default:
					return;
			}

			Formulas.calcLethalHit(activeChar, target, skill);

		}
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);
	}

}
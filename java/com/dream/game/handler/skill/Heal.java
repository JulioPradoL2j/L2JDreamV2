package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2GuardInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.templates.skills.L2SkillType;

public class Heal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HEAL,
		L2SkillType.HEAL_PERCENT,
		L2SkillType.HEAL_STATIC,
		L2SkillType.HEAL_MOB
	};
	private L2Summon activeSummon;

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF).useSkill(activeChar, skill, targets);

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2PcInstance player = null;
		boolean consumeSoul = true;

		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}

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
			if (Math.abs(target.getZ() - activeChar.getZ()) > 200)
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
			if ((target instanceof L2Boss || target instanceof L2GuardInstance) && activeChar.getActingPlayer() != null)
			{
				activeChar.getActingPlayer().updatePvPStatus();
			}
			double mAtk = activeChar.getMAtk(target, skill);
			double hp = skill.getPower() + (mAtk / 50);

			if (skill.getSkillType() == L2SkillType.HEAL_PERCENT)
			{
				hp = target.getMaxHp() * hp / 100.0;
			}
			else if (skill.getSkillType() != L2SkillType.HEAL_STATIC)
				if (weaponInst != null)
				{
					if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					{
						hp *= 1.5;
						if (consumeSoul)
						{
							weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						}
						consumeSoul = false;
					}
					else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					{
						hp *= 1.3;
						if (consumeSoul)
						{
							weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						}
						consumeSoul = false;
					}
				}
				else if (activeChar instanceof L2Summon)
				{
					activeSummon = (L2Summon) activeChar;
					if (activeSummon != null)
						if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
						{
							hp *= 1.5;
							if (consumeSoul)
							{
								activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
							}
							consumeSoul = false;
						}
						else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
						{
							hp *= 1.3;
							if (consumeSoul)
							{
								activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
							}
							consumeSoul = false;
						}
				}
				else if (activeChar instanceof L2Npc)
					if (((L2Npc) activeChar).isUsingShot(false))
					{
						hp *= 1.5;
					}

			if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
			{
				hp = 0;
			}
			else if (skill.getSkillType() == L2SkillType.HEAL_STATIC)
			{
				hp = skill.getPower();
			}
			else if (skill.getSkillType() != L2SkillType.HEAL_PERCENT)
			{
				hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
				hp *= activeChar.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) / 100;
				if (!skill.isPotion())
				{
					hp += target.calcStat(Stats.HEAL_STATIC_BONUS, 0, null, null);
				}
			}

			int msg1 = (int) hp;
			if (target.getStatus().getCurrentHp() + hp >= target.getMaxHp())
			{
				hp = target.getMaxHp() - target.getStatus().getCurrentHp();
			}

			if (hp > 0)
			{
				target.getStatus().increaseHp(hp);

				target.setLastHealAmount((int) hp);
				StatusUpdate su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_HP, (int) target.getStatus().getCurrentHp());
				target.sendPacket(su);
				L2PcInstance pc = target.getActingPlayer();
				if (pc != null && pc.getPvpFlag() > 0 && activeChar instanceof L2PcInstance)
				{
					((L2PcInstance) activeChar).updatePvPStatus();
				}
			}

			if (target instanceof L2PcInstance && skill.getSkillType() != L2SkillType.HEAL_PERCENT)
				if (skill.getId() == 4051)
				{
					target.sendPacket(SystemMessageId.REJUVENATING_HP);
				}
				else if (activeChar instanceof L2PcInstance && activeChar != target)
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(msg1));
				}
				else
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber(msg1));
				}
		}
	}
}
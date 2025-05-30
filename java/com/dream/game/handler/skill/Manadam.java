package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ClanHallManagerInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2SkillType;

public class Manadam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MANADAM
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

			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
			{
				target = activeChar;
			}

			boolean acted = Formulas.calcMagicAffected(activeChar, target, skill);
			if (!acted || target.isInvul() || target.isPetrified())
			{
				activeChar.sendPacket(SystemMessageId.MISSED_TARGET);
			}
			else if (target instanceof L2ClanHallManagerInstance)
			{
				activeChar.sendPacket(SystemMessageId.MISSED_TARGET);
			}
			else
			{
				double damage = Formulas.calcManaDam(activeChar, target, skill, ss, bss);

				double mp = damage > target.getStatus().getCurrentMp() ? target.getStatus().getCurrentMp() : damage;
				target.reduceCurrentMp(mp);
				if (damage > 0)
				{
					if (target.isSleeping())
					{
						target.stopSleeping(null);
					}
					if (target.isImmobileUntilAttacked())
					{
						target.stopImmobileUntilAttacked(null);
					}
				}

				if (target instanceof L2PcInstance)
				{
					StatusUpdate sump = new StatusUpdate(target);
					sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getStatus().getCurrentMp());
					target.sendPacket(sump);
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_S1).addCharName(activeChar).addNumber((int) mp));
				}

				if (activeChar instanceof L2PcInstance)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1).addNumber((int) mp));
				}
			}
		}
	}
}
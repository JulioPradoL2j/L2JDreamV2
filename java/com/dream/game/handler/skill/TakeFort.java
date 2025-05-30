package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.manager.FortManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;

public class TakeFort implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.TAKEFORT
	};

	public static boolean checkIfOkToCastFlagDisplay(L2Character activeChar, boolean isCheckOnly, L2Skill skill, L2Object target)
	{
		return checkIfOkToCastFlagDisplay(activeChar, FortManager.getInstance().getFort(activeChar), isCheckOnly, skill, target);
	}

	public static boolean checkIfOkToCastFlagDisplay(L2Character activeChar, Fort fort, boolean isCheckOnly, L2Skill skill, L2Object target)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm;
		L2PcInstance player = (L2PcInstance) activeChar;

		if (fort == null || fort.getFortId() <= 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (!Util.checkIfInRange(200, player, player.getTarget(), true))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else
		{
			if (!isCheckOnly)
			{
				fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), player.getClan().getName() + " trying to capture Fort!");
			}
			return true;
		}

		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}

		return false;
	}

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

		L2Object target = player.getTarget();

		if (player.getClan() == null)
			return;

		if (target == null)
			return;

		Fort fort = FortManager.getInstance().getFort(player);
		if (fort == null || !checkIfOkToCastFlagDisplay(player, fort, true, skill, target))
			return;

		fort.endOfSiege(player.getClan());
	}
}
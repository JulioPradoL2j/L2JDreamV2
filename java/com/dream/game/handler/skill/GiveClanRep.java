package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.templates.skills.L2SkillType;

public class GiveClanRep implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GIVE_CLANREP
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

		if (!((L2PcInstance) activeChar).isClanLeader())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.sendMessage("Dear " + activeChar.getName() + " You're not a Clan leader, only clan leaders can it!");
			return;		
}

		if (player.isClanLeader())
		{
			L2Clan clan = player.getClan();
			if (clan == null)
		{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			int repToAdd = (int) skill.getPower();
			clan.setReputationScore(clan.getReputationScore() + repToAdd, true);
		}
	}
	}
}
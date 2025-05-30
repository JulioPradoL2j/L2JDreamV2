package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.manager.RecipeController;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.templates.skills.L2SkillType;

public class Craft implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.COMMON_CRAFT,
		L2SkillType.DWARVEN_CRAFT
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

		if (player.getPrivateStoreType() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING);
			return;
		}
		RecipeController.getInstance().requestBookOpen(player, skill.getSkillType() == L2SkillType.DWARVEN_CRAFT);
	}
}
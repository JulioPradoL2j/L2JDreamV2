package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.tools.random.Rnd;

public class GetPlayer implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GET_PLAYER
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

		for (L2Object target : targets)
			if (target instanceof L2PcInstance)
			{
				L2PcInstance trg = (L2PcInstance) target;

				if (trg.isAlikeDead())
				{
					continue;
				}

				trg.getPosition().setXYZ(activeChar.getX() + Rnd.get(-10, 10), activeChar.getY() + Rnd.get(-10, 10), activeChar.getZ());
				trg.stopMove(null);
				trg.sendPacket(new ValidateLocation(trg));
			}
	}
}
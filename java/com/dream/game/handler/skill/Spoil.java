package com.dream.game.handler.skill;

import com.dream.game.ai.CtrlEvent;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2SkillType;

public class Spoil implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SPOIL
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

		for (L2Character element : targets)
		{
			if (!(element instanceof L2MonsterInstance))
			{
				continue;
			}

			L2MonsterInstance target = (L2MonsterInstance) element;

			if (target.isSpoil())
			{
				activeChar.sendPacket(SystemMessageId.ALREADY_SPOILED);
				continue;
			}

			boolean spoil = false;
			if (!target.isDead())
			{
				spoil = Formulas.calcMagicSuccess(activeChar, target, skill);

				if (spoil)
				{
					target.setSpoil(true);
					target.setIsSpoiledBy(activeChar.getObjectId());
					activeChar.sendPacket(SystemMessageId.SPOIL_SUCCESS);
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(skill));
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
			}
		}
	}
}
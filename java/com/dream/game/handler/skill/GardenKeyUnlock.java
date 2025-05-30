package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;

public class GardenKeyUnlock implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GARDEN_KEY_UNLOCK
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

		L2Object[] targetList = skill.getTargetList(activeChar);

		if (targetList == null)
			return;

		L2DoorInstance door = (L2DoorInstance) targetList[0];
		switch (skill.getId())
		{
			case 9703:
				if (door.getDoorId() == 16200002)
				{
					door.openMe();
				}
				break;
			case 9704:
				if (door.getDoorId() == 16200005)
				{
					door.openMe();
				}
				break;
			case 9705:
				if (door.getDoorId() == 16200009)
				{
					door.openMe();
				}
				break;
			case 9706:
				if (door.getDoorId() == 16200003)
				{
					door.openMe();
				}
				break;
			case 9707:
				if (door.getDoorId() == 16200007)
				{
					door.openMe();
				}
				break;
			case 9708:
				if (door.getDoorId() == 16200008)
				{
					door.openMe();
				}
				break;
			case 9709:
				if (door.getDoorId() == 16200010)
				{
					door.openMe();
				}
				break;
			case 9710:
				if (door.getDoorId() == 16200006)
				{
					door.openMe();
				}
				break;
			case 9711:
				if (door.getDoorId() == 16200011)
				{
					door.openMe();
				}
				break;
			case 9712:
				if (door.getDoorId() == 16200012)
				{
					door.openMe();
				}
				break;
		}
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(skill.getId() + 7360));
	}
}
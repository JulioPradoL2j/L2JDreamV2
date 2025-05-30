package com.dream.game.handler.skill;

import com.dream.Message;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.tools.random.Rnd;

public class Unlock implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.UNLOCK
	};

	private static final int[] UnlocedDoors =
	{
		20260001,
		20260002,
		20260005,
		20260006
	};

	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
			if (element == obj)
				return true;
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
		for (L2Character element : targets)
			if (element instanceof L2DoorInstance)
			{
				L2DoorInstance door = (L2DoorInstance) element;
				if (!door.isUnlockable() || door.getFort() != null || contains(UnlocedDoors, door.getDoorId()))
				{
					activeChar.sendPacket(SystemMessageId.UNABLE_TO_UNLOCK_DOOR);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (!door.getOpen() && Formulas.calculateUnlockChance(skill))
				{
					door.openMe();
					door.onOpen();
					if (activeChar instanceof L2PcInstance)
					{
						((L2PcInstance) activeChar).sendMessage(Message.getMessage((L2PcInstance) activeChar, Message.MessageId.MSG_DOOR_OPEN));
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.FAILED_TO_UNLOCK_DOOR);
				}
			}
			else if (element instanceof L2ChestInstance)
			{
				L2ChestInstance chest = (L2ChestInstance) element;
				if (chest.getStatus().getCurrentHp() <= 0 || chest.isInteracted())
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				int chestChance = 0;
				int chestGroup = 0;
				int chestTrapLimit = 0;

				if (chest.getLevel() > 60)
				{
					chestGroup = 4;
				}
				else if (chest.getLevel() > 40)
				{
					chestGroup = 3;
				}
				else if (chest.getLevel() > 30)
				{
					chestGroup = 2;
				}
				else
				{
					chestGroup = 1;
				}

				switch (chestGroup)
				{
					case 1:
						if (skill.getLevel() > 10)
						{
							chestChance = 100;
						}
						else if (skill.getLevel() >= 3)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 2)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 1)
						{
							chestChance = 40;
						}
						chestTrapLimit = 10;
						break;
					case 2:
						if (skill.getLevel() > 12)
						{
							chestChance = 100;
						}
						else if (skill.getLevel() >= 7)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 6)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 5)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 4)
						{
							chestChance = 35;
						}
						else if (skill.getLevel() == 3)
						{
							chestChance = 30;
						}
						chestTrapLimit = 30;
						break;
					case 3:
						if (skill.getLevel() >= 14)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 13)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 12)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 11)
						{
							chestChance = 35;
						}
						else if (skill.getLevel() == 10)
						{
							chestChance = 30;
						}
						else if (skill.getLevel() == 9)
						{
							chestChance = 25;
						}
						else if (skill.getLevel() == 8)
						{
							chestChance = 20;
						}
						else if (skill.getLevel() == 7)
						{
							chestChance = 15;
						}
						else if (skill.getLevel() == 6)
						{
							chestChance = 10;
						}
						chestTrapLimit = 50;
						break;
					case 4:
						if (skill.getLevel() >= 14)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 13)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 12)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 11)
						{
							chestChance = 35;
						}
						chestTrapLimit = 80;
						break;
				}
				if (Rnd.get(100) <= chestChance)
				{
					activeChar.broadcastPacket(new SocialAction(activeChar, 3));
					chest.setSpecialDrop();
					chest.setMustRewardExpSp(false);
					chest.setInteracted();
					chest.reduceCurrentHp(chest.getMaxHp(), activeChar, skill);
				}
				else
				{
					activeChar.broadcastPacket(new SocialAction(activeChar, 13));
					if (Rnd.get(100) < chestTrapLimit)
					{
						chest.chestTrap(activeChar);
					}
					chest.setInteracted();
					chest.addDamageHate(activeChar, 0, 1);
					chest.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
				}
			}
	}
}
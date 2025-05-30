package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;

public class Sweep implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SWEEP
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
		InventoryUpdate iu = new InventoryUpdate();
		boolean send = false;

		if (skill.hasEffects())
			skill.getEffectsSelf(activeChar);

		for (L2Character element : targets)
		{
			if (!(element instanceof L2Attackable))
			{
				continue;
			}

			L2Attackable target = (L2Attackable) element;

			L2Attackable.RewardItem[] items = null;
			boolean isSweeping = false;
			synchronized (target)
			{
				if (target.isSweepActive())
				{
					items = target.takeSweep();
					isSweeping = true;
				}
			}
			if (isSweeping == true)
			{
				if (items == null || items.length == 0)
				{
					continue;
				}

				for (L2Attackable.RewardItem ritem : items)
					if (player.isInParty())
					{
						player.getParty().distributeItem(player, ritem, true, target);
					}
					else if (player.getInventory().validateCapacityByItemId(ritem.getItemId()))
					{
						L2ItemInstance item = player.getInventory().addItem("Sweep", ritem.getItemId(), ritem.getCount(), player, target);
						if (iu != null)
						{
							iu.addItem(item);
						}
						send = true;
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2).addItemName(item).addNumber(ritem.getCount()));
					}
			}
			else if ((target.isDead() == true) && (isSweeping == false))
			{
				target.endDecayTask();
			}
			if (send)
				if (iu != null)
				{
					player.sendPacket(iu);
				}
				else
				{
					player.sendPacket(new ItemList(player, false));
				}
		}
	}
}
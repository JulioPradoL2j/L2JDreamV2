package com.dream.game.handler.item;

import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;

public class PetFood implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		2515,
		4038,
		5168,
		5169,
		6316,
		7582
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	public boolean useFood(L2Playable activeChar, int magicId, L2ItemInstance item)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, 1);

		if (skill != null)
			if (activeChar instanceof L2PetInstance)
			{
				boolean eatSuccess = false;
				if (((L2PetInstance) activeChar).destroyItem("Consume", item.getObjectId(), 1, null, false))
				{
					eatSuccess = true;
				}
				else if (((L2PetInstance) activeChar).getOwner().destroyItem("Consume", item.getObjectId(), 1, null, false))
				{
					eatSuccess = true;
				}
				if (eatSuccess == true)
				{
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0, false));
					((L2PetInstance) activeChar).setCurrentFed(((L2PetInstance) activeChar).getCurrentFed() + skill.getFeed());
					activeChar.broadcastStatusUpdate();
					if (((L2PetInstance) activeChar).getCurrentFed() < 0.55 * ((L2PetInstance) activeChar).getPetData().getPetMaxFeed())
					{
						((L2PetInstance) activeChar).getOwner().sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
					}
					return true;
				}
			}
			else if (activeChar instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar;
				int itemId = item.getItemId();
				if (player.isMounted())
				{
					int petId = player.getMountNpcId();

					if (PetDataTable.isPetFood(petId, itemId))
					{
						if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
						{
							player.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0, false));
							player.setCurrentFeed(player.getCurrentFeed() + skill.getFeed());
						}
						return true;
					}
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
					return false;
				}
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
				return false;
			}
		return false;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		int itemId = item.getItemId();
		switch (itemId)
		{
			case 2515: // Wolf's food
				useFood(playable, 2048, item);
				break;
			case 4038: // Hatchling's food
				useFood(playable, 2063, item);
				break;
			case 5168: // Strider's food
				useFood(playable, 2101, item);
				break;
			case 5169: // ClanHall / Castle Strider's food
				useFood(playable, 2102, item);
				break;
			case 6316: // Wyvern's food
				useFood(playable, 2180, item);
				break;
			case 7582: // Baby Pet's food
				useFood(playable, 2048, item);
				break;
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
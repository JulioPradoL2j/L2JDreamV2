package com.dream.game.handler.skill;

import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable.RewardItem;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.tools.random.Rnd;

public class Harvest implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HARVEST
	};

	private static int getPenalty(L2PcInstance activePlayer, L2MonsterInstance target)
	{
		int basicSuccess = 100;
		int levelPlayer = activePlayer.getLevel();
		int levelTarget = target.getLevel();

		int diff = levelPlayer - levelTarget;
		if (diff < 0)
		{
			diff = -diff;
		}

		if (diff > 5)
		{
			basicSuccess -= (diff - 5) * 5;
		}

		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}
		return basicSuccess;
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

		L2PcInstance activePlayer = (L2PcInstance) activeChar;

		InventoryUpdate iu = new InventoryUpdate();

		for (L2Character element : targets)
		{
			if (!(element instanceof L2MonsterInstance))
			{
				continue;
			}

			L2MonsterInstance target = (L2MonsterInstance) element;

			if (activePlayer != target.getSeeder())
			{
				activePlayer.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
				continue;
			}

			boolean send = false;
			int total = 0;
			int cropId = 0;

			if (target.isSeeded())
			{
				int penalty = getPenalty(activePlayer, target);
				if (Rnd.nextInt(99) < penalty)
				{
					RewardItem[] items = target.takeHarvest();
					if (items != null && items.length > 0)
					{
						for (RewardItem ritem : items)
						{
							cropId = ritem.getItemId();
							ritem.setCount((int) ((double) ritem.getCount() / 100 * penalty));
							if (activePlayer.isInParty())
							{
								activePlayer.getParty().distributeItem(activePlayer, ritem, true, target);
							}
							else
							{
								L2ItemInstance item = activePlayer.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), activePlayer, target);
								if (iu != null)
								{
									iu.addItem(item);
								}
								send = true;
								total += ritem.getCount();
							}
						}
						if (send)
						{
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2).addItemName(cropId).addNumber(total));
							if (activePlayer.getParty() != null)
							{
								activePlayer.getParty().broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HARVESTED_S3_S2S).addString(activeChar.getName()).addNumber(total).addItemName(cropId));
							}

							if (iu != null)
							{
								activePlayer.sendPacket(iu);
							}
							else
							{
								activePlayer.sendPacket(new ItemList(activePlayer, false));
							}
						}
					}
				}
				else
				{
					activePlayer.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
				}
			}
			else
			{
				activePlayer.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
			}
		}
	}
}
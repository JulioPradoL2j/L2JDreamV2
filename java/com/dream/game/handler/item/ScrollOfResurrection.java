package com.dream.game.handler.item;

import com.dream.Message;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class ScrollOfResurrection implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		737,
		3936,
		3959,
		6387,
		9157
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		if (activeChar.isMovementDisabled())
			return;

		int itemId = item.getItemId();
		boolean humanScroll = itemId == 3936 || itemId == 3959 || itemId == 737 || itemId == 9157;
		boolean petScroll = itemId == 6387 || itemId == 737;

		L2Object object = activeChar.getTarget();
		if (object != null && object instanceof L2Character)
		{
			L2Character target = (L2Character) object;

			if (target.isDead())
			{
				L2PcInstance targetPlayer = null;

				if (target instanceof L2PcInstance)
				{
					targetPlayer = (L2PcInstance) target;
				}

				L2PetInstance targetPet = null;

				if (target instanceof L2PetInstance)
				{
					targetPet = (L2PetInstance) target;
				}

				if (targetPlayer != null || targetPet != null)
				{
					boolean condGood = true;

					Siege siege = null;
					FortSiege fsiege = null;
					if (targetPlayer != null)
					{
						siege = SiegeManager.getSiege(targetPlayer);
						fsiege = FortSiegeManager.getSiege(targetPlayer);
					}
					else
					{
						siege = SiegeManager.getSiege(targetPet);
						fsiege = FortSiegeManager.getSiege(targetPet);
					}

					if (siege != null && siege.getIsInProgress() || fsiege != null && fsiege.getIsInProgress())
					{
						condGood = false;
						activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
					}

					siege = null;

					if (targetPet != null)
					{
						if (targetPet.getOwner().isPetReviveRequested())
						{
							activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
							condGood = false;
						}
						else if (!petScroll && targetPet.getOwner() != activeChar)
						{
							condGood = false;
							activeChar.sendPacket(SystemMessageId.INCORRECT_ITEM);
						}
					}
					else if (targetPlayer != null && targetPlayer.isFestivalParticipant())
					{
						condGood = false;
						activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_CANNOT_USE), "SOR"));
					}
					else if (targetPlayer != null && targetPlayer.isReviveRequested())
					{
						activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
						condGood = false;
					}
					else if (!humanScroll)
					{
						condGood = false;
						activeChar.sendPacket(SystemMessageId.INCORRECT_ITEM);
					}

					if (condGood && !activeChar.isMuted())
					{
						if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
							return;

						int skillId = 0;
						int skillLevel = 1;

						switch (itemId)
						{
							case 737:
								skillId = 2014;
								break;
							case 3936:
								skillId = 2049;
								break;
							case 3959:
								skillId = 2062;
								break;
							case 6387:
								skillId = 2179;
								break;
							case 9157:
								skillId = 2321;
								break;
						}

						if (skillId != 0)
						{
							activeChar.useMagic(SkillTable.getInstance().getInfo(skillId, skillLevel), true, true);
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item));
						}
					}
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
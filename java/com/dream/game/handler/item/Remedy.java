package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.templates.skills.L2SkillType;

public class Remedy implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		1831,
		1832,
		1833,
		1834,
		3889
	};

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if (playable instanceof L2PetInstance)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		else
			return;

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

		int itemId = item.getItemId();
		if (itemId == 1831) // antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			activeChar.sendPacket(new MagicSkillUse(playable, playable, 2042, 1, 0, 0, false));
			activeChar.broadcastPacket(new MagicSkillUse(playable, playable, 2042, 1, 0, 0, false));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1832) // advanced antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			activeChar.sendPacket(new MagicSkillUse(playable, playable, 2043, 1, 0, 0, false));
			activeChar.broadcastPacket(new MagicSkillUse(playable, playable, 2043, 1, 0, 0, false));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1833) // bandage
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			activeChar.sendPacket(new MagicSkillUse(playable, playable, 34, 1, 0, 0, false));
			activeChar.broadcastPacket(new MagicSkillUse(playable, playable, 34, 1, 0, 0, false));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1834) // emergency dressing
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			activeChar.sendPacket(new MagicSkillUse(playable, playable, 2045, 1, 0, 0, false));
			activeChar.broadcastPacket(new MagicSkillUse(playable, playable, 2045, 1, 0, 0, false));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 3889) // potion of recovery
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
				if (e.getSkill().getId() == 4082)
				{
					e.exit();
				}
			activeChar.setIsImmobilized(false);
			if (activeChar.getFirstEffect(L2EffectType.ROOT) == null)
			{
				activeChar.stopRooting(null);
			}
			activeChar.sendPacket(new MagicSkillUse(playable, playable, 2042, 1, 0, 0, false));
			activeChar.broadcastPacket(new MagicSkillUse(playable, playable, 2042, 1, 0, 0, false));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
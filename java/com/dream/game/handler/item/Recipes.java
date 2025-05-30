package com.dream.game.handler.item;

import com.dream.Message;
import com.dream.game.handler.IItemHandler;
import com.dream.game.manager.RecipeController;
import com.dream.game.model.L2RecipeList;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler
{
	private final int[] ITEM_IDS;

	public Recipes()
	{
		ITEM_IDS = RecipeController.getInstance().getAllItemIds();
	}

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
		L2RecipeList rp = RecipeController.getInstance().getRecipeByItemId(item.getItemId());
		if (activeChar.hasRecipeList(rp.getId()))
		{
			activeChar.sendPacket(SystemMessageId.RECIPE_ALREADY_REGISTERED);
		}
		else if (rp.isDwarvenRecipe())
		{
			if (activeChar.hasDwarvenCraft())
			{
				if (rp.getLevel() > activeChar.getDwarvenCraft())
				{
					activeChar.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
				}
				else if (activeChar.getDwarvenRecipeBook().length >= activeChar.getDwarfRecipeLimit())
				{
					// Up to $s1 recipes can be registered.
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.getDwarfRecipeLimit()));
				}
				else
				{
					activeChar.registerDwarvenRecipeList(rp);
					activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
					activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_RECIPE_ADD_DWARV), item.getItemName()));
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
			}
		}
		else if (activeChar.hasCommonCraft())
		{
			if (rp.getLevel() > activeChar.getCommonCraft())
			{
				activeChar.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
			}
			else if (activeChar.getCommonRecipeBook().length >= activeChar.getCommonRecipeLimit())
			{
				// Up to $s1 recipes can be registered.
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.getCommonRecipeLimit()));
			}
			else
			{
				activeChar.registerCommonRecipeList(rp);
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_RECIPE_ADD_COMMON), item.getItemName()));
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
		}
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
	}
}
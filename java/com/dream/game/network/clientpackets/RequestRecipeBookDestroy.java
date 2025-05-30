package com.dream.game.network.clientpackets;

import com.dream.game.manager.RecipeController;
import com.dream.game.model.L2RecipeList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.RecipeBookItemList;
import com.dream.game.network.serverpackets.SystemMessage;

public class RequestRecipeBookDestroy extends L2GameClientPacket
{
	private int _recipeId;

	@Override
	protected void readImpl()
	{
		_recipeId = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
			return;
		}

		final L2RecipeList rp = RecipeController.getInstance().getRecipeList(_recipeId);
		if (rp == null)
			return;

		activeChar.unregisterRecipeList(_recipeId);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED).addItemName(_recipeId));

		RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), activeChar.getMaxMp());
		if (rp.isDwarvenRecipe())
		{
			response.addRecipes(activeChar.getDwarvenRecipeBook());
		}
		else
		{
			response.addRecipes(activeChar.getCommonRecipeBook());
		}

		activeChar.sendPacket(response);
	}

}
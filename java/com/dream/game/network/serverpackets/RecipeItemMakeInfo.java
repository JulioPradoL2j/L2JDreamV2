package com.dream.game.network.serverpackets;

import com.dream.Config;
import com.dream.game.manager.RecipeController;
import com.dream.game.model.L2RecipeList;
import com.dream.game.model.actor.instance.L2PcInstance;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private final int _id;
	private final L2PcInstance _activeChar;
	private final boolean _success;

	public RecipeItemMakeInfo(int id, L2PcInstance player)
	{
		_id = id;
		_activeChar = player;
		_success = true;
	}

	public RecipeItemMakeInfo(int id, L2PcInstance player, boolean success)
	{
		_id = id;
		_activeChar = player;
		_success = success;
	}

	@Override
	protected final void writeImpl()
	{
		L2RecipeList recipe = RecipeController.getInstance().getRecipeList(_id);

		if (recipe != null)
		{
			writeC(0xD7);

			writeD(_id);
			writeD(recipe.isDwarvenRecipe() ? 0 : 1);
			writeD((int) _activeChar.getStatus().getCurrentMp());
			writeD(_activeChar.getMaxMp());
			writeD(_success ? 1 : 0);
		}
		else if (Config.DEBUG)
		{
			_log.info("No recipe found with ID = " + _id);
		}
	}

}
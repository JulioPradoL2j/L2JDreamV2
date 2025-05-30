package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.RecipeShopItemInfo;

public class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private int _playerObjectId, _recipeId;

	@Override
	protected void readImpl()
	{
		_playerObjectId = readD();
		_recipeId = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		final L2PcInstance shop = L2World.getInstance().getPlayer(_playerObjectId);
		if (shop == null || shop.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_MANUFACTURE)
			return;

		player.sendPacket(new RecipeShopItemInfo(shop, _recipeId));
	}

}
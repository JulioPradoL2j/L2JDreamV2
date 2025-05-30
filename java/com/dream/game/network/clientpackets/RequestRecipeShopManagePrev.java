package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.RecipeShopSellList;

public class RequestRecipeShopManagePrev extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.getTarget() == null)
			return;

		if (player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!(player.getTarget() instanceof L2PcInstance))
			return;
		player.sendPacket(new RecipeShopSellList(player, (L2PcInstance) player.getTarget()));
	}

}
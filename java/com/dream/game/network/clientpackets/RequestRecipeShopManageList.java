package com.dream.game.network.clientpackets;

import com.dream.game.model.L2ManufactureList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2TradeZone;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.RecipeShopManageList;

public class RequestRecipeShopManageList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		player.revalidateZone(true);
		L2TradeZone z = (L2TradeZone) player.getZone("Trade");
		if (z != null && !z.canCrfat())
		{
			player.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isInsideZone(L2Zone.FLAG_NOSTORE))
		{
			player.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getPrivateStoreType() != 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			if (player.isSitting())
			{
				player.standUp();
			}
		}

		if (player.getCreateList() == null)
		{
			player.setCreateList(new L2ManufactureList());
		}

		player.sendPacket(new RecipeShopManageList(player, true));
	}

}
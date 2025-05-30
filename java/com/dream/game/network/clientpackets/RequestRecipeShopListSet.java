package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.model.L2ManufactureItem;
import com.dream.game.model.L2ManufactureList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.RecipeShopMsg;

public class RequestRecipeShopListSet extends L2GameClientPacket
{
	private int _count;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_count = readD();
		if (_count < 0 || _count * 8 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
		}
		_items = new int[_count * 2];
		for (int x = 0; x < _count; x++)
		{
			int recipeID = readD();
			_items[x * 2] = recipeID;
			int cost = readD();
			_items[x * 2 + 1] = cost;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (player.isOfflineTrade())
			return;
		if (player.isInDuel())
		{
			player.sendPacket(SystemMessageId.CANT_CRAFT_DURING_COMBAT);
			return;
		}
		player.stopMove();
		if (player.isInsideZone(L2Zone.FLAG_NOSTORE))
		{
			player.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (_count > 20)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (_count == 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			player.standUp();
		}
		else
		{
			L2ManufactureList createList = new L2ManufactureList();

			for (int x = 0; x < _count; x++)
			{
				int recipeID = _items[x * 2];
				int cost = _items[x * 2 + 1];
				createList.add(new L2ManufactureItem(recipeID, cost));
			}
			createList.setStoreName(player.getCreateList() != null ? player.getCreateList().getStoreName() : "");
			player.setCreateList(createList);

			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_MANUFACTURE);
			player.sitDown();
			player.broadcastUserInfo();
			player.broadcastPacket(new RecipeShopMsg(player));
		}
	}

}
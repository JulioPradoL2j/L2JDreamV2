package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.cache.HtmCache;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2FishermanInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MerchantInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.util.Util;

public class RequestSellItem extends L2GameClientPacket
{
	private int _listId, _count;
	private int[] _items;

	protected void processSell()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}

		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return;

		L2Object target = player.getTarget();
		if (!player.isGM() && (target == null || !(target instanceof L2MerchantInstance) || !player.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false)))
			return;

		boolean ok = true;
		String htmlFolder = "";

		if (target != null)
		{
			if (target instanceof L2MerchantInstance)
			{
				htmlFolder = "merchant";
			}
			else if (target instanceof L2FishermanInstance)
			{
				htmlFolder = "fisherman";
			}
			else
			{
				ok = false;
			}
		}
		else
		{
			ok = false;
		}

		L2Npc merchant = null;

		if (ok)
		{
			merchant = (L2Npc) target;
		}

		if (merchant != null && _listId > 1000000)
			if (merchant.getTemplate().getNpcId() != _listId - 1000000)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

		long totalPrice = 0;

		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 3];
			int count = _items[i * 3 + 2];

			if (count < 0)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}

			L2ItemInstance item = player.checkItemManipulation(objectId, count, "sell");
			if (item == null || !item.isSellable())
			{
				continue;
			}
			if (Config.SET_ETCITEM_MAX_SELL)
			{
				if (count > Config.SET_ETCITEM_MAX_SELL_VALUE)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				continue;
				}
			}

			if (Config.L2OFF_ADENA_PROTECTION)
			if ((player.getAdena() + (int) totalPrice) > 2000000000 || (int) totalPrice > 2000000000)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
				break;
			}

			totalPrice += item.getReferencePrice() * count / 2;
			if (totalPrice > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}

			item = player.getInventory().destroyItem("Sell", objectId, count, player, null);
		}
		player.addAdena("Sell", (int) totalPrice, merchant, true);

		if (merchant != null)
		{
			String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");

			if (html != null)
			{
				NpcHtmlMessage soldMsg = new NpcHtmlMessage(merchant.getObjectId());
				soldMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
				player.sendPacket(soldMsg);
			}
		}

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
		player.getInventory().updateDatabase();
	}

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if (_count <= 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			_items[i * 3] = objectId;
			int itemId = readD();
			_items[i * 3 + 1] = itemId;
			long cnt = readD();
			if (cnt > Integer.MAX_VALUE || cnt <= 0)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 3 + 2] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		processSell();
	}

}
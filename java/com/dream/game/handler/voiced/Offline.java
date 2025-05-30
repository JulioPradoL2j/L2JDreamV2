package com.dream.game.handler.voiced;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class Offline implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"offline"
	};

	public boolean cantEnable(L2PcInstance activeChar)
	{
		if (activeChar.getActiveEnchantItem() != null)
			return true;
		if (activeChar.getPrivateStoreType() != 5 && !activeChar.inTradeZone())
			return true;
		return false;
	}

	@Override
	public String getDescription(String command)
	{
		if (command.equals("offline"))
			return "Enables Offline Trade/Craft service.";
		return null;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
			return false;

		if (command.startsWith("offline"))
		{
			int StoreType = activeChar.getPrivateStoreType();

			if (StoreType == L2PcInstance.STORE_PRIVATE_BUY || StoreType == L2PcInstance.STORE_PRIVATE_SELL || StoreType == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL || StoreType == L2PcInstance.STORE_PRIVATE_MANUFACTURE)
			{
				int _itemId = Config.OFFLINE_TRADE_PRICE_ITEM_ID;
				int _count = Config.OFFLINE_TRADE_PRICE_COUNT;

				if (_count > 0)
					if (cantEnable(activeChar))
					{
						activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ACCESSABLE));
						return false;
					}
					else if (activeChar.getInventory().getInventoryItemCount(_itemId, -1) < _count)
					{
						activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						return false;
					}
					else
					{
						activeChar.destroyItemByItemId("OfflinePrice", _itemId, _count, activeChar, true);
						return activeChar.doOffline();
					}
				if (cantEnable(activeChar))
				{
					activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ACCESSABLE));
					return false;
				}
				return activeChar.doOffline();
			}
			else if (StoreType == 5 && Config.ALLOW_OFFLINE_TRADE_CRAFT)
			{
				int _itemId = Config.OFFLINE_CRAFT_PRICE_ITEM_ID;
				int _count = Config.OFFLINE_CRAFT_PRICE_COUNT;
				if (_count > 0)
					if (cantEnable(activeChar))
					{
						activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ACCESSABLE));
						return false;
					}
					else if (activeChar.getInventory().getInventoryItemCount(_itemId, -1) < _count)
					{
						activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						return false;
					}
					else
					{
						activeChar.destroyItemByItemId("OfflinePrice", _itemId, _count, activeChar, true);
						return activeChar.doOffline();
					}
				if (cantEnable(activeChar))
				{
					activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ACCESSABLE));
					return false;
				}
				return activeChar.doOffline();
			}
			else
			{
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_SERVICE_NEED_TO_ACTIVE));
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_OFFLINE_TRADE_NEED));
				if (Config.ALLOW_OFFLINE_TRADE_CRAFT)
				{
					activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_OFFLINE_CRAFT_NEED));
				}
				return false;
			}
		}
		return false;
	}
}

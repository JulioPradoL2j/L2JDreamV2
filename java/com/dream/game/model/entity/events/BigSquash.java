package com.dream.game.model.entity.events;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.manager.EventsDropManager;
import com.dream.game.manager.EventsDropManager.ruleType;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.SystemMessage;

public class BigSquash
{
	private static Logger _log = Logger.getLogger("Event");

	public static void addDrop()
	{
		int item[] =
		{
			6391
		};
		int cnt[] =
		{
			1
		};
		int chance[] =
		{
			Config.BIGSQUASH_CHANCE
		};
		EventsDropManager.getInstance().addRule("BigSquash", ruleType.ALL_NPC, item, cnt, chance);
	}

	public static void exchangeItem(L2PcInstance player, int val)
	{
		if (val == 1)
		{
			L2ItemInstance item1 = player.getInventory().getItemByItemId(6391);
			if (item1 == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			if (item1.getCount() >= 10)
			{
				player.destroyItemByItemId("Quest", 6391, 10, player, true);
				L2ItemInstance item = player.getInventory().addItem("Quest", 6389, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
		}
		if (val == 2)
		{
			L2ItemInstance item1 = player.getInventory().getItemByItemId(6391);
			if (item1 == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			if (item1.getCount() >= 20)
			{
				player.destroyItemByItemId("Quest", 6391, 20, player, true);
				L2ItemInstance item = player.getInventory().addItem("Quest", 6390, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
		}
		if (val == 3)
		{
			L2ItemInstance item1 = player.getInventory().getItemByItemId(6391);
			L2ItemInstance item2 = player.getInventory().getItemByItemId(7058);
			if (item2 != null)
				return;
			if (item1 == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			if (item1.getCount() >= 20)
			{
				player.destroyItemByItemId("Quest", 6391, 20, player, true);
				L2ItemInstance item = player.getInventory().addItem("Quest", 7058, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				player.sendPacket(new ItemList(player, false));
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
		}
	}

	public static void spawnManager()
	{

	}

	public static void startEvent()
	{
		boolean started = false;

		if (Config.BIGSQUASH_DROP)
		{
			addDrop();
			started = true;
		}
		if (Config.BIGSQUASH_SPAWN)
		{
			spawnManager();
			started = true;
		}

		if (started)
		{
			_log.info("BigSquash Event: Initialized.");
		}
		else
		{
			_log.info("BigSquash Event: Disabled.");
		}
	}
}
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

public class Cristmas
{
	private static Logger _log = Logger.getLogger("Event");

	public static void addDrop()
	{
		int item[] =
		{
			5556,
			5557,
			5558,
			5559
		};
		int cnt[] =
		{
			1,
			1,
			1,
			1
		};
		int chance[] =
		{
			Config.CRISTMAS_CHANCE,
			Config.CRISTMAS_CHANCE,
			Config.CRISTMAS_CHANCE,
			Config.CRISTMAS_CHANCE
		};
		EventsDropManager.getInstance().addRule("Cristmas", ruleType.ALL_NPC, item, cnt, chance);
	}

	public static void exchangeItem(L2PcInstance player, int val)
	{
		if (val == 1)
		{
			L2ItemInstance item1 = player.getInventory().getItemByItemId(5556);
			L2ItemInstance item2 = player.getInventory().getItemByItemId(5557);
			L2ItemInstance item3 = player.getInventory().getItemByItemId(5558);
			L2ItemInstance item4 = player.getInventory().getItemByItemId(5559);
			if (item1 == null || item2 == null || item3 == null || item4 == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			if (item1.getCount() >= 4 && item2.getCount() >= 4 && item3.getCount() >= 10 && item4.getCount() >= 1)
			{
				player.destroyItemByItemId("Quest", 5556, 4, player, true);
				player.destroyItemByItemId("Quest", 5557, 4, player, true);
				player.destroyItemByItemId("Quest", 5558, 10, player, true);
				player.destroyItemByItemId("Quest", 5559, 1, player, true);
				L2ItemInstance item = player.getInventory().addItem("Quest", 5560, 1, player, player.getTarget());
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
			L2ItemInstance item1 = player.getInventory().getItemByItemId(5560);
			if (item1 == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			if (item1.getCount() >= 10)
			{
				player.destroyItemByItemId("Quest", 5560, 10, player, true);
				L2ItemInstance item = player.getInventory().addItem("Quest", 5561, 1, player, player.getTarget());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
				item = player.getInventory().addItem("Quest", 9138, 1, player, player.getTarget());
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

	public static void spawnSanta()
	{
	}

	public static void startEvent()
	{
		boolean started = false;

		if (Config.CRISTMAS_DROP)
		{
			addDrop();
			started = true;
		}
		if (Config.CRISTMAS_SPAWN)
		{
			spawnSanta();
			started = true;
		}

		if (started)
		{
			_log.info("Cristmas Event: Initialized.");
		}
		else
		{
			_log.info("Cristmas Event: Disabled.");
		}
	}
}
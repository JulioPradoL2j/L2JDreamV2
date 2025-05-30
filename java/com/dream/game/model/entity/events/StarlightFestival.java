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

public class StarlightFestival
{
	private static Logger _log = Logger.getLogger("Event");

	public static void addDrop()
	{
		int item[] =
		{
			6403,
			6404,
			6405
		};
		int cnt[] =
		{
			1,
			1,
			1
		};
		int chance[] =
		{
			Config.STAR_CHANCE1,
			Config.STAR_CHANCE2,
			Config.STAR_CHANCE3
		};
		EventsDropManager.getInstance().addRule("StarlightFestival", ruleType.ALL_NPC, item, cnt, chance);
	}

	public static void exchangeItem(L2PcInstance player, int val)
	{
		if (val == 1)
		{
			L2ItemInstance item1 = player.getInventory().getItemByItemId(6403);
			L2ItemInstance item2 = player.getInventory().getItemByItemId(6404);
			if (item1 == null || item2 == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			if (item1.getCount() >= 2 && item2.getCount() >= 2)
			{
				player.destroyItemByItemId("Quest", 6403, 2, player, true);
				player.destroyItemByItemId("Quest", 6404, 2, player, true);
				L2ItemInstance item = player.getInventory().addItem("Quest", 6406, 1, player, player.getTarget());
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
			L2ItemInstance item1 = player.getInventory().getItemByItemId(6403);
			L2ItemInstance item2 = player.getInventory().getItemByItemId(6404);
			L2ItemInstance item3 = player.getInventory().getItemByItemId(6405);
			if (item1 == null || item2 == null || item3 == null)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			if (item1.getCount() >= 4 && item2.getCount() >= 4 && item3.getCount() >= 1)
			{
				player.destroyItemByItemId("Quest", 6403, 4, player, true);
				player.destroyItemByItemId("Quest", 6404, 4, player, true);
				player.destroyItemByItemId("Quest", 6405, 1, player, true);
				L2ItemInstance item = player.getInventory().addItem("Quest", 6407, 1, player, player.getTarget());
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

	public static void spawnEventManager()
	{
	}

	public static void startEvent()
	{
		boolean started = false;

		if (Config.STAR_DROP)
		{
			addDrop();
			started = true;
		}
		if (Config.STAR_SPAWN)
		{
			spawnEventManager();
			started = true;
		}
		if (started)
		{
			_log.info("Starlight Festival Event: Initialized.");
		}
		else
		{
			_log.info("Starlight Festival Event: Disabled.");
		}
	}
}
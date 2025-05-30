package com.dream.game.model.entity.events;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.manager.EventsDropManager;
import com.dream.game.manager.EventsDropManager.ruleType;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.multisell.L2Multisell;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.tools.random.Rnd;

public class EventMedals
{
	private static Logger _log = Logger.getLogger("Event");

	private static int[] BAGES =
	{
		6399,
		6400,
		6401,
		6402
	};

	public static void addDrop()
	{
		int item[] =
		{
			6392,
			6393
		};
		int cnt[] =
		{
			1,
			1
		};
		int chance[] =
		{
			Config.MEDAL_CHANCE1,
			Config.MEDAL_CHANCE2
		};
		EventsDropManager.getInstance().addRule("Medals", ruleType.ALL_NPC, item, cnt, chance);
	}

	public static void exchangeItem(L2PcInstance player, int val)
	{
		if (val == 1)
		{
			if (player.getInventory().getItemByItemId(BAGES[0]) != null)
			{
				L2Multisell.getInstance().SeparateAndSend(31228002, player, false, 1);
			}
			else if (player.getInventory().getItemByItemId(BAGES[1]) != null)
			{
				L2Multisell.getInstance().SeparateAndSend(31228003, player, false, 1);
			}
			else if (player.getInventory().getItemByItemId(BAGES[2]) != null)
			{
				L2Multisell.getInstance().SeparateAndSend(31228004, player, false, 1);
			}
			else if (player.getInventory().getItemByItemId(BAGES[3]) != null)
			{
				L2Multisell.getInstance().SeparateAndSend(31228005, player, false, 1);
			}
			else
			{
				L2Multisell.getInstance().SeparateAndSend(31228001, player, false, 1);
			}
			return;
		}
		else if (val == 2)
		{
			if (player.getInventory().getItemByItemId(6393) == null)
			{
				sendHtml(player, "31229-4");
				return;
			}
			L2ItemInstance item1 = player.getInventory().getItemByItemId(BAGES[0]);
			L2ItemInstance item2 = player.getInventory().getItemByItemId(BAGES[1]);
			L2ItemInstance item3 = player.getInventory().getItemByItemId(BAGES[2]);
			L2ItemInstance item4 = player.getInventory().getItemByItemId(BAGES[3]);
			if (item1 == null && item2 == null && item3 == null && item4 == null)
			{
				if (player.getInventory().getItemByItemId(6393).getCount() >= 5)
				{
					if (Rnd.get(100) < 33)
					{
						player.destroyItemByItemId("Quest", 6393, 5, player, true);
						L2ItemInstance item = player.getInventory().addItem("Quest", BAGES[0], 1, player, player.getTarget());
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
						player.sendPacket(new ItemList(player, false));
						sendHtml(player, "31229-7");
						return;
					}
					player.destroyItemByItemId("Quest", 6393, 5, player, true);
					sendHtml(player, "31229-5");
					return;
				}
				sendHtml(player, "31229-4");
				return;
			}
			else if (item1 != null)
			{
				if (player.getInventory().getItemByItemId(6393).getCount() >= 10)
				{
					if (Rnd.get(100) < 33)
					{
						player.destroyItemByItemId("Quest", 6393, 10, player, true);
						player.destroyItemByItemId("Quest", BAGES[0], 1, player, true);
						L2ItemInstance item = player.getInventory().addItem("Quest", BAGES[1], 1, player, player.getTarget());
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
						player.sendPacket(new ItemList(player, false));
						sendHtml(player, "31229-7");
						return;
					}
					player.destroyItemByItemId("Quest", 6393, 10, player, true);
					sendHtml(player, "31229-5");
					return;
				}
				sendHtml(player, "31229-4");
				return;
			}
			else if (item2 != null)
			{
				if (player.getInventory().getItemByItemId(6393).getCount() >= 20)
				{
					if (Rnd.get(100) < 33)
					{
						player.destroyItemByItemId("Quest", 6393, 20, player, true);
						player.destroyItemByItemId("Quest", BAGES[1], 1, player, true);
						L2ItemInstance item = player.getInventory().addItem("Quest", BAGES[2], 1, player, player.getTarget());
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
						player.sendPacket(new ItemList(player, false));
						sendHtml(player, "31229-7");
						return;
					}
					player.destroyItemByItemId("Quest", 6393, 20, player, true);
					sendHtml(player, "31229-5");
					return;
				}
				sendHtml(player, "31229-4");
				return;
			}
			else if (item3 != null)
			{
				if (player.getInventory().getItemByItemId(6393).getCount() >= 40)
				{
					if (Rnd.get(100) < 33)
					{
						player.destroyItemByItemId("Quest", 6393, 40, player, true);
						player.destroyItemByItemId("Quest", BAGES[2], 1, player, true);
						L2ItemInstance item = player.getInventory().addItem("Quest", BAGES[3], 1, player, player.getTarget());
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
						player.sendPacket(new ItemList(player, false));
						sendHtml(player, "31229-7");
						return;
					}
					player.destroyItemByItemId("Quest", 6393, 40, player, true);
					sendHtml(player, "31229-5");
					return;
				}
				sendHtml(player, "31229-4");
				return;
			}
			else if (item4 != null)
			{
				sendHtml(player, "31229-6");
				return;
			}
		}
		else if (val == 3)
		{
			L2Multisell.getInstance().SeparateAndSend(31230001, player, false, 1);
		}
		else if (val == 4)
		{
			L2ItemInstance item1 = player.getInventory().getItemByItemId(BAGES[0]);
			L2ItemInstance item2 = player.getInventory().getItemByItemId(BAGES[1]);
			L2ItemInstance item3 = player.getInventory().getItemByItemId(BAGES[2]);
			L2ItemInstance item4 = player.getInventory().getItemByItemId(BAGES[3]);
			L2Npc npc = (L2Npc) player.getTarget();
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/default/31229-3.htm");
			html.replace("%objectId%", String.valueOf(npc.getObjectId()));
			if (item1 != null)
			{
				html.replace("%count%", "10");
				html.replace("%no%", "");
				html.replace("%Level%", "Rabbit");
			}
			else if (item2 != null)
			{
				html.replace("%count%", "20");
				html.replace("%no%", "");
				html.replace("%Level%", "Hyena");
			}
			else if (item3 != null)
			{
				html.replace("%count%", "40");
				html.replace("%no%", "");
				html.replace("%Level%", "Fox");
			}
			if (item4 != null)
			{
				html.replace("%count%", "40");
				html.replace("%no%", "");
				html.replace("%Level%", "Wolf");
			}
			else
			{
				html.replace("%count%", "5");
				html.replace("%no%", "не ");
				html.replace("%Level%", "any");
			}
			player.sendPacket(html);
		}
	}

	private static void sendHtml(L2PcInstance player, String val)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/default/" + val + ".htm");
		player.sendPacket(html);
	}

	public static void spawnEventManagers()
	{

	}

	public static void startEvent()
	{
		boolean started = false;

		if (Config.MEDAL_DROP)
		{
			addDrop();
			started = true;
		}
		if (Config.MEDAL_SPAWN)
		{
			spawnEventManagers();
			started = true;
		}

		if (started)
		{
			_log.info("Medals Event: Initialized.");
		}
		else
		{
			_log.info("Medals Event: Disabled.");
		}
	}
}
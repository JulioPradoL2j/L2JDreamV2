package com.dream.game.model.entity.events;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.manager.EventsDropManager;
import com.dream.game.manager.EventsDropManager.ruleType;
import com.dream.game.model.actor.instance.L2PcInstance;

public class L2DropDay
{
	private static Logger _log = Logger.getLogger("Event");

	public static void addDrop()
	{
		int item[] =
		{
			Config.L2DROPDAY_ITEM1,
			Config.L2DROPDAY_ITEM2,
			Config.L2DROPDAY_ITEM3,
			Config.L2DROPDAY_ITEM4,
			Config.L2DROPDAY_ITEM5,
			Config.L2DROPDAY_ITEM6,
			Config.L2DROPDAY_ITEM7,
			Config.L2DROPDAY_ITEM8,
			Config.L2DROPDAY_ITEM9,
			Config.L2DROPDAY_ITEM10,
			Config.L2DROPDAY_ITEM11,
			Config.L2DROPDAY_ITEM12,
			Config.L2DROPDAY_ITEM13,
			Config.L2DROPDAY_ITEM14,
			Config.L2DROPDAY_ITEM15,
			Config.L2DROPDAY_ITEM16,
			Config.L2DROPDAY_ITEM17,
			Config.L2DROPDAY_ITEM18,
			Config.L2DROPDAY_ITEM19,
			Config.L2DROPDAY_ITEM20,
			Config.L2DROPDAY_ITEM21,
			Config.L2DROPDAY_ITEM22
		};
		int cnt[] =
		{
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1
		};
		int chance[] =
		{
			Config.L2DROPDAY_CHANCE1,
			Config.L2DROPDAY_CHANCE2,
			Config.L2DROPDAY_CHANCE3,
			Config.L2DROPDAY_CHANCE4,
			Config.L2DROPDAY_CHANCE5,
			Config.L2DROPDAY_CHANCE6,
			Config.L2DROPDAY_CHANCE7,
			Config.L2DROPDAY_CHANCE8,
			Config.L2DROPDAY_CHANCE9,
			Config.L2DROPDAY_CHANCE10,
			Config.L2DROPDAY_CHANCE11,
			Config.L2DROPDAY_CHANCE12,
			Config.L2DROPDAY_CHANCE13,
			Config.L2DROPDAY_CHANCE14,
			Config.L2DROPDAY_CHANCE15,
			Config.L2DROPDAY_CHANCE16,
			Config.L2DROPDAY_CHANCE17,
			Config.L2DROPDAY_CHANCE18,
			Config.L2DROPDAY_CHANCE19,
			Config.L2DROPDAY_CHANCE20,
			Config.L2DROPDAY_CHANCE21,
			Config.L2DROPDAY_CHANCE22
		};
		EventsDropManager.getInstance().addRule("L2DropDay", ruleType.ALL_NPC, item, cnt, chance);
	}

	public static void exchangeItem(L2PcInstance player, int val)
	{
	}

	public static void spawnEventManager()
	{
	}

	public static void startEvent()
	{
		boolean started = false;

		if (Config.L2DROPDAY_ENABLE)
		{
			addDrop();
			started = true;
		}

		if (started)
		{
			_log.info("L2DropDay Event: Initialized.");
		}
		else
		{
			_log.info("L2DropDay Event: Disabled.");
		}
	}
}
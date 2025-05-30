package com.dream.game.manager;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.Disconnection;
import com.dream.game.network.ThreadPoolManager;

import org.apache.log4j.Logger;

public class OfflineManager
{
	private class Checker implements Runnable
	{
		@Override
		public void run()
		{
			int count = 0;
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player == null)
				{
					continue;
				}
				if (!player.isOfflineTrade())
				{
					continue;
				}

				if (player.getEndOfflineTime() < System.currentTimeMillis())
				{
					player.setOfflineTrade(false);
					player.standUp();
					new Disconnection(player).defaultSequence(false);
					count++;
				}
			}
			if (count > 0)
			{
				_log.info("Offline Manager: " + count + " player(s) deleted, offline time expired.");
			}
		}
	}

	public static final Logger _log = Logger.getLogger(OfflineManager.class);

	private static OfflineManager _instance = null;

	public static OfflineManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Offline Manager: Initialized.");
			_instance = new OfflineManager();
		}
		return _instance;
	}


	private OfflineManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Checker(), 1800000, 1800000);
	}

	public void removeTrader(L2PcInstance player)
	{
		if (player == null)
			return;

		player.setOfflineTrade(false);
		player.standUp();
		new Disconnection(player).defaultSequence(false);
	}
}

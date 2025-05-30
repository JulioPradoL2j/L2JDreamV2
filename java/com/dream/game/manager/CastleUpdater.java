package com.dream.game.manager;

import com.dream.Config;
import com.dream.game.model.L2Clan;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.itemcontainer.ItemContainer;
import com.dream.game.network.ThreadPoolManager;

public class CastleUpdater implements Runnable
{
	private final L2Clan _clan;
	private int _runCount = 0;

	public CastleUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}

	@Override
	public void run()
	{
		try
		{
			ItemContainer warehouse = _clan.getWarehouse();
			if (warehouse != null && _clan.getHasCastle() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.getHasCastle());
				if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
					if (_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
					}
				CastleUpdater cu = new CastleUpdater(_clan, ++_runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
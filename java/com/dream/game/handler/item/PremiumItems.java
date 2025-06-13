package com.dream.game.handler.item;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.handler.IItemHandler;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class PremiumItems implements IItemHandler
{
	public static Logger LOGGER = Logger.getLogger(PremiumItems.class.getName());
	
	private static final int ITEM_IDS[] = {
		
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{
		
	}
	
	@Override
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) playable;
		final int itemId = item.getItemId();
		final int days = item.getItem().getDays();
		
		if (!Config.USE_PREMIUM_SERVICE)
		{
			player.sendMessage("This feature is currently unavailable.");
			return;
		}
		
		if (player.getPremServiceData() > Calendar.getInstance().getTimeInMillis())
		{
			player.sendMessage("You already have a premium account.");
			return;
		}
		if (days != 0)
		{
			
			long premiumTime = 0L;
			
			try
			{
				Calendar now = Calendar.getInstance();
				now.add(Calendar.DATE, days);
				premiumTime = now.getTimeInMillis();
			}
			catch (NumberFormatException nfe)
			{
				return;
			}
			
			if (!player.destroyItemByItemId("ServicePremium" + days, itemId, 1, player, true))
				return;
			
			player.setPremiumService(1);
			player.setVip(true);
			updateDatabasePremium(premiumTime, player.getAccountName());
			player.sendMessage(String.format("You have purchased a premium account.\n Number of days: %d.", days));
			player.broadcastUserInfo();
		}
		
	}
	
	private static final String UPDATE_PREMIUMSERVICE = "REPLACE INTO account_premium (premium_service,enddate,account_name) values(?,?,?)";
	
	private static void updateDatabasePremium(long time, String AccName)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE))
		{
			statement.setInt(1, 1);
			statement.setLong(2, time);
			statement.setString(3, AccName);
			statement.execute();
		}
		catch (Exception e)
		{
			LOGGER.info("updateDatabasePremium: Could not update account:" + AccName + " data:" + e);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
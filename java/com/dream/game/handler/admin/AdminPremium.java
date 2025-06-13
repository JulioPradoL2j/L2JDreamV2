package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.access.gmHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class AdminPremium extends gmHandler
{
	public static Logger LOGGER = Logger.getLogger(AdminPremium.class.getName());
	
	private static final String[] commands =
	{
		"premium",
	
	};
	
	@Override
	public String[] getCommandList()
	{
		return commands;
	}
	
	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		if (admin == null)
			return;
		
		if (!Config.USE_PREMIUM_SERVICE)
		{
			admin.sendMessage("This feature is currently unavailable.");
			return;
		}
		
		// Verifica se é o comando "premium"
		if (params.length == 0 || !params[0].equalsIgnoreCase("premium"))
			return;
		
		String playerName = null;
		int days = 0;
		
		// Caso tenha parâmetros suficientes (nome e dias)
		if (params.length >= 3)
		{
			playerName = params[1];
			try
			{
				days = Integer.parseInt(params[2]);
			}
			catch (NumberFormatException e)
			{
				admin.sendMessage("Please enter a valid number of days.");
				return;
			}
			
			L2PcInstance targetPlayer = L2World.getInstance().getPlayer(playerName);
			if (targetPlayer != null)
			{
				sendPremium(admin, targetPlayer, days);
			}
			else
			{
				admin.sendMessage("Player not found or not online.");
			}
		}
		else
		{
			
			L2Object target = admin.getTarget();
			if (target instanceof L2PcInstance)
			{
				admin.sendMessage("Usage: //premium [name] [days]");
			}
			else
			{
				admin.sendMessage("You must specify the name and number of days or target a player.");
			}
		}
	}
	
	private static void sendPremium(L2PcInstance admin, L2PcInstance player, int days)
	{
		if (player.getPremServiceData() > Calendar.getInstance().getTimeInMillis())
		{
			admin.sendMessage(player.getName() + " already has a premium account.");
			return;
		}
		
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, days);
		long premiumTime = now.getTimeInMillis();
		
		player.setPremiumService(1);
		player.setVip(true);
		updateDatabasePremium(premiumTime, player.getAccountName());
		
		player.sendMessage("You have received a premium account for " + days + " day(s)!");
		player.broadcastUserInfo();
		
		admin.sendMessage("You have successfully given premium to " + player.getName() + " for " + days + " day(s).");
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
}

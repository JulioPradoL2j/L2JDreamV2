package com.dream.game.handler.admin;

import com.dream.Config;
import com.dream.game.access.gmHandler;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CastleManorManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

import java.util.ArrayList;

public class AdminManor extends gmHandler
{
	private static final String[] commands =
	{
		"manor",
		"manor_approve",
		"manor_setnext",
		"manor_reset",
		"manor_setmaintenance",
		"manor_save",
		"manor_disable"
	};

	private static String formatTime(long millis)
	{
		String s = "";
		int secs = (int) millis / 1000;
		int mins = secs / 60;
		secs -= mins * 60;
		int hours = mins / 60;
		mins -= hours * 60;

		if (hours > 0)
		{
			s += hours + ":";
		}
		s += mins + ":";
		s += secs;
		return s;
	}

	private static void showMainPage(L2PcInstance admin)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");

		replyMSG.append("<center><font color=\"LEVEL\"> [Manor System] </font></center><br>");
		replyMSG.append("<table width=\"100%\"><tr><td>");
		replyMSG.append("Disabled: " + (CastleManorManager.getInstance().isDisabled() ? "yes" : "no") + "</td><td>");
		replyMSG.append("Under Maintenance: " + (CastleManorManager.getInstance().isUnderMaintenance() ? "yes" : "no") + "</td></tr><tr><td>");
		replyMSG.append("Time to refresh: " + formatTime(CastleManorManager.getInstance().getMillisToManorRefresh()) + "</td><td>");
		replyMSG.append("Time to approve: " + formatTime(CastleManorManager.getInstance().getMillisToNextPeriodApprove()) + "</td></tr>");
		replyMSG.append("</table>");

		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Set Next\" action=\"bypass -h admin_manor_setnext\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Approve Next\" action=\"bypass -h admin_manor_approve\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr><tr><td>");
		replyMSG.append("<button value=\"" + (CastleManorManager.getInstance().isUnderMaintenance() ? "Set normal" : "Set mainteance") + "\" action=\"bypass -h admin_manor_setmaintenance\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"" + (CastleManorManager.getInstance().isDisabled() ? "Enable" : "Disable") + "\" action=\"bypass -h admin_manor_disable\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr><tr><td>");
		replyMSG.append("<button value=\"Refresh\" action=\"bypass -h admin_manor\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td>");
		replyMSG.append("<button value=\"Back\" action=\"bypass -h admin_admin\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
		replyMSG.append("</table></center>");

		replyMSG.append("<br><center>Castle Information:<table width=\"100%\">");
		replyMSG.append("<tr><td></td><td>Current Period</td><td>Next Period</td></tr>");

		for (Castle c : CastleManager.getInstance().getCastles().values())
		{
			replyMSG.append("<tr><td>" + c.getName() + "</td>" + "<td>" + c.getManorCost(CastleManorManager.PERIOD_CURRENT) + "a</td>" + "<td>" + c.getManorCost(CastleManorManager.PERIOD_NEXT) + "a</td>" + "</tr>");
		}

		replyMSG.append("</table><br>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		admin.sendPacket(adminReply);
	}

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

		final String command = params[0];

		if (command.equals("manor"))
		{
			showMainPage(admin);
		}
		else if (command.equals("manor_setnext"))
		{
			CastleManorManager.getInstance().setNextPeriod();
			CastleManorManager.getInstance().setNewManorRefresh();
			CastleManorManager.getInstance().updateManorRefresh();
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor System: set to next period");
			showMainPage(admin);
		}
		else if (command.equals("manor_approve"))
		{
			CastleManorManager.getInstance().approveNextPeriod();
			CastleManorManager.getInstance().setNewPeriodApprove();
			CastleManorManager.getInstance().updatePeriodApprove();
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor System: next period approved");
			showMainPage(admin);
		}
		else if (command.equals("manor_reset"))
		{
			int castleId = 0;
			try
			{
				castleId = Integer.parseInt(params[1]);
			}
			catch (Exception e)
			{
			}

			if (castleId > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(castleId);
				castle.setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
				castle.setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
				castle.setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
				castle.setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
				if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					castle.saveCropData();
					castle.saveSeedData();
				}
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor data for " + castle.getName() + " was nulled");
			}
			else
			{
				for (Castle castle : CastleManager.getInstance().getCastles().values())
				{
					castle.setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
					castle.setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
					castle.setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
					castle.setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
					if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
					{
						castle.saveCropData();
						castle.saveSeedData();
					}
				}
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor data was nulled");
			}
			showMainPage(admin);
		}
		else if (command.equals("manor_setmaintenance"))
		{
			boolean mode = CastleManorManager.getInstance().isUnderMaintenance();
			CastleManorManager.getInstance().setUnderMaintenance(!mode);
			if (mode)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor System: not under maintenance");
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor System: under maintenance");
			}
			showMainPage(admin);
		}
		else if (command.equals("manor_save"))
		{
			CastleManorManager.getInstance().saveData();
			admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor System: all data saved");
			showMainPage(admin);
		}
		else if (command.equals("manor_disable"))
		{
			boolean mode = CastleManorManager.getInstance().isDisabled();
			CastleManorManager.getInstance().setDisabled(!mode);
			if (mode)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor System: enabled");
			}
			else
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Manor System: disabled");
			}
			showMainPage(admin);
		}

		return;
	}
}
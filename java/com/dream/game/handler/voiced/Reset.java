package com.dream.game.handler.voiced;

import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.xml.ResetData;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.model.ResetManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.holders.ResetHolder;
import com.dream.game.model.holders.ResetPrize;
import com.dream.game.model.holders.ResetType;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Reset implements IVoicedCommandHandler
{
	public static Logger LOGGER = Logger.getLogger(Reset.class);
	
	private static String[] _voicedCommands =
	{
		"reset",
		"rank"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equals("reset"))
		{
			ResetManager.getInstance().tryReset(activeChar);
			return true;
		}
		else if (command.equals("rank"))
		{
			showResetRanking(activeChar);
			return true;
		}
		return false;
	}
	
	private void showResetRanking(L2PcInstance player)
	{
		StringBuilder html = new StringBuilder();
		html.append("<html><body><center>");
		
		ResetHolder holder = ResetData.getInstance().getResets().get(0);
		boolean showAny = false;
		
		for (ResetPrize prize : holder.getPrizes())
		{
			if (!prize.isEnabled())
				continue;
			
			showAny = true;
			
			if (prize.getType() == ResetType.DAILY)
			{
				html.append("<font color=LEVEL><b>Top Daily Reset</b></font><br>");
				appendRankingTable(html, ResetManager.getInstance().getRanking(ResetType.DAILY));
				html.append("<br>");
			}
			else if (prize.getType() == ResetType.MONTH)
			{
				html.append("<font color=LEVEL><b>Top Monthly Reset</b></font><br>");
				appendRankingTable(html, ResetManager.getInstance().getRanking(ResetType.MONTH));
				html.append("<br>");
			}
		}
		
		if (!showAny)
		{
			html.append("<font color=FF0000><b>Nenhum ranking de reset está ativo no momento.</b></font>");
		}
		
		html.append("</center></body></html>");
		
		sendHtmlMessage(player, "ResetRanking.htm", html.toString());
	}
	
	private static void appendRankingTable(StringBuilder html, Map<Integer, Integer> ranking)
	{
		html.append("<table width=300>");
		html.append("<tr><td><b>Position</b></td><td><b>Name</b></td><td><b>Resets</b></td></tr>");
		
		List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(ranking.entrySet());
		sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
		
		int pos = 1;
		for (Map.Entry<Integer, Integer> entry : sorted)
		{
			if (pos > ResetData.getInstance().getResets().get(0).getRankingDisplayLimit())
				break;
			
			String name;
			L2PcInstance player = L2World.getInstance().getPlayer(entry.getKey());
			if (player != null)
			{
				name = player.getName();
			}
			else
			{
				name = getOfflineCharName(entry.getKey());
			}
			
			html.append("<tr>");
			html.append("<td>").append(pos).append("</td>");
			html.append("<td>").append(name).append("</td>");
			html.append("<td>").append(entry.getValue()).append("</td>");
			html.append("</tr>");
			pos++;
		}
		
		html.append("</table>");
	}
	
	public void sendHtmlMessage(L2PcInstance player, String fileName, String content)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(content);
		
		player.sendPacket(msg);
	}
	
	private static String getOfflineCharName(int charId)
	{
		String name = "Unknown";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement("SELECT char_name FROM characters WHERE charId = ?"))
		{
			ps.setInt(1, charId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					name = rs.getString("char_name");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Reset rank error when searching for charId: " + charId + " - " + e.getMessage());
		}
		
		return name;
	}
	
	@Override
	public String getDescription(String command)
	{
		if (command.equals("reset"))
			return "Displays a reset of commands.";
		return "In detail in the reset.";
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
}

package com.dream.game.communitybbs.Manager;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.cache.HtmCache;
import com.dream.game.communitybbs.module.CastleStatus;
import com.dream.game.communitybbs.module.PlayerList;
import com.dream.game.communitybbs.module.ServerStats;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.util.ResourceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * @author Matim
 * @version 2.0
 */
public class TopBBSManager extends BaseBBSManager
{
	private static class SingletonHolder
	{
		protected static final TopBBSManager INSTANCE = new TopBBSManager();
	}
	

	private static String getCharList(L2PcInstance activeChar)
	{
		String result = "";
		String repCharAcc = activeChar.getAccountName();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?");
			statement.setString(1, repCharAcc);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
				if (activeChar.getName().compareTo(rset.getString(1)) != 0)
				{
					result += rset.getString(1) + ";";
				}
			
			rset.close();
			ResourceUtil.closeStatement(statement);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return result;
		}
		finally
		{
			ResourceUtil.closeConnection(con);
		}
		return result;
	}
	
	public static TopBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static String getOnlineGMs()
	{
		String msg = "<br>";
		
		if (L2World.getAllGMs().isEmpty())
		{
			msg = "There are not Online GMs at this moment!";
		}
		else
		{
			for (L2PcInstance player : L2World.getAllGMs())
			{
				msg += player.getName();
				msg += "<br>";
			}
		}
		return msg;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		String fileName = "";
		
		if (Config.ALLOW_CUSTOM_COMMUNITY)
		{
			if (command.startsWith("_bbstop") || command.startsWith("_bbshome"))
			{
				fileName = "data/html/communityboard/custom/customindex.htm";
			}
			if (command.startsWith("_bbstop;"))
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				
				String f = st.nextToken();
				fileName = "data/html/communityboard/custom/" + f + ".htm";
			}
			if (fileName.length() == 0)
				return;
			
			String content = HtmCache.getInstance().getHtm(fileName);
			
			String ip = "N/A";
			String account = "N/A";
			try
			{
				String clientInfo = activeChar.getClient().toString();
				account = clientInfo.substring(clientInfo.indexOf("Account: ") + 9, clientInfo.indexOf(" - IP: "));
				ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
			}
			catch (Exception e)
			{
				
			}
			
			if (fileName.equals("data/html/communityboard/custom/toppvp.htm"))
			{
				
			}
			{
				// Top 15 PvP List
				PlayerList pvp = new PlayerList(1);
				content = content.replaceAll("%pvplist%", pvp.loadPlayerList());
			}
			if (fileName.equals("data/html/communityboard/custom/castle.htm"))
			{
				
			}
			{
				CastleStatus status = new CastleStatus();
				content = content.replaceAll("%castle%", status.loadCastleList());
			}
			if (fileName.equals("data/html/communityboard/custom/toppk.htm"))
			{
				
			}
			{
				// Top 15 Pk List
				PlayerList pk = new PlayerList(0);
				content = content.replaceAll("%pklist%", pk.loadPlayerList());
			}
			if (fileName.equals("data/html/communityboard/custom/accinfo.htm"))
			{
				
			}
			{
				// Char Info
				content = content.replaceAll("%name%", activeChar.getName());
				content = content.replaceAll("%level%", String.valueOf(activeChar.getLevel()));
				content = content.replaceAll("%clan%", String.valueOf(ClanTable.getInstance().getClan(activeChar.getClanId())));
				content = content.replaceAll("%xp%", String.valueOf(activeChar.getExp()));
				content = content.replaceAll("%sp%", String.valueOf(activeChar.getSp()));
				content = content.replaceAll("%class%", String.valueOf(activeChar.getClassId()));
				content = content.replaceAll("%currenthp%", String.valueOf((int) activeChar.getCurrentHp()));
				content = content.replaceAll("%maxhp%", String.valueOf(activeChar.getMaxHp()));
				content = content.replaceAll("%currentmp%", String.valueOf((int) activeChar.getCurrentMp()));
				content = content.replaceAll("%maxmp%", String.valueOf(activeChar.getMaxMp()));
				content = content.replaceAll("%currentcp%", String.valueOf((int) activeChar.getCurrentCp()));
				content = content.replaceAll("%maxcp%", String.valueOf(activeChar.getMaxCp()));
				content = content.replaceAll("%currentload%", String.valueOf(activeChar.getCurrentLoad()));
				content = content.replaceAll("%maxload%", String.valueOf(activeChar.getMaxLoad()));
				content = content.replaceAll("%account%", account);
				content = content.replaceAll("%ip%", ip);
			}
			if (fileName.equals("data/html/communityboard/custom/gms.htm"))
			{
				
			}
			{
				// Online Gamemasters
				content = content.replaceAll("%gmlist%", getOnlineGMs());
			}
			if (fileName.equals("data/html/communityboard/custom/repair.htm"))
			{
				
			}
			{
				// Character Repair
				content = content.replaceAll("%acc_chars%", getCharList(activeChar));
			}
			if (fileName.equals("data/html/communityboard/custom/stats.htm"))
			{
				
			}
			{
				// Server Stats
				ServerStats stats = new ServerStats();
				content = content.replaceAll("%stats%", stats.getServerStats());
				content = content.replaceAll("%online%", stats.getOnlineCount());
			}
			
			if (content == null)
			{
				sendError404(activeChar, fileName);
			}
			else
			{
				separateAndSend(content, activeChar);
			}
		}
		else
		{
			if (command.equals("_bbstop") || command.equals("_bbshome"))
			{
				fileName = "data/html/communityboard/index.htm";
			}
			if (command.equals("_bbstop;"))
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				
				String f = st.nextToken();
				fileName = "data/html/communityboard/" + f + ".htm";
			}
			if (fileName.length() == 0)
				return;
			
			String content = HtmCache.getInstance().getHtm(fileName);
			
			if (content == null)
			{
				sendError404(activeChar, fileName);
			}
			else
			{
				separateAndSend(content, activeChar);
			}
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		
	}
}
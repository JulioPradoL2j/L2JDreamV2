package com.dream.game;

import com.dream.Config;
import com.dream.game.cache.HtmCache;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.taskmanager.AutoAnnounceTaskManager;
import com.dream.tools.random.DateRange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Announcements
{
	private final static Logger _log = Logger.getLogger(Announcements.class.getName());

	private static Announcements _instance;

	public static Announcements getInstance()
	{
		if (_instance == null)
		{
			_instance = new Announcements();
		}
		return _instance;
	}

	private final List<String> _announcements = new ArrayList<>();

	private final List<List<Object>> _eventAnnouncements = new ArrayList<>();

	public Announcements()
	{
		loadAnnouncements();
		if (Config.LOAD_AUTOANNOUNCE_AT_STARTUP)
		{
			AutoAnnounceTaskManager.getInstance();
		}
		else
		{
			_log.info("AnnounceManager: Auto announce disabled");
		}
	}

	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}

	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		List<Object> entry = new ArrayList<>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
	}

	public void announceToAll(L2GameServerPacket gsp)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(gsp);
		}
	}

	public void announceToAll(String text)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (Config.ANNOUNCE_MODE.equals("l2j"))
			{
			player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Announce, "", text));
			}
			else if (Config.ANNOUNCE_MODE.equals("l2off"))
			{
				player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Critical_Announce, "", text));
			}
		}
	}

	public void announceToAll(SystemMessageId sm)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(sm);
		}
	}

	public void announceToAlly(L2Clan clan, String text)
	{
		if (Config.ANNOUNCE_MODE.equals("l2j"))
		{
			clan.broadcastToOnlineAllyMembers(new CreatureSay(0, SystemChatChannelId.Chat_Announce, "", text));
		}
		else if (Config.ANNOUNCE_MODE.equals("l2off"))
		{
			clan.broadcastToOnlineAllyMembers(new CreatureSay(0, SystemChatChannelId.Chat_Critical_Announce, "", text));
		}
	}

	public void announceToClan(L2Clan clan, String text)
	{
		if (Config.ANNOUNCE_MODE.equals("l2j"))
		{
			clan.broadcastToOnlineMembers(new CreatureSay(0, SystemChatChannelId.Chat_Announce, "", text));
		}
		else if (Config.ANNOUNCE_MODE.equals("l2off"))
		{
			clan.broadcastToOnlineMembers(new CreatureSay(0, SystemChatChannelId.Chat_Critical_Announce, "", text));
		}
	}

	public void announceToPlayers(String message)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player != null)
			{
				player.sendMessage(message);
			}
	}

	public void criticalAnnounceToAll(String text)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Critical_Announce, "", text));
		}
	}

	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}

	public void handleAnnounce(String command)
	{
		try
		{
			announceToAll(command);
		}
		catch (StringIndexOutOfBoundsException e)
		{
		}
	}

	public void handleCriticalAnnounce(String command)
	{
		try
		{
			criticalAnnounceToAll(command);
		}
		catch (StringIndexOutOfBoundsException e)
		{
		}
	}

	public void listAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtm("data/html/admin/menus/submenus/announce_menu.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		String temp = "";
		StringBuilder replyMSG = new StringBuilder("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			temp = _announcements.get(i).length() > 27 ? _announcements.get(i).substring(0, 26) + "..." : _announcements.get(i);
			replyMSG.append("<table width=260><tr><td width=220>" + temp + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_announce_del " + i + "\" width=60 height=19 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr></table>");
		}
		String html = replyMSG.toString();
		adminReply.replace("%announces%", html.length() > 30 ? html : "<br><center>Announcements not found</center>");
		activeChar.sendPacket(adminReply);
	}

	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.info("AnnounceManager: File is not exist");
		}
	}

	
	private void readFromDisk(File file)
	{
		FileInputStream fStream = null;
		LineNumberReader lnr = null;
		try
		{
			int i = 0;
			String line = null;
			fStream = new FileInputStream(file);
			lnr = new LineNumberReader(new InputStreamReader(fStream, "UTF-8"));
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					String announcement = st.nextToken();
					_announcements.add(announcement);
					i++;
				}
			}
			_log.info("Announcement : Loaded " + i + " announcements.");
		}
		catch (IOException e1)
		{
			_log.fatal("Error reading announcements", e1);
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(fStream);
		}
	}

	private void saveToDisk()
	{
		File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
		FileWriter save = null;

		try
		{
			save = new FileWriter(file);
			for (int i = 0; i < _announcements.size(); i++)
			{
				save.write(_announcements.get(i));
				save.write("\r\n");
			}
		}
		catch (IOException e)
		{
			_log.warn("saving the announcements file has failed: " + e);
		}
		finally
		{
			try
			{
				if (save != null)
				{
					save.close();
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	public void showAnnouncements()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			if (player != null)
			{
				showAnnouncements(player);
			}
	}

	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (int i = 0; i < _announcements.size(); i++)
		{
			if (Config.ANNOUNCE_MODE.equals("l2j"))
			{
				activeChar.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Announce, activeChar.getName(), _announcements.get(i).replace("%name%", activeChar.getName())));

			}
			else if (Config.ANNOUNCE_MODE.equals("l2off"))
			{
				activeChar.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Critical_Announce, activeChar.getName(), _announcements.get(i).replace("%name%", activeChar.getName())));
			}
		}
		for (int i = 0; i < _eventAnnouncements.size(); i++)
		{
			List<Object> entry = _eventAnnouncements.get(i);

			DateRange validDateRange = (DateRange) entry.get(0);
			String[] msg = (String[]) entry.get(1);
			Date currentDate = new Date();

			if (validDateRange.isValid() && validDateRange.isWithinRange(currentDate))
			{
				for (String element : msg)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString(element));
				}
			}
		}
	}
}
/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.communitybbs;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.dream.Config;
import com.dream.game.cache.HtmCache;
import com.dream.game.communitybbs.Manager.BaseBBSManager;
import com.dream.game.communitybbs.Manager.BuffBBSManager;
import com.dream.game.communitybbs.Manager.ClanBBSManager;
import com.dream.game.communitybbs.Manager.PostBBSManager;
import com.dream.game.communitybbs.Manager.RegionBBSManager;
import com.dream.game.communitybbs.Manager.RepairBBSManager;
import com.dream.game.communitybbs.Manager.TeleBBSManager;
import com.dream.game.communitybbs.Manager.TopBBSManager;
import com.dream.game.communitybbs.Manager.TopicBBSManager;
import com.dream.game.handler.IBBSHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.multisell.L2Multisell;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ShowBoard;
import com.dream.game.network.serverpackets.SystemMessage;

public class CommunityBoard
{
	private static class SingletonHolder
	{
		protected static final CommunityBoard _instance = new CommunityBoard();
	}

	public static CommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<String, IBBSHandler> _handlers;

	public CommunityBoard()
	{
		_handlers = new HashMap<>();
	}

	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();

		if (activeChar == null)
			return;

		if (Config.COMMUNITY_TYPE.equals("full"))
		{
			String cmd = command.substring(4);
			String params = "";
			int iPos = cmd.indexOf(" ");
			if (iPos != -1)
			{
				params = cmd.substring(iPos + 1);
				cmd = cmd.substring(0, iPos);
			}

			IBBSHandler bbsh = _handlers.get(cmd);
			if (bbsh != null)
			{
				bbsh.handleCommand(cmd, activeChar, params);
			}
			else if (command.startsWith("_bbsclan"))
			{
				ClanBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbsmemo"))
			{
				TopicBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbsgetfav"))
			{
				String text = HtmCache.getInstance().getHtm("data/html/communityboard/favorites.htm");
				if (text != null)
				{
					text = text.replace("%username%", activeChar.getName());
					text = text.replace("%charId%", String.valueOf(activeChar.getObjectId()));
					BaseBBSManager.separateAndSend(text, activeChar);
				}
				else
				{
					activeChar.sendPacket(new ShowBoard("<html><body><br><br><br><br></body></html>", "101"));
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}

			}
			else if (command.startsWith("_bbstopics"))
			{
				TopicBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbsposts"))
			{
				PostBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbstop"))
			{
				TopBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbshome"))
			{
				TopBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbsloc"))
			{
				RegionBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbsbuff"))
			{
				BuffBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbstele"))
			{
				TeleBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbsrepair"))
			{
				RepairBBSManager.getInstance().parsecmd(command, activeChar);
			}
			else if (command.startsWith("_bbsshop"))
			{
				String htm = HtmCache.getInstance().getHtm("data/html/communityboard/custom/shop.htm");
				if (htm != null)
				{
					htm = htm.replace("%username%", activeChar.getName());
					BaseBBSManager.separateAndSend(htm, activeChar);
				}
				else
				{
					activeChar.sendPacket(new ShowBoard("<html><body><br><br><br><br></body></html>", "101"));
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}

			}
			else if (command.startsWith("_bbsmultisell;"))
			{
				/**
				 * Usage: For ex: bypass _bbsmultisell;shop;9910 Where "shop" - its html where need back after open multisell, cuz if we dont enter this value html just "freeze" any bypass doesnt works. Where "9910" its multisell id, and also we need in multisell value: <list isCommunity="true"> to attemp phx bypass to other multisell.
				 */
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
				L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(st.nextToken()), activeChar, false, 0);
			}
			else
			{
				activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101"));
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));

			}

		}
		else if (Config.COMMUNITY_TYPE.equals("old"))
		{
			RegionBBSManager.getInstance().parsecmd(command, activeChar);
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
		}

		activeChar = null;
	}

	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();

		if (activeChar == null)
			return;

		if (Config.COMMUNITY_TYPE.equals("full"))
		{
			if (url.equals("Topic"))
			{
				TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Post"))
			{
				PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Region"))
			{
				RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else if (url.equals("Notice"))
			{
				ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
			}
			else
			{
				activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>the command: " + url + " is not implemented yet</center><br><br></body></html>", "101"));
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
		}
		else if (Config.COMMUNITY_TYPE.equals("old"))
		{
			RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
		}
		else
		{
			activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>The Community board is currently disable</center><br><br></body></html>", "101"));
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}

		activeChar = null;
	}

	public void registerBBSHandler(IBBSHandler handler)
	{
		for (String s : handler.getBBSCommands())
		{
			_handlers.put(s, handler);
		}
	}
}
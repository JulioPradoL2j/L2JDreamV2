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
package com.dream.game.communitybbs.Manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.dream.Config;
import com.dream.game.L2GameServer;
import com.dream.game.model.BlockList;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ShowBoard;
import com.dream.game.network.serverpackets.SystemMessage;

import javolution.util.FastMap;

public class RegionBBSManager extends BaseBBSManager
{
	private static class SingletonHolder
	{
		protected static final RegionBBSManager _instance = new RegionBBSManager();
	}

	private static Logger _logChat = Logger.getLogger("chat");

	private static FastMap<Integer, List<L2PcInstance>> _onlinePlayers = new FastMap<Integer, List<L2PcInstance>>().shared();

	private static FastMap<Integer, FastMap<String, String>> _communityPages = new FastMap<Integer, FastMap<String, String>>().shared();

	public static RegionBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static List<L2PcInstance> getOnlinePlayers(int page)
	{
		return _onlinePlayers.get(page);
	}

	private static void showOldCommunityPI(L2PcInstance activeChar, String name)
	{
		StringBuilder htmlCode = new StringBuilder("<html><body><br>");
		htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>" + "Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
		L2PcInstance player = L2World.getInstance().getPlayer(name);

		if (player != null)
		{
			if (player.getAppearance().getSex())
			{

			}

			String levelApprox = "low";

			if (player.getLevel() >= 60)
			{
				levelApprox = "very high";
			}
			else if (player.getLevel() >= 40)
			{
				levelApprox = "high";
			}
			else if (player.getLevel() >= 20)
			{
				levelApprox = "medium";
			}

			htmlCode.append("<tr><td>Level: " + levelApprox + "</td></tr>");
			htmlCode.append("<tr><td><br></td></tr>");

			levelApprox = null;

			if (activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId() || Config.SHOW_LEVEL_COMMUNITYBOARD))
			{
				long nextLevelExp = 0;
				long nextLevelExpNeeded = 0;

				if (player.getLevel() < Experience.MAX_LEVEL - 1)
				{
					nextLevelExp = player.getLevel() + 1;
					nextLevelExpNeeded = nextLevelExp - player.getExp();
				}

				htmlCode.append("<tr><td>Level: " + player.getLevel() + "</td></tr>");
				htmlCode.append("<tr><td>Experience: " + player.getExp() + "/" + nextLevelExp + "</td></tr>");
				htmlCode.append("<tr><td>Experience needed for level up: " + nextLevelExpNeeded + "</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
			}

			int uptime = (int) player.getUptime() / 1000;
			int h = uptime / 3600;
			int m = (uptime - h * 3600) / 60;
			int s = uptime - h * 3600 - m * 60;

			htmlCode.append("<tr><td>Uptime: " + h + "h " + m + "m " + s + "s</td></tr>");
			htmlCode.append("<tr><td><br></td></tr>");

			if (player.getClan() != null)
			{
				htmlCode.append("<tr><td>Clan: " + player.getClan().getName() + "</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
			}

			htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM " + player.getName() + " pm pm pm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td><br><button value" + "=\"Back\" action=\"bypass _bbsloc\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
			htmlCode.append("</td></tr></table>");
			htmlCode.append("</body></html>");
			separateAndSend(htmlCode.toString(), activeChar);
		}
		else
		{
			activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>No player with name " + name + "</center><br><br></body></html>", "101"));
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	private int _onlineCount = 0;
	private int _onlineCountGm = 0;
	private L2PcInstance receiver;

	private void addOnlinePlayer(L2PcInstance player)
	{
		boolean added = false;

		for (List<L2PcInstance> page : _onlinePlayers.values())
			if (page.size() < Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				if (!page.contains(player))
				{
					page.add(player);

					if (!player.getAppearance().isInvisible())
					{
						_onlineCount++;
					}

					_onlineCountGm++;
				}

				added = true;
				break;
			}
			else if (page.contains(player))
			{
				added = true;
				break;
			}

		if (!added)
		{
			List<L2PcInstance> temp = new ArrayList<>();
			int page = _onlinePlayers.size() + 1;
			if (temp.add(player))
			{
				_onlinePlayers.put(page, temp);
				if (!player.getAppearance().isInvisible())
				{
					_onlineCount++;
				}
				_onlineCountGm++;
			}
		}
	}

	public synchronized void changeCommunityBoard()
	{
		Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
		List<L2PcInstance> sortedPlayers = new ArrayList<>();
		sortedPlayers.addAll(players);
		players = null;

		Collections.sort(sortedPlayers, new Comparator<L2PcInstance>()
		{
			@Override
			public int compare(L2PcInstance p1, L2PcInstance p2)
			{
				return p1.getName().compareToIgnoreCase(p2.getName());
			}

			@Override
			public Comparator<L2PcInstance> reversed()
			{
				return null;
			}

			@Override
			public Comparator<L2PcInstance> thenComparing(Comparator<? super L2PcInstance> other)
			{
				return null;
			}

			@Override
			public <U extends Comparable<? super U>> Comparator<L2PcInstance> thenComparing(Function<? super L2PcInstance, ? extends U> keyExtractor)
			{
				return null;
			}

			@Override
			public <U> Comparator<L2PcInstance> thenComparing(Function<? super L2PcInstance, ? extends U> keyExtractor, Comparator<? super U> keyComparator)
			{
				return null;
			}

			@Override
			public Comparator<L2PcInstance> thenComparingDouble(ToDoubleFunction<? super L2PcInstance> keyExtractor)
			{
				return null;
			}

			@Override
			public Comparator<L2PcInstance> thenComparingInt(ToIntFunction<? super L2PcInstance> keyExtractor)
			{
				return null;
			}

			@Override
			public Comparator<L2PcInstance> thenComparingLong(ToLongFunction<? super L2PcInstance> keyExtractor)
			{
				return null;
			}
		});

		_onlinePlayers.clear();
		_onlineCount = 0;
		_onlineCountGm = 0;

		for (L2PcInstance player : sortedPlayers)
		{
			addOnlinePlayer(player);
		}

		sortedPlayers = null;
		_communityPages.clear();
		writeCommunityPages();
	}

	public String getCommunityPage(int page, String type)
	{
		if (_communityPages.get(page) != null)
			return _communityPages.get(page).get(type);
		return null;
	}

	private int getOnlineCount(String type)
	{
		if (type.equalsIgnoreCase("gm"))
			return _onlineCountGm;
		return _onlineCount;
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("_bbsloc"))
		{
			showOldCommunity(activeChar, 1);
		}
		else if (command.startsWith("_bbsloc;page;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int page = 0;

			try
			{
				page = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException nfe)
			{

			}

			showOldCommunity(activeChar, page);
		}
		else if (command.startsWith("_bbsloc;playerinfo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String name = st.nextToken();

			showOldCommunityPI(activeChar, name);
		}
		else if (Config.COMMUNITY_TYPE.equals("old"))
		{
			showOldCommunity(activeChar, 1);
		}
		else
		{
			activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101"));
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;

		if (ar1.equals("PM"))
		{
			StringBuilder htmlCode = new StringBuilder("<html><body><br>");
			htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>" + "Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

			try
			{

				receiver = L2World.getInstance().getPlayer(ar2);

				if (receiver == null)
				{
					htmlCode.append("Player not found!<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;" + ar2 + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					htmlCode.append("</td></tr></table></body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
					return;
				}

				if (receiver.isInJail())
				{
					activeChar.sendMessage("Player is in jail.");
					return;
				}

				if (receiver.isChatBanned())
				{
					activeChar.sendMessage("Player is chat banned.");
					return;
				}

				if (activeChar.isInJail())
				{
					activeChar.sendMessage("You can not chat while in jail.");
					return;
				}

				if (Config.LOG_CHAT)
				{
					LogRecord record = new LogRecord(Level.INFO, ar3);
					record.setLoggerName("chat");
					record.setParameters(new Object[]
					{
						"TELL ",
						"[" + activeChar.getName() + " to " + receiver.getName() + "]"
					});
					_logChat.log(record);
					record = null;
				}

				ar3 = ar3.replaceAll("\\\\n", "");

				if (receiver != null && !BlockList.isBlocked(receiver, activeChar))
				{
					if (!receiver.getMessageRefusal())
					{
						receiver.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Custom, activeChar.getName(), ar3));
						activeChar.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_Custom, "->" + receiver.getName(), ar3));
						htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;" + receiver.getName() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						htmlCode.append("</td></tr></table></body></html>");
						separateAndSend(htmlCode.toString(), activeChar);
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
						parsecmd("_bbsloc;playerinfo;" + receiver.getName(), activeChar);
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_ONLINE).addString(receiver.getName()));
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{

			}
		}
		else
		{
			activeChar.sendPacket(new ShowBoard("<html><body><br><br><center>the command: " + ar1 + " is not implemented yet</center><br><br></body></html>", "101"));
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	private void showOldCommunity(L2PcInstance activeChar, int page)
	{
		separateAndSend(getCommunityPage(page, activeChar.isGM() ? "gm" : "pl"), activeChar);
	}

	private void writeCommunityPages()
	{
		for (int page : _onlinePlayers.keySet())
		{
			FastMap<String, String> communityPage = new FastMap<>();

			StringBuilder htmlCode = new StringBuilder("<html><body><br>");
			String tdClose = "</td>";
			String tdOpen = "<td align=left valign=top>";
			String trClose = "</tr>";
			String trOpen = "<tr>";
			String colSpacer = "<td FIXWIDTH=15></td>";

			htmlCode.append("<table>");

			htmlCode.append(trOpen);
			htmlCode.append("<td align=left valign=top>Server Restarted: " + L2GameServer._serverStarted.getTime() + tdClose);
			htmlCode.append(trClose);

			htmlCode.append("</table>");

			htmlCode.append("<table>");

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + "XP Rate: x" + Config.RATE_XP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Party XP Rate: x" + Config.RATE_XP * Config.RATE_PARTY_XP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "XP Exponent: x" + Config.ALT_GAME_EXPONENT_XP + tdClose);
			htmlCode.append(trClose);

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + "SP Rate: x" + Config.RATE_SP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Party SP Rate: x" + Config.RATE_SP * Config.RATE_PARTY_SP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "SP Exponent: x" + Config.ALT_GAME_EXPONENT_SP + tdClose);
			htmlCode.append(trClose);

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + "Drop Rate: x" + Config.RATE_DROP_ITEMS + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Spoil Rate: x" + Config.RATE_DROP_SPOIL + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Adena Rate: x" + Config.RATE_DROP_ADENA + tdClose);
			htmlCode.append(trClose);

			htmlCode.append("</table>");

			htmlCode.append("<table>");
			htmlCode.append(trOpen);
			htmlCode.append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>");
			htmlCode.append(trClose);

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + L2World.getInstance().getAllVisibleObjectsCount() + " Object count</td>");
			htmlCode.append(trClose);

			htmlCode.append(trOpen);
			if (Config.ONLINE_COMMUNITY_BOARD)
			{
				htmlCode.append(tdOpen + getOnlineCount("gm") + " Player(s) Online: ");
				if (Config.COLOR_COMMUNITY_BOARD)
				{
					htmlCode.append("<font color=\"00FF00\">Game Master</font>, ");
					htmlCode.append("<font color=\"DD9537\">Noble</font>, ");
					htmlCode.append("<font color=\"5192B0\">Hero</font>, ");
					htmlCode.append("<font color=\"DA00C3\">Karma</font>, ");
					htmlCode.append("<font color=\"E41E00\">Cursed</font>, ");
					htmlCode.append("<font color=\"979797\">Jailled</font>.");
				}
				htmlCode.append("</td>");
			}
			htmlCode.append(trClose);
			htmlCode.append("</table>");

			htmlCode.append("<table border=0>");
			htmlCode.append("<tr><td><table border=0>");

			int cell = 0;

			for (L2PcInstance player : getOnlinePlayers(page))
			{
				cell++;

				if (cell == 1)
				{
					htmlCode.append(trOpen);
				}

				if (Config.ONLINE_COMMUNITY_BOARD)
				{
					htmlCode.append("<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;" + player.getName() + "\">");

					if (Config.COLOR_COMMUNITY_BOARD)
					{
						if (player.isGM())
						{
							htmlCode.append("<font color=\"00FF00\">" + player.getName() + "</font>");
						}
						else if (player.isNoble())
						{
							htmlCode.append("<font color=\"DD9537\">" + player.getName() + "</font>");
						}
						else if (player.isHero())
						{
							htmlCode.append("<font color=\"5192B0\">" + player.getName() + "</font>");
						}
						else if (player.isCursedWeaponEquipped())
						{
							htmlCode.append("<font color=\"E41E00\">" + player.getName() + "</font>");
						}
						else if (player.isInJail())
						{
							htmlCode.append("<font color=\"979797\">" + player.getName() + "</font>");
						}
						else if (player.getKarma() > 0)
						{
							htmlCode.append("<font color=\"0000FF\">" + player.getName() + "</font>");
						}
						else
						{
							htmlCode.append(player.getName());
						}
					}
					else if (player.isGM())
					{
						htmlCode.append("<font color=\"LEVEL\">" + player.getName() + "</font>");
					}
					else
					{
						htmlCode.append(player.getName());
						htmlCode.append("</a></td>");
					}

					if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						htmlCode.append(colSpacer);
					}

					if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
			}
			if (cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
			{
				htmlCode.append(trClose);
			}
			htmlCode.append("</table><br></td></tr>");

			htmlCode.append(trOpen);
			htmlCode.append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>");
			htmlCode.append(trClose);

			htmlCode.append("</table>");

			if (getOnlineCount("gm") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600>");

				htmlCode.append("<tr>");

				if (page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"Prev\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}
				else
				{
					htmlCode.append("<td align=right width=190><button value=\"Prev\" action=\"bypass _bbsloc;page;" + (page - 1) + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}

				htmlCode.append("<td FIXWIDTH=10></td>");
				htmlCode.append("<td align=center valign=top width=200>Displaying " + ((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + 1) + " - " + ((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + getOnlinePlayers(page).size()) + " player(s)</td>");
				htmlCode.append("<td FIXWIDTH=10></td>");

				if (getOnlineCount("gm") <= page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
				{
					htmlCode.append("<td width=190><button value=\"Next\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}
				else
				{
					htmlCode.append("<td width=190><button value=\"Next\" action=\"bypass _bbsloc;page;" + (page + 1) + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}

				htmlCode.append("</tr>");
				htmlCode.append("</table>");
			}

			htmlCode.append("</body></html>");

			communityPage.put("gm", htmlCode.toString());

			htmlCode = new StringBuilder("<html><body><br>");
			htmlCode.append("<table>");

			htmlCode.append(trOpen);
			htmlCode.append("<td align=left valign=top>Server Restarted: " + L2GameServer._serverStarted.getTime() + tdClose);
			htmlCode.append(trClose);

			htmlCode.append("</table>");

			htmlCode.append("<table>");

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + "XP Rate: " + Config.RATE_XP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Party XP Rate: " + Config.RATE_PARTY_XP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "XP Exponent: " + Config.ALT_GAME_EXPONENT_XP + tdClose);
			htmlCode.append(trClose);

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + "SP Rate: " + Config.RATE_SP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Party SP Rate: " + Config.RATE_PARTY_SP + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "SP Exponent: " + Config.ALT_GAME_EXPONENT_SP + tdClose);
			htmlCode.append(trClose);

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + "Drop Rate: " + Config.RATE_DROP_ITEMS + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Spoil Rate: " + Config.RATE_DROP_SPOIL + tdClose);
			htmlCode.append(colSpacer);
			htmlCode.append(tdOpen + "Adena Rate: " + Config.RATE_DROP_ADENA + tdClose);
			htmlCode.append(trClose);

			htmlCode.append("</table>");

			htmlCode.append("<table>");
			htmlCode.append(trOpen);
			htmlCode.append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>");
			htmlCode.append(trClose);

			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + getOnlineCount("pl") + " Player(s) Online</td>");
			htmlCode.append(trClose);
			htmlCode.append("</table>");

			htmlCode.append("<table border=0>");
			htmlCode.append("<tr><td><table border=0>");

			cell = 0;
			for (L2PcInstance player : getOnlinePlayers(page))
			{
				if (player == null || player.getAppearance().isInvisible())
				{
					continue;
				}

				cell++;

				if (cell == 1)
				{
					htmlCode.append(trOpen);
				}

				htmlCode.append("<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;" + player.getName() + "\">");

				if (player.isGM())
				{
					htmlCode.append("<font color=\"LEVEL\">" + player.getName() + "</font>");
				}
				else
				{
					htmlCode.append(player.getName());
				}

				htmlCode.append("</a></td>");

				if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
				{
					htmlCode.append(colSpacer);
				}

				if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
				{
					cell = 0;
					htmlCode.append(trClose);
				}
			}
			if (cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
			{
				htmlCode.append(trClose);
			}

			htmlCode.append("</table><br></td></tr>");

			htmlCode.append(trOpen);
			htmlCode.append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>");
			htmlCode.append(trClose);

			htmlCode.append("</table>");

			if (getOnlineCount("pl") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600>");

				htmlCode.append("<tr>");

				if (page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"Prev\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}
				else
				{
					htmlCode.append("<td align=right width=190><button value=\"Prev\" action=\"bypass _bbsloc;page;" + (page - 1) + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}

				htmlCode.append("<td FIXWIDTH=10></td>");
				htmlCode.append("<td align=center valign=top width=200>Displaying " + ((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + 1) + " - " + ((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD + getOnlinePlayers(page).size()) + " player(s)</td>");
				htmlCode.append("<td FIXWIDTH=10></td>");

				if (getOnlineCount("pl") <= page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
				{
					htmlCode.append("<td width=190><button value=\"Next\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}
				else
				{
					htmlCode.append("<td width=190><button value=\"Next\" action=\"bypass _bbsloc;page;" + (page + 1) + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
				}

				htmlCode.append("</tr>");
				htmlCode.append("</table>");
			}

			htmlCode.append("</body></html>");

			communityPage.put("pl", htmlCode.toString());

			_communityPages.put(page, communityPage);
		}
	}
}
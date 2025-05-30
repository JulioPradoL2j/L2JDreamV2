package com.dream.game.model.actor.instance;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.game.model.multisell.L2Multisell;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.olympiad.OlympiadManager;
import com.dream.game.network.serverpackets.ExHeroList;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2OlympiadManagerInstance extends L2NpcInstance
{
	private final static Logger _log = Logger.getLogger(L2OlympiadManagerInstance.class.getName());
	private static final int GATE_PASS = 6651;

	public L2OlympiadManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("OlympiadDesc"))
		{
			int val = Integer.parseInt(command.substring(13, 14));
			String suffix = command.substring(14);
			showChatWindow(player, val, suffix);
		}
		else if (command.startsWith("OlympiadNoble"))
		{
			if (!player.isNoble() || player.getClassId().level() < 3)
				return;

			int val = Integer.parseInt(command.substring(14));
			NpcHtmlMessage reply;
			StringBuilder replyMSG;
			switch (val)
			{
				case 1:
					Olympiad.unRegisterNoble(player);
					break;
				case 2:
					int classed = 0;
					int nonClassed = 0;
					int[] array = Olympiad.getWaitingList();

					if (array != null)
					{
						classed = array[0];
						nonClassed = array[1];
					}
					reply = new NpcHtmlMessage(getObjectId());
					reply.setFile("data/html/olympiad/await.htm");
					reply.replace("%classed%", String.valueOf(classed));
					reply.replace("%nonClassesd%", String.valueOf(nonClassed));
					reply.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(reply);
					break;
				case 3:
					int points = Olympiad.getNoblePoints(player.getObjectId());
					if (points >= 0)
					{
						reply = new NpcHtmlMessage(getObjectId());
						replyMSG = new StringBuilder("<html><body>");
						replyMSG.append("There are " + points + " Grand Olympiad " + "points granted for this event.<br><br>" + "<a action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 2a\">Back</a>");
						replyMSG.append("</body></html>");
						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 4:
					Olympiad.getInstance().registerNoble(player, false);
					break;
				case 5:
					Olympiad.getInstance().registerNoble(player, true);
					break;
				case 6:
					int passes = Olympiad.getInstance().getNoblessePasses(player.getObjectId());
					if (passes > 0)
					{
						player.addItem("Olympiad", GATE_PASS, passes, player, true, true);
					}
					else
					{
						reply = new NpcHtmlMessage(getObjectId());
						replyMSG = new StringBuilder("<html><body>");
						replyMSG.append("The Manager Of The Great Olympics :<br>" + "Sorry, you do not have enough points to exchange for Noblesse Gate Pass. Try next time.<br>" + "<a action=\"bypass -h npc_" + getObjectId() + "_OlympiadDesc 4a\">Back</a>");
						replyMSG.append("</body></html>");
						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 7:
					L2Multisell.getInstance().SeparateAndSend(102, player, false, getCastle().getTaxRate());
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else if (command.startsWith("Olympiad"))
		{
			if (player._event != null)
				return;
			int val = Integer.parseInt(command.substring(9, 10));
			NpcHtmlMessage reply = new NpcHtmlMessage(getObjectId());
			StringBuilder replyMSG = new StringBuilder("<html><body><br>");

			switch (val)
			{
				case 1:
					Map<Integer, String> matches = OlympiadManager.getInstance().getAllTitles();
					replyMSG.append("Observation of the great Olympics<br>" + "Warning: you will not be able to watch the Olympics, if you called a pet or a servant.<br><br>");

					for (int i = 0; i < Olympiad.getStadiumCount(); i++)
					{
						int arenaID = i + 1;
						String title = "";
						if (matches.containsKey(i))
						{
							title = matches.get(i);
						}
						else
						{
							title = "Preparation";
						}
						replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_Olympiad 3_" + i + "\">" + "Arena-" + arenaID + "&nbsp;&nbsp;&nbsp;" + title + "</a><br>");
					}
					replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
					replyMSG.append("<table width=270 border=0 cellpadding=0 cellspacing=0>");
					replyMSG.append("<tr><td width=90 height=20 align=center>");
					replyMSG.append("<button value=\"Back\" action=\"bypass -h npc_" + getObjectId() + "_Chat 0\" width=80 height=27 back=\"sek.cbui94\" fore=\"L2UI_CT1.Button_DF\">");
					replyMSG.append("</td></tr></table></body></html>");

					reply.setHtml(replyMSG.toString());
					player.sendPacket(reply);
					break;
				case 2:
					int classId = Integer.parseInt(command.substring(11));
					if (classId >= 88 && classId <= 118)
					{
						replyMSG.append("<center>The Ranks Of The Great Olympics");
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");

						List<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
						if (!names.isEmpty())
						{
							replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");
							int index = 1;
							for (String name : names)
							{
								replyMSG.append("<tr>");
								replyMSG.append("<td align=\"left\">" + index++ + "</td>");
								replyMSG.append("<td align=\"right\">" + name + "</td>");
								replyMSG.append("</tr>");
							}
							replyMSG.append("</table>");
						}
						replyMSG.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
						replyMSG.append("<button value=\"Back\" action=\"bypass -h npc_" + getObjectId() + "_Chat 0\" back=\"sek.cbui94\" fore=\"L2UI_CT1.Button_DF\" width=80 height=27>");
						replyMSG.append("</center>");
						replyMSG.append("</body></html>");
						reply.setHtml(replyMSG.toString());
						player.sendPacket(reply);
					}
					break;
				case 3:
					int id = Integer.parseInt(command.substring(11));
					Olympiad.addSpectator(id, player, true);
					break;
				case 4:
					player.sendPacket(new ExHeroList());
					break;
				default:
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
					break;
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showChatWindow(L2PcInstance player, int val, String suffix)
	{
		String filename = Olympiad.OLYMPIAD_HTML_PATH;

		filename += "noble_desc" + val;
		filename += suffix != null ? suffix + ".htm" : ".htm";

		if (filename.equals(Olympiad.OLYMPIAD_HTML_PATH + "noble_desc0.htm"))
		{
			filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
		}
		showChatWindow(player, filename);
	}
}
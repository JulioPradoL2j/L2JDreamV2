package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.world.L2World;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public final class L2ReportInstance extends L2NpcInstance
{
	private static String _type;

	static
	{
		new File("data/reports/").mkdirs();
	}

	public L2ReportInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);

			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
		}
		else
		{
			showHtmlWindow(player);
		}

		player.sendPacket(new ActionFailed());
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("send_report"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			String msg = null;
			String type = null;
			type = st.nextToken();
			msg = st.nextToken();
			try
			{
				while (st.hasMoreTokens())
				{
					msg = msg + " " + st.nextToken();
				}

				sendReport(player, type, msg);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
	}

	
	private static void sendReport(L2PcInstance player, String command, String msg)
	{
		String type = command;
		L2GameClient info = player.getClient().getConnection().getClient();

		if (type.equals("General"))
		{
			_type = "General";
		}
		if (type.equals("Fatal"))
		{
			_type = "Fatal";
		}
		if (type.equals("Misuse"))
		{
			_type = "Misuse";
		}
		if (type.equals("Balance"))
		{
			_type = "Balance";
		}
		if (type.equals("Other"))
		{
			_type = "Other";
		}

		try
		{
			SimpleDateFormat formatter;
			formatter = new SimpleDateFormat("dd-MM-yyyy-H-mm-ss");
			String date = formatter.format(new Date());

			String fname = "data/reports/" + Config.SERVER_NAME + date + ".dream";
			File file = new File(fname);
			boolean exist = file.createNewFile();
			if (!exist)
			{
				player.sendChatMessage(0, SystemChatChannelId.Chat_Normal, "SYS", "Please try again.");
				return;
			}
			FileWriter fstream = new FileWriter(fname);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Character Info: " + info + "\r\nBug Type: " + _type + "\r\nMessage: " + msg);
			player.sendChatMessage(0, SystemChatChannelId.Chat_Normal, "SYS", "Report sent. GMs will check it soon. Thanks.");

			for (L2PcInstance allgms : L2World.getAllGMs())
			{
				allgms.sendChatMessage(0, SystemChatChannelId.Chat_Inner_Partymaster, "SYS", "Bug Report Manager " + player.getName() + " sent a bug report.");
			}
			out.close();
		}
		catch (Exception e)
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_Normal, "SYS", "Something went wrong try again.");
		}
	}

	private void showHtmlWindow(L2PcInstance activeChar)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html>");
		replyMSG.append("<title>Bug Report Manager</title>");
		replyMSG.append("<body><br><br><center>");
		replyMSG.append("<table border=0 height=10 width=240>");
		replyMSG.append("<tr><td align=center><font color=\"00FFFF\">Hello " + activeChar.getName() + ".</font></td></tr>");
		replyMSG.append("<tr><td align=center><font color=\"00FFFF\">There are no Gms online at the moment</font></td></tr>");
		replyMSG.append("<tr><td align=center><font color=\"00FFFF\">do you want to report something?</font></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<img src=\"L2UI.SquareWhite\" width=280 height=1><br><br>");
		replyMSG.append("<table width=250><tr>");
		replyMSG.append("<td><font color=\"LEVEL\">Select Report Type:</font></td>");
		replyMSG.append("<td><combobox width=105 var=type list=General;Fatal;Misuse;Balance;Other></td>");
		replyMSG.append("</tr></table><br><br>");
		replyMSG.append("<multiedit var=\"msg\" width=250 height=50><br>");
		replyMSG.append("<br><img src=\"L2UI.SquareWhite\" width=280 height=1><br><br><br><br><br><br><br>");
		replyMSG.append("<button value=\"Send Report\" action=\"bypass -h npc_" + getObjectId() + "_send_report $type $msg\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\">");
		replyMSG.append("</center></body></html>");
		nhm.setHtml(replyMSG.toString());
		activeChar.sendPacket(nhm);

		activeChar.sendPacket(new ActionFailed());
	}
}
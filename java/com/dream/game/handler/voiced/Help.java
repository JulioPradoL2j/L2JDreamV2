package com.dream.game.handler.voiced;

import com.dream.Config;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.handler.VoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class Help implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"help"
	};

	@Override
	public String getDescription(String command)
	{
		if (command.equals("help"))
			return "Displays the current menu.";
		return null;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
			return false;
		if (activeChar.isGM() == false)
			return false;

		if (command.startsWith("devinfo"))
		{
			String html = "<html><title>Server info</title><body>";
			html += "<br><center>";
			html += "Version " + "1751" + "<br1>";
			html += "Server IP " + Config.GAMESERVER_HOSTNAME + "<br>";
			html += "</center></body></html>";
			NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.setHtml(html);
			activeChar.sendPacket(msg);
		}
		else if (command.startsWith("help"))
		{

			NpcHtmlMessage help = new NpcHtmlMessage(5);
			StringBuilder html = new StringBuilder("<html><body><br>");
			html.append("<center><font color=\"LEVEL\">A list of available voice commands and their descriptions.</font><table>");
			for (String comm : VoicedCommandHandler.getInstance().getVoicedCommandHandlers().keySet())
			{
				try
				{
					if (comm.equals("devinfo"))
					{
						continue;
					}
					IVoicedCommandHandler handler = VoicedCommandHandler.getInstance().getVoicedCommandHandler(comm);
					try
					{
						String desc = handler.getDescription(comm);
						if (desc == null)
						{
							desc = "Description not available.";
						}
						html.append("<tr><td width=190><font color=\"00FF00\">" + comm + "</font></td><td> - " + desc + "</td></tr>");
					}
					catch (AbstractMethodError e)
					{

					}
				}
				catch (Exception e)
				{
					continue;
				}
			}
			html.append("</table></center>");
			html.append("</body></html>");
			help.setHtml(html.toString());
			activeChar.sendPacket(help);
			return true;
		}
		return false;
	}

}
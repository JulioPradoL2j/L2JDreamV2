package com.dream.game.access;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.util.JarUtils;

public class gmController
{
	public static Logger _log = Logger.getLogger(gmController.class.getName());

	public static Logger _logGm = Logger.getLogger("GmAccess");

	private static gmController _instance = null;

	public static gmController getInstance()
	{
		if (_instance == null)
		{
			_instance = new gmController();
		}
		return _instance;
	}

	private static void showInfo(L2PcInstance admin)
	{
		GmPlayer gm = gmCache.getInstance().getGmPlayer(admin.getObjectId());
		if (gm == null)
			return;

		String text = "";
		text += "<html><title>Gm Help</title><body>";

		if (gm.isRoot())
		{
			text += "<center><font color=\"LEVEL\">";
			text += "Your privilegies are the Root Admin!<br1>";
			text += "All commands are available!";
			text += "</font></center>";
		}
		else if (gm.isGm())
		{
			int id = 1;
			text += "<center><font color=\"LEVEL\">";
			text += "List of commands:";
			text += "</font></center><br1>";
			for (String cmd : gm.getCommands())
			{
				text += "<font color=\"LEVEL\">№ " + id + ":</font> //" + cmd + "<br1>";
				id++;
			}
		}
		else
		{
			text += "<center><font color=\"LEVEL\">";
			text += "The level of your rights is not defined!<br1>";
			text += "Can't make a list!";
			text += "</font></center>";
		}
		text += "</body></html>";

		NpcHtmlMessage htm = new NpcHtmlMessage(5);
		htm.setHtml(text);
		admin.sendPacket(htm);
	}

	private final Map<String, gmHandler> _commands = new HashMap<>();

	private gmController()
	{
		_commands.clear();
		try
		{
			for (String handler : JarUtils.enumClasses("com.dream.game.handler.admin"))
			{
				try
				{
					Class<?> _handler = Class.forName(handler);
					if (_handler != null && gmHandler.class.isAssignableFrom(_handler))
					{
						Constructor<?> ctor = _handler.getConstructor();
						if (ctor != null)
						{
							regCommand((gmHandler) ctor.newInstance());
						}
					}
				}
				catch (Exception e)
				{
					continue;
				}
			}
		}
		catch (Exception e)
		{

		}

		_log.info("Admin Handler: Loaded " + _commands.size() + " handler(s).");
	}

	public void checkAdmins()
	{
		int deleted = 0;
		GmPlayer gm = null;
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null || !player.isGM())
			{
				continue;
			}

			gm = gmCache.getInstance().getGmPlayer(player.getObjectId());
			if (gm == null)
			{
				player.setGmSetting(false, false, false, false);
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_LOST_GM_RIGHTS));
				deleted++;
			}
			else
			{
				player.setGmSetting(gm.isGm(), gm.allowFixRes(), gm.allowAltG(), gm.allowPeaceAtk());
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_EARN_GM_RIGHTS));
			}
		}

		if (deleted > 0)
		{
			_log.info("GmController: removed " + deleted + " GM player(s)");
		}
	}

	public boolean checkPrivs(L2PcInstance player)
	{
		final GmPlayer gm = gmCache.getInstance().getGmPlayer(player.getObjectId());
		if (gm != null)
		{
			if (gm.getIsTemp())
			{
				gmCache.getInstance().removeGM(player.getObjectId());
				return false;
			}
			if (gm.checkIp())
			{
				final String gmAddress = player.getHost();
				final String gmHost = player.getClient().getConnection().getInetAddress().getHostAddress();
				for (String val : gm.secureIp())
					if (gmAddress.compareTo(val) == 0 || gmHost.compareTo(val) == 0)
					{
						player.setGmSetting(gm.isGm(), gm.allowFixRes(), gm.allowAltG(), gm.allowPeaceAtk());
						return true;
					}
			}
			else
			{
				player.setGmSetting(gm.isGm(), gm.allowFixRes(), gm.allowAltG(), gm.allowPeaceAtk());
				return true;
			}
		}
		return false;
	}

	public boolean cmdExists(String command)
	{
		if (_commands.get(command) != null)
			return true;
		return false;
	}

	public boolean hasAccess(String command, int objId)
	{
		GmPlayer gm = gmCache.getInstance().getGmPlayer(objId);
		if (gm == null)
			return false;

		if (_commands.get(command) == null)
			return false;

		if (gm.isRoot())
			return true;
		for (String cmd : gm.getCommands())
			if (command.equals(cmd))
				return true;
		return false;
	}

	public void regCommand(gmHandler handler)
	{
		String[] cmd = handler.getCommandList();
		for (String name : cmd)
		{
			if (name.startsWith("admin_"))
			{
				name = name.substring(6);
			}
			_commands.put(name, handler);
		}
	}

	public void showCommands(L2PcInstance pc)
	{
		GmPlayer gm = gmCache.getInstance().getGmPlayer(pc.getObjectId());
		for (String cmd : _commands.keySet())
		{
			String state = "disabled";
			if (!gm.isRoot())
			{
				if (gm.getCommands().contains(cmd))
				{
					state = "enabled";
				}
			}
			else
			{
				state = "enabled";
			}
			_logGm.info("//" + cmd + ", " + state + " for " + pc.getName());
		}
		pc.sendMessage("All GM commands send to GM log");
	}

	public void useCommand(L2PcInstance player, String... params)
	{
		if (!player.isGM())
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_INSUFFICIENT_RIGHT));
			return;
		}
		GmPlayer gm = gmCache.getInstance().getGmPlayer(player.getObjectId());
		if (gm == null)
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_INSUFFICIENT_RIGHT));
			return;
		}

		final String command = params[0];

		if (command.equals("info"))
		{
			showInfo(player);
			return;
		}

		gmHandler hadler = _commands.get(command);
		if (hadler == null)
		{
			player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_GM_COMMAND_NOT_EXIST), command));
			return;
		}

		if (!gm.isRoot())
		{
			boolean result = false;
			for (String cmd : gm.getCommands())
				if (command.equals(cmd))
				{
					result = true;
					break;
				}

			if (!result)
			{
				player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_NO_RIGHTS_USE_COMMAND), command));
				return;
			}
		}

		if (Config.GM_AUDIT)
		{
			String para = "Used: " + params[0];
			for (int x = 1; x < params.length; x++)
			{
				para += " " + params[x];
			}
			_logGm.info("GM: " + player.getName() + " use admin command: " + params[0] + " to target: " + player.getTarget() + ". Full cmd (" + para + ").");
		}
		hadler.runCommand(player, params);
	}

}
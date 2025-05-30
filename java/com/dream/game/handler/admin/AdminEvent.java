package com.dream.game.handler.admin;

import com.dream.game.access.gmController;
import com.dream.game.access.gmHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.model.entity.events.GameEventManager;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class AdminEvent extends gmHandler
{
	private static final String[] commands =
	{
		"events"
	};

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

		if (params[0].equals("events"))
		{
			if (params.length == 3)
			{
				GameEvent evt = GameEventManager.getInstance().findEvent(params[1]);
				if (evt != null)
				{
					if (params[2].equals("START"))
					{
						if (evt.getState() == GameEvent.STATE_INACTIVE)
						{
							evt.start();
						}
					}
					else if (params[2].equals("STOP"))
					{
						if (evt.getState() != GameEvent.STATE_INACTIVE)
						{
							evt.finish();
						}
					}
				}
			}

			NpcHtmlMessage msg = new NpcHtmlMessage(1);
			msg.setFile("data/html/admin/menus/events.htm");
			String html = "";

			for (GameEvent evt : GameEventManager.getInstance().getAllEvents())
			{
				html += "<tr><td>";

				if (gmController.getInstance().cmdExists(evt.getName()))
				{
					html += "<a action=\"bypass admin_" + evt.getName() + "\">" + evt.getName() + "</a>";
				}
				else
				{
					html += evt.getName();
				}

				html += "</td><td><font color=\"LEVEL\">";

				if (evt.getState() == GameEvent.STATE_INACTIVE)
				{
					html += "Stoped";
				}
				else if (evt.getState() == GameEvent.STATE_ACTIVE)
				{
					html += "Registering";
				}
				else if (evt.getState() == GameEvent.STATE_RUNNING)
				{
					html += "Running";
				}

				html += "</font></td><td>";

				html += "<button width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_events ";
				html += evt.getName() + " " + (evt.getState() == GameEvent.STATE_INACTIVE ? "START" : "STOP");
				html += "\" value=\"";
				html += (evt.getState() == GameEvent.STATE_INACTIVE ? "START" : "STOP") + "\"></td>";
				html += "</tr>";
			}
			msg.replace("%events%", html);
			admin.sendPacket(msg);
		}
		return;
	}
}
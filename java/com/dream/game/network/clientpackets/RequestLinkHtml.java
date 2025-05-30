package com.dream.game.network.clientpackets;

import org.apache.log4j.Logger;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcHtmlMessage;

public class RequestLinkHtml extends L2GameClientPacket
{
	public static Logger _log = Logger.getLogger(RequestLinkHtml.class.getName());
	private String _link;

	@Override
	protected void readImpl()
	{
		_link = readS();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if (actor == null)
			return;

		_link = readS();

		if (_link.contains("..") || !_link.contains(".htm"))
		{
			_log.warn("[RequestLinkHtml] hack by " + actor.getName() + "? link contains prohibited characters: '" + _link + "', skipped");
			return;
		}

		try
		{
			String filename = "data/html/" + _link;
			NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.setFile(filename);
			L2Npc npc = actor.getLastFolkNPC();
			if (npc != null)
			{
				msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
			}
			sendPacket(msg);
		}
		catch (Exception e)
		{
			_log.warn("Bad RequestLinkHtml: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
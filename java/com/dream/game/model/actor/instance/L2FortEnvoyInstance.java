package com.dream.game.model.actor.instance;

import java.util.StringTokenizer;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2FortEnvoyInstance extends L2Npc
{
	public L2FortEnvoyInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);

			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
		}
		else
		{
			showMessageWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		String par = "";

		if (st.countTokens() >= 1)
		{
			par = st.nextToken();
		}

		if (actualCommand.equalsIgnoreCase("select"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(par);
			}
			catch (IndexOutOfBoundsException ioobe)
			{

			}
			catch (NumberFormatException nfe)
			{

			}
			int castleId = 0;
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			if (val == 2)
			{
				castleId = getFort().getCastleIdFromEnvoy(getNpcId());
				if (CastleManager.getInstance().getCastleById(castleId).getOwnerId() < 1)
				{
					html.setHtml("<html><body>An Alliance is currently impossible.<br> Castle " + CastleManager.getInstance().getCastleById(castleId).getName() + " currently has the Lord.</body></html>");
					player.sendPacket(html);
					return;
				}
			}
			getFort().setFortState(val, castleId);
			html.setFile("data/html/fortress/envoy-ok.htm");
			html.replace("%castleName%", String.valueOf(CastleManager.getInstance().getCastleById(getFort().getCastleIdFromEnvoy(getNpcId())).getName()));
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		String filename;
		if (!player.isClanLeader() || player.getClan() == null || getFort().getFortId() != player.getClan().getHasFort())
		{
			filename = "data/html/fortress/envoy-noclan.htm";
		}
		else if (getFort().getFortState() == 0)
		{
			filename = "data/html/fortress/envoy.htm";
		}
		else
		{
			filename = "data/html/fortress/envoy-no.htm";
		}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%castleName%", String.valueOf(CastleManager.getInstance().getCastleById(getFort().getCastleIdFromEnvoy(getNpcId())).getName()));
		player.sendPacket(html);
	}
}
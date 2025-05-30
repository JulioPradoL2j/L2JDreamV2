package com.dream.game.model.actor.instance;

import java.util.StringTokenizer;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Clan;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public final class L2MercManagerInstance extends L2MerchantInstance
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;

	public L2MercManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		player.setLastFolkNPC(this);

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
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;

		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		else if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken();

			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if (actualCommand.equalsIgnoreCase("hire"))
			{
				if (val.isEmpty())
					return;

				showBuyWindow(player, Integer.parseInt(val));
				return;
			}
		}

		super.onBypassFeedback(player, command);
	}

	public void showMessageWindow(L2PcInstance player)
	{
		String filename = "data/html/mercmanager/mercmanager-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			filename = "data/html/mercmanager/mercmanager-busy.htm";
		}
		else if (condition == COND_OWNER)
		{
			filename = "data/html/mercmanager/mercmanager.htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private int validateCondition(L2PcInstance player)
	{
		if (getCastle() != null && getCastle().getCastleId() > 0)
			if (player.getClan() != null)
				if (getCastle().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				else if (getCastle().getOwnerId() == player.getClanId())
					if ((player.getClanPrivileges() & L2Clan.CP_CS_MERCENARIES) == L2Clan.CP_CS_MERCENARIES)
						return COND_OWNER;
		return COND_ALL_FALSE;
	}
}
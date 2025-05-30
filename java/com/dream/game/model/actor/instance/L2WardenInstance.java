package com.dream.game.model.actor.instance;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2WardenInstance extends L2Npc
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public L2WardenInstance(int objectId, L2NpcTemplate template)
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
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
		}
		else
		{
			showMessageWindow(player, 0);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}

			if (quest.length() == 0)
			{
				showQuestWindow(player);
			}
			else
			{
				showQuestWindow(player, quest);
			}
		}
	}

	public void showMessageWindow(L2PcInstance player, int val)
	{
		String filename = "data/html/fortress/warden-nocondition.htm";
		if (validateCondition(player) == COND_OWNER)
			if (val == 0)
			{
				filename = "data/html/fortress/warden.htm";
			}
			else
			{
				filename = "data/html/fortress/warden-no.htm";
			}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	protected int validateCondition(L2PcInstance player)
	{
		if (player.isGM())
			return COND_OWNER;
		if (getCastle() != null && getCastle().getCastleId() > 0)
			if (player.getClan() != null)
				if (getCastle().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				else if (getCastle().getOwnerId() == player.getClanId())
					return COND_OWNER;
		if (getFort() != null && getFort().getFortId() > 0)
			if (player.getClan() != null)
				if (getFort().getSiege().getIsInProgress())
					return COND_BUSY_BECAUSE_OF_SIEGE;
				else if (getFort().getOwnerId() == player.getClanId())
					return COND_OWNER;
		return COND_ALL_FALSE;
	}
}
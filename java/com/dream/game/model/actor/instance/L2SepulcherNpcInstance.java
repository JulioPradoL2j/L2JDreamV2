package com.dream.game.model.actor.instance;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.FourSepulchersManager.FourSepulchersMausoleum;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2SepulcherNpcInstance extends L2Npc
{
	protected FourSepulchersMausoleum _mausoleum;

	public L2SepulcherNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	protected void doAction(L2PcInstance player)
	{
		super.onAction(player);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";

		if (val == 0)
		{
			filename = "" + npcId;
		}
		else
		{
			filename = npcId + "-" + val;
		}

		return "data/html/SepulcherNpc/" + filename + ".htm";
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);

			if (isAutoAttackable(player))
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

				StatusUpdate su = new StatusUpdate(this);
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			else
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			}

			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) && !isAlikeDead())
				if (Math.abs(player.getZ() - getZ()) < 400)
				{
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}

			if (!isAutoAttackable(player))
				if (!canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
				}
				else
				{
					broadcastPacket(new SocialAction(this, Rnd.get(8)));

					doAction(player);
				}
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (isBusy())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcbusy.htm");
			html.replace("%busymessage%", getBusyMessage());
			html.replace("%npcname%", getName());
			html.replace("%playername%", player.getName());
			player.sendPacket(html);
		}
		else if (command.startsWith("open_gate"))
		{
			L2ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
			if (hallsKey == null)
			{
				showChatWindow(player, "data/html/SepulcherNpc/Gatekeeper-no.htm");
			}
			else
			{
				player.destroyItem("FourSepuchers", hallsKey, this, true);
				_mausoleum.nextRoom();
			}

		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	public void setMausoleum(FourSepulchersMausoleum mausoleum)
	{
		_mausoleum = mausoleum;
	}
}
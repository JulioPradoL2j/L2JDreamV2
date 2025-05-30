package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.Announcements;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.CoupleManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.entity.Couple;
import com.dream.game.model.world.L2World;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2WeddingManagerInstance extends L2Npc
{
	public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
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
			showMessageWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public synchronized void onBypassFeedback(final L2PcInstance player, String command)
	{
		String filename = "data/html/mods/wedding/start.htm";
		String replace = "";

		if (player.getPartnerId() == 0)
		{
			filename = "data/html/mods/wedding/nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}

		L2Object obj = L2World.getInstance().findObject(player.getPartnerId());
		final L2PcInstance ptarget = obj instanceof L2PcInstance ? (L2PcInstance) obj : null;
		if (ptarget == null || ptarget.isOnline() == 0)
		{
			filename = "data/html/mods/wedding/notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}

		if (player.isMaried())
		{
			filename = "data/html/mods/wedding/already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (player.isMaryAccepted())
		{
			filename = "data/html/mods/wedding/waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AcceptWedding"))
		{
			player.setMaryAccepted(true);
			Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
			couple.marry();
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NOW_YOU_MARIED));
			player.setMaried(true);
			player.setMaryRequest(false);
			ptarget.sendMessage(Message.getMessage(ptarget, Message.MessageId.MSG_NOW_YOU_MARIED));
			ptarget.setMaried(true);
			ptarget.setMaryRequest(false);
			if (Config.WEDDING_GIVE_CUPID_BOW)
			{
				player.addItem("Cupids Bow", 9140, 1, player, true, true);
				ptarget.addItem("Cupids Bow", 9140, 1, ptarget, true, true);
				player.sendSkillList();
				ptarget.sendSkillList();
			}

			player.broadcastPacket(new MagicSkillUse(player, player, 2230, 1, 1, 0, false));
			ptarget.broadcastPacket(new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0, false));

			L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
			if (skill != null)
			{
				player.sendPacket(new MagicSkillUse(player, player, 2025, 1, 1, 0, false));
				player.broadcastPacket(new MagicSkillUse(player, player, 2025, 1, 1, 0, false));
				player.useMagic(skill, false, false);

				ptarget.sendPacket(new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0, false));
				ptarget.broadcastPacket(new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0, false));
				ptarget.useMagic(skill, false, false);
			}
			Announcements.getInstance().announceToAll("Congratulations on your, " + player.getName() + " and " + ptarget.getName() + " with your wedding!");

			filename = "data/html/mods/wedding/accepted.htm";
			replace = ptarget.getName();
			sendHtmlMessage(ptarget, filename, replace);
			if (Config.WEDDING_HONEYMOON_PORT)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						player.teleToLocation(Config.WEDDING_PORT_X, Config.WEDDING_PORT_Y, Config.WEDDING_PORT_Z);
						ptarget.teleToLocation(Config.WEDDING_PORT_X, Config.WEDDING_PORT_Y, Config.WEDDING_PORT_Z);
					}
				}, 10000);
			}
			return;
		}
		else if (command.startsWith("DeclineWedding"))
		{
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			player.setMaryAccepted(false);
			ptarget.setMaryAccepted(false);
			ptarget.sendMessage(Message.getMessage(ptarget, Message.MessageId.MSG_PARTNER_DECLINE));
			replace = ptarget.getName();
			filename = "data/html/mods/wedding/declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if (player.isMary())
		{
			if (Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			filename = "data/html/mods/wedding/ask.htm";
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AskWedding"))
			if (Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else if (player.getAdena() < Config.WEDDING_PRICE)
			{
				filename = "data/html/mods/wedding/adena.htm";
				replace = String.valueOf(Config.WEDDING_PRICE);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else
			{
				player.setMaryAccepted(true);
				ptarget.setMaryRequest(true);
				replace = ptarget.getName();
				filename = "data/html/mods/wedding/requested.htm";
				player.getInventory().reduceAdena("Wedding", Config.WEDDING_PRICE, player, player.getLastFolkNPC());
				sendHtmlMessage(player, filename, replace);
				return;
			}
		sendHtmlMessage(player, filename, replace);
	}

	private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void showMessageWindow(L2PcInstance player)
	{
		String filename = "data/html/mods/wedding/start.htm";
		String replace = "";

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		html.replace("%price%", Config.WEDDING_PRICE);
		player.sendPacket(html);
	}
}
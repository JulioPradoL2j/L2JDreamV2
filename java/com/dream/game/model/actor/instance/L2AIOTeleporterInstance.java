package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.xml.TeleportLocationTable;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.position.L2TeleportLocation;
import com.dream.game.model.restriction.AvailableRestriction;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

import java.util.StringTokenizer;

public class L2AIOTeleporterInstance extends L2NpcInstance
{
	public L2AIOTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (SiegeManager.checkIfInZone(list.getLocX(), list.getLocY(), list.getLocZ()))
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			if (TownManager.getInstance().townHasCastleInSiege(list.getLocX(), list.getLocY(), list.getLocZ()))
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
				return;
			}
			if (player.isAlikeDead())
				return;
			
			int price = list.getPrice();
			
			if (player.reduceAdena("Teleport", price, this, true))
			{
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
				if (Config.PLAYER_SPAWN_PROTECTION > 0 && !isInsidePeaceZone(player))
				{
					player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_SPAWN_PROTECTION), Config.PLAYER_SPAWN_PROTECTION));
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
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
		
		return "data/html/mods/AIOTeleporter/" + filename + ".htm";
	}
	
	@Override
	public boolean canInteract(L2PcInstance player)
	{
		// Can't interact while casting a spell.
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			return false;
		
		// Can't interact if not AIO.
		if (!player.isAio())
			return false;
		
		// Can't interact while died.
		if (player.isDead() || player.isFakeDeath())
			return false;
		
		// Can't interact sitted.
		if (player.isSitting())
			return false;
		
		// Can't interact in shop mode, or during a transaction or a request.
		if (player.isInStoreMode() || player.isProcessingTransaction())
			return false;
		
		if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
			return false;
		
		return true;
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (ObjectRestrictions.getInstance().checkRestriction(player, AvailableRestriction.PlayerTeleport))
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return;
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		
		if (actualCommand.equalsIgnoreCase("goto"))
		{
			if (player.isImmobilized())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (st.countTokens() <= 0)
				return;
			
			int whereTo = Integer.parseInt(st.nextToken());
			
			doTeleport(player, whereTo);
			
			return;
			
		}
		else if (command.startsWith("Chat"))
		{
			showChatWindow(player, 0);
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/mods/AIOTeleporter/" + getNpcId() + ".htm";
		filename = getHtmlPath(getNpcId(), 0);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%player%", player.getName());
		player.sendPacket(html);
	}
	
}
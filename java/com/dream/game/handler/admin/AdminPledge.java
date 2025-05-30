package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.GMViewPledgeInfo;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.SystemMessage;

public class AdminPledge extends gmHandler
{
	private static final String[] commands =
	{
		"pledge"
	};
	private String action;

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

		final String command = params[0];

		L2Object target = admin.getTarget();
		L2PcInstance player = null;
		if (target != null && target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			admin.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		String name = player.getName();
		if (command.startsWith("pledge"))
		{
			action = null;
			String parameter = null;
			try
			{
				action = params[1];
				parameter = params[2];
			}
			catch (Exception e)
			{
			}

			if (action.equals("create"))
			{
				long cet = player.getClanCreateExpiryTime();
				player.setClanCreateExpiryTime(0);
				if (parameter == null)
					return;

				L2Clan clan = ClanTable.getInstance().createClan(player, parameter);
				if (clan != null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Clan " + parameter + " created. The Leader Of The: " + name);
				}
				else
				{
					player.setClanCreateExpiryTime(cet);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Unable to create a clan");
				}
			}
			else if (!player.isClanLeader())
			{
				admin.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(name));
				return;
			}
			else if (action.equals("dismiss"))
			{
				ClanTable.getInstance().destroyClan(player.getClanId());
				L2Clan clan = player.getClan();
				if (clan == null)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The clan is deleted");
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Unable to create a clan");
				}
			}
			else if (parameter == null)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //pledge <setlevel|rep> <number>");
			}
			else if (action.equals("info"))
			{
				admin.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
			}
			else if (action.equals("setlevel"))
			{
				int level = 0;
				try
				{
					level = Integer.parseInt(parameter);
				}
				catch (NumberFormatException nfe)
				{
				}

				if (level >= 0 && level < 12)
				{
					player.getClan().changeLevel(level);
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The level of the clan " + player.getClan().getName() + " added on " + level);
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Invalid level");
				}
			}
			else if (action.startsWith("rep"))
			{
				int points = 0;
				try
				{
					points = Integer.parseInt(parameter);
				}
				catch (NumberFormatException nfe)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //pledge <rep> <number>");
				}

				L2Clan clan = player.getClan();
				if (clan.getLevel() < 5)
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Reputation can only receive the clans who have reached level 5");
					return;
				}
				clan.setReputationScore(clan.getReputationScore() + points, true);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You " + (points > 0 ? "added " : "taken away ") + Math.abs(points) + " clan reputation points " + clan.getName() + ". Number of reputation of the clan was " + clan.getReputationScore());
			}
		}
		return;
	}
}
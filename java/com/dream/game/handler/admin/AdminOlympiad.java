package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.util.StatsSet;

public class AdminOlympiad extends gmHandler
{
	private static final String[] commands =
	{
		"saveolymp",
		"endolympiad",
		"addolypoints",
		"removeolypoints",
		"setolypoints",
		"getolypoints"
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

		String command = params[0];

		if (command.equals("saveolymp"))
		{
			try
			{
				Olympiad.getInstance().saveOlympiadStatus();
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The Olympiad successfully saved");
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "An error occurred while saving the Olympiad");
			}
			return;
		}
		else if (command.equals("endolympiad"))
		{
			try
			{
				Olympiad.getInstance().manualSelectHeroes();
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Heroes updated successfully");
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "An error occurred while updating the heroes");
			}
			return;
		}
		else if (command.startsWith("addolypoints"))
		{
			try
			{
				String val = command.substring(19);
				L2Object target = admin.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
					if (player.isNoble())
					{
						StatsSet playerStat = Olympiad.getNobleStats(player.getObjectId());
						if (playerStat == null)
						{
							admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This player hasn't played on Olympiad yet!");
							return;
						}

						int oldpoints = Olympiad.getNoblePoints(player.getObjectId());
						int points = oldpoints + Integer.parseInt(val);
						if (points > 1000)
						{
							admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You can't set more than 1000 or less than 0 Olympiad points!");
							return;
						}
						playerStat.set("olympiad_points", points);

						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + player.getName() + " now has " + points + "Olympiad points.");
					}
					else
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This player is not noblesse!");
						return;
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: target a player and write the amount of points you would like to add.");
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Example: //addolypoints 10");
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "However, keep in mind that you can't have less than 0 or more than 1000 points.");
				}

			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //addolypoints points");
			}
		}
		else if (command.startsWith("removeolypoints"))
		{
			try
			{
				String val = command.substring(22);
				L2Object target = admin.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
					if (player.isNoble())
					{
						StatsSet playerStat = Olympiad.getNobleStats(player.getObjectId());
						if (playerStat == null)
						{
							admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This player hasn't played on Olympiad yet!");
							return;
						}

						int oldpoints = Olympiad.getNoblePoints(player.getObjectId());
						int points = oldpoints - Integer.parseInt(val);
						if (points < 0)
						{
							points = 0;
						}

						playerStat.set("olympiad_points", points);

						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + player.getName() + " now has " + points + " Olympiad points.");
					}
					else
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This player is not noblesse!");
						return;
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: target a player and write the amount of points you would like to remove.");
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Example: //removeolypoints 10");
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "However, keep in mind that you can't have less than 0 or more than 1000 points.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //removeolypoints points");
			}
		}
		else if (command.startsWith("setolypoints"))
		{
			try
			{
				String val = command.substring(19);
				L2Object target = admin.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
					if (player.isNoble())
					{
						StatsSet playerStat = Olympiad.getNobleStats(player.getObjectId());
						if (playerStat == null)
						{
							admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This player hasn't played on Olympiad yet!");
							return;
						}
						if (Integer.parseInt(val) < 1 && Integer.parseInt(val) > 1000)
						{
							admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You can't set more than 1000 or less than 0 Olympiad points! or lower then 0");
							return;
						}
						playerStat.set("olympiad_points", Integer.parseInt(val));
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Player " + player.getName() + " now has " + Integer.parseInt(val) + " Olympiad points.");
					}
					else
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This player is not noblesse!");
						return;
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: target a player and write the amount of points you would like to set.");
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Example: //setolypoints 10");
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "However, keep in mind that you can't have less than 0 or more than 1000 points.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //setolypoints points");
			}
		}
		else if (command.startsWith("getolypoints"))
		{
			try
			{
				L2Object target = admin.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
					if (player.isNoble())
					{
						admin.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS).addNumber(Olympiad.getCompetitionDone(player.getObjectId())).addNumber(Olympiad.getCompetitionWon(player.getObjectId())).addNumber(Olympiad.getCompetitionLost(player.getObjectId())).addNumber(Olympiad.getNoblePoints(player.getObjectId())));
					}
					else
					{
						admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "This player is not noblesse!");
						return;
					}
				}
				else
				{
					admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You must target a player to use the command.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //getolypoints");
			}
		}
	}
}
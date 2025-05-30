package com.dream.game.handler.user;

import java.text.SimpleDateFormat;
import java.util.Map;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.handler.IUserCommandHandler;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.util.StringUtil;

public class ClanPenalty implements IUserCommandHandler
{
	private static final String NO_PENALTY = "<tr><td width=170>No penalty is imposed.</td><td width=100 align=center></td></tr>";

	private static final int[] COMMAND_IDS =
	{
		100
	};

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder content = new StringBuilder();

		// Join a clan penalty.
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			StringUtil.append(content, "<tr><td width=170>Unable to join a clan.</td><td width=100 align=center>", format.format(activeChar.getClanJoinExpiryTime()), "</td></tr>");
		}

		// Create a clan penalty.
		if (activeChar.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			StringUtil.append(content, "<tr><td width=170>Unable to create a clan.</td><td width=100 align=center>", format.format(activeChar.getClanCreateExpiryTime()), "</td></tr>");
		}

		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			// Invitation in a clan penalty.
			if (clan.getCharPenaltyExpiryTime() > System.currentTimeMillis())
			{
				StringUtil.append(content, "<tr><td width=170>Unable to invite a clan member.</td><td width=100 align=center>", format.format(clan.getCharPenaltyExpiryTime()), "</td></tr>");
			}

			// War penalty.
			if (!clan.getWarPenalty().isEmpty())
			{
				for (Map.Entry<Integer, Long> entry : clan.getWarPenalty().entrySet())
					if (entry.getValue() > System.currentTimeMillis())
					{
						final L2Clan enemyClan = ClanTable.getInstance().getClan(entry.getKey());
						if (enemyClan != null)
						{
							StringUtil.append(content, "<tr><td width=170>Unable to attack ", enemyClan.getName(), " clan.</td><td width=100 align=center>", format.format(entry.getValue()), "</td></tr>");
						}
					}
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/clan_penalty.htm");
		html.replace("%content%", content.length() == 0 ? NO_PENALTY : content.toString());
		activeChar.sendPacket(html);
		return true;
	}
}
/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.handler.admin;

import com.dream.game.access.gmHandler;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;

/**
 * @author Bluur
 */
public class AdminClanFull extends gmHandler
{
	private static final String[] commands =
	{
		"clanfull"
	};

	private final int reputation = 30000000;
	private final byte level = 8;

	// id skills
	private final int[] clanSkills =
	{
		370,
		371,
		372,
		373,
		374,
		375,
		376,
		377,
		378,
		379,
		380,
		381,
		382,
		383,
		384,
		385,
		386,
		387,
		388,
		389,
		390,
		391
	};

	@Override
	public String[] getCommandList()
	{
		return commands;
	}

	@Override
	public void runCommand(L2PcInstance admin, String... params)
	{
		final String command = params[0];
		if (command.startsWith("clanfull"))
		{
			try
			{
				L2Clan clan = ClanTable.getInstance().getClanByName(params[1]);

				if (clan == null)
				{
					admin.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
					return;
				}

				for (int s : clanSkills)
				{
					L2Skill clanSkill = SkillTable.getInstance().getInfo(s, SkillTable.getInstance().getMaxLevel(s));
					clan.addNewSkill(clanSkill);
				}

				clan.setReputationScore(clan.getReputationScore() + reputation, true);
				clan.changeLevel(level);
				clan.broadcastClanStatus();

				admin.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "The Clan " + clan.getName() + " now as Full Level and All Skill's!");
			}
			catch (IndexOutOfBoundsException e)
			{
				admin.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
			}
		}
		return;
	}
}

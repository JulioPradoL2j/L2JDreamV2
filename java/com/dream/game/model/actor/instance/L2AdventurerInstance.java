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
package com.dream.game.model.actor.instance;

import com.dream.game.network.serverpackets.ExQuestInfo;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2AdventurerInstance extends L2NpcInstance
{
	public L2AdventurerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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

		return "data/html/adventurer_guildsman/" + filename + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("raidInfo"))
		{
			int bossLevel = Integer.parseInt(command.substring(9).trim());
			String filename = "data/html/adventurer_guildsman/raid_info/info.htm";
			if (bossLevel != 0)
			{
				filename = "data/html/adventurer_guildsman/raid_info/level" + bossLevel + ".htm";
			}

			showChatWindow(player, filename);
		}
		else if (command.equalsIgnoreCase("questlist"))
		{
			player.sendPacket(ExQuestInfo.STATIC_PACKET);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
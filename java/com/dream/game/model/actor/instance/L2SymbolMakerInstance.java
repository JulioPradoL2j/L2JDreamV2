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

import com.dream.game.datatables.sql.HennaTreeTable;
import com.dream.game.network.serverpackets.HennaEquipList;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SymbolMakerInstance extends L2NpcInstance
{
	public L2SymbolMakerInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/symbolmaker/SymbolMaker.htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.equals("Draw"))
		{
			player.sendPacket(new HennaEquipList(player, HennaTreeTable.getInstance().getAvailableHenna(player.getClassId())));
			player.sendPacket(new ItemList(player, false));
		}
		else if (command.equals("RemoveList"))
		{
			showRemoveChat(player);
		}
		else if (command.startsWith("Remove "))
		{
			int slot = Integer.parseInt(command.substring(7));
			player.removeHenna(slot);
			player.sendPacket(new ItemList(player, false));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showRemoveChat(L2PcInstance player)
	{
		StringBuilder html1 = new StringBuilder("<html><body>");
		html1.append("Select the symbol that you want to remove:<br><br>");
		boolean hasHennas = false;

		for (int i = 1; i <= 3; i++)
		{
			L2HennaInstance henna = player.getHenna(i);

			if (henna != null)
			{
				hasHennas = true;
				html1.append("<a action=\"bypass -h npc_%objectId%_Remove " + i + "\">" + henna.getName() + "</a><br>");
			}
		}

		if (!hasHennas)
		{
			html1.append("You do not have symbols!");
		}

		html1.append("</body></html>");

		insertObjectIdAndShowChatWindow(player, html1.toString());
	}
}
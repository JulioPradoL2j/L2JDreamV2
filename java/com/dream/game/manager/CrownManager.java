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
package com.dream.game.manager;

import com.dream.game.datatables.CrownTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.util.ArrayUtils;

public final class CrownManager
{
	private static CrownManager _instance;

	public static void checkCrowns(L2Clan clan)
	{
		if (clan == null)
			return;

		for (L2ClanMember member : clan.getMembers())
			if (member != null && member.isOnline())
			{
				checkCrowns(member.getPlayerInstance());
			}
	}

	public static void checkCrowns(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;

		int crownId = -1;

		boolean isLeader = false;

		final L2Clan clan = activeChar.getClan();

		if (clan != null)
		{
			final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);

			if (castle != null)
			{
				crownId = CrownTable.getCrownId(castle.getCastleId());
			}

			if (clan.getLeaderId() == activeChar.getObjectId())
			{
				isLeader = true;
			}
		}

		boolean alreadyFoundCirclet = false;
		boolean alreadyFoundCrown = false;

		for (L2ItemInstance item : activeChar.getInventory().getItems())
			if (ArrayUtils.contains(CrownTable.getCrownIds(), item.getItemId()))
			{
				if (crownId > 0)
					if (item.getItemId() == crownId)
					{
						if (!alreadyFoundCirclet)
						{
							alreadyFoundCirclet = true;
							continue;
						}
					}
					else if (item.getItemId() == 6841 && isLeader)
						if (!alreadyFoundCrown)
						{
							alreadyFoundCrown = true;
							continue;
						}

				if (item.getItemId() == 6841)
				{
					activeChar.destroyItem("Removing Crown", item, activeChar, true);
				}
				else if (item.isEquipped())
				{
					activeChar.getInventory().unEquipItemInSlot(item.getLocationSlot());
				}
			}
	}

	public static CrownManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new CrownManager();
		}

		return _instance;
	}

	private CrownManager()
	{

	}
}
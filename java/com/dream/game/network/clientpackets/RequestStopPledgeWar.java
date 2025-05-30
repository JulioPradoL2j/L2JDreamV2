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
package com.dream.game.network.clientpackets;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public final class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		final L2Clan playerClan = player.getClan();
		if (playerClan == null)
			return;

		final L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
			return;

		if ((player.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		if (!playerClan.isAtWarWith(clan.getClanId()))
		{
			player.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
			return;
		}

		for (L2PcInstance member : playerClan.getOnlineMembers())
		{
			if (member.isInCombat())
			{
				player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
				return;
			}
		}

		ClanTable.getInstance().deleteClansWars(playerClan.getClanId(), clan.getClanId());

		for (L2PcInstance member : clan.getOnlineMembers())
			member.broadcastUserInfo();

		for (L2PcInstance member : playerClan.getOnlineMembers())
			member.broadcastUserInfo();
	}
}
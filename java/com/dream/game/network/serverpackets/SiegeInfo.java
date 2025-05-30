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
package com.dream.game.network.serverpackets;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.siege.Castle;

public class SiegeInfo extends L2GameServerPacket
{
	public static Logger _log = Logger.getLogger(SiegeInfo.class.getName());

	private final Castle _castle;
	private final ClanHall _clanHall;
	private final Calendar _siegeDate;

	public SiegeInfo(Castle castle, ClanHall clanHall, Calendar siegeDate)
	{
		_castle = castle;
		_clanHall = clanHall;
		_siegeDate = siegeDate;
	}

	@Override
	protected final void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (_castle == null)
		{
			writeC(0xc9);
			writeD(_clanHall.getId());
			writeD(_clanHall.getOwnerId() == activeChar.getClanId() && activeChar.isClanLeader() ? 0x01 : 0x00);
			writeD(_clanHall.getOwnerId());
			if (_clanHall.getOwnerId() > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(_clanHall.getOwnerId());
				if (owner != null)
				{
					writeS(owner.getName());
					writeS(owner.getLeaderName());
					writeD(owner.getAllyId());
					writeS(owner.getAllyName());
				}
				else
				{
					_log.warn("Null owner for castle: " + _castle.getName());
				}
			}
			else
			{
				writeS("NPC");
				writeS("");
				writeD(0);
				writeS("");
			}

			writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
			writeD((int) (_siegeDate.getTimeInMillis() / 1000));
			writeD(0x00);
		}
		else
		{
			writeC(0xc9);
			writeD(_castle.getCastleId());
			writeD(_castle.getOwnerId() == activeChar.getClanId() && activeChar.isClanLeader() ? 0x01 : 0x00);
			writeD(_castle.getOwnerId());
			if (_castle.getOwnerId() > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(_castle.getOwnerId());
				if (owner != null)
				{
					writeS(owner.getName());
					writeS(owner.getLeaderName());
					writeD(owner.getAllyId());
					writeS(owner.getAllyName());
				}
				else
				{
					_log.warn("Null owner for castle: " + _castle.getName());
				}
			}
			else
			{
				writeS("NPC");
				writeS("");
				writeD(0);
				writeS("");
			}

			writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
			writeD((int) (_castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
			writeD(0x00);
		}
	}

}
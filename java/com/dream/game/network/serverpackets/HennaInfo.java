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

import com.dream.game.model.actor.instance.L2HennaInstance;
import com.dream.game.model.actor.instance.L2PcInstance;

public final class HennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2HennaInstance[] _hennas = new L2HennaInstance[3];
	private int _count = 0;

	public HennaInfo(L2PcInstance player)
	{
		_activeChar = player;
		_count = 0;

		for (int i = 0; i < 3; i++)
		{
			L2HennaInstance henna = _activeChar.getHenna(i + 1);
			if (henna != null)
			{
				_hennas[_count++] = henna;
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe4);

		writeC(_activeChar.getHennaStatINT());
		writeC(_activeChar.getHennaStatSTR());
		writeC(_activeChar.getHennaStatCON());
		writeC(_activeChar.getHennaStatMEN());
		writeC(_activeChar.getHennaStatDEX());
		writeC(_activeChar.getHennaStatWIT());

		writeD(3);

		writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
	}

}
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

public final class GMHennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _cha;
	private final L2HennaInstance[] _hennas = new L2HennaInstance[3];
	private final int _count;

	public GMHennaInfo(L2PcInstance cha)
	{
		_cha = cha;

		int j = 0;
		for (int i = 0; i < 3; i++)
		{
			L2HennaInstance h = _cha.getHenna(i + 1);
			if (h != null)
			{
				_hennas[j++] = h;
			}
		}
		_count = j;
	}

	@Override
	protected void writeImpl()
	{
		writeC(234);

		writeC(_cha.getHennaStatINT());
		writeC(_cha.getHennaStatSTR());
		writeC(_cha.getHennaStatCON());
		writeC(_cha.getHennaStatMEN());
		writeC(_cha.getHennaStatDEX());
		writeC(_cha.getHennaStatWIT());

		writeD(3);

		writeD(_count);
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(1);
		}
	}

}
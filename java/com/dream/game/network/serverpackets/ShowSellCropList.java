/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.instance.L2PcInstance;

public class ShowSellCropList extends L2GameServerPacket
{
	private byte _manorId = 1;

	public ShowSellCropList(L2PcInstance player, byte manorId)
	{
		_manorId = manorId;
	}

	@Override
	public void runImpl()
	{

	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x21);

		writeD(_manorId);
		writeD(1);

		writeD(0);
		writeD(5078);
		writeD(31);
		writeC(1);
		writeD(1871);
		writeC(1);
		writeD(4042);

		writeD(_manorId);
		writeD(3);
		writeD(10);
		writeC(1);
		writeD(20);
	}

}
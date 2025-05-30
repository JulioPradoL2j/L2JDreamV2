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

public class ShowSeedSetting extends L2GameServerPacket
{
	@Override
	public void runImpl()
	{

	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1F);

		writeD(0);
		writeD(1);

		writeD(5033);
		writeD(31);
		writeC(1);
		writeD(1871);
		writeC(1);
		writeD(4042);
		writeD(2250);
		writeD(20);
		writeD(12);
		writeD(200);
		writeD(4);
		writeD(5);
		writeD(6);
		writeD(7);
	}

}
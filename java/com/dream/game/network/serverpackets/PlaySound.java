/*
 * This program is free software; you can redistribute it and/or modify
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

public class PlaySound extends L2GameServerPacket
{
	private final int _unknown1;
	private final String _soundFile;
	private final int _unknown3;
	private final int _unknown4;
	private final int _unknown5;
	private final int _unknown6;
	private final int _unknown7;

	public PlaySound(int unknown1, String soundFile, int unknown3, int unknown4, int unknown5, int unknown6, int unknown7)
	{
		_unknown1 = unknown1;
		_soundFile = soundFile;
		_unknown3 = unknown3;
		_unknown4 = unknown4;
		_unknown5 = unknown5;
		_unknown6 = unknown6;
		_unknown7 = unknown7;
	}

	public PlaySound(String soundFile)
	{
		_unknown1 = 0;
		_soundFile = soundFile;
		_unknown3 = 0;
		_unknown4 = 0;
		_unknown5 = 0;
		_unknown6 = 0;
		_unknown7 = 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x98);
		writeD(_unknown1);
		writeS(_soundFile);
		writeD(_unknown3);
		writeD(_unknown4);
		writeD(_unknown5);
		writeD(_unknown6);
		writeD(_unknown7);
	}

}
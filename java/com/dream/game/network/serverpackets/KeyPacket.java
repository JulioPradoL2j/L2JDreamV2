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

public final class KeyPacket extends L2GameServerPacket
{
	private byte[] _key;
	private byte[] _data;
	private boolean _isLAMEPacket = false;

	public KeyPacket(byte data[])
	{
		_data = data;
		_isLAMEPacket = true;
	}

	public KeyPacket(byte[] key, int id)
	{
		_key = key;
	}

	@Override
	public void writeImpl()
	{
		if (_isLAMEPacket)
		{
			writeC(0x00);
			writeC(_data == null ? 0x00 : 0x01);
			if (_data != null)
			{
				writeB(_data);
				writeD(0x01);
				writeD(0x01);
			}
			return;
		}
		writeC(0x00);
		writeC(0x01);
		writeB(_key);
		writeD(0x01);
		writeC(0x01);

	}

}
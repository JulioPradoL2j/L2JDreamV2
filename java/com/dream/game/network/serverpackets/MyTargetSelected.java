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

public class MyTargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _color;

	public MyTargetSelected(int objectId, int color)
	{
		_objectId = objectId;
		_color = color;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xa6);
		writeD(_objectId);
		writeH(_color);
	}

}
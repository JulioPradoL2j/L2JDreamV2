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

public class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;

	private final int _color;
	private final int _time1;
	private final int _time2;

	public SetupGauge(int color, int time)
	{
		_color = color;
		_time1 = time;
		_time2 = time;
	}

	public SetupGauge(int color, int currentTime, int maxTime)
	{
		_color = color;
		_time1 = currentTime;
		_time2 = maxTime;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x6d);

		writeD(_color);
		writeD(_time1);
		writeD(_time2);
	}

}
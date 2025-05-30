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

public class EnchantResult extends L2GameServerPacket
{
	public static final EnchantResult SUCCESS = new EnchantResult(0);
	public static final EnchantResult UNK_RESULT_1 = new EnchantResult(1);
	public static final EnchantResult CANCELLED = new EnchantResult(2);
	public static final EnchantResult UNSUCCESS = new EnchantResult(3);
	public static final EnchantResult UNK_RESULT_4 = new EnchantResult(4);

	private final int _result;

	public EnchantResult(int result)
	{
		_result = result;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x81);
		writeD(_result);
	}

}
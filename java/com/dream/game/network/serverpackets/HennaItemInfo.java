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

public class HennaItemInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2HennaInstance _henna;

	public HennaItemInfo(L2HennaInstance henna, L2PcInstance player)
	{
		_henna = henna;
		_activeChar = player;
	}

	@Override
	protected final void writeImpl()
	{

		writeC(0xe3);
		writeD(_henna.getSymbolId());
		writeD(_henna.getItemIdDye());
		writeD(_henna.getAmountDyeRequire());
		writeD(_henna.getPrice());
		writeD(1);
		writeD(_activeChar.getAdena());

		writeD(_activeChar.getINT());
		writeC(_activeChar.getINT() + _henna.getStatINT());
		writeD(_activeChar.getSTR());
		writeC(_activeChar.getSTR() + _henna.getStatSTR());
		writeD(_activeChar.getCON());
		writeC(_activeChar.getCON() + _henna.getStatCON());
		writeD(_activeChar.getMEN());
		writeC(_activeChar.getMEN() + _henna.getStatMEM());
		writeD(_activeChar.getDEX());
		writeC(_activeChar.getDEX() + _henna.getStatDEX());
		writeD(_activeChar.getWIT());
		writeC(_activeChar.getWIT() + _henna.getStatWIT());
	}

}
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

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.zone.L2Zone;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;

	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}

	@Override
	protected void writeImpl()
	{
		boolean showCharmOfCourageEffect = false;
		if (_activeChar.getCharmOfCourage() && _activeChar.getCanUseCharmOfCourageRes())
		{
			showCharmOfCourageEffect = true;
		}

		writeC(0xF3);
		writeD(_activeChar.getCharges());
		writeD(_activeChar.getWeightPenalty());
		writeD(_activeChar.getMessageRefusal() ? 1 : 0);
		writeD(_activeChar.isInsideZone(L2Zone.FLAG_DANGER) ? 1 : 0);
		writeD(_activeChar.getExpertisePenalty());
		writeD(showCharmOfCourageEffect ? 1 : 0);
		writeD(_activeChar.getDeathPenaltyBuffLevel());
	}

}
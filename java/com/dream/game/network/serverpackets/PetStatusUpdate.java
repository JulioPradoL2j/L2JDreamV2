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

import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PetInstance;

public class PetStatusUpdate extends L2GameServerPacket
{
	private final L2Summon _summon;
	private int _maxFed, _curFed;

	public PetStatusUpdate(L2Summon summon)
	{
		_summon = summon;
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed();
			_maxFed = pet.getMaxFed();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb5);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeS(_summon.getTitle());
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getStatus().getCurrentHp());
		writeD(_summon.getMaxHp());
		writeD((int) _summon.getStatus().getCurrentMp());
		writeD(_summon.getMaxMp());
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel());
		writeQ(_summon.getExpForNextLevel());
	}

}
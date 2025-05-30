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

import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;

public final class PartySpelled extends EffectInfoPacket
{
	public PartySpelled(EffectInfoPacketList list)
	{
		super(list);
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xee);
		writeD(getPlayable() instanceof L2SummonInstance ? 2 : getPlayable() instanceof L2PetInstance ? 1 : 0);
		writeD(getPlayable().getObjectId());
		writeD(size());
		writeEffectInfos();
	}

}
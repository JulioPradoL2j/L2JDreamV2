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

import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private final String _name;
	private final int _lvl;
	private final int _classId;
	private final int _isOnline;
	private final int _pledgeType;
	private final int _sex;
	private final int _race;

	public PledgeShowMemberListAdd(L2ClanMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassId();
		_isOnline = cm.isOnline() ? cm.getObjectId() : 0;
		_pledgeType = cm.getPledgeType();
		_sex = cm.getSex();
		_race = cm.getRaceOrdinal();
	}

	public PledgeShowMemberListAdd(L2PcInstance player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getClassId().getId();
		_isOnline = player.isOnline() == 1 ? player.getObjectId() : 0;
		_pledgeType = player.getPledgeType();
		_sex = player.getAppearance().getSex() ? 1 : 0;
		_race = player.getRace().ordinal();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x55);
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline);
		writeD(_pledgeType);
	}

}
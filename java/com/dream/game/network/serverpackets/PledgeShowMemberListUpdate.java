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

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _pledgeType;
	private int _hasSponsor;
	private final String _name;
	private final int _level;
	private final int _classId;
	private final int _objectId;
	private final boolean _isOnline;
	private final int _race;
	private final int _sex;

	public PledgeShowMemberListUpdate(L2ClanMember player)
	{
		_activeChar = player.getPlayerInstance();
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId();
		_objectId = player.getObjectId();
		_isOnline = player.isOnline();
		_pledgeType = player.getPledgeType();
		_race = player.getRaceOrdinal();
		_sex = player.getSex();
		if (_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = player.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
	}

	public PledgeShowMemberListUpdate(L2PcInstance player)
	{
		_activeChar = player;
		_pledgeType = _activeChar.getPledgeType();
		if (_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _activeChar.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
		_name = _activeChar.getName();
		_level = _activeChar.getLevel();
		_classId = _activeChar.getClassId().getId();
		_race = _activeChar.getRace().ordinal();
		_sex = _activeChar.getAppearance().getSex() ? 1 : 0;
		_objectId = _activeChar.getObjectId();
		_isOnline = _activeChar.isOnline() == 1;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline ? _objectId : 0);
		writeD(_pledgeType);
		writeD(_hasSponsor);
	}

}
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

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;

public final class ServerObjectInfo extends L2GameServerPacket
{
	private final L2Npc _activeChar;
	private final int _x, _y, _z, _heading;
	private final int _idTemplate;
	private final int _collisionHeight, _collisionRadius;

	public ServerObjectInfo(L2Npc activeChar)
	{
		_activeChar = activeChar;
		_idTemplate = _activeChar.getTemplate().getIdTemplate();
		_collisionHeight = _activeChar.getCollisionHeight();
		_collisionRadius = _activeChar.getCollisionRadius();
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		boolean isAttackable = _activeChar.isAutoAttackable(activeChar);

		writeC(0x8C);
		writeD(_activeChar.getObjectId());
		writeD(_idTemplate + 1000000);
		writeS("");
		writeD(isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeF(1.0);
		writeF(1.0);
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD((int) (isAttackable ? _activeChar.getCurrentHp() : 0));
		writeD(isAttackable ? _activeChar.getMaxHp() : 0);
		writeD(0x01);
		writeD(0x00);
	}

}
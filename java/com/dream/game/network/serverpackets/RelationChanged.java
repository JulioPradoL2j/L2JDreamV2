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

import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2PcInstance;

public final class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PVP_FLAG = 0x00002; // pvp ???
	public static final int RELATION_HAS_KARMA = 0x00004; // karma ???
	public static final int RELATION_LEADER = 0x00080; // leader
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x08000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x10000; // single fist

	public static void sendRelationChanged(L2PcInstance target, L2PcInstance attacker)
	{
		if (target == null || attacker == null || attacker.isOfflineTrade())
			return;

		int currentRelation = target.getRelation(attacker);
		Integer oldRelation = attacker.getKnownList().getKnownRelations().put(target.getObjectId(), currentRelation);
		if (oldRelation != null && oldRelation == currentRelation)
			return;

		attacker.sendPacket(new RelationChanged(target, currentRelation, attacker));
		if (target.getPet() != null)
		{
			attacker.sendPacket(new RelationChanged(target.getPet(), currentRelation, attacker));
		}
	}

	private final int _objId;
	private final int _relation;
	private final int _autoAttackable;
	private final int _karma;
	private final int _pvpFlag;

	public RelationChanged(L2Playable cha, int relation, boolean autoattackable)
	{
		_objId = cha.getObjectId();
		_relation = relation;
		_autoAttackable = autoattackable ? 1 : 0;
		_karma = cha.getActingPlayer().getKarma();
		_pvpFlag = cha.getActingPlayer().getPvpFlag();
	}

	public RelationChanged(L2Playable target, int relation, L2PcInstance attacker)
	{
		_objId = target.getObjectId();
		_relation = relation;
		_autoAttackable = target.isAutoAttackable(attacker) ? 1 : 0;
		_karma = target.getActingPlayer().getKarma();
		_pvpFlag = target.getActingPlayer().getPvpFlag();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xce);
		writeD(_objId);
		writeD(_relation);
		writeD(_autoAttackable);
		writeD(_karma);
		writeD(_pvpFlag);
	}

}
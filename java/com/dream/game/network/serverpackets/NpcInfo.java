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

import com.dream.Config;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.model.zone.L2Zone;

public class NpcInfo extends L2GameServerPacket
{
	private final L2Character _activeChar;
	private final int _x, _y, _z, _heading;
	private final int _idTemplate;
	private boolean _isSummoned;
	private final int _mAtkSpd, _pAtkSpd;
	private final int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private final int _rhand, _lhand;
	private int _chest;
	private final int _collisionHeight, _collisionRadius;
	private String _name = "";
	private String _title = "";
	protected int _clanCrest, _allyCrest, _allyId, _clanId;

	public NpcInfo(L2Npc cha)
	{
		_activeChar = cha;
		_idTemplate = cha.getTemplate().getIdTemplate();
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_isSummoned = cha.isShowSummonAnimation();
		_collisionHeight = cha.getCollisionHeight();
		_collisionRadius = cha.getCollisionRadius();
		if (cha.getTemplate()._serverSideName)
		{
			_name = cha.getTemplate()._name;
		}

		if (Config.CHAMPION_ENABLE && cha.isChampion())
		{
			_title = Config.CHAMPION_TITLE;
		}
		else if (cha.getTemplate()._serverSideTitle)
		{
			_title = cha.getTemplate()._title;
		}
		else
		{
			_title = cha.getTitle();
		}

		if (Config.SHOW_NPC_CREST && cha instanceof L2NpcInstance && cha.isInsideZone(L2Zone.FLAG_PEACE) && cha.getCastle().getOwnerId() != 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
			_clanCrest = clan.getCrestId();
			_clanId = clan.getClanId();
			_allyCrest = clan.getAllyCrestId();
			_allyId = clan.getAllyId();
		}

		if (Config.SHOW_NPC_LVL && _activeChar instanceof L2MonsterInstance)
		{
			String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
			if (_title != null && !_title.isEmpty())
			{
				t += " " + _title;
			}

			_title = t;
		}

		if (_activeChar instanceof L2SiegeFlagInstance)
		{
			_title = cha.getTitle();
		}

		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = _activeChar.getTemplate().getBaseRunSpd();
		_walkSpd = _activeChar.getTemplate().getBaseWalkSpd();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}

	public NpcInfo(L2Summon cha, int val)
	{
		_activeChar = cha;
		_idTemplate = cha.getTemplate().getIdTemplate();
		_rhand = cha.getWeapon();
		_lhand = 0;
		_chest = cha.getArmor();
		_collisionHeight = _activeChar.getTemplate().getCollisionHeight();
		_collisionRadius = _activeChar.getTemplate().getCollisionRadius();

		if (cha.getTemplate()._serverSideName || cha instanceof L2PetInstance)
		{
			_name = _activeChar.getName();
			_title = cha.getTitle();
		}
		_title = cha.getOwner() != null ? cha.getOwner().isOnline() == 0 ? "" : cha.getOwner().getName() : "";

		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = cha.getPetSpeed();
		_walkSpd = cha.isMountable() ? 45 : 30;
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;

		L2PcInstance owner = cha.getOwner();

		if (Config.SHOW_NPC_CREST && owner != null && owner.getClan() != null)
		{
			L2Clan clan = ClanTable.getInstance().getClan(owner.getClanId());
			_clanCrest = clan.getCrestId();
			_clanId = clan.getClanId();
			_allyCrest = clan.getAllyCrestId();
			_allyId = clan.getAllyId();
		}
	}

	@Override
	protected void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (_activeChar instanceof L2Summon)
			if (((L2Summon) _activeChar).getOwner() != null && ((L2Summon) _activeChar).getOwner().getAppearance().isInvisible())
				return;
		writeC(0x16);
		writeD(_activeChar.getObjectId());
		writeD(_idTemplate + 1000000);
		writeD(_activeChar.isAutoAttackable(activeChar) ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1.1);

		writeF(_pAtkSpd / 277.478340719);
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(_rhand);
		writeD(_chest);
		writeD(_lhand);
		writeC(1);
		writeC(_activeChar.isRunning() ? 1 : 0);
		writeC(_activeChar.isInCombat() ? 1 : 0);
		writeC(_activeChar.isAlikeDead() ? 1 : 0);
		writeC(_isSummoned ? 2 : 0);
		writeS(_name);
		writeS(_title);

		if (_activeChar instanceof L2Summon)
		{
			writeD(0x01);
			writeD(((L2Summon) _activeChar).getPvpFlag());
			writeD(((L2Summon) _activeChar).getKarma());

		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}

		writeD(_activeChar.getAbnormalEffect());

		if (Config.SHOW_NPC_CREST)
		{
			writeD(_clanId);
			writeD(_clanCrest);
			writeD(_allyId);
			writeD(_allyCrest);
		}
		else
		{
			writeD(0000);
			writeD(0000);
			writeD(0000);
			writeD(0000);
		}

		writeC(_activeChar.isFlying() ? 0x02 : _activeChar.isInWater() ? 0x01 : 0x00);
		writeC(_activeChar.getTeam());
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(0x00);
		writeD(_activeChar.isFlying() ? 0x01 : 0x00);
	}

}
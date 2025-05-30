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

public class PetInfo extends L2GameServerPacket
{
	private final L2Summon _summon;
	private final int _x, _y, _z, _heading;
	private final boolean _isSummoned;
	private final int _val;
	private final int _mAtkSpd, _pAtkSpd;
	private final int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private final int _maxHp, _maxMp;
	private int _maxFed, _curFed;
	private final float _multiplier;

	public PetInfo(L2Summon summon, int val)
	{
		summon.updateEffectIcons();

		_summon = summon;
		_isSummoned = _summon.isShowSummonAnimation();
		_x = _summon.getX();
		_y = _summon.getY();
		_z = _summon.getZ();
		_heading = _summon.getHeading();
		_mAtkSpd = _summon.getMAtkSpd();
		_pAtkSpd = _summon.getPAtkSpd();
		_multiplier = _summon.getStat().getMovementSpeedMultiplier();
		_runSpd = _summon.getPetSpeed();
		_walkSpd = _summon.isMountable() ? 45 : 30;
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_maxHp = _summon.getMaxHp();
		_maxMp = _summon.getMaxMp();
		_val = val;
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
		writeC(0xb1);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getIdTemplate() + 1000000);
		writeD(0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0);
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

		writeF(_multiplier);
		writeF(1);
		writeF(_summon.getTemplate().getCollisionRadius());
		writeF(_summon.getTemplate().getCollisionHeight());
		writeD(_summon.getWeapon());
		writeD(_summon.getArmor());
		writeD(0x00);
		writeC(_summon.getOwner() != null ? 1 : 0);
		writeC(1);
		writeC(_summon.isInCombat() ? 1 : 0);
		writeC(_summon.isAlikeDead() ? 1 : 0);
		writeC(_isSummoned ? 2 : _val);
		writeS(_summon.getName());
		writeS(_summon.getTitle());
		writeD(0x01);
		writeD(_summon.getOwner() != null ? _summon.getOwner().getPvpFlag() : 0);
		writeD(_summon.getOwner() != null ? _summon.getOwner().getKarma() : 0);
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getStatus().getCurrentHp());
		writeD(_maxHp);
		writeD((int) _summon.getStatus().getCurrentMp());
		writeD(_maxMp);
		writeD(_summon.getStat().getSp());
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		if (_summon.getExpForThisLevel() > _summon.getStat().getExp())
		{
			writeQ(_summon.getStat().getExp());
		}
		else
		{
			writeQ(_summon.getExpForThisLevel());
		}
		writeQ(_summon.getExpForNextLevel());
		writeD(_summon instanceof L2PetInstance ? _summon.getInventory().getTotalWeight() : 0);
		writeD(_summon.getMaxLoad());
		writeD(_summon.getPAtk(null));
		writeD(_summon.getPDef(null));
		writeD(_summon.getMAtk(null, null));
		writeD(_summon.getMDef(null, null));
		writeD(_summon.getAccuracy());
		writeD(_summon.getEvasionRate(null));
		writeD(_summon.getCriticalHit(null, null));
		writeD((int) _summon.getStat().getMoveSpeed());
		writeD(_summon.getPAtkSpd());
		writeD(_summon.getMAtkSpd());

		writeD(_summon.getAbnormalEffect());

		writeH(_summon.isMountable() ? 1 : 0);

		writeC(0);

		writeH(0);
		writeC(_summon.getOwner() != null ? _summon.getOwner().getTeam() : 0);
		writeD(_summon.getSoulShotsPerHit());
		writeD(_summon.getSpiritShotsPerHit());
	}

}
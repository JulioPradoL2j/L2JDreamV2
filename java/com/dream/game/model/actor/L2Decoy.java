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

package com.dream.game.model.actor;

import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.knownlist.DecoyKnownList;
import com.dream.game.network.serverpackets.CharInfo;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.templates.item.L2Weapon;

public class L2Decoy extends L2Character
{
	private final L2PcInstance _owner;

	private boolean _isSit;

	public L2Decoy(L2PcInstance owner)
	{
		super(IdFactory.getInstance().getNextId(), null);
		getKnownList();
		_owner = owner;
		getPosition().setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
		setHeading(owner.getHeading());
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastPacket(new CharInfo(this));
	}

	public void deleteMe(L2PcInstance owner)
	{
		decayMe();
		getKnownList().removeAllKnownObjects();

	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public DecoyKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new DecoyKnownList(this);
		}

		return (DecoyKnownList) _knownList;
	}

	@Override
	public int getLevel()
	{
		return _owner.getLevel();
	}

	public final L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	public boolean isSitting()
	{
		return _isSit;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setTarget(this);
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
	}

	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		broadcastFullInfo();
	}

	public void sitDown()
	{
		_isSit = true;
	}

	public void standUp()
	{
		_isSit = false;
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	@Override
	public void updateAbnormalEffect()
	{
		broadcastPacket(new CharInfo(this));
	}
}
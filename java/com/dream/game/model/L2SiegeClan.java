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
package com.dream.game.model;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.util.Util;

import javolution.util.FastSet;

public class L2SiegeClan
{
	public enum SiegeClanType
	{
		OWNER,
		DEFENDER,
		ATTACKER,
		DEFENDER_PENDING
	}

	private int _clanId = 0;
	private FastSet<L2Npc> _flags;
	private int _numFlagsAdded = 0;

	private SiegeClanType _type;

	public L2SiegeClan(int clanId, SiegeClanType type)
	{
		_clanId = clanId;
		_type = type;
	}

	public void addFlag(L2Npc flag)
	{
		_numFlagsAdded++;
		getFlag().add(flag);
	}

	public final int getClanId()
	{
		return _clanId;
	}

	public final L2Npc getClosestFlag(L2Object obj)
	{
		double closestDistance = Double.MAX_VALUE;
		double distance;
		L2Npc _flag = null;

		for (L2Npc flag : getFlag())
		{
			if (flag == null)
			{
				continue;
			}
			distance = Util.calculateDistance(obj.getX(), obj.getY(), obj.getZ(), flag.getX(), flag.getX(), flag.getZ(), true);
			if (closestDistance > distance)
			{
				closestDistance = distance;
				_flag = flag;
			}
		}
		return _flag;
	}

	public final FastSet<L2Npc> getFlag()
	{
		if (_flags == null)
		{
			_flags = new FastSet<>();
		}
		return _flags;
	}

	public int getNumFlags()
	{
		return _numFlagsAdded;
	}

	public SiegeClanType getType()
	{
		return _type;
	}

	public boolean removeFlag(L2Npc flag)
	{
		if (flag == null)
			return false;

		boolean ret = getFlag().remove(flag);
		if (ret)
		{
			getFlag().remove(flag);
		}

		flag.deleteMe();
		_numFlagsAdded--;
		return ret;
	}

	public void removeFlags()
	{
		for (L2Npc flag : getFlag())
		{
			removeFlag(flag);
		}
	}

	public void setType(SiegeClanType setType)
	{
		_type = setType;
	}
}

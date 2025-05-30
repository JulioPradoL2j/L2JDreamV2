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
package com.dream.game.model.actor.stat;

import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.base.Experience;

public class PlayableStat extends CharStat
{
	public PlayableStat(L2Playable activeChar)
	{
		super(activeChar);
	}

	public boolean addExp(long value)
	{
		byte minimumLevel = 1;
		if (getActiveChar() instanceof L2PetInstance)
		{
			minimumLevel = ((L2PetInstance) getActiveChar()).getTemplate().getLevel();
		}

		if (value < 0 && getLevel() <= minimumLevel && getExp() + value <= getExpForLevel(minimumLevel))
			return true;

		if (getExp() + value < 0 || getExp() == getExpForLevel(Experience.MAX_LEVEL) - 1)
			return true;

		if (getExp() + value >= getExpForLevel(Experience.MAX_LEVEL))
		{
			value = getExpForLevel(Experience.MAX_LEVEL) - 1 - getExp();
		}

		setExp(getExp() + value);

		byte level = minimumLevel;
		byte tmp;
		for (tmp = level; tmp <= Experience.MAX_LEVEL; tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			tmp--;
			level = tmp;
			break;
		}
		if (level != getLevel() && level >= minimumLevel)
		{
			addLevel((byte) (level - getLevel()));
		}

		return true;
	}

	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		boolean expAdded = false;
		boolean spAdded = false;
		if (addToExp >= 0)
		{
			expAdded = addExp(addToExp);
		}
		if (addToSp >= 0)
		{
			spAdded = addSp(addToSp);
		}

		return expAdded || spAdded;
	}

	public boolean addLevel(byte value)
	{
		if (getLevel() + value > Experience.MAX_LEVEL - 1)
			if (getLevel() < Experience.MAX_LEVEL - 1)
			{
				value = (byte) (Experience.MAX_LEVEL - 1 - getLevel());
			}
			else
				return false;

		boolean levelIncreased = getLevel() + value > getLevel();
		value += getLevel();
		setLevel(value);

		if (getExp() >= getExpForLevel(getLevel() + 1) || getExpForLevel(getLevel()) > getExp())
		{
			setExp(getExpForLevel(getLevel()));
		}

		if (!levelIncreased)
			return false;

		getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
		getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());

		return true;
	}

	public boolean addSp(int value)
	{
		if (value < 0)
		{
			_log.warn("PlayableStat.addSp(" + value + ") : Wrong usage!");
			return false;
		}
		int currentSp = getSp();
		if (currentSp == Integer.MAX_VALUE)
			return false;

		if (currentSp > Integer.MAX_VALUE - value)
		{
			value = Integer.MAX_VALUE - currentSp;
		}

		setSp(currentSp + value);
		return true;
	}

	@Override
	public L2Playable getActiveChar()
	{
		return (L2Playable) _activeChar;
	}

	public long getExpForLevel(int level)
	{
		return level;
	}

	public boolean removeExp(long value)
	{
		if (getExp() - value < 0)
		{
			value = getExp() - 1;
		}

		setExp(getExp() - value);

		byte minimumLevel = 1;
		if (getActiveChar() instanceof L2PetInstance)
		{
			minimumLevel = ((L2PetInstance) getActiveChar()).getTemplate().getLevel();
		}

		byte level = minimumLevel;
		byte tmp;
		for (tmp = level; tmp <= Experience.MAX_LEVEL; tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			tmp--;
			level = tmp;
			break;
		}
		if (level != getLevel() && level >= minimumLevel)
		{
			addLevel((byte) (level - getLevel()));
		}
		return true;
	}

	public boolean removeExpAndSp(long removeExp, int removeSp)
	{
		boolean expRemoved = false;
		boolean spRemoved = false;
		if (removeExp > 0)
		{
			expRemoved = removeExp(removeExp);
		}
		if (removeSp > 0)
		{
			spRemoved = removeSp(removeSp);
		}

		return expRemoved || spRemoved;
	}

	public boolean removeSp(int value)
	{
		int currentSp = getSp();
		if (currentSp < value)
		{
			value = currentSp;
		}
		setSp(getSp() - value);
		return true;
	}
}
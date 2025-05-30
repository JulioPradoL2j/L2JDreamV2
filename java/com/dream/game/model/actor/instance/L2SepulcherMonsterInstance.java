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
package com.dream.game.model.actor.instance;

import com.dream.game.manager.FourSepulchersManager.FourSepulchersMausoleum;
import com.dream.game.model.actor.L2Character;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SepulcherMonsterInstance extends L2MonsterInstance
{
	protected FourSepulchersMausoleum _mausoleum;

	private int _stage;

	public L2SepulcherMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setShowSummonAnimation(true);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		if (_mausoleum != null)
		{
			_mausoleum.onKill(this, killer);
		}

		return true;
	}

	public int getStage()
	{
		return _stage;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return true;
	}

	@Override
	public void onSpawn()
	{
		setShowSummonAnimation(false);
		super.onSpawn();
	}

	public void setMausoleum(FourSepulchersMausoleum mausoleum)
	{
		_mausoleum = mausoleum;
	}

	public void setStage(int stage)
	{
		_stage = stage;
	}
}
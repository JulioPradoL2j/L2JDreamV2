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
package com.dream.game.model.zone;

import com.dream.game.manager.CastleManager;
import com.dream.game.model.actor.L2Character;

public class L2TrapZone extends EntityZone
{
	private boolean _enabled;

	public boolean isEnabled()
	{
		return _enabled;
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (isEnabled())
		{
			character.setInsideZone(this, FLAG_CASTLETRAP, true);
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (isEnabled())
		{
			character.setInsideZone(this, FLAG_CASTLETRAP, false);
		}

		super.onExit(character);
	}

	@Override
	protected void register()
	{
		_entity = CastleManager.getInstance().getCastleById(_castleId);
		if (_entity != null)
		{
			_entity.registerZone(this);
		}
		else
		{
			_log.warn("Invalid castleId: " + _castleId);
		}
	}

	@Override
	public void setEnabled(boolean val)
	{
		_enabled = val;
	}
}
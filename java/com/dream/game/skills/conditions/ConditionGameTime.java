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
package com.dream.game.skills.conditions;

import com.dream.game.GameTimeController;
import com.dream.game.skills.Env;

public final class ConditionGameTime extends Condition
{
	public enum CheckGameTime
	{
		NIGHT
	}

	private final CheckGameTime _check;
	private final boolean _required;

	public ConditionGameTime(CheckGameTime check, boolean required)
	{
		_check = check;
		_required = required;
	}

	@Override
	boolean testImpl(Env env)
	{
		switch (_check)
		{
			case NIGHT:
				return GameTimeController.isNowNight() == _required;
		}
		return !_required;
	}
}
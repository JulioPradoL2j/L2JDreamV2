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
package com.dream.game.skills.effects;

import java.util.HashMap;
import java.util.Map;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.geodata.GeoData;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.instance.L2FortCommanderInstance;
import com.dream.game.model.actor.instance.L2FortSiegeGuardInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.model.actor.instance.L2SiegeGuardInstance;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.world.Location;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectFear extends L2Effect
{
	public static final int FEAR_RANGE = 500;
	private static Map<Integer, float[]> _deltas = new HashMap<>();

	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}

	@Override
	public boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();
		float[] delta = _deltas.get(getEffected().getObjectId());
		float _dX = -1;
		float _dY = -1;
		if (delta != null)
		{
			_dX = delta[0];
			_dY = delta[1];
		}

		if (_dX != 0)
		{
			posX += _dX * FEAR_RANGE;
		}
		if (_dY != 0)
		{
			posY += _dY * FEAR_RANGE;
		}

		Location destiny = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ);
		if (!(getEffected() instanceof L2PetInstance))
		{
			getEffected().setRunning();
		}
		getEffected().getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(destiny.getX(), destiny.getY(), destiny.getZ(), 0));

		double damage = calc();
		if (damage != 0)
		{
			getEffected().reduceCurrentHp(damage, getEffector(), true, true, getSkill());
		}
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopFear(this);
		_deltas.remove(getEffected().getObjectId());
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance && getEffector() instanceof L2PcInstance)
		{
			switch (getSkill().getId())
			{
				case 65:
				case 98:
				case 1092:
				case 1169:
				case 1272:
				case 1376:
				case 1381:
				case 4108:
				case 4689:
				case 5092:
				case 5173:
				case 5203:
				case 5220:
					break;
				default:
					return false;
			}
		}

		if (getEffected() instanceof L2NpcInstance || getEffected() instanceof L2SiegeGuardInstance || getEffected() instanceof L2SiegeFlagInstance || getEffected() instanceof L2SiegeSummonInstance || getEffected() instanceof L2FortSiegeGuardInstance || getEffected() instanceof L2FortCommanderInstance)
			return false;

		if (!getEffected().isAfraid())
		{
			float _dX = getEffector().getX() - getEffected().getX();
			float _dY = getEffector().getY() - getEffected().getY();

			if (_dX == 0)
			{
				_dX = 0;
				if (_dY > 0)
				{
					_dY = -1;
				}
				else
				{
					_dY = 1;
				}
			}
			else if (_dY == 0)
			{
				_dY = 0;
				if (_dX > 0)
				{
					_dX = -1;
				}
				else
				{
					_dX = 1;
				}
			}
			else if (_dX > 0 && _dY > 0)
			{
				if (_dX > _dY)
				{
					_dY = -1 * _dY / _dX;
					_dX = -1;
				}
				else
				{
					_dX = -1 * _dX / _dY;
					_dY = -1;
				}
			}
			else if (_dX > 0 && _dY < 0)
			{
				if (_dX > -1 * _dY)
				{
					_dY = -1 * (_dY / _dX);
					_dX = -1;
				}
				else
				{
					_dX = _dX / _dY;
					_dY = 1;
				}
			}
			else if (_dX < 0 && _dY > 0)
			{
				if (-1 * _dX > _dY)
				{
					_dY = _dY / _dX;
					_dX = 1;
				}
				else
				{
					_dX = -1 * (_dX / _dY);
					_dY = -1;
				}
			}
			else if (_dX < 0 && _dY < 0)
			{
				if (_dX > _dY)
				{
					_dY = _dY / _dX;
					_dX = 1;
				}
				else
				{
					_dX = _dX / _dY;
					_dY = 1;
				}
			}
			else if (_dX == 0 && _dY == 0)
			{
				_dX = -1;
				_dY = -1;
			}
			_deltas.put(getEffected().getObjectId(), new float[]
			{
				_dX,
				_dY
			});
			getEffected().startFear();
			onActionTime();
			return true;
		}
		return false;
	}
}
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

import com.dream.Config;
import com.dream.game.geodata.GeoData;
import com.dream.game.model.L2Effect;
import com.dream.game.model.world.Location;
import com.dream.game.network.serverpackets.FlyToLocation;
import com.dream.game.network.serverpackets.FlyToLocation.FlyType;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectEnemyCharge extends L2Effect
{

	private int _x, _y, _z;

	public EffectEnemyCharge(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		getEffector().getPosition().setXYZ(_x, _y, _z);
		getEffector().broadcastPacket(new ValidateLocation(getEffector()));
	}

	@Override
	public boolean onStart()
	{
		final int curX = getEffector().getX();
		final int curY = getEffector().getY();
		final int curZ = getEffector().getZ();

		double dx = getEffected().getX() - curX;
		double dy = getEffected().getY() - curY;
		double dz = getEffected().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);

		int offset = Math.max((int) distance - getSkill().getFlyRadius(), 30);

		double cos;
		double sin;

		offset -= Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}

		if (distance < 1 || distance - offset <= 0)
			return false;

		sin = dy / distance;
		cos = dx / distance;

		_x = curX + (int) ((distance - offset) * cos);
		_y = curY + (int) ((distance - offset) * sin);
		_z = getEffected().getZ();

		if (Config.GEODATA)
		{
			Location destiny = GeoData.getInstance().moveCheck(getEffector().getX(), getEffector().getY(), getEffector().getZ(), _x, _y, _z);
			_x = destiny.getX();
			_y = destiny.getY();
		}
		getEffector().broadcastPacket(new FlyToLocation(getEffector(), _x, _y, _z, FlyType.CHARGE));
		return true;
	}
}

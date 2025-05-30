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

import java.lang.reflect.Constructor;

import com.dream.Config;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class L2GroupSpawn extends L2Spawn
{
	private final Constructor<?> _constructor;
	private final L2NpcTemplate _template;

	public L2GroupSpawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException
	{
		super(mobTemplate);
		_constructor = Class.forName("com.dream.game.model.actor.instance.L2ControllableMobInstance").getConstructors()[0];
		_template = mobTemplate;

		setAmount(1);
	}

	public L2Npc doGroupSpawn()
	{
		L2Npc mob = null;

		try
		{
			if (_template.getType().equalsIgnoreCase("L2Pet") || _template.getType().equalsIgnoreCase("L2Minion"))
				return null;

			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};
			Object tmp = _constructor.newInstance(parameters);

			if (!(tmp instanceof L2Npc))
				return null;

			mob = (L2Npc) tmp;

			int newlocx, newlocy, newlocz;

			newlocx = getLocx();
			newlocy = getLocy();
			newlocz = getLocz();

			mob.getStatus().setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());

			if (getHeading() == -1)
			{
				mob.setHeading(Rnd.nextInt(61794));
			}
			else
			{
				mob.setHeading(getHeading());
			}

			mob.setSpawn(this);
			mob.spawnMe(newlocx, newlocy, newlocz);
			mob.onSpawn();

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("spawned Mob ID: " + _template.getNpcId() + " ,at: " + mob.getX() + " x, " + mob.getY() + " y, " + mob.getZ() + " z");
			}

			return mob;

		}
		catch (Exception e)
		{
			_log.warn("NPC class not found: " + e);
			return null;
		}
	}
}
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

/**
 * @author Forsaiken
 */
package com.dream.game.skills.l2skills;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2EffectPointInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.geometry.Point3D;
import com.dream.util.StatsSet;

public final class L2SkillSignet extends L2Skill
{
	private final int _effectNpcId;
	public int effectId;

	public L2SkillSignet(StatsSet set)
	{
		super(set);
		_effectNpcId = set.getInteger("effectNpcId", -1);
		effectId = set.getInteger("effectId", -1);
	}

	@Override
	public void useSkill(L2Character caster, L2Character... targets)
	{
		if (caster.isAlikeDead())
			return;

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(_effectNpcId);
		L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, caster);
		effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
		effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
		L2World.getInstance().storeObject(effectPoint);

		int x = caster.getX();
		int y = caster.getY();
		int z = caster.getZ();

		if (caster instanceof L2PcInstance && getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
		{
			Point3D wordPosition = ((L2PcInstance) caster).getCurrentSkillWorldPosition();

			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		getEffects(caster, effectPoint);

		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);
	}
}

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

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2EffectPointInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.skills.Env;
import com.dream.game.skills.l2skills.L2SkillSignet;
import com.dream.game.skills.l2skills.L2SkillSignetCasttime;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectSignet extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;

	public EffectSignet(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_EFFECT;
	}

	@Override
	public boolean onActionTime()
	{
		if (_skill == null)
			return true;

		int mpConsume = _skill.getMpConsume();

		if (mpConsume > getEffector().getStatus().getCurrentMp())
		{
			getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}

		getEffector().reduceCurrentMp(mpConsume);

		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null)
			{
				continue;
			}
			_skill.getEffects(_actor, cha);
			_actor.broadcastPacket(new MagicSkillUse(_actor, cha, _skill.getId(), _skill.getLevel(), 0, 0, false));
		}
		return true;
	}

	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}

	@Override
	public boolean onStart()
	{
		if (getSkill() instanceof L2SkillSignet)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignet) getSkill()).effectId, getLevel());
		}
		else if (getSkill() instanceof L2SkillSignetCasttime)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime) getSkill()).effectId, getLevel());
		}
		_actor = (L2EffectPointInstance) getEffected();
		return true;
	}
}
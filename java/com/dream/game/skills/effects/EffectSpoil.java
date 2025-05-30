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

import com.dream.game.ai.CtrlEvent;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectSpoil extends L2Effect
{
	public EffectSpoil(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SPOIL;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public boolean onStart()
	{
		if (!(getEffector() instanceof L2PcInstance))
			return false;

		if (!(getEffected() instanceof L2MonsterInstance))
			return false;

		L2MonsterInstance target = (L2MonsterInstance) getEffected();

		if (target.isSpoil())
		{
			getEffector().sendPacket(SystemMessageId.ALREADY_SPOILED);
			return false;
		}

		boolean spoil = false;
		if (!target.isDead())
		{
			spoil = Formulas.calcMagicSuccess(getEffector(), target, getSkill());

			if (spoil)
			{
				target.setSpoil(true);
				target.setIsSpoiledBy(getEffector().getObjectId());
				getEffector().sendPacket(SystemMessageId.SPOIL_SUCCESS);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2);
				sm.addCharName(target);
				sm.addSkillName(this);
				getEffector().sendPacket(sm);
			}
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
		}
		return true;
	}
}
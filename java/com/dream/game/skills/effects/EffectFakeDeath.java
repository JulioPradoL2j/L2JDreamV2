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

import com.dream.game.model.L2Effect;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectFakeDeath extends L2Effect
{

	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FAKE_DEATH;
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;

		double manaDam = calc();

		if (manaDam > getEffected().getStatus().getCurrentMp())
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
				return false;
			}

		getEffected().reduceCurrentMp(manaDam);
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopFakeDeath(this);
	}

	@Override
	public boolean onStart()
	{
		getEffected().startFakeDeath();
		return true;
	}
}

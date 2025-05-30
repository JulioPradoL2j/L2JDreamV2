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

import com.dream.game.ai.CtrlIntention;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.network.serverpackets.StartRotation;
import com.dream.game.network.serverpackets.StopRotation;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectBluff extends L2Effect
{
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLUFF;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		getEffected().stopStunning(this);
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2NpcInstance)
			return false;
		if (getEffected() instanceof L2Npc && ((L2Npc) getEffected()).getNpcId() == 35062)
			return false;
		if (getEffected() instanceof L2SiegeSummonInstance)
			return false;

		getEffected().setTarget(null);
		getEffected().abortAttack();
		getEffected().abortCast();
		getEffected().getAI().setIntention(CtrlIntention.IDLE, getEffector());
		getEffected().broadcastPacket(new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		getEffected().sendPacket(new ValidateLocation(getEffector()));
		getEffector().sendPacket(new ValidateLocation(getEffected()));
		getEffected().startStunning();
		return true;
	}
}

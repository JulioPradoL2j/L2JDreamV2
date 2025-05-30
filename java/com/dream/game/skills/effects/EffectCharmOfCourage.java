/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.skills.effects;

import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Env;
import com.dream.game.templates.skills.L2EffectType;

public final class EffectCharmOfCourage extends L2Effect
{
	public EffectCharmOfCourage(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CHARMOFCOURAGE;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		((L2PcInstance) getEffected()).setCharmOfCourage(false);
		((L2PcInstance) getEffected()).setCanUseCharmOfCourageRes(true);
		((L2PcInstance) getEffected()).setCanUseCharmOfCourageItem(true);
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).setCharmOfCourage(true);
			((L2PcInstance) getEffected()).setCanUseCharmOfCourageItem(false);
			return true;
		}
		return false;
	}
}
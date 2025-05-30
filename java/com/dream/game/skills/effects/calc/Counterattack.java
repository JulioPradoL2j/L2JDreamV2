package com.dream.game.skills.effects.calc;

import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.item.L2WeaponType;

public class Counterattack
{
	public static void vengeanceValue(Env env)
	{

		if (env.target.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			env.value = 0;
		}
		else
		{
			env.value = (int) Formulas.calcPhysDam(env.player, env.target, null, (byte) 0, true, false, env.player.getActiveWeaponInstance().getChargedSoulshot() != L2ItemInstance.CHARGED_NONE);
		}
	}
}

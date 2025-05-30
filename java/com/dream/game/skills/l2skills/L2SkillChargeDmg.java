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
package com.dream.game.skills.l2skills;

import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.BaseStats;
import com.dream.game.skills.Formulas;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.util.StatsSet;

public class L2SkillChargeDmg extends L2Skill
{
	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead() || !(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		double modifier = 0.8 + 0.201 * player.getCharges();
		if (getConsumeCharges())
		{
			player.decreaseCharges(getNeededCharges());
		}

		for (L2Character target : targets)
		{
			L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
			if (target.isAlikeDead())
			{
				continue;
			}

			byte shld = Formulas.calcShldUse(activeChar, target);
			boolean crit = false;
			if (getBaseCritRate() > 0)
			{
				crit = Formulas.calcCrit(getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar));
			}

			boolean soul = weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER;

			int damage = (int) Formulas.calcPhysDam(activeChar, target, this, shld, false, false, soul);
			if (crit)
			{
				damage *= 2;
			}

			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, this);
			if (skillIsEvaded)
			{
				if (activeChar instanceof L2PcInstance)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK);
					sm.addCharName(target);
					activeChar.sendPacket(sm);
				}
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK);
					sm.addCharName(activeChar);
					target.sendPacket(sm);
				}
			}
			else if (damage > 0)
			{
				double finalDamage = damage * modifier;
				target.reduceCurrentHp(finalDamage, activeChar, this);

				activeChar.sendDamageMessage(target, (int) finalDamage, false, crit, false);

				if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
				{
					activeChar.reduceCurrentHp(damage, target, this);
				}

				if (soul && weapon != null)
				{
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, true);
				}
			}
			else
			{
				activeChar.sendDamageMessage(target, 0, false, false, true);
			}
		}
		L2Effect seffect = activeChar.getFirstEffect(getId());
		if (seffect != null && seffect.isSelfEffect())
		{
			seffect.exit();
		}

		getEffectsSelf(activeChar);
	}
}
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

import com.dream.Config;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Formulas;
import com.dream.util.StatsSet;

public class L2SkillDrain extends L2Skill
{
	private final float _absorbPart;
	private final int _absorbAbs;

	public L2SkillDrain(StatsSet set)
	{
		super(set);

		_absorbPart = set.getFloat("absorbPart", 0.f);
		_absorbAbs = set.getInteger("absorbAbs", 0);
	}

	public void useCubicSkill(L2CubicInstance activeCubic, L2Character... targets)
	{
		if (Config.DEBUG)
		{
			_log.info("L2SkillDrain: useCubicSkill()");
		}

		for (L2Character target : targets)
		{
			if (target == null)
			{
				continue;
			}

			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
			int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit);
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.info("L2SkillDrain: useCubicSkill() -> damage = " + damage);
			}

			double hpAdd = _absorbAbs + _absorbPart * damage;
			L2PcInstance owner = activeCubic.getOwner();
			double hp = owner.getStatus().getCurrentHp() + hpAdd > owner.getMaxHp() ? owner.getMaxHp() : owner.getStatus().getCurrentHp() + hpAdd;

			owner.getStatus().setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(owner);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			owner.sendPacket(suhp);

			if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
			{
				target.reduceCurrentHp(damage, activeCubic.getOwner(), this);

				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}

	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean bss = false;

		for (L2Character target : targets)
		{
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}

			if (activeChar != target && (target.isInvul() || target.isPetrified()))
			{
				continue;
			}

			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

			if (weaponInst != null)
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				}
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					ss = true;
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				}
			}
			else if (activeChar instanceof L2Summon)
			{
				L2Summon activeSummon = (L2Summon) activeChar;

				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				}
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					ss = true;
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				}
			}

			else if (activeChar instanceof L2Npc)
			{
				bss = ((L2Npc) activeChar).isUsingShot(false);
				ss = ((L2Npc) activeChar).isUsingShot(true);
			}

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			int damage = (int) Formulas.calcMagicDam(activeChar, target, this, ss, bss, mcrit);

			int _drain = 0;
			int _cp = (int) target.getStatus().getCurrentCp();
			int _hp = (int) target.getStatus().getCurrentHp();

			if (_cp > 0)
			{
				if (damage < _cp)
				{
					_drain = 0;
				}
				else
				{
					_drain = damage - _cp;
				}
			}
			else if (damage > _hp)
			{
				_drain = _hp;
			}
			else
			{
				_drain = damage;
			}

			double hpAdd = _absorbAbs + _absorbPart * _drain;
			double hp = activeChar.getStatus().getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getStatus().getCurrentHp() + hpAdd;

			double hpDiff = hp - activeChar.getStatus().getCurrentHp();

			activeChar.getStatus().increaseHp(hpDiff);

			StatusUpdate suhp = new StatusUpdate(activeChar);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);

			if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
			{
				if (Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (hasEffects() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
					if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_SUCCEED) > 0)
					{
						activeChar.stopSkillEffects(getId());
						getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(this));
					}
					else
					{
						target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(activeChar, target, this, false, ss, bss))
						{
							getEffects(activeChar, target);
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_UNAFFECTED_BY_S2).addCharName(target).addSkillName(this));
						}
					}

				target.reduceCurrentHp(damage, activeChar, this);
			}
			if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2Npc)
			{
				((L2Npc) target).endDecayTask();
			}
		}

		L2Effect effect = activeChar.getFirstEffect(getId());
		if (effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}

		getEffectsSelf(activeChar);
	}
}

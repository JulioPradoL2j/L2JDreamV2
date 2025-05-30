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
package com.dream.game.templates.item;

import java.util.List;

import com.dream.game.handler.SkillHandler;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.skills.Formulas;
import com.dream.tools.random.Rnd;
import com.dream.util.StatsSet;

public final class L2Weapon extends L2Equip
{
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _pDam;
	private final int _rndDam;
	private final int _critical;
	private final double _hitModifier;
	private final int _avoidModifier;
	private final int _shieldDef;
	private final double _shieldDefRate;
	private final int _atkSpeed;
	private final int _atkReuse;
	private final int _mpConsume;
	private final int _mDam;

	private L2Skill[] _onCastSkills;
	private int[] _onCastChances;
	private L2Skill[] _onCritSkills;
	private int[] _onCritChances;
	private L2Skill[] _enchant4Skills;

	public L2Weapon(L2WeaponType type, StatsSet set)
	{
		super(type, set);
		_soulShotCount = set.getInteger("soulshots");
		_spiritShotCount = set.getInteger("spiritshots");
		_pDam = set.getInteger("p_dam");
		_rndDam = set.getInteger("rnd_dam");
		_critical = set.getInteger("critical");
		_hitModifier = set.getDouble("hit_modify");
		_avoidModifier = set.getInteger("avoid_modify");
		_shieldDef = set.getInteger("shield_def");
		_shieldDefRate = set.getDouble("shield_def_rate");
		_atkSpeed = set.getInteger("atk_speed");
		_atkReuse = set.getInteger("atk_reuse", initAtkReuse(type));
		_mpConsume = set.getInteger("mp_consume");
		_mDam = set.getInteger("m_dam");

		String[] enchant4SkillDefs = set.getString("skills_enchant4").split(";");
		String[] onCastSkillDefs = set.getString("skills_onCast").split(";");
		String[] onCritSkillDefs = set.getString("skills_onCrit").split(";");

		List<L2Skill> enchant4Skills = null;
		List<WeaponSkill> onCastSkills = null;
		List<WeaponSkill> onCritSkills = null;

		if (enchant4SkillDefs != null && enchant4SkillDefs.length > 0)
		{
			enchant4Skills = parseSkills(enchant4SkillDefs, "enchant4", "weapon");
		}

		if (onCastSkillDefs != null && onCastSkillDefs.length > 0)
		{
			onCastSkills = parseChanceSkills(onCastSkillDefs, "onCast", "weapon");
		}

		if (onCritSkillDefs != null && onCritSkillDefs.length > 0)
		{
			onCritSkills = parseChanceSkills(onCritSkillDefs, "onCrit", "weapon");
		}

		if (enchant4Skills != null && !enchant4Skills.isEmpty())
		{
			_enchant4Skills = enchant4Skills.toArray(new L2Skill[enchant4Skills.size()]);
		}
		if (onCastSkills != null && !onCastSkills.isEmpty())
		{
			_onCastSkills = new L2Skill[onCastSkills.size()];
			_onCastChances = new int[onCastSkills.size()];
			int i = 0;
			for (WeaponSkill ws : onCastSkills)
			{
				_onCastSkills[i] = ws.skill;
				_onCastChances[i] = ws.chance;
				i++;
			}
		}
		if (onCritSkills != null && !onCritSkills.isEmpty())
		{
			_onCritSkills = new L2Skill[onCritSkills.size()];
			_onCritChances = new int[onCritSkills.size()];
			int i = 0;
			for (WeaponSkill ws : onCritSkills)
			{
				_onCritSkills[i] = ws.skill;
				_onCritChances[i] = ws.chance;
				i++;
			}
		}
	}

	public int getAttackReuseDelay()
	{
		return _atkReuse;
	}

	public int getAttackSpeed()
	{
		return _atkSpeed;
	}

	public int getAvoidModifier()
	{
		return _avoidModifier;
	}

	public int getCritical()
	{
		return _critical;
	}

	public L2Skill[] getEnchant4Skills()
	{
		return _enchant4Skills;
	}

	public double getHitModifier()
	{
		return _hitModifier;
	}

	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}

	@Override
	public L2WeaponType getItemType()
	{
		return (L2WeaponType) super._type;
	}

	public int getMDamage()
	{
		return _mDam;
	}

	public int getMpConsume()
	{
		return _mpConsume;
	}

	public int getPDamage()
	{
		return _pDam;
	}

	public int getRandomDamage()
	{
		return _rndDam;
	}

	public int getShieldDef()
	{
		return _shieldDef;
	}

	public double getShieldDefRate()
	{
		return _shieldDefRate;
	}

	public boolean getSkillEffectsByCast(L2Character caster, L2Character target, L2Skill trigger)
	{
		if (_onCastSkills == null)
			return false;

		boolean affected = false;
		for (int i = 0; i < _onCastSkills.length; i++)
		{
			L2Skill skill = _onCastSkills[i];

			if (trigger.isOffensive() != skill.isOffensive())
			{
				continue;
			}

			if (trigger.isToggle() || trigger.isPotion())
			{
				continue;
			}

			if (trigger.getId() >= 1320 && trigger.getId() <= 1322)
			{
				continue;
			}

			if (!(Rnd.get(100) < _onCastChances[i]))
			{
				continue;
			}

			if (skill.isOffensive())
				if (!Formulas.calcSkillSuccess(caster, target, skill, false, false, false))
				{
					continue;
				}

			L2Character[] targets = new L2Character[]
			{
				target
			};

			try
			{
				SkillHandler.getInstance().getSkillHandler(skill.getSkillType()).useSkill(caster, skill, targets);

				affected = true;

				if (caster instanceof L2PcInstance)
				{
					for (L2Object spMob : caster.getKnownList().getKnownObjects().values())
						if (spMob instanceof L2Npc)
						{
							L2Npc npcMob = (L2Npc) spMob;

							if (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
							{
								for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
								{
									quest.notifySkillSee(npcMob, (L2PcInstance) caster, skill, targets, false);
								}
							}
						}
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}

		return affected;
	}

	public void getSkillEffectsByCrit(L2Character caster, L2Character target)
	{
		if (_onCritSkills == null)
			return;

		for (int i = 0; i < _onCritSkills.length; i++)
		{
			L2Skill skill = _onCritSkills[i];

			if (!(Rnd.get(100) < _onCritChances[i]))
			{
				continue;
			}

			if (!Formulas.calcSkillSuccess(caster, target, skill, false, false, false))
			{
				continue;
			}

			L2Effect effect = target.getFirstEffect(skill.getId());
			if (effect != null)
			{
				effect.exit();
			}
			skill.getEffects(caster, target);
		}
	}

	public int getSoulShotCount()
	{
		return _soulShotCount;
	}

	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}

	private int initAtkReuse(L2WeaponType type)
	{
		if (type == L2WeaponType.BOW)
		{
			if (_atkSpeed == 293)
				return 1500;
			if (_atkSpeed == 227)
				return 820;
		}

		return 0;
	}
}

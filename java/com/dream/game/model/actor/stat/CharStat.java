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
package com.dream.game.model.actor.stat;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Calculator;
import com.dream.game.skills.Env;
import com.dream.game.skills.Stats;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;

public class CharStat
{
	protected final static Logger _log = Logger.getLogger(CharStat.class.getName());

	protected final L2Character _activeChar;
	private long _exp = 0;
	private int _sp = 0;
	private byte _level = 1;
	public float _bonusHP = 1;
	public float _bonusMP = 1;
	public float _bonusAtk = 1;
	public float _bonusMAtk = 1;
	public float _bonusPDef = 1;
	public float _bonusMDef = 1;

	@SuppressWarnings("unused")
	private int _fire, _water, _wind, _earth, _holy, _dark = 0;

	public CharStat(L2Character activeChar)
	{
		_activeChar = activeChar;
	}

	public final void addElement(L2Skill skill)
	{
		switch (skill.getElement())
		{
			case L2Skill.ELEMENT_EARTH:
				_earth += skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_FIRE:
				_fire += skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_WATER:
				_water += skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_WIND:
				_wind += skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_HOLY:
				_holy += skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_DARK:
				_dark += skill.getMagicLevel();
				break;
		}
	}

	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return init;

		int id = stat.ordinal();

		Calculator c = _activeChar.getCalculators()[id];

		if (c == null || c.size() == 0)
			return init;

		Env env = new Env();
		env.player = _activeChar;
		env.target = target;
		env.skill = skill;
		env.value = init;
		env.baseValue = init;

		c.calc(env);
		if (env.value <= 0 && (stat == Stats.MAX_HP || stat == Stats.MAX_MP || stat == Stats.MAX_CP || stat == Stats.MAGIC_DEFENCE || stat == Stats.POWER_DEFENCE || stat == Stats.POWER_ATTACK || stat == Stats.MAGIC_ATTACK || stat == Stats.POWER_ATTACK_SPEED || stat == Stats.MAGIC_ATTACK_SPEED || stat == Stats.SHIELD_DEFENCE || stat == Stats.STAT_CON || stat == Stats.STAT_DEX || stat == Stats.STAT_INT || stat == Stats.STAT_MEN || stat == Stats.STAT_STR || stat == Stats.STAT_WIT))
		{
			env.value = 1;
		}

		c = null;

		return env.value;
	}

	public int getAccuracy()
	{
		if (_activeChar == null)
			return 0;

		return (int) (calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / _activeChar.getWeaponExpertisePenalty());
	}

	public L2Character getActiveChar()
	{
		return _activeChar;
	}

	public final float getAttackSpeedMultiplier()
	{
		if (_activeChar == null)
			return 1;

		return (float) (1.1 * getPAtkSpd() / _activeChar.getTemplate().getBasePAtkSpd());
	}

	public double getBowCritRate()
	{
		return 1;
	}

	public final int getCON()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_CON, _activeChar.getTemplate().getBaseCON(), null, null);
	}

	public double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}

	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		int criticalHit = (int) (calcStat(Stats.CRITICAL_RATE, _activeChar.getTemplate().getBaseCritRate(), target, skill) * 10.0 + 0.5);
		criticalHit /= 10;

		if (criticalHit > Config.ALT_PCRITICAL_CAP)
		{
			criticalHit = Config.ALT_PCRITICAL_CAP;
		}

		return criticalHit;
	}

	public final int getDEX()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_DEX, _activeChar.getTemplate().getBaseDEX(), null, null);
	}

	public int getEvasionRate(L2Character target)
	{
		if (_activeChar == null)
			return 1;

		int val = (int) (calcStat(Stats.EVASION_RATE, 0, target, null) / _activeChar.getArmourExpertisePenalty());

		return val;
	}

	public long getExp()
	{
		return _exp;
	}

	public int getINT()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_INT, _activeChar.getTemplate().getBaseINT(), null, null);
	}

	public byte getLevel()
	{
		return _level;
	}

	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);

		if (_activeChar == null)
			return 1;

		return _activeChar.getTemplate().getBaseAtkRange();
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		float bonusAtk = _bonusMAtk;
		if (_activeChar.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}

		double attack = _activeChar.getTemplate().getBaseMAtk() * bonusAtk;

		if (skill != null)
		{
			attack += skill.getPower();
		}

		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}

	public int getMAtkSpd()
	{
		if (_activeChar == null)
			return 1;

		float bonusSpdAtk = 1;
		if (_activeChar.isChampion())
		{
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		}

		int val = (int) calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.getTemplate().getBaseMAtkSpd() * bonusSpdAtk, null, null);

		if ((val > Config.MAX_MATK_SPEED) && (!getActiveChar().charIsGM()))
		{
			return Config.MAX_MATK_SPEED;
		}

		return val;
	}

	public int getMaxCp()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.MAX_CP, _activeChar.getTemplate().getBaseCpMax(), null, null);
	}

	public int getMaxHp()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.MAX_HP, _activeChar.getTemplate().getBaseHpMax() * _bonusHP, null, null);
	}

	public int getMaxMp()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.MAX_MP, _activeChar.getTemplate().getBaseMpMax() * _bonusMP, null, null);
	}

	public int getMCriticalHit(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		double mrate = calcStat(Stats.MCRITICAL_RATE, _activeChar.getTemplate().getBaseMCritRate(), target, skill);

		if (mrate > Config.ALT_MCRITICAL_CAP)
		{
			mrate = Config.ALT_MCRITICAL_CAP;
		}

		return (int) mrate;
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		double defence = _activeChar.getTemplate().getBaseMDef();

		if (_activeChar.isRaid())
		{
			defence *= Config.RAID_MDEFENCE_MULTIPLIER;
		}

		return (int) calcStat(Stats.MAGIC_DEFENCE, defence * _bonusMDef, target, skill);
	}

	public final int getMEN()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_MEN, _activeChar.getTemplate().getBaseMEN(), null, null);
	}

	public float getMovementSpeedMultiplier()
	{
		if (_activeChar == null)
			return 1;

		return getRunSpeed() / (float) _activeChar.getTemplate().getBaseRunSpd();
	}

	public final float getMoveSpeed()
	{
		if (_activeChar == null)
			return 1;

		if (_activeChar instanceof L2BoatInstance)
			return ((L2BoatInstance) _activeChar).boatSpeed;
		if (_activeChar.isRunning())
			return getRunSpeed();
		return getWalkSpeed();
	}

	public final int getMpConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;

		int mpconsume = skill.getMpConsume();

		if (!Config.ALT_DANCE_MP_CONSUME && (skill.isDance() || skill.isSong()) && _activeChar != null)
		{
			int count = _activeChar.getDanceCount(skill.isDance(), skill.isSong());
			if (count > 0)
			{
				mpconsume += count * skill.getNextDanceMpCost();
			}
		}

		return (int) calcStat(Stats.MP_CONSUME, mpconsume, null, skill);
	}

	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;

		return (int) calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
	}

	public final double getMReuseRate(L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		return calcStat(Stats.MAGIC_REUSE_RATE, _activeChar.getTemplate().getBaseMReuseRate(), null, skill);
	}

	public int getPAtk(L2Character target)
	{
		if (_activeChar == null)
			return 1;

		float bonusAtk = _bonusAtk;
		if (_activeChar.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}

		return (int) calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().getBasePAtk() * bonusAtk, target, null);
	}

	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}

	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}

	public final double getPAtkGiants(L2Character target)
	{
		return calcStat(Stats.PATK_GIANTS, 1, target, null);
	}

	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}

	public final double getPAtkMagic(L2Character target)
	{
		return calcStat(Stats.PATK_MAGIC, 1, target, null);
	}

	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}

	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}

	public int getPAtkSpd()
	{
		if (_activeChar == null)
			return 1;

		float bonusSpdAtk = 1;
		if (_activeChar.isChampion())
		{
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		}

		int val = (int) calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.getTemplate().getBasePAtkSpd() * bonusSpdAtk, null, null);

		if ((val > Config.MAX_PATK_SPEED) && (!getActiveChar().charIsGM()))
		{
			return Config.MAX_PATK_SPEED;
		}

		return val;
	}

	public final double getPAtkUndead(L2Character target)
	{
		return calcStat(Stats.PATK_UNDEAD, 1, target, null);
	}

	public int getPDef(L2Character target)
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.POWER_DEFENCE, _activeChar.isRaid() ? _activeChar.getTemplate().getBasePDef() * Config.RAID_PDEFENCE_MULTIPLIER * _bonusPDef : _activeChar.getTemplate().getBasePDef() * _bonusPDef, target, null);
	}

	public final double getPDefAnimals(L2Character target)
	{
		return calcStat(Stats.PDEF_ANIMALS, 1, target, null);
	}

	public final double getPDefDragons(L2Character target)
	{
		return calcStat(Stats.PDEF_DRAGONS, 1, target, null);
	}

	public final double getPDefGiants(L2Character target)
	{
		return calcStat(Stats.PDEF_GIANTS, 1, target, null);
	}

	public final double getPDefInsects(L2Character target)
	{
		return calcStat(Stats.PDEF_INSECTS, 1, target, null);
	}

	public final double getPDefMagic(L2Character target)
	{
		return calcStat(Stats.PDEF_MAGIC, 1, target, null);
	}

	public final double getPDefMonsters(L2Character target)
	{
		return calcStat(Stats.PDEF_MONSTERS, 1, target, null);
	}

	public final double getPDefPlants(L2Character target)
	{
		return calcStat(Stats.PDEF_PLANTS, 1, target, null);
	}

	public final double getPDefUndead(L2Character target)
	{
		return calcStat(Stats.PDEF_UNDEAD, 1, target, null);
	}

	public final int getPhysicalAttackRange()
	{
		if (_activeChar == null)
			return 1;

		L2Weapon weaponItem = _activeChar.getActiveWeaponItem();
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.POLE)
			return (int) calcStat(Stats.POWER_ATTACK_RANGE, 66, null, null);
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, _activeChar.getTemplate().getBaseAtkRange(), null, null);
	}

	public final double getPReuseRate(L2Skill skill)
	{
		if (_activeChar == null)
			return 1;

		return calcStat(Stats.PHYS_REUSE_RATE, _activeChar.getTemplate().baseMReuseRate, null, skill);
	}

	public int getRunSpeed()
	{
		if (_activeChar == null)
			return 1;

		double baseRunSpd = _activeChar.getTemplate().getBaseRunSpd();

		int val = (int) (calcStat(Stats.RUN_SPEED, baseRunSpd, null, null) * Config.RATE_RUN_SPEED);

		if (_activeChar.isInWater())
		{
			val /= 2;
		}

		val /= _activeChar.getArmourExpertisePenalty();

		if (_activeChar.isFlying())
		{
			val += Config.WYVERN_SPEED;
			return val;
		}

		if (_activeChar.isRiding())
		{
			val += Config.STRIDER_SPEED;
			return val;
		}

		if (val > Config.MAX_RUN_SPEED && Config.MAX_RUN_SPEED > 0 && !getActiveChar().charIsGM())
			return Config.MAX_RUN_SPEED;

		return val;
	}

	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}

	public int getSp()
	{
		return _sp;
	}

	public final int getSTR()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_STR, _activeChar.getTemplate().getBaseSTR(), null, null);
	}

	public int getWalkSpeed()
	{
		if (_activeChar == null)
			return 1;

		if (_activeChar instanceof L2PcInstance)
			return getRunSpeed() * 70 / 100;

		return (int) calcStat(Stats.WALK_SPEED, _activeChar.getTemplate().getBaseWalkSpd(), null, null);
	}

	public final int getWIT()
	{
		if (_activeChar == null)
			return 1;

		return (int) calcStat(Stats.STAT_WIT, _activeChar.getTemplate().getBaseWIT(), null, null);
	}

	public final void removeElement(L2Skill skill)
	{
		switch (skill.getElement())
		{
			case L2Skill.ELEMENT_EARTH:
				_earth -= skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_FIRE:
				_fire -= skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_WATER:
				_water -= skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_WIND:
				_wind -= skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_HOLY:
				_holy -= skill.getMagicLevel();
				break;
			case L2Skill.ELEMENT_DARK:
				_dark -= skill.getMagicLevel();
				break;
		}
	}

	public void setExp(long value)
	{
		_exp = value;
	}

	public void setLevel(byte value)
	{
		_level = value;
	}

	public void setSp(int value)
	{
		_sp = value;
	}
}
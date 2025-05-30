package com.dream.game.skills;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.xml.ZoneTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.base.PlayerState;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.conditions.ConditionPlayerState;
import com.dream.game.skills.conditions.ConditionUsingItemType;
import com.dream.game.skills.funcs.Func;
import com.dream.game.templates.chars.L2NpcTemplate.Race;
import com.dream.game.templates.chars.L2PcTemplate;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

import org.apache.log4j.Logger;

public final class Formulas
{
	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();
			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncAddLevel3(stat);
			}
			return _instancies[pos];
		}

		private FuncAddLevel3(Stats pStat)
		{
			super(pStat, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value += env.player.getLevel() / 3.0;
		}
	}

	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

		static Func getInstance()
		{
			return _faa_instance;
		}

		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();

			env.value += Math.sqrt(env.player.getDEX()) * 6;
			env.value += level;
			if (level > 89)
			{
				env.value += level - 89;
			}
			if (env.player instanceof L2Summon)
			{
				env.value += level < 89 ? 4 : 5;
			}
		}
	}

	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, 0x09, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			if (p instanceof L2SummonInstance)
			{
				env.value = 40;
			}
			else if (p instanceof L2PcInstance)
				if (p.getActiveWeaponInstance() == null)
				{
					env.value = 40;
				}
				else
				{
					env.value *= BaseStats.DEX.calcBonus(p);
					env.value *= 10;
				}
			env.baseValue = env.value;
		}
	}

	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

		static Func getInstance()
		{
			return _fae_instance;
		}

		private FuncAtkEvasion()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();

			env.value += Math.sqrt(env.player.getDEX()) * 6;
			env.value += level;
			if (level > 89)
			{
				env.value += level - 89;
			}
		}
	}

	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange _fbarInstance = new FuncBowAtkRange();

		static Func getInstance()
		{
			return _fbarInstance;
		}

		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null, new ConditionUsingItemType(L2WeaponType.BOW.mask()));
		}

		@Override
		public void calc(Env env)
		{
			env.value += 460;
		}
	}

	static class FuncGatesMDefMod extends Func
	{
		static final FuncGatesMDefMod _fmm_instance = new FuncGatesMDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncGatesMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
			{
				env.value *= Config.ALT_SIEGE_DAWN_GATES_MDEF_MULT;
			}
			else if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK)
			{
				env.value *= Config.ALT_SIEGE_DUSK_GATES_MDEF_MULT;
			}
		}
	}

	static class FuncGatesPDefMod extends Func
	{
		static final FuncGatesPDefMod _fmm_instance = new FuncGatesPDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncGatesPDefMod()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
			{
				env.value *= Config.ALT_SIEGE_DAWN_GATES_PDEF_MULT;
			}
			else if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK)
			{
				env.value *= Config.ALT_SIEGE_DUSK_GATES_PDEF_MULT;
			}
		}
	}

	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON _fhInstance = new FuncHennaCON();

		static Func getInstance()
		{
			return _fhInstance;
		}

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatCON();
			}
		}
	}

	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX _fhInstance = new FuncHennaDEX();

		static Func getInstance()
		{
			return _fhInstance;
		}

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatDEX();
			}
		}
	}

	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT _fhInstance = new FuncHennaINT();

		static Func getInstance()
		{
			return _fhInstance;
		}

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatINT();
			}
		}
	}

	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN _fhInstance = new FuncHennaMEN();

		static Func getInstance()
		{
			return _fhInstance;
		}

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatMEN();
			}
		}
	}

	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR _fhInstance = new FuncHennaSTR();

		static Func getInstance()
		{
			return _fhInstance;
		}

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatSTR();
			}
		}
	}

	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT _fhInstance = new FuncHennaWIT();

		static Func getInstance()
		{
			return _fhInstance;
		}

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatWIT();
			}
		}
	}

	static class FuncMAtkCritical extends Func
	{
		static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncMAtkCritical()
		{
			super(Stats.MCRITICAL_RATE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			if (p instanceof L2Summon)
			{
				env.value = 8;
			}
			else if (p instanceof L2PcInstance && p.getActiveWeaponInstance() != null)
			{
				env.value *= BaseStats.WIT.calcBonus(p);
			}
		}
	}

	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

		static Func getInstance()
		{
			return _fma_instance;
		}

		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			double intb = BaseStats.INT.calcBonus(env.player);
			double lvlb = env.player.getLevelMod();
			env.value *= lvlb * lvlb * (intb * intb);
		}
	}

	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.WIT.calcBonus(env.player);
		}
	}

	static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd _fmcaInstance = new FuncMaxCpAdd();

		static Func getInstance()
		{
			return _fmcaInstance;
		}

		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.getClassBaseLevel();
			double cpmod = t.getLvlCpMod() * lvl;
			double cpmax = (t.getLvlCpAdd() + cpmod) * lvl;
			double cpmin = t.getLvlCpAdd() * lvl + cpmod;
			env.value += (cpmax + cpmin) / 2;
		}
	}

	static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

		static Func getInstance()
		{
			return _fmcm_instance;
		}

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.player);
		}
	}

	static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd _fmhaInstance = new FuncMaxHpAdd();

		static Func getInstance()
		{
			return _fmhaInstance;
		}

		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.getClassBaseLevel();
			double hpmod = t.getLvlHpMod() * lvl;
			double hpmax = (t.getLvlHpAdd() + hpmod) * lvl;
			double hpmin = t.getLvlHpAdd() * lvl + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}

	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

		static Func getInstance()
		{
			return _fmhm_instance;
		}

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.player);
		}
	}

	static class FuncMaxLoad extends Func
	{
		static final FuncMaxLoad _fmsInstance = new FuncMaxLoad();

		static Func getInstance()
		{
			return _fmsInstance;
		}

		private FuncMaxLoad()
		{
			super(Stats.MAX_LOAD, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.player);
		}
	}

	static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd _fmmaInstance = new FuncMaxMpAdd();

		static Func getInstance()
		{
			return _fmmaInstance;
		}

		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.getClassBaseLevel();
			double mpmod = t.getLvlMpMod() * lvl;
			double mpmax = (t.getLvlMpAdd() + mpmod) * lvl;
			double mpmin = t.getLvlMpAdd() * lvl + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}

	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

		static Func getInstance()
		{
			return _fmmm_instance;
		}

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.player);
		}
	}

	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod _fmm_instance = new FuncMDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if (env.player instanceof L2PcInstance)
			{
				L2PcInstance p = (L2PcInstance) env.player;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				{
					env.value -= 5;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				{
					env.value -= 5;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				{
					env.value -= 9;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				{
					env.value -= 9;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				{
					env.value -= 13;
				}
			}
			env.value *= BaseStats.MEN.calcBonus(env.player) * env.player.getLevelMod();
		}
	}

	static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

		static Func getInstance()
		{
			return _fms_instance;
		}

		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
		}
	}

	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{

			int pos = stat.ordinal();
			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultLevelMod(stat);
			}
			return _instancies[pos];
		}

		private FuncMultLevelMod(Stats pStat)
		{
			super(pStat, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= env.player.getLevelMod();
		}
	}

	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();

			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultRegenResting(stat);
			}

			return _instancies[pos];
		}

		private FuncMultRegenResting(Stats pStat)
		{
			super(pStat, 0x20, null, new ConditionPlayerState(PlayerState.RESTING, true));
		}

		@Override
		public void calc(Env env)
		{
			env.value *= 1.45;
		}
	}

	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

		static Func getInstance()
		{
			return _fpa_instance;
		}

		private FuncPAtkMod()
		{
			super(Stats.POWER_ATTACK, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.STR.calcBonus(env.player) * env.player.getLevelMod();
		}
	}

	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
		}
	}

	static class FuncPDefMod extends Func
	{
		static final FuncPDefMod _fmm_instance = new FuncPDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncPDefMod()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if (env.player instanceof L2PcInstance)
			{
				L2PcInstance p = (L2PcInstance) env.player;
				boolean hasMagePDef = p.getClassId().isMage() || p.getClassId().getId() == 0x31; // orc mystics are a
				// special case
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
				{
					env.value -= 12;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
				{
					env.value -= hasMagePDef ? 15 : 31;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
				{
					env.value -= hasMagePDef ? 8 : 18;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
				{
					env.value -= 8;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
				{
					env.value -= 7;
				}
			}
			env.value *= env.player.getLevelMod();
		}
	}

	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());

	private static final int HP_REGENERATE_PERIOD = 3000;

	public static final byte SKILL_REFLECT_FAILED = 0;

	public static final byte SKILL_REFLECT_SUCCEED = 1;

	public static final byte SKILL_REFLECT_VENGEANCE = 2;

	private static final int MELEE_ATTACK_RANGE = 900;

	public static void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpAdd.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			if (Config.LEVEL_ADD_LOAD)
			{
				cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAX_LOAD));
			}
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
			cha.addStatFunc(FuncMaxLoad.getInstance());
			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());
		}
		else if (cha instanceof L2PetInstance)
		{
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
		}
		else if (cha instanceof L2Summon)
		{
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
		}
	}

	public static final boolean calcAtkBreak(L2Character target, double dmg)
	{
		if (target.isRaid() || target.isInvul() || dmg <= 0)
			return false;

		if (target.getFusionSkill() != null)
			return true;

		double init = 0;

		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
		{
			init = 15;
		}
		else if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow() && target.getActiveWeaponItem() != null && target.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			init = 15;
		}
		else
			return false;

		init += Math.sqrt(13 * dmg);

		init -= BaseStats.MEN.calcBonus(target) * 100 - 100;

		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);

		if (rate > 99)
		{
			rate = 99;
		}
		else if (rate < 1)
		{
			rate = 1;
		}

		return Rnd.get(100) < rate;
	}

	public static final int calcAtkSpd(L2Character attacker, L2Skill skill, double time)
	{
		if (skill.isItemSkill() && Config.ALT_ITEM_SKILLS_NOT_INFLUENCED)
			return (int) time;
		else if (skill.isMagic())
			return (int) (time * 333 / attacker.getMAtkSpd());
		else
			return (int) (time * 333 / attacker.getPAtkSpd());
	}

	public static final boolean calcBlow(L2Character activeChar, L2Character target, int chance)
	{
		double rate = activeChar.calcStat(Stats.BLOW_RATE, chance * (1.0 + (activeChar.getStat().getDEX() - 20) / 100), target, null);
		boolean success = rate > Rnd.get(100);

		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isShowSkillChance() && !Config.SHOW_DEBUFF_ONLY)
		{
			if (rate > 100)
			{
				rate = 100;
			}
			else if (rate < 0)
			{
				rate = 0;
			}

			if (success)
			{
				((L2PcInstance) activeChar).sendMessage(String.format(Message.getMessage((L2PcInstance) activeChar, Message.MessageId.MSG_SKILL_CHANS_SUCCES), (int) rate));
			}
			else
			{
				((L2PcInstance) activeChar).sendMessage(String.format(Message.getMessage((L2PcInstance) activeChar, Message.MessageId.MSG_SKILL_CHANS), (int) rate));
			}
		}

		return success;
	}

	public static double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss)
	{
		boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		double power = skill.getPower();
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);

		damage *= calcSkillVulnerability(target, skill);
		damage += calcValakasAttribute(attacker, target, skill);

		if (ss)
		{
			damage *= 2.;
		}

		switch (shld)
		{
			case 1:
				defence += target.getShldDef();
				break;
			case 2:
				return 1;
		}

		if (ss && skill.getSSBoost() > 0)
		{
			power *= skill.getSSBoost();
		}

		damage = (attacker.calcStat(Stats.CRITICAL_DAMAGE, damage + power, target, skill) + attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.5) * target.calcStat(Stats.CRIT_VULN, target.getTemplate().baseCritVuln, target, skill);

		if (target instanceof L2Npc)
		{
			damage *= ((L2Npc) target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
		}

		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage *= 70. / defence;
		damage += Rnd.nextDouble() * attacker.getRandomDamage(target);

		if (isPvP)
		{
			damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}

		if (target instanceof L2PcInstance)
		{
			L2Armor armor = ((L2PcInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((L2PcInstance) target).isWearingHeavyArmor())
				{
					damage *= 1;
				}
				if (((L2PcInstance) target).isWearingLightArmor())
				{
					damage *= 1;
				}
				if (((L2PcInstance) target).isWearingMagicArmor())
				{
					damage *= 1;
				}
			}
		}
		else
		{
			damage *= 1;
		}

		return damage < 1 ? 1. : damage;
	}

	public static final double calcCpRegen(L2Character cha)
	{
		double init = cha.getTemplate().getBaseHpReg();
		double cpRegenMultiplier = Config.PLAYER_CP_REGEN_MULTIPLIER;
		double cpRegenBonus = 0;

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;

			if (player.isSitting())
			{
				init *= 1.5;
			}
			else if (!player.isMoving())
			{
				init *= 1.1;
			}
			else if (player.isRunning())
			{
				init *= 0.7;
			}
		}
		else if (!cha.isMoving())
		{
			init *= 1.1;
		}
		else if (cha.isRunning())
		{
			init *= 0.7;
		}

		init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);

		if (init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier + cpRegenBonus;
	}

	public static final boolean calcCrit(double rate)
	{
		return rate > Rnd.get(1000);
	}

	public static final boolean calcCrit(L2Character attacker, L2Character target, double rate)
	{
		int critHit = Rnd.get(1000);
		if (attacker instanceof L2PcInstance)
		{
			if (attacker.isBehindTarget())
			{
				critHit = Rnd.get(700);
			}
			else if (!attacker.isFacing(target, 60) && !attacker.isBehindTarget())
			{
				critHit = Rnd.get(800);
			}
			critHit = Rnd.get(900);
		}
		return rate > critHit;
	}

	public static boolean calcCubicSkillSuccess(L2CubicInstance attacker, L2Character target, L2Skill skill)
	{
		L2SkillType type = skill.getSkillType();

		if (target.isRaid() && (type == L2SkillType.CONFUSION || type == L2SkillType.MUTE || type == L2SkillType.PARALYZE || type == L2SkillType.ROOT || type == L2SkillType.FEAR || type == L2SkillType.SLEEP || type == L2SkillType.STUN || type == L2SkillType.DEBUFF || type == L2SkillType.AGGDEBUFF))
			return false;

		if (calcSkillReflect(target, skill) != SKILL_REFLECT_FAILED)
			return false;

		int value = (int) skill.getPower();
		int lvlDepend = skill.getLevelDepend();

		if (type == L2SkillType.PDAM || type == L2SkillType.MDAM)
		{
			value = skill.getEffectPower();
			type = skill.getEffectType();
		}

		if (value == 0)
		{
			value = type == L2SkillType.PARALYZE ? 50 : type == L2SkillType.FEAR ? 40 : 80;
		}
		if (lvlDepend == 0)
		{
			lvlDepend = type == L2SkillType.PARALYZE || type == L2SkillType.FEAR ? 1 : 2;
		}

		double statmodifier = calcSkillStatModifier(skill, target);
		double resmodifier = calcSkillVulnerability(target, skill);

		int rate = (int) (value * statmodifier * resmodifier);
		if (skill.isMagic())
		{
			rate += (int) (Math.pow((double) attacker.getMAtk() / target.getMDef(attacker.getOwner(), skill), 0.2) * 100) - 100;
		}

		if (lvlDepend > 0)
		{
			double delta = 0;
			int attackerLvlmod = attacker.getOwner().getLevel();
			int targetLvlmod = target.getLevel();

			if (attackerLvlmod >= 70)
			{
				attackerLvlmod = (attackerLvlmod - 69) * 2 + 70;
			}
			if (targetLvlmod >= 70)
			{
				targetLvlmod = (targetLvlmod - 69) * 2 + 70;
			}

			if (skill.getMagicLevel() == 0)
			{
				delta = attackerLvlmod - targetLvlmod;
			}
			else
			{
				delta = (skill.getMagicLevel() + attackerLvlmod) / 2 - targetLvlmod;
			}

			double deltamod = 1;

			if (delta + 3 < 0)
			{
				if (delta <= -20)
				{
					deltamod = 0.05;
				}
				else
				{
					deltamod = 1 - -1 * (delta / 20);
					if (deltamod >= 1)
					{
						deltamod = 0.05;
					}
				}
			}
			else
			{
				deltamod = 1 + (delta + 3) / 75;
			}

			if (deltamod < 0)
			{
				deltamod *= -1;
			}

			rate *= deltamod;
		}

		if (rate > 99)
		{
			rate = 99;
		}
		else if (rate < 1)
		{
			rate = 1;
		}
		boolean success = Rnd.get(100) < rate;
		if (attacker.getOwner().isShowSkillChance())
		{
			attacker.getOwner().sendMessage("Cubic skill " + skill.getName() + " chance is " + rate + ", " + (success ? "success" : "unsuccess"));
		}
		return success;
	}

	@SuppressWarnings("deprecation")
	public static final double calcFestivalRegenModifier(L2PcInstance activeChar)
	{
		final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
		final int oracle = festivalInfo[0];
		final int festivalId = festivalInfo[1];
		int[] festivalCenter;

		if (festivalId < 0)
			return 0;

		if (oracle == SevenSigns.CABAL_DAWN)
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
		}
		else
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
		}

		double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);
		return 1.0 - distToCenter * 0.0005;
	}

	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		int delta = attacker.getAccuracy() - target.getEvasionRate(attacker);
		int chance;
		if (delta >= 10)
		{
			chance = 980;
		}
		else
		{
			switch (delta)
			{
				case 9:
					chance = 975;
					break;
				case 8:
					chance = 970;
					break;
				case 7:
					chance = 965;
					break;
				case 6:
					chance = 960;
					break;
				case 5:
					chance = 955;
					break;
				case 4:
					chance = 945;
					break;
				case 3:
					chance = 935;
					break;
				case 2:
					chance = 925;
					break;
				case 1:
					chance = 915;
					break;
				case 0:
					chance = 905;
					break;
				case -1:
					chance = 890;
					break;
				case -2:
					chance = 875;
					break;
				case -3:
					chance = 860;
					break;
				case -4:
					chance = 845;
					break;
				case -5:
					chance = 830;
					break;
				case -6:
					chance = 815;
					break;
				case -7:
					chance = 800;
					break;
				case -8:
					chance = 785;
					break;
				case -9:
					chance = 770;
					break;
				case -10:
					chance = 755;
					break;
				case -11:
					chance = 735;
					break;
				case -12:
					chance = 715;
					break;
				case -13:
					chance = 695;
					break;
				case -14:
					chance = 675;
					break;
				case -15:
					chance = 655;
					break;
				case -16:
					chance = 625;
					break;
				case -17:
					chance = 595;
					break;
				case -18:
					chance = 565;
					break;
				case -19:
					chance = 535;
					break;
				case -20:
					chance = 505;
					break;
				case -21:
					chance = 455;
					break;
				case -22:
					chance = 405;
					break;
				case -23:
					chance = 355;
					break;
				case -24:
					chance = 305;
					break;
				default:
					chance = 275;
			}
			if (!attacker.isInFrontOfTarget())
			{
				if (attacker.isBehindTarget())
				{
					chance *= 1.2;
				}
				else
				{
					chance *= 1.1;
				}

				if (chance > 980)
				{
					chance = 980;
				}
			}
		}
		int seed = 0;
		boolean seedCaled = false;
		for (int i = 0; i < 10; i++)
		{
			seed = Rnd.get(1000);
			if (seed > 0)
			{
				seedCaled = true;
				break;
			}
		}
		if (!seedCaled)
		{
			_log.warn("Impossible! 10 iterations got 0 for " + attacker.getName() + " to " + target.getName());
		}
		return chance < Rnd.get(1000);
	}

	public static final double calcHpRegen(L2Character cha)
	{

		if (cha == null)
			return 1;

		double init = cha.getTemplate().getBaseHpReg();
		double hpRegenMultiplier;
		double hpRegenBonus = 0;

		if (cha.isRaid())
		{
			hpRegenMultiplier = Config.RAID_HP_REGEN_MULTIPLIER;
		}
		else if (cha instanceof L2PcInstance)
		{
			hpRegenMultiplier = Config.PLAYER_HP_REGEN_MULTIPLIER;
		}
		else if (cha instanceof L2PcInstance)
		{
			hpRegenMultiplier = Config.PET_HP_REGEN_MULTIPLIER;
		}
		else
		{
			hpRegenMultiplier = Config.NPC_HP_REGEN_MULTIPLIER;
		}

		if (cha.isChampion())
		{
			hpRegenMultiplier *= Config.CHAMPION_HP_REGEN;
		}

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;

			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				init *= calcFestivalRegenModifier(player);
			}
			else
			{
				double siegeModifier = calcSiegeRegenModifer(player);
				if (siegeModifier > 0)
				{
					init *= siegeModifier;
				}
			}

			if (player.isInsideZone(L2Zone.FLAG_MOTHERTREE))
			{
				hpRegenBonus += 2;
			}

			int clanHallId = player.getClan() == null ? 0 : player.getClan().getHasHideout();
			if (player.isInsideZone(L2Zone.FLAG_CLANHALL) && clanHallId > 0)
			{
				ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallId);
				if (clansHall != null)
				{
					L2Zone zone = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Clanhall, player.getX(), player.getY());
					int zoneChId = zone == null ? -1 : zone.getClanhallId();

					if (clanHallId == zoneChId && clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
					{
						hpRegenMultiplier *= 1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100;
					}
				}
			}

			int castleId = player.getClan() == null ? 0 : player.getClan().getHasCastle();
			if (player.isInsideZone(L2Zone.FLAG_CASTLE) && castleId > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(castleId);
				if (castle != null)
				{
					L2Zone zone = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Castle, player.getX(), player.getY());
					int zoneCsId = zone == null ? -1 : zone.getCastleId();

					if (castleId == zoneCsId && castle.getFunction(Castle.FUNC_RESTORE_HP) != null)
					{
						hpRegenMultiplier *= 1 + castle.getFunction(Castle.FUNC_RESTORE_HP).getLvl() / 100;
					}
				}
			}

			int fortId = player.getClan() == null ? 0 : player.getClan().getHasFort();
			if (player.isInsideZone(L2Zone.FLAG_FORT) && fortId > 0)
			{
				Fort fort = FortManager.getInstance().getFortById(fortId);
				if (fort != null)
				{
					L2Zone zone = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Fort, player.getX(), player.getY());
					int zoneFdId = zone == null ? -1 : zone.getFortId();

					if (fortId == zoneFdId && fort.getFunction(Fort.FUNC_RESTORE_HP) != null)
					{
						hpRegenMultiplier *= 1 + fort.getFunction(Fort.FUNC_RESTORE_HP).getLvl() / 100;
					}
				}
			}

			// Calculate Movement bonus
			if (player.isSitting())
			{
				hpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				hpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				hpRegenMultiplier *= 0.7; // Running
			}

			// Add CON bonus
			init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		}
		else if (cha instanceof L2PetInstance)
		{
			init = ((L2PetInstance) cha).getPetData().getPetRegenHP();
		}

		if (init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
	}

	public static final double calcLethal(L2Character activeChar, L2Character target, int baseLethal, int magiclvl)
	{
		double chance = 0;
		if (magiclvl > 0)
		{
			int delta = (magiclvl + activeChar.getLevel()) / 2 - 1 - target.getLevel();

			if (delta >= -3)
			{
				chance = baseLethal * ((double) activeChar.getLevel() / target.getLevel());
			}
			else if (delta < -3 && delta >= -9)
			{
				chance = -3 * (baseLethal / delta);
			}
			else
			{
				chance = baseLethal / 15;
			}
		}
		else
		{
			chance = baseLethal * ((double) activeChar.getLevel() / target.getLevel());
		}
		return activeChar.calcStat(Stats.LETHAL_RATE, chance, target, null);
	}

	public static final boolean calcLethalHit(L2Character activeChar, L2Character target, L2Skill skill)
	{
		if (!(target instanceof L2Boss) && !(target instanceof L2DoorInstance) && !(target instanceof L2Npc && ((L2Npc) target).isLethalImmune() && !(Config.ALLOW_LETHAL_PROTECTION_MOBS && target instanceof L2Npc && (Config.LIST_LETHAL_PROTECTED_MOBS.contains(((L2Npc) target).getNpcId())))))
		{
			if (target.isInvul() || target.isPetrified())
				return false;
			int chance = 100;
			double rate = 0;
			switch (skill.getLethalType())
			{
				case 1:
					rate = Config.ALT_LETHAL_RATE_OTHER;
					break;
				case 2:
					rate = Config.ALT_LETHAL_RATE_DAGGER;
					break;
				case 3:
					rate = Config.ALT_LETHAL_RATE_ARCHERY;
					break;
			}
			chance *= target.calcStat(Stats.LETHAL_VULN, 1, target, skill);

			if (skill.getLethalChance1() > 0 && Rnd.get(chance) < rate * calcLethal(activeChar, target, skill.getLethalChance1(), skill.getMagicLevel()))
			{
				if (target instanceof L2PcInstance)
				{
					((L2PcInstance) target).getStatus().setCurrentCp(1);
					activeChar.sendPacket(SystemMessageId.LETHAL_STRIKE);
					target.sendPacket(SystemMessageId.LETHAL_STRIKE);
				}
				else if (target instanceof L2Npc)
				{
					target.reduceCurrentHp(target.getStatus().getCurrentHp() / 2, activeChar, skill);
					activeChar.sendPacket(SystemMessageId.LETHAL_STRIKE);
					target.sendPacket(SystemMessageId.LETHAL_STRIKE);
				}
			}

			else if (skill.getLethalChance2() > 0 && Rnd.get(chance) < rate * calcLethal(activeChar, target, skill.getLethalChance2(), skill.getMagicLevel()))
			{
				if (target instanceof L2PcInstance)
				{
					((L2PcInstance) target).getStatus().setCurrentHp(1);
					((L2PcInstance) target).getStatus().setCurrentCp(1);
					((L2PcInstance) activeChar).sendPacket(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL);
					((L2PcInstance) target).sendPacket(SystemMessageId.LETHAL_STRIKE);
				}
				else if (target instanceof L2Npc)
				{
					target.reduceCurrentHp(target.getStatus().getCurrentHp() - 1, activeChar, skill);
					activeChar.sendPacket(SystemMessageId.LETHAL_STRIKE);
					target.sendPacket(SystemMessageId.LETHAL_STRIKE);
				}
			}

			else
				return false;
		}
		else
			return false;

		return true;
	}

	public static boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		double defence = 0;
		double attack = 0;

		if (skill.isActive() && skill.isOffensive() && !skill.isNeutral())
		{
			defence = target.getMDef(actor, skill);
		}

		if (actor instanceof L2PcInstance)
		{
			attack = 3.7 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
		}
		else
		{
			attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
		}

		double d = attack - defence;
		d /= attack + defence;
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}

	@SuppressWarnings("null")
	public static final double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss, boolean mcrit)
	{
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;

		if (bss)
		{
			mAtk *= 4;
		}
		else if (ss)
		{
			mAtk *= 2;
		}

		double power = skill.getPower();

		if (skill.getSkillType() == L2SkillType.DEATHLINK)
		{
			double part = attacker.getStatus().getCurrentHp() / attacker.getMaxHp();
			power *= Math.pow(1.7165 - part, 2) * 0.577;
		}

		double damage = 91 * Math.sqrt(mAtk) / mDef * power * calcSkillVulnerability(target, skill);

		if (attacker instanceof L2Summon && target instanceof L2PcInstance)
		{
			damage *= 0.9;
		}
		if (Config.USE_LEVEL_PENALTY && skill != null && !skill.isItemSkill() && skill.getMagicLvl() > 40 && attacker instanceof L2Playable)
		{
			int lvl = attacker.getActingPlayer().getLevel();
			int sklvl = skill.getLevel() > 100 ? 76 : skill.getMagicLvl();
			if (lvl - sklvl < -2)
			{
				damage *= 1 / (skill.getMagicLvl() - lvl);
			}

		}

		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			SystemMessage sm;
			if (attacker instanceof L2PcInstance)
			{

				L2PcInstance attOwner = attacker.getActingPlayer();
				if (calcMagicSuccess(attacker, target, skill) && target.getLevel() - attacker.getLevel() <= 9)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
					sm.addString(target.getName() + " successfully resists magic " + skill.getName());
					attOwner.sendPacket(sm);

					damage /= 2;
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
					sm.addString(target.getName() + " successfully resists magic " + skill.getName());
					attOwner.sendPacket(sm);

					if (mcrit)
					{
						damage = 1;
					}
					else
					{
						damage = Rnd.nextBoolean() ? 1 : 0;
					}

					return damage;
				}
			}

			if (target instanceof L2PcInstance)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
				sm.addString(target.getName() + " successfully resists magic " + skill.getName());
				target.getActingPlayer().sendPacket(sm);
			}
		}
		if (!Config.USE_CHAR_LEVEL_MOD && attacker instanceof L2Playable && target instanceof L2Playable)
		{
			damage /= 2;
		}

		if (mcrit)
		{
			damage *= Config.MCRIT_RATE;
		}

		damage += Rnd.nextDouble() * attacker.getRandomDamage(target);

		if (isPvP)
			if (!(skill.isItemSkill() && Config.ALT_ITEM_SKILLS_NOT_INFLUENCED))
				if (skill.isMagic())
				{
					damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1, null, null);
					damage /= target.calcStat(Stats.PVP_MAGICAL_DEF, 1, null, null);
				}
				else
				{
					damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
					damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
				}

		if (attacker instanceof L2PcInstance)
		{
			if (((L2PcInstance) attacker).getClassId().isMage())
			{
				damage *= 1;
			}
			else
			{
				damage *= 1;
			}
		}

		else if (attacker instanceof L2Summon)
		{
			damage *= 1;
		}
		else if (attacker instanceof L2Npc)
		{
			damage *= 1;
		}

		if (attacker instanceof L2PcInstance && attacker.getLevel() > 40)
			if (skill != null && attacker.getLevel() - skill.getMagicLvl() > 20)
			{
				damage /= 50;
				if (damage < 1)
				{
					damage = 1;
				}
			}
		return damage;
	}

	public static final double calcMagicDam(L2CubicInstance attacker, L2Character target, L2Skill skill, boolean mcrit)
	{
		if (target.isInvul())
			return 0;

		double mAtk = attacker.getMAtk();
		double mDef = target.getMDef(attacker.getOwner(), skill);

		L2PcInstance owner = attacker.getOwner();

		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower() * calcSkillVulnerability(target, skill);

		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(owner, target, skill))
		{
			if (calcMagicSuccess(owner, target, skill) && target.getLevel() - skill.getMagicLevel() <= 9)
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					owner.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
				}
				else
				{
					owner.sendPacket(SystemMessageId.ATTACK_FAILED);
				}
				damage /= 2;
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
				sm.addString(target.getName() + " successfully resists magic " + skill.getName());
				owner.sendPacket(sm);
				damage = 1;
			}

			if (target instanceof L2PcInstance)
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN);
					sm.addPcName(owner);
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
					sm.addPcName(owner);
					target.sendPacket(sm);
				}
		}
		else if (mcrit)
		{
			damage *= 4;
		}

		return damage;
	}

	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		double lvlDifference = target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel());
		if (!Config.USE_CHAR_LEVEL_MOD && attacker.getActingPlayer() != null && target.getActingPlayer() != null)
		{
			lvlDifference = lvlDifference > 1 ? 1 : lvlDifference;
		}
		int rate = Math.round((float) (Math.pow(1.3, lvlDifference) * 100));

		boolean success = rate < Rnd.get(10000);
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isShowSkillChance() && !Config.SHOW_DEBUFF_ONLY)
		{
			if (rate > 10000)
			{
				rate = 10000;
			}
			else if (rate < 0)
			{
				rate = 0;
			}

			if (success)
			{
				((L2PcInstance) attacker).sendMessage(String.format(Message.getMessage((L2PcInstance) attacker, Message.MessageId.MSG_SKILL_CHANS_SUCCES), 100 - rate / 100));
			}
			else
			{
				((L2PcInstance) attacker).sendMessage(String.format(Message.getMessage((L2PcInstance) attacker, Message.MessageId.MSG_SKILL_CHANS), 100 - rate / 100));
			}
		}

		return success;
	}

	public static double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
	{
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		double mp = target.getMaxMp();
		if (bss)
		{
			mAtk *= 4;
		}
		else if (ss)
		{
			mAtk *= 2;
		}

		double damage = Math.sqrt(mAtk) * skill.getPower(attacker) * (mp / 97) / mDef;
		damage *= calcSkillVulnerability(target, skill);
		return damage;
	}

	public static final boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}

	public static final double calcMpRegen(L2Character cha)
	{
		if (cha == null)
			return 1;

		double init = cha.getTemplate().getBaseMpReg();
		double mpRegenMultiplier;
		double mpRegenBonus = 0;

		if (cha.isRaid())
		{
			mpRegenMultiplier = Config.RAID_MP_REGEN_MULTIPLIER;
		}
		else if (cha instanceof L2PcInstance)
		{
			mpRegenMultiplier = Config.PLAYER_MP_REGEN_MULTIPLIER;
		}
		else if (cha instanceof L2PcInstance)
		{
			mpRegenMultiplier = Config.PET_MP_REGEN_MULTIPLIER;
		}
		else
		{
			mpRegenMultiplier = Config.NPC_MP_REGEN_MULTIPLIER;
		}

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			init += 0.3 * ((player.getLevel() - 1) / 10.0);

			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				init *= calcFestivalRegenModifier(player);
			}

			if (player.isInsideZone(L2Zone.FLAG_MOTHERTREE))
			{
				mpRegenBonus += 2;
			}

			int clanHallId = player.getClan() == null ? 0 : player.getClan().getHasHideout();
			if (player.isInsideZone(L2Zone.FLAG_CLANHALL) && clanHallId > 0)
			{
				ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallId);
				if (clansHall != null)
				{
					L2Zone zone = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Clanhall, player.getX(), player.getY());
					int zoneChId = zone == null ? -1 : zone.getClanhallId();

					if (clanHallId == zoneChId && clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
					{
						mpRegenMultiplier *= 1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100;
					}
				}
			}

			int castleId = player.getClan() == null ? 0 : player.getClan().getHasCastle();
			if (player.isInsideZone(L2Zone.FLAG_CASTLE) && castleId > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(castleId);
				if (castle != null)
				{
					L2Zone zone = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Castle, player.getX(), player.getY());
					int zoneCsId = zone == null ? -1 : zone.getCastleId();

					if (castleId == zoneCsId && castle.getFunction(Castle.FUNC_RESTORE_MP) != null)
					{
						mpRegenMultiplier *= 1 + castle.getFunction(Castle.FUNC_RESTORE_MP).getLvl() / 100;
					}
				}
			}

			int fortId = player.getClan() == null ? 0 : player.getClan().getHasFort();
			if (player.isInsideZone(L2Zone.FLAG_FORT) && fortId > 0)
			{
				Fort fort = FortManager.getInstance().getFortById(fortId);
				if (fort != null)
				{
					L2Zone zone = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Fort, player.getX(), player.getY());
					int zoneFdId = zone == null ? -1 : zone.getFortId();

					if (fortId == zoneFdId && fort.getFunction(Fort.FUNC_RESTORE_MP) != null)
					{
						mpRegenMultiplier *= 1 + fort.getFunction(Fort.FUNC_RESTORE_MP).getLvl() / 100;
					}
				}
			}

			if (player.isSitting())
			{
				mpRegenMultiplier *= 1.5;
			}
			else if (!player.isMoving())
			{
				mpRegenMultiplier *= 1.1;
			}
			else if (player.isRunning())
			{
				mpRegenMultiplier *= 0.7;
			}

			init *= cha.getLevelMod() * BaseStats.MEN.calcBonus(cha);
		}
		else if (cha instanceof L2PetInstance)
		{
			init = ((L2PetInstance) cha).getPetData().getPetRegenMP();
		}

		if (init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
	}

	public static final int calcPAtkSpd(L2Character attacker, L2Character target, double atkSpd, double base)
	{
		if (attacker instanceof L2PcInstance)
		{
			base *= Config.ALT_ATTACK_DELAY;
		}

		if (atkSpd < 10)
		{
			atkSpd = 10;
		}

		return (int) (base / atkSpd);
	}


	public static final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean crit, boolean dual, boolean ss)
	{
		boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);

		damage *= calcSkillVulnerability(target, skill);
		damage += calcValakasAttribute(attacker, target, skill);

		switch (shld)
		{
			case 1:
				if (!Config.ALT_GAME_SHIELD_BLOCKS)
				{
					defence += target.getShldDef();
				}
				break;
			case 2:
				return 1.;
		}

		if (ss)
		{
			damage *= 2;
		}

		if (skill != null)
		{
			double skillpower = skill.getPower();
			if (skill.getSkillType() == L2SkillType.FATALCOUNTER)
			{
				skillpower *= 3.5 * (1 - attacker.getStatus().getCurrentHp() / attacker.getMaxHp());
			}

			float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
			{
				damage += skillpower;
			}
			else if (ssboost > 0)
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
				{
					damage += skillpower;
				}
		}

		if (Config.USE_LEVEL_PENALTY && skill != null && !skill.isItemSkill() && skill.getMagicLvl() > 40 && attacker instanceof L2Playable)
		{
			int lvl = attacker.getActingPlayer().getLevel();
			int sklvl = skill.getLevel() > 100 ? 76 : skill.getMagicLvl();
			if (lvl - sklvl < -2)
			{
				damage *= 1 / (skill.getMagicLvl() - lvl);
				crit = false;
			}

		}

		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					stat = Stats.BOW_WPN_VULN;
					break;
				case BLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case BIGBLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				case PET:
					stat = Stats.PET_WPN_VULN;
					break;
			}
		}

		if (crit)
		{

			damage *= 2 * attacker.getCriticalDmg(target, 1) * target.calcStat(Stats.CRIT_VULN, target.getTemplate().baseCritVuln, target, skill);
			damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill);
			if (weapon != null && weapon.getItemType() == L2WeaponType.BOW)
			{
				damage *= attacker.getStat().getBowCritRate();
			}
		}
		damage *= 70. / defence;

		if (attacker instanceof L2Summon && target instanceof L2PcInstance)
		{
			damage *= 0.9;
		}

		if (stat != null)
		{
			damage = target.calcStat(stat, damage, target, null);
			if (target instanceof L2Npc)
			{
				damage *= ((L2Npc) target).getTemplate().getVulnerability(stat);
			}
		}

		damage += Rnd.nextDouble() * damage / 10;

		if (shld > 0 && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0)
			{
				damage = 0;
			}
		}

		if (target instanceof L2Npc)
		{
			Race raceType = ((L2Npc) target).getTemplate().getRace();
			if (raceType != null)
			{
				switch (raceType)
				{
					case UNDEAD:
						damage *= attacker.getStat().getPAtkUndead(target);
						break;
					case BEAST:
						damage *= attacker.getStat().getPAtkMonsters(target);
						break;
					case ANIMAL:
						damage *= attacker.getStat().getPAtkAnimals(target);
						break;
					case PLANT:
						damage *= attacker.getStat().getPAtkPlants(target);
						break;
					case DRAGON:
						damage *= attacker.getStat().getPAtkDragons(target);
						break;
					case BUG:
						damage *= attacker.getStat().getPAtkInsects(target);
						break;
					case GIANT:
						damage *= attacker.getStat().getPAtkGiants(target);
						break;
					case MAGICCREATURE:
						damage *= attacker.getStat().getPAtkMagic(target);
						break;
					default:
						break;
				}
			}
		}
		if (attacker instanceof L2Npc)
		{
			Race raceType = ((L2Npc) attacker).getTemplate().getRace();
			if (raceType != null)
			{
				switch (raceType)
				{
					case UNDEAD:
						damage /= target.getStat().getPDefUndead(attacker);
						break;
					case BEAST:
						damage /= target.getStat().getPDefMonsters(attacker);
						break;
					case ANIMAL:
						damage /= target.getStat().getPDefAnimals(attacker);
						break;
					case PLANT:
						damage /= target.getStat().getPDefPlants(attacker);
						break;
					case DRAGON:
						damage /= target.getStat().getPDefDragons(attacker);
						break;
					case BUG:
						damage /= target.getStat().getPDefInsects(attacker);
						break;
					case GIANT:
						damage /= target.getStat().getPDefGiants(attacker);
						break;
					case MAGICCREATURE:
						damage /= target.getStat().getPDefMagic(attacker);
						break;
					default:
						break;
				}
			}
		}

		if (damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}

		if (isPvP)
			if (skill == null)
			{
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
				damage /= target.calcStat(Stats.PVP_PHYSICAL_DEF, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
				damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
			}

		if (Config.USE_BOW_CROSSBOW_DISTANCE_PENALTY && attacker instanceof L2PcInstance && weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
				{
					double distance = target.getRangeToTarget(attacker);
					double maxDistance = attacker.getPhysicalAttackRange();
					if (distance > maxDistance)
					{
						distance = maxDistance;
					}
					double factor = distance / maxDistance;
					double calcfactor = Config.BOW_CROSSBOW_DISTANCE_PENALTY + factor * (1 - Config.BOW_CROSSBOW_DISTANCE_PENALTY);
					damage *= calcfactor;
					break;
				}
			}
		}

		if (skill == null)
		{
			if (attacker instanceof L2PcInstance)
			{
				if (((L2PcInstance) attacker).getClassId().isMage())
				{
					damage *= 1;
				}
				else
				{
					damage *= 1;
				}
			}
			else if (attacker instanceof L2Summon)
			{
				damage *= 1;
			}
			else if (attacker instanceof L2Npc)
			{
				damage *= 1;
			}
		}
		else if (attacker instanceof L2PcInstance)
			if (target instanceof L2PcInstance)
			{
				L2Armor armor = ((L2PcInstance) target).getActiveChestArmorItem();
				if (armor != null)
				{
					if (((L2PcInstance) target).isWearingHeavyArmor())
					{
						damage *= 1;
					}
					if (((L2PcInstance) target).isWearingLightArmor())
					{
						damage *= 1;
					}
					if (((L2PcInstance) target).isWearingMagicArmor())
					{
						damage *= 1;
					}
				}
			}
			else
			{
				damage *= 1;
			}
		return damage;
	}

	public static boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
	{
		if (skill.isMagic() && skill.getSkillType() != L2SkillType.BLOW || skill.getCastRange() > 40)
			return false;

		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}

	public static byte calcShldUse(L2Character attacker, L2Character target)
	{
		return calcShldUse(attacker, target, true);
	}

	public static byte calcShldUse(L2Character attacker, L2Character target, boolean sendSysMsg)
	{
		int dex = target.getStat().getDEX();
		if (dex >= BaseStats.MAX_STAT_VALUE)
		{
			dex = BaseStats.MAX_STAT_VALUE - 1;
		}
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * BaseStats.DEX.calcBonus(target);

		if (shldRate == 0.0)
			return 0;

		L2ItemInstance shieldInst = target.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (shieldInst == null || shieldInst.getItemType() != L2WeaponType.NONE)
			return 0;

		double shldAngle = target.calcStat(Stats.SHIELD_ANGLE, 60, null, null);

		if (shldAngle < 360 && !target.isFacing(attacker, (int) shldAngle))
			return 0;

		if (attacker != null && attacker.getActiveWeaponItem() != null && attacker.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			shldRate *= 1.5;
		}

		byte shldSuccess = 0;

		if (shldRate > 0 && 100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
		{
			shldSuccess = 2;
		}
		else if (shldRate > Rnd.get(100))
		{
			shldSuccess = 1;
		}

		if (sendSysMsg && target instanceof L2PcInstance)
		{
			L2PcInstance enemy = (L2PcInstance) target;
			switch (shldSuccess)
			{
				case 1:
					enemy.sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL);
					break;
				case 2:
					enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
					break;
			}
		}
		return shldSuccess;
	}

	public static final double calcSiegeRegenModifer(L2PcInstance activeChar)
	{
		if (activeChar == null || activeChar.getClan() == null)
			return 0;

		Siege siege = SiegeManager.getSiege(activeChar);
		if (siege == null || !siege.getIsInProgress())
			return 0;

		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
		if (siegeClan == null || siegeClan.getFlag().size() == 0 || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().valueOf(siegeClan.getFlag().head().getNext()), true))
			return 0;

		return 1.5;
	}

	public static boolean calcSkillMastery(L2Character actor, L2Skill skill)
	{
		if (skill.getSkillType() == L2SkillType.FISHING)
			return false;

		if (skill.isChance())
			return false;

		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);

		if (actor instanceof L2PcInstance)
			if (((L2PcInstance) actor).isMageClass())
			{
				val *= BaseStats.INT.calcBonus(actor);
			}
			else
			{
				val *= BaseStats.STR.calcBonus(actor);
			}
		return Rnd.get(100) < val;
	}

	public static double calcSkillProficiency(L2Skill skill, L2Character attacker, L2Character target)
	{
		double multiplier = 1;
		if (skill != null)
		{
			L2SkillType type = skill.getSkillType();
			if (type == L2SkillType.PDAM || type == L2SkillType.MDAM || type == L2SkillType.DRAIN || type == L2SkillType.WEAPON_SA)
			{
				type = skill.getEffectType();
			}

			if (type != null)
			{
				switch (type)
				{
					case BLEED:
						multiplier = attacker.calcStat(Stats.BLEED_PROF, multiplier, target, null);
						break;
					case POISON:
						multiplier = attacker.calcStat(Stats.POISON_PROF, multiplier, target, null);
						break;
					case STUN:
						multiplier = attacker.calcStat(Stats.STUN_PROF, multiplier, target, null);
						break;
					case PARALYZE:
						multiplier = attacker.calcStat(Stats.PARALYZE_PROF, multiplier, target, null);
						break;
					case ROOT:
						multiplier = attacker.calcStat(Stats.ROOT_PROF, multiplier, target, null);
						break;
					case SLEEP:
						multiplier = attacker.calcStat(Stats.SLEEP_PROF, multiplier, target, null);
						break;
					case MUTE:
						multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
						break;
					case FEAR:
						multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
						break;
					case ERASE:
					case BETRAY:
					case AGGREDUCE_CHAR:
					case CONFUSION:
					case CONFUSE_MOB_ONLY:
						multiplier = attacker.calcStat(Stats.CONFUSION_PROF, multiplier, target, null);
						break;
					case DEBUFF:
						multiplier = attacker.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
						break;
					case WEAKNESS:
						multiplier = attacker.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
						break;
					default:
						break;
				}
			}
		}
		return multiplier;
	}


	public static byte calcSkillReflect(L2Character target, L2Skill skill)
	{
		if (!skill.canBeReflected())
			return SKILL_REFLECT_FAILED;

		if (!skill.isMagic() && (skill.getCastRange() == -1 || skill.getCastRange() > MELEE_ATTACK_RANGE))
			return SKILL_REFLECT_FAILED;

		byte reflect = SKILL_REFLECT_FAILED;

		switch (skill.getSkillType())
		{
			case BUFF:
			case REFLECT:
			case HEAL_PERCENT:
			case MANAHEAL_PERCENT:
			case HOT:
			case CPHOT:
			case MPHOT:
			case UNDEAD_DEFENSE:
			case AGGDEBUFF:
			case CONT:
				return SKILL_REFLECT_FAILED;
			case PDAM:
			case BLOW:
			case MDAM:
			case DEATHLINK:
			case CHARGEDAM:
				final Stats stat = skill.isMagic() ? Stats.VENGEANCE_SKILL_MAGIC_DAMAGE : Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE;
				final double venganceChance = target.getStat().calcStat(stat, 0, target, skill);
				if (venganceChance > Rnd.get(100))
				{
					reflect |= SKILL_REFLECT_VENGEANCE;
				}
				break;
		}

		final double reflectChance = target.calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, skill);

		if (Rnd.get(100) < reflectChance)
		{
			reflect |= SKILL_REFLECT_SUCCEED;
		}

		return reflect;
	}

	public static double calcSkillStatModifier(L2Skill skill, L2Character target)
	{
		BaseStats saveVs = skill.getSaveVs();
		if (saveVs == null)
		{
			switch (skill.getSkillType())
			{
				case STUN:
					saveVs = BaseStats.CON;
					break;
				case BLEED:
					saveVs = BaseStats.CON;
					break;
				case POISON:
					saveVs = BaseStats.CON;
					break;
				case SLEEP:
					saveVs = BaseStats.MEN;
					break;
				case DEBUFF:
					saveVs = BaseStats.MEN;
					break;
				case WEAKNESS:
					saveVs = BaseStats.MEN;
					break;
				case ERASE:
					saveVs = BaseStats.MEN;
					break;
				case ROOT:
					saveVs = BaseStats.MEN;
					break;
				case MUTE:
					saveVs = BaseStats.MEN;
					break;
				case FEAR:
					saveVs = BaseStats.MEN;
					break;
				case BETRAY:
					saveVs = BaseStats.MEN;
					break;
				case CONFUSION:
				case CONFUSE_MOB_ONLY:
				case AGGREDUCE_CHAR:
				case PARALYZE:
					saveVs = BaseStats.MEN;
					break;
				default:
					return 1;
			}
		}

		double multiplier = 2 - Math.sqrt(saveVs.calcBonus(target));
		if (multiplier < 0)
		{
			multiplier = 0;
		}
		return multiplier;
	}

	public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss)
	{
		if (skill.isMagic() && target.isPreventedFromReceivingBuffs())
			return false;
		if (skill.isBadBuff())
			return true;

		int value = (int) skill.getPower();
		int lvlDepend = skill.getLevelDepend();

		L2SkillType type = skill.getSkillType();
		if (type == L2SkillType.PDAM || type == L2SkillType.MDAM || type == L2SkillType.DRAIN || type == L2SkillType.WEAPON_SA)
		{
			value = skill.getEffectPower();
			type = skill.getEffectType();
		}
		if (type == null)
			if (skill.getSkillType() == L2SkillType.PDAM)
			{
				type = L2SkillType.STUN;
				type = L2SkillType.BLEED;
			}
			else if (skill.getSkillType() == L2SkillType.MDAM)
			{
				type = L2SkillType.PARALYZE;
				type = L2SkillType.DOT;
				type = L2SkillType.MDOT;
				type = L2SkillType.POISON;
			}
		if (value == 0)
		{
			value = type == L2SkillType.PARALYZE ? 50 : type == L2SkillType.FEAR ? 40 : 80;
		}

		double statmodifier = calcSkillStatModifier(skill, target);
		double resmodifier = calcSkillVulnerability(target, skill, type);

		int ssmodifier = 100;
		if (bss)
		{
			ssmodifier = 200;
		}
		else if (sps || ss)
		{
			ssmodifier = 150;
		}

		int rate = (int) (value * statmodifier);
		if (skill.isMagic())
		{
			rate += (int) (Math.pow((double) attacker.getMAtk(target, skill) / target.getMDef(attacker, skill), 0.1) * 100) - 100;
		}

		if (ssmodifier != 100)
			if (rate > 10000 / (100 + ssmodifier))
			{
				rate = 100 - (100 - rate) * 100 / ssmodifier;
			}
			else
			{
				rate = rate * ssmodifier / 100;
			}

		if (lvlDepend > 0)
		{
			double delta = 0;
			int attackerLvlmod = attacker.getLevel();
			int targetLvlmod = target.getLevel();

			if (attackerLvlmod >= 70)
			{
				attackerLvlmod = (attackerLvlmod - 69) * 2 + 70;
			}
			if (targetLvlmod >= 70)
			{
				targetLvlmod = (targetLvlmod - 69) * 2 + 70;
			}

			if (skill.getMagicLevel() == 0)
			{
				delta = attackerLvlmod - targetLvlmod;
			}
			else
			{
				delta = (skill.getMagicLevel() + attackerLvlmod) / 2 - targetLvlmod;
			}

			// double delta = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : 0)+attacker.getLevel() )/2 - target.getLevel();
			double deltamod = 1;

			if (delta + 3 < 0)
			{
				if (delta <= -20)
				{
					deltamod = 0.05;
				}
				else
				{
					deltamod = 1 - -1 * (delta / 20);
					if (deltamod >= 1)
					{
						deltamod = 0.05;
					}
				}
			}
			else
			{
				deltamod = 1 + (delta + 3) / 75; // (double) attacker.getLevel()/target.getLevel();
			}

			if (deltamod < 0)
			{
				deltamod *= -1;
			}

			rate *= deltamod;
		}
		if (rate > 99)
		{
			rate = 99;
		}
		else if (rate < 1)
		{
			rate = 1;
		}

		rate *= resmodifier * calcSkillProficiency(skill, attacker, target);

		boolean success = Rnd.get(100) <= rate;
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isShowSkillChance())
			if (success)
			{
				((L2PcInstance) attacker).sendMessage(String.format(Message.getMessage((L2PcInstance) attacker, Message.MessageId.MSG_SKILL_CHANS_SUCCES), rate));
			}
			else
			{
				((L2PcInstance) attacker).sendMessage(String.format(Message.getMessage((L2PcInstance) attacker, Message.MessageId.MSG_SKILL_CHANS), rate));
			}

		return success;
	}

	public static double calcSkillVulnerability(L2Character target, L2Skill skill)
	{
		if (skill != null)
			return calcSkillVulnerability(target, skill, skill.getSkillType());

		return calcSkillVulnerability(target, null, null);
	}

	
	public static double calcSkillVulnerability(L2Character target, L2Skill skill, L2SkillType type)
	{
		double multiplier = 1;

		if (skill != null)
		{
			switch (skill.getElement())
			{
				case L2Skill.ELEMENT_EARTH:
					multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_FIRE:
					multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WATER:
					multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WIND:
					multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_HOLY:
					multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_DARK:
					multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
					break;
			}

			if (type != null)
			{
				switch (type)
				{
					case BLEED:
						multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
						break;
					case POISON:
						multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
						break;
					case STUN:
						multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
						break;
					case PARALYZE:
						multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
						break;
					case ROOT:
						multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
						break;
					case SLEEP:
						multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
						break;
					case MUTE:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case FEAR:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case BETRAY:
					case AGGREDUCE_CHAR:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case CONFUSION:
					case CONFUSE_MOB_ONLY:
						multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
						break;
					case DEBUFF:
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
					case WEAKNESS:
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
					case CANCEL:
						multiplier = target.calcStat(Stats.CANCEL_VULN, multiplier, target, null);
						break;
				}
			}
		}
		return multiplier;
	}

	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, L2Character caster)
	{
		if (baseRestorePercent == 0 || baseRestorePercent == 100)
			return baseRestorePercent;

		double restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster);
		if (restorePercent - baseRestorePercent > 20.0)
		{
			restorePercent += 20.0;
		}

		restorePercent = Math.max(restorePercent, baseRestorePercent);
		restorePercent = Math.min(restorePercent, 90.0);

		return restorePercent;
	}

	public static boolean calculateUnlockChance(L2Skill skill)
	{
		int level = skill.getLevel();
		int chance = 0;
		switch (level)
		{
			case 1:
				chance = 30;
				break;
			case 2:
				chance = 50;
				break;
			case 3:
				chance = 75;
				break;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
				chance = 100;
				break;
		}
		return Rnd.get(120) <= chance;
	}

	public static double calcValakasAttribute(L2Character attacker, L2Character target, L2Skill skill)
	{
		double calcPower = 0;
		double calcDefen = 0;

		if (skill != null && skill.getAttributeName().contains("valakas"))
		{
			calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
			calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill);
		}
		else
		{
			calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
			if (calcPower > 0)
			{
				calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
				calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill);
			}
		}
		return calcPower - calcDefen;
	}

	public static double calcWeightRuneModifed(L2Character ch)
	{
		return 1;
	}

	public static int getRegeneratePeriod(L2Character cha)
	{
		if (cha instanceof L2DoorInstance)
			return HP_REGENERATE_PERIOD * 100;

		return HP_REGENERATE_PERIOD;
	}

	public static Calculator[] getStdDoorCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncMDefMod.getInstance());

		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncPDefMod.getInstance());

		std[Stats.MAGIC_ATTACK.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK.ordinal()].addFunc(FuncMAtkMod.getInstance());

		std[Stats.POWER_ATTACK.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK.ordinal()].addFunc(FuncPAtkMod.getInstance());

		std[Stats.CRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.CRITICAL_RATE.ordinal()].addFunc(FuncAtkCritical.getInstance());

		std[Stats.MCRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.MCRITICAL_RATE.ordinal()].addFunc(FuncMAtkCritical.getInstance());

		std[Stats.MOVEMENT.ordinal()] = new Calculator();
		std[Stats.MOVEMENT.ordinal()].addFunc(FuncMoveSpeed.getInstance());

		std[Stats.POWER_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK_SPEED.ordinal()].addFunc(FuncPAtkSpeed.getInstance());

		std[Stats.MAGIC_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()].addFunc(FuncMAtkSpeed.getInstance());

		// Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

		// Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

		// SevenSigns PDEF Modifier
		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncGatesPDefMod.getInstance());

		// SevenSigns MDEF Modifier
		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncGatesMDefMod.getInstance());

		return std;
	}

	public static Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		return std;
	}
}

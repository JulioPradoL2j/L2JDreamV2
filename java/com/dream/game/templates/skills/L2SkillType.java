package com.dream.game.templates.skills;

import java.lang.reflect.Constructor;

import com.dream.game.model.L2Skill;
import com.dream.game.skills.l2skills.L2ScriptSkill;
import com.dream.game.skills.l2skills.L2SkillChargeDmg;
import com.dream.game.skills.l2skills.L2SkillCreateItem;
import com.dream.game.skills.l2skills.L2SkillDrain;
import com.dream.game.skills.l2skills.L2SkillMount;
import com.dream.game.skills.l2skills.L2SkillSignet;
import com.dream.game.skills.l2skills.L2SkillSignetCasttime;
import com.dream.game.skills.l2skills.L2SkillSummon;
import com.dream.util.StatsSet;

public enum L2SkillType
{
	PDAM,
	MDAM,
	CPDAM,
	AGGDAMAGE,
	DOT,
	HOT,
	MHOT,
	BLEED,
	POISON,
	CPHOT,
	MPHOT,
	BUFF,
	DEBUFF,
	STUN,
	ROOT,
	CONT,
	FUSION,
	CONFUSION,
	PARALYZE,
	FEAR,
	SLEEP,
	DEATH_MARK,
	HEAL,
	HEAL_MOB,
	COMBATPOINTHEAL,
	MANAHEAL,
	MANAHEAL_PERCENT,
	MANARECHARGE,
	RESURRECT,
	PASSIVE,
	UNLOCK,
	GIVE_SP,
	NEGATE,
	CANCEL,
	CANCEL_DEBUFF,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	CONFUSE_MOB_ONLY,
	DEATHLINK,
	BLOW,
	FATALCOUNTER,
	DETECT_WEAKNESS,
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	ENCHANT_ATTRIBUTE,
	FEED_PET,
	HEAL_PERCENT,
	HEAL_STATIC,
	LUCK,
	MANADAM,
	MAKE_KILLABLE,
	MDOT,
	MUTE,
	RECALL,
	REFLECT,
	SUMMON_FRIEND,
	SOULSHOT,
	SPIRITSHOT,
	SPOIL,
	SWEEP,
	WEAKNESS,
	STEAL_BUFF,
	BAD_BUFF,
	DEATHLINK_PET,
	MANA_BY_LEVEL,
	FAKE_DEATH,
	SIEGEFLAG,
	TAKECASTLE,
	TAKEFORT,
	UNDEAD_DEFENSE,
	BEAST_FEED,
	DRAIN_SOUL,
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	WEAPON_SA,
	DELUXE_KEY_UNLOCK,
	SOW,
	HARVEST,
	CHARGESOUL,
	GET_PLAYER,
	FISHING,
	PUMPING,
	REELING,
	CANCEL_TARGET,
	AGGDEBUFF,
	COMBATPOINTPERCENTHEAL,
	SUMMONCP,
	SUMMON_TREASURE_KEY,
	SUMMON_CURSED_BONES,
	EXTRACTABLE,
	EXTRACTABLE_COMBO,
	ERASE,
	MAGE_BANE,
	WARRIOR_BANE,
	STRSIEGEASSAULT,
	RAID_DESCRIPTION,
	UNSUMMON_ENEMY_PET,
	BETRAY,
	BALANCE_LIFE,
	SERVER_SIDE,
	REMOVE_TRAP,
	SHIFT_TARGET,
	INSTANT_JUMP,
	GARDEN_KEY_UNLOCK,
	CLAN_GATE,
	ZAKEN_MOVE,
	MOUNT(L2SkillMount.class),
	CHARGEDAM(L2SkillChargeDmg.class),
	CREATE_ITEM(L2SkillCreateItem.class),
	DRAIN(L2SkillDrain.class),
	LUCKNOBLESSE(L2SkillCreateItem.class),
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	SUMMON(L2SkillSummon.class),
	DUMMY,
	SCRIPT(L2ScriptSkill.class),
	COREDONE,
	CUSTOM,
	GIVE_CLANREP,
	NOTDONE;

	private final Constructor<? extends L2Skill> _constructor;

	private L2SkillType()
	{
		this(L2Skill.class);
	}

	private L2SkillType(Class<? extends L2Skill> clazz)
	{
		try
		{
			_constructor = clazz.getConstructor(StatsSet.class);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public L2Skill makeSkill(StatsSet set) throws Exception
	{
		return _constructor.newInstance(set);
	}
}
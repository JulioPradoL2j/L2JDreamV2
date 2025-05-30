package com.dream.game.model;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.manager.CoupleManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ArtefactInstance;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2CubicInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.entity.Couple;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.skills.BaseStats;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.skills.conditions.Condition;
import com.dream.game.skills.effects.EffectTemplate;
import com.dream.game.skills.funcs.Func;
import com.dream.game.skills.funcs.FuncOwner;
import com.dream.game.skills.funcs.FuncTemplate;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;
import com.dream.lang.L2Integer;
import com.dream.util.LinkedBunch;
import com.dream.util.StatsSet;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class L2Skill implements FuncOwner
{
	public static enum SkillOpType
	{
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE,
		OP_CHANCE,
		OP_BALANCE
	}

	public static enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PET,
		TARGET_SUMMON,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_AREA,
		TARGET_FRONT_AREA,
		TARGET_BEHIND_AREA,
		TARGET_AURA,
		TARGET_FRONT_AURA,
		TARGET_BEHIND_AURA,
		TARGET_CORPSE,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_AREA_CORPSES,
		TARGET_MULTIFACE,
		TARGET_AREA_UNDEAD,
		TARGET_ITEM,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_FLAGPOLE,
		TARGET_PARTY_MEMBER,
		TARGET_PARTY_OTHER,
		TARGET_ENEMY_SUMMON,
		TARGET_OWNER_PET,
		TARGET_ENEMY_ALLY,
		TARGET_ENEMY_PET,
		TARGET_GATE,
		TARGET_COUPLE,
		TARGET_MOB,
		TARGET_AREA_MOB,
		TARGET_KNOWNLIST,
		TARGET_GROUND,
		TARGET_PIG
	}

	protected static Logger _log = Logger.getLogger(L2Skill.class.getName());

	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;

	public static final int SKILL_CLAN_LUCK = 390;
	public static final int SKILL_SOUL_MASTERY = 467;
	public static final int SKILL_FAKE_INT = 9001;
	public static final int SKILL_FAKE_WIT = 9002;
	public static final int SKILL_FAKE_MEN = 9003;
	public static final int SKILL_FAKE_CON = 9004;

	public static final int SKILL_FAKE_DEX = 9005;

	public static final int SKILL_FAKE_STR = 9006;

	public final static int ELEMENT_FIRE = 0;
	public final static int ELEMENT_WATER = 1;
	public final static int ELEMENT_WIND = 2;
	public final static int ELEMENT_EARTH = 3;
	public final static int ELEMENT_HOLY = 4;
	public final static int ELEMENT_DARK = 5;

	public final static int COND_RUNNING = 0x0001;
	public final static int COND_WALKING = 0x0002;
	public final static int COND_SIT = 0x0004;
	public final static int COND_BEHIND = 0x0008;
	public final static int COND_CRIT = 0x0010;
	public final static int COND_LOWHP = 0x0020;
	public final static int COND_ROBES = 0x0040;
	public final static int COND_CHARGES = 0x0080;
	public final static int COND_SHIELD = 0x0100;
	public final static int COND_GRADEA = 0x010000;
	public final static int COND_GRADEB = 0x020000;
	public final static int COND_GRADEC = 0x040000;
	public final static int COND_GRADED = 0x080000;
	public final static int COND_GRADES = 0x100000;

	public final static boolean skillLevelExists(int skillId, int level)
	{
		return SkillTable.getInstance().getInfo(skillId, level) != null;
	}

	private final Integer _id;

	private final int _level;

	private final int _displayId;

	private final int _refId;
	private final String _name;

	private final String _attribute;
	private final SkillOpType _operateType;
	private final boolean _magic;
	private boolean _itemSkill;
	private final boolean _physic;
	private final boolean _staticReuse;
	private final boolean _staticHitTime;
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;

	private final int _cpConsume;
	private final int _itemConsume;
	private final int _itemConsumeId;
	private final int _itemConsumeOT;
	private final int _itemConsumeIdOT;

	private final int _itemConsumeSteps;
	private final int _targetConsume;

	private final int _targetConsumeId;
	private final int _summonTotalLifeTime;
	private final int _summonTimeLostIdle;
	private final int _summonTimeLostActive;
	private final int _itemConsumeTime;
	private final boolean _isCubic;
	private final int _cubicSkillsLevel;

	private final boolean _useAlways;
	private final int _feed;
	private int _effectLevel;
	private final int _activationtime;

	private final int _activationchance;
	private final int _castRange;

	private final int _effectRange;

	private final int _abnormalLvl;
	private final int _negateLvl;
	private final int _negateId;
	private final String[] _negateStats;

	private final int _maxNegatedEffects;
	private final int _cancelId;
	private final int _hitTime;
	private final int _skillInterruptTime;
	private final int _coolTime;
	private final int _reuseDelay;

	private final int _equipDelay;
	private final SkillTargetType _targetType;
	private final double _power;

	private final int _levelDepend;

	private final boolean _killByDOT;

	private final int _skillRadius;
	private final L2SkillType _skillType;
	private final L2SkillType _effectType;
	private final int _effectAbnormalLvl;
	private final int _effectPower;
	private final int _effectId;
	private final float _effectLvl;

	private final int _skill_landing_percent;

	private final boolean _ispotion;
	private final int _element;

	private final int _elementPower;
	private final boolean _isSuicideAttack;
	private final int _activateRate;
	private final int _magicLevel;
	private final BaseStats _saveVs;
	private final int _condition;
	private final boolean _overhit;
	private final boolean _ignoreShld;
	private final int _weaponsAllowed;

	private final int _armorsAllowed;
	private final int _addCrossLearn;
	private final float _mulCrossLearn;
	private final float _mulCrossLearnRace;
	private final float _mulCrossLearnProf;
	private final List<ClassId> _canLearn;
	private final List<Integer> _teachers;
	private final boolean _isOffensive;

	private final boolean _isNeutral;
	private final int _needCharges;
	private final int _giveCharges;
	private final int _maxCharges;
	private final boolean _consumeCharges;

	private final boolean _continueAfterMax;
	private final int _triggeredId;
	private final int _triggeredLevel;

	private final int _triggeredCount;
	private final boolean _bestow;

	private final boolean _bestowed;
	private final int _soulConsume;
	private final int _soulMaxConsume;
	private final int _numSouls;
	private final int _expNeeded;

	private final int _critChance;

	private int _duration;
	private final int _baseCritRate;
	private final int _lethalEffect1;
	private final int _lethalEffect2;
	private final boolean _directHpDmg;
	private final boolean _isDance;
	private final boolean _isSong;
	private final int _nextDanceCost;

	private final float _sSBoost;

	private final int _timeMulti;

	private final boolean _isAdvanced;

	private final int _minPledgeClass;

	private final int _aggroPoints;
	protected Condition _preCondition;
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;

	protected EffectTemplate[] _effectTemplatesSelf;
	protected L2Skill[] _skillsOnCast;
	protected int[] _skillsOnCastId, _skillsOnCastLvl;

	protected int timesTriggered = 1;

	protected ChanceCondition _chanceCondition;
	private final String _flyType;
	private final int _flyRadius;

	private final float _flyCourse;
	private final boolean _nextActionIsAttack;
	private final boolean _isDebuff;

	private final boolean _canBeReflected;

	private final int _lethalType;

	private String _weaponDependancyMessage;

	private final boolean _chMagic;

	private final boolean _heroMagic;

	private final boolean _bufferMagic;

	private final boolean _is5MinMagic;

	public L2Skill(StatsSet set)
	{
		_id = L2Integer.valueOf(set.getInteger("skill_id"));
		_level = set.getInteger("level");

		_attribute = set.getString("attribute", "");

		_displayId = set.getInteger("displayId", _id);
		_name = set.getString("name").intern();
		if (set.getSet().containsKey("effectLevel"))
		{
			_effectLevel = set.getInteger("effectLevel");
		}
		else
		{
			_effectLevel = 0;
		}
		_skillType = set.getEnum("skillType", L2SkillType.class);
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_targetType = set.getEnum("target", SkillTargetType.class);
		_magic = set.getBool("isMagic", isSkillTypeMagic());
		_itemSkill = set.getBool("isItem", false);
		_physic = set.getBool("isPhysic", false);
		_ispotion = set.getBool("isPotion", false);
		_staticReuse = set.getBool("staticReuse", false);
		_staticHitTime = set.getBool("staticHitTime", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_cpConsume = set.getInteger("cpConsume", 0);
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		_targetConsume = set.getInteger("targetConsumeCount", 0);
		_targetConsumeId = set.getInteger("targetConsumeId", 0);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		_isCubic = set.getBool("isCubic", false);
		_cubicSkillsLevel = set.getInteger("cubicSkillsLevel", 0);
		_activationtime = set.getInteger("activationtime", 8);
		_activationchance = set.getInteger("activationchance", 30);

		_refId = set.getInteger("referenceId", _itemConsumeId);

		_castRange = set.getInteger("castRange", 0);
		_effectRange = set.getInteger("effectRange", -1);

		_abnormalLvl = set.getInteger("abnormalLvl", -1);
		_effectAbnormalLvl = set.getInteger("effectAbnormalLvl", -1);
		_negateLvl = set.getInteger("negateLvl", -1);
		String[] negateStats = set.getString("negateStats", "").split(" ");
		if (negateStats.length > 0)
		{
			List<String> stats = new ArrayList<>();
			for (String stat : negateStats)
			{
				stats.add(stat.toLowerCase().intern());
			}
			_negateStats = stats.toArray(new String[stats.size()]);
		}
		else
		{
			_negateStats = negateStats;
		}
		_negateId = set.getInteger("negateId", 0);
		_maxNegatedEffects = set.getInteger("maxNegated", 0);

		_cancelId = set.getInteger("cancelId", 0);

		_killByDOT = set.getBool("killByDOT", false);

		_hitTime = set.getInteger("hitTime", 0);
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = isMagic() ? getHitTime() / 2 : 0;
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_equipDelay = set.getInteger("equipDelay", 0);

		_isDance = set.getBool("isDance", false);
		_isSong = set.getBool("isSong", false);

		_chMagic = set.getBool("isChMagic", false);
		_heroMagic = set.getBool("isHeroMagic", false);
		_bufferMagic = set.getBool("isBufferMagic", false);
		_is5MinMagic = set.getBool("is5MinMagic", false);
		_useAlways = set.getBool("useAlways", false);


		if (_isDance)
		{
			_timeMulti = Config.ALT_DANCE_TIME;
		}
		else if (_isSong)
		{
			_timeMulti = Config.ALT_SONG_TIME;
		}
		else if (_bufferMagic)
		{
			_timeMulti = Config.ALT_BUFFER_TIME;
		}
		else if (_chMagic)
		{
			_timeMulti = Config.ALT_CH_TIME;
		}
		else if (_heroMagic)
		{
			_timeMulti = Config.ALT_HERO_TIME;
		}
		else if (_is5MinMagic)
		{
			_timeMulti = Config.ALT_5MIN_TIME;
		}
		else
		{
			_timeMulti = 1;
		}

		_skillRadius = set.getInteger("skillRadius", 80);

		_power = _id != 2005 ? set.getFloat("power", 0.f) : Config.MANAHEAL_POWER;

		_levelDepend = set.getInteger("lvlDepend", 0);

		_isAdvanced = set.getBool("isAdvanced", false);
		_isDebuff = set.getBool("isDebuff", false);
		_feed = set.getInteger("feed", 0);

		_effectType = set.getEnum("effectType", L2SkillType.class, null);
		_effectPower = set.getInteger("effectPower", 0);
		_effectId = set.getInteger("effectId", 0);
		_effectLvl = set.getFloat("effectLevel", 0.f);
		_skill_landing_percent = set.getInteger("skill_landing_percent", 0);

		_element = set.getInteger("element", -1);
		_elementPower = set.getInteger("elementPower", 20);

		_activateRate = set.getInteger("activateRate", -1);
		_magicLevel = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
		_saveVs = set.getEnum("saveVs", BaseStats.class, null);
		_ignoreShld = set.getBool("ignoreShld", false);
		_condition = set.getInteger("condition", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_armorsAllowed = set.getInteger("armorsAllowed", 0);

		_addCrossLearn = set.getInteger("addCrossLearn", 1000);
		_mulCrossLearn = set.getFloat("mulCrossLearn", 2.f);
		_mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.f);
		_mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.f);
		_isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		_isNeutral = set.getBool("neutral", false);

		_needCharges = set.getInteger("needCharges", 0);
		_giveCharges = set.getInteger("giveCharges", 0);
		_maxCharges = set.getInteger("maxCharges", 0);
		_consumeCharges = set.getBool("consumeCharges", true);
		_continueAfterMax = set.getBool("continueAfterMax", false);

		_minPledgeClass = set.getInteger("minPledgeClass", 0);

		_triggeredId = set.getInteger("triggeredId", 0);
		_triggeredLevel = set.getInteger("triggeredLevel", 1);
		int triggeredCount = set.getInteger("triggeredCount", 1);

		if (_triggeredId == 0)
		{
			_triggeredCount = 0;
		}
		else if (triggeredCount == 0)
		{
			_triggeredCount = 1;
		}
		else
		{
			_triggeredCount = triggeredCount;
		}

		if (_operateType == SkillOpType.OP_CHANCE)
		{
			_chanceCondition = ChanceCondition.parse(set);
		}

		_bestow = set.getBool("bestowTriggered", false);
		_bestowed = set.getBool("bestowed", false);

		_numSouls = set.getInteger("num_souls", 0);
		_soulConsume = set.getInteger("soulConsumeCount", 0);
		_soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
		_expNeeded = set.getInteger("expNeeded", 0);
		_critChance = set.getInteger("critChance", 0);

		_duration = set.getInteger("duration", 0);
		_baseCritRate = set.getInteger("baseCritRate", _skillType == L2SkillType.PDAM || _skillType == L2SkillType.BLOW ? 0 : -1);
		_lethalEffect1 = set.getInteger("lethal1", 0);
		_lethalEffect2 = set.getInteger("lethal2", 0);
		_lethalType = set.getInteger("LethalType", 1);
		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_nextDanceCost = set.getInteger("nextDanceCost", 0);
		_canBeReflected = set.getBool("canBeReflected", true);
		_sSBoost = set.getFloat("SSBoost", 1.f);

		_aggroPoints = set.getInteger("aggroPoints", 0);
		_nextActionIsAttack = set.getBool("nextActionAttack", false);

		_flyType = set.getString("flyType", null);
		_flyRadius = set.getInteger("flyRadius", 200);
		_flyCourse = set.getFloat("flyCourse", 0);

		String canLearn = set.getString("canLearn", null);
		if (canLearn == null)
		{
			_canLearn = null;
		}
		else
		{
			_canLearn = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				String cls = st.nextToken();
				try
				{
					_canLearn.add(ClassId.valueOf(cls));
				}
				catch (Exception e)
				{
					_log.fatal("Bad class " + cls + " to learn skill", e);
				}
			}
		}

		String teachers = set.getString("teachers", null);
		if (teachers == null)
		{
			_teachers = null;
		}
		else
		{
			_teachers = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				String npcid = st.nextToken();
				try
				{
					_teachers.add(Integer.parseInt(npcid));
				}
				catch (Exception e)
				{
					_log.fatal("Bad teacher id " + npcid + " to teach skill", e);
				}
			}
		}
	}

	public final void attach(Condition c)
	{
		Condition old = _preCondition;
		if (old != null)
		{
			_log.fatal("Replaced " + old + " condition with " + c + " condition at skill: " + this);
		}
		_preCondition = c;
	}

	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}

	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplatesSelf.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}

	public boolean bestowed()
	{
		return _bestowed;
	}

	public boolean bestowTriggered()
	{
		return _bestow;
	}

	public boolean canBeReflected()
	{
		return _canBeReflected;
	}

	public final boolean canTeachBy(int npcId)
	{
		return _teachers == null || _teachers.contains(npcId);
	}

	public boolean checkCondition(L2Character activeChar, L2Object target)
	{
		Condition preCondition = _preCondition;
		if (preCondition == null)
			return true;

		Env env = new Env();
		env.player = activeChar;
		if (target instanceof L2Character)
		{
			env.target = (L2Character) target;
		}
		env.skill = this;

		if (preCondition.test(env))
			return true;

		if (activeChar instanceof L2PcInstance)
		{
			preCondition.sendMessage((L2PcInstance) activeChar, this);
		}
		return false;
	}

	public String generateUniqueStackType()
	{
		int count = _effectTemplates == null ? 0 : _effectTemplates.length;
		count += _effectTemplatesSelf == null ? 0 : _effectTemplatesSelf.length;
		return _id + "-" + count;
	}

	public final int getAbnormalLvl()
	{
		return _abnormalLvl;
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public final int getActivationChance()
	{
		return _activationchance;
	}

	public final int getActivationTime()
	{
		return _activationtime;
	}

	public final int getAggroPoints()
	{
		return _aggroPoints;
	}

	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}

	public String getAttributeName()
	{
		return _attribute;
	}

	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}

	public final int getCancelId()
	{
		return _cancelId;
	}

	public final boolean getCanLearn(ClassId cls)
	{
		return _canLearn == null || _canLearn.contains(cls);
	}

	public final int getCastRange()
	{
		return _castRange;
	}

	public ChanceCondition getChanceCondition()
	{
		return _chanceCondition;
	}

	public final int getCondition()
	{
		return _condition;
	}

	public boolean getConsumeCharges()
	{
		return _consumeCharges;
	}

	public boolean getContinueAfterMax()
	{
		return _continueAfterMax;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public final int getCpConsume()
	{
		return _cpConsume;
	}

	public final int getCritChance()
	{
		return _critChance;
	}

	public final int getCrossLearnAdd()
	{
		return _addCrossLearn;
	}

	public final float getCrossLearnMul()
	{
		return _mulCrossLearn;
	}

	public final float getCrossLearnProf()
	{
		return _mulCrossLearnProf;
	}

	public final float getCrossLearnRace()
	{
		return _mulCrossLearnRace;
	}

	public final int getCubicSkillLevel()
	{
		return _cubicSkillsLevel;
	}

	public int getDisplayId()
	{
		return _displayId;
	}

	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}

	public final int getDuration()
	{
		return _duration;
	}

	public final int getEffectAbnormalLvl()
	{
		return _effectAbnormalLvl;
	}

	public final int getEffectId()
	{
		return _effectId;
	}

	public final int getEffectLevel()
	{
		return _effectLevel;
	}

	public final float getEffectLvl()
	{
		return _effectLvl;
	}

	public final int getEffectPower()
	{
		return _effectPower;
	}

	public final int getEffectRange()
	{
		return _effectRange;
	}

	public final L2Effect[] getEffects(L2Character effector, L2Character effected)
	{
		return getEffects(effector, effected, effector);
	}

	public synchronized final L2Effect[] getEffects(L2Character effector, L2Character effected, IEffector effectorObj)
	{

		if (isPassive())
			return L2Effect.EMPTY_ARRAY;

		if (_effectTemplates == null)
			return L2Effect.EMPTY_ARRAY;

		if (effected instanceof L2DoorInstance || effected instanceof L2SiegeFlagInstance)
			return L2Effect.EMPTY_ARRAY;

		if (effector != effected)
			if (effected.isInvul())
				return L2Effect.EMPTY_ARRAY;


		LinkedBunch<L2Effect> effects = new LinkedBunch<>();

		boolean skillMastery = false;

		if (!isToggle() && effector.getActingPlayer() != null && Formulas.calcSkillMastery(effector, this))
		{
			skillMastery = true;
		}

		if (getSkillType() == L2SkillType.BUFF)
		{
			for (L2Effect ef : effector.getAllEffects())
			{
				if (ef.getSkill().getId() == getId() && ef.getSkill().getLevel() > getLevel())
				{
					return L2Effect.EMPTY_ARRAY;
				}
			}
		}

		for (EffectTemplate et : _effectTemplates)
		{
			if (!effected.isAllow(et, this))
				return L2Effect.EMPTY_ARRAY;
			Env env = new Env();
			env.player = effector;
			env.target = effected;
			env.object = effectorObj;
			env.skill = this;
			env.skillMastery = skillMastery;
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				effects.add(e);
			}
		}
		if (effects.size() == 0)
			return L2Effect.EMPTY_ARRAY;
		return effects.moveToArray(new L2Effect[effects.size()]);
	}

	@SuppressWarnings("unlikely-arg-type")
	public final void getEffects(L2CubicInstance effector, L2Character effected)
	{
		if (isPassive())
			return;
		if (_effectTemplates == null)
			return;
		if (effected instanceof L2DoorInstance || effected instanceof L2SiegeFlagInstance)
			return;
		if (!effector.equals(effected) && effected.isInvul())
			return;
		if ((isDebuff() || isOffensive()) && effector.getOwner() == effected.getActingPlayer())
			return;
		for (EffectTemplate et : _effectTemplates)
		{
			Env env = new Env();
			env.player = effector.getOwner();
			env.cubic = effector;
			env.target = effected;
			env.skill = this;
			et.getEffect(env);
		}
	}

	public final void getEffectsSelf(L2Character effector)
	{
		if (isPassive())
			return;

		if (_effectTemplatesSelf == null)
			return;

		for (EffectTemplate et : _effectTemplatesSelf)
		{
			Env env = new Env();
			env.player = effector;
			env.target = effector;
			env.skill = this;
			et.getEffect(env);
		}
	}

	public EffectTemplate[] getEffectTempate()
	{
		return _effectTemplates;
	}

	public final L2SkillType getEffectType()
	{
		return _effectType;
	}

	public final int getElement()
	{
		return _element;
	}

	public int getElementPower()
	{
		return _elementPower;
	}

	public final int getEquipDelay()
	{
		return _equipDelay;
	}

	public final int getExpNeeded()
	{
		return _expNeeded;
	}

	public final int getFeed()
	{
		return _feed;
	}

	public final L2Character getFirstOfTargetList(L2Character activeChar)
	{
		L2Character[] targets;

		targets = getTargetList(activeChar, true);
		if (targets == null || targets.length == 0)
			return null;
		return targets[0];
	}

	public final float getFlyCourse()
	{
		return _flyCourse;
	}

	public final int getFlyRadius()
	{
		return _flyRadius;
	}

	public final String getFlyType()
	{
		return _flyType;
	}

	@Override
	public String getFuncOwnerName()
	{
		return getName();
	}

	@Override
	public final L2Skill getFuncOwnerSkill()
	{
		return this;
	}

	public final int getGiveCharges()
	{
		return _giveCharges;
	}

	public final int getHitTime()
	{
		return _hitTime;
	}

	public final int getHpConsume()
	{
		return _hpConsume;
	}

	public final Integer getId()
	{
		return _id;
	}

	public final int getItemConsume()
	{
		return _itemConsume;
	}

	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}

	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}

	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}

	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}

	public final int getLandingPercent()
	{
		return _skill_landing_percent;
	}

	public final int getLethalChance1()
	{
		return _lethalEffect1;
	}

	public final int getLethalChance2()
	{
		return _lethalEffect2;
	}

	public int getLethalType()
	{
		return _lethalType;
	}

	public final int getLevel()
	{
		return _level;
	}

	public final int getLevelDepend()
	{
		return _levelDepend;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public final int getMagicLvl()
	{
		return _magicLevel;
	}

	public final int getMaxCharges()
	{
		return _maxCharges;
	}

	public final int getMaxNegatedEffects()
	{
		return _maxNegatedEffects;
	}

	public final int getMaxSoulConsumeCount()
	{
		return _soulMaxConsume;
	}

	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}

	public final int getMpConsume()
	{
		return _mpConsume;
	}

	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}

	public final L2Character[] getMultiFaceTargetList(L2Character activeChar)
	{
		LinkedBunch<L2Character> targetList = new LinkedBunch<>();
		L2Object target;
		L2Object FirstTarget;
		L2PcInstance tgOwner;
		L2Clan acClan;
		L2Clan tgClan;
		L2Party acPt = activeChar.getParty();
		int radius = getSkillRadius();

		if (getCastRange() <= 0)
		{
			target = activeChar;
		}
		else
		{
			target = activeChar.getTarget();
		}
		FirstTarget = target;

		if (target == null || !(target instanceof L2Character))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		int newHeading = getNewHeadingToTarget(activeChar, (L2Character) target);

		if (target.getObjectId() != activeChar.getObjectId())
			if (!((L2Character) target).isAlikeDead())
			{
				targetList.add((L2Character) target);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return null;
			}

		if (!(activeChar instanceof L2Playable))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2PcInstance)
					if (((L2PcInstance) obj).inObserverMode())
					{
						continue;
					}
				if (obj instanceof L2Playable)
					if (!Util.checkIfInRange(radius, target, obj, true))
					{
						continue;
					}
					else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) target))
					{
						continue;
					}
					else if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
			}
			if (targetList.size() == 0)
				return null;
			return targetList.moveToArray(new L2Character[targetList.size()]);
		}

		if (activeChar.getActingPlayer() != null)
		{
			acClan = activeChar.getActingPlayer().getClan();
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		if (activeChar.isInsideZone(L2Zone.FLAG_SIEGE))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
				{
					continue;
				}
				if (!Util.checkIfInRange(radius, target, obj, true))
				{
					continue;
				}
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
				{
					continue;
				}

				if (obj instanceof L2PcInstance)
				{
					if (((L2PcInstance) obj).inObserverMode())
					{
						continue;
					}
					tgClan = ((L2PcInstance) obj).getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
						{
							continue;
						}
						else if (!((L2Character) obj).isAlikeDead())
						{
							targetList.add((L2Character) obj);
						}
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
						{
							continue;
						}
						else if (tgClan.getAllyId() == acClan.getAllyId())
						{
							continue;
						}
						else if (!((L2Character) obj).isAlikeDead())
						{
							targetList.add((L2Character) obj);
						}
					}
					else if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();
					tgClan = tgOwner.getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
						{
							continue;
						}
						else if (!((L2Character) obj).isAlikeDead())
						{
							targetList.add((L2Character) obj);
						}
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
						{
							continue;
						}
						else if (tgClan.getAllyId() == acClan.getAllyId())
						{
							continue;
						}
						else if (!((L2Character) obj).isAlikeDead())
						{
							targetList.add((L2Character) obj);
						}
					}
					else if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
				}
				else
				{
					continue;
				}
			}
		}
		else if (activeChar.isInsideZone(L2Zone.FLAG_STADIUM) || activeChar.isInsideZone(L2Zone.FLAG_PVP))
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
				{
					continue;
				}
				if (!Util.checkIfInRange(radius, target, obj, true))
				{
					continue;
				}
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
				{
					continue;
				}

				if (obj instanceof L2PcInstance)
				{
					if (((L2PcInstance) obj).inObserverMode())
					{
						continue;
					}
					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
						{
							continue;
						}
						else if (!((L2Character) obj).isAlikeDead())
						{
							targetList.add((L2Character) obj);
						}
					}
					else if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
						{
							continue;
						}
						else if (!((L2Character) obj).isAlikeDead())
						{
							targetList.add((L2Character) obj);
						}
					}
					else if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
				}
				else
				{
					continue;
				}
			}
		}
		else
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Character))
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
					if (((L2PcInstance) obj).inObserverMode())
					{
						continue;
					}
				if (!Util.checkIfInRange(radius, target, obj, true))
				{
					continue;
				}
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
				{
					continue;
				}

				if (obj instanceof L2MonsterInstance)
					if (!((L2Character) obj).isAlikeDead())
					{
						targetList.add((L2Character) obj);
					}
			}
		}

		if (targetList.size() == 0)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		return targetList.moveToArray(new L2Character[targetList.size()]);
	}

	public final String getName()
	{
		return _name;
	}

	public final int getNeededCharges()
	{
		return _needCharges;
	}

	public final int getNegateId()
	{
		return _negateId;
	}

	public final int getNegateLvl()
	{
		return _negateLvl;
	}

	public final String[] getNegateStats()
	{
		return _negateStats;
	}

	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}

	public final int getNumSouls()
	{
		return _numSouls;
	}

	public final double getPower()
	{
		return _power;
	}

	public final double getPower(L2Character activeChar)
	{
		return _power;
	}

	public int getReferenceItemId()
	{
		return _refId;
	}

	public final int getReuseDelay()
	{
		return _reuseDelay;
	}

	public final BaseStats getSaveVs()
	{
		return _saveVs;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final int getSkillRadius()
	{
		return _skillRadius;
	}

	public final L2SkillType getSkillType()
	{
		return _skillType;
	}

	public final int getSoulConsumeCount()
	{
		return _soulConsume;
	}

	public final float getSSBoost()
	{
		return _sSBoost;
	}

	public final Func[] getStatFuncs(L2Effect effect, L2Character player)
	{
		if (!(player instanceof L2PcInstance) && !(player instanceof L2Attackable) && !(player instanceof L2Summon))
			return Func.EMPTY_ARRAY;

		if (_funcTemplates == null)
			return Func.EMPTY_ARRAY;

		LinkedBunch<Func> funcs = new LinkedBunch<>();
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.skill = this;
			Func f = t.getFunc(env, this);
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.size() == 0)
			return Func.EMPTY_ARRAY;
		return funcs.moveToArray(new Func[funcs.size()]);
	}

	public final int getTargetConsume()
	{
		return _targetConsume;
	}

	public final int getTargetConsumeId()
	{
		return _targetConsumeId;
	}

	public final L2Character[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}

	public final L2Character[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		L2Character target = null;

		L2Object objTarget = activeChar.getTarget();
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}

		return getTargetList(activeChar, onlyFirst, target);
	}


	public final L2Character[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		LinkedBunch<L2Character> targetList = new LinkedBunch<>();

		try
		{
			SkillTargetType targetType = getTargetType();
			L2SkillType skillType = getSkillType();

			switch (targetType)
			{
				case TARGET_ONE:
				{
					if (isPositive() && target == null)
					{
						target = activeChar;
					}

					boolean canTargetSelf = false;
					switch (skillType)
					{
						case BUFF:
						case HEAL:
						case HOT:
						case HEAL_PERCENT:
						case MANARECHARGE:
						case MANAHEAL:
						case NEGATE:
						case CANCEL:
						case CANCEL_DEBUFF:
						case REFLECT:
						case COMBATPOINTHEAL:
						case COMBATPOINTPERCENTHEAL:
						case MAGE_BANE:
						case WARRIOR_BANE:
						case BETRAY:
						case BALANCE_LIFE:
							canTargetSelf = true;
							break;
					}

					if (target == null || target.isDead() || target == activeChar && !canTargetSelf)
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}
					if (target != activeChar && !GeoData.getInstance().canSeeTarget(activeChar, target))
						return null;
					return new L2Character[]
					{
						target
					};
				}
				case TARGET_SELF:
				case TARGET_GROUND:
				{
					return new L2Character[]
					{
						activeChar
					};
				}
				case TARGET_HOLY:
				{
					if (activeChar instanceof L2PcInstance)
						if (target instanceof L2ArtefactInstance)
							return new L2Character[]
							{
								target
							};

					return null;
				}
				case TARGET_FLAGPOLE:
				{
					return new L2Character[]
					{
						activeChar
					};
				}
				case TARGET_COUPLE:
				{
					if (target != null && target instanceof L2PcInstance)
					{
						int _chaid = activeChar.getObjectId();
						int targetId = target.getObjectId();
						for (Couple cl : CoupleManager.getInstance().getCouples())
							if (cl.getPlayer1Id() == _chaid && cl.getPlayer2Id() == targetId || cl.getPlayer2Id() == _chaid && cl.getPlayer1Id() == targetId)
								return new L2Character[]
								{
									target
								};
					}
					return null;
				}
				case TARGET_PET:
				{
					target = activeChar.getPet();
					if (target != null && !target.isDead())
						return new L2Character[]
						{
							target
						};
					return null;
				}
				case TARGET_SUMMON:
				{
					target = activeChar.getPet();
					if (target != null && !target.isDead() && target instanceof L2SummonInstance)
						return new L2Character[]
						{
							target
						};
					return null;
				}
				case TARGET_OWNER_PET:
				{
					if (activeChar instanceof L2Summon)
					{
						target = ((L2Summon) activeChar).getOwner();
						if (target != null && !target.isDead())
							return new L2Character[]
							{
								target
							};
					}
					return null;
				}
				case TARGET_ENEMY_PET:
				{
					if (target != null && target instanceof L2Summon)
					{
						L2Summon targetPet = null;
						targetPet = (L2Summon) target;
						if (activeChar instanceof L2PcInstance && activeChar.getPet() != targetPet && !targetPet.isDead() && targetPet.getOwner().getPvpFlag() != 0)
							return new L2Character[]
							{
								target
							};
					}
					return null;
				}
				case TARGET_CORPSE_PET:
				{
					if (activeChar instanceof L2PcInstance)
					{
						target = activeChar.getPet();
						if (target != null && target.isDead())
							return new L2Character[]
							{
								target
							};
					}
					return null;
				}
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				{
					int radius = getSkillRadius();
					boolean srcInPvP = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					L2PcInstance src = activeChar.getActingPlayer();

					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						boolean checkPeace = true;
						if (obj instanceof L2PcInstance)
							if (((L2PcInstance) obj).inObserverMode() || ((L2PcInstance) obj).isGM() && obj != activeChar)
							{
								continue;
							}
						if (obj instanceof L2Character)
							if (activeChar._event != null && activeChar._event.isRunning())
							{
								if (!activeChar._event.canBeSkillTarget(activeChar, (L2Character) obj, this))
								{
									continue;
								}
								if (activeChar._event != null && activeChar._event.isRunning() && ((L2Character) obj)._event != null && ((L2Character) obj)._event.isRunning())
								{
									checkPeace = false;
								}
							}
						if (obj instanceof L2Attackable || obj instanceof L2Playable)
						{
							L2Character cha = (L2Character) obj;
							boolean targetInPvP = cha.isInsideZone(L2Zone.FLAG_PVP) && !cha.isInsideZone(L2Zone.FLAG_SIEGE);

							if (obj == activeChar || obj == src || ((L2Character) obj).isDead())
							{
								continue;
							}

							if (src != null)
							{
								switch (targetType)
								{
									case TARGET_FRONT_AURA:
										if (!cha.isInFrontOf(activeChar))
										{
											continue;
										}
										break;
									case TARGET_BEHIND_AURA:
										if (!cha.isBehind(activeChar))
										{
											continue;
										}
										break;
								}

								if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
								{
									continue;
								}

								if (obj instanceof L2PcInstance)
								{
									if (!src.checkPvpSkill(obj, this))
									{
										continue;
									}

									if (src.isInOlympiadMode() && !src.isOlympiadStart())
									{
										continue;
									}
									if (checkPeace && ((L2PcInstance) obj).isInsideZone(L2Zone.FLAG_PEACE) && !src.allowPeaceAttack())
									{
										continue;
									}
									if (!obj.isVisible())
									{
										continue;
									}
									if (src.getParty() != null && ((L2PcInstance) obj).getParty() != null && src.getParty().getPartyLeaderOID() == ((L2PcInstance) obj).getParty().getPartyLeaderOID())
									{
										continue;
									}
									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == ((L2PcInstance) obj).getAllyId() && src.getAllyId() != 0)
										{
											continue;
										}
										if (src.getClanId() != 0 && src.getClanId() == ((L2PcInstance) obj).getClanId())
										{
											continue;
										}
									}
								}
								else if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
									{
										continue;
									}
									if (src.isInOlympiadMode() && !src.isOlympiadStart())
									{
										continue;
									}
									if (!src.checkPvpSkill(trg, this))
									{
										continue;
									}
									if (checkPeace && ((L2Summon) obj).isInsideZone(L2Zone.FLAG_PEACE) && !src.allowPeaceAttack())
									{
										continue;
									}
									if (!obj.isVisible())
									{
										continue;
									}
									if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									{
										continue;
									}
									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										{
											continue;
										}
										if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
										{
											continue;
										}
									}
								}
							}
							else if (!(obj instanceof L2Playable) && !activeChar.isConfused())
							{
								continue;
							}
							if (!Util.checkIfInRange(radius, activeChar, obj, true))
							{
								continue;
							}

							if (!onlyFirst)
							{
								targetList.add((L2Character) obj);
							}
							else
								return new L2Character[]
								{
									(L2Character) obj
								};
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_AREA:
				case TARGET_FRONT_AREA:
				case TARGET_BEHIND_AREA:
				{
					if (!(target instanceof L2Attackable || target instanceof L2Playable) || getCastRange() >= 0 && (target == activeChar || target.isAlikeDead()))
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					L2Character cha;
					if (getCastRange() >= 0)
					{
						cha = target;

						if (!onlyFirst)
						{
							targetList.add(cha);
						}
						else
							return new L2Character[]
							{
								cha
							};
					}
					else
					{
						cha = activeChar;
					}

					boolean effectOriginIsL2PlayableInstance = cha instanceof L2Playable;
					boolean srcIsSummon = activeChar instanceof L2Summon;

					L2PcInstance src = activeChar.getActingPlayer();

					int radius = getSkillRadius();

					boolean srcInPvP = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);

					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						boolean checkPeace = true;
						if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
						{
							continue;
						}
						if (obj == cha)
						{
							continue;
						}
						target = (L2Character) obj;

						if (obj instanceof L2Character)
							if (activeChar._event != null && activeChar._event.isRunning())
								if (!activeChar._event.canBeSkillTarget(activeChar, (L2Character) obj, this))
								{
									continue;
								}
								else if (activeChar._event != null && activeChar._event.isRunning() && ((L2Character) obj)._event != null && ((L2Character) obj)._event.isRunning())
								{
									checkPeace = false;
								}

						boolean targetInPvP = target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInsideZone(L2Zone.FLAG_SIEGE);

						if (!GeoData.getInstance().canSeeTarget(activeChar, target))
						{
							continue;
						}

						if (!target.isDead() && target != activeChar)
						{
							if (!Util.checkIfInRange(radius, obj, cha, true))
							{
								continue;
							}

							if (src != null)
							{
								switch (targetType)
								{
									case TARGET_FRONT_AREA:
										if (!cha.isInFrontOf(activeChar))
										{
											continue;
										}
										break;
									case TARGET_BEHIND_AREA:
										if (!cha.isBehind(activeChar))
										{
											continue;
										}
										break;
								}

								if (obj instanceof L2PcInstance)
								{
									L2PcInstance trg = (L2PcInstance) obj;
									if (trg == src)
									{
										continue;
									}
									if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									{
										continue;
									}

									if (!trg.isVisible())
									{
										continue;
									}
									if (checkPeace && trg.isInsideZone(L2Zone.FLAG_PEACE))
									{
										continue;
									}

									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										{
											continue;
										}

										if (src.getClan() != null && trg.getClan() != null)
											if (src.getClan().getClanId() == trg.getClan().getClanId())
											{
												continue;
											}

										if (!src.checkPvpSkill(obj, this, srcIsSummon))
										{
											continue;
										}
									}
								}
								if (obj instanceof L2Summon)
								{
									L2PcInstance trg = ((L2Summon) obj).getOwner();
									if (trg == src)
									{
										continue;
									}

									if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									{
										continue;
									}

									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										{
											continue;
										}

										if (src.getClan() != null && trg.getClan() != null)
											if (src.getClan().getClanId() == trg.getClan().getClanId())
											{
												continue;
											}

										if (!src.checkPvpSkill(trg, this, srcIsSummon))
										{
											continue;
										}
									}

									if (checkPeace && trg.isInsideZone(L2Zone.FLAG_PEACE))
									{
										continue;
									}
								}
							}
							else if (effectOriginIsL2PlayableInstance && !(obj instanceof L2Playable))
							{
								continue;
							}
							targetList.add((L2Character) obj);
						}
					}

					if (targetList.size() == 0)
						return null;

					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_AREA_UNDEAD:
				{
					L2Character cha;
					int radius = getSkillRadius();
					if (getCastRange() >= 0 && (target instanceof L2Npc || target instanceof L2SummonInstance) && target.isUndead() && !target.isAlikeDead())
					{
						cha = target;

						if (!onlyFirst)
						{
							targetList.add(cha);
						}
						else
							return new L2Character[]
							{
								cha
							};
					}
					else
					{
						cha = activeChar;
					}

					for (L2Object obj : cha.getKnownList().getKnownObjects().values())
					{
						if (obj instanceof L2Npc)
						{
							target = (L2Npc) obj;
						}
						else if (obj instanceof L2SummonInstance)
						{
							target = (L2SummonInstance) obj;
						}
						else
						{
							continue;
						}

						if (!GeoData.getInstance().canSeeTarget(activeChar, target))
						{
							continue;
						}

						if (!target.isAlikeDead())
						{
							if (!target.isUndead())
							{
								continue;
							}
							if (!Util.checkIfInRange(radius, cha, obj, true))
							{
								continue;
							}

							if (!onlyFirst)
							{
								targetList.add((L2Character) obj);
							}
							else
								return new L2Character[]
								{
									(L2Character) obj
								};
						}
					}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_PARTY:
				{
					if (onlyFirst)
						return new L2Character[]
						{
							activeChar
						};

					targetList.add(activeChar);

					L2PcInstance player = null;

					if (activeChar instanceof L2Summon)
					{
						player = ((L2Summon) activeChar).getOwner();
						targetList.add(player);
					}
					else if (activeChar instanceof L2PcInstance)
					{
						player = (L2PcInstance) activeChar;
						if (activeChar.getPet() != null)
						{
							targetList.add(activeChar.getPet());
						}
					}

					if (activeChar.getParty() != null)
					{
						List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();

						for (L2PcInstance partyMember : partyList)
						{
							if (player == null || partyMember == null || partyMember == player)
							{
								continue;
							}
							if (player.isInDuel() && player.getDuelId() != partyMember.getDuelId())
							{
								continue;
							}
							if (activeChar._event != null && !activeChar._event.canBeSkillTarget(activeChar, partyMember, this))
							{
								continue;
							}
							if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
							{
								targetList.add(partyMember);

								if (partyMember.getPet() != null && !partyMember.getPet().isDead())
								{
									targetList.add(partyMember.getPet());
								}
							}
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_PARTY_MEMBER:
				{
					if (target != null && target == activeChar || target != null && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID() || target != null && activeChar instanceof L2PcInstance && target instanceof L2Summon && activeChar.getPet() == target || target != null && activeChar instanceof L2Summon && target instanceof L2PcInstance && activeChar == target.getPet())
					{
						if (!target.isDead())
							return new L2Character[]
							{
								target
							};

						return null;
					}
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				case TARGET_PARTY_OTHER:
				{
					if (target != null && target != activeChar && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
					{
						if (!target.isDead())
						{
							if (target instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance) target;
								switch (getId())
								{
									case 426:
										if (!player.isMageClass())
											return new L2Character[]
											{
												target
											};
										return null;
									case 427:
										if (player.isMageClass())
											return new L2Character[]
											{
												target
											};
										return null;
								}
							}
							return new L2Character[]
							{
								target
							};
						}
						return null;
					}
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				case TARGET_CORPSE_ALLY:
				case TARGET_ALLY:
				{
					if (activeChar instanceof L2Playable)
					{
						int radius = getSkillRadius();
						L2PcInstance player = activeChar.getActingPlayer();
						if (player == null)
							return null;

						L2Clan clan = player.getClan();

						if (player.isInOlympiadMode())
						{
							if (player.getPet() == null)
								return new L2Character[]
								{
									player
								};

							return new L2Character[]
							{
								player,
								player.getPet()
							};
						}
						if (targetType != SkillTargetType.TARGET_CORPSE_ALLY)
							if (!onlyFirst)
							{
								targetList.add(player);
							}
							else
								return new L2Character[]
								{
									player
								};
						if (activeChar.getPet() != null)
							if (targetType != SkillTargetType.TARGET_CORPSE_ALLY && !activeChar.getPet().isDead())
							{
								targetList.add(activeChar.getPet());
							}
						if (clan != null)
						{
							for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
							{
								if (obj == player || !(obj instanceof L2Playable) || obj.getActingPlayer() == null)
								{
									continue;
								}

								L2PcInstance newTarget = obj.getActingPlayer();

								if ((newTarget.getAllyId() == 0 || newTarget.getAllyId() != player.getAllyId()) && (newTarget.getClan() == null || newTarget.getClanId() != player.getClanId()))
								{
									continue;
								}

								if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() || player.getParty() != null && player.getParty() != newTarget.getParty()))
								{
									continue;
								}
								if (activeChar._event != null && !activeChar._event.canBeSkillTarget(activeChar, newTarget, this))
								{
									continue;
								}
								L2Summon pet = newTarget.getPet();
								if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst && targetType == SkillTargetType.TARGET_ALLY && !pet.isDead() && player.checkPvpSkill(newTarget, this))
								{
									targetList.add(pet);
								}

								if (targetType == SkillTargetType.TARGET_CORPSE_ALLY)
									if (!newTarget.isDead())
									{
										continue;
									}

								if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
								{
									continue;
								}

								if (!player.checkPvpSkill(newTarget, this))
								{
									continue;
								}

								if (!onlyFirst)
								{
									targetList.add(newTarget);
								}
								else
									return new L2Character[]
									{
										newTarget
									};
							}
						}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_ENEMY_ALLY:
				{
					int radius = getSkillRadius();
					L2Character newTarget;

					if (getCastRange() > -1 && target != null)
					{
						newTarget = target;
					}
					else
					{
						newTarget = activeChar;
					}

					if (newTarget != activeChar || isSkillTypeOffensive())
					{
						targetList.add(newTarget);
					}

					for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
					{
						if (obj == newTarget || obj == activeChar)
						{
							continue;
						}

						if (obj instanceof L2Attackable)
							if (!obj.isAlikeDead())
							{
								if (activeChar instanceof L2PcInstance && !((L2PcInstance) activeChar).checkPvpSkill(obj, this))
								{
									continue;
								}

								if (activeChar instanceof L2PcInstance && obj instanceof L2PcInstance && (((L2PcInstance) activeChar).getClanId() != ((L2PcInstance) obj).getClanId() || ((L2PcInstance) activeChar).getAllyId() != ((L2PcInstance) obj).getAllyId() && activeChar.getParty() != null && obj.getParty() != null && activeChar.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID()))
								{
									continue;
								}

								targetList.add(obj);
							}
					}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_CORPSE_CLAN:
				case TARGET_CLAN:
				{
					if (activeChar instanceof L2Playable)
					{
						int radius = getSkillRadius();
						L2PcInstance player = activeChar.getActingPlayer();
						if (player == null)
							return null;

						L2Clan clan = player.getClan();

						if (player.isInOlympiadMode())
						{
							if (player.getPet() == null)
								return new L2Character[]
								{
									player
								};

							return new L2Character[]
							{
								player,
								player.getPet()
							};
						}

						if (targetType != SkillTargetType.TARGET_CORPSE_CLAN)
							if (!onlyFirst)
							{
								targetList.add(player);
							}
							else
								return new L2Character[]
								{
									player
								};

						if (activeChar.getPet() != null)
							if (targetType != SkillTargetType.TARGET_CORPSE_ALLY && !activeChar.getPet().isDead())
							{
								targetList.add(activeChar.getPet());
							}
						if (clan != null)
						{
							for (L2ClanMember member : clan.getMembers())
							{
								L2PcInstance newTarget = member.getPlayerInstance();

								if (newTarget == null || newTarget == player)
								{
									continue;
								}

								if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() || player.getParty() == null && player.getParty() != newTarget.getParty()))
								{
									continue;
								}
								if (activeChar._event != null && !activeChar._event.canBeSkillTarget(activeChar, newTarget, this))
								{
									continue;
								}

								L2Summon pet = newTarget.getPet();
								if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst && targetType == SkillTargetType.TARGET_CLAN && !pet.isDead() && player.checkPvpSkill(newTarget, this))
								{
									targetList.add(pet);
								}

								if (targetType == SkillTargetType.TARGET_CORPSE_CLAN)
								{
									if (!newTarget.isDead())
									{
										continue;
									}
									if (getSkillType() == L2SkillType.RESURRECT)
									{
										Siege siege = SiegeManager.getSiege(newTarget);
										if (siege != null && siege.getIsInProgress())
											if (!newTarget.getCharmOfCourage() || player.getSiegeState() == 0)
											{
												continue;
											}
									}
								}
								if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
								{
									continue;
								}

								if (!player.checkPvpSkill(newTarget, this))
								{
									continue;
								}

								if (!onlyFirst)
								{
									targetList.add(newTarget);
								}
								else
									return new L2Character[]
									{
										newTarget
									};
							}
						}
					}
					else if (activeChar instanceof L2Npc)
					{
						L2Npc npc = (L2Npc) activeChar;
						for (L2Object newTarget : activeChar.getKnownList().getKnownObjects().values())
							if (newTarget instanceof L2Npc && ((L2Npc) newTarget).getFactionId() == npc.getFactionId())
							{
								if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true))
								{
									continue;
								}
								if (((L2Npc) newTarget).getFirstEffect(this) != null)
								{
									targetList.add((L2Npc) newTarget);
									break;
								}
							}
						if (targetList.isEmpty())
						{
							targetList.add(activeChar);
						}
					}

					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_MULTIFACE:
				{
					return getMultiFaceTargetList(activeChar);
				}
				case TARGET_CORPSE_PLAYER:
				{
					if (target != null && target.isDead())
					{
						L2PcInstance player = null;

						if (activeChar instanceof L2PcInstance)
						{
							player = (L2PcInstance) activeChar;
						}
						L2PcInstance targetPlayer = null;

						if (target instanceof L2PcInstance)
						{
							targetPlayer = (L2PcInstance) target;
						}
						L2PetInstance targetPet = null;

						if (target instanceof L2PetInstance)
						{
							targetPet = (L2PetInstance) target;
						}

						if (player != null && (targetPlayer != null || targetPet != null))
						{
							boolean condGood = true;

							if (getSkillType() == L2SkillType.RESURRECT)
							{
								Siege siege = null;

								if (targetPlayer != null)
								{
									siege = SiegeManager.getSiege(targetPlayer);
								}
								else if (targetPet != null)
								{
									siege = SiegeManager.getSiege(targetPet);
								}

								if (siege != null && siege.getIsInProgress() && targetPlayer != null && (!targetPlayer.getCharmOfCourage() || player.getSiegeState() == 0))
								{
									condGood = false;
									player.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
								}

								if (targetPlayer != null)
								{
									if (targetPlayer.isReviveRequested())
									{
										player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
										condGood = false;
									}
								}
								else if (targetPet != null)
									if (targetPet.getOwner() != player)
									{
										condGood = false;
										player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_YOU_NOT_PET_OWNER));
									}
							}

							if (condGood)
							{
								if (!onlyFirst)
								{
									targetList.add(target);
									return targetList.moveToArray(new L2Character[targetList.size()]);
								}

								return new L2Character[]
								{
									target
								};
							}
						}
					}
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				case TARGET_CORPSE_MOB:
				{
					if (!(target instanceof L2Attackable) || !target.isDead())
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					switch (getSkillType())
					{
						case DRAIN:
						case SUMMON:
						{
							if (DecayTaskManager.getInstance().hasDecayTask(target))
								if (DecayTaskManager.getInstance().getRemainingDecayTime(target) < 0.5)
								{
									activeChar.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
									return null;
								}
						}
					}
					if (!onlyFirst)
					{
						targetList.add(target);
						return targetList.moveToArray(new L2Character[targetList.size()]);
					}
					return new L2Character[]
					{
						target
					};
				}
				case TARGET_AREA_CORPSE_MOB:
				{
					if (!(target instanceof L2Attackable) || !target.isDead())
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					if (!onlyFirst)
				{
						targetList.add(target);
					}
					else
						return new L2Character[]
						{
							target
						};

					boolean srcInArena = activeChar.isInsideZone(L2Zone.FLAG_PVP) && !activeChar.isInsideZone(L2Zone.FLAG_SIEGE);
					L2PcInstance src = null;

					if (activeChar instanceof L2PcInstance)
					{
						src = (L2PcInstance) activeChar;
					}

					L2PcInstance trg = null;

					int radius = getSkillRadius();

					if (activeChar.getKnownList() != null)
					{
						for (L2Object obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{



							if (!Util.checkIfInRange(radius, target, obj, true))
							{
								continue;
							}

							if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
							{
								continue;
							}

							if (isOffensive() && L2Character.isInsidePeaceZone(activeChar, obj))
							{
								continue;
							}

							if (obj instanceof L2PcInstance && src != null)
							{
								trg = (L2PcInstance) obj;

								if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								{
									continue;
								}

								if (!srcInArena && !(trg.isInsideZone(L2Zone.FLAG_PVP) && !trg.isInsideZone(L2Zone.FLAG_SIEGE)))
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									{
										continue;
									}

									if (src.getClan() != null && trg.getClan() != null)
										if (src.getClan().getClanId() == trg.getClan().getClanId())
										{
											continue;
										}

									if (!src.checkPvpSkill(obj, this))
									{
										continue;
									}
								}
							}
							if (obj instanceof L2Summon && src != null)
							{
								trg = ((L2Summon) obj).getOwner();

								if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								{
									continue;
								}

								if (!srcInArena && !(trg.isInsideZone(L2Zone.FLAG_PVP) && !trg.isInsideZone(L2Zone.FLAG_SIEGE)))
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									{
										continue;
									}

									if (src.getClan() != null && trg.getClan() != null)
										if (src.getClan().getClanId() == trg.getClan().getClanId())
										{
											continue;
										}

									if (!src.checkPvpSkill(trg, this))
									{
										continue;
									}
								}

							}
							if (trg == src)
							{
								continue;
							}

							targetList.add((L2Character) obj);
						}
					}

					if (targetList.size() == 0)
						return null;

					trg = null;
					src = null;

					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_AREA_CORPSES:
				{
					if (!(target instanceof L2Attackable) || !target.isDead())
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}
					if (!onlyFirst)
					{
						targetList.add(target);
					}
					else
						return new L2Character[]
						{
							target
						};

					int radius = getSkillRadius();
					if (activeChar.getKnownList() != null)
					{
						for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
						{
							if (obj == null || !(obj instanceof L2Attackable))
							{
								continue;
							}
							L2Character cha = (L2Character) obj;

							if (!cha.isDead() || !Util.checkIfInRange(radius, target, cha, true))
							{
								continue;
							}

							if (!GeoData.getInstance().canSeeTarget(activeChar, cha))
							{
								continue;
							}

							targetList.add(cha);
						}
					}
					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_UNLOCKABLE:
				{
					if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance))
						return null;

					if (!onlyFirst)
					{
						targetList.add(target);
						return targetList.moveToArray(new L2Character[targetList.size()]);
					}
					return new L2Character[]
					{
						target
					};
				}
				case TARGET_ENEMY_SUMMON:
				{
					if (target instanceof L2Summon)
					{
						L2Summon targetSummon = (L2Summon) target;
						if (activeChar instanceof L2PcInstance && activeChar.getPet() != targetSummon && !targetSummon.isDead() && (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0) || targetSummon.getOwner().isInsideZone(L2Zone.FLAG_PVP) && activeChar.isInsideZone(L2Zone.FLAG_PVP))
							return new L2Character[]
							{
								targetSummon
							};
					}
					return null;
				}
				case TARGET_GATE:
				{
					if (target == null || target.isDead() || !(target instanceof L2DoorInstance))
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}
					return new L2Character[]
					{
						target
					};
				}
				case TARGET_MOB:
				{
					if (target == null || target.isDead() || !(target instanceof L2Attackable))
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}
					return new L2Character[]
					{
						target
					};
				}
				case TARGET_KNOWNLIST:
				{
					if (target != null && target.getKnownList() != null)
					{
						for (L2Object obj : target.getKnownList().getKnownObjects().values())
							if (obj instanceof L2Attackable || obj instanceof L2Playable)
								return new L2Character[]
								{
									(L2Character) obj
								};
					}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_PIG:
				{
					if (target != null && target instanceof L2Npc)
					{
						int npcId = ((L2Npc) target).getNpcId();
						if (npcId >= 13031 && npcId <= 13035)
							return new L2Character[]
							{
								target
							};
					}
					return null;
				}
				default:
					return null;
			}
		}
		finally
		{
			targetList.clear();
		}
	}

	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}

	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}

	public final int getTimeMulti()
	{
		return _timeMulti;
	}

	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}

	public int getTriggeredCount()
	{
		return _triggeredCount;
	}

	public int getTriggeredId()
	{
		return _triggeredId;
	}

	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}

	public L2Skill getTriggeredSkill()
	{
		return SkillTable.getInstance().getInfo(_triggeredId, _triggeredLevel); // is
		// there
		// any
		// skill
		// with
		// bigger
		// level
		// than
		// one?!
		// :$
	}

	public final boolean getWeaponDependancy(L2Character activeChar, boolean message)
	{
		int weaponsAllowed = getWeaponsAllowed();
		if (weaponsAllowed == 0)
			return true;

		L2Weapon weapon = activeChar.getActiveWeaponItem();
		if (weapon != null && (weapon.getItemType().mask() & weaponsAllowed) != 0)
			return true;

		L2Weapon weapon2 = activeChar.getSecondaryWeaponItem();
		if (weapon2 != null && (weapon2.getItemType().mask() & weaponsAllowed) != 0)
			return true;

		if (message)
		{
			if (_weaponDependancyMessage == null)
			{
				StringBuilder sb = new StringBuilder();
				for (L2WeaponType wt : L2WeaponType.VALUES)
					if ((wt.mask() & weaponsAllowed) != 0)
					{
						if (sb.length() != 0)
						{
							sb.append('/');
						}

						sb.append(wt);
					}
				sb.append(".");

				_weaponDependancyMessage = getName() + " " + Message.getMessage((L2PcInstance) activeChar, Message.MessageId.MSG_WEAPON_MAY_USE_WITH) + " " + sb.toString();
				_weaponDependancyMessage = _weaponDependancyMessage.intern();
			}

			activeChar.sendMessage(_weaponDependancyMessage);
		}

		return false;
	}

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}

	public boolean hasEffects()
	{
		return _effectTemplates != null && _effectTemplates.length > 0;
	}

	public final boolean ignoreShld()
	{
		return _ignoreShld;
	}

	public boolean is5MinMagic()
	{
		return _is5MinMagic;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}

	public final boolean isBalance()
	{
		return _operateType == SkillOpType.OP_BALANCE;
	}

	public final boolean isAdvanced()
	{
		return _isAdvanced;
	}

	public final boolean isBadBuff()
	{
		switch (_skillType)
		{
			case BAD_BUFF:
				return true;
			default:
				return false;
		}
	}

	public boolean isBehindFromCaster(int heading, L2Character caster, L2Character target)
	{
		if (caster == null || target == null)
			return true;

		double befHeading = Util.convertHeadingToDegree(heading);
		if (befHeading > 360)
		{
			befHeading -= 360;
		}
		else if (befHeading < 0)
		{
			befHeading += 360;
		}

		int dx = caster.getX() - target.getX();
		int dy = caster.getY() - target.getY();

		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist == 0)
		{
			dist = 0.01;
		}

		double sin = dy / dist;
		double cos = dx / dist;
		int newheading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);

		double aftHeading = Util.convertHeadingToDegree(newheading);
		if (aftHeading > 360)
		{
			aftHeading -= 360;
		}
		else if (aftHeading < 0)
		{
			aftHeading += 360;
		}

		double diffHeading = Math.abs(aftHeading - befHeading);
		if (diffHeading > 360)
		{
			diffHeading -= 360;
		}
		else if (diffHeading < 0)
		{
			diffHeading += 360;
		}
		return diffHeading > 90 && diffHeading < 270;

	}

	public boolean isBufferMagic()
	{
		return _bufferMagic;
	}

	public final boolean isChance()
	{
		return _operateType == SkillOpType.OP_CHANCE;
	}

	public boolean isChMagic()
	{
		return _chMagic;
	}

	public final boolean isCubic()
	{
		return _isCubic;
	}

	public final boolean isDance()
	{
		return _isDance;
	}

	public final boolean isDebuff()
	{
		return _isDebuff;
	}

	public boolean isHeroMagic()
	{
		return _heroMagic;
	}

	public final boolean isItemSkill()
	{
		return _itemSkill;
	}

	public final boolean isMagic()
	{
		return _magic;
	}

	public final boolean isNeedWeapon()
	{
		return _skillType == L2SkillType.MDAM;
	}

	public final boolean isNeutral()
	{
		return _isNeutral;
	}

	public final boolean isOffensive()
	{
		return _isOffensive;
	}

	public final boolean isOverhit()
	{
		return _overhit;
	}

	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}

	public final boolean isPhysical()
	{
		return _physic;
	}

	public final boolean isPositive()
	{
		switch (_skillType)
		{
			case BUFF:
			case HEAL:
			case HEAL_PERCENT:
			case HOT:
			case MANAHEAL:
			case MANARECHARGE:
			case COMBATPOINTHEAL:
			case COMBATPOINTPERCENTHEAL:
			case REFLECT:
			case SHIFT_TARGET:
				return true;
			default:
				return false;
		}
	}

	public final boolean isPotion()
	{
		return _ispotion;
	}

	public final boolean isPvpSkill()
	{
		switch (_skillType)
		{
			case DOT:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MANADAM:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case CANCEL_TARGET:
			case BETRAY:
			case STEAL_BUFF:
			case BAD_BUFF:
			case AGGDAMAGE:
			case DELUXE_KEY_UNLOCK:
			case FATALCOUNTER:
			case MAKE_KILLABLE:
			case AGGREDUCE_CHAR:
				return true;
			default:
				return false;
		}
	}

	public final boolean isSkillTypeMagic()
	{
		switch (getSkillType())
		{
			case MDAM:
			case HEAL:
			case SUMMON_FRIEND:
			case BALANCE_LIFE:
				return true;
			default:
				return false;
		}
	}

	public final boolean isSkillTypeOffensive()
	{
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAM:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case FATALCOUNTER:
			case DETECT_WEAKNESS:
			case MDOT:
			case MANADAM:
			case MUTE:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case WEAKNESS:
			case MANA_BY_LEVEL:
			case SWEEP:
			case PARALYZE:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case UNSUMMON_ENEMY_PET:
			case CANCEL_TARGET:
			case BETRAY:
			case SOW:
			case HARVEST:
			case STEAL_BUFF:
			case BAD_BUFF:
			case INSTANT_JUMP:
				return true;
			default:
				return isDebuff();
		}
	}

	public final boolean isSong()
	{
		return _isSong;
	}

	public final boolean isStaticHitTime()
	{
		return _staticHitTime;
	}

	public final boolean isStaticReuse()
	{
		return _staticReuse;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}

	public final boolean killByDOT()
	{
		return _killByDOT;
	}

	public final void markAsItemSkill()
	{
		_itemSkill = true;
	}

	public final boolean nextActionIsAttack()
	{
		return _nextActionIsAttack;
	}

	public final boolean ownedFuncShouldBeDisabled(L2Character activeChar)
	{
		if (isOffensive())
			return false;

		if (!(isDance() || isSong()) && !getWeaponDependancy(activeChar, false))
			return true;

		return false;
	}

	public final void setDuration(int dur)
	{
		_duration = dur;
	}

	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + " item=" + isItemSkill() + " ]";
	}

	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}

	public boolean useAlways()
	{
		return _useAlways;
	}

	public final boolean useFishShot()
	{
		return getSkillType() == L2SkillType.PUMPING || getSkillType() == L2SkillType.REELING;
	}

	public void useSkill(L2Character caster, L2Character... targets)
	{
		caster.sendPacket(ActionFailed.STATIC_PACKET);

		if (caster instanceof L2PcInstance)
		{
			// ((L2PcInstance) caster).sendMessage(String.format(Message.getMessage((L2PcInstance) caster, Message.MessageId.MSG_SKILL_NOT_IMPLEMENTED), getId()));
		}
	}

	public final boolean useSoulShot()
	{
		boolean result = false;

		if (!isMagic() && getSkillType() != L2SkillType.PUMPING || getSkillType() != L2SkillType.REELING)
		{
			result = true;
		}
		return result;
	}

	public final boolean useSpiritShot()
	{
		return isMagic();
	}

	protected int getNewHeadingToTarget(L2Character caster, L2Character target)
	{
		if (caster == null || target == null)
			return 0;

		double befHeading = Util.convertHeadingToDegree(caster.getHeading());
		if (befHeading > 360)
		{
			befHeading -= 360;
		}

		int dx = caster.getX() - target.getX();
		int dy = caster.getY() - target.getY();

		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist == 0)
		{
			dist = 0.01;
		}

		double sin = dy / dist;
		double cos = dx / dist;
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
		return heading;
	}
}
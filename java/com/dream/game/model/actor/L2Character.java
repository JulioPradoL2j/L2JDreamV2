package com.dream.game.model.actor;

import static com.dream.game.ai.CtrlIntention.ACTIVE;
import static com.dream.game.ai.CtrlIntention.ATTACK;
import static com.dream.game.ai.CtrlIntention.FOLLOW;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.GameTimeController;
import com.dream.game.Shutdown;
import com.dream.game.ai.CtrlEvent;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2AttackableAI;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.geodata.pathfinding.AbstractNodeLoc;
import com.dream.game.geodata.pathfinding.PathFinding;
import com.dream.game.handler.SkillHandler;
import com.dream.game.manager.BotsPreventionManager;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortResistSiegeManager;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.ChanceCondition;
import com.dream.game.model.ChanceSkillList;
import com.dream.game.model.CharEffectList;
import com.dream.game.model.FusionSkill;
import com.dream.game.model.IEffector;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2SiegeStatus;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.actor.instance.L2ArtefactInstance;
import com.dream.game.model.actor.instance.L2BabyPetInstance;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2ControlTowerInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2EffectPointInstance;
import com.dream.game.model.actor.instance.L2FlameTowerInstance;
import com.dream.game.model.actor.instance.L2GuardInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MinionInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2NpcWalkerInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PcInstance.SkillDat;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2RiftInvaderInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.model.actor.knownlist.CharKnownList;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.actor.stat.CharStat;
import com.dream.game.model.actor.status.CharStatus;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.model.world.Location;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.Disconnection;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.Attack;
import com.dream.game.network.serverpackets.ChangeMoveType;
import com.dream.game.network.serverpackets.ChangeWaitType;
import com.dream.game.network.serverpackets.DeleteObject;
import com.dream.game.network.serverpackets.FlyToLocation;
import com.dream.game.network.serverpackets.FlyToLocation.FlyType;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.MagicSkillCanceled;
import com.dream.game.network.serverpackets.MagicSkillLaunched;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.MoveToLocation;
import com.dream.game.network.serverpackets.Revive;
import com.dream.game.network.serverpackets.SetupGauge;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.StopMove;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.TeleportToLocation;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.skills.AbnormalEffect;
import com.dream.game.skills.Calculator;
import com.dream.game.skills.Formulas;
import com.dream.game.skills.Stats;
import com.dream.game.skills.effects.EffectTemplate;
import com.dream.game.skills.funcs.Func;
import com.dream.game.skills.funcs.FuncOwner;
import com.dream.game.skills.l2skills.L2SkillChargeDmg;
import com.dream.game.skills.l2skills.L2SkillMount;
import com.dream.game.skills.l2skills.L2SkillSummon;
import com.dream.game.templates.chars.L2CharTemplate;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Broadcast;
import com.dream.game.util.Util;
import com.dream.lang.L2System;
import com.dream.tools.geometry.Point3D;
import com.dream.tools.random.Rnd;
import com.dream.util.SingletonList;
import com.dream.util.SingletonSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

public abstract class L2Character extends L2Object implements IEffector
{
	public class AIAccessor
	{
		public AIAccessor()
		{
		}

		public void detachAI()
		{
			_ai = null;
		}

		public void doAttack(L2Character target)
		{
			if (L2Character.this != target)
			{
				L2Character.this.doAttack(target);
			}
		}

		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}

		public L2Character getActor()
		{
			return L2Character.this;
		}

		public boolean moveTo(int x, int y, int z)
		{
			return moveToLocation(x, y, z, 0);
		}

		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}

		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}

		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}
	}

	private class AutoSS implements Runnable
	{
		private final L2Character _character;
		private final int[] SKILL_IDS =
		{
			2039,
			2150,
			2151,
			2152,
			2153,
			2154,
			2154,
			2154
		};

		public AutoSS(L2Character Character)
		{
			_character = Character;
		}

		@Override
		public void run()
		{
			if (_character instanceof L2PcInstance)
				if (((L2PcInstance) _character).getAutoSoulShot().size() > 0)
				{
					L2PcInstance pl = (L2PcInstance) _character;
					L2Weapon weaponItem = _character.getActiveWeaponItem();
					if (weaponItem != null && weaponItem.getSoulShotCount() > 0)
					{
						int weaponGrade = weaponItem.getCrystalType();
						if (weaponGrade == L2Item.CRYSTAL_NONE && !pl.getAutoSoulShot().containsKey(5789) && !pl.getAutoSoulShot().containsKey(1853) || weaponGrade == L2Item.CRYSTAL_D && !pl.getAutoSoulShot().containsKey(1463) || weaponGrade == L2Item.CRYSTAL_C && !pl.getAutoSoulShot().containsKey(1464) || weaponGrade == L2Item.CRYSTAL_B && !pl.getAutoSoulShot().containsKey(1465) || weaponGrade == L2Item.CRYSTAL_A && !pl.getAutoSoulShot().containsKey(1466) || weaponGrade == L2Item.CRYSTAL_S && !pl.getAutoSoulShot().containsKey(1467) || weaponGrade == L2Item.CRYSTAL_R && !pl.getAutoSoulShot().containsKey(1467) || weaponGrade == L2Item.CRYSTAL_S80 && !pl.getAutoSoulShot().containsKey(1467) || weaponGrade == L2Item.CRYSTAL_S84 && !pl.getAutoSoulShot().containsKey(1467))
							return;

						_character.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
						Broadcast.toSelfAndKnownPlayersInRadius(_character, new MagicSkillUse(_character, _character, SKILL_IDS[weaponGrade], 1, 0, 0, false), 360000);
					}
				}
			if (_character instanceof L2Summon)
				if (((L2Summon) _character).getOwner().getAutoSoulShot().size() > 0)
				{
					L2ItemInstance weaponInst = _character.getActiveWeaponInstance();
					if (weaponInst != null)
					{
						if (((L2Summon) _character).getOwner().getAutoSoulShot().containsKey(6645))
						{
							((L2Summon) _character).getOwner().sendPacket(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT);
							Broadcast.toSelfAndKnownPlayersInRadius(((L2Summon) _character).getOwner(), new MagicSkillUse(_character, _character, 2033, 1, 0, 0, false), 360000);
						}
					}
					else if (!(_character instanceof L2PetInstance) || _character instanceof L2BabyPetInstance)
						if (((L2Summon) _character).getOwner().getAutoSoulShot().containsKey(6645))
						{
							((L2Summon) _character).getOwner().sendPacket(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT);
							Broadcast.toSelfAndKnownPlayersInRadius(((L2Summon) _character).getOwner(), new MagicSkillUse(_character, _character, 2033, 1, 0, 0, false), 360000);
						}
				}
		}
	}

	public class CheckFalling implements Runnable
	{
		private final int _fallHeight;
		private Future<?> _task;

		public CheckFalling(int fallHeight)
		{
			_fallHeight = fallHeight;
		}

		@Override
		public void run()
		{
			if (_task != null)
			{
				_task.cancel(true);
				_task = null;
			}

			try
			{
				isFalling(true, _fallHeight);
			}
			catch (Exception e)
			{

			}
		}

		public void setTask(Future<?> task)
		{
			_task = task;
		}
	}

	class EnableSkill implements Runnable
	{
		int _skillId;

		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}

		@Override
		public void run()
		{
			try
			{
				enableSkill(_skillId);
			}
			catch (Exception e)
			{

			}
		}
	}

	class FlyToLocationTask implements Runnable
	{
		L2Object _target;
		L2Character _actor;
		L2Skill _skill;

		public FlyToLocationTask(L2Character actor, L2Object target, L2Skill skill)
		{
			_actor = actor;
			_target = target;
			_skill = skill;
		}

		@Override
		public void run()
		{
			try
			{
				FlyType _flyType = FlyType.valueOf(_skill.getFlyType());
				broadcastPacket(new FlyToLocation(_actor, _target, _flyType));
				getPosition().setXYZ(_target.getX(), _target.getY(), _target.getZ());
				broadcastPacket(new ValidateLocation(_actor));
			}
			catch (Exception e)
			{

			}
		}
	}

	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		byte _shld;
		boolean _soulshot;

		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}

		@Override
		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (Exception e)
			{

			}
		}
	}

	class MagicUseTask implements Runnable
	{
		L2Character[] _targets;
		L2Skill _skill;
		int _coolTime;
		int _phase;
		boolean _simultaneously;

		public MagicUseTask(L2Character[] targets, L2Skill skill, int coolTime, int phase, boolean simultaneously)
		{
			_targets = targets;
			_skill = skill;
			_coolTime = coolTime;
			_phase = phase;
			_simultaneously = simultaneously;
		}

		@Override
		public void run()
		{
			try
			{
				switch (_phase)
				{
					case 1:
						onMagicLaunchedTimer(_targets, _skill, _coolTime, false, _simultaneously);
						break;
					case 2:
						try
						{
							onMagicHitTimer(_targets, _skill, _coolTime, false, _simultaneously);
						}
						catch (Exception e)
						{

						}
						break;
					case 3:
						onMagicFinalizer(_skill, _targets[0], _simultaneously);
						break;
					default:
						break;
				}
			}
			catch (Exception e)
			{
				if (_simultaneously)
				{
					setIsCastingSimultaneouslyNow(false);
				}
				else
				{
					setIsCastingNow(false);
				}
			}
		}
	}

	public static class MoveData
	{
		public int _moveStartTime;
		public int _moveTimestamp;
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate;
		public double _yAccurate;
		public double _zAccurate;
		public int _yMoveFrom;
		public int _zMoveFrom;
		public int _heading;
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}

	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;

		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}

		@Override
		public void run()
		{
			try
			{
				if (getAI() == null)
					return;

				getAI().notifyEvent(_evt, null);
			}
			catch (Exception e)
			{
			}
		}
	}

	public class PvPFlag implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (System.currentTimeMillis() > getPvpFlagLasts())
				{
					stopPvPFlag();
				}
				else if (System.currentTimeMillis() > getPvpFlagLasts() - 20000)
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance _currPlayer;
		L2Skill _queuedSkill;
		boolean _isCtrlPressed;
		boolean _isShiftPressed;

		public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}

		@Override
		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Exception e)
			{
			}
		}
	}

	private class UsePotionTask implements Runnable
	{
		private final L2Character _activeChar;
		private final L2Skill _skill;

		UsePotionTask(L2Character activeChar, L2Skill skill)
		{
			_activeChar = activeChar;
			_skill = skill;
		}

		@Override
		public void run()
		{
			try
			{
				_activeChar.doSimultaneousCast(_skill);
			}
			catch (Exception e)
			{

			}
		}
	}

	public final static Logger _log = Logger.getLogger(L2Character.class.getName());

	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;

	private static final Calculator[] NPC_STD_CALCULATOR;

	static
	{
		NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
	}

	public static boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
			return false;
		if (!(target instanceof L2Playable && attacker instanceof L2Playable))
			return false;

		// Attack Monster on Peace Zone like L2OFF.
		if (target instanceof L2MonsterInstance || attacker instanceof L2MonsterInstance)
			return false;

		// Attack Guard on Peace Zone like L2OFF.
		if (target instanceof L2GuardInstance || attacker instanceof L2GuardInstance)
			return false;
		// Attack NPC on Peace Zone like L2OFF.
		if (target instanceof L2NpcInstance || attacker instanceof L2NpcInstance)
			return false;

		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			if (target.getActingPlayer() != null && target.getActingPlayer().getKarma() > 0)
				return false;
			if (attacker.getActingPlayer() != null && attacker.getActingPlayer().getKarma() > 0 && target.getActingPlayer() != null && target.getActingPlayer().getPvpFlag() > 0)
				return false;
		}

		return ((L2Character) attacker).isInsideZone(L2Zone.FLAG_PEACE) || ((L2Character) target).isInsideZone(L2Zone.FLAG_PEACE);
	}

	public static boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return !attacker.allowPeaceAttack() && isInsidePeaceZone((L2Object) attacker, target);
	}

	private List<L2Character> _attackByList;
	private L2Character _attackingChar;
	private volatile boolean _isCastingNow = false;
	private volatile boolean _isCastingSimultaneouslyNow = false;
	private L2Skill _lastSimultaneousSkillCast;
	private boolean _block_buffs = false;
	private boolean _isAfraid = false;
	private boolean _isConfused = false;
	private boolean _isFakeDeath = false;
	private boolean _isFallsdown = false;
	private boolean _isMuted = false;
	private boolean _isPhysicalMuted = false;
	private boolean _isPhysicalAttackMuted = false;
	private boolean _isDead = false;
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false;
	private boolean _isParalyzed = false;
	private boolean _isPetrified = false;
	private boolean _isPendingRevive = false;
	private boolean _isRooted = false;
	private boolean _isRunning = true;
	private boolean _isImmobileUntilAttacked = false;
	private boolean _isSleeping = false;
	private boolean _lastHitIsCritical;
	private boolean _isBlessedByNoblesse = false;
	private boolean _isLuckByNoblesse = false;
	private boolean _isBetrayed = false;
	private boolean _isStunned = false;
	protected boolean _isTeleporting = false;
	protected boolean _isInvul = false;
	protected boolean _isDisarmed = false;
	protected boolean _isMarked = false;
	private int _lastHealAmount = 0;
	public GameEvent _event;
	public final int[] lastPosition =
	{
		0,
		0,
		0
	};
	protected CharStat _stat;
	protected CharStatus _status;
	private L2CharTemplate _template;
	protected boolean _showSummonAnimation = false;
	protected String _title;
	private boolean _champion = false;
	private double _hpUpdateIncCheck = .0;

	private double _hpUpdateDecCheck = .0;

	private double _hpUpdateInterval = .0;

	private int _healLimit = 0;

	public L2Effect _itemActiveSkill = null;

	private boolean _isFlying = false;

	private Calculator[] _calculators;

	protected Map<Integer, L2Skill> _skills = new LinkedHashMap<>();

	protected ChanceSkillList _chanceSkills;

	protected FusionSkill _fusionSkill;

	protected byte _zoneValidateCounter = 4;

	private boolean _isRaid = false;

	private boolean _isBoss = false;

	private boolean _isGrandBoss = false;

	private final byte[] _currentZones = new byte[28];

	protected CharKnownList _knownList;

	private int _team = 0;

	private final List<L2Zone> _currentZoneList = new ArrayList<>();

	private int _AbnormalEffects;

	private final CharEffectList _effects = new CharEffectList(this);

	protected Set<Integer> _disabledSkills;

	private boolean _allSkillsDisabled;

	protected MoveData _move;

	private int _heading;

	private L2Object _target = null;

	private long _attackEndTime;

	private int _attacking;

	private int _disableBowAttackEndTime;

	private int _castInterruptTime;

	protected L2CharacterAI _ai;

	protected Future<?> _skillCast;

	protected Future<?> _skillCast2;

	private List<QuestState> _NotifyQuestOfDeathList = new SingletonList<>();

	private long _lastKnowUpdate;

	protected boolean _isMoving;

	protected Location _startLoc;

	private Future<?> _PvPRegTask;

	private long _pvpFlagLasts;

	private boolean _AIdisabled = false;

	private boolean _isMinion = false;

	private long _nextReducingHPByOverTime = -1;

	private long _nextReducingMPByOverTime = -1;

	private final List<L2Zone> _currentZonesInstances = new ArrayList<>();

	private String _eventTitle;

	private L2PcInstance player;

	// Tournament Event
	private boolean inArenaEvent = false;
	private boolean _ArenaAttack;
	private boolean _ArenaProtection;

	public void setInArenaEvent(boolean val)
	{
		inArenaEvent = val;
	}

	public boolean isInArenaEvent()
	{
		return inArenaEvent;
	}

	public void setArenaAttack(boolean comm)
	{
		_ArenaAttack = comm;
	}

	public boolean isArenaAttack()
	{
		return _ArenaAttack;
	}

	public void setArenaProtection(boolean comm)
	{
		_ArenaProtection = comm;
	}

	public boolean isArenaProtection()
	{
		return _ArenaProtection;
	}

	// Tournament Arenas
	private boolean _Arena1x1;
	private boolean _Arena2x2;
	private boolean _Arena4x4;
	private boolean _Arena9x9;

	public void setArena1x1(boolean comm)
	{
		_Arena1x1 = comm;
	}

	public boolean isArena1x1()
	{
		return _Arena1x1;
	}

	public void setArena2x2(boolean comm)
	{
		_Arena2x2 = comm;
	}

	public boolean isArena2x2()
	{
		return _Arena2x2;
	}

	public void setArena4x4(boolean comm)
	{
		_Arena4x4 = comm;
	}

	public boolean isArena4x4()
	{
		return _Arena4x4;
	}

	public void setArena9x9(boolean comm)
	{
		_Arena9x9 = comm;
	}

	public boolean isArena9x9()
	{
		return _Arena9x9;
	}

	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		getKnownList();

		_template = template;

		if (template != null && this instanceof L2Npc)
		{
			if (this instanceof L2DoorInstance)
			{
				_calculators = Formulas.getStdDoorCalculators();
			}
			else
			{
				_calculators = NPC_STD_CALCULATOR;
			}
			_skills = ((L2NpcTemplate) template).getSkills();
			if (_skills != null)
			{
				for (Map.Entry<Integer, L2Skill> skill : _skills.entrySet())
				{
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
				}
			}
		}
		else
		{
			_skills = new ConcurrentHashMap<>();
			_calculators = new Calculator[Stats.NUM_STATS];
			Formulas.addFuncsToNewCharacter(this);
		}

		if (!(this instanceof L2Playable) && !(this instanceof L2Attackable) && !(this instanceof L2ControlTowerInstance) && !(this instanceof L2FlameTowerInstance) && !(this instanceof L2DoorInstance) && !(this instanceof L2SiegeFlagInstance) && !(this instanceof L2Decoy) && !(this instanceof L2EffectPointInstance) && !(this instanceof L2NpcInstance))
		{
			setIsInvul(true);
		}
	}

	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			setAttackingChar(this);
			_attacking = 0;
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public final void abortCast()
	{
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}
			if (_skillCast2 != null)
			{
				_skillCast2.cancel(false);
				_skillCast2 = null;
			}

			if (getFusionSkill() != null)
			{
				getFusionSkill().onCastAbort();
			}

			L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
			if (mog != null)
			{
				mog.exit();
			}

			if (_allSkillsDisabled)
			{
				enableAllSkills();
			}
			setIsCastingNow(false);
			setIsCastingSimultaneouslyNow(false);
			_castInterruptTime = 0;
			if (this instanceof L2PcInstance)
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
			}
			broadcastPacket(new MagicSkillCanceled(getObjectId()));
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public void addAttackerToAttackByList(L2Character player)
	{
		if (player == null || player == this || getAttackByList() == null || getAttackByList().contains(player))
			return;
		getAttackByList().add(player);
	}

	public synchronized void addChanceEffect(L2Skill skill)
	{
		if (_chanceSkills == null)
		{
			_chanceSkills = new ChanceSkillList(this);
		}
		ChanceCondition ck = new ChanceCondition(ChanceCondition.TriggerType.ON_EXIT, 100);
		_chanceSkills.put(skill, ck);
	}

	public synchronized void addChanceSkill(L2Skill skill)
	{
		if (_chanceSkills == null)
		{
			_chanceSkills = new ChanceSkillList(this);
		}
		_chanceSkills.put(skill, skill.getChanceCondition());
	}

	public void addEffect(L2Effect newEffect)
	{
		_effects.addEffect(newEffect);
	}

	public void addExpAndSp(long addToExp, int addToSp)
	{
	}

	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;
		_NotifyQuestOfDeathList.add(qs);
	}

	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;

		if (newSkill != null)
		{
			synchronized (_skills)
			{
				oldSkill = _skills.put(newSkill.getId(), newSkill);
			}
			if (oldSkill != null)
			{
				if ((oldSkill.bestowTriggered() || oldSkill.triggerAnotherSkill()) && oldSkill.getTriggeredId() > 0)
				{
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}

			if (newSkill.getSkillType() != L2SkillType.NOTDONE)
			{
				addStatFuncs(newSkill.getStatFuncs(null, this));
			}

			try
			{
				if (newSkill.getElement() > -1)
				{
					getStat().addElement(newSkill);
				}
			}
			catch (Exception e)
			{

			}

			if (oldSkill != null && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (newSkill.isChance())
			{
				addChanceSkill(newSkill);
			}

			if (!newSkill.isChance() && newSkill.getTriggeredId() > 0 && newSkill.bestowTriggered())
			{
				L2Skill bestowed = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(bestowed);
			}

			if (newSkill.isChance() && newSkill.getTriggeredId() > 0 && !newSkill.bestowTriggered() && newSkill.triggerAnotherSkill())
			{
				L2Skill triggeredSkill = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(triggeredSkill);
			}
		}

		return oldSkill;
	}

	public final void addStatFunc(Func f)
	{
		if (f == null)
			return;

		synchronized (_calculators)
		{
			if (_calculators == NPC_STD_CALCULATOR)
			{
				_calculators = new Calculator[Stats.NUM_STATS];

				for (int i = 0; i < Stats.NUM_STATS; i++)
					if (NPC_STD_CALCULATOR[i] != null)
					{
						_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
					}
			}

			int stat = f.stat.ordinal();

			if (_calculators[stat] == null)
			{
				_calculators[stat] = new Calculator();
			}

			_calculators[stat].addFunc(f);

			if (this instanceof L2PcInstance)
			{
				((L2PcInstance) this).onFuncAddition(f);
			}
		}

		broadcastFullInfo();
	}

	public final void addStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
		{
			addStatFunc(f);
		}
	}

	public final void addStatFuncs(Iterable<Func> funcs)
	{
		for (Func f : funcs)
		{
			addStatFunc(f);
		}
	}

	public void addTimeStamp(int skill, int reuse)
	{

	}

	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		L2Character target = null;
		if (isAlikeDead() && skill.getTargetType() != SkillTargetType.TARGET_SELF)
			return;
		if (isTeleporting())
			return;
		if (this instanceof L2Boss && getCurrentMp() < Config.RAID_MIN_MP_TO_CAST)
			return;
		if (_event != null && !_event.canUseSkill(this, skill))
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			sendPacket(ActionFailed.STATIC_PACKET);
			getAI().setIntention(ACTIVE);
			return;
		}
		if (skill.useSoulShot())
			if (this instanceof L2Npc)
			{
				((L2Npc) this).rechargeAutoSoulShot(true, false);
			}

		L2Character[] targets = skill.getTargetList(this);

		if (skill.isPotion())
		{
			target = this;
		}
		else
		{
			switch (skill.getTargetType())
			{
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_GROUND:
				{
					target = this;
					break;
				}
				default:
				{
					if (targets == null || targets.length == 0)
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else
						{
							setIsCastingNow(false);
						}

						if (this instanceof L2PcInstance)
						{
							sendPacket(ActionFailed.STATIC_PACKET);
							getAI().setIntention(ACTIVE);
						}
						return;
					}

					switch (skill.getSkillType())
					{
						case BUFF:
						case HEAL:
						case COMBATPOINTHEAL:
						case MANAHEAL:
						case REFLECT:
							target = targets[0];
							break;
						default:
						{
							switch (skill.getTargetType())
							{
								case TARGET_SELF:
								case TARGET_PET:
								case TARGET_SUMMON:
								case TARGET_PARTY:
								case TARGET_CLAN:
								case TARGET_CORPSE_CLAN:
								case TARGET_ALLY:
								case TARGET_ENEMY_ALLY:
									target = targets[0];
									break;
								case TARGET_OWNER_PET:
									if (this instanceof L2PetInstance)
									{
										target = ((L2PetInstance) this).getOwner();
									}
									break;
								default:
								{
									target = (L2Character) getTarget();
									break;
								}
							}
						}
					}
				}
			}
		}

		if (target == null)
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}

			if (this instanceof L2PcInstance)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(ACTIVE);
			}
			return;
		}
		if (!checkDoCastConditions(skill, target))
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			if (this instanceof L2PcInstance)
			{
				getAI().setIntention(ACTIVE);
			}
			return;
		}
		setAttackingChar(this);

		int magicId = skill.getId();
		int displayId = skill.getDisplayId();
		int level = skill.getLevel();
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		int skillInterruptTime = skill.getSkillInterruptTime();
		boolean effectWhileCasting = skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME;

		if (level < 1)
		{
			level = 1;
		}

		if (!effectWhileCasting && !skill.isStaticHitTime())
		{
			hitTime = Formulas.calcAtkSpd(this, skill, hitTime);
			if (coolTime > 0)
			{
				coolTime = Formulas.calcAtkSpd(this, skill, coolTime);
			}
		}

		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null && skill.isMagic() && !effectWhileCasting && skill.getTargetType() != SkillTargetType.TARGET_SELF)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT || weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
				skillInterruptTime = (int) (0.70 * skillInterruptTime);

				switch (skill.getSkillType())
				{
					case BUFF:
					case MANAHEAL:
					case RESURRECT:
					case RECALL:
					case DOT:
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						break;
				}
			}
		}
		else if ((this instanceof L2Summon || this instanceof L2BabyPetInstance) && (((L2Summon) this).getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT || ((L2Summon) this).getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT))
		{
			hitTime = (int) (0.70 * hitTime);
			coolTime = (int) (0.70 * coolTime);
			skillInterruptTime = (int) (0.70 * skillInterruptTime);
			switch (skill.getSkillType())
			{
				case BUFF:
				case MANAHEAL:
				case RESURRECT:
				case RECALL:
				case DOT:
					((L2Summon) this).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
					break;
			}
		}
		else if (this instanceof L2Npc && skill.useSpiritShot() && !effectWhileCasting)
			if (((L2Npc) this).rechargeAutoSoulShot(false, true))
			{
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
				skillInterruptTime = (int) (0.70 * skillInterruptTime);
			}

		if (skill.isStaticHitTime())
		{
			hitTime = skill.getHitTime();
			coolTime = skill.getCoolTime();
		}
		else if (skill.getHitTime() >= Config.HIT_TIME_LIMITER && hitTime < Config.HIT_TIME_LIMITER)
		{
			hitTime = Config.HIT_TIME_LIMITER;
		}

		if (isCastingSimultaneouslyNow() && simultaneously)
		{
			ThreadPoolManager.getInstance().scheduleAi(new UsePotionTask(this, skill), 100);
			return;
		}

		if (simultaneously)
		{
			setIsCastingSimultaneouslyNow(true);
		}
		else
		{
			setIsCastingNow(true);
		}

		if (!simultaneously)
		{
			_castInterruptTime = GameTimeController.getGameTicks() + skillInterruptTime / GameTimeController.MILLIS_IN_TICK;
		}
		else
		{
			setLastSimultaneousSkillCast(skill);
		}

		int reuseDelay = skill.getReuseDelay();

		boolean skillMastery = Formulas.calcSkillMastery(this, skill);
		if (skillMastery)
		{
			reuseDelay = 0;
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
		}
		else if (!skill.isStaticReuse() && !skill.isPotion())
		{
			if (skill.isMagic())
			{
				reuseDelay *= getStat().getMReuseRate(skill);
			}
			else
			{
				reuseDelay *= getStat().getPReuseRate(skill);
			}

			reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
		}

		if (reuseDelay > 30000)
		{
			addTimeStamp(skill.getId(), reuseDelay);
		}

		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			StatusUpdate su = new StatusUpdate(this);
			if (skill.isDance() || skill.isSong())
			{
				getStatus().reduceMp(calcStat(Stats.DANCE_CONSUME_RATE, initmpcons, null, null));
			}
			else if (skill.isMagic())
			{
				getStatus().reduceMp(calcStat(Stats.MAGIC_CONSUME_RATE, initmpcons, null, null));
			}
			else
			{
				getStatus().reduceMp(calcStat(Stats.PHYSICAL_CONSUME_RATE, initmpcons, null, null));
			}
			su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
			sendPacket(su);
		}
		if (reuseDelay > 10)
		{
			disableSkill(skill.getId(), reuseDelay);
		}

		if (target != this)
		{
			setHeading(Util.calculateHeadingFrom(this, target));
		}

		if (effectWhileCasting)
		{
			if (skill.getItemConsume() > 0)
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, false))
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					if (this instanceof L2PcInstance)
					{
						getAI().setIntention(ACTIVE);
					}
					return;
				}

			if (this instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) this;

				if (skill.getNeededCharges() > 0 && !(skill instanceof L2SkillChargeDmg) && skill.getConsumeCharges())
				{
					player.decreaseCharges(skill.getNeededCharges());
				}
			}

			if (skill.getSkillType() == L2SkillType.FUSION)
			{
				startFusionSkill(target, skill);
			}
			else
			{
				callSkill(skill, targets);
			}
		}

		broadcastPacket(new MagicSkillLaunched(this, magicId, level, skill.isPositive(), targets));

		broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay, skill.isPositive()));

		if (this instanceof L2PcInstance)
		{
			long protTime = hitTime + coolTime;

			if (reuseDelay < protTime)
			{
				protTime /= 2;
			}

			((L2PcInstance) this).setSkillQueueProtectionTime(System.currentTimeMillis() + protTime);
		}

		if (this instanceof L2PcInstance)
		{
			switch (magicId)
			{
				case 1312: // Fishing
				{
					break;
				}
				case 2046: // Wolf Collar
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMON_A_PET));
					break;
				}
				default:
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_S1).addSkillName(skill));
				}
			}
		}

		switch (skill.getTargetType())
		{
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			{
				if (targets.length == 0)
				{

				}
				break;
			}
			default:
				break;
		}
		if (skill.getFlyType() != null && this instanceof L2PcInstance)
		{
			ThreadPoolManager.getInstance().scheduleEffect(new FlyToLocationTask(this, target, skill), 50);
		}

		if (hitTime > 210)
		{
			if (this instanceof L2PcInstance && !effectWhileCasting)
			{
				sendPacket(new SetupGauge(SetupGauge.BLUE, hitTime));
			}

			if (simultaneously)
			{
				if (_skillCast2 != null)
				{
					_skillCast2.cancel(true);
					_skillCast2 = null;
				}
				if (effectWhileCasting)
				{
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, simultaneously), hitTime);
				}
				else
				{
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1, simultaneously), hitTime - 200);
				}
			}
			else
			{
				if (_skillCast != null)
				{
					_skillCast.cancel(true);
					_skillCast = null;
				}
				if (effectWhileCasting)
				{
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, simultaneously), hitTime);
				}
				else
				{
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1, simultaneously), hitTime - 200);
				}
			}
		}
		else
		{
			onMagicLaunchedTimer(targets, skill, coolTime, true, simultaneously);
		}
	}

	public void breakAttack()
	{
		if (isAttackingNow())
		{
			abortAttack();

			if (this instanceof L2PcInstance)
			{
				sendPacket(ActionFailed.STATIC_PACKET);

				sendPacket(SystemMessageId.ATTACK_FAILED);
			}
		}
	}

	public void breakCast()
	{
		if (isCastingNow() && canAbortCast())
		{
			abortCast();

			if (this instanceof L2PcInstance)
			{
				sendPacket(SystemMessageId.CASTING_INTERRUPTED);
			}
		}
	}

	public final void broadcastFullInfo()
	{
		broadcastFullInfoImpl();
	}

	public abstract void broadcastFullInfoImpl();

	public final void broadcastPacket(L2GameServerPacket mov)
	{
		Broadcast.toSelfAndKnownPlayers(this, mov);
	}

	public final void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(this, mov, radiusInKnownlist);
	}

	public final void broadcastStatusUpdate()
	{
		broadcastStatusUpdateImpl();
	}

	public void broadcastStatusUpdateImpl()
	{

		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
		broadcastPacket(su);
	}

	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}

	public int calcHeading(Location dest)
	{
		if (dest == null)
			return 0;
		return calcHeading(dest.getX(), dest.getY());
	}

	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}

	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if (weapon == null)
			return 0;

		double reuse = weapon.getAttackReuseDelay();

		if (reuse == 0)
			return 0;

		reuse = getBowReuse(reuse) * 333;

		return Formulas.calcPAtkSpd(this, target, getPAtkSpd(), reuse);
	}

	protected void calculateRewards(L2Character killer)
	{

	}

	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		return Formulas.calcPAtkSpd(this, target, getPAtkSpd(), 500000);
	}

	public void callSkill(L2Skill skill, L2Character... targets)
	{
		L2Weapon activeWeapon = getActiveWeaponItem();
		L2PcInstance player = getActingPlayer();

		for (L2Object trg : targets)
		{
			if (trg == null)
			{
				continue;
			}
			if (Config.SIEGE_ONLY_REGISTERED && player != null && trg instanceof L2PcInstance)
				if (!((L2PcInstance) trg).canBeTargetedByAtSiege(player))
					return;

			if (trg instanceof L2Character)
			{

				L2Character target = (L2Character) trg;
				if (target.isInFunEvent())
				{
					target._event.onSkillHit(this, target, skill);
				}

				L2Character targetsAttackTarget = target.getAI().getAttackTarget();
				L2Character targetsCastTarget = target.getAI().getCastTarget();

				if (!Config.ALT_DISABLE_RAIDBOSS_PETRIFICATION && (target.isRaid() && getLevel() > target.getLevel() + 8 && target.getLevel() <= Config.MAX_LEVEL_RAID_CURSE || !skill.isOffensive() && targetsAttackTarget != null && targetsAttackTarget.isRaid() && targetsAttackTarget.getAttackByList().contains(target) && getLevel() > targetsAttackTarget.getLevel() + 8 && targetsAttackTarget.getLevel() <= Config.MAX_LEVEL_RAID_CURSE || !skill.isOffensive() && targetsCastTarget != null && targetsCastTarget.isRaid() && targetsCastTarget.getAttackByList().contains(target) && getLevel() > targetsCastTarget.getLevel() + 8 && targetsCastTarget.getLevel() <= Config.MAX_LEVEL_RAID_CURSE))
				{
					if (skill.isMagic())
					{
						L2Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
						if (tempSkill != null)
						{
							if (target != target.getActingPlayer())
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.IDLE);
								tempSkill.getEffects(target, this);
							}
							tempSkill.getEffects(target.getActingPlayer(), this);
						}
					}
					else
					{
						L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
						if (tempSkill != null)
						{
							if (target != target.getActingPlayer())
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.IDLE);
								tempSkill.getEffects(target, this);
							}
							tempSkill.getEffects(target.getActingPlayer(), this);
						}
					}
					return;
				}

				if (skill.isOverhit())
					if (target instanceof L2Attackable)
					{
						((L2Attackable) target).overhitEnabled(true);
					}

				if (activeWeapon != null && !target.isDead())
					if (activeWeapon.getSkillEffectsByCast(this, target, skill) && this instanceof L2PcInstance)
					{
						sendMessage(Message.getMessage((L2PcInstance) this, Message.MessageId.MSG_TARGET_RECIVE_SPECIAL_EFFECT));
					}

				if (_chanceSkills != null)
				{
					_chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
				}
				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
				}
			}
		}

		SkillHandler.getInstance().getSkillHandler(skill.getSkillType()).useSkill(this, skill, targets);

		if (player != null)
		{
			for (L2Object target : targets)
				if (target instanceof L2Character)
					if (skill.getSkillType() != L2SkillType.AGGREMOVE && skill.getSkillType() != L2SkillType.AGGREDUCE && skill.getSkillType() != L2SkillType.AGGREDUCE_CHAR)
						if (skill.isNeutral())
						{
						}
						else if (skill.isOffensive())
						{
							if (target instanceof L2PcInstance || target instanceof L2Summon)
							{
								if (skill.getSkillType() != L2SkillType.SIGNET && skill.getSkillType() != L2SkillType.SIGNET_CASTTIME)
								{
									if (skill.getSkillType() != L2SkillType.AGGREDUCE && skill.getSkillType() != L2SkillType.AGGREDUCE_CHAR && skill.getSkillType() != L2SkillType.AGGREMOVE)
									{
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
									}

									if (target instanceof L2PcInstance)
									{
										((L2PcInstance) target).getAI().clientStartAutoAttack();
									}
									else if (target instanceof L2Summon)
									{
										L2PcInstance owner = ((L2Summon) target).getOwner();
										if (owner != null)
										{
											owner.getAI().clientStartAutoAttack();
										}
									}

									if (!(target instanceof L2Summon) || player.getPet() != target)
									{
										player.updatePvPStatus(target.getActingPlayer());
									}
								}
							}
							else if (target instanceof L2Attackable)
								if (skill.getSkillType() != L2SkillType.AGGREDUCE && skill.getSkillType() != L2SkillType.AGGREDUCE_CHAR && skill.getSkillType() != L2SkillType.AGGREMOVE)
								{
									switch (skill.getId())
									{
										case 51:
										case 511:
											break;
										default:
											((L2Character) target).addAttackerToAttackByList(this);
											((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
									}
								}
						}
						else if (target instanceof L2PcInstance)
						{
							if (target != this && (((L2PcInstance) target).getPvpFlag() > 0 || ((L2PcInstance) target).getKarma() > 0))
							{
								player.updatePvPStatus();
							}
						}
						else if (target instanceof L2Attackable && !(skill.getSkillType() == L2SkillType.SUMMON) && !(skill.getSkillType() == L2SkillType.BEAST_FEED) && !(skill.getSkillType() == L2SkillType.UNLOCK) && !(skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK) && !(skill.getSkillType() == L2SkillType.HEAL_MOB) && !(skill.getSkillType() == L2SkillType.MAKE_KILLABLE) && (!(target instanceof L2Summon) || player.getPet() != target))
						{
							player.updatePvPStatus();
						}

			for (L2Object spMob : player.getKnownList().getKnownObjects().values())
				if (spMob instanceof L2Npc)
				{
					L2Npc npcMob = (L2Npc) spMob;

					if (npcMob.isInsideRadius(player, 1000, true, true) && npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
					{
						for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
						{
							quest.notifySkillSee(npcMob, player, skill, targets, this instanceof L2Summon);
						}
					}

					if (skill.getAggroPoints() > 0)
						if (npcMob.isInsideRadius(player, 1000, true, true) && npcMob.hasAI() && npcMob.getAI().getIntention() == ATTACK)
						{
							L2Object npcTarget = npcMob.getTarget();
							for (L2Object target : targets)
								if (npcTarget == target || npcMob == target)
								{
									npcMob.seeSpell(player, target, skill);
								}
						}
				}
		}
	}

	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}

	public boolean charIsGM()
	{
		if (this instanceof L2PcInstance)
			if (((L2PcInstance) this).isGM())
				return true;

		return false;
	}

	protected boolean checkAndEquipArrows()
	{
		return true;
	}


	private boolean checkDoCastConditions(L2Skill skill, L2Object target)
	{
		if (skill == null || isSkillDisabled(skill.getId()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (target != null && target != this && skill.getTargetType() == SkillTargetType.TARGET_ONE && !GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if (this instanceof L2PcInstance && !skill.checkCondition(this, target))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (getStatus().getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			if (this instanceof L2PcInstance)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_MP);

				sendPacket(ActionFailed.STATIC_PACKET);
			}
			return false;
		}

		if (getStatus().getCurrentHp() <= skill.getHpConsume())
		{
			if (this instanceof L2PcInstance)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_HP);

				sendPacket(ActionFailed.STATIC_PACKET);
			}
			return false;
		}

		switch (skill.getSkillType())
		{
			case SUMMON:
			{
				if (!skill.isCubic() && this instanceof L2PcInstance && (getPet() != null || ((L2PcInstance) this).isMounted()))
				{
					sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
					return false;
				}
			}
		}

		if (!skill.isPotion())
			if (skill.isMagic())
			{
				if (isMuted())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if (isPhysicalMuted())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (isPhysicalAttackMuted())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

		if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
				return false;
			boolean canCast = true;
			if (skill.getTargetType() == SkillTargetType.TARGET_GROUND && this instanceof L2PcInstance)
			{
				Point3D wp = ((L2PcInstance) this).getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
				{
					canCast = false;
				}
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
			{
				canCast = false;
			}
			if (!canCast)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}

		if (!skill.getWeaponDependancy(this, true))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (skill.getItemConsume() > 0 && getInventory() != null)
		{
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());

			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1).addItemName(skill.getItemConsumeId()).addNumber(skill.getItemConsume()));
					return false;
				}

				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return false;
			}
		}
		return true;
	}

	private void consume(L2Skill skill)
	{
		StatusUpdate su = new StatusUpdate(this);
		boolean isSendStatus = false;
		double mpConsume = getStat().getMpConsume(skill);

		if (mpConsume > 0)
		{
			if (skill.isDance() || skill.isSong())
			{
				getStatus().reduceMp(calcStat(Stats.DANCE_CONSUME_RATE, mpConsume, null, null));
			}
			else if (skill.isMagic())
			{
				getStatus().reduceMp(calcStat(Stats.MAGIC_CONSUME_RATE, mpConsume, null, null));
			}
			else
			{
				getStatus().reduceMp(calcStat(Stats.PHYSICAL_CONSUME_RATE, mpConsume, null, null));
			}
			su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
			isSendStatus = true;
		}

		if (skill.getHpConsume() > 0)
		{
			double consumeHp;

			consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
			if (consumeHp + 1 >= getStatus().getCurrentHp())
			{
				consumeHp = getStatus().getCurrentHp() - 1.0;
			}

			getStatus().reduceHp(consumeHp, this);

			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			isSendStatus = true;
		}

		if (skill.getCpConsume() > 0)
		{
			double consumeCp;

			consumeCp = skill.getCpConsume();
			if (consumeCp + 1 >= getStatus().getCurrentHp())
			{
				consumeCp = getStatus().getCurrentHp() - 1.0;
			}

			getStatus().reduceCp((int) consumeCp);

			su.addAttribute(StatusUpdate.CUR_CP, (int) getStatus().getCurrentCp());
			isSendStatus = true;
		}

		if (isSendStatus)
		{
			sendPacket(su);
		}

	}

	public final L2Zone[] currentZones()
	{
		return _currentZonesInstances.toArray(new L2Zone[_currentZonesInstances.size()]);
	}

	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		return true;
	}

	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		return true;
	}

	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}

	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}

	public synchronized void disableSkill(int skillId)
	{
		if (_disabledSkills == null)
		{
			_disabledSkills = new SingletonSet<>();
		}
		try
		{
			_disabledSkills.add(skillId);
		}
		catch (NullPointerException e)
		{

		}
	}

	public void disableSkill(int skillId, long delay)
	{
		disableSkill(skillId);
		if (delay > 10)
		{
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay);
		}
	}

	protected void doAttack(L2Character target)
	{
		if (isAlikeDead() || target == null || this instanceof L2Npc && target.isAlikeDead() || this instanceof L2PcInstance && target.isDead() && !target.isFakeDeath())
		{
			getAI().setIntention(CtrlIntention.ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (isAttackingDisabled())
			return;

		if (!(target instanceof L2DoorInstance) && !GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			getAI().setIntention(CtrlIntention.ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (_event != null && target._event == _event && _event.isRunning() && _event.canAttack(this, target))
		{

		}
		else if (this instanceof L2PcInstance)
		{
			if (((L2PcInstance) this).isMounted() && ((L2PcInstance) this).getMountNpcId() == 12621)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (((L2PcInstance) this).inObserverMode())
			{
				sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isCursedWeaponEquipped() && getLevel() <= 20)
				{
					sendMessage(Message.getMessage((L2PcInstance) this, Message.MessageId.MSG_CW_YOUR_LVL_LOW));
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (((L2PcInstance) this).isCursedWeaponEquipped() && target.getLevel() <= 20)
				{
					sendMessage(Message.getMessage((L2PcInstance) this, Message.MessageId.MSG_CW_TARGET_LVL_LOW));
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (target.isInsidePeaceZone((L2PcInstance) this))
				{
					getAI().setIntention(CtrlIntention.ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (Config.ALLOW_OFFLINE_TRADE_PROTECTION && ((L2PcInstance) target).isOfflineTrade())
				{
					getAI().setIntention(CtrlIntention.ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (isInsidePeaceZone(this, target))
			{
				getAI().setIntention(CtrlIntention.ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null && weaponInst.getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD)
		{
			sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
			getAI().setIntention(CtrlIntention.IDLE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (weaponItem != null)
			if (weaponItem.getItemType() == L2WeaponType.BOW)
				if (this instanceof L2PcInstance)
				{
					if (target.isInsidePeaceZone((L2PcInstance) this))
					{
						getAI().setIntention(CtrlIntention.ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
					{
						int saMpConsume = (int) getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
						int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;

						if (getStatus().getCurrentMp() < mpConsume)
						{
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						getStatus().reduceMp(mpConsume);
						_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					if (!checkAndEquipArrows())
					{
						getAI().setIntention(CtrlIntention.IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessageId.NOT_ENOUGH_ARROWS);
						return;
					}
				}
				else if (this instanceof L2Npc)
					if (_disableBowAttackEndTime > GameTimeController.getGameTicks())
						return;

		target.getKnownList().addKnownObject(this);

		if (Config.ALT_GAME_TIREDNESS)
		{
			getStatus().setCurrentCp(getStatus().getCurrentCp() - 10);
		}

		boolean wasSSCharged;
		if (this instanceof L2Npc)
		{
			try
			{
				wasSSCharged = ((L2Npc) this).rechargeAutoSoulShot(true, false);
			}
			catch (Exception e)
			{
				wasSSCharged = false;
			}
		}
		else if (this instanceof L2Summon && !(this instanceof L2PetInstance) || this instanceof L2BabyPetInstance)
		{
			wasSSCharged = ((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE;
		}
		else
		{
			wasSSCharged = weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE;
		}

		int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		int timeToHit = timeAtk / 2;
		int reuse = calculateReuseTime(target, weaponItem);

		_attackEndTime = L2System.milliTime() + timeAtk;

		int ssGrade = 0;

		if (weaponItem != null)
		{
			ssGrade = weaponItem.getCrystalType();
			if (ssGrade == 7)
			{
				ssGrade = 6;
			}
		}

		Attack attack = new Attack(this, target, wasSSCharged, ssGrade);
		setAttackingBodypart();
		setHeading(Util.calculateHeadingFrom(this, target));

		boolean hitted;
		if (weaponItem == null)
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, target, timeToHit);
		}
		else if (isUsingDualWeapon())
		{
			hitted = doAttackHitByDual(attack, target, timeToHit);
		}
		else
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}

		L2PcInstance player = getActingPlayer();

		if (player != null && player.getPet() != target)
		{
			player.updatePvPStatus(target);
		}
		if (!hitted)
		{
			abortAttack();
		}
		else
		{
			if (_chanceSkills != null)
			{
				_chanceSkills.onAttack(target);
			}

			if (this instanceof L2Summon && !(this instanceof L2PetInstance) || this instanceof L2BabyPetInstance)
			{
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst != null)
			{
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, false);
			}

			if (player != null)
				if (player.isCursedWeaponEquipped())
				{
					if (!target.isInvul())
					{
						target.getStatus().setCurrentCp(0);
					}
				}
				else if (player.isHero())
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
					{
						target.getStatus().setCurrentCp(0);
					}
		}

		if (attack.hasHits())
		{
			broadcastPacket(attack);
		}

		if (this instanceof L2PcInstance && target instanceof L2PcInstance && !target.isAutoAttackable(this))
		{
			((L2PcInstance) this).getAI().clientStopAutoAttack();
			((L2PcInstance) this).getAI().setIntention(CtrlIntention.IDLE, this);
		}

		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}

	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		boolean miss1 = Formulas.calcHitMiss(this, target);
		if (miss1)
		{
			sendPacket(SystemMessageId.MISSED_TARGET);
		}

		reduceArrowCount(false);
		_move = null;

		if (!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));

			if (crit1 == true)
				if (target instanceof L2Attackable)
				{
					target.setCriticalDmg(true);
				}
				else
				{
					target.setCriticalDmg(false);
				}

			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}

		if (this instanceof L2PcInstance)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));
			sendPacket(new SetupGauge(SetupGauge.RED, sAtk + reuse));
		}

		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		if (!miss1)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoSS(this), sAtk + 10);
		}
		_disableBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks() - 1;

		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		return !miss1;
	}

	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;

		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		if (miss1)
		{
			sendPacket(SystemMessageId.MISSED_TARGET);
		}
		if (miss2)
		{
			sendPacket(SystemMessageId.MISSED_TARGET);
		}

		if (!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));

			if (crit1 == true)
				if (target instanceof L2Attackable)
				{
					target.setCriticalDmg(true);
				}
				else
				{
					target.setCriticalDmg(false);
				}

			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
			damage1 /= 2;
		}

		if (!miss2)
		{
			shld2 = Formulas.calcShldUse(this, target);
			crit2 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
			damage2 /= 2;
		}

		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);
		if (!miss1 || !miss2)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoSS(this), sAtk + 10);
		}

		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1), attack.createHit(target, damage2, miss2, crit2, shld2));
		return !miss1 || !miss2;
	}

	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		double angleChar;
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);

		angleChar = Util.convertHeadingToDegree(getHeading());
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 3, null, null) - 1;
		int attackcount = 0;

		if (angleChar <= 0)
		{
			angleChar += 360;
		}

		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;
		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj == target)
			{
				continue;
			}

			if (obj instanceof L2Character)
			{
				if (obj instanceof L2PetInstance && this instanceof L2PcInstance && ((L2PetInstance) obj).getOwner() == this)
				{
					continue;
				}

				if (!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}

				if (!GeoData.getInstance().canSeeTarget(this, obj))
				{
					continue;
				}

				if (Math.abs(obj.getZ() - getZ()) > 650)
				{
					continue;
				}

				if (!isFacing(obj, maxAngleDiff))
				{
					continue;
				}

				temp = (L2Character) obj;

				if (!temp.isAlikeDead())
				{
					attackcount += 1;
					if (attackcount <= attackRandomCountMax)
						if (temp == getAI().getAttackTarget() || temp.isAutoAttackable(this))
						{
							hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
							attackpercent /= 1.15;
						}
				}
			}
		}
		return hitted;
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		boolean miss1 = Formulas.calcHitMiss(this, target);
		if (miss1)
		{
			sendPacket(SystemMessageId.MISSED_TARGET);
		}

		if (!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));

			if (crit1 == true)
				if (target instanceof L2Attackable)
				{
					target.setCriticalDmg(true);
				}
				else
				{
					target.setCriticalDmg(false);
				}

			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);

			if (attackpercent != 100)
			{
				damage1 = (int) (damage1 * attackpercent / 100);
			}
		}

		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		if (!miss1)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoSS(this), sAtk + 10);
		}

		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		return !miss1;
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}

	public void doCast(L2Skill skill)
	{
		beginCast(skill, false);
	}

	public boolean doDie(L2Character killer)
	{
		synchronized (this)
		{
			if (isDead())
				return false;

			getStatus().setCurrentHp(0);

			if (isFakeDeath())
			{
				stopFakeDeath(null);
			}

			setIsDead(true);
		}
		setTarget(null);
		stopMove(null);
		getStatus().stopHpMpRegeneration();

		if (this instanceof L2Playable)
		{
			L2Playable pl = (L2Playable) this;
			if (pl.isPhoenixBlessed())
			{
				if (pl.getCharmOfLuck())
				{
					pl.stopCharmOfLuck(null);
				}
				if (pl.isNoblesseBlessed())
				{
					pl.stopNoblesseBlessing(null);
				}
			}
			else if (pl.isNoblesseBlessed())
			{
				pl.stopNoblesseBlessing(null);
				if (pl.getCharmOfLuck())
				{
					pl.stopCharmOfLuck(null);
				}
			}
			else
			{
				stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		calculateRewards(killer);

		if (Config.BOTS_PREVENTION)
		{
			BotsPreventionManager.getInstance().updatecounter(killer, this);
		}

		if (this instanceof L2Npc)
		{
			if (((L2Npc) this).getNpcId() == 35368)
				if (FortResistSiegeManager.getInstance().getIsInProgress())
				{
					FortResistSiegeManager.getInstance().endSiege(true);
				}
			if (((L2Npc) this).getNpcId() == 35410)
				if (DevastatedCastleSiege.getInstance().getIsInProgress())
				{
					DevastatedCastleSiege.getInstance().endSiege(killer);
				}
			if (((L2Npc) this).getNpcId() == 35630)
				if (FortressOfDeadSiege.getInstance().getIsInProgress())
				{
					FortressOfDeadSiege.getInstance().endSiege(killer);
				}
		}
		broadcastStatusUpdate();

		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this);
		}

		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);

		for (QuestState qs : getNotifyQuestOfDeath())
		{
			qs.getQuest().notifyDeath(killer == null ? this : killer, this, qs);
		}

		getNotifyQuestOfDeath().clear();
		if (this instanceof L2Summon)
		{
			if (((L2Summon) this).isPhoenixBlessed() && ((L2Summon) this).getOwner() != null)
			{
				((L2Summon) this).getOwner().revivePetRequest(((L2Summon) this).getOwner(), null);
			}
		}
		else if (this instanceof L2PcInstance)
			if (((L2Playable) this).isPhoenixBlessed())
			{
				((L2PcInstance) this).reviveRequest((L2PcInstance) this, null);
			}
			else if (((L2PcInstance) this).getCharmOfCourage() && isInsideZone(L2Zone.FLAG_SIEGE) && ((L2PcInstance) this).getSiegeState() != 0)
				if (((L2PcInstance) this).getCanUseCharmOfCourageRes())
				{
					((L2PcInstance) this).reviveRequest((L2PcInstance) this, null);
					((L2PcInstance) this).setCanUseCharmOfCourageRes(false);
				}
		if (isInsideZone(L2Zone.FLAG_SIEGE))
		{
			if (this instanceof L2PcInstance && ((L2PcInstance) this).getSiegeState() != 0)
			{
				int playerClanId = ((L2PcInstance) this).getClanId();
				int playerCharId = this.getObjectId();
				L2SiegeStatus.getInstance().addStatus(playerClanId, playerCharId, false);
			}
			if (killer instanceof L2PcInstance && ((L2PcInstance) killer).getSiegeState() != 0)
			{
				int killerClanId = ((L2PcInstance) killer).getClanId();
				int killerCharId = killer.getObjectId();
				L2SiegeStatus.getInstance().addStatus(killerClanId, killerCharId, true);
			}
			if (killer instanceof L2Summon && ((L2Summon) killer).getOwner().getSiegeState() != 0)
			{
				int killerClanId = ((L2Summon) killer).getOwner().getClanId();
				int killerCharId = ((L2Summon) killer).getOwner().getObjectId();
				L2SiegeStatus.getInstance().addStatus(killerClanId, killerCharId, true);
			}
		}

		getAttackByList().clear();

		try
		{
			if (_fusionSkill != null)
			{
				abortCast();
			}

			for (L2Character character : getKnownList().getKnownCharacters())
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
				{
					character.abortCast();
				}
		}
		catch (Exception e)
		{

		}

		return true;
	}

	private void doFallDamage(int fallHeight)
	{
		isFalling(false, 0);

		if (isInvul() || this instanceof L2PcInstance)
		{
			setIsFallsdown(false);
			return;
		}

		int damage = getFallDamage(fallHeight);

		if (damage < 1)
			return;

		if (this instanceof L2PcInstance)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
		}

		getStatus().reduceHp(damage, this);
		getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
	}

	public void doRevive()
	{
		if (!isDead())
			return;
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsDead(false);

			boolean restorefull = false;

			if (this instanceof L2Playable && ((L2Playable) this).isPhoenixBlessed())
			{
				restorefull = true;
				((L2Playable) this).stopPhoenixBlessing(null);
			}

			if (restorefull)
			{
				_status.setCurrentCp(getMaxCp());
				_status.setCurrentHp(getMaxHp());
				_status.setCurrentMp(getMaxMp());
			}
			else
			{
				_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
			}

			broadcastPacket(new Revive(this));

			// Start paralyze task if it's a player
			if (this instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance) this;

				// Schedule a paralyzed task to wait for the animation to finish
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						player.setIsParalyzed(false);
					}
				}, player.getAnimationTimer());
				setIsParalyzed(true);
			}

			if (getWorldRegion() != null)
			{
				getWorldRegion().onRevive(this);
			}
		}
		else
		{
			setIsPendingRevive(true);
		}
	}

	public void doRevive(double revivePower)
	{
		doRevive();
	}

	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}

	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}

	public synchronized void enableSkill(int skillId)
	{
		if (_disabledSkills == null)
			return;

		_disabledSkills.remove(Integer.valueOf(skillId));

		if (this instanceof L2PcInstance)
		{
			removeTimeStamp(skillId);
		}
	}

	private int fallSafeHeight()
	{

		int safeFallHeight = Config.ALT_MINIMUM_FALL_HEIGHT;

		try
		{
			if (this instanceof L2PcInstance)
			{
				safeFallHeight = ((L2PcInstance) this).getTemplate().getBaseFallSafeHeight(((L2PcInstance) this).getAppearance().getSex());
			}
		}
		catch (Exception e)
		{

		}
		return safeFallHeight;
	}

	public void finishMovement()
	{
		_isMoving = false;
	}

	public final void forceIsCasting(int newSkillCastEndTick)
	{
		setIsCastingNow(true);
		_castInterruptTime = newSkillCastEndTick - 2;
	}

	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (isStunned())
		{
			ae |= AbnormalEffect.STUN.getMask();
		}
		if (isRooted())
		{
			ae |= AbnormalEffect.ROOT.getMask();
		}
		if (isSleeping())
		{
			ae |= AbnormalEffect.SLEEP.getMask();
		}
		if (isConfused())
		{
			ae |= AbnormalEffect.CONFUSED.getMask();
		}
		if (isMuted())
		{
			ae |= AbnormalEffect.MUTED.getMask();
		}
		if (isPhysicalMuted())
		{
			ae |= AbnormalEffect.MUTED.getMask();
		}
		return ae;
	}

	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}

	public boolean isInWater()
	{
		return isInsideZone(L2Zone.FLAG_WATER);
	}

	public abstract L2ItemInstance getActiveWeaponInstance();

	public abstract L2Weapon getActiveWeaponItem();

	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				_ai = new L2CharacterAI(new AIAccessor());
				return _ai;
			}
		}

		return ai;
	}

	public AIAccessor getAIAccessor()
	{
		return new AIAccessor();
	}

	public final L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}

	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];
		synchronized (_skills)
		{
			return _skills.values().toArray(new L2Skill[_skills.values().size()]);
		}
	}

	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}

	public final List<L2Character> getAttackByList()
	{
		if (_attackByList == null)
		{
			_attackByList = new SingletonList<>();
		}
		return _attackByList;
	}

	public long getAttackEndTime()
	{
		return _attackEndTime;
	}

	public final int getAttackingBodyPart()
	{
		return _attacking;
	}

	public final L2Character getAttackingChar()
	{
		return _attackingChar;
	}

	public final double getBowReuse(double reuse)
	{
		return calcStat(Stats.BOW_REUSE, reuse, null, null);
	}

	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}

	public final Calculator[] getCalculators()
	{
		return _calculators;
	}

	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}

	@Override
	public int getColHeight()
	{
		return getTemplate().getCollisionHeight();
	}

	@Override
	public int getColRadius()
	{
		return getTemplate() == null ? 50 : getTemplate().getCollisionRadius();
	}

	public int getCON()
	{
		return getStat().getCON();
	}

	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}

	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	public double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}

	public double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}

	public double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}

	public int getDanceCount(boolean dances, boolean songs)
	{
		return _effects.getDanceCount(dances, songs);
	}

	public int getDEX()
	{
		return getStat().getDEX();
	}

	@Deprecated
	public final double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return Math.sqrt(dx * dx + dy * dy);
	}

	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return dx * dx + dy * dy + dz * dz;
	}

	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}

	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}

	private int getFallDamage(int fallHeight)
	{
		int damage = (fallHeight - fallSafeHeight()) * 2;
		damage = (int) (damage / getStat().calcStat(Stats.FALL_VULN, 1, this, null));

		if (damage >= getStatus().getCurrentHp())
		{
			damage = (int) (getStatus().getCurrentHp() - 1);
		}

		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
		disableAllSkills();

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				L2Character.this.enableAllSkills();
				broadcastPacket(new ChangeWaitType(L2Character.this, ChangeWaitType.WT_STOP_FAKEDEATH));
				setIsFallsdown(false);

				lastPosition[0] = getPosition().getX();
				lastPosition[1] = getPosition().getY();
				lastPosition[2] = getPosition().getZ();
			}
		}, 1100);
		return damage;
	}

	public final L2Effect getFirstEffect(int index)
	{
		return _effects.getFirstEffect(index);
	}

	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}

	public final L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}

	public FusionSkill getFusionSkill()
	{
		return _fusionSkill;
	}

	@Override
	public final int getHeading()
	{
		return _heading;
	}

	public int getHeadingTo(L2Character target, boolean toChar)
	{
		if (target == null || target == this)
			return -1;

		int dx = target.getX() - getX();
		int dy = target.getY() - getY();
		int heading = (int) (Math.atan2(-dy, -dx) * 32768. / Math.PI);
		if (toChar)
		{
			heading = target.getHeading() - (heading + 32768);
		}
		else
		{
			heading = getHeading() - (heading + 32768);
		}

		if (heading < 0)
		{
			heading += 65536;
		}
		return heading;
	}

	public int getHealLimit()
	{
		return _healLimit;
	}

	public final int getINT()
	{
		return getStat().getINT();
	}

	public Inventory getInventory()
	{
		return null;
	}

	public int getInventoryLimit()
	{
		return 1000;
	}

	@Override
	public CharKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new CharKnownList(this);
		}

		return _knownList;
	}

	public final L2Skill getKnownSkill(int skillId)
	{
		if (_skills == null)
			return null;
		synchronized (_skills)
		{
			return _skills.get(skillId);
		}
	}

	public boolean getLastCriticalDmg()
	{
		return _lastHitIsCritical;
	}

	public int getLastHealAmount()
	{
		return _lastHealAmount;
	}

	public long getLastKnowUpdate()
	{
		return _lastKnowUpdate;
	}

	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}

	public abstract int getLevel();

	public double getLevelMod()
	{
		return 1;
	}

	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}

	public final int getMAtkSps(L2Character target, L2Skill skill)
	{
		int matk = (int) calcStat(Stats.MAGIC_ATTACK, _template.getBaseMAtk(), target, skill);
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				matk *= 4;
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				matk *= 2;
			}
		return matk;
	}

	public int getMaxBuffCount()
	{
		return Config.BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevel(L2Skill.SKILL_DIVINE_INSPIRATION));
	}

	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}

	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}

	public final int getMaxMp()
	{
		return getStat().getMaxMp();
	}

	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	public int getMEN()
	{
		return getStat().getMEN();
	}

	public final List<QuestState> getNotifyQuestOfDeath()
	{
		if (_NotifyQuestOfDeathList == null)
		{
			_NotifyQuestOfDeathList = new SingletonList<>();
		}

		return _NotifyQuestOfDeathList;
	}

	public L2Party getParty()
	{
		return null;
	}

	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}

	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}

	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}

	public L2Summon getPet()
	{
		return null;
	}

	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}

	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return dx * dx + dy * dy;
	}

	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}

	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}

	public final int getRandomDamage(L2Character target)
	{
		L2Weapon weaponItem = getActiveWeaponItem();

		if (weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());

		return weaponItem.getRandomDamage();
	}

	public final double getRangeToTarget(L2Object par)
	{
		return Math.sqrt(getPlanDistanceSq(par));
	}

	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}

	public abstract L2ItemInstance getSecondaryWeaponInstance();

	public abstract L2Weapon getSecondaryWeaponItem();

	public int getShldDef()
	{
		return getStat().getShldDef();
	}

	public int getSkillLevel(int skillId)
	{
		if (_skills == null)
			return -1;
		synchronized (_skills)
		{
			L2Skill skill = _skills.get(skillId);

			if (skill == null)
				return -1;
			return skill.getLevel();
		}
	}

	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}

	public CharStat getStat()
	{
		if (_stat == null)
		{
			_stat = new CharStat(this);
		}

		return _stat;
	}

	public CharStatus getStatus()
	{
		if (_status == null)
		{
			_status = new CharStatus(this);
		}

		return _status;
	}

	public int getSTR()
	{
		return getStat().getSTR();
	}

	public final L2Object getTarget()
	{
		return _target;
	}

	public final int getTargetId()
	{
		if (_target != null)
			return _target.getObjectId();

		return -1;
	}

	public int getTeam()
	{
		return _team;
	}

	public L2CharTemplate getTemplate()
	{
		return _template;
	}

	public String getTitle()
	{

		if (_event != null && _event.isRunning())
		{
			if (_eventTitle == null)
			{
				_eventTitle = _title;
			}
			return _eventTitle;
		}
		return _title;
	}

	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}

	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}

	public int getWIT()
	{
		return getStat().getWIT();
	}

	public final int getXdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._xDestination;

		return getX();
	}

	public final int getYdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._yDestination;

		return getY();
	}

	public final int getZdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._zDestination;

		return getZ();
	}

	public L2Zone getZone(String type)
	{
		for (L2Zone z : _currentZoneList)
			if (z.getClass().getSimpleName().equalsIgnoreCase("L2" + type + "Zone"))
				return z;
		return null;
	}

	public List<L2Zone> getZones()
	{
		return _currentZoneList;
	}

	public boolean hasAI()
	{
		return _ai != null;
	}

	public final boolean hasSkill(int skillId)
	{
		return getKnownSkill(skillId) != null;
	}

	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getMaxHp() / 352.0;
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateDecCheck = getMaxHp() - _hpUpdateInterval;
	}

	public final boolean isAfraid()
	{
		return _isAfraid;
	}

	public final boolean isAlikeDead()
	{
		return isFakeDeath() || _isDead;
	}

	public boolean isAllow(EffectTemplate effect, L2Skill skill)
	{
		return _effects.isPossible(effect, skill);
	}

	public boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isImmobileUntilAttacked() || isParalyzed() || isPetrified();
	}

	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}

	public boolean isAttackingDisabled()
	{
		return isStunned() || isSleeping() || isImmobileUntilAttacked() || isAttackingNow() || isFakeDeath() || isParalyzed() || isPetrified() || isFallsdown() || isPhysicalAttackMuted() || isCoreAIDisabled();
	}

	public boolean isAttackingNow()
	{
		return getAttackEndTime() > L2System.milliTime();
	}

	public boolean isBehind(L2Object target)
	{

		if (target == null)
			return false;

		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;

			double myHeading = Util.calculateAngleFrom(this, target1);
			double targetHeading = Util.convertHeadingToDegree(target1.getHeading());
			if (Math.abs(targetHeading - myHeading) <= 45)
				return true;
		}
		return false;
	}

	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}

	public final boolean isBetrayed()
	{
		return _isBetrayed;
	}

	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}

	public boolean isBoss()
	{
		return _isBoss;
	}

	public final boolean isCastingNow()
	{
		return _isCastingNow;
	}

	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}

	public boolean isChampion()
	{
		return _champion;
	}

	public final boolean isConfused()
	{
		return _isConfused;
	}

	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}

	public final boolean isDead()
	{
		return _isDead;
	}

	public final boolean isDisarmed()
	{
		return _isDisarmed;
	}

	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;

		if (target == null)
			return false;

		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - maxAngleDiff)
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}

	public final boolean isFakeDeath()
	{
		return _isFakeDeath;
	}

	public int isFalling(boolean falling, int fallHeight)
	{

		if (isFallsdown() && fallHeight == 0)
			return -1;

		if (!falling || lastPosition[0] == 0 && lastPosition[1] == 0 && lastPosition[2] == 0)
		{
			lastPosition[0] = getPosition().getX();
			lastPosition[1] = getPosition().getY();
			lastPosition[2] = getPosition().getZ();
			setIsFallsdown(false);
			return -1;
		}

		int moveChangeX = Math.abs(lastPosition[0] - getPosition().getX()), moveChangeY = Math.abs(lastPosition[1] - getPosition().getY()), moveChangeZ = Math.max(lastPosition[2] - getPosition().getZ(), lastPosition[2] - getZ());

		if (moveChangeZ > fallSafeHeight() && moveChangeY < moveChangeZ && moveChangeX < moveChangeZ && !isFlying())
		{

			setIsFallsdown(true);
			fallHeight += moveChangeZ;

			lastPosition[0] = getPosition().getX();
			lastPosition[1] = getPosition().getY();
			lastPosition[2] = getPosition().getZ();
			getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

			CheckFalling cf = new CheckFalling(fallHeight);
			Future<?> task = ThreadPoolManager.getInstance().scheduleGeneral(cf, Math.min(1200, moveChangeZ));
			cf.setTask(task);

			return fallHeight;
		}

		lastPosition[0] = getPosition().getX();
		lastPosition[1] = getPosition().getY();
		lastPosition[2] = getPosition().getZ();
		getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

		if (fallHeight > fallSafeHeight())
		{
			doFallDamage(fallHeight);
			return fallHeight;
		}

		return -1;
	}

	public final boolean isFallsdown()
	{
		return _isFallsdown;
	}

	/**
	 * Return True if the L2Character is flying.
	 * @return true, if is flying
	 */
	public final boolean isFlying()
	{
		return _isFlying;
	}

	public boolean isGrandBoss()
	{
		return _isGrandBoss;
	}

	public final boolean isImmobileUntilAttacked()
	{
		return _isImmobileUntilAttacked;
	}

	public boolean isImmobilized()
	{
		return _isImmobilized;
	}

	public boolean isInActiveRegion()
	{
		L2WorldRegion region = getWorldRegion();
		return region != null && region.isActive();
	}

	public boolean isInCombat()
	{
		return getAI().getAttackTarget() != null || getAI().isAutoAttacking();
	}

	public boolean isInFrontOf(L2Character target)
	{

		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;

		if (target == null)
			return false;

		angleTarget = Util.calculateAngleFrom(target, this);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - maxAngleDiff)
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}

	public boolean isInFrontOfTarget()
	{
		L2Object target = getTarget();
		if (target instanceof L2Character)
			return isInFrontOf((L2Character) target);

		return false;
	}

	public boolean isInParty()
	{
		return false;
	}

	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}

	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}

	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		if (strictCheck)
		{
			if (checkZ)
				return dx * dx + dy * dy + dz * dz < radius * radius;

			return dx * dx + dy * dy < radius * radius;
		}

		if (checkZ)
			return dx * dx + dy * dy + dz * dz <= radius * radius;

		return dx * dx + dy * dy <= radius * radius;
	}

	public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		if (object == null)
			return false;

		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}

	public boolean isInsideZone(byte zone)
	{
		return zone == L2Zone.FLAG_PVP ? _currentZones[L2Zone.FLAG_PVP] > 0 && _currentZones[L2Zone.FLAG_PEACE] == 0 : _currentZones[zone] > 0;
	}

	public final boolean isInsideZone(String zoneType)
	{
		for (L2Zone z : _currentZonesInstances)
			if (z.getTypeName().equals(zoneType))
				return true;
		return false;
	}

	public boolean isInvul()
	{
		return _isInvul || _isTeleporting;
	}

	public final boolean isLuckByNoblesse()
	{
		return _isLuckByNoblesse;
	}

	public boolean isMovementDisabled()
	{
		return isStunned() || isRooted() || isSleeping() || isTeleporting() || isImmobileUntilAttacked() || isOverloaded() || isParalyzed() || isImmobilized() || isFakeDeath() || isFallsdown() || isPetrified();
	}

	public final boolean isMoving()
	{
		return _move != null;
	}

	public final boolean isMuted()
	{
		return _isMuted;
	}

	public final boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if (m == null)
			return false;
		if (m.onGeodataPathIndex == -1)
			return false;

		return m.onGeodataPathIndex != m.geoPath.size() - 1;
	}

	public boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}

	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}

	public final boolean isParalyzed()
	{
		return _isParalyzed;
	}

	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}

	public final boolean isPetrified()
	{
		return _isPetrified;
	}

	public final boolean isPhysicalAttackMuted()
	{
		return _isPhysicalAttackMuted;
	}

	public final boolean isPhysicalMuted()
	{
		return _isPhysicalMuted;
	}

	public boolean isPreventedFromReceivingBuffs()
	{
		return _block_buffs;
	}

	public boolean isRaid()
	{
		return _isRaid;
	}

	public boolean isRaidBoss()
	{
		return _isRaid && !_isMinion;
	}

	public boolean isRaidMinion()
	{
		return _isMinion;
	}

	public boolean isRiding()
	{
		return false;
	}

	public final boolean isRooted()
	{
		return _isRooted;
	}

	public final boolean isRunning()
	{
		return _isRunning;
	}

	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}

	public boolean isSkillDisabled(int skillId)
	{
		if (isAllSkillsDisabled())
			return true;

		if (_disabledSkills == null)
			return false;

		return _disabledSkills.contains(skillId);
	}

	public final boolean isSleeping()
	{
		return _isSleeping;
	}

	public final boolean isStunned()
	{
		return _isStunned;
	}

	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}

	public boolean isUndead()
	{
		return _template.isUndead();
	}

	public boolean isUsingDualWeapon()
	{
		return false;
	}

	protected boolean moveToLocation(int x, int y, int z, int offset)
	{

		_startLoc = new Location(getLoc());
		_isMoving = true;
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		double dx = x - curX;
		double dy = y - curY;
		double dz = z - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (Config.GEODATA && isInsideZone(L2Zone.FLAG_WATER) && distance > 700)
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = x - curX;
			dy = y - curY;
			dz = z - curZ;
			distance = Math.sqrt(dx * dx + dy * dy);
		}

		double cos;
		double sin;

		if (offset > 0 || distance < 1)
		{

			offset -= Math.abs(dz);

			if (offset < 5)
			{
				offset = 5;
			}

			if (distance < 1 || distance - offset <= 0)
			{
				sin = 0;
				cos = 1;
				distance = 0;
				x = curX;
				y = curY;

				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);

				return false;
			}
			sin = dy / distance;
			cos = dx / distance;

			distance -= offset - 5;

			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);

		}
		else
		{
			sin = dy / distance;
			cos = dx / distance;
		}

		MoveData m = new MoveData();

		m.onGeodataPathIndex = -1;
		m.disregardingGeodata = false;

		if (Config.PATHFINDING && !isFlying() && (!isInsideZone(L2Zone.FLAG_WATER) || isInsideZone(L2Zone.FLAG_SIEGE)) && !(this instanceof L2NpcWalkerInstance))
		{
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = originalX - L2World.MAP_MIN_X >> 4;
			int gty = originalY - L2World.MAP_MIN_Y >> 4;

			if (Config.PATHFINDING && !(this instanceof L2Attackable && ((L2Attackable) this).isReturningToSpawnPoint()) || this instanceof L2PcInstance || this instanceof L2Summon && !(getAI().getIntention() == FOLLOW) || isAfraid() || this instanceof L2RiftInvaderInstance)
			{
				if (isOnGeodataPath())
				{
					try
					{
						if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
							return false;
						_move.onGeodataPathIndex = -1;
					}
					catch (NullPointerException e)
					{
					}
				}
				if (curX < L2World.MAP_MIN_X || curX > L2World.MAP_MAX_X || curY < L2World.MAP_MIN_Y || curY > L2World.MAP_MAX_Y)
				{
					_log.warn("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.IDLE);
					if (this instanceof L2PcInstance)
					{
						new Disconnection((L2PcInstance) this).defaultSequence(true);
					}
					else if (!(this instanceof L2Summon))
					{
						onDecay();
					}
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z);

				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				distance = Math.sqrt((x - curX) * (x - curX) + (y - curY) * (y - curY));
			}
			if (Config.PATHFINDING && originalDistance - distance > 100 && distance < 2000 && !isAfraid())
			{
				m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, getActingPlayer() != null);
				if (m.geoPath == null || m.geoPath.size() < 2)
				{
					if (this instanceof L2PcInstance || !(this instanceof L2Playable) && !(this instanceof L2MinionInstance) && Math.abs(z - curZ) > 140 || this instanceof L2Summon && !((L2Summon) this).getFollowStatus())
					{
						getAI().setIntention(CtrlIntention.IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}

					m.disregardingGeodata = true;
					x = originalX;
					y = originalY;
					z = originalZ;
					distance = originalDistance;
				}
				else
				{
					m.onGeodataPathIndex = 0;
					m.geoPathGtx = gtx;
					m.geoPathGty = gty;
					m.geoPathAccurateTx = originalX;
					m.geoPathAccurateTy = originalY;

					x = m.geoPath.get(m.onGeodataPathIndex).getX();
					y = m.geoPath.get(m.onGeodataPathIndex).getY();
					z = m.geoPath.get(m.onGeodataPathIndex).getZ();

					if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z))
					{
						m.geoPath = null;
						getAI().setIntention(CtrlIntention.IDLE);
						return false;
					}
					for (int i = 0; i < m.geoPath.size() - 1; i++)
						if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath.get(i), m.geoPath.get(i + 1)))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.IDLE);
							sendPacket(ActionFailed.STATIC_PACKET);
							return false;
						}

					dx = x - curX;
					dy = y - curY;
					distance = Math.sqrt(dx * dx + dy * dy);
					sin = dy / distance;
					cos = dx / distance;
				}
			}
			if (distance < 1 && (Config.PATHFINDING || this instanceof L2Playable || isAfraid() || this instanceof L2RiftInvaderInstance))
			{
				if (this instanceof L2Summon)
				{
					((L2Summon) this).setFollowStatus(false);
				}
				getAI().setIntention(CtrlIntention.IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);

		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z;

		m._heading = 0;
		setHeading(Util.calculateHeadingFrom(cos, sin));
		m._moveStartTime = GameTimeController.getGameTicks();

		_move = m;

		GameTimeController.getInstance().registerMovingChar(this);

		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		return true;
	}

	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			_move = null;
			return false;
		}

		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
		{
			_move = null;
			return false;
		}

		MoveData m = new MoveData();
		MoveData md = _move;
		if (md == null)
			return false;

		m.onGeodataPathIndex = md.onGeodataPathIndex + 1;
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;

		if (md.onGeodataPathIndex == md.geoPath.size() - 2)
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		double dx = m._xDestination - super.getX();
		double dy = m._yDestination - super.getY();
		double distance = Math.sqrt(dx * dx + dy * dy);
		double sin = dy / distance;
		double cos = dx / distance;

		int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);

		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378);
		heading += 32768;
		setHeading(heading);
		m._heading = 0;

		m._moveStartTime = GameTimeController.getGameTicks();

		_move = m;

		GameTimeController.getInstance().registerMovingChar(this);

		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}

		MoveToLocation msg = new MoveToLocation(this);
		broadcastPacket(msg);

		return true;
	}

	public boolean mustFallDownOnDeath()
	{
		return isDead();
	}

	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getStatus().getCurrentHp();

		if (currentHp <= 1.0 || getMaxHp() < barPixels)
			return true;

		if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
		{
			if (currentHp == getMaxHp())
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			return true;
		}
		return false;
	}

	private void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		if (this instanceof L2Npc)
		{
			try
			{
				if (((L2NpcTemplate) getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null)
				{
					L2PcInstance player = target.getActingPlayer();
					for (Quest quest : ((L2NpcTemplate) getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED))
					{
						quest.notifySpellFinished((L2Npc) this, player, skill);
					}
				}
			}
			catch (Exception e)
			{

			}
		}
	}

	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		decayMe();
		if (reg != null)
		{
			reg.removeFromZones(this);
		}
	}

	@Override
	public void onEffectFinished(L2Character effected, L2Skill skill)
	{

	}

	public void onExitChanceEffect()
	{
		if (_chanceSkills == null)
			return;
		_chanceSkills.onExit();
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		if (player.getTarget() == null || !(player.getTarget() instanceof L2Character))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2Character target = (L2Character) player.getTarget();

		if (player.isInOlympiadMode() && target instanceof L2Playable)
		{
			L2PcInstance ptarget = target.getActingPlayer();

			if (ptarget.isInOlympiadMode() && !player.isOlympiadStart() || player.getOlympiadGameId() != ptarget.getOlympiadGameId())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		if (!target.isAttackable() && !player.allowPeaceAttack())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isConfused())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (this instanceof L2ArtefactInstance)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!GeoData.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		player.getAI().setIntention(CtrlIntention.ATTACK, this);
	}

	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		if (target == null || isAlikeDead())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if (this instanceof L2Npc && target.isAlikeDead() || target.isDead() || !getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (miss)
		{
			if (target.getChanceSkills() != null)
			{
				target.getChanceSkills().onEvadedHit(this);
			}

			if (target instanceof L2PcInstance)
			{
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK).addCharName(this));
			}
		}

		if (!isAttackAborted())
		{
			if (target.isRaid() && !Config.ALT_DISABLE_RAIDBOSS_PETRIFICATION && target.getLevel() <= Config.MAX_LEVEL_RAID_CURSE)
			{
				int level = 0;
				if (this instanceof L2PcInstance)
				{
					level = getLevel();
				}
				else if (this instanceof L2Summon)
				{
					level = ((L2Summon) this).getOwner().getLevel();
				}

				if (level > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);

					if (skill != null)
					{
						if (target != target.getActingPlayer())
						{
							skill.getEffects(target, this);
						}
						if (getActingPlayer() != null)
						{
							skill.getEffects(target, this.getActingPlayer());
						}
					}
					damage = 0;
				}
			}

			sendDamageMessage(target, damage, false, crit, miss);

			if (target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance) target;
				enemy.getAI().clientStartAutoAttack();
			}
			else if (target instanceof L2Summon)
			{
				((L2Summon) target).getOwner().getAI().clientStartAutoAttack();
			}

			if (!miss && damage > 0)
			{
				L2Weapon weapon = getActiveWeaponItem();
				boolean isRangeWeapon = weapon != null && weapon.getItemType() == L2WeaponType.BOW;

				int reflectedDamage = 0;
				if (!isRangeWeapon && !(this instanceof L2Boss))
				{
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);

					if (reflectPercent > 0)
					{
						reflectedDamage = (int) (reflectPercent / 100. * damage);

						if (reflectedDamage > target.getMaxHp())
						{
							reflectedDamage = target.getMaxHp();
						}
					}
				}
				target.reduceCurrentHp(damage, this, null);

				if (reflectedDamage > 0)
				{
					reduceCurrentHp(reflectedDamage, target, true, false, null);
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(reflectedDamage));
				}

				if (!isRangeWeapon)
				{
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);

					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxHp() - getStatus().getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);

						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb;
						}

						if (absorbDamage > 0)
						{
							getStatus().increaseHp(absorbDamage);
						}
					}

					double absorbCPPercent = getStat().calcStat(Stats.ABSORB_CP_PERCENT, 0, null, null);

					if (absorbCPPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxCp() - getStatus().getCurrentCp());
						int absorbDamage = (int) (absorbCPPercent / 100. * damage);

						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb;
						}

						getStatus().setCurrentCp(getStatus().getCurrentCp() + absorbDamage);
					}
				}

				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				if (this instanceof L2Summon)
				{
					L2PcInstance owner = ((L2Summon) this).getOwner();
					if (owner != null)
					{
						owner.getAI().clientStartAutoAttack();
					}
				}

				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				if (_chanceSkills != null)
				{
					_chanceSkills.onHit(target, false, crit);
				}

				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onHit(this, true, crit);
				}

				L2Weapon activeWeapon = getActiveWeaponItem();

				if (activeWeapon != null && crit)
				{
					activeWeapon.getSkillEffectsByCrit(this, target);
				}
			}
			return;
		}

		if (!isCastingNow() && !isCastingSimultaneouslyNow())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
		}
	}

	public void onMagicFinalizer(L2Skill skill, L2Object target, boolean simultaneously)
	{
		if (simultaneously)
		{
			_skillCast2 = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		_skillCast = null;
		setIsCastingNow(false);
		_castInterruptTime = 0;

		if (skill.nextActionIsAttack() && getTarget() instanceof L2Character && getTarget() != this && getTarget() == target && getTarget().isAttackable())
			if (getAI() == null || getAI().getNextIntention() == null || getAI().getNextIntention().getCtrlIntention() != CtrlIntention.MOVE_TO)
			{
				getAI().setIntention(CtrlIntention.ATTACK, target);
			}

		if (skill.isOffensive() && !skill.isNeutral() && skill.getSkillType() != L2SkillType.UNLOCK && skill.getSkillType() != L2SkillType.DELUXE_KEY_UNLOCK && skill.getSkillType() != L2SkillType.MAKE_KILLABLE)
		{
			getAI().clientStartAutoAttack();
		}

		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

		notifyQuestEventSkillFinished(skill, target);

		if (getAI().getIntention() == ATTACK)
		{
			switch (skill.getSkillType())
			{
				case PDAM:
				case BLOW:
				case CHARGEDAM:
				case SPOIL:
				case SOW:
				case DRAIN_SOUL:
					if ((getTarget() instanceof L2Character) && (getTarget() != this) && (target == getTarget()))
					{
						getAI().setIntention(CtrlIntention.ATTACK, getTarget());
					}
					break;
			}
		}
		if ((_chanceSkills != null) && (target != null))
		{
			_chanceSkills.onAttack(target);
		}

		if (this instanceof L2PcInstance)
		{
			L2PcInstance currPlayer = (L2PcInstance) this;
			SkillDat queuedSkill = currPlayer.getQueuedSkill();

			currPlayer.setCurrentSkill(null, false, false);

			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);
				ThreadPoolManager.getInstance().executeAi(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
			if (skill.getItemConsume() != 0)
			{
				sendPacket(new InventoryUpdate());
			}
		}
	}

	@SuppressWarnings("null")
	public void onMagicHitTimer(L2Character[] targets, L2Skill skill, int coolTime, boolean instant, boolean simultaneously)
	{
		if (skill == null)
		{
			abortCast();
			setAttackingChar(null);
			return;
		}

		if ((targets == null || targets.length == 0) && skill.getTargetType() != SkillTargetType.TARGET_AURA)
		{
			abortCast();
			setAttackingChar(null);
			return;
		}

		if (getFusionSkill() != null)
		{
			if (simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			if (targets != null && targets.length > 0)
			{
				notifyQuestEventSkillFinished(skill, targets[0]);
			}
			getFusionSkill().onCastAbort();
			return;
		}

		L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			if (simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			mog.exit();
			if (targets != null && targets.length > 0)
			{
				notifyQuestEventSkillFinished(skill, targets[0]);
			}
			return;
		}
		if (Config.CONSUME_ON_SUCCESS)
		{
			consume(skill);
		}
		try
		{
			for (L2Object element : targets)
				if (element instanceof L2Playable)
				{
					L2Character target = (L2Character) element;

					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}

					if (this instanceof L2PcInstance && target instanceof L2Summon)
					{
						target.broadcastFullInfo();
					}
				}

			if (skill.getItemConsume() > 0)
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, false))
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					abortCast();
					return;
				}

			if (this instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) this;
				if (skill.getNeededCharges() > 0 && !(skill instanceof L2SkillChargeDmg) && skill.getConsumeCharges())
				{
					player.decreaseCharges(skill.getNeededCharges());
				}
			}

			callSkill(skill, targets);
		}
		catch (Exception e)
		{

		}

		if (instant || coolTime == 0)
		{
			onMagicFinalizer(skill, targets == null || targets.length == 0 ? null : targets[0], simultaneously);
		}
		else if (simultaneously)
		{
			_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, simultaneously), coolTime);
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, simultaneously), coolTime);
		}
	}

	@SuppressWarnings("null")
	public void onMagicLaunchedTimer(L2Character[] targets, L2Skill skill, int coolTime, boolean instant, boolean simultaneously)
	{
		if (skill != null && !Config.CONSUME_ON_SUCCESS)
		{
			consume(skill);
		}

		if (skill == null)
		{
			abortCast();
			setAttackingChar(null);
			return;
		}

		if ((targets == null || targets.length == 0) && skill.getTargetType() != SkillTargetType.TARGET_AURA)
		{
			abortCast();
			setAttackingChar(null);
			return;
		}

		if (skill.getSkillType() == L2SkillType.NOTDONE)
		{
			abortCast();
			return;
		}

		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
		{
			escapeRange = skill.getSkillRadius();
		}

		if (escapeRange > 0)
		{
			List<L2Character> targetList = new ArrayList<>();
			for (L2Object element : targets)
				if (element instanceof L2Character)
				{
					if (!Util.checkIfInRange(escapeRange, this, element, true) || !GeoData.getInstance().canSeeTarget(this, element))
					{
						continue;
					}
					if (skill.isOffensive() && !skill.isNeutral())
						if (this instanceof L2PcInstance)
						{
							if (((L2Character) element).isInsidePeaceZone((L2PcInstance) this))
							{
								continue;
							}
						}
						else if (L2Character.isInsidePeaceZone(this, element))
						{
							continue;
						}
					targetList.add((L2Character) element);
				}
			if (targetList.isEmpty() && skill.getTargetType() != SkillTargetType.TARGET_AURA)
			{
				abortCast();
				return;
			}

			targets = targetList.toArray(new L2Character[targetList.size()]);
		}

		if (simultaneously && !isCastingSimultaneouslyNow() || !simultaneously && !isCastingNow() || isAlikeDead() && !skill.isPotion())
		{
			setAttackingChar(null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

			_castInterruptTime = 0;
			return;
		}

		int level = getSkillLevel(skill.getId());

		if (level < 1)
		{
			level = 1;
		}

		if (!skill.isPotion() && targets.length > 1)
		{
			broadcastPacket(new MagicSkillLaunched(this, skill.getId(), level, skill.isPositive(), targets));
		}

		if (instant)
		{
			try
			{
				onMagicHitTimer(targets, skill, coolTime, true, simultaneously);
			}
			catch (Exception e)
			{

			}
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, simultaneously), 200);
		}
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone(true);
	}

	public void onTeleported()
	{
		if (!isTeleporting())
			return;

		if (this instanceof L2Summon)
		{
			((L2Summon) this).getOwner().sendPacket(new TeleportToLocation(this, getPosition().getX(), getPosition().getY(), getPosition().getZ()));
		}

		setIsTeleporting(false);
		spawnMe();

		if (_isPendingRevive)
		{
			doRevive();
		}
	}

	protected void reduceArrowCount(boolean bolts)
	{
	}

	public void reduceCurrentHp(double i, L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true, false, null);
	}

	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		getStatus().reduceHp(i, attacker, awake);
	}

	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		getStatus().reduceHp(i, attacker, awake, isDOT);
	}

	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, awake, false, skill);
	}

	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}

	public void reduceCurrentHpByDamOverTime(double i, L2Character attacker, boolean awake, int period)
	{
		if (_nextReducingHPByOverTime > System.currentTimeMillis())
			return;

		_nextReducingHPByOverTime = System.currentTimeMillis() + period * 1000;
		reduceCurrentHp(i, attacker, awake);

	}

	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}

	public void reduceCurrentMpByDamOverTime(double i, int period)
	{
		if (_nextReducingMPByOverTime > System.currentTimeMillis())
			return;

		_nextReducingMPByOverTime = System.currentTimeMillis() + period * 1000;
		reduceCurrentMp(i);

	}

	protected void refreshSkills()
	{
		_calculators = NPC_STD_CALCULATOR;
		_stat = new CharStat(this);

		_skills = ((L2NpcTemplate) _template).getSkills();
		if (_skills != null)
		{
			synchronized (_skills)
			{
				for (Map.Entry<Integer, L2Skill> skill : _skills.entrySet())
				{
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
				}
			}
		}
		getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());
	}

	public synchronized void removeChanceEffect(int id)
	{
		if (_chanceSkills == null)
			return;

		for (L2Skill skill : _chanceSkills.keySet())
			if (skill.getId() == id)
			{
				_chanceSkills.remove(skill);
			}
		if (_chanceSkills.size() == 0)
		{
			_chanceSkills = null;
		}
	}

	public synchronized void removeChanceSkill(int id)
	{
		if (_chanceSkills == null)
			return;

		for (L2Skill skill : _chanceSkills.keySet())
			if (skill.getId() == id)
			{
				_chanceSkills.remove(skill);
			}
		if (_chanceSkills.size() == 0)
		{
			_chanceSkills = null;
		}
	}

	public void removeEffect(L2Effect effect)
	{
		_effects.removeEffect(effect);
	}

	public boolean removeNotifyQuestOfDeath(QuestState st)
	{
		return getNotifyQuestOfDeath().remove(st);
	}

	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}

	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		L2Skill oldSkill;
		synchronized (_skills)
		{
			oldSkill = _skills.remove(skillId);
		}
		if (oldSkill != null)
		{
			try
			{
				if (oldSkill.getElement() > -1)
				{
					getStat().removeElement(oldSkill);
				}
			}
			catch (Exception e)
			{

			}

			if ((oldSkill.bestowTriggered() || oldSkill.triggerAnotherSkill()) && oldSkill.getTriggeredId() > 0)
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}

			if (this instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) this;
				if (player.getCurrentSkill() != null && isCastingNow())
					if (oldSkill.getId() == player.getCurrentSkill().getSkillId())
					{
						abortCast();
					}
			}
			if (getLastSimultaneousSkillCast() != null && isCastingSimultaneouslyNow())
				if (oldSkill.getId() == getLastSimultaneousSkillCast().getId())
				{
					abortCast();
				}

			if (cancelEffect || oldSkill.isToggle())
			{
				removeStatsOwner(oldSkill);
				stopSkillEffects(oldSkill.getId());
			}
			if (oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (oldSkill instanceof L2SkillMount && this instanceof L2PcInstance && ((L2PcInstance) this).isMounted())
			{
				((L2PcInstance) this).dismount();
			}
			if (oldSkill instanceof L2SkillSummon && oldSkill.getId() == 710 && this instanceof L2PcInstance && ((L2PcInstance) this).getPet() != null && ((L2PcInstance) this).getPet().getNpcId() == 14870)
			{
				((L2PcInstance) this).getPet().unSummon((L2PcInstance) this);
			}
		}
		return oldSkill;
	}

	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null)
			return null;

		return removeSkill(skill.getId(), true);
	}

	public L2Skill removeSkill(L2Skill skill, boolean cancelEffect)
	{
		if (skill == null)
			return null;

		return removeSkill(skill.getId(), cancelEffect);
	}

	public final void removeStatsOwner(FuncOwner owner)
	{
		synchronized (_calculators)
		{
			for (int i = 0; i < _calculators.length; i++)
				if (_calculators[i] != null)
				{
					_calculators[i].removeOwner(owner, this);

					if (_calculators[i].size() == 0)
					{
						_calculators[i] = null;
					}
				}

			if (this instanceof L2Npc)
			{
				int i = 0;
				for (; i < Stats.NUM_STATS; i++)
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}

				if (i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
		}

		broadcastFullInfo();
	}

	public void removeTimeStamp(int skill)
	{

	}

	public void returnHome()
	{
	}

	public void revalidateZone(boolean force)
	{
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
				return;
		}

		if (getWorldRegion() == null)
			return;
		getWorldRegion().revalidateZones(this);
	}

	public void seeSpell(L2PcInstance caster, L2Object target, L2Skill skill)
	{
		if (this instanceof L2Attackable)
		{
			((L2Attackable) this).addDamageHate(caster, 0, -skill.getAggroPoints() / Config.ALT_BUFFER_HATE);
		}
	}

	public void sendAvoidMessage(L2Character attacker)
	{
	}

	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss)
		{
			target.sendAvoidMessage(this);
		}
	}

	public void sendMessage(String message)
	{
		if (getActingPlayer() != null)
		{
			getActingPlayer().sendPacket(SystemMessage.sendString(message));
		}
	}

	public void sendPacket(L2GameServerPacket gsp)
	{

	}

	public void sendPacket(SystemMessageId sm)
	{

	}

	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if (oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
		{
			oldAI.stopAITask();
		}
		_ai = newAI;
	}

	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}

	public final void setAttackingChar(L2Character player)
	{
		if (player == null || player == this)
			return;

		_attackingChar = player;
		addAttackerToAttackByList(player);
	}

	public void setChampion(boolean champ)
	{
		_champion = champ;
	}

	public void setCriticalDmg(boolean par)
	{
		_lastHitIsCritical = par;
	}

	public void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}

	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}

	public void setFusionSkill(FusionSkill fb)
	{
		_fusionSkill = fb;
	}

	public final void setHeading(int heading)
	{
		_heading = heading;
	}

	public void setHealLimit(int power)
	{
		_healLimit = power;
	}

	public void setInsideZone(L2Zone zone, byte zoneType, boolean state)
	{
		if (state)
		{
			_currentZones[zoneType]++;
			if (!_currentZoneList.contains(zone))
			{
				_currentZoneList.add(zone);
			}
		}
		else
		{
			if (_currentZones[zoneType] > 0)
			{
				_currentZones[zoneType]--;
			}
			_currentZoneList.remove(zone);
		}
	}

	public final void setIsAfraid(boolean value)
	{
		_isAfraid = value;
	}

	public final void setIsBetrayed(boolean value)
	{
		_isBetrayed = value;
	}

	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}

	public void setIsBoss(boolean b)
	{
		_isBoss = b;
	}

	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}

	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}

	public final void setIsConfused(boolean value)
	{
		_isConfused = value;
	}

	public final void setIsDead(boolean value)
	{
		_isDead = value;
	}

	public final void setIsDisarmed(boolean value)
	{
		_isDisarmed = value;
	}

	public final void setIsFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}

	public final void setIsFallsdown(boolean value)
	{
		_isFallsdown = value;
	}

	/**
	 * Set the L2Character flying mode to True.
	 * @param mode the new checks if is flying
	 */
	public final void setIsFlying(final boolean mode)
	{
		_isFlying = mode;
	}

	public void setIsGrandBoss(boolean b)
	{
		_isGrandBoss = b;
	}

	public final void setIsImmobileUntilAttacked(boolean value)
	{
		_isImmobileUntilAttacked = value;
	}

	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}

	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}

	public final void setIsLuckByNoblesse(boolean value)
	{
		_isLuckByNoblesse = value;
	}

	public final void setIsMuted(boolean value)
	{
		_isMuted = value;
	}

	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}

	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}

	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public final void setIsPetrified(boolean value)
	{
		_isPetrified = value;
	}

	public final void setIsPhysicalAttackMuted(boolean value)
	{
		_isPhysicalAttackMuted = value;
	}

	public final void setIsPhysicalMuted(boolean value)
	{
		_isPhysicalMuted = value;
	}

	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}

	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isMinion = val;
	}

	public final void setIsRooted(boolean value)
	{
		_isRooted = value;
	}

	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		if (getRunSpeed() != 0)
		{
			broadcastPacket(new ChangeMoveType(this));
		}

		broadcastFullInfo();
	}

	public final void setIsSleeping(boolean value)
	{
		_isSleeping = value;
	}

	public final void setIsStunned(boolean value)
	{
		_isStunned = value;
	}

	public final void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}

	public void setKnowAsUpdated(long time)
	{
		_lastKnowUpdate = time;
	}

	public void setKnownList(CharKnownList value)
	{
		_knownList = value;
	}

	public void setLastHealAmount(int hp)
	{
		_lastHealAmount = hp;
	}

	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}

	public void setPreventedFromReceivingBuffs(boolean value)
	{
		_block_buffs = value;
	}

	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}

	public final void setRunning()
	{
		if (!isRunning())
		{
			setIsRunning(true);
		}
	}

	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}

	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}

	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
		{
			object = null;
		}

		if (object != null && object != _target)
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		_target = object;

	}

	public void setTeam(int team)
	{
		_team = team;
		if (getPet() != null)
		{
			getPet().broadcastFullInfo();
		}
	}

	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}

	public final void setTitle(String value)
	{
		if (_event != null && _event.isRunning())
		{
			_eventTitle = value;
			if (_eventTitle == null)
			{
				_eventTitle = "";
			}
			return;
		}

		if (value == null)
		{
			_title = "";
			return;
		}

		if (this instanceof L2PcInstance && value.length() > 16)
		{
			value = value.substring(0, 15);
		}

		_title = value;
	}

	public final void setWalking()
	{
		if (isRunning())
		{
			setIsRunning(false);
		}
	}

	public final void startAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects |= mask.getMask();
		updateAbnormalEffect();
	}

	public final void startAbnormalEffect(int mask)
	{
		if (_AbnormalEffects != (_AbnormalEffects |= mask))
		{
			updateAbnormalEffect();
		}
	}

	public final void startBetray()
	{
		setIsBetrayed(true);
		getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
		updateAbnormalEffect();
	}

	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}

	public final void startFakeDeath()
	{
		if (this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
			if (player != null)
			{
				if (player._event != null && player._event.getState() == GameEvent.STATE_RUNNING)
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_EVENT_SKILL_NOT_ALOWED));
					return;
				}
			}
		}

		setIsFallsdown(true);

		if (Config.FAIL_FAKEDEATH)
		{
			setIsFakeDeath(true);
			if (_attackingChar != null)
			{
				int _diff = _attackingChar.getLevel() - getLevel();
				switch (_diff)
				{
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
						if (Rnd.nextInt(100) >= 95)
						{
							setIsFakeDeath(false);
						}
						break;
					case 6:
						if (Rnd.nextInt(100) >= 90)
						{
							setIsFakeDeath(false);
						}
						break;
					case 7:
						if (Rnd.nextInt(100) >= 85)
						{
							setIsFakeDeath(false);
						}
						break;
					case 8:
						if (Rnd.nextInt(100) >= 80)
						{
							setIsFakeDeath(false);
						}
						break;
					case 9:
						if (Rnd.nextInt(100) >= 75)
						{
							setIsFakeDeath(false);
						}
						break;
					default:
						if (_diff > 9)
						{
							if (Rnd.nextInt(100) >= 50)
							{
								setIsFakeDeath(false);
							}
						}
						else
						{
							setIsFakeDeath(true);
						}
				}
				if (_attackingChar.isRaid())
				{
					setIsFakeDeath(false);
				}
			}
			else if (Rnd.nextInt(100) >= 75)
			{
				setIsFakeDeath(false);
			}
		}
		else
		{
			setIsFakeDeath(true);
		}

		abortAttack();
		abortCast();
		stopMove(null);
		sendPacket(ActionFailed.STATIC_PACKET);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}

	public final void startFear()
	{
		setIsAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}

	public void startFusionSkill(L2Character target, L2Skill skill)
	{
		if (skill.getSkillType() != L2SkillType.FUSION)
			return;

		if (_fusionSkill == null)
		{
			_fusionSkill = new FusionSkill(this, target, skill);
		}
	}

	public final void startImmobileUntilAttacked()
	{
		setIsImmobileUntilAttacked(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	public final void startLuckNoblesse()
	{
		setIsBlessedByNoblesse(true);
		getAI().notifyEvent(CtrlEvent.EVT_LUCKNOBLESSE, null);
	}

	public final void startMuted()
	{
		setIsMuted(true);
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	public final void startParalyze()
	{
		setIsParalyzed(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED, null);
		updateAbnormalEffect();
	}

	public final void startPhysicalAttackMuted()
	{
		setIsPhysicalAttackMuted(true);
		abortAttack();
	}

	public final void startPhysicalMuted()
	{
		setIsPhysicalMuted(true);
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	public void startPvPFlag()
	{
		updatePvPFlag(1);
		if (_PvPRegTask != null)
		{
			_PvPRegTask.cancel(true);
		}
		_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
	}

	public final void startRooted()
	{
		setIsRooted(true);
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}

	public final void startSleeping()
	{
		setIsSleeping(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	public final void startStunning()
	{
		setIsStunned(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}

	public final void stopAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}

	public final void stopAbnormalEffect(int mask)
	{
		if (_AbnormalEffects != (_AbnormalEffects &= ~mask))
		{
			updateAbnormalEffect();
		}
	}

	public final void stopAllEffects()
	{
		_effects.stopAllEffects();
		broadcastFullInfo();
	}

	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
		broadcastFullInfo();
	}

	public final void stopBetray()
	{
		stopEffects(L2EffectType.BETRAY);
		setIsBetrayed(false);
		updateAbnormalEffect();
	}

	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.CONFUSION);
		}
		else
		{
			removeEffect(effect);
		}

		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}

	public final void stopFakeDeath(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.FAKE_DEATH);
		}
		else
		{
			removeEffect(effect);
		}

		final L2PcInstance player = (L2PcInstance) this;

		setIsFakeDeath(false);
		setIsFallsdown(false);

		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).setRecentFakeDeath(true);
		}

		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH));
		broadcastPacket(new Revive(this));
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				player.setIsParalyzed(false);
			}
		}, player.getAnimationTimer());
		setIsParalyzed(true);
	}

	public final void stopFear(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.FEAR);
		}
		else
		{
			removeEffect(effect);
		}

		setIsAfraid(false);
		updateAbnormalEffect();
	}

	public final void stopImmobileUntilAttacked(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.IMMOBILEUNTILATTACKED);
		}
		else
		{
			removeEffect(effect);
			stopSkillEffects(effect.getSkill().getNegateId());
		}

		setIsImmobileUntilAttacked(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopLuckNoblesse()
	{
		setIsBlessedByNoblesse(false);
		getAI().notifyEvent(CtrlEvent.EVT_LUCKNOBLESSE, null);
	}

	public void stopMove()
	{
		if (!_isMoving)
			return;
		_move = null;
		_isMoving = false;
		getPosition().setXYZ(_startLoc.getX(), _startLoc.getY(), _startLoc.getZ());
		Broadcast.toKnownPlayers(this, new StopMove(this));
	}

	public void stopMove(L2CharPosition pos)
	{
		stopMove(pos, false);
	}

	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		_move = null;
		_isMoving = false;
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if (updateKnownObjects)
		{
			getKnownList().updateKnownObjects();
		}
	}

	public final void stopMuted(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.MUTE);
		}
		else
		{
			removeEffect(effect);
		}

		setIsMuted(false);
		updateAbnormalEffect();
	}

	public final void stopNoblesse()
	{
		stopEffects(L2EffectType.NOBLESSE_BLESSING);
		stopEffects(L2EffectType.LUCKNOBLESSE);
		setIsBlessedByNoblesse(false);
		setIsLuckByNoblesse(false);
		getAI().notifyEvent(CtrlEvent.EVT_LUCKNOBLESSE, null);
	}

	public final void stopParalyze(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PARALYZE);
		}
		else
		{
			removeEffect(effect);
		}

		setIsParalyzed(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopPhysicalAttackMuted(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PHYSICAL_ATTACK_MUTE);
		}
		else
		{
			removeEffect(effect);
		}
		setIsPhysicalAttackMuted(false);
	}

	public final void stopPhysicalMuted(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PHYSICAL_MUTE);
		}
		else
		{
			removeEffect(effect);
		}

		setIsPhysicalMuted(false);
		updateAbnormalEffect();
	}

	public void stopPvPFlag()
	{
		if (_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
		}

		updatePvPFlag(0);

		_PvPRegTask = null;
	}

	public final void stopRooting(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.ROOT);
		}
		else
		{
			removeEffect(effect);
		}

		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}

	public final void stopSleeping(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.SLEEP);
		}
		else
		{
			removeEffect(effect);
		}

		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopStunning(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.STUN);
		}
		else
		{
			removeEffect(effect);
		}

		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, true);
	}

	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if (this instanceof L2PcInstance && Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TELEPORT && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			sendMessage(Message.getMessage((L2PcInstance) this, Message.MessageId.MSG_ACTION_NOT_ALLOWED_DURING_SHUTDOWN));
			return;
		}

		abortCast();
		abortAttack();
		getAI().setIntention(CtrlIntention.IDLE);
		setTarget(this);
		disableAllSkills();
		abortAttack();
		abortCast();
		isFalling(false, 0);
		setIsTeleporting(true);

		if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
			y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
		}
		z += 5;

		decayMe();
		broadcastPacket(new TeleportToLocation(this, x, y, z));
		getPosition().setWorldPosition(x, y, z);
		isFalling(false, 0);

		if (!(this instanceof L2PcInstance))
		{
			onTeleported();
		}

		enableAllSkills();
		getAI().setIntention(CtrlIntention.ACTIVE);
		getKnownList().updateKnownObjects();
	}

	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), allowRandomOffset);
	}

	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
	}

	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}

	public abstract void updateAbnormalEffect();

	public void updateInvisibilityStatus()
	{
		DeleteObject de = new DeleteObject(this);
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
			if (!player.canSee(this))
			{
				if (player.getTarget() == this)
				{
					player.setTarget(null);
					player.abortAttack();
				}
				player.sendPacket(de);
			}
		broadcastFullInfo();
	}

	public boolean updatePosition(int gameTicks)
	{
		MoveData m = _move;

		if (m == null)
			return true;

		if (!isVisible())
		{
			_move = null;
			return true;
		}

		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}

		if (m._moveTimestamp == gameTicks)
			return false;

		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ();

		double dx, dy, dz, distFraction;
		if (Config.COORD_SYNCHRONIZE == 1)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		if (Config.GEODATA && Config.COORD_SYNCHRONIZE == 2 && !isFlying() && !isInsideZone(L2Zone.FLAG_WATER) && !m.disregardingGeodata && GameTimeController.getGameTicks() % 10 == 0 && !(this instanceof L2BoatInstance))
		{
			short geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30, getObjectId());
			dz = m._zDestination - geoHeight;
			if (this instanceof L2PcInstance && Math.abs(getZ() - geoHeight) > 200 && Math.abs(getZ() - geoHeight) < 1500)
			{
				dz = m._zDestination - zPrev;
			}
			else if (isInCombat() && Math.abs(dz) > 200 && dx * dx + dy * dy < 40000)
			{
				dz = m._zDestination - zPrev;
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
		{
			dz = m._zDestination - zPrev;
		}

		double distPassed = getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
		if (dx * dx + dy * dy < 10000 && dz * dz > 2500)
		{
			distFraction = distPassed / Math.sqrt(dx * dx + dy * dy);
		}
		else
		{
			distFraction = distPassed / Math.sqrt(dx * dx + dy * dy + dz * dz);
		}

		if (distFraction > 1)
		{
			super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
			if (this instanceof L2BoatInstance)
			{
				((L2BoatInstance) this).updatePeopleInTheBoat(m._xDestination, m._yDestination, m._zDestination);
			}
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;

			super.getPosition().setXYZ((int) m._xAccurate, (int) m._yAccurate, zPrev + (int) (dz * distFraction + 0.5));
			if (this instanceof L2BoatInstance)
			{
				((L2BoatInstance) this).updatePeopleInTheBoat((int) m._xAccurate, (int) m._yAccurate, zPrev + (int) (dz * distFraction + 0.5));
			}
			else
			{
				revalidateZone(false);
			}
		}

		m._moveTimestamp = gameTicks;

		return distFraction > 1;
	}

	public void updatePvPFlag(int value)
	{
	}

	public boolean validateMovementHeading(int heading)
	{
		MoveData md = _move;
		if (md == null)
			return true;

		boolean result = true;
		if (md._heading != heading)
		{
			result = md._heading == 0;
			md._heading = heading;
		}
		return result;
	}

	public final void ZoneEnter(L2Zone zone)
	{
		_currentZonesInstances.add(zone);
	}

	public final void ZoneLeave(L2Zone zone)
	{
		_currentZonesInstances.remove(zone);
	}
}
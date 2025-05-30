package com.dream.game.ai;

import static com.dream.game.ai.CtrlIntention.ACTIVE;
import static com.dream.game.ai.CtrlIntention.ATTACK;
import static com.dream.game.ai.CtrlIntention.CAST;
import static com.dream.game.ai.CtrlIntention.FOLLOW;
import static com.dream.game.ai.CtrlIntention.IDLE;
import static com.dream.game.ai.CtrlIntention.INTERACT;
import static com.dream.game.ai.CtrlIntention.MOVE_TO;
import static com.dream.game.ai.CtrlIntention.PICK_UP;
import static com.dream.game.ai.CtrlIntention.REST;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.serverpackets.AutoAttackStop;
import com.dream.game.taskmanager.AttackStanceTaskManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.tools.geometry.Point3D;
import com.dream.tools.random.Rnd;

public class L2CharacterAI extends AbstractAI
{
	public class IntentionCommand
	{
		protected CtrlIntention _crtlIntention;
		protected Object _arg0, _arg1;

		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			_crtlIntention = pIntention;
			_arg0 = pArg0;
			_arg1 = pArg1;
		}

		public CtrlIntention getCtrlIntention()
		{
			return _crtlIntention;
		}
	}

	protected class SelfAnalysis
	{
		public boolean isMage = false;
		public boolean isBalanced;
		public boolean isArcher = false;
		public boolean isHealer = false;
		public boolean isFighter = false;
		public boolean cannotMoveOnLand = false;
		public List<L2Skill> generalSkills = new ArrayList<>();
		public List<L2Skill> buffSkills = new ArrayList<>();
		public int lastBuffTick = 0;
		public List<L2Skill> debuffSkills = new ArrayList<>();
		public int lastDebuffTick = 0;
		public List<L2Skill> cancelSkills = new ArrayList<>();
		public List<L2Skill> healSkills = new ArrayList<>();
		public List<L2Skill> generalDisablers = new ArrayList<>();
		public List<L2Skill> sleepSkills = new ArrayList<>();
		public List<L2Skill> rootSkills = new ArrayList<>();
		public List<L2Skill> muteSkills = new ArrayList<>();
		public List<L2Skill> resurrectSkills = new ArrayList<>();
		public boolean hasHealOrResurrect = false;
		public boolean hasLongRangeSkills = false;
		public boolean hasLongRangeDamageSkills = false;
		public int maxCastRange = 0;

		public SelfAnalysis()
		{
		}

		public void init()
		{
			switch (((L2NpcTemplate) _actor.getTemplate()).getAI())
			{
				case FIGHTER:
					isFighter = true;
					break;
				case MAGE:
					isMage = true;
					break;
				case BALANCED:
					isBalanced = true;
					break;
				case ARCHER:
					isArcher = true;
					break;
				case HEALER:
					isHealer = true;
					break;
				default:
					isFighter = true;
					break;
			}
			if (_actor instanceof L2Npc)
			{
				switch (((L2Npc) _actor).getNpcId())
				{
					case 20314:
					case 20849:
						cannotMoveOnLand = true;
						break;
					default:
						cannotMoveOnLand = false;
						break;
				}
			}
			for (L2Skill sk : _actor.getAllSkills())
			{
				if (sk.isPassive())
				{
					continue;
				}
				int castRange = sk.getCastRange();
				boolean hasLongRangeDamageSkill = false;
				switch (sk.getSkillType())
				{
					case HEAL:
					case HEAL_PERCENT:
					case HEAL_STATIC:
					case BALANCE_LIFE:
					case HOT:
						healSkills.add(sk);
						hasHealOrResurrect = true;
						continue;
					case BUFF:
						buffSkills.add(sk);
						continue;
					case PARALYZE:
					case STUN:
						switch (sk.getId())
						{
							case 367:
							case 4111:
							case 4383:
							case 4616:
							case 4578:
								sleepSkills.add(sk);
								break;
							default:
								generalDisablers.add(sk);
								break;
						}
						break;
					case MUTE:
						muteSkills.add(sk);
						break;
					case SLEEP:
						sleepSkills.add(sk);
						break;
					case ROOT:
						rootSkills.add(sk);
						break;
					case FEAR:
					case CONFUSION:
					case DEBUFF:
						debuffSkills.add(sk);
						break;
					case CANCEL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case NEGATE:
						cancelSkills.add(sk);
						break;
					case RESURRECT:
						resurrectSkills.add(sk);
						hasHealOrResurrect = true;
						break;
					case NOTDONE:
					case COREDONE:
						continue;
					default:
						generalSkills.add(sk);
						hasLongRangeDamageSkill = true;
						break;
				}
				if (castRange > 70)
				{
					hasLongRangeSkills = true;
					if (hasLongRangeDamageSkill)
					{
						hasLongRangeDamageSkills = true;
					}
				}
				if (castRange > maxCastRange)
				{
					maxCastRange = castRange;
				}

			}
			if (!hasLongRangeDamageSkills && isMage)
			{
				isBalanced = true;
				isMage = false;
				isFighter = false;
			}
			if (!hasLongRangeSkills && (isMage || isBalanced))
			{
				isBalanced = false;
				isMage = false;
				isFighter = true;
			}
			if (generalSkills.isEmpty() && isMage)
			{
				isBalanced = true;
				isMage = false;
			}
		}
	}

	protected class TargetAnalysis
	{
		public L2Character character;
		public boolean isMage;
		public boolean isBalanced;
		public boolean isArcher;
		public boolean isFighter;
		public boolean isCanceled;
		public boolean isSlower;
		public boolean isMagicResistant;

		public TargetAnalysis()
		{

		}

		public void update(L2Character target)
		{
			if (target == character && Rnd.nextInt(100) > 25)
				return;
			character = target;
			if (target == null)
				return;
			isMage = false;
			isBalanced = false;
			isArcher = false;
			isFighter = false;
			isCanceled = false;

			if (target.getMAtk(null, null) > 1.5 * target.getPAtk(null))
			{
				isMage = true;
			}
			else if (target.getPAtk(null) * 0.8 < target.getMAtk(null, null) || target.getMAtk(null, null) * 0.8 > target.getPAtk(null))
			{
				isBalanced = true;
			}
			else
			{
				L2Weapon weapon = target.getActiveWeaponItem();
				if (weapon != null && weapon.getItemType() == L2WeaponType.BOW)
				{
					isArcher = true;
				}
				else
				{
					isFighter = true;
				}
			}
			isSlower = target.getRunSpeed() < _actor.getRunSpeed() - 3;

			isMagicResistant = target.getMDef(null, null) * 1.2 > _actor.getMAtk(null, null);

			if (target.getBuffCount() < 4)
			{
				isCanceled = true;
			}
		}
	}

	public L2CharacterAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}

	protected boolean checkTargetLost(L2Object target)
	{
		if (!L2World.getInstance().findObject(target))
		{
			setIntention(ACTIVE);
			return true;
		}
		return false;
	}

	protected boolean checkTargetLostOrDead(L2Character target)
	{
		if (target == null || target.isAlikeDead())
		{
			if (target != null && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
				return false;
			}

			setIntention(ACTIVE);
			return true;
		}
		return false;
	}

	public IntentionCommand getNextIntention()
	{
		return null;
	}

	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		if (target == null)
		{
			_log.info("maybeMoveToPawn: target == NULL!");
			return false;
		}
		if (offset < 0)
			return false;

		offset += _actor.getTemplate().getCollisionRadius();
		if (target instanceof L2Character)
		{
			offset += ((L2Character) target).getColRadius();
		}

		if (!_actor.isInsideRadius(target, offset, false, false))
		{
			if (getFollowTarget() != null)
			{
				if (!_actor.isInsideRadius(target, offset + 100, false, false))
					return true;
				stopFollow();
				return false;
			}

			if (_actor.isMovementDisabled())
				return true;

			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}

			stopFollow();
			if (target instanceof L2Character && !(target instanceof L2DoorInstance))
			{
				if (((L2Character) target).isMoving())
				{
					offset -= 100;
				}
				if (offset < 5)
				{
					offset = 5;
				}

				startFollow((L2Character) target, offset);
			}
			else
			{
				moveToPawn(target, offset);
			}

			return true;
		}

		if (getFollowTarget() != null)
		{
			stopFollow();
		}

		return false;
	}

	protected boolean maybeMoveToPosition(Point3D worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			_log.info("maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}

		if (offset < 0)
			return false;

		if (!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().getCollisionRadius(), false))
		{
			if (_actor.isMovementDisabled())
				return true;

			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}

			stopFollow();

			int x = _actor.getX();
			int y = _actor.getY();

			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;

			double dist = Math.sqrt(dx * dx + dy * dy);

			double sin = dy / dist;
			double cos = dx / dist;

			dist -= offset - 5;

			x += (int) (dist * cos);
			y += (int) (dist * sin);

			moveTo(x, y, worldPosition.getZ());
			return true;
		}

		if (getFollowTarget() != null)
		{
			stopFollow();
		}

		return false;
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{

	}

	@Override
	protected void onEvtArrived()
	{
		_accessor.getActor().revalidateZone(true);

		if (_accessor.getActor().moveToNextRoutePoint())
		{
			clientActionFailed();
			return;
		}

		if (_accessor.getActor() instanceof L2Attackable)
		{
			((L2Attackable) _accessor.getActor()).setisReturningToSpawnPoint(false);
		}

		clientStoppedMoving();

		if (getIntention() == MOVE_TO)
		{
			setIntention(ACTIVE);
		}

		onEvtThink();

		if (_actor instanceof L2BoatInstance)
		{
			((L2BoatInstance) _actor).evtArrived();
		}
	}

	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		if (getIntention() == MOVE_TO || getIntention() == CAST)
		{
			setIntention(ACTIVE);
		}

		clientStopMoving(blocked_at_pos);

		onEvtThink();
	}

	@Override
	protected void onEvtArrivedRevalidate()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (!(attacker instanceof L2Attackable) || !attacker.isCoreAIDisabled())
		{
			clientStartAutoAttack();
		}
	}

	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();

		stopFollow();

		if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		}

		onEvtThink();
	}

	@Override
	protected void onEvtConfused(L2Character attacker)
	{
		clientStopMoving(null);

		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtDead()
	{
		stopFollow();

		clientNotifyDead();

		if (!(_actor instanceof L2PcInstance))
		{
			_actor.setWalking();
		}
	}

	@Override
	protected void onEvtFakeDeath()
	{
		stopFollow();

		clientStopMoving(null);

		_intention = IDLE;
		setTarget(null);
		setCastTarget(null);
		setAttackTarget(null);
	}

	@Override
	protected void onEvtFinishCasting()
	{

	}

	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		if (getTarget() == object)
		{
			setTarget(null);

			if (getIntention() == INTERACT)
			{
				setIntention(ACTIVE);
			}
			else if (getIntention() == PICK_UP)
			{
				setIntention(ACTIVE);
			}
		}

		if (getAttackTarget() == object)
		{
			setAttackTarget(null);

			setIntention(ACTIVE);
		}

		if (getCastTarget() == object)
		{
			setCastTarget(null);

			setIntention(ACTIVE);
		}

		if (getFollowTarget() == object)
		{
			clientStopMoving(null);

			stopFollow();

			setIntention(ACTIVE);
		}

		if (_actor == object)
		{
			setTarget(null);
			setAttackTarget(null);
			setCastTarget(null);

			stopFollow();

			clientStopMoving(null);

			changeIntention(IDLE, null, null);
		}
	}

	@Override
	protected void onEvtMuted(L2Character attacker)
	{
		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtParalyzed(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		setAutoAttacking(false);

		clientStopMoving(null);

		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		clientStopMoving(null);

		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		setAutoAttacking(false);

		clientStopMoving(null);
	}

	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}

		setAutoAttacking(false);

		clientStopMoving(null);

		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtThink()
	{

	}

	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{

	}

	@Override
	protected void onIntentionActive()
	{
		if (getIntention() != ACTIVE)
		{
			changeIntention(ACTIVE, null, null);

			setCastTarget(null);
			setAttackTarget(null);

			clientStopMoving(null);

			clientStopAutoAttack();

			if (_actor instanceof L2Attackable)
			{
				((L2Npc) _actor).startRandomAnimationTimer();
			}

			onEvtThink();
		}
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if (target == null)
		{
			clientActionFailed();
			return;
		}

		if (getIntention() == REST)
		{
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			clientActionFailed();
			return;
		}
		if (target instanceof L2PcInstance && _actor instanceof L2PcInstance)
			if (((L2PcInstance) _actor).getKarma() > 0 && _actor.getLevel() - target.getLevel() >= 10 && ((L2Playable) target).getProtectionBlessing() && !target.isInsideZone(L2Zone.FLAG_PVP))
			{
				clientActionFailed();
				return;
			}
		if (getIntention() == ATTACK)
		{
			if (getAttackTarget() != target)
			{
				setAttackTarget(target);

				stopFollow();

				notifyEvent(CtrlEvent.EVT_THINK, null);

			}
			else
			{
				clientActionFailed();
			}

		}
		else
		{
			changeIntention(ATTACK, target, null);

			setAttackTarget(target);

			stopFollow();

			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if (getIntention() == REST && skill.isMagic())
		{
			clientActionFailed();
			_actor.setIsCastingNow(false);
			return;
		}

		if (target instanceof L2PcInstance && _actor instanceof L2PcInstance)
			if (((L2PcInstance) _actor).getKarma() > 0 && _actor.getLevel() - ((L2PcInstance) target).getLevel() >= 10 && ((L2Playable) target).getProtectionBlessing() && !((L2Character) target).isInsideZone(L2Zone.FLAG_PVP))
			{
				clientActionFailed();
				return;
			}
		setCastTarget((L2Character) target);

		if (skill.getHitTime() > 50)
		{
			_actor.abortAttack();
		}

		_skill = skill;

		changeIntention(CAST, skill, target);

		notifyEvent(CtrlEvent.EVT_THINK, null);
	}

	@Override
	protected void onIntentionFollow(L2Character target)
	{
		if (getIntention() == REST)
		{
			clientActionFailed();
			return;
		}
		if (_actor.isStunned() || _actor.isSleeping())
		{
			clientActionFailed();
			return;
		}
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}

		if (_actor.isImmobilized() || _actor.isRooted())
		{
			clientActionFailed();
			return;
		}

		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}

		if (_actor == target)
		{
			clientActionFailed();
			return;
		}

		clientStopAutoAttack();

		changeIntention(FOLLOW, target, null);

		startFollow(target);
	}

	@Override
	protected void onIntentionIdle()
	{
		changeIntention(IDLE, null, null);

		setCastTarget(null);
		setAttackTarget(null);

		clientStopMoving(null);

		clientStopAutoAttack();
	}

	@Override
	protected void onIntentionInteract(L2Object object)
	{
		if (getIntention() == REST)
		{
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}

		clientStopAutoAttack();

		if (getIntention() != INTERACT)
		{
			changeIntention(INTERACT, object, null);

			setTarget(object);

			moveToPawn(object, 60);
		}
	}

	@Override
	protected void onIntentionMoveTo(L2CharPosition pos)
	{
		if (getIntention() == REST)
		{
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}

		changeIntention(MOVE_TO, pos, null);

		clientStopAutoAttack();

		_actor.abortAttack();

		moveTo(pos.x, pos.y, pos.z);
	}

	@Override
	protected void onIntentionMoveToInABoat(L2CharPosition destination, L2CharPosition origin)
	{
		if (getIntention() == REST)
		{
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}

		clientStopAutoAttack();

		_actor.abortAttack();

		moveToInABoat(destination, origin);
	}

	@Override
	protected void onIntentionPickUp(L2Object object)
	{
		if (getIntention() == REST)
		{
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}

		if (object.getX() == 0 && object.getY() == 0)
		{
			if (object instanceof L2ItemInstance && ((L2ItemInstance) object).getLocation() != L2ItemInstance.ItemLocation.VOID)
			{
				L2PcInstance player = null;
				if (getActor() instanceof L2PcInstance)
				{
					player = (L2PcInstance) getActor();
				}
				else if (getActor() instanceof L2Summon)
				{
					player = ((L2Summon) getActor()).getOwner();
				}

				if (player != null)
				{
					_log.warn("Item coordinates is 0! :: Item location is: " + ((L2ItemInstance) object).getLocation() + " :: player name: " + player.getName());
					return;
				}
			}

			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}

		if (object instanceof L2ItemInstance && ((L2ItemInstance) object).getLocation() != ItemLocation.VOID)
			return;

		clientStopAutoAttack();

		changeIntention(PICK_UP, object, null);

		setTarget(object);

		moveToPawn(object, 20);
	}

	@Override
	protected void onIntentionRest()
	{
		setIntention(IDLE);
	}

	public void stopAITask()
	{

	}
}
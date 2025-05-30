package com.dream.game.ai;

import static com.dream.game.ai.CtrlIntention.ATTACK;
import static com.dream.game.ai.CtrlIntention.FOLLOW;
import static com.dream.game.ai.CtrlIntention.IDLE;

import org.apache.log4j.Logger;

import com.dream.game.GameTimeController;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.world.Location;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.AutoAttackStart;
import com.dream.game.network.serverpackets.AutoAttackStop;
import com.dream.game.network.serverpackets.Die;
import com.dream.game.network.serverpackets.MoveToLocation;
import com.dream.game.network.serverpackets.MoveToLocationInVehicle;
import com.dream.game.network.serverpackets.MoveToPawn;
import com.dream.game.network.serverpackets.StopMove;
import com.dream.game.network.serverpackets.StopRotation;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;
import com.dream.game.taskmanager.AttackStanceTaskManager;
import com.dream.game.templates.chars.L2CharTemplate;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.util.Util;

public abstract class AbstractAI implements Ctrl
{
	private static final class AttackFollowTaskManager extends AbstractIterativePeriodicTaskManager<AbstractAI>
	{
		private static final AttackFollowTaskManager _instance = new AttackFollowTaskManager();

		public static AttackFollowTaskManager getInstance()
		{
			return _instance;
		}

		private AttackFollowTaskManager()
		{
			super(ATTACK_FOLLOW_INTERVAL);
		}

		@Override
		protected void callTask(AbstractAI task)
		{
			task.followTarget();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "attackFollowTarget()";
		}
	}

	private static final class FollowTaskManager extends AbstractIterativePeriodicTaskManager<AbstractAI>
	{
		private static final FollowTaskManager _instance = new FollowTaskManager();

		public static FollowTaskManager getInstance()
		{
			return _instance;
		}

		private FollowTaskManager()
		{
			super(FOLLOW_INTERVAL);
		}

		@Override
		protected void callTask(AbstractAI task)
		{
			task.followTarget();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "followTarget()";
		}
	}

	protected static final Logger _log = Logger.getLogger(AbstractAI.class);

	private static final int FOLLOW_INTERVAL = 1000;

	private static final int ATTACK_FOLLOW_INTERVAL = 500;

	private NextAction _nextAction;

	private L2Character _followTarget;

	private int _followRange;

	protected final L2Character _actor;

	protected final L2Character.AIAccessor _accessor;

	protected CtrlIntention _intention = IDLE;

	protected Object _intentionArg0 = null;

	protected Object _intentionArg1 = null;

	protected boolean _clientMoving;

	protected boolean _clientAutoAttacking;

	protected int _clientMovingToPawnOffset;

	private L2Object _target;

	private L2Character _castTarget;

	protected L2Character _attackTarget;

	L2Skill _skill;

	private int _moveToPawnTimeout;

	protected AbstractAI(L2Character.AIAccessor accessor)
	{
		_accessor = accessor;

		_actor = accessor.getActor();
	}

	protected boolean canMoveTo(Location loc)
	{
		return true;
	}

	void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention = intention;
		_intentionArg0 = arg0;
		_intentionArg1 = arg1;
	}

	protected void clientActionFailed()
	{
		if (_actor instanceof L2PcInstance)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	protected void clientNotifyDead()
	{
		_actor.broadcastPacket(new Die(_actor));

		_intention = IDLE;
		_target = null;
		_castTarget = null;
		setAttackTarget(null);

		stopFollow();
	}

	public void clientStartAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}
		if (!isAutoAttacking())
		{
			if (_actor instanceof L2PcInstance && ((L2PcInstance) _actor).getPet() != null)
			{
				((L2PcInstance) _actor).getPet().broadcastPacket(new AutoAttackStart(((L2PcInstance) _actor).getPet().getObjectId()));
			}
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}

	public void clientStopAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		if (_actor instanceof L2PcInstance)
		{
			if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor) && isAutoAttacking())
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
			}
		}
		else if (isAutoAttacking())
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			setAutoAttacking(false);
		}
	}

	protected void clientStopMoving(L2CharPosition pos)
	{
		if (_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}

		_clientMovingToPawnOffset = 0;

		if (_clientMoving || pos != null)
		{
			_clientMoving = false;

			_actor.broadcastPacket(new StopMove(_actor));

			if (pos != null)
			{
				StopRotation sr = new StopRotation(_actor.getObjectId(), pos.heading, 65535);
				_actor.sendPacket(sr);
				_actor.broadcastPacket(sr);
			}
		}
	}

	protected void clientStoppedMoving()
	{
		if (_clientMovingToPawnOffset > 0)
		{
			_clientMovingToPawnOffset = 0;
			_actor.broadcastPacket(new StopMove(_actor));
		}
		_clientMoving = false;
		_actor.finishMovement();
	}

	public void describeStateToPlayer(L2PcInstance player)
	{
		if (_clientMoving)
			if (_clientMovingToPawnOffset != 0 && _followTarget != null)
			{
				player.sendPacket(new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset));
			}
			else
			{
				player.sendPacket(new MoveToLocation(_actor));
			}
	}

	public synchronized final void followTarget()
	{
		final double distance = Util.calculateDistance(_actor, _followTarget, true);

		if (distance > (_actor instanceof L2PcInstance && _followTarget instanceof L2PcInstance ? 2000 : 3000))
		{
			if (_actor instanceof L2Summon)
			{
				((L2Summon) _actor).setFollowStatus(false);
			}

			setIntention(IDLE);
			return;
		}
		else if (distance > _followRange)
		{
			moveToPawn(_followTarget, _followRange);
		}
	}

	@Override
	public L2Character getActor()
	{
		return _actor;
	}

	@Override
	public L2Character getAttackTarget()
	{
		return _attackTarget;
	}

	public L2Character getCastTarget()
	{
		return _castTarget;
	}

	protected L2Character getFollowTarget()
	{
		return _followTarget;
	}

	@Override
	public CtrlIntention getIntention()
	{
		return _intention;
	}

	public Object getIntentionArg0()
	{
		return _intentionArg0;
	}

	public Object getIntentionArg1()
	{
		return _intentionArg1;
	}

	public NextAction getNextAction()
	{
		return _nextAction;
	}

	protected L2Object getTarget()
	{
		return _target;
	}

	public boolean isAutoAttacking()
	{
		return _clientAutoAttacking;
	}

	protected void moveTo(int x, int y, int z)
	{
		moveTo(x, y, z, 0);
	}

	protected void moveTo(int x, int y, int z, int offset)
	{
		if (_actor.isMovementDisabled())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		_clientMoving = true;

		if (_accessor == null)
			return;

		_accessor.moveTo(x, y, z, offset);

		if (!_actor.isMoving())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		_actor.broadcastPacket(new MoveToLocation(_actor));
	}

	protected void moveToInABoat(L2CharPosition destination, L2CharPosition origin)
	{
		if (!_actor.isMovementDisabled())
		{
			if (((L2PcInstance) _actor).getBoat() != null)
			{
				_actor.broadcastPacket(new MoveToLocationInVehicle(_actor, destination, origin));
			}

		}
		else
		{
			clientActionFailed();
		}
	}

	protected void moveToPawn(L2Object pawn, int offset)
	{
		if (_clientMoving && _target == pawn && _actor.isOnGeodataPath() && GameTimeController.getGameTicks() < _moveToPawnTimeout)
			return;

		_target = pawn;
		if (_target == null)
			return;

		_moveToPawnTimeout = GameTimeController.getGameTicks() + 20;

		moveTo(_target.getX(), _target.getY(), _target.getZ(), offset = offset < 10 ? 10 : offset);

	}

	@Override
	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}

	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}

	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if (!_actor.isVisible() || !_actor.hasAI())
			return;
		switch (evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) arg0);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((L2Character) arg0);
				break;
			case EVT_PARALYZED:
				onEvtParalyzed((L2Character) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((L2Character) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((L2Character) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((L2Character) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((L2Character) arg0);
				break;
			case EVT_READY_TO_ACT:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtReadyToAct();
				}
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtArrived();
				}
				break;
			case EVT_ARRIVED_REVALIDATE:
				if (_actor.isMoving())
				{
					onEvtArrivedRevalidate();
				}
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((L2CharPosition) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) arg0);
				break;
			case EVT_CANCEL:
				onEvtCancel();
				break;
			case EVT_DEAD:
				onEvtDead();
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
			default:
				return;
		}

		if (_nextAction != null && _nextAction.getEvents().contains(evt))
		{
			_nextAction.doAction();
		}
	}

	protected abstract void onEvtAggression(L2Character target, int aggro);

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedBlocked(L2CharPosition blocked_at_pos);

	protected abstract void onEvtArrivedRevalidate();

	protected abstract void onEvtAttacked(L2Character attacker);

	protected abstract void onEvtCancel();

	protected abstract void onEvtConfused(L2Character attacker);

	protected abstract void onEvtDead();

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting();

	protected abstract void onEvtForgetObject(L2Object object);

	protected abstract void onEvtMuted(L2Character attacker);

	protected abstract void onEvtParalyzed(L2Character attacker);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtRooted(L2Character attacker);

	protected abstract void onEvtSleeping(L2Character attacker);

	protected abstract void onEvtStunned(L2Character attacker);

	protected abstract void onEvtThink();

	protected abstract void onEvtUserCmd(Object arg0, Object arg1);

	protected abstract void onIntentionActive();

	protected abstract void onIntentionAttack(L2Character target);

	protected abstract void onIntentionCast(L2Skill skill, L2Object target);

	protected abstract void onIntentionFollow(L2Character target);

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionInteract(L2Object object);

	protected abstract void onIntentionMoveTo(L2CharPosition destination);

	protected abstract void onIntentionMoveToInABoat(L2CharPosition destination, L2CharPosition origin);

	protected abstract void onIntentionPickUp(L2Object item);

	protected abstract void onIntentionRest();

	protected synchronized void setAttackTarget(L2Character target)
	{
		_attackTarget = target;
	}

	public void setAutoAttacking(boolean isAutoAttacking)
	{
		_clientAutoAttacking = isAutoAttacking;
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
			}
			return;
		}
	}

	protected synchronized void setCastTarget(L2Character target)
	{
		_castTarget = target;
	}

	@Override
	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}

	@Override
	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}

	@Override
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		L2CharTemplate template = _actor.getTemplate();
		if (template instanceof L2NpcTemplate)
		{
			Quest[] qlst = ((L2NpcTemplate) template).getEventQuests(Quest.QuestEventType.ON_INTENTION_CHANGE);
			if (qlst != null)
			{
				for (Quest q : qlst)
				{
					q.notifyIntentionChange(_actor, intention);
				}
			}
		}
		if (intention != FOLLOW && intention != ATTACK)
		{
			stopFollow();
		}

		switch (intention)
		{
			case IDLE:
				onIntentionIdle();
				break;
			case ACTIVE:
				onIntentionActive();
				break;
			case REST:
				onIntentionRest();
				break;
			case ATTACK:
				onIntentionAttack((L2Character) arg0);
				break;
			case CAST:
				onIntentionCast((L2Skill) arg0, (L2Object) arg1);
				break;
			case MOVE_TO:
				onIntentionMoveTo((L2CharPosition) arg0);
				break;
			case MOVE_TO_IN_A_BOAT:
				onIntentionMoveToInABoat((L2CharPosition) arg0, (L2CharPosition) arg1);
				break;
			case FOLLOW:
				onIntentionFollow((L2Character) arg0);
				break;
			case PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
		}

		if (_nextAction != null && _nextAction.getIntentions().contains(intention))
		{
			_nextAction = null;
		}
	}

	public void setNextAction(NextAction nextAction)
	{
		_nextAction = nextAction;
	}

	protected synchronized void setTarget(L2Object target)
	{
		_target = target;
	}

	public final synchronized void startFollow(L2Character target)
	{
		if (target == null)
		{
			stopFollow();
			return;
		}

		_followTarget = target;
		_followRange = 60;

		FollowTaskManager.getInstance().startTask(this);

		followTarget();
	}

	public final synchronized void startFollow(L2Character target, int range)
	{
		if (target == null)
		{
			stopFollow();
			return;
		}

		_followTarget = target;
		_followRange = range;

		AttackFollowTaskManager.getInstance().startTask(this);

		followTarget();
	}

	public final synchronized void stopFollow()
	{
		FollowTaskManager.getInstance().stopTask(this);
		AttackFollowTaskManager.getInstance().stopTask(this);

		_followTarget = null;
	}
}
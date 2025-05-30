package com.dream.game.ai;

import static com.dream.game.ai.CtrlIntention.ATTACK;
import static com.dream.game.ai.CtrlIntention.CAST;
import static com.dream.game.ai.CtrlIntention.IDLE;
import static com.dream.game.ai.CtrlIntention.INTERACT;
import static com.dream.game.ai.CtrlIntention.MOVE_TO;
import static com.dream.game.ai.CtrlIntention.PICK_UP;
import static com.dream.game.ai.CtrlIntention.REST;

import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Character.AIAccessor;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2StaticObjectInstance;
import com.dream.game.model.actor.position.L2CharPosition;

public class L2PlayerAI extends L2CharacterAI
{
	private volatile boolean _thinking;

	IntentionCommand _nextIntention = null;

	public L2PlayerAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention != CAST || arg0 != null && ((L2Skill) arg0).isOffensive())
		{
			_nextIntention = null;
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		if (intention == _intention && arg0 == _intentionArg0 && arg1 == _intentionArg1)
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		saveNextIntention(_intention, _intentionArg0, _intentionArg1);
		super.changeIntention(intention, arg0, arg1);
	}

	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;

		super.clientNotifyDead();
	}

	@Override
	public IntentionCommand getNextIntention()
	{
		return _nextIntention;
	}

	@Override
	protected void onEvtArrivedRevalidate()
	{
		getActor().getKnownList().updateKnownObjects();
		super.onEvtArrivedRevalidate();
	}

	@Override
	protected void onEvtCancel()
	{
		_nextIntention = null;
		super.onEvtCancel();
	}

	@Override
	protected void onEvtFinishCasting()
	{
		if (getIntention() == CAST)
		{
			IntentionCommand nextIntention = _nextIntention;
			if (nextIntention != null)
			{
				if (nextIntention._crtlIntention != CAST)
				{
					setIntention(nextIntention._crtlIntention, nextIntention._arg0, nextIntention._arg1);
				}
				else
				{
					setIntention(IDLE);
				}
			}
			else
			{
				setIntention(IDLE);
			}
		}
	}

	@Override
	protected void onEvtReadyToAct()
	{
		if (_nextIntention != null)
		{
			setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
			_nextIntention = null;
		}
		super.onEvtReadyToAct();
	}

	@Override
	protected void onEvtThink()
	{
		if (_thinking && getIntention() != CAST)
		{
			clientActionFailed();
			return;
		}

		_thinking = true;
		try
		{
			if (getIntention() == ATTACK)
			{
				thinkAttack();
			}
			else if (getIntention() == CAST)
			{
				thinkCast();
			}
			else if (getIntention() == PICK_UP)
			{
				thinkPickUp();
			}
			else if (getIntention() == INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}

	@Override
	protected void onIntentionActive()
	{
		setIntention(IDLE);
	}

	@Override
	protected void onIntentionMoveTo(L2CharPosition pos)
	{
		if (getIntention() == REST)
		{
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			saveNextIntention(MOVE_TO, pos, null);
			return;
		}

		changeIntention(MOVE_TO, pos, null);

		clientStopAutoAttack();

		_actor.abortAttack();

		moveTo(pos.x, pos.y, pos.z);
	}

	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != REST)
		{
			changeIntention(REST, null, null);
			setTarget(null);
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			clientStopMoving(null);
		}
	}

	void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_nextIntention = new IntentionCommand(intention, arg0, arg1);
	}

	private void thinkAttack()
	{
		L2Character target = getAttackTarget();
		if (target == null)
		{
			clientActionFailed();
			return;
		}

		if (checkTargetLostOrDead(target))
		{
			setAttackTarget(null);
			clientActionFailed();
			return;
		}

		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
		{
			clientActionFailed();
			return;
		}

		_accessor.doAttack(target);
	}

	private void thinkCast()
	{
		L2Character target = getCastTarget();

		if (_skill.getTargetType() == SkillTargetType.TARGET_GROUND && _actor instanceof L2PcInstance)
		{
			if (maybeMoveToPosition(((L2PcInstance) _actor).getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setIsCastingNow(false);
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				if (_skill.isOffensive() && getAttackTarget() != null)
				{
					setCastTarget(null);
				}
				_actor.setIsCastingNow(false);
				return;
			}
			if (target != null && maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setIsCastingNow(false);
				clientActionFailed();
				return;
			}
		}

		if (_skill.getHitTime() > 50)
		{
			clientStopMoving(null);
		}

		L2Object oldTarget = _actor.getTarget();
		if (oldTarget != null && target != null && oldTarget != target)
		{
			_actor.setTarget(getCastTarget());
			_accessor.doCast(_skill);
			_actor.setTarget(oldTarget);
		}
		else
		{
			_accessor.doCast(_skill);
		}
	}

	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			clientActionFailed();
			return;
		}
		if (maybeMoveToPawn(target, 36))
		{
			clientActionFailed();
			return;
		}
		if (!(target instanceof L2StaticObjectInstance))
		{
			((L2PcInstance.AIAccessor) _accessor).doInteract((L2Character) target);
		}
		setIntention(IDLE);
	}

	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			clientActionFailed();
			return;
		}
		if (maybeMoveToPawn(target, 36))
		{
			clientActionFailed();
			return;
		}
		setIntention(IDLE);
		((L2PcInstance.AIAccessor) _accessor).doPickupItem(target);
	}
}
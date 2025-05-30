package com.dream.game.ai;

import static com.dream.game.ai.CtrlIntention.ATTACK;
import static com.dream.game.ai.CtrlIntention.FOLLOW;
import static com.dream.game.ai.CtrlIntention.IDLE;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.grandbosses.QueenAntManager;
import com.dream.game.model.actor.L2Character.AIAccessor;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.serverpackets.AutoAttackStop;
import com.dream.game.taskmanager.AttackStanceTaskManager;

public class L2SummonAI extends L2CharacterAI
{
	private volatile boolean _thinking;
	private boolean _startFollow = ((L2Summon) _actor).getFollowStatus();

	public L2SummonAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		super.changeIntention(intention, arg0, arg1);
	}

	private boolean checkZone()
	{
		if (_actor.isInsideZone(L2Zone.FLAG_QUEEN))
			if (_actor.getActingPlayer().getLevel() > QueenAntManager.SAFE_LEVEL)
			{
				_actor.abortAttack();
				_actor.abortCast();
				_actor.getAI().setIntention(FOLLOW, _actor.getActingPlayer());
				SkillTable.getInstance().getInfo(4515, 1).getEffects(_actor.getActingPlayer(), _actor.getActingPlayer());
				// _actor.getActingPlayer().teleToLocation(TeleportWhereType.Town);
				_actor.abortAttack();
				_actor.abortCast();
				super.changeIntention(CtrlIntention.ACTIVE, null, null);
				return false;
			}
		return true;
	}

	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		switch (getIntention())
		{
			case ACTIVE:
			case FOLLOW:
			case IDLE:
				((L2Summon) _actor).setFollowStatus(_startFollow);
			default:
				return;
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
	}

	@Override
	protected void onEvtFinishCasting()
	{
		if (getIntention() != ATTACK)
		{
			((L2Summon) _actor).setFollowStatus(_startFollow);
		}
	}

	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
			return;
		_thinking = true;
		try
		{
			switch (getIntention())
			{
				case ATTACK:
					thinkAttack();
					break;
				case CAST:
					thinkCast();
					break;
				case PICK_UP:
					thinkPickUp();
					break;
				case INTERACT:
					thinkInteract();
					break;
				default:
					return;
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
		L2Summon summon = (L2Summon) _actor;
		if (_startFollow)
		{
			setIntention(FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}

	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		_startFollow = false;
		onIntentionActive();
	}

	public void setStartFollowController(boolean val)
	{
		_startFollow = val;
	}

	private void thinkAttack()
	{
		if (!checkZone())
			return;
		if (checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}
		if (_actor instanceof L2SiegeSummonInstance && ((L2SiegeSummonInstance) _actor).isOnSiegeMode())
			return;
		if (maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange()))
			return;
		clientStopMoving(null);
		_accessor.doAttack(getAttackTarget());
	}

	private void thinkCast()
	{
		if (!checkZone())
			return;
		L2Summon summon = (L2Summon) _actor;
		if (checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		boolean val = _startFollow;
		if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
			return;
		clientStopMoving(null);
		summon.setFollowStatus(false);
		setIntention(IDLE);
		_startFollow = val;
		_accessor.doCast(_skill);
	}

	private void thinkInteract()
	{
		if (checkTargetLost(getTarget()))
			return;
		if (maybeMoveToPawn(getTarget(), 36))
			return;

		setIntention(IDLE);
	}

	private void thinkPickUp()
	{
		if (checkTargetLost(getTarget()))
			return;
		if (maybeMoveToPawn(getTarget(), 36))
			return;
		if (!checkZone())
			return;

		setIntention(IDLE);
		((L2Summon.AIAccessor) _accessor).doPickupItem(getTarget());
	}
}
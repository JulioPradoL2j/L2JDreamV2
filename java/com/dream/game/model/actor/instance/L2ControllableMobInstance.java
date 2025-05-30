package com.dream.game.model.actor.instance;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2ControllableMobAI;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2ControllableMobInstance extends L2MonsterInstance
{
	protected class ControllableAIAcessor extends AIAccessor
	{
		@Override
		public void detachAI()
		{

		}
	}

	private boolean _isInvul;

	private L2ControllableMobAI _aiBackup;

	public L2ControllableMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void deleteMe()
	{
		removeAI();
		super.deleteMe();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		removeAI();
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 500;
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_aiBackup == null)
				{
					_ai = new L2ControllableMobAI(new ControllableAIAcessor());
					_aiBackup = (L2ControllableMobAI) _ai;
				}
				else
				{
					_ai = _aiBackup;
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isInvul() || isDead() || isPetrified())
			return;

		if (awake)
		{
			if (isSleeping())
			{
				stopSleeping(null);
			}
			if (isImmobileUntilAttacked())
			{
				stopImmobileUntilAttacked(null);
			}
		}

		i = getStatus().getCurrentHp() - i;

		if (i < 0)
		{
			i = 0;
		}

		getStatus().setCurrentHp(i);

		if (getStatus().getCurrentHp() < 0.5)
		{
			stopMove(null);
			doDie(attacker);
			getStatus().setCurrentHp(0);
		}
	}

	protected void removeAI()
	{
		synchronized (this)
		{
			if (_aiBackup != null)
			{
				_aiBackup.setIntention(CtrlIntention.IDLE);
				_aiBackup = null;
				_ai = null;
			}
		}
	}

	public void setInvul(boolean isInvul)
	{
		_isInvul = isInvul;
	}
}
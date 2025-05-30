package com.dream.game.model.actor.instance;

import java.util.concurrent.Future;

import com.dream.Config;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.status.SummonStatus;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.SetSummonRemainTime;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2SummonInstance extends L2Summon
{
	static class SummonConsume implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2SummonInstance _summon;

		SummonConsume(L2PcInstance activeChar, L2SummonInstance newpet)
		{
			_activeChar = activeChar;
			_summon = newpet;
		}

		@Override
		public void run()
		{
			try
			{
				double oldTimeRemaining = _summon.getTimeRemaining();
				int maxTime = _summon.getTotalLifeTime();
				double newTimeRemaining;

				if (_summon.isAttackingNow())
				{
					_summon.decTimeRemaining(_summon.getTimeLostActive());
				}
				else
				{
					_summon.decTimeRemaining(_summon.getTimeLostIdle());
				}

				newTimeRemaining = _summon.getTimeRemaining();
				if (newTimeRemaining < 0)
				{
					_summon.unSummon(_activeChar);
				}
				else if (newTimeRemaining <= _summon.getNextItemConsumeTime() && oldTimeRemaining > _summon.getNextItemConsumeTime())
				{
					_summon.decNextItemConsumeTime(maxTime / (_summon.getItemConsumeSteps() + 1));

					if (_summon.getItemConsumeCount() > 0 && _summon.getItemConsumeId() != 0 && !_summon.isDead() && !_summon.destroyItemByItemId("Consume", _summon.getItemConsumeId(), _summon.getItemConsumeCount(), _activeChar, true))
					{
						_summon.unSummon(_activeChar);
					}
				}

				if (_summon.lastShowntimeRemaining - newTimeRemaining > maxTime / 352)
				{
					_summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					_summon.lastShowntimeRemaining = (int) newTimeRemaining;
				}
			}
			catch (Exception e)
			{
				_log.error("Error on player [" + _activeChar.getName() + "] summon item consume task.", e);
			}
		}
	}

	private static final int SUMMON_LIFETIME_INTERVAL = 1200000;
	private static int _lifeTime = SUMMON_LIFETIME_INTERVAL;
	private float _expPenalty = 0;
	private int _itemConsumeId, _itemConsumeCount, _itemConsumeSteps, _totalLifeTime, _timeLostIdle;
	private int _timeRemaining, _timeLostActive, _nextItemConsumeTime;
	public int lastShowntimeRemaining;

	private Future<?> _summonConsumeTask;

	public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner);
		setShowSummonAnimation(true);

		if (owner.getPet() != null && owner.getPet().getTemplate().getNpcId() == template.getNpcId())
			return;

		_itemConsumeId = 0;
		_itemConsumeCount = 0;
		_itemConsumeSteps = 0;
		_totalLifeTime = 1200000;
		_timeLostIdle = 1000;
		_timeLostActive = 1000;

		if (skill != null)
		{
			_itemConsumeId = skill.getItemConsumeIdOT();
			_itemConsumeCount = skill.getItemConsumeOT();
			_itemConsumeSteps = skill.getItemConsumeSteps();
			_totalLifeTime = skill.getTotalLifeTime();
			_timeLostIdle = skill.getTimeLostIdle();
			_timeLostActive = skill.getTimeLostActive();
		}

		_timeRemaining = _totalLifeTime;
		lastShowntimeRemaining = _totalLifeTime;

		if (_itemConsumeId == 0)
		{
			_nextItemConsumeTime = -1;
		}
		else if (_itemConsumeSteps == 0)
		{
			_nextItemConsumeTime = -1;
		}
		else
		{
			_nextItemConsumeTime = _totalLifeTime - _totalLifeTime / (_itemConsumeSteps + 1);
		}

		int delay = 1000;
		_summonConsumeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonConsume(getOwner(), this), delay, delay);
		getOwner().sendPacket(new SetSummonRemainTime(getTotalLifeTime(), getTotalLifeTime()));
	}

	public void addExpAndSp(int addToExp, int addToSp)
	{
		getOwner().addExpAndSp(addToExp, addToSp);
	}

	public void decNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime -= value;
	}

	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}

	@Override
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}

	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		if (_summonConsumeTask != null)
		{
			_summonConsumeTask.cancel(true);
			_summonConsumeTask = null;
		}

		return true;
	}

	@Override
	public int getCurrentFed()
	{
		return _lifeTime;
	}

	public float getExpPenalty()
	{
		return _expPenalty;
	}

	public int getItemConsumeCount()
	{
		return _itemConsumeCount;
	}

	public int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	public int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}

	@Override
	public final int getLevel()
	{
		return getTemplate() != null ? getTemplate().getLevel() : 0;
	}

	@Override
	public int getMaxFed()
	{
		return SUMMON_LIFETIME_INTERVAL;
	}

	public int getNextItemConsumeTime()
	{
		return _nextItemConsumeTime;
	}

	@Override
	public final SummonStatus getStatus()
	{
		if (_status == null)
		{
			_status = new SummonStatus(this);
		}

		return (SummonStatus) _status;
	}

	@Override
	public int getSummonType()
	{
		return 1;
	}

	public int getTimeLostActive()
	{
		return _timeLostActive;
	}

	public int getTimeLostIdle()
	{
		return _timeLostIdle;
	}

	public int getTimeRemaining()
	{
		return _timeRemaining;
	}

	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}

	public void setExpPenalty(float expPenalty)
	{
		float ratePenalty = Config.ALT_GAME_SUMMON_PENALTY_RATE;
		_expPenalty = expPenalty * ratePenalty;
	}

	public void setNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime = value;
	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (_summonConsumeTask != null)
		{
			_summonConsumeTask.cancel(true);
			_summonConsumeTask = null;
		}
		super.unSummon(owner);
	}
}
/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.actor.status;

import org.apache.log4j.Logger;

import com.dream.game.ai.CtrlIntention;
import com.dream.game.manager.DuelManager;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PcInstance.ConditionListenerDependency;
import com.dream.game.model.actor.stat.CharStat;
import com.dream.game.model.entity.Duel;
import com.dream.game.model.quest.QuestState;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.skills.Formulas;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;
import com.dream.tools.random.Rnd;

public class CharStatus
{
	private static final class RegenTaskManager extends AbstractIterativePeriodicTaskManager<CharStatus>
	{
		private static final RegenTaskManager _instance = new RegenTaskManager();

		public static RegenTaskManager getInstance()
		{
			return _instance;
		}

		private RegenTaskManager()
		{
			super(1000);
		}

		@Override
		protected void callTask(CharStatus task)
		{
			task.regenTask();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "regenTask()";
		}

		@Override
		public boolean hasTask(CharStatus task)
		{
			return super.hasTask(task);
		}
	}

	protected static final Logger _log = Logger.getLogger(CharStatus.class);
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;

	private static final byte REGEN_FLAG_CP = 4;
	protected final L2Character _activeChar;

	private final int _period;
	private byte _flagsRegenActive = 0;
	private double _currentHp = 0;
	private double _currentMp = 0;

	private double _currentCp = 0;

	private long _runTime = System.currentTimeMillis();

	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
		_period = Formulas.getRegeneratePeriod(_activeChar);
	}

	boolean canReduceHp(double value, L2Character attacker, boolean awake, boolean isDOT)
	{
		if (attacker == null || getActiveChar().isDead() || getActiveChar().isPetrified())
			return false;

		if (getActiveChar().isInvul())
			return false;

		if (Duel.isInvul(attacker, getActiveChar()))
			return false;

		if (getActiveChar() instanceof L2Attackable)
			return ((L2Attackable) getActiveChar()).canReduceHp(value, attacker);
		return true;
	}

	protected L2Character getActiveChar()
	{
		return _activeChar;
	}

	public final double getCurrentCp()
	{
		return _currentCp;
	}

	public final double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentMp()
	{
		return _currentMp;
	}

	public final void increaseHp(double value)
	{
		setCurrentHp(getCurrentHp() + value);
	}

	public final void reduceCp(int value)
	{
		setCurrentCp(getCurrentCp() - value);
	}

	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}

	public final void reduceHp(double value, L2Character attacker, boolean awake)
	{
		reduceHp(value, attacker, awake, false);
	}

	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT)
	{
		if (!canReduceHp(value, attacker, awake, isDOT))
			return;

		reduceHp0(value, attacker, awake, isDOT);
	}

	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT)
	{
		if (!isDOT)
		{
			if (awake)
			{
				if (getActiveChar().isSleeping())
				{
					getActiveChar().stopSleeping(null);
				}
				if (getActiveChar().isImmobileUntilAttacked())
				{
					getActiveChar().stopImmobileUntilAttacked(null);
				}
			}

			if (getActiveChar().isStunned() && Rnd.get(10) == 0)
			{
				getActiveChar().stopStunning(null);
			}
		}
		else if (awake && getActiveChar() instanceof L2PcInstance)
			if (getActiveChar().isSleeping())
			{
				getActiveChar().stopSleeping(null);
			}

		final L2PcInstance player = getActiveChar().getActingPlayer();
		final L2PcInstance attackerPlayer = attacker.getActingPlayer();

		if (value > 0)
		{
			if (player != null && player.isInOlympiadMode() && attacker instanceof L2PcInstance && attackerPlayer.isInOlympiadMode())
			{
				attackerPlayer.addOlyDamage((int) value);
			}

			if (getActiveChar() instanceof L2Attackable)
				if (((L2Attackable) getActiveChar()).isOverhit())
				{
					((L2Attackable) getActiveChar()).setOverhitValues(attacker, value);
				}
				else
				{
					((L2Attackable) getActiveChar()).overhitEnabled(false);
				}
			value = getCurrentHp() - value;
			if (value <= 0)
				if (player != null && player.isInDuel() && getActiveChar() instanceof L2PcInstance)
				{
					getActiveChar().disableAllSkills();
					stopHpMpRegeneration();
					attacker.getAI().setIntention(CtrlIntention.ACTIVE);
					attacker.sendPacket(ActionFailed.STATIC_PACKET);

					DuelManager.getInstance().onPlayerDefeat(player);
					value = 1;
				}
				else
				{
					value = 0;
				}

			setCurrentHp(value);
		}
		else if (getActiveChar() instanceof L2Attackable)
		{
			((L2Attackable) getActiveChar()).overhitEnabled(false);
		}

		if (getActiveChar().getStatus().getCurrentHp() < 1)
		{
			if (player != null && player.isInOlympiadMode() && getActiveChar() instanceof L2PcInstance)
			{
				stopHpMpRegeneration();
				player.setIsDead(true);
				player.setIsPendingRevive(true);
				if (player.getPet() != null)
				{
					player.getPet().getAI().setIntention(CtrlIntention.IDLE, null);
				}
				return;
			}

			getActiveChar().doDie(attacker);

			if (player != null)
			{
				QuestState qs = player.getQuestState("255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE30", null, player);
				}
			}
		}
		else if (getActiveChar() instanceof L2Attackable)
		{
			((L2Attackable) getActiveChar()).overhitEnabled(false);
		}

		return;
	}

	public void reduceMp(double value)
	{
		setCurrentMp(getCurrentMp() - value);
	}

	public final void regenTask()
	{
		if (System.currentTimeMillis() < _runTime)
			return;

		_runTime += _period;

		CharStat cs = getActiveChar().getStat();

		if (getCurrentHp() == cs.getMaxHp() && getCurrentMp() == cs.getMaxMp() && getCurrentCp() == cs.getMaxCp())
		{
			stopHpMpRegeneration();
			return;
		}

		if (getCurrentHp() < cs.getMaxHp())
		{
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()));
		}

		if (getCurrentMp() < cs.getMaxMp())
		{
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()));
		}

		if (getCurrentCp() < cs.getMaxCp())
		{
			setCurrentCp(getCurrentCp() + Formulas.calcCpRegen(getActiveChar()));
		}
	}

	public final void setCurrentCp(double newCp)
	{
		if (getActiveChar().isDead())
			return;

		double maxCp = getActiveChar().getStat().getMaxCp();
		if (newCp < 0)
		{
			newCp = 0;
		}

		if (getActiveChar().getHealLimit() > 0)
			if (newCp > maxCp / 100 * getActiveChar().getHealLimit())
			{
				newCp = maxCp / 100 * getActiveChar().getHealLimit();
			}

		synchronized (this)
		{
			if (newCp >= maxCp)
			{
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;

				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;

				startHpMpRegeneration();
			}
		}

		getActiveChar().broadcastStatusUpdate();
	}

	public final void setCurrentHp(double newHp)
	{
		if (getActiveChar().isDead())
			return;

		double maxHp = getActiveChar().getStat().getMaxHp();
		if (newHp < 0)
		{
			newHp = 0;
		}

		if (getActiveChar().getHealLimit() > 0)
			if (newHp > maxHp / 100 * getActiveChar().getHealLimit())
			{
				newHp = maxHp / 100 * getActiveChar().getHealLimit();
			}

		synchronized (this)
		{
			if (newHp >= maxHp)
			{
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;

				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;

				startHpMpRegeneration();
			}
		}

		if (getActiveChar() instanceof L2PcInstance)
			if (getCurrentHp() <= maxHp * 0.3)
			{
				QuestState qs = ((L2PcInstance) getActiveChar()).getQuestState("255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE45", null, (L2PcInstance) getActiveChar());
				}
			}

		getActiveChar().broadcastStatusUpdate();

		if (getActiveChar() instanceof L2PcInstance)
		{
			((L2PcInstance) getActiveChar()).refreshConditionListeners(ConditionListenerDependency.PLAYER_HP);
		}
	}

	public final void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp);
		setCurrentMp(newMp);
	}

	public final void setCurrentMp(double newMp)
	{
		if (getActiveChar().isDead())
			return;

		double maxMp = getActiveChar().getStat().getMaxMp();
		if (newMp < 0)
		{
			newMp = 0;
		}

		if (getActiveChar().getHealLimit() > 0)
			if (newMp > maxMp / 100 * getActiveChar().getHealLimit())
			{
				newMp = maxMp / 100 * getActiveChar().getHealLimit();
			}

		synchronized (this)
		{
			if (newMp >= maxMp)
			{
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;

				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;

				startHpMpRegeneration();
			}
		}

		getActiveChar().broadcastStatusUpdate();
	}

	public synchronized final void startHpMpRegeneration()
	{
		if (!getActiveChar().isDead() && !RegenTaskManager.getInstance().hasTask(this))
		{
			RegenTaskManager.getInstance().startTask(this);

			_runTime = System.currentTimeMillis();
		}
	}

	public synchronized final void stopHpMpRegeneration()
	{
		_flagsRegenActive = 0;

		RegenTaskManager.getInstance().stopTask(this);
	}

}
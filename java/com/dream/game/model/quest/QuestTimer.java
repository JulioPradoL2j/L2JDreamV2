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
package com.dream.game.model.quest;

import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.ThreadPoolManager;

public class QuestTimer
{
	public class ScheduleTimerTask implements Runnable
	{
		@SuppressWarnings("null")
		@Override
		public void run()
		{
			if (this == null || !getIsActive())
				return;

			try
			{
				if (!getIsRepeating())
				{
					cancel();
				}

				getQuest().notifyEvent(getName(), getNpc(), getPlayer());
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	protected final static Logger _log = Logger.getLogger(QuestTimer.class.getName());

	private boolean _isActive = true;
	private final String _name;
	private final Quest _quest;
	private final L2Npc _npc;
	private final L2PcInstance _player;
	private final boolean _isRepeating;
	private ScheduledFuture<?> _scheduler;

	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player)
	{
		this(quest, name, time, npc, player, false);
	}

	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		_name = name;
		_quest = quest;
		_player = player;
		_npc = npc;
		_isRepeating = repeating;
		if (repeating)
		{
			_scheduler = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time);
		}
		else
		{
			_scheduler = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time);
		}
	}

	public QuestTimer(QuestState qs, String name, long time)
	{
		this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
	}

	public void cancel()
	{
		_isActive = false;

		if (_scheduler != null)
		{
			_scheduler.cancel(true);
		}

		getQuest().removeQuestTimer(this);
	}

	public final boolean getIsActive()
	{
		return _isActive;
	}

	public final boolean getIsRepeating()
	{
		return _isRepeating;
	}

	public final String getName()
	{
		return _name;
	}

	public final L2Npc getNpc()
	{
		return _npc;
	}

	public final L2PcInstance getPlayer()
	{
		return _player;
	}

	public final Quest getQuest()
	{
		return _quest;
	}

	public boolean isMatch(Quest quest, String name, L2Npc npc, L2PcInstance player)
	{
		if (quest == null || name == null)
			return false;
		if (quest != getQuest() || !name.equalsIgnoreCase(getName()))
			return false;
		return npc == getNpc() && player == getPlayer();
	}

	@Override
	public final String toString()
	{
		return _name;
	}
}
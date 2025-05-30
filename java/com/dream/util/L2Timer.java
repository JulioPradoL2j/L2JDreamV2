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
package com.dream.util;

import java.util.Timer;
import java.util.TimerTask;

import com.dream.util.concurrent.ExecuteWrapper;

public final class L2Timer extends Timer
{
	private static final class L2TimerTask extends TimerTask
	{
		private final Runnable _runnable;

		public L2TimerTask(Runnable runnable)
		{
			_runnable = runnable;
		}

		@Override
		public void run()
		{
			ExecuteWrapper.execute(_runnable);
		}
	}

	public L2Timer(String name)
	{
		super(name, true);

		scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				purge();
			}
		}, 600000, 600000);
	}

	public L2Timer schedule(Runnable runnable, long delay)
	{
		schedule(new L2TimerTask(runnable), delay);

		return this;
	}

	public L2Timer scheduleAtFixedRate(Runnable runnable, long delay, long period)
	{
		scheduleAtFixedRate(new L2TimerTask(runnable), delay, period);

		return this;
	}
}
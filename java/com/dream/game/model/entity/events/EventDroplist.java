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
package com.dream.game.model.entity.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.tools.random.DateRange;

public class EventDroplist
{

	public class DateDrop
	{
		public DateRange dateRange;

		public int[] items;

		public int min;

		public int max;

		public int chance;

	}

	private static class SingletonHolder
	{
		protected static final EventDroplist _instance = new EventDroplist();
	}

	private final static Logger _log = Logger.getLogger(EventDroplist.class.getName());

	public static EventDroplist getInstance()
	{
		return SingletonHolder._instance;
	}

	private final List<DateDrop> _allNpcDateDrops;

	protected EventDroplist()
	{
		_allNpcDateDrops = new ArrayList<>();
	}

	public void addGlobalDrop(int[] items, int[] count, int chance, DateRange range)
	{
		DateDrop date = new DateDrop();

		date.dateRange = range;
		date.items = items;
		date.min = count[0];
		date.max = count[1];
		date.chance = chance;

		_allNpcDateDrops.add(date);
	}

	public List<DateDrop> getAllDrops()
	{
		List<DateDrop> list = new ArrayList<>();

		for (DateDrop drop : _allNpcDateDrops)
		{
			Date currentDate = new Date();
			if (drop.dateRange.isWithinRange(currentDate))
			{
				list.add(drop);
			}
		}

		return list;
	}

	public void getAllDropsDates(String QuestName)
	{
		for (DateDrop drop : _allNpcDateDrops)
		{
			Date currentDate = new Date();
			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.debug("Event :: " + QuestName + " : Date Range From: " + drop.dateRange.getStartDate() + " To: " + drop.dateRange.getEndDate() + " Now: " + currentDate);
			}
		}
	}
}
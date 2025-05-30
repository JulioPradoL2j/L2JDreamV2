/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.game.util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ClassMasterSettings
{
	private final Map<Integer, HashMap<Integer, Integer>> _claimItems;
	private final Map<Integer, HashMap<Integer, Integer>> _rewardItems;
	private final Map<Integer, Boolean> _allowedClassChange;

	public ClassMasterSettings(String _configLine)
	{
		_claimItems = new HashMap<>();
		_rewardItems = new HashMap<>();
		_allowedClassChange = new HashMap<>();
		if (_configLine != null)
		{
			parseConfigLine(_configLine.trim());
		}
	}

	public Map<Integer, Integer> getRequireItems(int job)
	{
		if (_claimItems.containsKey(job))
			return _claimItems.get(job);
		return null;
	}

	public Map<Integer, Integer> getRewardItems(int job)
	{
		if (_rewardItems.containsKey(job))
			return _rewardItems.get(job);
		return null;
	}

	public boolean isAllowed(int job)
	{
		if (_allowedClassChange == null)
			return false;
		if (_allowedClassChange.containsKey(job))
			return _allowedClassChange.get(job);
		return false;
	}

	private void parseConfigLine(String _configLine)
	{
		StringTokenizer st = new StringTokenizer(_configLine, ";");

		while (st.hasMoreTokens())
		{
			int job = Integer.parseInt(st.nextToken());

			_allowedClassChange.put(job, true);

			HashMap<Integer, Integer> _items = new HashMap<>();

			if (st.hasMoreTokens())
			{
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");

				while (st2.hasMoreTokens())
				{
					StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					int _itemId = Integer.parseInt(st3.nextToken());
					int _quantity = Integer.parseInt(st3.nextToken());
					_items.put(_itemId, _quantity);
				}
			}

			_claimItems.put(job, _items);
			_items = new HashMap<>();

			if (st.hasMoreTokens())
			{
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");

				while (st2.hasMoreTokens())
				{
					StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					int _itemId = Integer.parseInt(st3.nextToken());
					int _quantity = Integer.parseInt(st3.nextToken());
					_items.put(_itemId, _quantity);
				}
			}
			_rewardItems.put(job, _items);
		}
	}

}
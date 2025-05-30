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
package com.dream.game.model;

import java.util.ArrayList;
import java.util.List;

public class L2ManufactureList
{
	private List<L2ManufactureItem> _list;
	private boolean _confirmed;
	private String _manufactureStoreName;

	public L2ManufactureList()
	{
		_list = new ArrayList<>();
		_confirmed = false;
	}

	public void add(L2ManufactureItem item)
	{
		_list.add(item);
	}

	public List<L2ManufactureItem> getList()
	{
		return _list;
	}

	public String getStoreName()
	{
		return _manufactureStoreName;
	}

	public boolean hasConfirmed()
	{
		return _confirmed;
	}

	public void setConfirmedTrade(boolean x)
	{
		_confirmed = x;
	}

	public void setList(List<L2ManufactureItem> list)
	{
		_list = list;
	}

	public void setStoreName(String manufactureStoreName)
	{
		_manufactureStoreName = manufactureStoreName;
	}

	public int size()
	{
		return _list.size();
	}
}
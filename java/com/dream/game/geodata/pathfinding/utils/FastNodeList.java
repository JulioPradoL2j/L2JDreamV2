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
package com.dream.game.geodata.pathfinding.utils;

import com.dream.game.geodata.pathfinding.Node;

public class FastNodeList
{
	private final Node[] _list;
	private int _size;

	public FastNodeList(int size)
	{
		_list = new Node[size];
	}

	public void add(Node n)
	{
		_list[_size++] = n;
	}

	public boolean contains(Node n)
	{
		for (int i = 0; i < _size; i++)
			if (_list[i].equals(n))
				return true;
		return false;
	}

	public boolean containsRev(Node n)
	{
		for (int i = _size - 1; i >= 0; i--)
			if (_list[i].equals(n))
				return true;
		return false;
	}
}
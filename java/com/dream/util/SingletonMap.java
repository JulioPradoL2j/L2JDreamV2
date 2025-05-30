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
package com.dream.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

public final class SingletonMap<K, V> implements Map<K, V>
{
	private FastMap<K, V> _map;

	@Override
	public void clear()
	{
		get(false).clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return get(false).containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return get(false).containsValue(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return get(false).entrySet();
	}

	private Map<K, V> get(boolean init)
	{
		if (_map == null)
			if (init)
			{
				_map = new FastMap<K, V>().shared();
			}
			else
				return L2Collections.emptyMap();

		return _map;
	}

	@Override
	public V get(Object key)
	{
		return get(false).get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return get(false).isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return get(false).keySet();
	}

	@Override
	public V put(K key, V value)
	{
		return get(true).put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		get(true).putAll(m);
	}

	@Override
	public V remove(Object key)
	{
		return get(false).remove(key);
	}

	public SingletonMap<K, V> setShared()
	{

		if (_map != null)
		{
			_map.shared();
		}

		return this;
	}

	@Override
	public int size()
	{
		return get(false).size();
	}

	@Override
	public Collection<V> values()
	{
		return get(false).values();
	}
}
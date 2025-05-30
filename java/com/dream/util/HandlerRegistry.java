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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class HandlerRegistry<K, V>
{
	public static final Logger _log = Logger.getLogger(HandlerRegistry.class);

	private final HashMap<K, V> _map = new HashMap<>();

	public final V get(K key)
	{
		return _map.get(key);
	}

	public Map<K, V> getHandlers()
	{
		return Collections.unmodifiableMap(_map);
	}

	public final void register(K key, V handler)
	{
		V old = _map.put(key, handler);

		if (old != null)
		{
			_log.warn(getClass().getSimpleName() + ": Replaced type(" + key + "), " + old + " -> " + handler + ".");
		}
	}

	@SafeVarargs
	public final void registerAll(V handler, K... keys)
	{
		for (K key : keys)
		{
			register(key, handler);
		}
	}

	public final int size()
	{
		return _map.size();
	}
}
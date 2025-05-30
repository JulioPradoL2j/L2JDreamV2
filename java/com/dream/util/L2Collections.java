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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class L2Collections
{
	private static final class ConcatenatedIterable<E> implements Iterable<E>
	{
		private final Iterable<? extends E>[] _iterables;

		public ConcatenatedIterable(Iterable<? extends E>... iterables)
		{
			_iterables = iterables;
		}

		@Override
		public Iterator<E> iterator()
		{
			return concatenatedIterator(_iterables);
		}
	}

	private static final class ConcatenatedIterator<E> implements Iterator<E>
	{
		private final Iterable<? extends E>[] _iterables;

		private Iterator<? extends E> _iterator;
		private int _index = -1;

		public ConcatenatedIterator(Iterable<? extends E>... iterables)
		{
			_iterables = iterables;

			validateIterator();
		}

		@Override
		public boolean hasNext()
		{
			validateIterator();

			return _iterator != null && _iterator.hasNext();
		}

		@Override
		public E next()
		{
			if (!hasNext())
				throw new NoSuchElementException();

			return _iterator.next();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		private void validateIterator()
		{
			while (_iterator == null || !_iterator.hasNext())
			{
				_index++;

				if (_index >= _iterables.length)
					return;

				_iterator = _iterables[_index].iterator();
			}
		}
	}

	private static final class EmptyBunch implements Bunch<Object>
	{
		public static final Bunch<Object> INSTANCE = new EmptyBunch();

		@Override
		public Bunch<Object> add(Object e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bunch<Object> addAll(Iterable<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bunch<Object> addAll(Object[] array)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bunch<Object> cleanByFilter(Filter<Object> filter)
		{
			return this;
		}

		@Override
		public void clear()
		{
		}

		@Override
		public boolean contains(Object o)
		{
			return false;
		}

		@Override
		public Object get(int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		public Object[] moveToArray()
		{
			return EMPTY_ARRAY;
		}

		@Override
		public <T> T[] moveToArray(Class<T> clazz)
		{
			return (T[]) Array.newInstance(clazz, 0);
		}

		@Override
		public <T> T[] moveToArray(T[] array)
		{
			if (array.length != 0)
			{
				array = (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
			}

			return array;
		}

		@Override
		public List<Object> moveToList(List<Object> list)
		{
			return list;
		}

		@Override
		public Object remove(int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bunch<Object> remove(Object o)
		{
			return this;
		}

		@Override
		public Object set(int index, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int size()
		{
			return 0;
		}
	}

	private static class EmptyCollection implements Collection<Object>
	{
		public static final Collection<Object> INSTANCE = new EmptyCollection();

		@Override
		public boolean add(Object e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
		}

		@Override
		public boolean contains(Object o)
		{
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		public Iterator<Object> iterator()
		{
			return emptyListIterator();
		}

		@Override
		public boolean remove(Object o)
		{
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public Object[] toArray()
		{
			return EMPTY_ARRAY;
		}

		@Override
		public <T> T[] toArray(T[] a)
		{
			if (a.length != 0)
			{
				a = (T[]) Array.newInstance(a.getClass().getComponentType(), 0);
			}

			return a;
		}
	}


	public static final class EmptyList extends EmptyCollection implements List<Object>
	{
		public static final List<Object> INSTANCE = new EmptyList();

		@Override
		public void add(int index, Object element)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object get(int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int indexOf(Object o)
		{
			return -1;
		}

		@Override
		public int lastIndexOf(Object o)
		{
			return -1;
		}

		@Override
		public ListIterator<Object> listIterator()
		{
			return emptyListIterator();
		}

		@Override
		public ListIterator<Object> listIterator(int index)
		{
			return emptyListIterator();
		}

		@Override
		public Object remove(int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object set(int index, Object element)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Object> subList(int fromIndex, int toIndex)
		{
			throw new UnsupportedOperationException();
		}
	}

	private static final class EmptyListIterator implements ListIterator<Object>
	{
		public static final ListIterator<Object> INSTANCE = new EmptyListIterator();

		@Override
		public void add(Object obj)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public boolean hasPrevious()
		{
			return false;
		}

		@Override
		public Object next()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int nextIndex()
		{
			return 0;
		}

		@Override
		public Object previous()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int previousIndex()
		{
			return -1;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(Object obj)
		{
			throw new UnsupportedOperationException();
		}
	}

	private static final class EmptyMap implements Map<Object, Object>
	{
		public static final Map<Object, Object> INSTANCE = new EmptyMap();

		@Override
		public void clear()
		{
		}

		@Override
		public boolean containsKey(Object key)
		{
			return false;
		}

		@Override
		public boolean containsValue(Object value)
		{
			return false;
		}

		@Override
		public Set<Map.Entry<Object, Object>> entrySet()
		{
			return emptySet();
		}

		@Override
		public Object get(Object key)
		{
			return null;
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		public Set<Object> keySet()
		{
			return emptySet();
		}

		@Override
		public Object put(Object key, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends Object, ? extends Object> m)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove(Object key)
		{
			return null;
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public Collection<Object> values()
		{
			return emptyCollection();
		}
	}


	public static final class EmptySet extends EmptyCollection implements Set<Object>
	{
		public static final Set<Object> INSTANCE = new EmptySet();
	}

	public interface Filter<E>
	{
		public boolean accept(E element);
	}

	private static final class FilteredIterable<E> implements Iterable<E>
	{
		private final Iterable<? super E> _iterable;
		private final Filter<E> _filter;
		private final Class<E> _clazz;

		public FilteredIterable(Class<E> clazz, Iterable<? super E> iterable, Filter<E> filter)
		{
			_iterable = iterable;
			_filter = filter;
			_clazz = clazz;
		}

		@Override
		public Iterator<E> iterator()
		{
			return filteredIterator(_clazz, _iterable, _filter);
		}
	}

	private static final class FilteredIterator<E> implements Iterator<E>
	{
		private final Iterator<? super E> _iterator;
		private final Filter<E> _filter;
		private final Class<E> _clazz;

		private E _next;

		public FilteredIterator(Class<E> clazz, Iterable<? super E> iterable, Filter<E> filter)
		{
			_iterator = iterable.iterator();
			_filter = filter;
			_clazz = clazz;

			step();
		}

		@Override
		public boolean hasNext()
		{
			return _next != null;
		}

		@Override
		public E next()
		{
			if (!hasNext())
				throw new NoSuchElementException();

			E next = _next;

			step();

			return next;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		private void step()
		{
			while (_iterator.hasNext())
			{
				Object next = _iterator.next();

				if (next == null || !_clazz.isInstance(next))
				{
					continue;
				}

				if (_filter == null || _filter.accept((E) next))
				{
					_next = (E) next;
					return;
				}
			}

			_next = null;
		}
	}

	public static final Object[] EMPTY_ARRAY = new Object[0];

	public static <T> Iterable<T> concatenatedIterable(Iterable<? extends T>... iterables)
	{
		return new ConcatenatedIterable<>(iterables);
	}

	public static <T> Iterable<T> concatenatedIterable(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2)
	{
		return new ConcatenatedIterable<>(iterable1, iterable2);
	}

	public static <T> Iterable<T> concatenatedIterable(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2, Iterable<? extends T> iterable3)
	{
		return new ConcatenatedIterable<>(iterable1, iterable2, iterable3);
	}

	public static <T> Iterator<T> concatenatedIterator(Iterable<? extends T>... iterables)
	{
		return new ConcatenatedIterator<>(iterables);
	}

	public static <T> Iterator<T> concatenatedIterator(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2)
	{
		return new ConcatenatedIterator<>(iterable1, iterable2);
	}

	public static <T> Iterator<T> concatenatedIterator(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2, Iterable<? extends T> iterable3)
	{
		return new ConcatenatedIterator<>(iterable1, iterable2, iterable3);
	}

	public static <T> Bunch<T> emptyBunch()
	{
		return (Bunch<T>) EmptyBunch.INSTANCE;
	}

	public static <T> Collection<T> emptyCollection()
	{
		return (Collection<T>) EmptyCollection.INSTANCE;
	}

	public static <T> List<T> emptyList()
	{
		return (List<T>) EmptyList.INSTANCE;
	}

	public static <T> ListIterator<T> emptyListIterator()
	{
		return (ListIterator<T>) EmptyListIterator.INSTANCE;
	}

	public static <K, V> Map<K, V> emptyMap()
	{
		return (Map<K, V>) EmptyMap.INSTANCE;
	}

	public static <T> Set<T> emptySet()
	{
		return (Set<T>) EmptySet.INSTANCE;
	}

	public static <T> Iterable<T> filteredIterable(Class<T> clazz, Iterable<? super T> iterable)
	{
		return filteredIterable(clazz, iterable, null);
	}

	public static <T> Iterable<T> filteredIterable(Class<T> clazz, Iterable<? super T> iterable, Filter<T> filter)
	{
		return new FilteredIterable<>(clazz, iterable, filter);
	}

	public static <T> Iterator<T> filteredIterator(Class<T> clazz, Iterable<? super T> iterable)
	{
		return filteredIterator(clazz, iterable, null);
	}

	public static <T> Iterator<T> filteredIterator(Class<T> clazz, Iterable<? super T> iterable, Filter<T> filter)
	{
		return new FilteredIterator<>(clazz, iterable, filter);
	}
}
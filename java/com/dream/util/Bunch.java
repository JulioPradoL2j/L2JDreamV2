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

import java.util.List;

import com.dream.util.L2Collections.Filter;

public interface Bunch<E>
{
	public Bunch<E> add(E value);

	public Bunch<E> addAll(E[] array);

	public Bunch<E> addAll(Iterable<? extends E> c);

	public Bunch<E> cleanByFilter(Filter<E> filter);

	public void clear();

	public boolean contains(E value);

	public E get(int index);

	public boolean isEmpty();

	public Object[] moveToArray();

	public <T> T[] moveToArray(Class<T> clazz);

	public <T> T[] moveToArray(T[] array);

	public List<E> moveToList(List<E> list);

	public Bunch<E> remove(E value);

	public E remove(int index);

	public E set(int index, E value);

	public int size();
}
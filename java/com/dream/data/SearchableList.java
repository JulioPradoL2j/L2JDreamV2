package com.dream.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SearchableList<T> extends Searchable<T> implements List<T>
{

	private final ArrayList<T> _container = new ArrayList<>();

	@Override
	public void add(int index, T element)
	{
		_container.add(index, element);
	}

	@Override
	public boolean add(T e)
	{
		return _container.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		return _container.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		return _container.addAll(index, c);
	}

	@Override
	public void clear()
	{
		_container.clear();

	}

	@Override
	public boolean contains(Object o)
	{
		return _container.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return _container.containsAll(c);
	}

	@Override
	public T get(int index)
	{
		return _container.get(index);
	}

	@Override
	public int indexOf(Object o)
	{
		return _container.lastIndexOf(o);
	}

	@Override
	public boolean isEmpty()
	{
		return _container.isEmpty();
	}

	@Override
	public Iterator<T> iterator()
	{
		return _container.iterator();
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return _container.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return _container.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index)
	{
		return _container.listIterator(index);
	}

	@Override
	public T remove(int index)
	{
		return _container.remove(index);
	}

	@Override
	public boolean remove(Object o)
	{
		return _container.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return _container.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return _container.retainAll(c);
	}

	@Override
	public T set(int index, T element)
	{
		return _container.set(index, element);
	}

	@Override
	public int size()
	{
		return _container.size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		return _container.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray()
	{
		return _container.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E[] toArray(E[] a)
	{
		return (E[]) _container.toArray();
	}

}
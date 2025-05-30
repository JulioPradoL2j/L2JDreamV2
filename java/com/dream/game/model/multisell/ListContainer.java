package com.dream.game.model.multisell;

import java.util.ArrayList;
import java.util.List;

public class ListContainer
{
	private int _listId;
	private boolean _applyTaxes = false;
	private boolean _maintainEnchantment = false;
	private int _maintainEnchantmentMinLvl = 0;

	List<Entry> _entriesC;

	public ListContainer()
	{
		_entriesC = new ArrayList<>();
	}

	public void addEntry(Entry e)
	{
		_entriesC.add(e);
	}

	public boolean getApplyTaxes()
	{
		return _applyTaxes;
	}

	public List<Entry> getEntries()
	{
		return _entriesC;
	}

	public int getListId()
	{
		return _listId;
	}

	public boolean getMaintainEnchantment()
	{
		return _maintainEnchantment;
	}

	public int getMaintainEnchantmentLvl()
	{
		return _maintainEnchantmentMinLvl;
	}

	public void setApplyTaxes(boolean applyTaxes)
	{
		_applyTaxes = applyTaxes;
	}

	public void setListId(int listId)
	{
		_listId = listId;
	}

	public void setMaintainEnchantment(boolean maintainEnchantment)
	{
		_maintainEnchantment = maintainEnchantment;
	}

	public void setMaintainEnchantmentLvl(int par)
	{
		_maintainEnchantmentMinLvl = par;
	}
}
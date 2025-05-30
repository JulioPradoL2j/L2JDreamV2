package com.dream.game.model.multisell;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.datatables.sql.ItemTable;

public class Entry
{
	private int _entryId;

	private final List<Ingredient> _products = new ArrayList<>();
	private final List<Ingredient> _ingredients = new ArrayList<>();

	public void addIngredient(final Ingredient ingredient)
	{
		_ingredients.add(ingredient);
	}

	public void addProduct(final Ingredient product)
	{
		_products.add(product);
	}

	public int getEntryId()
	{
		return _entryId;
	}

	public List<Ingredient> getIngredients()
	{
		return _ingredients;
	}

	public List<Ingredient> getProducts()
	{
		return _products;
	}

	public void setEntryId(final int entryId)
	{
		_entryId = entryId;
	}

	public int stackable()
	{
		for (Ingredient p : _products)
			if (!ItemTable.getInstance().createDummyItem(p.getItemId()).isStackable())
				return 0;
		return 1;
	}
}
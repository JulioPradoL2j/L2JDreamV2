package com.dream.game.model.multisell;

public class Ingredient
{
	private int _itemId, _itemCount, _enchantmentLevel, _augmentationId, _manaLeft;
	private boolean _isTaxIngredient, _mantainIngredient;

	public Ingredient(Ingredient e)
	{
		_itemId = e.getItemId();
		_itemCount = e.getItemCount();
		_enchantmentLevel = e.getEnchantmentLevel();
		_isTaxIngredient = e.isTaxIngredient();
		_mantainIngredient = e.getMantainIngredient();
	}

	public Ingredient(int itemId, int itemCount, boolean isTaxIngredient, boolean mantainIngredient)
	{
		this(itemId, itemCount, 0, isTaxIngredient, mantainIngredient);
	}

	public Ingredient(int itemId, int itemCount, int enchantmentLevel, boolean isTaxIngredient, boolean mantainIngredient)
	{
		setItemId(itemId);
		setItemCount(itemCount);
		setEnchantmentLevel(enchantmentLevel);
		setIsTaxIngredient(isTaxIngredient);
		setMantainIngredient(mantainIngredient);
	}

	public int getAugmentationId()
	{
		return _augmentationId;
	}

	public int getEnchantmentLevel()
	{
		return _enchantmentLevel;
	}

	public int getItemCount()
	{
		return _itemCount;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getManaLeft()
	{
		return _manaLeft;
	}

	public boolean getMantainIngredient()
	{
		return _mantainIngredient;
	}

	public boolean isTaxIngredient()
	{
		return _isTaxIngredient;
	}

	public void setAugmentationId(int id)
	{
		_augmentationId = id;
	}

	public void setEnchantmentLevel(int enchantmentLevel)
	{
		_enchantmentLevel = enchantmentLevel;
	}

	public void setIsTaxIngredient(boolean isTaxIngredient)
	{
		_isTaxIngredient = isTaxIngredient;
	}

	public void setItemCount(int itemCount)
	{
		_itemCount = itemCount;
	}

	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	public void setManaLeft(int mana)
	{
		_manaLeft = mana;
	}

	public void setMantainIngredient(boolean mantainIngredient)
	{
		_mantainIngredient = mantainIngredient;
	}
}
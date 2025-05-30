package com.dream.game.templates.item;

public enum L2EtcItemType implements AbstractL2ItemType
{
	ARROW(0, "Arrow"),
	MATERIAL(1, "Material"),
	PET_COLLAR(2, "PetCollar"),
	POTION(3, "Potion"),
	RECEIPE(4, "Receipe"),
	SCROLL(5, "Scroll"),
	QUEST(6, "Quest"),
	MONEY(7, "Money"),
	OTHER(8, "Other"),
	SPELLBOOK(9, "Spellbook"),
	SEED(10, "Seed"),
	SHOT(11, "Shot"),
	HERB(12, "Herb");

	final int _id;
	final String _name;

	L2EtcItemType(int id, String name)
	{
		_id = id;
		_name = name;
	}

	@Override
	public int mask()
	{
		return 1 << _id + 21;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}
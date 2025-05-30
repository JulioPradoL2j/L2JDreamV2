package com.dream.game.templates.item;

public enum L2WeaponType implements AbstractL2ItemType
{
	NONE(1, "Shield"),
	SWORD(2, "Sword"),
	BLUNT(3, "Blunt"),
	DAGGER(4, "Dagger"),
	BOW(5, "Bow"),
	POLE(6, "Pole"),
	ETC(7, "Etc"),
	FIST(8, "Fist"),
	DUAL(9, "Dual Sword", "DualSword"),
	DUALFIST(10, "Dual Fist", "DualFist"),
	BIGSWORD(11, "Big Sword", "BigSword"),
	PET(12, "Pet"),
	ROD(13, "Rod"),
	BIGBLUNT(14, "Big Blunt", "BigBlunt");

	public static final L2WeaponType[] VALUES = L2WeaponType.values();
	private final int _id;
	private final String _name;

	private final String _shortname;

	private L2WeaponType(int id, String name)
	{
		_id = id;
		_name = name;
		_shortname = name;

	}

	private L2WeaponType(int id, String name, String shortName)
	{
		_id = id;
		_name = name;
		_shortname = shortName;
	}

	public boolean isBowType()
	{
		return this == BOW;
	}

	@Override
	public int mask()
	{
		return 1 << _id;
	}

	public String shortName()
	{
		return _shortname;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}
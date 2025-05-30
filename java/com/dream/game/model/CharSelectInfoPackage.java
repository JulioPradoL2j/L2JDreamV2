package com.dream.game.model;

import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.itemcontainer.PcInventory;

public class CharSelectInfoPackage
{
	private String _name;
	private int _charId = 0x00030b7a;
	private final int[][] _paperdoll;
	private long _exp, _deleteTimer, _lastAccess = 0;
	private int _sp, _clanId, _karma, _pkKills, _augmentationId, _race, _classId, _baseClassId, _face, _hairStyle, _hairColor, _sex, _level, _maxHp, _maxMp, _objectId = 0;
	private boolean _isBanned = false;
	private double _currentHp, _currentMp = 0;

	public CharSelectInfoPackage(int objectId, String name)
	{
		setObjectId(objectId);
		_name = name;
		_paperdoll = PcInventory.restoreVisibleInventory(objectId);
	}

	public int getAugmentationId()
	{
		return _augmentationId;
	}

	public int getBaseClassId()
	{
		return _baseClassId;
	}

	public int getCharId()
	{
		return _charId;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public int getClassId()
	{
		return _classId;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public long getDeleteTimer()
	{
		return _deleteTimer;
	}

	public int getEnchantEffect()
	{
		return _paperdoll[Inventory.PAPERDOLL_RHAND][2];
	}

	public long getExp()
	{
		return _exp;
	}

	public int getFace()
	{
		return _face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public int getKarma()
	{
		return _karma;
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMaxHp()
	{
		return _maxHp;
	}

	public int getMaxMp()
	{
		return _maxMp;
	}

	public String getName()
	{
		return _name;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getPaperdollItemDisplayId(int slot)
	{
		return _paperdoll[slot][3];
	}

	public int getPaperdollItemId(int slot)
	{
		return _paperdoll[slot][1];
	}

	public int getPaperdollObjectId(int slot)
	{
		return _paperdoll[slot][0];
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public int getRace()
	{
		return _race;
	}

	public int getSex()
	{
		return _sex;
	}

	public int getSp()
	{
		return _sp;
	}

	public boolean isBanned()
	{
		return _isBanned;
	}

	public void setAugmentationId(int augmentationId)
	{
		_augmentationId = augmentationId;
	}

	public void setBanned(boolean val)
	{
		_isBanned = val;
	}

	public void setBaseClassId(int baseClassId)
	{
		_baseClassId = baseClassId;
	}

	public void setCharId(int charId)
	{
		_charId = charId;
	}

	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}

	public void setClassId(int classId)
	{
		_classId = classId;
	}

	public void setCurrentHp(double currentHp)
	{
		_currentHp = currentHp;
	}

	public void setCurrentMp(double currentMp)
	{
		_currentMp = currentMp;
	}

	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public void setExp(long exp)
	{
		_exp = exp;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public void setKarma(int karma)
	{
		_karma = karma;
	}

	public void setLastAccess(long lastAccess)
	{
		_lastAccess = lastAccess;
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}

	public void setMaxMp(int maxMp)
	{
		_maxMp = maxMp;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}

	public void setPkKills(int PkKills)
	{
		_pkKills = PkKills;
	}

	public void setRace(int race)
	{
		_race = race;
	}

	public void setSex(int sex)
	{
		_sex = sex;
	}

	public void setSp(int sp)
	{
		_sp = sp;
	}
}
package com.dream.game.templates.chars;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.base.Race;
import com.dream.util.StatsSet;

public class L2PcTemplate extends L2CharTemplate
{
	public static final class PcTemplateItem
	{
		private final int _itemId;
		private final int _amount;
		private final boolean _equipped;

		public PcTemplateItem(int itemId, int amount, boolean equipped)
		{
			_itemId = itemId;
			_amount = amount;
			_equipped = equipped;
		}

		public int getAmount()
		{
			return _amount;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public boolean isEquipped()
		{
			return _equipped;
		}
	}

	public ClassId classId;
	private Race race;

	public String className;
	private int spawnX;
	private int spawnY;

	private int spawnZ;
	public int classBaseLevel;
	private float lvlHpAdd;
	private float lvlHpMod;
	private float lvlCpAdd;
	private float lvlCpMod;
	private float lvlMpAdd;

	private float lvlMpMod;

	private final List<PcTemplateItem> _items = new ArrayList<>();

	public L2PcTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");

		spawnX = set.getInteger("spawnX");
		spawnY = set.getInteger("spawnY");
		spawnZ = set.getInteger("spawnZ");

		classBaseLevel = set.getInteger("classBaseLevel");
		lvlHpAdd = set.getFloat("lvlHpAdd");
		lvlHpMod = set.getFloat("lvlHpMod");
		lvlCpAdd = set.getFloat("lvlCpAdd");
		lvlCpMod = set.getFloat("lvlCpMod");
		lvlMpAdd = set.getFloat("lvlMpAdd");
		lvlMpMod = set.getFloat("lvlMpMod");
	}

	public void addItem(int itemId, int amount, boolean equipped)
	{
		_items.add(new PcTemplateItem(itemId, amount, equipped));
	}

	public int getBaseFallSafeHeight(boolean female)
	{
		if (classId.getRace() == Race.Darkelf || classId.getRace() == Race.Elf)
			return classId.isMage() ? female ? 330 : 300 : female ? 380 : 350;
		else if (classId.getRace() == Race.Dwarf)
			return female ? 200 : 180;
		else if (classId.getRace() == Race.Human)
			return classId.isMage() ? female ? 220 : 200 : female ? 270 : 250;
		else if (classId.getRace() == Race.Orc)
			return classId.isMage() ? female ? 280 : 250 : female ? 220 : 200;

		return Config.ALT_MINIMUM_FALL_HEIGHT;
	}

	public int getClassBaseLevel()
	{
		return classBaseLevel;
	}

	public ClassId getClassId()
	{
		return classId;
	}

	public String getClassName()
	{
		return className;
	}

	public List<PcTemplateItem> getItems()
	{
		return _items;
	}

	public float getLvlCpAdd()
	{
		return lvlCpAdd;
	}

	public float getLvlCpMod()
	{
		return lvlCpMod;
	}

	public float getLvlHpAdd()
	{
		return lvlHpAdd;
	}

	public float getLvlHpMod()
	{
		return lvlHpMod;
	}

	public float getLvlMpAdd()
	{
		return lvlMpAdd;
	}

	public float getLvlMpMod()
	{
		return lvlMpMod;
	}

	public Race getRace()
	{
		return race;
	}

	public int getSpawnX()
	{
		return spawnX;
	}

	public int getSpawnY()
	{
		return spawnY;
	}

	public int getSpawnZ()
	{
		return spawnZ;
	}

	public void setClassBaseLevel(int _classBaseLevel)
	{
		classBaseLevel = _classBaseLevel;
	}

	public void setClassId(ClassId _classId)
	{
		classId = _classId;
	}

	public void setClassName(String _className)
	{
		className = _className;
	}

	public void setLvlCpAdd(float _lvlCpAdd)
	{
		lvlCpAdd = _lvlCpAdd;
	}

	public void setLvlCpMod(float _lvlCpMod)
	{
		lvlCpMod = _lvlCpMod;
	}

	public void setLvlHpAdd(float _lvlHpAdd)
	{
		lvlHpAdd = _lvlHpAdd;
	}

	public void setLvlHpMod(float _lvlHpMod)
	{
		lvlHpMod = _lvlHpMod;
	}

	public void setLvlMpAdd(float _lvlMpAdd)
	{
		lvlMpAdd = _lvlMpAdd;
	}

	public void setLvlMpMod(float _lvlMpMod)
	{
		lvlMpMod = _lvlMpMod;
	}

	public void setRace(Race _race)
	{
		race = _race;
	}

	public void setSpawnX(int _spawnX)
	{
		spawnX = _spawnX;
	}

	public void setSpawnY(int _spawnY)
	{
		spawnY = _spawnY;
	}

	public void setSpawnZ(int _spawnZ)
	{
		spawnZ = _spawnZ;
	}
}
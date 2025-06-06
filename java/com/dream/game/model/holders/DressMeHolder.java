package com.dream.game.model.holders;

import com.dream.data.xml.StatSet;

public class DressMeHolder
{
	private final int _skillId;
	private final String _name;
	private final boolean _isVip;
	private final DressMeVisualType _type;
	
	private int _chestId, _legsId, _glovesId, _feetId, _helmetId;
	private int _rHandId, _lHandId, _lrHandId;
	private String _weaponTypeVisual;
	
	private DressMeEffectHolder _effect;
	
	public DressMeHolder(StatSet set)
	{
		_skillId = set.getInteger("skillId");
		_name = set.getString("name", "");
		_type = DressMeVisualType.valueOf(set.getString("type", "ARMOR"));
		_isVip = set.getBool("isVip", false);
	}
	
	public void setVisualSet(StatSet set)
	{
		_chestId = set.getInteger("chest", 0);
		_legsId = set.getInteger("legs", 0);
		_glovesId = set.getInteger("gloves", 0);
		_feetId = set.getInteger("feet", 0);
		_helmetId = set.getInteger("helmet", 0);
	}
	
	public void setWeaponSet(StatSet set)
	{
		_rHandId = set.getInteger("rhand", 0);
		_lHandId = set.getInteger("lhand", 0);
		_lrHandId = set.getInteger("lrhand", 0);
		_weaponTypeVisual = set.getString("type", "");
	}
	
	public void setEffect(DressMeEffectHolder effect)
	{
		_effect = effect;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isVip()
	{
		return _isVip;
	}
	
	public DressMeVisualType getType()
	{
		return _type;
	}
	
	public int getChestId()
	{
		return _chestId;
	}
	
	public int getLegsId()
	{
		return _legsId;
	}
	
	public int getGlovesId()
	{
		return _glovesId;
	}
	
	public int getFeetId()
	{
		return _feetId;
	}
	
	public int getHelmetId()
	{
		return _helmetId;
	}
	
	public String getWeaponTypeVisual()
	{
		return _weaponTypeVisual;
	}
	
	public int getRightHandId()
	{
		return _rHandId;
	}
	
	public int getLeftHandId()
	{
		return _lHandId;
	}
	
	public int getTwoHandId()
	{
		return _lrHandId;
	}
	
	public DressMeEffectHolder getEffect()
	{
		return _effect;
	}
}

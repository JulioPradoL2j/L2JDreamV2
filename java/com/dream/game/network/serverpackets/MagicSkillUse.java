package com.dream.game.network.serverpackets;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;

public class MagicSkillUse extends L2GameServerPacket
{
	private boolean _isPositive = false;
	private final int _targetId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillTime;
	private final int _reuseDelay;
	private final int _charObjId, _x, _y, _z;

	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int skillTime, int reuseDelay, boolean isPositive)
	{
		this(cha, cha, skillId, skillLevel, skillTime, reuseDelay, isPositive);
	}

	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int skillTime, int reuseDelay, boolean isPositive)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_skillTime = skillTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_isPositive = isPositive;
	}

	public MagicSkillUse(L2Character cha, L2Character target, L2Skill skill, int skillTime, int reuseDelay, boolean isPositive)
	{
		this(cha, target, skill.getDisplayId(), skill.getLevel(), skillTime, reuseDelay, skill.isPositive());
	}

	public MagicSkillUse(L2Character cha, L2Skill skill, int skillTime, int reuseDelay, boolean isPositive)
	{
		this(cha, cha, skill.getDisplayId(), skill.getLevel(), skillTime, reuseDelay, skill.isPositive());
	}

	public boolean isPositive()
	{
		return _isPositive;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_skillTime);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
	}

}
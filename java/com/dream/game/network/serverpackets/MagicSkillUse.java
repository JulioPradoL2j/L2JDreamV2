package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.L2Character;



public class MagicSkillUse extends L2GameServerPacket
{
	private final int _objectId;
	private final int _targetId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _tX;
	private final int _tY;
	private final int _tZ;
	private final boolean _success;
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay, boolean crit)
	{
		_objectId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tX = target.getX();
		_tY = target.getY();
		_tZ = target.getZ();
		_success = crit;
	}
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay, false);
	}
	
	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		this(cha, cha, skillId, skillLevel, hitTime, reuseDelay, false);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_objectId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		if (_success)
		{
			writeD(0x01);
			writeH(0x00);
		}
		else
			writeD(0x00);
		writeD(_tX);
		writeD(_tY);
		writeD(_tZ);
	}
}
package com.dream.game.network.serverpackets;

import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;

public final class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _skillId;
	private final int _skillLevel;
	private final L2Object[] _targets;

	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, boolean isPositive)
	{
		this(cha, skillId, skillLevel, isPositive, cha.getTarget());
	}

	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, boolean isPositive, L2Object... targets)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x76);
		writeD(_charObjId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.length);

		if (_targets.length == 0)
		{
			writeD(0);
		}
		else
		{
			for (L2Object target : _targets)
			{
				writeD(target == null ? 0 : target.getObjectId());
			}
		}
	}

}
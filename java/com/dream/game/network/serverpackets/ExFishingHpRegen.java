package com.dream.game.network.serverpackets;

import com.dream.game.model.actor.L2Character;

public class ExFishingHpRegen extends L2GameServerPacket
{
	private final L2Character _activeChar;
	private final int _time, _fishHp, _hpMode, _anim, _goodUse, _penalty, _hpBarColor;

	public ExFishingHpRegen(L2Character character, int time, int fishHp, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
	{
		_activeChar = character;
		_time = time;
		_fishHp = fishHp;
		_hpMode = HPmode;
		_goodUse = GoodUse;
		_anim = anim;
		_penalty = penalty;
		_hpBarColor = hpBarColor;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x16);

		writeD(_activeChar.getObjectId());
		writeD(_time);
		writeD(_fishHp);
		writeC(_hpMode);
		writeC(_goodUse);
		writeC(_anim);
		writeD(_penalty);
		writeC(_hpBarColor);
	}

}
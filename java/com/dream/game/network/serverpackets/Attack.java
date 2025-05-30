package com.dream.game.network.serverpackets;

import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;

public class Attack extends L2GameServerPacket
{
	public class Hit
	{
		protected final int _targetId;
		protected final int _damage;
		protected int _flags;

		Hit(L2Object target, int damage, boolean miss, boolean crit, byte shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;
			if (miss)
			{
				_flags = HITFLAG_MISS;
				return;
			}
			if (soulshot)
			{
				_flags = HITFLAG_USESS | _ssGrade;
			}
			if (crit)
			{
				_flags |= HITFLAG_CRIT;
			}
			if (shld > 0 && !(target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode()))
			{
				_flags |= HITFLAG_SHLD;
			}
		}
	}

	public static final int HITFLAG_USESS = 0x10;
	public static final int HITFLAG_CRIT = 0x20;
	public static final int HITFLAG_SHLD = 0x40;

	public static final int HITFLAG_MISS = 0x80;

	private final int _attackerObjId;
	private final int _targetObjId;
	public final boolean soulshot;
	public final int _ssGrade;
	private final int _x;
	private final int _y;
	private final int _z;
	private Hit[] _hits;

	public Attack(L2Character attacker, L2Object target, boolean useShots, int ssGrade)
	{
		_attackerObjId = attacker.getObjectId();
		_targetObjId = target.getObjectId();
		soulshot = useShots;
		_ssGrade = ssGrade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
	}

	public Hit createHit(L2Object target, int damage, boolean miss, boolean crit, byte shld)
	{
		return new Hit(target, damage, miss, crit, shld);
	}

	public boolean hasHits()
	{
		return _hits != null;
	}

	public void hit(Hit... hits)
	{
		if (_hits == null)
		{
			_hits = hits;
			return;
		}

		Hit[] tmp = new Hit[hits.length + _hits.length];
		System.arraycopy(_hits, 0, tmp, 0, _hits.length);
		System.arraycopy(hits, 0, tmp, _hits.length, hits.length);
		_hits = tmp;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x05);

		writeD(_attackerObjId);
		writeD(_targetObjId);
		writeD(_hits[0]._damage);
		writeC(_hits[0]._flags);
		writeD(_x);
		writeD(_y);
		writeD(_z);

		writeH(_hits.length - 1);

		if (_hits.length > 1)
		{
			for (int i = 1; i < _hits.length; i++)
			{
				writeD(_hits[i]._targetId);
				writeD(_hits[i]._damage);
				writeC(_hits[i]._flags);
			}
		}

	}

}
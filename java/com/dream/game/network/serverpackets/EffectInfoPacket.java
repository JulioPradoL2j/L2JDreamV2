package com.dream.game.network.serverpackets;

import java.util.TreeSet;

import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.L2Playable;

public abstract class EffectInfoPacket extends L2GameServerPacket
{
	private final EffectInfoPacketList _list;

	protected EffectInfoPacket(EffectInfoPacketList list)
	{
		_list = list;
	}

	protected final L2Playable getPlayable()
	{
		return _list._playable;
	}

	protected final int size()
	{
		return _list._effects.size();
	}

	protected final void writeEffectInfos()
	{
		for (Effect e : _list._effects)
		{
			writeD(e.id);
			writeH(e.level);
			writeD(e.duration);
		}
	}

	private static class Effect implements Comparable<Effect>
	{
		public final int id;
		public final int level;
		public final int duration;
		private final int num;

		public Effect(int id, int level, int duration, int num)
		{
			this.id = id;
			this.level = level;
			this.duration = duration;
			this.num = num;
		}

		@Override
		public int compareTo(Effect effect)
		{
			return effect.num > num ? -1 : 1;
		}

		@Override
		public boolean equals(Object object)
		{
			return object instanceof Effect && ((Effect) object).id == id;
		}
	}

	public static final class EffectInfoPacketList
	{
		public final TreeSet<Effect> _effects = new TreeSet<>();

		public final L2Playable _playable;

		public EffectInfoPacketList(L2Playable playable)
		{
			_playable = playable;

			for (L2Effect effect : _playable.getAllEffects())
				effect.addPacket(EffectInfoPacketList.this);
		}

		public final void addEffect(int id, int level, int duration)
		{
			_effects.add(new Effect(id, level, duration, _effects.size()));
		}

		public final boolean contains(int id)
		{
			for (Effect effect : _effects)
				if (effect.id == id)
					return true;
			return false;
		}
	}
}
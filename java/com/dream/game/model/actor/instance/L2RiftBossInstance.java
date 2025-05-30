package com.dream.game.model.actor.instance;

import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2RiftBossAI;
import com.dream.game.model.entity.DimensionalRift;
import com.dream.game.templates.chars.L2NpcTemplate;

public final class L2RiftBossInstance extends L2RaidBossInstance
{
	DimensionalRift _dimensionalRift;

	public L2RiftBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			_ai = new L2RiftBossAI(new AIAccessor());
		}
		return _ai;
	}

	public DimensionalRift getDimensionalRift()
	{
		return _dimensionalRift;
	}

	public void setDimensionalRift(DimensionalRift DR)
	{
		_dimensionalRift = DR;
	}
}
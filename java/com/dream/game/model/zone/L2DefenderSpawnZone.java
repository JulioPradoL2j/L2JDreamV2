package com.dream.game.model.zone;

import com.dream.game.manager.CastleManager;
import com.dream.game.model.actor.L2Character;

public class L2DefenderSpawnZone extends EntityZone
{
	@Override
	protected void onEnter(L2Character character)
	{
	}

	@Override
	protected void onExit(L2Character character)
	{
	}

	@Override
	protected void register()
	{
		_entity = CastleManager.getInstance().getCastleById(_castleId);
		if (_entity != null)
		{
			_entity.registerDefenderSpawn(this);
		}
		else
		{
			_log.warn("Invalid castleId: " + _castleId);
		}
	}
}
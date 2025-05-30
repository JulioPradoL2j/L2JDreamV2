package com.dream.game.model.zone;

import com.dream.game.manager.CastleManager;
import com.dream.game.model.actor.L2Character;

public class L2HeadQuartersZone extends EntityZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		super.onExit(character);
	}

	@Override
	protected void register()
	{
		if (_castleId > 0)
		{
			_entity = CastleManager.getInstance().getCastleById(_castleId);
			_entity.registerHeadquartersZone(this);
		}
	}
}
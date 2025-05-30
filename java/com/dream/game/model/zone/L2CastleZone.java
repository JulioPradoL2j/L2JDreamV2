package com.dream.game.model.zone;

import com.dream.game.manager.CastleManager;
import com.dream.game.model.actor.L2Character;

public class L2CastleZone extends EntityZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(this, FLAG_CASTLE, true);
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(this, FLAG_CASTLE, false);
		super.onExit(character);
	}

	@Override
	protected void register()
	{
		_entity = CastleManager.getInstance().getCastleById(_castleId);
		if (_entity != null)
		{
			_entity.registerZone(this);
		}
		else
		{
			_log.warn("Invalid castleId: " + _castleId);
		}
	}
}
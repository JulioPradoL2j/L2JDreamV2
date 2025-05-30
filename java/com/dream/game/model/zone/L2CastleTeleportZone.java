package com.dream.game.model.zone;

import com.dream.game.manager.CastleManager;
import com.dream.game.model.actor.L2Character;

public class L2CastleTeleportZone extends EntityZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(this, FLAG_NOSUMMON, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(this, FLAG_NOSUMMON, false);
	}

	@Override
	protected void register()
	{
		_entity = CastleManager.getInstance().getCastleById(_castleId);
		if (_entity != null)
		{
			_entity.registerTeleportZone(this);
		}
		else
		{
			_log.warn("Invalid castleId: " + _castleId);
		}
	}
}
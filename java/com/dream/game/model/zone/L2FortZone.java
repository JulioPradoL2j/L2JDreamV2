package com.dream.game.model.zone;

import com.dream.game.manager.FortManager;
import com.dream.game.model.actor.L2Character;

public class L2FortZone extends EntityZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		super.onEnter(character);
		character.setInsideZone(this, FLAG_FORT, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		super.onExit(character);
		character.setInsideZone(this, FLAG_FORT, false);
	}

	@Override
	protected void register()
	{
		_entity = FortManager.getInstance().getFortById(_fortId);
		if (_entity != null)
		{
			_entity.registerZone(this);
			_entity.registerHeadquartersZone(this);
		}
		else
		{
			_log.warn("Invalid fortId: " + _fortId);
		}
	}
}
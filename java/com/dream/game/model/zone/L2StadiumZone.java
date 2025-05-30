package com.dream.game.model.zone;

import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;

public class L2StadiumZone extends L2DefaultZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance && ((L2PcInstance) character).getOlympiadGameId() == -1 && !((L2PcInstance) character).isGM())
		{
			character.teleToLocation(TeleportWhereType.Town);
			return;
		}
		character.setInsideZone(this, FLAG_STADIUM, true);
		character.setInsideZone(this, FLAG_PVP, true);
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(this, FLAG_STADIUM, false);
		character.setInsideZone(this, FLAG_PVP, false);
		super.onExit(character);
	}
}
package com.dream.game.model.zone;

import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;

public class L2SepultureZone extends L2Zone
{

	@Override
	public void onDieInside(L2Character character)
	{

	}

	@Override
	protected void onEnter(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = cha.getActingPlayer();
			if (player.getParty() == null && !player.isGM())
			{
				player.teleToLocation(TeleportWhereType.Town);
			}
			player._inSepulture = true;
		}

	}

	@Override
	protected void onExit(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			((L2PcInstance) cha)._inSepulture = false;
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{

	}

}

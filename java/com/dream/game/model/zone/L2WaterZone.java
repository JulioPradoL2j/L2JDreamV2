package com.dream.game.model.zone;

import com.dream.Config;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;

public class L2WaterZone extends L2DefaultZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if (!Config.ALLOW_AIO_LEAVE_TOWN)
		{
			if (character instanceof L2PcInstance)
			{
				L2PcInstance activeChar = ((L2PcInstance) character);
				if (activeChar.isAio() && !character.isInsideZone(L2Zone.FLAG_ALLOWAIO))
				{
					activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
			}
		}
		if (character instanceof L2PcInstance && ((L2PcInstance) character).isInBoat())
			return;

		boolean firstEntered = !character.isInWater();

		character.setInsideZone(this, FLAG_WATER, true);

		if (firstEntered)
		{
			if (character.isPlayer())
			{
				character.getPlayer().startWaterTask();
				character.getPlayer().broadcastUserInfo();
			}
			else if (character.isNpc())
				character.broadcastFullInfo();
		}

		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(this, FLAG_WATER, false);

		if (!character.isInWater())
		{
			if (character.isPlayer())
			{
				character.getPlayer().stopWaterTask();
				character.getPlayer().broadcastUserInfo();
			}
			else if (character.isNpc())
				character.broadcastFullInfo();
		}

		super.onExit(character);
	}
}
package com.dream.game.model.zone;

import com.dream.Config;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.network.serverpackets.AgitDecoInfo;

public class L2TownZone extends L2DefaultZone
{
	@Override
	protected void onEnter(L2Character character)
	{
		boolean peace = true;

		if (character instanceof L2PcInstance)
			if (((L2PcInstance) character).getSiegeState() != 0 && Config.ZONE_TOWN == 1)
			{
				character.setInsideZone(this, FLAG_PVP, true);
				peace = false;
			}

		if (Config.ZONE_TOWN == 2)
		{
			peace = false;
			character.setInsideZone(this, FLAG_PVP, true);
		}
		if (peace)
		{
			character.setInsideZone(this, FLAG_PEACE, true);
		}

		if (character instanceof L2PcInstance)
		{
			ClanHall[] townHalls = ClanHallManager.getInstance().getTownClanHalls(getTownId());
			if (townHalls != null)
			{
				for (ClanHall ch : townHalls)
					if (ch.getOwnerId() > 0)
					{
						character.getActingPlayer().sendPacket(new AgitDecoInfo(ch));
					}
			}
		}
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character.isInsideZone(FLAG_PVP))
		{
			character.setInsideZone(this, FLAG_PVP, false);
		}

		if (character.isInsideZone(FLAG_PEACE))
		{
			character.setInsideZone(this, FLAG_PEACE, false);
		}

		if (!Config.ALLOW_AIO_LEAVE_TOWN)
		{
			if (character instanceof L2PcInstance)
			{
				L2PcInstance activeChar = ((L2PcInstance) character);
				if (activeChar.isAio())
				{
					activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
			}
		}

		super.onExit(character);
	}

	@Override
	protected void register()
	{
		TownManager.getInstance().registerTown(this);
	}
}
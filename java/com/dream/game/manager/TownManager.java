/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.entity.Town;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.model.mapregion.L2MapRegionRestart;
import com.dream.game.model.zone.L2Zone;

public class TownManager
{
	protected static Logger _log = Logger.getLogger(TownManager.class.getName());

	private static TownManager _instance;

	public static final TownManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new TownManager();
		}
		return _instance;
	}

	private Map<Integer, Town> _towns;

	public final boolean checkIfInZone(int x, int y, int z)
	{
		return getTown(x, y, z) != null;
	}

	public final boolean checkIfInZone(L2Object obj)
	{
		return getTown(obj) != null;
	}

	public final Town getClosestTown(int x, int y, int z)
	{
		L2MapRegion region = MapRegionTable.getInstance().getRegion(x, y, z);

		L2MapRegionRestart restart = null;
		if (region != null)
		{
			restart = MapRegionTable.getInstance().getRestartLocation(region.getRestartId());
		}
		else
		{
			_log.info("No L2MapRegion defined at " + x + " " + y + " " + z);
		}
		return getTownByMaprestart(restart);
	}

	public final Town getClosestTown(L2Object activeObject)
	{
		return getClosestTown(activeObject.getPosition().getX(), activeObject.getPosition().getY(), activeObject.getPosition().getZ());
	}

	public String getClosestTownName(L2Character activeChar)
	{
		return getTownName(getClosestTown(activeChar).getTownId());
	}

	public int getRedirectTownNumber(int townId)
	{
		int redirectTownId = 8;

		switch (townId)
		{
			case 5:
				redirectTownId = 6;
				break; // Gludio => Gludin
			case 7:
				redirectTownId = 5;
				break; // Dion => Gludio
			case 8:
				redirectTownId = 12;
				break; // Giran => Giran Harbor
			case 9:
				redirectTownId = 11;
				break; // Oren => HV
			case 10:
				redirectTownId = 9;
				break; // Aden => Oren
			case 15:
				redirectTownId = 14;
				break; // Goddard => Rune
			case 14:
				redirectTownId = 15;
				break; // Rune => Goddard
			case 13:
				redirectTownId = 12;
				break; // Heine => Giran Harbor
			case 16:
				redirectTownId = 14;
				break; // Schuttgart => Rune
			case 17:
				redirectTownId = 9;
				break; // Ivory Tower => Oren
			case 18:
				redirectTownId = 14;
				break; // Primeval Isle Wharf => Rune
		}

		return redirectTownId;
	}

	public final Town getTown(int townId)
	{
		return getTowns().get(townId);
	}

	public final Town getTown(int x, int y)
	{
		for (Town town : getTowns().values())
			if (town != null && town.checkIfInZone(x, y))
				return town;
		return null;
	}

	public final Town getTown(int x, int y, int z)
	{
		for (Town town : getTowns().values())
			if (town != null && town.checkIfInZone(x, y, z))
				return town;
		return null;
	}

	public final Town getTown(L2Object activeObject)
	{
		return getTown(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Town getTownByMaprestart(L2MapRegionRestart restart)
	{
		if (restart == null)
			return getTown(Config.ALT_DEFAULT_RESTARTTOWN);

		switch (restart.getBbsId())
		{
			case 1: // Talking Island
				return getTown(0);
			case 2: // Gludin
				return getTown(6);
			case 3: // Darkelfen Village
				return getTown(2);
			case 4: // Elfen Village
				return getTown(1);
			case 5: // Dion
				return getTown(7);
			case 6: // Giran
				return getTown(8);
			case 7: // Gludio
				return getTown(5);
			case 8: // Orc Village
				return getTown(3);
			case 9: // Dwarfen Village
				return getTown(4);
			case 10: // Oren Villag
				return getTown(9);
			case 11: // Hunters Village
				return getTown(11);
			case 12: // Heine
				return getTown(13);
			case 13: // Aden
				return getTown(10);
			case 14: // Rune
				return getTown(14);
			case 15: // Goddard
				return getTown(15);
			case 16: // Schuttgart
				return getTown(16);
			case 17: // Dimensional Gap
				return getTown(17);
			case 18: // Primeval Isle
				return getTown(18);
			default:
				return getTown(Config.ALT_DEFAULT_RESTARTTOWN);
		}
	}

	public String getTownName(int townId)
	{
		String nearestTown;

		switch (townId)
		{
			case 0:
				nearestTown = "Talking Island Village";
				break;
			case 1:
				nearestTown = "Elven Village";
				break;
			case 2:
				nearestTown = "Dark Elven Village";
				break;
			case 3:
				nearestTown = "Orc Village";
				break;
			case 4:
				nearestTown = "Dwarven Village";
				break;
			case 5:
				nearestTown = "Town of Gludio";
				break;
			case 6:
				nearestTown = "Gludin Village";
				break;
			case 7:
				nearestTown = "Town of Dion";
				break;
			case 8:
				nearestTown = "Town of Giran";
				break;
			case 9:
				nearestTown = "Town of Oren";
				break;
			case 10:
				nearestTown = "Town of Aden";
				break;
			case 11:
				nearestTown = "Hunters Village";
				break;
			case 12:
				nearestTown = "Giran Harbor";
				break;
			case 13:
				nearestTown = "Heine";
				break;
			case 14:
				nearestTown = "Rune Township";
				break;
			case 15:
				nearestTown = "Town of Goddard";
				break;
			case 16:
				nearestTown = "Town of Schuttgart";
				break;
			case 17:
				nearestTown = "Dimensional Gap";
				break;
			case 18:
				nearestTown = "Primeval Isle Wharf";
				break;
			case 19:
				nearestTown = "Floran Village";
				break;
			default:
				nearestTown = "";
				break;
		}
		return nearestTown;
	}

	public final Map<Integer, Town> getTowns()
	{
		if (_towns == null)
		{
			_towns = new HashMap<>();
		}

		return _towns;
	}

	public void registerTown(L2Zone zone)
	{
		Town t = new Town(zone);
		t.registerZone(zone);
		getTowns().put(zone.getTownId(), t);
	}

	public final boolean townHasCastleInSiege(int townId)
	{
		Town town = getTown(townId);
		return town != null && town.hasCastleInSiege();
	}

	public final boolean townHasCastleInSiege(int x, int y, int z)
	{
		Town town = getClosestTown(x, y, z);
		return town != null && town.hasCastleInSiege();
	}
}
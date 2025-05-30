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
package com.dream.game.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.dream.Config;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.TownManager;
import com.dream.game.manager.clanhallsiege.BanditStrongholdSiege;
import com.dream.game.manager.clanhallsiege.WildBeastFarmSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.model.entity.Town;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.mapregion.L2MapArea;
import com.dream.game.model.mapregion.L2MapRegion;
import com.dream.game.model.mapregion.L2MapRegionRestart;
import com.dream.game.model.world.Location;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.util.Util;
import com.dream.tools.geometry.Point3D;

public class MapRegionTable
{
	public enum TeleportWhereType
	{
		Castle,
		ClanHall,
		Fortress,
		SiegeFlag,
		Town
	}

	private static Logger _log = Logger.getLogger(MapRegionTable.class.getName());

	private static MapRegionTable _instance = null;

	public final static MapRegionTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new MapRegionTable();
		}

		return _instance;
	}

	private Map<Integer, L2MapRegion> _mapRegions = new HashMap<>();
	private Map<Integer, L2MapRegionRestart> _mapRegionRestart = new HashMap<>();

	private Map<Integer, L2MapArea> _mapRestartArea = new HashMap<>();
	private Map<Integer, L2MapArea> _mapAreas = new HashMap<>();
	private Map<Integer, L2MapRegion> _mapRegionsReload = null;
	private Map<Integer, L2MapRegionRestart> _mapRegionRestartReload = null;

	private Map<Integer, L2MapArea> _mapRestartAreaReload = null;

	private Map<Integer, L2MapArea> _mapAreasReload = null;

	public MapRegionTable()
	{
		load();
	}

	public int convertLocNameToL2Region(int locName)
	{
		switch (locName)
		{
			case 910:
				return 1;
			case 911:
			case 912:
			case 2190:
			case 2710:
			case 2711:
			case 2712:
			case 2716:
				return 2;
			case 913:
				return 7;
			case 914:
				return 4;
			case 915:
				return 3;
			case 916:
			case 917:
				return 5;
			case 918:
			case 919:
				return 6;
			case 920:
			case 921:
			case 1714:
				return 9;
			case 922:
				return 10;
			case 923:
				return 11;
			case 924:
			case 925:
			case 2189:
				return 13;
			case 926:
				return 12;
			case 1537:
			case 1924:
			case 2259:
				return 14;
			case 1538:
				return 15;
			default:
				return 0;
		}
	}

	public int getAreaCastle(L2Character activeChar)
	{
		Town town = TownManager.getInstance().getClosestTown(activeChar);

		if (town == null)
			return 5;

		return town.getCastleId();
	}

	public Point3D getChaosRestartPoint(int restartId)
	{
		L2MapRegionRestart restart = _mapRegionRestart.get(restartId);

		if (restart != null)
			return restart.getRandomChaosRestartPoint();

		restart = _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);
		return restart.getRandomChaosRestartPoint();
	}

	public Point3D getChaosRestartPoint(L2PcInstance activeChar)
	{
		L2MapRegion region = getRegion(activeChar);

		if (region != null)
		{
			int restartId = region.getRestartId(activeChar.getRace());

			L2MapRegionRestart restart = _mapRegionRestart.get(restartId);

			if (restart != null)
				return restart.getRandomChaosRestartPoint(activeChar.getRace());
		}

		L2MapRegionRestart restart = _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);
		return restart.getRandomChaosRestartPoint(activeChar.getRace());
	}

	public int getL2Region(L2PcInstance player)
	{
		L2MapRegion region = getRegion(player);
		int locName = -1;
		if (region != null)
		{
			int restartId = region.getRestartId();
			L2MapRegionRestart restart = getRestartLocation(restartId);
			locName = restart.getLocName();
		}
		return convertLocNameToL2Region(locName);
	}

	public Location getLocationFromPoint3D(Point3D point)
	{
		return new Location(point.getX(), point.getY(), point.getZ());
	}

	public int getNextAccessibleRestartId(L2MapRegionRestart restart, L2PcInstance activeChar)
	{
		Town town = TownManager.getInstance().getTownByMaprestart(restart);
		if (town != null && town.hasCastleInSiege() && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
		{
			int newTownId = TownManager.getInstance().getRedirectTownNumber(town.getTownId());
			town = TownManager.getInstance().getTown(newTownId);
			L2MapRegion region = town.getMapRegion();
			if (region != null)
				return region.getRestartId(activeChar.getRace());
		}
		return restart.getId();
	}

	public L2MapRegion getRegion(int x, int y)
	{
		return getRegion(x, y, -1);
	}

	public L2MapRegion getRegion(int x, int y, int z)
	{
		L2MapRegion areaRegion = null;

		for (L2MapRegion region : _mapRegions.values())
			if (region.checkIfInRegion(x, y, z))
			{
				if (region.isSpecialRegion())
					return region;

				areaRegion = region;
			}

		return areaRegion;
	}

	public L2MapRegion getRegion(L2Character activeChar)
	{
		return getRegion(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}

	public L2MapRegion getRegionById(int regionId)
	{
		return _mapRegions.get(regionId);
	}

	public Map<Integer, L2MapRegion> getRegions()
	{
		return _mapRegions;
	}

	public L2MapRegionRestart getRestartLocation(int restartId)
	{
		return _mapRegionRestart.get(restartId);
	}

	public L2MapRegionRestart getRestartLocation(L2PcInstance activeChar)
	{
		L2MapRegion region = getRegion(activeChar);

		if (region == null)
			return _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);

		int restartId = region.getRestartId(activeChar.getRace());

		return _mapRegionRestart.get(restartId);
	}

	public Point3D getRestartPoint(int restartId)
	{
		L2MapRegionRestart restart = _mapRegionRestart.get(restartId);

		if (restart != null)
			return restart.getRandomRestartPoint();

		restart = _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);
		return restart.getRandomRestartPoint();
	}

	public Point3D getRestartPoint(L2MapRegion region, L2PcInstance activeChar)
	{
		if (region != null)
		{
			int restartId = region.getRestartId(activeChar.getRace());

			L2MapRegionRestart restart = _mapRegionRestart.get(restartId);

			if (restart != null)
				return restart.getRandomRestartPoint(activeChar.getRace());
		}

		L2MapRegionRestart restart = _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);
		return restart.getRandomRestartPoint(activeChar.getRace());
	}

	public Point3D getRestartPoint(L2PcInstance activeChar)
	{
		return getRestartPoint(getRegion(activeChar), activeChar);
	}

	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeChar;
			L2Clan clan = player.getClan();
			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;

			if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), true))
			{
				if (player.isInParty() && player.getParty().isInDimensionalRift())
				{
					player.getParty().getDimensionalRift().usedTeleport(player);
				}

				return DimensionalRiftManager.getInstance().getWaitingRoomTeleport();
			}

			L2Zone arena = ZoneTable.getInstance().isInsideZone(L2Zone.ZoneType.Arena, player.getX(), player.getY());
			if (arena != null && arena.isInsideZone(player))
			{
				Location loc = arena.getRestartPoint(L2Zone.RestartType.OWNER);
				if (loc == null)
				{
					loc = arena.getRandomLocation();
				}
				return loc;
			}

			if (teleportWhere == TeleportWhereType.Town)
			{
				L2MapRegionRestart restart = getRestartLocation(player);
				int restartId = getNextAccessibleRestartId(restart, player);

				Location loc = null;
				if (player.getKarma() > 1 || player.isCursedWeaponEquipped())
				{
					loc = getLocationFromPoint3D(getChaosRestartPoint(restartId));
				}
				if (loc == null)
				{
					loc = getLocationFromPoint3D(getRestartPoint(restartId));
				}
				return loc;
			}

			if (clan != null)
			{
				if (teleportWhere == TeleportWhereType.ClanHall)
				{
					clanhall = ClanHallManager.getInstance().getClanHallByOwner(clan);
					if (clanhall != null)
					{
						L2Zone zone = clanhall.getZone();

						if (zone != null)
						{
							Location loc = zone.getRestartPoint(L2Zone.RestartType.OWNER);
							if (loc == null)
							{
								loc = zone.getRandomLocation();
							}
							return loc;
						}
					}
				}

				if (teleportWhere == TeleportWhereType.Castle)
				{
					castle = CastleManager.getInstance().getCastleByOwner(clan);
				}
				else if (teleportWhere == TeleportWhereType.Fortress)
				{
					fort = FortManager.getInstance().getFortByOwner(clan);
				}

				if (castle != null && teleportWhere == TeleportWhereType.Castle)
				{
					L2Zone zone = castle.getZone();

					if (zone != null)
					{
						if (castle.getSiege() != null && castle.getSiege().getIsInProgress() && (player.getKarma() > 1 || player.isCursedWeaponEquipped()))
							return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);

						return zone.getRestartPoint(L2Zone.RestartType.OWNER);
					}
				}
				else if (fort != null && teleportWhere == TeleportWhereType.Fortress)
				{
					L2Zone zone = fort.getZone();

					if (zone != null)
					{
						if (fort.getSiege() != null && fort.getSiege().getIsInProgress() && (player.getKarma() > 1 || player.isCursedWeaponEquipped()))
							return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);

						return zone.getRestartPoint(L2Zone.RestartType.OWNER);
					}
				}
				else if (teleportWhere == TeleportWhereType.SiegeFlag)
				{
					Siege siege = SiegeManager.getSiege(clan);
					FortSiege fsiege = FortSiegeManager.getSiege(clan);

					if (siege != null && fsiege == null && siege.checkIsAttacker(clan) && siege.checkIfInZone(player))
					{
						if (player.getKarma() > 1 || player.isCursedWeaponEquipped())
						{
							L2Zone zone = siege.getCastle().getZone();
							if (zone != null)
								return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);
						}
						L2Npc flag = siege.getClosestFlag(player);
						if (flag != null)
							return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
					else if (siege == null && fsiege != null && fsiege.checkIsAttacker(clan) && fsiege.checkIfInZone(player))
					{
						if (player.getKarma() > 1 || player.isCursedWeaponEquipped())
						{
							L2Zone zone = fsiege.getFort().getZone();
							if (zone != null)
								return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);
						}
						L2Npc flag = fsiege.getClosestFlag(player);
						if (flag != null)
							return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
					else if (BanditStrongholdSiege.getInstance().isPlayerRegister(((L2PcInstance) activeChar).getClan(), activeChar.getName()))
					{
						L2Npc flag = BanditStrongholdSiege.getInstance().getSiegeFlag(((L2PcInstance) activeChar).getClan());
						if (flag != null)
							return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
					else if (WildBeastFarmSiege.getInstance().isPlayerRegister(((L2PcInstance) activeChar).getClan(), activeChar.getName()))
					{
						L2Npc flag = WildBeastFarmSiege.getInstance().getSiegeFlag(((L2PcInstance) activeChar).getClan());
						if (flag != null)
							return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
				}
			}
		}

		return getLocationFromPoint3D(getRestartPoint(Config.ALT_DEFAULT_RESTARTTOWN));
	}

	private void load()
	{
		_mapRegionsReload = new HashMap<>();
		_mapRegionRestartReload = new HashMap<>();
		_mapRestartAreaReload = new HashMap<>();
		_mapAreasReload = new HashMap<>();

		for (File xml : Util.getDatapackFiles("xml/world/mapregion", ".xml"))
		{
			Document doc = null;

			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

				factory.setValidating(false);
				factory.setIgnoringComments(true);

				doc = factory.newDocumentBuilder().parse(xml);
			}
			catch (Exception e)
			{
				_log.warn("MapRegionManager: Error while loading XML definition: " + xml.getName() + e, e);
				return;
			}

			try
			{
				parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.warn("MapRegionManager: Error in XML definition: " + xml.getName() + e, e);
				return;
			}
		}

		_mapRegions = _mapRegionsReload;
		_mapRegionRestart = _mapRegionRestartReload;
		_mapRestartArea = _mapRestartAreaReload;
		_mapAreas = _mapAreasReload;

		_mapRegionsReload = null;
		_mapRegionRestartReload = null;
		_mapRestartAreaReload = null;
		_mapAreasReload = null;

		int redirectCount = 0;

		for (L2MapRegionRestart restart : _mapRegionRestart.values())
			if (restart.getBannedRace() != null)
			{
				redirectCount++;
			}

		_log.info("Map Region: Loaded " + _mapRegionRestart.size() + " Restart point(s).");
		_log.info("Map Region: Loaded " + _mapRestartArea.size() + " Restart areas with " + _mapAreas.size() + " area region(s).");
		_log.info("Map Region: Loaded " + _mapRegions.size() + " Zone region(s).");
		_log.info("Map Region: Loaded " + redirectCount + " Race depending redirects.");
	}

	private void parseDocument(Document doc) throws Exception
	{
		Map<Integer, L2MapRegion> regions = new HashMap<>();
		Map<Integer, L2MapRegionRestart> restarts = new HashMap<>();
		Map<Integer, L2MapArea> restartAreas = new HashMap<>();
		Map<Integer, L2MapArea> areas = new HashMap<>();

		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if ("mapregion".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if ("regions".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node f = d.getFirstChild(); f != null; f = f.getNextSibling())
							if ("region".equalsIgnoreCase(f.getNodeName()))
							{
								L2MapRegion region = new L2MapRegion(f);

								if (!regions.containsKey(region.getId()))
								{
									regions.put(region.getId(), region);
								}
								else
									throw new Exception("Duplicate zoneRegionId: " + region.getId() + ".");
							}
					}
					else if ("restartpoints".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node f = d.getFirstChild(); f != null; f = f.getNextSibling())
							if ("restartpoint".equalsIgnoreCase(f.getNodeName()))
							{
								L2MapRegionRestart restart = new L2MapRegionRestart(f);

								if (!restarts.containsKey(restart.getId()))
								{
									restarts.put(restart.getId(), restart);
								}
								else
									throw new Exception("Duplicate restartpointId: " + restart.getId() + ".");
							}
					}
					else if ("areas".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node f = d.getFirstChild(); f != null; f = f.getNextSibling())
							if ("restartarea".equalsIgnoreCase(f.getNodeName()))
							{
								int id = -1;

								Node e = f.getAttributes().getNamedItem("id");
								if (e != null)
								{
									id = Integer.parseInt(e.getTextContent());

									for (Node r = f.getFirstChild(); r != null; r = r.getNextSibling())
										if ("map".equalsIgnoreCase(r.getNodeName()))
										{
											int X = 0;
											int Y = 0;

											Node t = r.getAttributes().getNamedItem("X");
											if (t != null)
											{
												X = Integer.parseInt(t.getTextContent());
											}

											t = r.getAttributes().getNamedItem("Y");
											if (t != null)
											{
												Y = Integer.parseInt(t.getTextContent());
											}

											L2MapArea area = new L2MapArea(id, X, Y);

											if (!areas.containsKey(area.getId()))
											{
												restartAreas.put(id, area);
												areas.put(area.getId(), area);
												regions.put(area.getId(), area.getMapRegion());
											}
											else
												throw new Exception("Duplicate areaRegionId: " + area.getId() + ".");
										}
								}
							}
					}
			}

		_mapRegionsReload = regions;
		_mapRegionRestartReload = restarts;
		_mapRestartAreaReload = restartAreas;
		_mapAreasReload = areas;
	}

	public void reload()
	{
		load();
	}
}
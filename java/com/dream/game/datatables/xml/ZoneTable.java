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

import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.model.zone.L2Zone.ZoneType;
import com.dream.game.util.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ZoneTable
{
	protected static Logger _log = Logger.getLogger(ZoneTable.class.getName());

	private static ZoneTable _instance = null;

	public static boolean isReady = false;

	public static final ZoneTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ZoneTable();
		}
		return _instance;
	}

	public static short getMapRegion(int x, int y)
	{
		int rx = (x - L2World.MAP_MIN_X >> 15) + 16;
		int ry = (y - L2World.MAP_MIN_Y >> 15) + 10;

		return (short) ((rx << 8) + ry);
	}

	private Map<ZoneType, HashMap<String, L2Zone>> _zones;

	private ZoneTable()
	{
		load();
		isReady = true;
	}

	public L2Zone getZone(L2Zone.ZoneType type, String name)
	{
		return getZones(type).get(name);
	}

	@SuppressWarnings("unlikely-arg-type")
	public L2Zone getZoneById(int id)
	{
		for (HashMap<String, L2Zone> map : _zones.values())
			if (map.containsKey(id))
				return map.get(id);
		return null;
	}

	public Map<L2Zone.ZoneType, HashMap<String, L2Zone>> getZoneMap()
	{
		if (_zones == null)
		{
			_zones = new HashMap<>();
		}

		return _zones;
	}

	public Map<String, L2Zone> getZones(L2Zone.ZoneType type)
	{
		if (!getZoneMap().containsKey(type))
		{
			getZoneMap().put(type, new HashMap<>());
		}

		return getZoneMap().get(type);
	}

	public final L2Zone isInsideZone(L2Zone.ZoneType zt, int x, int y)
	{
		for (L2Zone temp : getZones(zt).values())
			if (temp.isInsideZone(x, y))
				return temp;

		return null;
	}

	private void load()
	{
		Document doc = null;

		for (File f : Util.getDatapackFiles("xml/zone", ".xml"))
		{
			int count = 0;
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error loading file " + f.getAbsolutePath(), e);
				continue;
			}
			try
			{
				count = parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error in file " + f.getAbsolutePath(), e);
				continue;
			}
			_log.info("Zone Data: File " + f.getName() + " loaded with " + count + " zones.");
		}
	}

	protected int parseDocument(Document doc)
	{
		int zoneCount = 0;

		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if ("zone".equalsIgnoreCase(d.getNodeName()))
					{
						L2Zone zone = L2Zone.parseZone(d);
						if (zone == null)
						{
							continue;
						}

						getZones(zone.getType()).put(zone.getName(), zone);

						int ax, ay, bx, by;
						for (int x = 0; x < worldRegions.length; x++)
						{
							for (int y = 0; y < worldRegions[x].length; y++)
							{
								ax = x - L2World.OFFSET_X << L2World.SHIFT_BY;
								bx = x + 1 - L2World.OFFSET_X << L2World.SHIFT_BY;
								ay = y - L2World.OFFSET_Y << L2World.SHIFT_BY;
								by = y + 1 - L2World.OFFSET_Y << L2World.SHIFT_BY;

								if (zone.intersectsRectangle(ax, bx, ay, by))
								{
									worldRegions[x][y].addZone(zone);
								}
							}
						}
						zoneCount++;
					}
			}

		return zoneCount;
	}

	public void reload()
	{
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		for (L2WorldRegion[] worldRegion : worldRegions)
		{
			for (L2WorldRegion element : worldRegion)
			{
				element.clearZones();
			}
		}

		load();
	}
}
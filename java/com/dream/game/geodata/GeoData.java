package com.dream.game.geodata;

import java.io.File;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.geodata.pathfinding.Node;
import com.dream.game.geodata.pathfinding.PathFinding;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.Location;
import com.dream.tools.geometry.Point3D;

public class GeoData
{
	public static enum PathFindingMode
	{
		PATHNODE,
		CELLFINDING
	}

	private static GeoData _instance;

	protected static Logger _log = Logger.getLogger(GeoData.class);

	public static GeoData getInstance()
	{
		if (_instance == null)
		{

			File f;
			if (Config.GEODATA)
			{
				try
				{
					f = new File(Config.GEODATA_ROOT, "/geodata");
					if (!f.exists())
					{
						_log.error("Geo Engine: folder geodata not found in " + Config.GEODATA_ROOT);
						System.exit(1);
						return null;
					}
					if (Config.PATHFINDING && Config.PATHFIND_MODE == PathFindingMode.PATHNODE)
					{
						f = new File(Config.GEODATA_ROOT, "/pathnode");
						if (!f.exists())
						{
							_log.error("Pathfind Engine: folder pathnode not found in " + Config.GEODATA_ROOT);
							System.exit(1);
							return null;
						}
					}
					_instance = GeoEngine.getInstance();
				}
				catch (Exception e)
				{
					_instance = new GeoData();
				}
			}
			else
			{
				_instance = new GeoData();
			}
			PathFinding.getInstance();
		}
		return _instance;
	}

	public static boolean loadGeodataFile(byte rx, byte ry)
	{
		return false;
	}

	public static void unloadGeodata(byte rx, byte ry)
	{
	}

	public void addGeoDataBug(L2PcInstance gm, String comment)
	{
	}

	public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		return true;
	}

	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		return Math.abs(z - tz) < 1000;
	}

	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		return Math.abs(target.getZ() - cha.getZ()) < 1000;
	}

	public boolean canSeeTarget(L2Object cha, Point3D worldPosition)
	{
		return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
	}

	public boolean canSeeTargetDebug(L2PcInstance gm, L2Object target)
	{
		return true;
	}

	public short findGround(int x, int y)
	{
		return 0;
	}

	public String geoPosition(int x, int y)
	{
		return "";
	}

	public short getHeight(int x, int y, int z)
	{
		return (short) z;
	}

	public short getHeightAndNSWE(int x, int y, short z)
	{
		return (short) (z << 1 | 15);
	}

	public Node[] getNeighbors(Node n)
	{
		return null;
	}

	public short getNSWE(int x, int y, int z)
	{
		return 15;
	}

	public short getSpawnHeight(int x, int y, int zmin, int zmax, int spawnid)
	{
		return (short) zmin;
	}

	public short getType(int x, int y)
	{
		return 0;
	}

	public boolean hasGeo(int x, int y)
	{
		return false;
	}

	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
	{
		return new Location(tx, ty, tz);
	}

}
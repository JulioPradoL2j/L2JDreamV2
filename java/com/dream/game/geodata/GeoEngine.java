package com.dream.game.geodata;

import com.dream.Config;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.geodata.pathfinding.Node;
import com.dream.game.geodata.pathfinding.cellnodes.CellPathFinding;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2FortSiegeGuardInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeGuardInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.Location;
import com.dream.tools.geometry.Point3D;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class GeoEngine extends GeoData
{
	private static final class SingletonHolder
	{
		protected static final GeoEngine INSTANCE = new GeoEngine();
	}

	private static final Logger _log = Logger.getLogger(GeoEngine.class);

	private final static byte _e = 1;

	private final static byte _w = 2;

	private final static byte _s = 4;

	private final static byte _n = 8;

	private static Map<Short, MappedByteBuffer> _geodata = new HashMap<>();

	private static Map<Short, IntBuffer> _geodataIndex = new HashMap<>();

	private static BufferedOutputStream _geoBugsOut;

	private static FileChannel roChannel;

	private static boolean canSee(int x, int y, double z, int tx, int ty, int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		final double dz = tz - z;
		final int distance2 = dx * dx + dy * dy;

		if (distance2 > 90000)
			return false;
		else if (distance2 < 82)
		{
			if (dz * dz > 22500)
			{
				short region = getRegionOffset(x, y);
				if (_geodata.get(region) != null)
					return false;
			}
			return true;
		}

		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		final double inc_z_directionx = dz * dx / distance2;
		final double inc_z_directiony = dz * dy / distance2;

		int next_x = x;
		int next_y = y;

		if (dx >= dy)
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;

			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, false))
						return false;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(next_x, y, (int) z, 0, inc_y, inc_z_directiony, tz, false))
						return false;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, false))
						return false;
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, false))
						return false;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, next_y, (int) z, inc_x, 0, inc_z_directionx, tz, false))
						return false;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, false))
						return false;
				}
			}
		}
		return true;
	}

	private static boolean canSeeDebug(L2PcInstance gm, int x, int y, double z, int tx, int ty, int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		final double dz = tz - z;
		final int distance2 = dx * dx + dy * dy;

		if (distance2 > 90000)
		{
			gm.sendMessage("dist > 300");
			return false;
		}
		else if (distance2 < 82)
		{
			if (dz * dz > 22500)
			{
				short region = getRegionOffset(x, y);
				if (_geodata.get(region) != null)
					return false;
			}
			return true;
		}

		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		final double inc_z_directionx = dz * dx / distance2;
		final double inc_z_directiony = dz * dy / distance2;

		gm.sendMessage("Los: from X: " + x + "Y: " + y + "--->> X: " + tx + " Y: " + ty);

		int next_x = x;
		int next_y = y;

		if (dx >= dy)
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;

			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, true))
						return false;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(next_x, y, (int) z, 0, inc_y, inc_z_directiony, tz, true))
						return false;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, y, (int) z, inc_x, 0, inc_z_directionx, tz, true))
						return false;
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, true))
						return false;
					next_x += inc_x;
					z += inc_z_directionx;
					if (!nLOS(x, next_y, (int) z, inc_x, 0, inc_z_directionx, tz, true))
						return false;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					z += inc_z_directiony;
					if (!nLOS(x, y, (int) z, 0, inc_y, inc_z_directiony, tz, true))
						return false;
				}
			}
		}
		return true;
	}

	private static boolean checkNSWE(short NSWE, int x, int y, int tx, int ty)
	{
		if (NSWE == 15)
			return true;
		if (tx > x)
		{
			if ((NSWE & _e) == 0)
				return false;
		}
		else if (tx < x)
			if ((NSWE & _w) == 0)
				return false;
		if (ty > y)
		{
			if ((NSWE & _s) == 0)
				return false;
		}
		else if (ty < y)
			if ((NSWE & _n) == 0)
				return false;
		return true;
	}

	private static int getBlock(int geo_pos)
	{
		return (geo_pos >> 3) % 256;
	}

	private static int getCell(int geo_pos)
	{
		return geo_pos % 8;
	}

	public static GeoEngine getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static short getRegionOffset(int x, int y)
	{
		int rx = x >> 11;
		int ry = y >> 11;
		return (short) ((rx + 16 << 5) + ry + 10);
	}

	
	public static boolean loadGeodataFile(byte rx, byte ry)
	{
		String fname = Config.GEODATA_ROOT + "/geodata/" + rx + "_" + ry + ".l2j";
		short regionoffset = (short) ((rx << 5) + ry);
		File Geo = new File(fname);
		int size, index = 0, block = 0, flor = 0;
		roChannel = null;

		try
		{
			roChannel = new RandomAccessFile(Geo, "r").getChannel();
			size = (int) roChannel.size();
			MappedByteBuffer geo;
			if (Config.FORCE_GEODATA)
			{
				geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			}
			else
			{
				geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			}
			geo.order(ByteOrder.LITTLE_ENDIAN);
			int sign = geo.get();
			boolean encrypted = sign == 0xF97971;
			if (!encrypted)
			{
				geo.position(0);
			}
			if (size > 196608)
			{
				IntBuffer indexs = IntBuffer.allocate(65536);
				while (block < 65536)
				{
					byte type = geo.get(index);
					indexs.put(block, index);
					block++;
					index++;
					if (type == 0)
					{
						index += 2;
					}
					else if (type == 1)
					{
						index += 128;
					}
					else
					{
						int b;
						for (b = 0; b < 64; b++)
						{
							byte layers = geo.get(index);
							index += (layers << 1) + 1;
							if (layers > flor)
							{
								flor = layers;
							}
						}
					}
				}
				_geodataIndex.put(regionoffset, indexs);
			}
			_geodata.put(regionoffset, geo);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warn("Failed to Load GeoFile at block: " + block + "\n");
			return false;
		}
		finally
		{
			try
			{
				roChannel.close();
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}

	private static Location moveCheck(Location startpoint, Location destiny, int x, int y, double z, int tx, int ty, int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		final int distance2 = dx * dx + dy * dy;

		if (distance2 == 0)
			return destiny;
		if (distance2 > 36100) // 190*190*16 = 3040 world coord
		{
			// Avoid too long check
			// Currently we calculate a middle point
			// for wyvern users and otherwise for comfort
			double divider = Math.sqrt((double) 30000 / distance2);
			tx = x + (int) (divider * dx);
			ty = y + (int) (divider * dy);
			int dz = tz - startpoint.getZ();
			tz = startpoint.getZ() + (int) (divider * dz);
			dx = tx - x;
			dy = ty - y;
			// return startpoint;
		}

		// Increment in Z coordinate when moving along X or Y axis
		// and not straight to the target. This is done because
		// calculation moves either in X or Y direction.
		final int inc_x = sign(dx);
		final int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);

		// gm.sendMessage("MoveCheck: from X: "+x+ "Y: "+y+ "--->> X: "+tx+" Y: "+ty);

		// next_* are used in NcanMoveNext check from x,y
		int next_x = x;
		int next_y = y;
		double tempz = z;

		// creates path to the target, using only x or y direction
		// calculation stops when next_* == target
		if (dx >= dy)// dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;

			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					z = tempz;
					next_y += inc_y;
					// _log.warning("2: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(next_x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					z = tempz;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					// _log.warning("3: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					z = tempz;
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				if (d > 0)
				{
					d += delta_B;
					next_y += inc_y;
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					z = tempz;
					next_x += inc_x;
					// _log.warning("5: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(x, next_y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					z = tempz;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					// _log.warning("6: next_x:"+next_x+" next_y"+next_y);
					tempz = nCanMoveNext(x, y, (int) z, next_x, next_y, tz);
					if (tempz == Double.MIN_VALUE)
						return new Location((x << 4) + L2World.MAP_MIN_X, (y << 4) + L2World.MAP_MIN_Y, (int) z);
					z = tempz;
				}
			}
		}
		if (z == startpoint.getZ())
			return destiny;
		return new Location(destiny.getX(), destiny.getY(), (int) z);
	}

	private static double nCanMoveNext(int x, int y, int z, int tx, int ty, int tz)
	{
		short region = getRegionOffset(x, y);
		int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;

		int index = 0;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return z;
		}
		byte type = geo.get(index);
		index++;
		if (type == 0)
			return geo.getShort(index);
		else if (type == 1)
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1);
			if (checkNSWE(NSWE, x, y, tx, ty))
				return height;
			return Double.MIN_VALUE;
		}
		else
		{
			cellX = getCell(x);
			cellY = getCell(y);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case3), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				return z;
			}
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1);

				if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
				{
					tempz = height;
					NSWE = geo.getShort(index);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
			if (checkNSWE(NSWE, x, y, tx, ty))
				return tempz;
			return Double.MIN_VALUE;
		}
	}

	private static short nGetHeight(int geox, int geoy, int z)
	{
		short region = getRegionOffset(geox, geoy);
		int blockX = getBlock(geox);
		int blockY = getBlock(geoy);
		int cellX, cellY, index;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return (short) z;
		}
		byte type = geo.get(index);
		index++;
		if (type == 0)
			return geo.getShort(index);
		else if (type == 1)// complex
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1);
			return height;
		}
		else
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				return (short) z;
			}
			short temph = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1);
				if ((z - temph) * (z - temph) > (z - height) * (z - height))
				{
					temph = height;
				}
				layers--;
				index += 2;
			}
			return temph;
		}
	}

	private static short nGetNSWE(int x, int y, int z)
	{
		short region = getRegionOffset(x, y);
		int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;

		int index = 0;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return 15;
		}
		byte type = geo.get(index);
		index++;
		if (type == 0)
			return 15;
		else if (type == 1)
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
		}
		else
		{
			cellX = getCell(x);
			cellY = getCell(y);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				return 15;
			}
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1);

				if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
				{
					tempz = height;
					NSWE = geo.get(index);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
		}
		return NSWE;
	}

	private static short nGetSpawnHeight(int geox, int geoy, int zmin, int zmax, int spawnid)
	{
		short region = getRegionOffset(geox, geoy);
		int blockX = getBlock(geox);
		int blockY = getBlock(geoy);
		int cellX, cellY, index;
		short temph = Short.MIN_VALUE;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return (short) zmin;
		}
		byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			temph = geo.getShort(index);
		}
		else if (type == 1)
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1);
			temph = height;
		}
		else
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			short height;
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case2), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				return (short) zmin;
			}
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1);
				if ((zmin - temph) * (zmin - temph) > (zmin - height) * (zmin - height))
				{
					temph = height;
				}
				layers--;
				index += 2;
			}
			if (temph > zmax + 200 || temph < zmin - 200)
			{
				if (_log.isDebugEnabled())
				{
					_log.warn("SpawnHeight Error - Couldnt find correct layer to spawn NPC - GeoData or Spawnlist Bug!: zmin: " + zmin + " zmax: " + zmax + " value: " + temph + " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
				}
				return (short) zmin;
			}
		}
		if (temph > zmax + 1000 || temph < zmin - 1000)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("SpawnHeight Error - Spawnlist z value is wrong or GeoData error: zmin: " + zmin + " zmax: " + zmax + " value: " + temph + " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
			}
			return (short) zmin;
		}
		return temph;
	}

	private static short nGetType(int x, int y)
	{
		short region = getRegionOffset(x, y);
		int blockX = getBlock(x);
		int blockY = getBlock(y);
		int index = 0;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return 0;
		}
		return geo.get(index);
	}

	private static short nGetUpperHeight(int geox, int geoy, int z)
	{
		short region = getRegionOffset(geox, geoy);
		int blockX = getBlock(geox);
		int blockY = getBlock(geoy);
		int cellX, cellY, index;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return (short) z;
		}
		byte type = geo.get(index);
		index++;
		if (type == 0)
			return geo.getShort(index);
		else if (type == 1)
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1);
			return height;
		}
		else
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				return (short) z;
			}
			short temph = Short.MAX_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1);
				if (height < z)
					return temph;
				temph = height;
				layers--;
				index += 2;
			}
			return temph;
		}
	}

	private static void nInitGeodata()
	{
		_log.info("Geo Engine: - Loading Geodata...");
		int fCnt = 0;
		File geodir = new File(Config.GEODATA_ROOT, "/geodata");
		if (geodir.isDirectory())
		{
			for (File f : geodir.listFiles())
				if (f.getName().endsWith(".l2j"))
				{
					try
					{

						String[] coords = f.getName().replace(".l2j", "").split("_");
						loadGeodataFile(Byte.parseByte(coords[0]), Byte.parseByte(coords[1]));
						fCnt++;
					}
					catch (Exception e)
					{
						_log.warn("Geo Engine: Error while loading " + f.getName(), e);
					}
				}
		}
		_log.info("Geo Engine: - Total Loaded: " + fCnt + " regions");
		try
		{
			File geo_bugs = new File(Config.GEODATA_ROOT, "/geodata/geo_bugs.txt");
			_geoBugsOut = new BufferedOutputStream(new FileOutputStream(geo_bugs, true));
		}
		catch (Exception e)
		{
			_geoBugsOut = new BufferedOutputStream(new ByteArrayOutputStream());
			_log.warn("GeoEngine: Failed to Load geo_bugs.txt File.");
		}
	}

	private static boolean nLOS(int x, int y, int z, int inc_x, int inc_y, double inc_z, int tz, boolean debug)
	{
		short region = getRegionOffset(x, y);
		int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;

		int index;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return true;
		}
		byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			short height = geo.getShort(index);
			if (debug)
			{
				_log.warn("flatheight:" + height);
			}
			if (z > height)
				return inc_z > height;
			return inc_z < height;
		}
		else if (type == 1)
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1);
			if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
			{
				if (debug)
				{
					_log.warn("height:" + height + " z" + z);
				}

				return z >= nGetUpperHeight(x + inc_x, y + inc_y, height);
			}
			return true;
		}
		else
		{
			cellX = getCell(x);
			cellY = getCell(y);

			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);

			index++;
			short tempZ = -1;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case4), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				return false;
			}
			short upperHeight = Short.MAX_VALUE;
			short lowerHeight = Short.MIN_VALUE;
			byte temp_layers = layers;
			boolean highestlayer = false;
			while (temp_layers > 0)
			{
				tempZ = geo.getShort(index);
				tempZ = (short) (tempZ & 0x0fff0);
				tempZ = (short) (tempZ >> 1);

				if (z > tempZ)
				{
					lowerHeight = tempZ;
					NSWE = geo.getShort(index);
					NSWE = (short) (NSWE & 0x0F);
					break;
				}
				highestlayer = false;
				upperHeight = tempZ;

				temp_layers--;
				index += 2;
			}
			if (debug)
			{
				_log.warn("z:" + z + " x: " + cellX + " y:" + cellY + " la " + layers + " lo:" + lowerHeight + " up:" + upperHeight);
			}
			if (z - upperHeight < -10 && z - upperHeight > inc_z - 10 && z - lowerHeight > 40)
			{
				if (debug)
				{
					_log.warn("false, incz" + inc_z);
				}
				return false;
			}

			if (!highestlayer)
			{
				if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
				{
					if (debug)
					{
						_log.warn("block and next in x" + inc_x + " y" + inc_y + " is:" + nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight));
					}

					return z >= nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight);
				}
				return true;
			}
			if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
				return z >= nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight);
			return true;
		}
	}

	private static byte sign(int x)
	{
		if (x >= 0)
			return +1;
		return -1;
	}

	public static void unloadGeodata(byte rx, byte ry)
	{
		short regionoffset = (short) ((rx << 5) + ry);
		_geodataIndex.remove(regionoffset);
		_geodata.remove(regionoffset);
	}

	public GeoEngine()
	{
		nInitGeodata();
	}

	@Override
	public void addGeoDataBug(L2PcInstance gm, String comment)
	{
		int gx = gm.getX() - L2World.MAP_MIN_X >> 4;
		int gy = gm.getY() - L2World.MAP_MIN_Y >> 4;
		int bx = getBlock(gx);
		int by = getBlock(gy);
		int cx = getCell(gx);
		int cy = getCell(gy);
		int rx = (gx >> 11) + 15;
		int ry = (gy >> 11) + 10;
		String out = rx + ";" + ry + ";" + bx + ";" + by + ";" + cx + ";" + cy + ";" + gm.getZ() + ";" + comment + "\n";
		try
		{
			_geoBugsOut.write(out.getBytes());
			_geoBugsOut.flush();
			gm.sendMessage("GeoData bug saved!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			gm.sendMessage("GeoData bug save Failed!");
		}
	}

	@Override
	public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		Location destiny = moveCheck(x, y, z, tx, ty, tz);
		return destiny.getX() == tx && destiny.getY() == ty && tz == destiny.getZ();
	}

	@Override
	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		return canSee(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
	}

	@Override
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		int z = cha.getZ() + 45;
		int z2 = target.getZ() + 45;

		if (cha instanceof L2SiegeGuardInstance || cha instanceof L2FortSiegeGuardInstance)
		{
			z += 30;
		}

		if (target instanceof L2DoorInstance)
			return true;

		if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2))
			return false;
		if (target instanceof L2SiegeGuardInstance || target instanceof L2FortSiegeGuardInstance)
		{
			z2 += 30;
		}
		if (cha.getZ() >= target.getZ())
			return canSeeTarget(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2);
		return canSeeTarget(target.getX(), target.getY(), z2, cha.getX(), cha.getY(), z);
	}

	@Override
	public boolean canSeeTarget(L2Object cha, Point3D target)
	{
		if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()))
			return false;

		if (cha.getZ() >= target.getZ())
			return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ());
		return canSeeTarget(target.getX(), target.getY(), target.getZ(), cha.getX(), cha.getY(), cha.getZ());
	}

	@Override
	public boolean canSeeTargetDebug(L2PcInstance gm, L2Object target)
	{
		int z = gm.getZ() + 45;
		int z2 = target.getZ() + 45;

		if (target instanceof L2DoorInstance)
		{
			gm.sendMessage("door always true");
			return true;
		}

		if (gm.getZ() >= target.getZ())
			return canSeeDebug(gm, gm.getX() - L2World.MAP_MIN_X >> 4, gm.getY() - L2World.MAP_MIN_Y >> 4, z, target.getX() - L2World.MAP_MIN_X >> 4, target.getY() - L2World.MAP_MIN_Y >> 4, z2);
		return canSeeDebug(gm, target.getX() - L2World.MAP_MIN_X >> 4, target.getY() - L2World.MAP_MIN_Y >> 4, z2, gm.getX() - L2World.MAP_MIN_X >> 4, gm.getY() - L2World.MAP_MIN_Y >> 4, z);
	}

	@Override
	public short findGround(int x, int y)
	{
		int geox = x - L2World.MAP_MIN_X >> 4;
		int geoy = y - L2World.MAP_MIN_Y >> 4;
		short region = getRegionOffset(geox, geoy);
		int blockX = getBlock(geox);
		int blockY = getBlock(geoy);
		int cellX, cellY, index;
		short temph = Short.MAX_VALUE;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
			return temph;
		byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			temph = geo.getShort(index);
		}
		else if (type == 1)
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1);
			temph = height;
		}
		else
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			short height;
			temph = 0;
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case2), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				return temph;
			}
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1);
				temph += height;
				layers--;
				index += 2;
			}
		}
		return temph;
	}

	@Override
	public String geoPosition(int x, int y)
	{
		int gx = x - L2World.MAP_MIN_X >> 4;
		int gy = y - L2World.MAP_MIN_Y >> 4;
		return "bx: " + getBlock(gx) + " by: " + getBlock(gy) + " cx: " + getCell(gx) + " cy: " + getCell(gy) + "  region offset: " + getRegionOffset(gx, gy);
	}

	@Override
	public short getHeight(int x, int y, int z)
	{
		return nGetHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z);
	}

	@Override
	public Node[] getNeighbors(Node n)
	{
		List<Node> Neighbors = new ArrayList<>(4);
		Node newNode;
		int x = n.getLoc().getNodeX();
		int y = n.getLoc().getNodeY();
		int parentdirection = 0;
		if (n.getParent() != null)
		{
			if (n.getParent().getLoc().getNodeX() > x)
			{
				parentdirection = 1;
			}
			if (n.getParent().getLoc().getNodeX() < x)
			{
				parentdirection = -1;
			}
			if (n.getParent().getLoc().getNodeY() > y)
			{
				parentdirection = 2;
			}
			if (n.getParent().getLoc().getNodeY() < y)
			{
				parentdirection = -2;
			}
		}
		short z = n.getLoc().getZ();
		short region = getRegionOffset(x, y);
		int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX, cellY;
		short NSWE = 0;
		int index = 0;
		if (_geodataIndex.get(region) == null)
		{
			index = ((blockX << 8) + blockY) * 3;
		}
		else
		{
			index = _geodataIndex.get(region).get((blockX << 8) + blockY);
		}
		ByteBuffer geo = _geodata.get(region);
		if (geo == null)
		{
			if (_log.isDebugEnabled())
			{
				_log.warn("Geo Region - Region Offset: " + region + " dosnt exist!!");
			}
			return null;
		}
		byte type = geo.get(index);
		index++;
		if (type == 0)
		{
			short height = geo.getShort(index);
			n.getLoc().setZ(height);
			if (parentdirection != 1)
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, height);
				Neighbors.add(newNode);
			}
			if (parentdirection != 2)
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, height);
				Neighbors.add(newNode);
			}
			if (parentdirection != -2)
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, height);
				Neighbors.add(newNode);
			}
			if (parentdirection != -1)
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, height);
				Neighbors.add(newNode);
			}
		}
		else if (type == 1)
		{
			cellX = getCell(x);
			cellY = getCell(y);
			index += (cellX << 3) + cellY << 1;
			short height = geo.getShort(index);
			NSWE = (short) (height & 0x0F);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1);
			n.getLoc().setZ(height);
			if (NSWE != 15 && parentdirection != 0)
				return null;
			if (parentdirection != 1 && checkNSWE(NSWE, x, y, x + 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, height);
				Neighbors.add(newNode);
			}
			if (parentdirection != 2 && checkNSWE(NSWE, x, y, x, y + 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, height);
				Neighbors.add(newNode);
			}
			if (parentdirection != -2 && checkNSWE(NSWE, x, y, x, y - 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, height);
				Neighbors.add(newNode);
			}
			if (parentdirection != -1 && checkNSWE(NSWE, x, y, x - 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, height);
				Neighbors.add(newNode);
			}
		}
		else
		{
			cellX = getCell(x);
			cellY = getCell(y);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = geo.get(index);
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = geo.get(index);
			index++;
			short height = -1;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
				return null;
			}
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = geo.getShort(index);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1);

				if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
				{
					tempz = height;
					NSWE = geo.get(index);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
			n.getLoc().setZ(tempz);
			if (NSWE != 15 && parentdirection != 0)
				return null;
			if (parentdirection != 1 && checkNSWE(NSWE, x, y, x + 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x + 1, y, tempz);
				Neighbors.add(newNode);
			}
			if (parentdirection != 2 && checkNSWE(NSWE, x, y, x, y + 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y + 1, tempz);
				Neighbors.add(newNode);
			}
			if (parentdirection != -2 && checkNSWE(NSWE, x, y, x, y - 1))
			{
				newNode = CellPathFinding.getInstance().readNode(x, y - 1, tempz);
				Neighbors.add(newNode);
			}
			if (parentdirection != -1 && checkNSWE(NSWE, x, y, x - 1, y))
			{
				newNode = CellPathFinding.getInstance().readNode(x - 1, y, tempz);
				Neighbors.add(newNode);
			}
		}
		Node[] result = new Node[Neighbors.size()];
		return Neighbors.toArray(result);
	}

	@Override
	public short getNSWE(int x, int y, int z)
	{
		return nGetNSWE(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z);
	}

	@Override
	public short getSpawnHeight(int x, int y, int zmin, int zmax, int spawnid)
	{
		return nGetSpawnHeight(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, zmin, zmax, spawnid);
	}

	@Override
	public short getType(int x, int y)
	{
		return nGetType(x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4);
	}

	@Override
	public boolean hasGeo(int x, int y)
	{
		int gx = x - L2World.MAP_MIN_X >> 4;
		int gy = y - L2World.MAP_MIN_Y >> 4;
		short region = getRegionOffset(gx, gy);
		return _geodata.get(region) != null;
	}

	@Override
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
	{
		Location startpoint = new Location(x, y, z);

		if (DoorTable.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz))
			return startpoint;

		Location destiny = new Location(tx, ty, tz);
		return moveCheck(startpoint, destiny, x - L2World.MAP_MIN_X >> 4, y - L2World.MAP_MIN_Y >> 4, z, tx - L2World.MAP_MIN_X >> 4, ty - L2World.MAP_MIN_Y >> 4, tz);
	}
}
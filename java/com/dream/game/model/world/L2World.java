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
package com.dream.game.model.world;

import com.dream.game.datatables.GmListTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.network.Disconnection;
import com.dream.tools.geometry.Point3D;
import com.dream.util.LinkedBunch;
import com.dream.util.concurrent.L2Collection;
import com.dream.util.concurrent.L2ReadWriteCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public final class L2World
{
	private static class SingletonHolder
	{
		protected static final L2World _instance = new L2World();
	}

	private static final Logger _log = Logger.getLogger(L2World.class);

	public static final int SHIFT_BY = 12;

	/** Map dimensions */
	public static final int MAP_MIN_X = -131072;

	public static final int MAP_MAX_X = 2286608;

	public static final int MAP_MIN_Y = -262144;

	public static final int MAP_MAX_Y = 262144;

	public static final int SHIFT_BY_FOR_Z = 9;

	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);

	/** number of regions */
	public static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;

	public static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;

	public static List<L2PcInstance> getAllGMs()
	{
		return GmListTable.getInstance().getAllGms(true);
	}

	public static L2World getInstance()
	{
		return SingletonHolder._instance;
	}

	public static L2Object[] getVisibleObjects(L2Object object, int radius)
	{
		if (object == null)
			return L2Object.EMPTY_ARRAY;

		final L2WorldRegion selfRegion = object.getWorldRegion();

		if (selfRegion == null)
			return L2Object.EMPTY_ARRAY;

		final int x = object.getX();
		final int y = object.getY();
		final int sqRadius = radius * radius;

		LinkedBunch<L2Object> result = new LinkedBunch<>();

		for (L2WorldRegion region : selfRegion.getSurroundingRegions())
		{
			for (L2Object obj : region.getVisibleObjects())
			{
				if (obj == null || obj == object || !obj.isVisible())
				{
					continue;
				}

				final int dx = obj.getX() - x;
				final int dy = obj.getY() - y;

				if (dx * dx + dy * dy < sqRadius)
				{
					result.add(obj);
				}
			}
		}

		return result.moveToArray(new L2Object[result.size()]);
	}

	public static L2Object[] getVisibleObjects3D(L2Object object, int radius)
	{
		if (object == null)
			return L2Object.EMPTY_ARRAY;

		final L2WorldRegion selfRegion = object.getWorldRegion();

		if (selfRegion == null)
			return L2Object.EMPTY_ARRAY;

		final int x = object.getX();
		final int y = object.getY();
		final int z = object.getZ();
		final int sqRadius = radius * radius;

		LinkedBunch<L2Object> result = new LinkedBunch<>();

		for (L2WorldRegion region : selfRegion.getSurroundingRegions())
		{
			for (L2Object obj : region.getVisibleObjects())
			{
				if (obj == null || obj == object || !obj.isVisible())
				{
					continue;
				}

				final int dx = obj.getX() - x;
				final int dy = obj.getY() - y;
				final int dz = obj.getZ() - z;

				if (dx * dx + dy * dy + dz * dz < sqRadius)
				{
					result.add(obj);
				}
			}
		}

		return result.moveToArray(new L2Object[result.size()]);
	}

	public static L2Playable[] getVisiblePlayable(L2Object object)
	{
		if (object == null)
			return L2Playable.EMPTY_ARRAY;

		final L2WorldRegion selfRegion = object.getWorldRegion();

		if (selfRegion == null)
			return L2Playable.EMPTY_ARRAY;

		LinkedBunch<L2Playable> result = new LinkedBunch<>();

		for (L2WorldRegion region : selfRegion.getSurroundingRegions())
		{
			for (L2Playable obj : region.getVisiblePlayables())
			{
				if (obj == null || obj == object || !obj.isVisible())
				{
					continue;
				}

				result.add(obj);
			}
		}

		return result.moveToArray(new L2Playable[result.size()]);
	}

	private static boolean validRegion(int x, int y)
	{
		return 0 <= x && x <= REGIONS_X && 0 <= y && y <= REGIONS_Y;
	}

	/** all visible objects */
	private final L2Collection<L2Object> _objects = new L2ReadWriteCollection<>();

	/** all the players in game */
	private final Map<String, L2PcInstance> _players;

	private final Map<Integer, L2PetInstance> _pets;

	private final L2WorldRegion[][] _worldRegions;

	public L2World()
	{

		_log.info("L2World: Setting up World Regions.");
		_players = new ConcurrentHashMap<>();
		_pets = new ConcurrentHashMap<>();
		_worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j] = new L2WorldRegion(i, j);
			}
		}

		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int a = -1; a <= 1; a++)
				{
					for (int b = -1; b <= 1; b++)
						if (validRegion(x + a, y + b))
						{
							_worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
						}
				}
			}
		}

		_log.info("L2World: (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
	}

	public L2PetInstance addPet(int ownerId, L2PetInstance pet)
	{
		return _pets.put(ownerId, pet);
	}

	public void addToAllPlayers(L2PcInstance cha)
	{
		_players.put(cha.getName().toLowerCase(), cha);
	}

	public void addVisibleObject(L2Object object, L2Character dropper)
	{
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			L2PcInstance old = getPlayer(player.getName());

			if (old != null && old != player)
			{
				_log.warn("Duplicate character!? Closing both characters (" + player.getName() + ")");

				new Disconnection(player).defaultSequence(true);
				new Disconnection(old).defaultSequence(true);
				return;
			}

			addToAllPlayers(player);
		}

		if (!object.getPosition().getWorldRegion().isActive())
			return;

		for (L2Object element : getVisibleObjects(object, 2000))
		{
			element.getKnownList().addKnownObject(object, dropper);
			object.getKnownList().addKnownObject(element, dropper);
		}
	}

	/**
	 * Deleted all spawns in the world.
	 */
	public synchronized void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPC's.");

		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}

		_log.info("All visible NPC's deleted.");
	}

	public L2Character findCharacter(int objectId)
	{
		L2Object obj = _objects.get(objectId);

		if (obj instanceof L2Character)
			return (L2Character) obj;

		return null;
	}

	public L2Object findObject(int objectId)
	{
		return _objects.get(objectId);
	}

	public boolean findObject(L2Object obj)
	{
		if (obj != null && _objects.contains(obj))
			return true;
		return false;
	}

	public L2PcInstance findPlayer(int objectId)
	{
		L2Object obj = _objects.get(objectId);

		if (obj instanceof L2PcInstance)
			return (L2PcInstance) obj;

		return null;
	}

	public Collection<L2PetInstance> getAllPets()
	{
		return _pets.values();
	}

	public Collection<L2PcInstance> getAllPlayers()
	{
		return _players.values();
	}

	/**
	 * Return how many players are online.<BR>
	 * <BR>
	 * @return number of online players.
	 */
	public int getAllPlayersCount()
	{
		return _players.size();
	}

	public L2Object[] getAllVisibleObjects()
	{
		return _objects.toArray(new L2Object[_objects.size()]);
	}

	/**
	 * Get the count of all visible objects in world.<br>
	 * <br>
	 * @return count off all L2World objects
	 */
	public final int getAllVisibleObjectsCount()
	{
		return _objects.size();
	}

	/**
	 * Returns the whole 2d array containing the world regions
	 * @return
	 */
	public L2WorldRegion[][] getAllWorldRegions()
	{
		return _worldRegions;
	}

	public L2Object[] getObjectsInRadius(int x, int y, int radius)
	{
		LinkedBunch<L2Object> result = new LinkedBunch<>();

		final L2WorldRegion selfRegion = getRegion(x, y);
		for (L2WorldRegion region : selfRegion.getSurroundingRegions())
		{
			for (L2Object obj : region.getVisibleObjects())
			{
				if (obj == null || !obj.isVisible())
				{
					continue;
				}

				final int dx = obj.getX() - x;
				final int dy = obj.getY() - y;

				if (dx * dx + dy * dy < radius)
				{
					result.add(obj);
				}
			}
		}

		return result.moveToArray(new L2Object[result.size()]);
	}

	public L2PetInstance getPet(int ownerId)
	{
		return _pets.get(ownerId);
	}

	public L2PcInstance getPlayer(int objectId)
	{
		L2Object object = _objects.get(objectId);
		return object instanceof L2PcInstance ? (L2PcInstance) object : null;
	}

	public L2PcInstance getPlayer(String name)
	{
		return _players.get(name.toLowerCase());
	}

	public L2WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}

	public L2WorldRegion getRegion(Point3D point)
	{
		return _worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}

	public void removeFromAllPlayers(L2PcInstance cha)
	{
		if (cha != null && !cha.isTeleporting())
		{
			_players.remove(cha.getName().toLowerCase());
		}
	}

	/**
	 * Remove L2Object object from _objects of L2World.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Delete item from inventory, tranfer Item from inventory to warehouse</li>
	 * <li>Crystallize item</li>
	 * <li>Remove NPC/PC/Pet from the world</li> <BR>
	 * @param object L2Object to remove from _objects of L2World
	 */
	public void removeObject(L2Object object)
	{
		_objects.remove(object); // suggestion by whatev
	}

	public void removeObjects(L2Object[] objects)
	{
		for (L2Object o : objects)
		{
			removeObject(o); // suggestion by whatev
		}
	}

	public void removeObjects(List<L2Object> list)
	{
		for (L2Object o : list)
		{
			removeObject(o); // suggestion by whatev
		}
	}

	public void removePet(int ownerId)
	{
		_pets.remove(ownerId);
	}

	public void removePet(L2PetInstance pet)
	{
		_pets.values().remove(pet);
	}

	/**
	 * Remove a L2Object from the world.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2Object (including L2PcInstance) are identified in <B>_visibleObjects</B> of his current L2WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>_players</B> of L2World, in <B>_players</B> of his current L2WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2Object object from _players* of L2World</li>
	 * <li>Remove the L2Object object from _visibleObjects and _players* of L2WorldRegion</li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters</li> <BR>
	 * <li>If object is a L2Character, remove all L2Object from its _knownObjects and all L2PcInstance from its _knownPlayer</li> <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _objects of L2World</B></FONT><BR> <BR> <I>* only if object is a L2PcInstance</I><BR> <I>** only if object is a GM L2PcInstance</I><BR> <BR> <B><U> Example of use </U> :</B><BR> <BR> <li>Pickup an Item</li> <li>Decay a L2Character</li> <BR> <BR>
	 * @param object L2object to remove from the world
	 * @param oldRegion L2WorldRegion in wich the object was before removing
	 */
	public void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
	{
		if (object == null || oldRegion == null)
			return;

		oldRegion.removeVisibleObject(object);

		// Go through all surrounding L2WorldRegion L2Characters
		for (L2WorldRegion reg : oldRegion.getSurroundingRegions())
		{
			for (L2Object obj : reg.getVisibleObjects())
			{
				obj.getKnownList().removeKnownObject(object);
				object.getKnownList().removeKnownObject(obj);
			}
		}

		object.getKnownList().removeAllKnownObjects();

		if (object instanceof L2PcInstance)
		{
			removeFromAllPlayers((L2PcInstance) object);
		}
	}

	public void storeObject(L2Object object)
	{
		if (object == null)
			return;
		L2Object obj = _objects.get(object.getObjectId());
		if (obj != null)
			if (obj != object)
			{
				if (_log.isDebugEnabled())
				{
					_log.warn("[L2World] objectId " + object.getObjectId() + " already exist in OID map!");
				}
				removeObject(object);
			}
			else
				return;

		_objects.add(object);
	}


	public static L2MonsterInstance[] getAroundMonsters(L2PcInstance player, int radius, int maxCount)
	{
		if (player == null || !player.isVisible())
			return new L2MonsterInstance[0];

		L2Object[] objects = getVisibleObjects3D(player, radius);

		List<L2MonsterInstance> monsters = new ArrayList<>();

		for (L2Object obj : objects)
		{
			if (obj instanceof L2MonsterInstance)
			{
				L2MonsterInstance monster = (L2MonsterInstance) obj;

				if (!monster.isDead())
				{
					monsters.add(monster);

					if (maxCount > 0 && monsters.size() >= maxCount)
						break;
				}
			}
		}

		return monsters.toArray(new L2MonsterInstance[0]);
	}

}
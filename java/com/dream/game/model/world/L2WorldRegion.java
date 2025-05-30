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

import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.ThreadPoolManager;
import com.dream.util.concurrent.L2Collection;
import com.dream.util.concurrent.L2SynchronizedCollection;

public final class L2WorldRegion
{
	private final class NeighborsTask implements Runnable
	{
		private final boolean _isActivating;

		public NeighborsTask(boolean isActivating)
		{
			_isActivating = isActivating;
		}

		@Override
		public void run()
		{
			if (_isActivating)
			{
				for (L2WorldRegion neighbor : getSurroundingRegions())
				{
					neighbor.setActive(true);
				}
			}
			else
			{
				if (areNeighborsEmpty())
				{
					setActive(false);
				}

				for (L2WorldRegion neighbor : getSurroundingRegions())
					if (neighbor.areNeighborsEmpty())
					{
						neighbor.setActive(false);
					}
			}
		}
	}

	private static final Logger _log = Logger.getLogger(L2WorldRegion.class);
	private final L2Collection<L2Playable> _playables = new L2SynchronizedCollection<>();

	private final L2Collection<L2Object> _objects = new L2SynchronizedCollection<>();
	private final int _tileX, _tileY;
	private L2WorldRegion[] _surroundingRegions = new L2WorldRegion[0];

	private L2Zone[] _zones = new L2Zone[0];
	private volatile boolean _active = Config.GRIDS_ALWAYS_ON;

	private ScheduledFuture<?> _neighborsTask;

	public L2WorldRegion(int pTileX, int pTileY)
	{
		_tileX = pTileX;
		_tileY = pTileY;
	}

	public void addSurroundingRegion(L2WorldRegion region)
	{
		_surroundingRegions = Arrays.copyOf(_surroundingRegions, _surroundingRegions.length + 1);
		_surroundingRegions[_surroundingRegions.length - 1] = region;
	}

	public void addVisibleObject(L2Object object)
	{
		if (Config.ASSERT)
		{
			assert object.getWorldRegion() == this;
		}

		if (object == null)
			return;

		_objects.add(object);

		if (object instanceof L2Playable)
		{
			_playables.add((L2Playable) object);

			// if this is the first player to enter the region, activate self &
			// neighbors
			if (!Config.GRIDS_ALWAYS_ON && _playables.size() == 1)
			{
				startActivation();
			}
		}
	}

	public void addZone(L2Zone zone)
	{
		_zones = Arrays.copyOf(_zones, _zones.length + 1);
		_zones[_zones.length - 1] = zone;
	}

	// check if all 9 neighbors (including self) are inactive or active but with
	// no players.
	// returns true if the above condition is met.
	public boolean areNeighborsEmpty()
	{
		// if this region is occupied, return false.
		if (isActive() && !_playables.isEmpty())
			return false;

		// if any one of the neighbors is occupied, return false
		for (L2WorldRegion neighbor : getSurroundingRegions())
			if (neighbor.isActive() && !neighbor._playables.isEmpty())
				return false;

		// in all other cases, return true.
		return true;
	}

	public boolean checkEffectRangeInsidePeaceZone(L2Skill skill, final int x, final int y, final int z)
	{
		final int range = skill.getEffectRange();
		final int up = y + range;
		final int down = y - range;
		final int left = x + range;
		final int right = x - range;

		for (L2Zone e : _zones)
			if (e.isPeace())
			{
				if (e.isInsideZone(x, up, z))
					return false;

				if (e.isInsideZone(x, down, z))
					return false;

				if (e.isInsideZone(left, y, z))
					return false;

				if (e.isInsideZone(right, y, z))
					return false;

				if (e.isInsideZone(x, y, z))
					return false;
			}

		return true;
	}

	public void clearZones()
	{
		_zones = new L2Zone[0];
	}

	public boolean containsZone(int zoneId)
	{
		for (L2Zone z : _zones)
			if (z.getId() == zoneId)
				return true;

		return false;
	}

	/**
	 * Deleted all spawns in the world.
	 */
	public synchronized void deleteVisibleNpcSpawns()
	{
		for (L2Object obj : getVisibleObjects())
			if (obj instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) obj;
				npc.deleteMe();

				L2Spawn spawn = npc.getSpawn();
				if (spawn != null)
				{
					spawn.stopRespawn();
					SpawnTable.getInstance().deleteSpawn(spawn, false);
				}
			}

		if (_log.isDebugEnabled())
		{
			_log.info("All visible NPC's deleted in Region: " + getName());
		}
	}

	public L2Object[][] getAllSurroundingObjects2DArray()
	{
		final L2Object[][] result = new L2Object[_surroundingRegions.length][];

		for (int i = 0; i < _surroundingRegions.length; i++)
		{
			result[i] = _surroundingRegions[i].getVisibleObjects();
		}

		return result;
	}

	public String getName()
	{
		return "(" + _tileX + ", " + _tileY + ")";
	}

	public L2WorldRegion[] getSurroundingRegions()
	{
		return _surroundingRegions;
	}

	public L2Object[] getVisibleObjects()
	{
		return _objects.toArray(new L2Object[_objects.size()]);
	}

	public L2Playable[] getVisiblePlayables()
	{
		return _playables.toArray(new L2Playable[_playables.size()]);
	}

	public L2Zone[] getZones()
	{
		return _zones;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void onDeath(L2Character character)
	{
		for (L2Zone z : _zones)
		{
			z.onDieInside(character);
		}
	}

	public void onRevive(L2Character character)
	{
		for (L2Zone z : _zones)
		{
			z.onReviveInside(character);
		}
	}

	public void removeFromZones(L2Character character)
	{
		for (L2Zone z : _zones)
		{
			z.removeCharacter(character);
		}
	}

	public void removeVisibleObject(L2Object object)
	{
		if (object == null)
			return;

		_objects.remove(object);

		if (object instanceof L2Playable)
		{
			_playables.remove((L2Playable) object);

			if (!Config.GRIDS_ALWAYS_ON && _playables.isEmpty())
			{
				startDeactivation();
			}
		}
	}

	public void revalidateZones(L2Character character)
	{
		// do NOT update the world region while the character is still in the
		// process of teleporting
		// Once the teleport is COMPLETED, revalidation occurs safely, at that
		// time.

		if (character.isTeleporting())
			return;

		for (L2Zone z : _zones)
		{
			z.revalidateInZone(character);
		}
	}

	public void setActive(boolean active)
	{
		if (_active == active)
			return;

		_active = active;

		if (!active)
		{
			for (L2Object obj : getVisibleObjects())
				if (obj instanceof L2Attackable)
				{
					L2Attackable mob = (L2Attackable) obj;

					mob.setTarget(null);
					mob.stopMove(null, false);
					mob.stopAllEffects();
					mob.clearAggroList();
					mob.clearDamageContributors();
					mob.resetAbsorbList();
					mob.getKnownList().removeAllKnownObjects();
					mob.getAI().setIntention(CtrlIntention.IDLE);
					mob.getAI().stopAITask();
				}
		}
		else
		{
			final L2Object[][] surroundingObjects = getAllSurroundingObjects2DArray();

			for (L2Object obj : getVisibleObjects())
			{
				if (obj == null)
				{
					continue;
				}

				if (obj instanceof L2Attackable)
				{
					((L2Attackable) obj).getStatus().startHpMpRegeneration();
				}
				else if (obj instanceof L2Npc)
				{
					((L2Npc) obj).startRandomAnimationTimer();
				}

				obj.getKnownList().tryAddObjects(surroundingObjects);
			}
		}
	}

	/**
	 * Immediately sets self as active and starts a timer to set neighbors as active this timer is to avoid turning on neighbors in the case when a person just teleported into a region and then teleported out immediately...there is no reason to activate all the neighbors in that case.
	 */
	private void startActivation()
	{
		// first set self to active and do self-tasks...
		setActive(true);

		// if the timer to deactivate neighbors is running, cancel it.
		synchronized (this)
		{
			if (_neighborsTask != null)
			{
				_neighborsTask.cancel(true);
				_neighborsTask = null;
			}

			// then, set a timer to activate the neighbors
			_neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
		}
	}

	/**
	 * starts a timer to set neighbors (including self) as inactive this timer is to avoid turning off neighbors in the case when a person just moved out of a region that he may very soon return to. There is no reason to turn self & neighbors off in that case.
	 */
	private void startDeactivation()
	{
		// if the timer to activate neighbors is running, cancel it.
		synchronized (this)
		{
			if (_neighborsTask != null)
			{
				_neighborsTask.cancel(true);
				_neighborsTask = null;
			}

			// start a timer to "suggest" a deactivate to self and neighbors.
			// suggest means: first check if a neighbor has L2PcInstances in it.
			// If not, deactivate.
			_neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000 * Config.GRID_NEIGHBOR_TURNOFF_TIME);
		}
	}
}
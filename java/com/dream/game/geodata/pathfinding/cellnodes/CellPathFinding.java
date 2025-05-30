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
package com.dream.game.geodata.pathfinding.cellnodes;

import java.util.List;

import com.dream.game.geodata.GeoData;
import com.dream.game.geodata.pathfinding.AbstractNodeLoc;
import com.dream.game.geodata.pathfinding.Node;
import com.dream.game.geodata.pathfinding.PathFinding;
import com.dream.game.model.world.L2World;

public class CellPathFinding extends PathFinding
{
	private static CellPathFinding _instance;

	public static CellPathFinding getInstance()
	{
		if (_instance == null)
		{
			_instance = new CellPathFinding();
		}
		return _instance;
	}

	private CellPathFinding()
	{
		_log.info("PathFinding Engine: - Using CellPathFinding");
	}

	@Override
	public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, boolean playable)
	{
		int gx = x - L2World.MAP_MIN_X >> 4;
		int gy = y - L2World.MAP_MIN_Y >> 4;

		if (!GeoData.getInstance().hasGeo(x, y))
			return null;

		short gz = GeoData.getInstance().getHeight(x, y, z);
		int gtx = tx - L2World.MAP_MIN_X >> 4;
		int gty = ty - L2World.MAP_MIN_Y >> 4;

		if (!GeoData.getInstance().hasGeo(tx, ty))
			return null;

		short gtz = GeoData.getInstance().getHeight(tx, ty, tz);
		Node start = readNode(gx, gy, gz);
		Node end = readNode(gtx, gty, gtz);

		return searchByClosest(start, end);
	}

	@Override
	public boolean pathNodesExist(short regionoffset)
	{
		return false;
	}

	@Override
	public Node[] readNeighbors(Node n, int idx)
	{
		return GeoData.getInstance().getNeighbors(n);
	}

	public Node readNode(int gx, int gy, short z)
	{
		return new Node(new NodeLoc(gx, gy, z), 0);
	}
}
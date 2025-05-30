package com.dream.game.geodata.pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.geodata.GeoData;
import com.dream.game.geodata.GeoData.PathFindingMode;
import com.dream.game.geodata.pathfinding.cellnodes.CellPathFinding;
import com.dream.game.geodata.pathfinding.geonodes.GeoPathFinding;
import com.dream.game.geodata.pathfinding.utils.BinaryNodeHeap;
import com.dream.game.geodata.pathfinding.utils.CellNodeMap;
import com.dream.game.geodata.pathfinding.utils.FastNodeList;
import com.dream.game.model.world.L2World;

public class PathFinding
{
	private static PathFinding _instance;

	protected static Logger _log = Logger.getLogger(PathFinding.class);

	public static PathFinding getInstance()
	{
		if (_instance == null)
			if (Config.GEODATA)
			{
				if (Config.PATHFIND_MODE == PathFindingMode.CELLFINDING)
				{
					_instance = CellPathFinding.getInstance();
				}
				else
				{
					_instance = GeoPathFinding.getInstance();
				}
			}
			else
			{
				_instance = new PathFinding();
				_log.error("PathFindingEngine: No engine avaliable, using default");
			}
		return _instance;
	}

	public int calculateWorldX(short node_x)
	{
		return L2World.MAP_MIN_X + node_x * 128 + 48;
	}

	public int calculateWorldY(short node_y)
	{
		return L2World.MAP_MIN_Y + node_y * 128 + 48;
	}

	public List<AbstractNodeLoc> constructPath(Node node)
	{
		LinkedList<AbstractNodeLoc> path = new LinkedList<>();
		int previousdirectionx = -1000;
		int previousdirectiony = -1000;
		int directionx;
		int directiony;
		while (node.getParent() != null)
		{
			if (node.getParent().getParent() != null && Math.abs(node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX()) == 1 && Math.abs(node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY()) == 1)
			{
				directionx = node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX();
				directiony = node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY();
			}
			else
			{
				directionx = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
				directiony = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
			}
			if (directionx != previousdirectionx || directiony != previousdirectiony)
			{
				previousdirectionx = directionx;
				previousdirectiony = directiony;
				path.addFirst(node.getLoc());
			}
			node = node.getParent();
		}
		if (path.size() > 4)
		{
			List<Integer> valueList = new ArrayList<>();
			for (int index = 0; index < path.size() - 3; index = index + 3)
				if (GeoData.getInstance().canMoveFromToTarget(path.get(index).getX(), path.get(index).getY(), path.get(index).getZ(), path.get(index + 3).getX(), path.get(index + 3).getY(), path.get(index + 3).getZ()))
				{
					valueList.add(index + 1);
					valueList.add(index + 2);
				}
			for (int index = valueList.size() - 1; index >= 0; index--)
			{
				path.remove(valueList.get(index).intValue());
			}
		}
		return path;
	}

	public List<AbstractNodeLoc> constructPath2(Node node)
	{
		LinkedList<AbstractNodeLoc> path = new LinkedList<>();
		int previousdirectionx = -1000;
		int previousdirectiony = -1000;
		int directionx;
		int directiony;
		while (node.getParent() != null)
		{
			directionx = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
			directiony = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
			if (directionx != previousdirectionx || directiony != previousdirectiony)
			{
				previousdirectionx = directionx;
				previousdirectiony = directiony;
				path.addFirst(node.getLoc());
			}
			node = node.getParent();
		}
		return path;
	}

	public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, boolean playable)
	{
		return null;
	}

	public short getNodeBlock(int node_pos)
	{
		return (short) (node_pos % 256);
	}

	public short getNodePos(int geo_pos)
	{
		return (short) (geo_pos >> 3);
	}

	public short getRegionOffset(byte rx, byte ry)
	{
		return (short) ((rx << 5) + ry);
	}

	public byte getRegionX(int node_pos)
	{
		return (byte) ((node_pos >> 8) + 15);
	}

	public byte getRegionY(int node_pos)
	{
		return (byte) ((node_pos >> 8) + 10);
	}

	public boolean pathNodesExist(short regionoffset)
	{
		return false;
	}

	public Node[] readNeighbors(Node n, int idx)
	{
		return null;
	}

	public List<AbstractNodeLoc> search(Node start, Node end, int instance)
	{
		LinkedList<Node> visited = new LinkedList<>();

		LinkedList<Node> to_visit = new LinkedList<>();
		to_visit.add(start);

		int i = 0;
		while (i < 800)
		{
			Node node;
			try
			{
				node = to_visit.removeFirst();
			}
			catch (Exception e)
			{
				return null;
			}
			if (node.equals(end))
				return constructPath(node);
			i++;
			visited.add(node);
			node.attachNeighbors();
			Node[] neighbors = node.getNeighbors();
			if (neighbors == null)
			{
				continue;
			}
			for (Node n : neighbors)
				if (!visited.contains(n) && !to_visit.contains(n))
				{
					n.setParent(node);
					to_visit.add(n);
				}
		}
		return null;
	}

	public List<AbstractNodeLoc> searchAStar(Node start, Node end, int instance)
	{
		int start_x = start.getLoc().getX();
		int start_y = start.getLoc().getY();
		int end_x = end.getLoc().getX();
		int end_y = end.getLoc().getY();
		FastNodeList visited = new FastNodeList(800);

		BinaryNodeHeap to_visit = new BinaryNodeHeap(800);
		to_visit.add(start);

		int i = 0;
		while (i < 800)
		{
			Node node;
			try
			{
				node = to_visit.removeFirst();
			}
			catch (Exception e)
			{
				return null;
			}
			if (node.equals(end))
				return constructPath(node);
			visited.add(node);
			node.attachNeighbors();
			for (Node n : node.getNeighbors())
				if (!visited.contains(n) && !to_visit.contains(n))
				{
					i++;
					n.setParent(node);
					n.setCost(Math.abs(start_x - n.getLoc().getNodeX()) + Math.abs(start_y - n.getLoc().getNodeY()) + Math.abs(end_x - n.getLoc().getNodeX()) + Math.abs(end_y - n.getLoc().getNodeY()));
					to_visit.add(n);
				}
		}
		return null;
	}

	public List<AbstractNodeLoc> searchByClosest(Node start, Node end)
	{
		CellNodeMap known = new CellNodeMap();

		LinkedList<Node> to_visit = new LinkedList<>();
		to_visit.add(start);
		known.add(start);
		int targetx = end.getLoc().getNodeX();
		int targety = end.getLoc().getNodeY();
		int targetz = end.getLoc().getZ();

		int dx, dy, dz;
		boolean added;
		int i = 0;
		while (i < Config.PATH_LENGTH)
		{
			Node node;
			try
			{
				node = to_visit.removeFirst();
			}
			catch (Exception e)
			{
				return null;
			}
			i++;

			node.attachNeighbors();
			if (node.equals(end))
				return constructPath(node);

			Node[] neighbors = node.getNeighbors();
			if (neighbors == null)
			{
				continue;
			}
			for (Node n : neighbors)
				if (!known.contains(n))
				{

					added = false;
					n.setParent(node);
					dx = targetx - n.getLoc().getNodeX();
					dy = targety - n.getLoc().getNodeY();
					dz = targetz - n.getLoc().getZ();
					n.setCost(dx * dx + dy * dy + dz / 2 * dz);
					for (int index = 0; index < to_visit.size(); index++)
						if (to_visit.get(index).getCost() > n.getCost())
						{
							to_visit.add(index, n);
							added = true;
							break;
						}
					if (!added)
					{
						to_visit.addLast(n);
					}
					known.add(n);
				}
		}
		return null;
	}

	public List<AbstractNodeLoc> searchByClosest2(Node start, Node end)
	{
		FastNodeList visited = new FastNodeList(550);

		LinkedList<Node> to_visit = new LinkedList<>();
		to_visit.add(start);
		int targetx = end.getLoc().getNodeX();
		int targety = end.getLoc().getNodeY();
		int dx, dy;
		boolean added;
		int i = 0;
		while (i < 550)
		{
			Node node;
			try
			{
				node = to_visit.removeFirst();
			}
			catch (Exception e)
			{
				return null;
			}
			if (node.equals(end))
				return constructPath2(node);
			i++;
			visited.add(node);
			node.attachNeighbors();
			Node[] neighbors = node.getNeighbors();
			if (neighbors == null)
			{
				continue;
			}
			for (Node n : neighbors)
				if (!visited.containsRev(n) && !to_visit.contains(n))
				{
					added = false;
					n.setParent(node);
					dx = targetx - n.getLoc().getNodeX();
					dy = targety - n.getLoc().getNodeY();
					n.setCost(dx * dx + dy * dy);
					for (int index = 0; index < to_visit.size(); index++)
						if (to_visit.get(index).getCost() > n.getCost())
						{
							to_visit.add(index, n);
							added = true;
							break;
						}
					if (!added)
					{
						to_visit.addLast(n);
					}
				}
		}
		return null;
	}
}
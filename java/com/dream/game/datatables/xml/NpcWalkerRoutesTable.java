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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dream.Config;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.model.L2NpcWalkerNode;

import javolution.util.FastList;

public class NpcWalkerRoutesTable
{
	public static NpcWalkerRoutesTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NpcWalkerRoutesTable();
			_log.info("Initializing Walkers Routes Table.");
		}

		return _instance;
	}

	private final static Logger _log = Logger.getLogger(SpawnTable.class.getName());

	private static NpcWalkerRoutesTable _instance;

	private final FastList<L2NpcWalkerNode> _routes = new FastList<>();

	private NpcWalkerRoutesTable()
	{
		if (Config.ALLOW_NPC_WALKERS)
			load();
	}

	public FastList<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		FastList<L2NpcWalkerNode> _return = new FastList<>();

		for (FastList.Node<L2NpcWalkerNode> n = _routes.head(), end = _routes.tail(); (n = n.getNext()) != end;)
			if (n.getValue().getNpcId() == id)
				_return.add(n.getValue());

		return _return;
	}

	public void load()
	{
		_routes.clear();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/world/walkers_routes.xml");
		if (!f.exists())
		{
			_log.warn("walker_routes.xml could not be loaded: file not found");
			return;
		}
		try
		{
			InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			L2NpcWalkerNode route;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if (n.getNodeName().equalsIgnoreCase("list"))
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if (d.getNodeName().equalsIgnoreCase("walker_route"))
						{
							route = new L2NpcWalkerNode();

							int route_id = Integer.valueOf(d.getAttributes().getNamedItem("route_id").getNodeValue());
							int npc_id = Integer.valueOf(d.getAttributes().getNamedItem("npc_id").getNodeValue());
							String move_point = String.valueOf(d.getAttributes().getNamedItem("move_point").getNodeValue());
							String chatText = String.valueOf(d.getAttributes().getNamedItem("chatText").getNodeValue());
							int move_x = Integer.valueOf(d.getAttributes().getNamedItem("move_x").getNodeValue());
							int move_y = Integer.valueOf(d.getAttributes().getNamedItem("move_y").getNodeValue());
							int move_z = Integer.valueOf(d.getAttributes().getNamedItem("move_z").getNodeValue());
							int delay = Integer.valueOf(d.getAttributes().getNamedItem("delay").getNodeValue());
							boolean running = Boolean.valueOf(d.getAttributes().getNamedItem("running").getNodeValue());

							route.setRouteId(route_id);
							route.setNpcId(npc_id);
							route.setMovePoint(move_point);
							route.setChatText(chatText);
							route.setMoveX(move_x);
							route.setMoveY(move_y);
							route.setMoveZ(move_z);
							route.setDelay(delay);
							route.setRunning(running);

							_routes.add(route);
							route = null;
						}
		}
		catch (SAXException e)
		{
			_log.error("Error while creating table", e);
		}
		catch (IOException e)
		{
			_log.error("Error while creating table", e);
		}
		catch (ParserConfigurationException e)
		{
			_log.error("Error while creating table", e);
		}

	}
}
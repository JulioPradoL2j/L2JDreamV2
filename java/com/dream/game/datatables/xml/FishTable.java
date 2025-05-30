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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.dream.game.model.FishData;
import com.dream.tools.random.Rnd;
import com.dream.util.XMLDocumentFactory;

public class FishTable
{
	private static class SingletonHolder
	{
		protected static final FishTable _instance = new FishTable();
	}

	private final static Logger _log = Logger.getLogger(FishTable.class.getName());

	private final static List<FishData> _fishes = new ArrayList<>();

	public static FishData getFish(int lvl, int type, int group)
	{
		final List<FishData> result = new ArrayList<>();

		for (FishData fish : _fishes)
		{
			if (fish.getLevel() != lvl || fish.getType() != type || fish.getGroup() != group)
			{
				continue;
			}

			result.add(fish);
		}

		if (result.isEmpty())
		{
			_log.warn("Couldn't find any fish with lvl: " + lvl + " and type: " + type);
			return null;
		}

		return result.get(Rnd.get(result.size()));
	}

	public static FishTable getInstance()
	{
		return SingletonHolder._instance;
	}

	protected FishTable()
	{
		try
		{
			final File f = new File("./data/xml/world/fishes.xml");
			final Document doc = XMLDocumentFactory.getInstance().loadDocument(f);

			final Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				if (d.getNodeName().equalsIgnoreCase("fish"))
				{
					NamedNodeMap attrs = d.getAttributes();

					int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					int lvl = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
					String name = attrs.getNamedItem("name").getNodeValue();
					int hp = Integer.parseInt(attrs.getNamedItem("hp").getNodeValue());
					int hpreg = Integer.parseInt(attrs.getNamedItem("hpregen").getNodeValue());
					int type = Integer.parseInt(attrs.getNamedItem("fish_type").getNodeValue());
					int group = Integer.parseInt(attrs.getNamedItem("fish_group").getNodeValue());
					int fish_guts = Integer.parseInt(attrs.getNamedItem("fish_guts").getNodeValue());
					int guts_check_time = Integer.parseInt(attrs.getNamedItem("guts_check_time").getNodeValue());
					int wait_time = Integer.parseInt(attrs.getNamedItem("wait_time").getNodeValue());
					int combat_time = Integer.parseInt(attrs.getNamedItem("combat_time").getNodeValue());

					_fishes.add(new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time));
				}
		}
		catch (Exception e)
		{
			_log.warn("FishTable: Error while creating table" + e);
		}

		_log.info("Fish Data: Loaded " + _fishes.size() + " fishes.");
	}
}
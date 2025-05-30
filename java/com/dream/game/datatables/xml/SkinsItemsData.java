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

/**
 *
 * @author efireX
 */

package com.dream.game.datatables.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.dream.Config;
import com.dream.game.model.L2SkinItem;

public class SkinsItemsData
{
	private static final Logger _log = Logger.getLogger(SkinsItemsData.class.getName());

	private static SkinsItemsData _instance;

	public static SkinsItemsData getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkinsItemsData();
		}

		return _instance;
	}

	private final Map<Integer, L2SkinItem> _skinitems;

	private int[] _skinItemIds;

	public SkinsItemsData()
	{
		_skinitems = new HashMap<>();
		Document doc = null;
		File file = new File(Config.DATAPACK_ROOT, "data/xml/player/skin_items.xml");

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);

			int itemID = 0, hairID = 0, chestID = 0, legsID = 0, glovesID = 0, feetID = 0;
			Node a;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							a = d.getAttributes().getNamedItem("id");
							if (a == null)
								throw new Exception("Error in skin item defenition!");
							itemID = Integer.parseInt(a.getNodeValue());

							for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())

								if ("hairId".equalsIgnoreCase(e.getNodeName()))
								{
									a = e.getAttributes().getNamedItem("val");
									if (a == null)
										throw new Exception("Not defined hair id for skin item id=" + itemID + "!");
									hairID = Integer.parseInt(a.getNodeValue());
								}
								else if ("chestId".equalsIgnoreCase(e.getNodeName()))
								{
									a = e.getAttributes().getNamedItem("val");
									if (a == null)
										throw new Exception("Not defined hair for skin item id=" + itemID + "!");
									chestID = Integer.parseInt(a.getNodeValue());
								}
								else if ("legsId".equalsIgnoreCase(e.getNodeName()))
								{
									a = e.getAttributes().getNamedItem("val");
									if (a == null)
										throw new Exception("Not defined leg for skin item id=" + itemID + "!");
									legsID = Integer.parseInt(a.getNodeValue());
								}
								else if ("glovesId".equalsIgnoreCase(e.getNodeName()))
								{
									a = e.getAttributes().getNamedItem("val");
									if (a == null)
										throw new Exception("Not defined gloves for skin item id=" + itemID + "!");
									glovesID = Integer.parseInt(a.getNodeValue());
								}
								else if ("feetId".equalsIgnoreCase(e.getNodeName()))
								{
									a = e.getAttributes().getNamedItem("val");
									if (a == null)
										throw new Exception("Not defined leg for skin item id=" + itemID + "!");
									feetID = Integer.parseInt(a.getNodeValue());
								}
							L2SkinItem skinitem = new L2SkinItem(itemID, hairID, chestID, legsID, glovesID, feetID);
							_skinitems.put(itemID, skinitem);
						}
				}
			_skinItemIds = new int[_skinitems.size()];
			int i = 0;
			for (int itemId : _skinitems.keySet())
			{
				_skinItemIds[i++] = itemId;
			}
		}
		catch (IOException e)
		{
			_log.warn("SkinItemsData: Can not find " + file.getAbsolutePath() + " !", e);
		}
		catch (Exception e)
		{
			_log.warn("SkinItemsData: Error while parsing " + file.getAbsolutePath() + " !", e);
		}
		_log.info("Skin Items Data: Loaded " + _skinitems.size() + " Skin Items.");
	}

	public L2SkinItem getSkinItem(int itemId)
	{
		return _skinitems.get(itemId);
	}

	public int[] itemIDs()
	{
		return _skinItemIds;
	}
}
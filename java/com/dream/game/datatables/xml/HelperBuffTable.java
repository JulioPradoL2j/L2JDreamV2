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

import com.dream.game.templates.L2HelperBuff;
import com.dream.util.StatsSet;
import com.dream.util.XMLDocumentFactory;

public class HelperBuffTable
{
	private static class SingletonHolder
	{
		protected static final HelperBuffTable _instance = new HelperBuffTable();
	}

	private final static Logger _log = Logger.getLogger(HelperBuffTable.class.getName());

	public static HelperBuffTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private final List<L2HelperBuff> _helperBuff;
	private int _magicClassLowestLevel = 100;
	private int _physicClassLowestLevel = 100;

	private int _magicClassHighestLevel = 1;

	private int _physicClassHighestLevel = 1;

	protected HelperBuffTable()
	{
		_helperBuff = new ArrayList<>();

		try
		{
			File f = new File("./data/xml/player/helper_buff_list.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(f);

			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				if (d.getNodeName().equalsIgnoreCase("buff"))
				{
					NamedNodeMap attrs = d.getAttributes();

					int id = Integer.valueOf(attrs.getNamedItem("id").getNodeValue());
					int skill_id = Integer.valueOf(attrs.getNamedItem("skill_id").getNodeValue());
					int skill_level = Integer.valueOf(attrs.getNamedItem("skill_level").getNodeValue());
					int lower_level = Integer.valueOf(attrs.getNamedItem("lower_level").getNodeValue());
					int upper_level = Integer.valueOf(attrs.getNamedItem("upper_level").getNodeValue());
					boolean is_magic_class = Boolean.valueOf(attrs.getNamedItem("is_magic_class").getNodeValue());

					StatsSet helperBuffDat = new StatsSet();

					helperBuffDat.set("id", id);
					helperBuffDat.set("skillID", skill_id);
					helperBuffDat.set("skillLevel", skill_level);
					helperBuffDat.set("lowerLevel", lower_level);
					helperBuffDat.set("upperLevel", upper_level);
					helperBuffDat.set("isMagicClass", is_magic_class);

					if (!is_magic_class)
					{
						if (lower_level < _physicClassLowestLevel)
						{
							_physicClassLowestLevel = lower_level;
						}

						if (upper_level > _physicClassHighestLevel)
						{
							_physicClassHighestLevel = upper_level;
						}
					}
					else
					{
						if (lower_level < _magicClassLowestLevel)
						{
							_magicClassLowestLevel = lower_level;
						}

						if (upper_level > _magicClassHighestLevel)
						{
							_magicClassHighestLevel = upper_level;
						}
					}

					_helperBuff.add(new L2HelperBuff(helperBuffDat));
				}
		}
		catch (Exception e)
		{
			_log.warn("HelperBuffTable: Error while creating table" + e);
		}

		_log.info("HelperBuff Data: Loaded " + _helperBuff.size() + " buffs.");
	}

	public List<L2HelperBuff> getHelperBuffTable()
	{
		return _helperBuff;
	}

	public int getMagicClassHighestLevel()
	{
		return _magicClassHighestLevel;
	}

	public int getMagicClassLowestLevel()
	{
		return _magicClassLowestLevel;
	}

	public int getPhysicClassHighestLevel()
	{
		return _physicClassHighestLevel;
	}

	public int getPhysicClassLowestLevel()
	{
		return _physicClassLowestLevel;
	}
}
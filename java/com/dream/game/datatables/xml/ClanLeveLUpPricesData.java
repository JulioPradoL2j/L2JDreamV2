package com.dream.game.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.dream.Config;
import com.dream.data.xml.XMLDocument;

public class ClanLeveLUpPricesData extends XMLDocument
{
	public class ClanLevel
	{
		public int sp = 0;
		public int exp = 0;
		public int reputation = 0;
		public int members = 0;

		private final Map<Integer, Integer> items = new HashMap<>();

		ClanLevel(int level)
		{
		}

		public void addItem(int id, int count)
		{
			if (items.containsKey(id))
			{
				count += items.get(id);
			}
			items.put(id, count);
		}

		public Map<Integer, Integer> getItems()
		{
			return items;
		}
	}

	private static final Logger _log = Logger.getLogger(ClanLeveLUpPricesData.class);

	private static ClanLeveLUpPricesData _instance = new ClanLeveLUpPricesData();

	public static ClanLeveLUpPricesData getInstance()
	{
		return _instance;
	}

	private final Map<Integer, ClanLevel> _levels;

	private ClanLeveLUpPricesData()
	{
		_levels = new HashMap<>();
		load();
	}

	public ClanLevel getClanLevel(int level)
	{
		return _levels.get(level);
	}

	public void load()
	{
		try
		{
			load(new File(Config.DATAPACK_ROOT, "data/xml/world/ClanLevelUpPrice.xml"));
			_log.info("ClanLevelUpPrice: Loaded " + _levels.size() + " levels.");
		}
		catch (Exception e)
		{
			_log.warn("ClanLevelUpPrice: Error while loading doors", e);
		}
	}

	@Override
	protected void parseDocument(Document doc)
	{
		for (Node list : getNodes(doc))
			if (isNodeName(list, "list"))
			{
				for (Node level : getNodes(list))
					if (isNodeName(level, "level"))
					{
						int lvl = get(level, "id", 0);
						ClanLevel clanLevel = new ClanLevel(lvl);
						_levels.put(lvl, clanLevel);
						for (Node data : getNodes(level))
							if (isNodeName(data, "item"))
							{
								clanLevel.addItem(get(data, "id", 0), get(data, "count", 0));
							}
							else if (isNodeName(data, "sp"))
							{
								clanLevel.sp += get(data, "count", 0);
							}
							else if (isNodeName(data, "exp"))
							{
								clanLevel.exp += get(data, "count", 0);
							}
							else if (isNodeName(data, "members"))
							{
								clanLevel.members += get(data, "count", 0);
							}
							else if (isNodeName(data, "reputation"))
							{
								clanLevel.reputation += get(data, "count", 0);
							}
					}
			}
	}
}
package com.dream.game.datatables.xml;

import com.dream.data.xml.IXmlReader;
import com.dream.game.model.holders.RoletaItem;
import com.dream.tools.random.Rnd;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RoletaData implements IXmlReader
{
	private final List<RoletaItem> _items = new ArrayList<>();
	
	private boolean _useAdena = true;
	private int _adenaCost = 10000;
	private int _itemId = 4037;
	private int _itemCount = 5;
	
	protected RoletaData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_items.clear();
		parseFile("./data/custom/mods/roulette.xml");
		LOGGER.info("Loaded {" + _items.size() + "} roulette items.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode ->
		{
			forEach(listNode, node ->
			{
				switch (node.getNodeName())
				{
					case "config":
						parseConfig(node);
						break;
					case "item":
						parseItem(node);
						break;
				}
			});
		});
	}
	
	private void parseConfig(Node node)
	{
		_useAdena = parseBoolean(node.getAttributes(), "useAdena");
		_adenaCost = parseInteger(node.getAttributes(), "adenaCost");
		_itemId = parseInteger(node.getAttributes(), "itemId");
		_itemCount = parseInteger(node.getAttributes(), "itemCount");
	}
	
	private void parseItem(Node node)
	{
		final int id = parseInteger(node.getAttributes(), "id");
		final int count = parseInteger(node.getAttributes(), "count");
		final int enchant = parseInteger(node.getAttributes(), "enchant");
		final int chance = parseInteger(node.getAttributes(), "chance");
		
		_items.add(new RoletaItem(id, count, enchant, chance));
	}
	
	public RoletaItem getRandomItem()
	{
		int random = (int) (Math.random() * 100000);
		int cumulativeChance = 0;
		
		for (RoletaItem item : _items)
		{
			cumulativeChance += item.getChance();
			if (random < cumulativeChance)
				return item;
		}
		return null;
	}
	
	public RoletaItem getRandomVisualItem()
	{
		if (_items.isEmpty())
			return null;
		
		int index = Rnd.get(_items.size());
		return _items.get(index);
	}
	
	public boolean isUseAdena()
	{
		return _useAdena;
	}
	
	public int getAdenaCost()
	{
		return _adenaCost;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getItemCount()
	{
		return _itemCount;
	}
	
	public static RoletaData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RoletaData INSTANCE = new RoletaData();
	}
}

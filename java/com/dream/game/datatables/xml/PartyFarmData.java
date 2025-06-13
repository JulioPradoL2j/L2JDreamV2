package com.dream.game.datatables.xml;

import com.dream.data.xml.IXmlReader;
import com.dream.data.xml.StatSet;
import com.dream.game.model.L2PartyFarmEvent;
import com.dream.game.model.holders.PTFarmHolder;
import com.dream.game.model.holders.PartyFarmConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PartyFarmData implements IXmlReader
{
	private final Map<String, List<PTFarmHolder>> _ptfarm = new HashMap<>();
	private PartyFarmConfig _config;
	
	public PartyFarmData()
	{
		load();
		L2PartyFarmEvent.start();
	}
	
	public void reload()
	{
		L2PartyFarmEvent.reset();
		_ptfarm.clear();
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/custom/events/partyfarm.xml");
		LOGGER.info("Loaded {" + _ptfarm.size() + "} Party Farm spawns.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "partyfarm", eventsNode ->
		{
			forEach(eventsNode, "event", eventNode ->
			{
				StatSet set = parseAttributes(eventNode);
				String id = set.getString("name", "partyfarm");
				
				boolean enabled = Boolean.parseBoolean(getChildText(eventNode, "enabled"));
				int duration = Integer.parseInt(getChildText(eventNode, "duration"));
				int preparation = Integer.parseInt(getChildText(eventNode, "preparation"));
				
				String[] dayTokens = getChildText(eventNode, "days").split(",");
				List<Integer> days = new ArrayList<>();
				for (String token : dayTokens)
					days.add(Integer.parseInt(token.trim()));
				
				List<String> times = new ArrayList<>();
				forEach(eventNode, "times", timesNode ->
				{
					forEach(timesNode, "time", timeNode -> times.add(timeNode.getTextContent()));
				});
				
				_config = new PartyFarmConfig(enabled, duration, preparation, days, times);
				
				forEach(eventNode, "spawns", spawnsNode ->
				{
					forEach(spawnsNode, "spawn", spawnNode ->
					{
						StatSet spawnSet = parseAttributes(spawnNode);
						PTFarmHolder spawn = new PTFarmHolder(spawnSet);
						_ptfarm.computeIfAbsent(id, k -> new ArrayList<>()).add(spawn);
					});
				});
			});
		});
	}
	
	public List<PTFarmHolder> getSpawns(String eventId)
	{
		return _ptfarm.getOrDefault(eventId, new ArrayList<>());
	}
	
	public PartyFarmConfig getConfig()
	{
		return _config;
	}
	
	public static PartyFarmData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PartyFarmData _instance = new PartyFarmData();
	}
	
	private static String getChildText(Node node, String tag)
	{
		Node child = getChild(node, tag);
		return (child != null) ? child.getTextContent().trim() : "";
	}
	
	private static Node getChild(Node node, String tag)
	{
		for (int i = 0; i < node.getChildNodes().getLength(); i++)
		{
			Node child = node.getChildNodes().item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && tag.equals(child.getNodeName()))
				return child;
		}
		return null;
	}
}

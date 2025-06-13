package com.dream.game.datatables.xml;

import com.dream.data.xml.IXmlReader;
import com.dream.data.xml.StatSet;
import com.dream.game.model.holders.GreetingHolder;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

public class GreetingData implements IXmlReader
{
	private final Map<Integer, GreetingHolder> _greetings = new HashMap<>();
	
	public GreetingData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/custom/mods/greeting.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _greetings.size() + " greetings.");
		
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> {
			forEach(listNode, "greeting", greetNode -> {
				StatSet set = new StatSet(parseAttributes(greetNode));
				GreetingHolder holder = new GreetingHolder(set);
				_greetings.put(holder.getCastleId(), holder);
			});
		});
	}
	
	public GreetingHolder getGreeting(int castleId)
	{
		return _greetings.get(castleId);
	}
	
	public static GreetingData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GreetingData _instance = new GreetingData();
	}
}

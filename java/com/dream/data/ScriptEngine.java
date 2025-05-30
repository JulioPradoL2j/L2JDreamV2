package com.dream.data;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ScriptEngine
{
	private static Map<String, ParserFactory> parserFactories = new HashMap<>();

	protected static Parser createParser(String name) throws ParserNotCreatedException
	{
		ParserFactory s = parserFactories.get(name);
		if (s == null)
		{
			try
			{
				Class.forName("com.dream.game.script." + name);

				s = parserFactories.get(name);
				if (s == null)
					throw new ParserNotCreatedException();
			}
			catch (ClassNotFoundException e)
			{
				throw new ParserNotCreatedException();
			}
		}
		return s.create();
	}

	public static Map<String, ParserFactory> getParserFactories()
	{
		return parserFactories;
	}

	public static void setParserFactories(Hashtable<String, ParserFactory> parserFactories)
	{
		ScriptEngine.parserFactories = parserFactories;
	}
}
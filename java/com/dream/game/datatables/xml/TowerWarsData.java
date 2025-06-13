package com.dream.game.datatables.xml;

import com.dream.data.xml.IXmlReader;
import com.dream.data.xml.StatSet;
import com.dream.game.towerwars.holder.TowerWarsCrystals;
import com.dream.game.towerwars.holder.TowerWarsPlayers;
import com.dream.game.towerwars.holder.TowerWarsPlayersSpot;
import com.dream.game.towerwars.holder.TowerWarsRoute;
import com.dream.game.towerwars.holder.TowerWarsSettings;
import com.dream.game.towerwars.holder.TowerWarsStatsHolder;
import com.dream.game.towerwars.holder.TowerWarsTower;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TowerWarsData implements IXmlReader
{
	private final List<TowerWarsRoute> _routes = new ArrayList<>();
	private final List<TowerWarsCrystals> _crystals = new ArrayList<>();
	private final List<TowerWarsPlayers> _players = new ArrayList<>();
	private TowerWarsSettings _config;
	private TowerWarsStatsHolder _stats = new TowerWarsStatsHolder();
	
	public TowerWarsData()
	{
		load();
	}
	
	public void reload()
	{
		_config = null;
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/custom/events/TowerWarsData.xml");
		LOGGER.info("Loaded Tower Wars configuration.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", eventsNode -> {
			forEach(eventsNode, "event", eventNode -> {
				
				final StatSet set = new StatSet();
				forEach(eventNode, "set", setNode -> set.putAll(parseAttributes(setNode)));
				
				if (set.containsKey("popup"))
				{
					_stats.setPopup(set.getBool("popup"));
				}
				
				if (set.containsKey("towertimerespawn"))
				{
					_stats.setRespawnTowerTime(set.getInteger("towertimerespawn"));
				}
				
				if (set.containsKey("inhibitorrespawntime"))
				{
					_stats.setRespawnInhibitorTime(set.getInteger("inhibitorrespawntime"));
				}
				
				boolean enabled = Boolean.parseBoolean(getChildText(eventNode, "enabled"));
				int duration = Integer.parseInt(getChildText(eventNode, "duration"));
				int preparation = Integer.parseInt(getChildText(eventNode, "preparation"));
				
				String[] dayTokens = getChildText(eventNode, "days").split(",");
				List<Integer> days = new ArrayList<>();
				for (String token : dayTokens)
					days.add(Integer.parseInt(token.trim()));
				
				List<String> times = new ArrayList<>();
				forEach(eventNode, "times", timesNode -> {
					forEach(timesNode, "time", timeNode -> times.add(timeNode.getTextContent()));
				});
				
				_config = new TowerWarsSettings(enabled, duration, preparation, days, times);
				
				forEach(eventNode, "routes", routesNode -> {
					forEach(routesNode, "route", routeNode -> {
						StatSet routeSet = parseAttributes(routeNode);
						List<TowerWarsTower> towers = new ArrayList<>();
						
						forEach(routeNode, "tower", towerNode -> {
							StatSet towerSet = parseAttributes(towerNode);
							towers.add(new TowerWarsTower(towerSet));
						});
						
						_routes.add(new TowerWarsRoute(routeSet, towers));
						
					});
				});
				
				///////////////////
				///
				///
				///
				///
				///
				forEach(eventNode, "crystals", routesNode -> {
					forEach(routesNode, "team", routeNode -> {
						StatSet routeSet = parseAttributes(routeNode);
						
						AtomicReference<TowerWarsTower> crystalRef = new AtomicReference<>();
						AtomicReference<TowerWarsTower> inhibitorRef = new AtomicReference<>();
						
						forEach(routeNode, "inhibitor", inhibitorNode -> {
							StatSet inhibitorSet = parseAttributes(inhibitorNode);
							inhibitorSet.set("order", 998); // Ordem fixa
							inhibitorRef.set(new TowerWarsTower(inhibitorSet));
						});
						
						forEach(routeNode, "crystal", crystalNode -> {
							StatSet crystalSet = parseAttributes(crystalNode);
							crystalSet.set("order", 999); // Ordem fixa
							crystalRef.set(new TowerWarsTower(crystalSet));
						});
						
						_crystals.add(new TowerWarsCrystals(routeSet, inhibitorRef.get(), crystalRef.get()));
						
					});
				});
				
				///////////////////
				///
				///
				///
				///
				///
				forEach(eventNode, "players", routesNode -> {
					forEach(routesNode, "team", routeNode -> {
						StatSet routeSet = parseAttributes(routeNode);
						
						AtomicReference<TowerWarsPlayersSpot> playersRef = new AtomicReference<>();
						
						forEach(routeNode, "spot", playersNode -> {
							StatSet playersSet = parseAttributes(playersNode);
							playersRef.set(new TowerWarsPlayersSpot(playersSet));
						});
						
						_players.add(new TowerWarsPlayers(routeSet, playersRef.get()));
						
					});
				});
			});
		});
	}
	
	public TowerWarsSettings getConfig()
	{
		return _config;
	}
	
	public List<TowerWarsRoute> getRoutes()
	{
		return _routes;
	}
	
	public List<TowerWarsCrystals> getCrystals()
	{
		return _crystals;
	}
	
	public List<TowerWarsPlayers> getPlayers()
	{
		return _players;
	}
	
	public TowerWarsStatsHolder getStats()
	{
		return _stats;
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
	
	public static TowerWarsData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final TowerWarsData INSTANCE = new TowerWarsData();
	}
}

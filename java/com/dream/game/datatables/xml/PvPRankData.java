package com.dream.game.datatables.xml;

import com.dream.data.xml.IXmlReader;
import com.dream.data.xml.StatSet;
import com.dream.game.model.pvpsystem.holders.BonusRange;
import com.dream.game.model.pvpsystem.holders.DecayRule;
import com.dream.game.model.pvpsystem.holders.DecaySettings;
import com.dream.game.model.pvpsystem.holders.PvPRank;
import com.dream.game.model.pvpsystem.holders.PvPRankSettings;
import com.dream.game.model.pvpsystem.holders.PvPTier;
import com.dream.game.model.pvpsystem.holders.PvpEventCheckKills;
import com.dream.game.model.pvpsystem.holders.PvpPointsSettings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PvPRankData implements IXmlReader
{
	private final Map<String, List<PvpEventCheckKills>> _chekers = new HashMap<>();
	private final List<PvPRank> _ranks = new ArrayList<>();
	private PvpPointsSettings _pointsSettings = new PvpPointsSettings();
	private  DecaySettings _decaySettings;
	
	private PvPRankSettings _config;
	
	public PvPRankData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/pvprank.xml");
		LOGGER.info("Loaded Custom PvP Config.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "rankings", eventsNode -> {
			forEach(eventsNode, "event", eventNode -> {
				StatSet set = parseAttributes(eventNode);
				String id = set.getString("name", "pvp_ranking_system");
				
				boolean enabled = Boolean.parseBoolean(getChildText(eventNode, "enabled"));
				
				_config = new PvPRankSettings(enabled);
				
				forEach(eventNode, "protections", protectionNode -> {
					forEach(protectionNode, "protection", protectioNode -> {
						StatSet protecSet = parseAttributes(protectioNode);
						PvpEventCheckKills check = new PvpEventCheckKills(protecSet);
						_chekers.computeIfAbsent(id, k -> new ArrayList<>()).add(check);
					});
				});
				
				forEach(eventNode, "points", pointsNode -> {
					forEach(pointsNode, "mode", modeNode -> {
						StatSet modeSet = parseAttributes(modeNode);
						String type = modeSet.getString("type");
						int base = modeSet.getInteger("basePoints");
						_pointsSettings.addBasePoint(type, base);
					});
					
					forEach(pointsNode, "bonus", bonusNode -> {
						forEach(bonusNode, "range", rangeNode -> {
							StatSet rangeSet = parseAttributes(rangeNode);
							int min = rangeSet.getInteger("min");
							int max = rangeSet.getInteger("max");
							double multiplier = rangeSet.getDouble("multiplier");
							_pointsSettings.addBonusRange(new BonusRange(min, max, multiplier));
						});
					});
				});
				
				forEach(eventNode, "decay", decayNode -> {
					_decaySettings = new DecaySettings(); // Suponha que esta seja uma variável de classe
					
					forEach(decayNode, "rule", ruleNode -> {
						StatSet ruleSet = parseAttributes(ruleNode);
						int min = ruleSet.getInteger("minPoints");
						int max = ruleSet.getInteger("maxPoints");
						double percent = ruleSet.getDouble("decayPercent");
						
						_decaySettings.addRule(new DecayRule(min, max, percent));
					});
					
					String intervalText = getChildText(decayNode, "intervalHours");
					if (intervalText != null)
					{
						_decaySettings.setIntervalHours(Integer.parseInt(intervalText));
					}
				});
				
				forEach(eventNode, "rank", rankNode -> {
					StatSet rankSet = parseAttributes(rankNode);
					List<PvPTier> tiers = new ArrayList<>();
					
					forEach(rankNode, "tier", tierNode -> {
						StatSet tierSet = parseAttributes(tierNode);
						tiers.add(new PvPTier(tierSet));
					});
					
					_ranks.add(new PvPRank(rankSet, tiers));
				});
				
			});
			
		});
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
	
	public PvPRankSettings getConfig()
	{
		return _config;
	}
	
	public List<PvpEventCheckKills> getEventId(String eventId)
	{
		return _chekers.getOrDefault(eventId, new ArrayList<>());
	}
	
	public String getRankTierByPoints(int points)
	{
		for (PvPRank rank : _ranks)
		{
			int accumulated = 0;
			for (PvPTier tier : rank.getTiers())
			{
				accumulated += tier.getPointsRequired();
				if (points < accumulated)
				{
					return rank.getName() + " Lv." + tier.getLevel();
				}
			}
			points -= accumulated; // avança pro próximo rank
		}
		return "Max Rank";
	}
	
	public PvPTier getTierByPoints(int points)
	{
		for (PvPRank rank : _ranks)
		{
			int accumulated = 0;
			for (PvPTier tier : rank.getTiers())
			{
				accumulated += tier.getPointsRequired();
				if (points < accumulated)
					return tier;
			}
			points -= accumulated;
		}
		return null;
	}
	
	public PvpPointsSettings getPointsSettings()
	{
		return _pointsSettings;
	}
	


	public DecaySettings getDecaySettings()
	{
		return _decaySettings;
	}
	
	public static PvPRankData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final PvPRankData INSTANCE = new PvPRankData();
	}
}

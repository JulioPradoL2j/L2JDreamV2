package com.dream.game.datatables.xml;

import com.dream.data.xml.IXmlReader;
import com.dream.data.xml.StatSet;
import com.dream.game.model.holders.PrizeHolder;
import com.dream.game.model.holders.ResetHolder;
import com.dream.game.model.holders.ResetPrize;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;

public class ResetData implements IXmlReader
{
	private final List<ResetHolder> _resets = new ArrayList<>();
	
	public ResetData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_resets.clear();
		parseFile("./data/xml/resetData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _resets.size() + " reset configs.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> {
			
			forEach(listNode, "reset", resetNode -> {
				StatSet baseSet = new StatSet(parseAttributes(resetNode));
				ResetHolder holder = new ResetHolder(baseSet);
				
				final StatSet set = new StatSet();
				forEach(resetNode, "set", setNode -> set.putAll(parseAttributes(setNode)));
				
				if (set.containsKey("rankingDisplayLimit"))
				{
					holder.setRankingDisplayLimit(set.getInteger("rankingDisplayLimit"));
				}

				if (set.containsKey("dailyPoints"))
				{
					holder.setDailyPoints(set.getInteger("dailyPoints"));
				}

				if (set.containsKey("monthlyPoints"))
				{
					holder.setMonthlyPoints(set.getInteger("monthlyPoints"));
				}
				
				if (set.containsKey("removeResetSkills"))
				{
					holder.setRemoveResetSkills(set.getBool("removeResetSkills"));
				}
				
				if (set.containsKey("logger"))
				{
					holder.setDebug(set.getBool("logger"));
				}

				
				// <requiredItems>
				forEach(resetNode, "requiredItems", requiredItemsNode -> {
					forEach(requiredItemsNode, "item", itemNode -> {
						StatSet itemSet = new StatSet(parseAttributes(itemNode));
						if (itemSet.containsKey("required"))
						{
							holder.getRequiredItems().addAll(itemSet.getIntIntHolderList("required"));
						}
					});
				});
				
				// <rewardItems>
				forEach(resetNode, "rewardItems", rewardItemsNode -> {
					forEach(rewardItemsNode, "item", itemNode -> {
						StatSet rewardSet = new StatSet(parseAttributes(itemNode));
						if (rewardSet.containsKey("reward"))
						{
							holder.getRewardItems().addAll(rewardSet.getIntIntHolderList("reward"));
						}
						if (rewardSet.containsKey("skill"))
						{
							holder.getRewardSkills().addAll(rewardSet.getIntIntHolderList("skill"));
						}
					});
				});
				
				// <prizes>
				forEach(resetNode, "prizes", prizesNode -> {
					StatSet prizeSet = new StatSet(parseAttributes(prizesNode));
					ResetPrize resetPrize = new ResetPrize(prizeSet);
					
					forEach(prizesNode, "prize", prizeNode -> {
						StatSet pSet = new StatSet(parseAttributes(prizeNode));
						resetPrize.addPrize(new PrizeHolder(pSet));
					});
					
					holder.addPrize(resetPrize);
				});
				
				_resets.add(holder);
			});
		});
	}
	
	public List<ResetHolder> getResets()
	{
		return _resets != null ? _resets : Collections.emptyList();
	}
	
	public static ResetData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		private static final ResetData _instance = new ResetData();
	}
}
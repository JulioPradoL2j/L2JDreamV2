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
package com.dream.game.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.dream.Config;
import com.dream.data.xml.XMLDocument;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.templates.item.L2Item;

public class L2Manor extends XMLDocument
{
	private class SeedData
	{
		public int _id;
		private final int _level;
		private final int _crop;
		private final int _mature;
		private int _type1;
		private int _type2;
		private int _manorId;
		private boolean _isAlternative;
		private int _limitSeeds;
		private int _limitCrops;

		public SeedData(int level, int crop, int mature)
		{
			_level = level;
			_crop = crop;
			_mature = mature;
		}

		public int getCrop()
		{
			return _crop;
		}

		public int getCropLimit()
		{
			return (int) (_limitCrops * Config.RATE_DROP_MANOR);
		}

		public int getId()
		{
			return _id;
		}

		public int getLevel()
		{
			return _level;
		}

		public int getManorId()
		{
			return _manorId;
		}

		public int getMature()
		{
			return _mature;
		}

		public int getReward(int type)
		{
			return type == 1 ? _type1 : _type2;
		}

		public int getSeedLimit()
		{
			return (int) (_limitSeeds * Config.RATE_DROP_MANOR);
		}

		public boolean isAlternative()
		{
			return _isAlternative;
		}

		public void setData(int id, int t1, int t2, int manorId, boolean isAlt, int lim1, int lim2)
		{
			_id = id;
			_type1 = t1;
			_type2 = t2;
			_manorId = manorId;
			_isAlternative = isAlt;
			_limitSeeds = lim1;
			_limitCrops = lim2;
		}

	}

	private final static Logger _log = Logger.getLogger(L2Manor.class.getName());

	private static L2Manor _instance;

	private static Map<Integer, SeedData> _seeds;

	public static L2Manor getInstance()
	{
		if (_instance == null)
		{
			_instance = new L2Manor();
		}
		return _instance;
	}

	private int manorId = 0;

	public L2Manor()
	{
		_seeds = new ConcurrentHashMap<>();
		parseData();
	}

	public List<Integer> getAllCrops()
	{
		List<Integer> crops = new ArrayList<>();
		for (SeedData seed : _seeds.values())
			if (!crops.contains(seed.getCrop()) && seed.getCrop() != 0 && !crops.contains(seed.getCrop()))
			{
				crops.add(seed.getCrop());
			}
		return crops;
	}

	public int getCastleIdForSeed(int seedId)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
			return seed.getManorId();
		return 0;
	}

	public int getCropBasicPrice(int cropId)
	{
		L2Item cropItem = ItemTable.getInstance().getTemplate(cropId);
		if (cropItem != null)
			return cropItem.getReferencePrice();

		return 0;
	}

	public int getCropPuchaseLimit(int cropId)
	{
		for (SeedData seed : _seeds.values())
			if (seed.getCrop() == cropId)
				return seed.getCropLimit();
		return 0;
	}

	public List<Integer> getCropsForCastle(int castleId)
	{
		List<Integer> crops = new ArrayList<>();
		for (SeedData seed : _seeds.values())
			if (seed.getManorId() == castleId && !crops.contains(seed.getCrop()))
			{
				crops.add(seed.getCrop());
			}
		return crops;
	}

	public int getCropType(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if (seed != null)
			return seed.getCrop();
		return -1;
	}

	public int getMatureCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
			if (seed.getCrop() == cropId)
				return seed.getMature();
		return 0;
	}

	public synchronized int getRewardItem(int cropId, int type)
	{
		for (SeedData seed : _seeds.values())
			if (seed.getCrop() == cropId)
				return seed.getReward(type);
		return -1;
	}

	public synchronized int getRewardItemBySeed(int seedId, int type)
	{
		SeedData seed = _seeds.get(seedId);
		if (seed != null)
			return seed.getReward(type);
		return 0;
	}

	public int getSeedBasicPrice(int seedId)
	{
		L2Item seedItem = ItemTable.getInstance().getTemplate(seedId);
		if (seedItem != null)
			return seedItem.getReferencePrice();

		return 0;
	}

	public int getSeedBasicPriceByCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
			if (seed.getCrop() == cropId)
				return getSeedBasicPrice(seed.getId());
		return 0;
	}

	public int getSeedBuyPrice(int seedId)
	{
		int buyPrice = getSeedBasicPrice(seedId) / 10;
		return buyPrice > 0 ? buyPrice : 1;
	}

	public int getSeedLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if (seed != null)
			return seed.getLevel();
		return -1;
	}

	public int getSeedLevelByCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
			if (seed.getCrop() == cropId)
				return seed.getLevel();
		return 0;
	}

	public int getSeedMaxLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if (seed != null)
			return seed.getLevel() + 5;
		return -1;
	}

	public int getSeedMinLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if (seed != null)
			return seed.getLevel() - 5;
		return -1;
	}

	public int getSeedSaleLimit(int seedId)
	{
		SeedData seed = _seeds.get(seedId);
		if (seed != null)
			return seed.getSeedLimit();
		return 0;
	}

	public List<Integer> getSeedsForCastle(int castleId)
	{
		List<Integer> seedsID = new ArrayList<>();
		for (SeedData seed : _seeds.values())
			if (seed.getManorId() == castleId && !seedsID.contains(seed.getId()))
			{
				seedsID.add(seed.getId());
			}
		return seedsID;
	}

	public boolean isAlternative(int seedId)
	{
		for (SeedData seed : _seeds.values())
			if (seed.getId() == seedId)
				return seed.isAlternative();
		return false;
	}

	private void parseData()
	{
		try
		{
			load(new File(Config.DATAPACK_ROOT, "data/xml/world/seeds.xml"));
			_log.info("Manor Data: Loaded " + _seeds.size() + " Seed(s).");
		}
		catch (Exception e)
		{
			_log.info("ManorManager: Error while loading seeds: " + e.getMessage());
		}
	}

	@Override
	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if (n.getNodeName().equals("list"))
			{
				for (Node manor = n.getFirstChild(); manor != null; manor = manor.getNextSibling())
					if (manor.getNodeName().equals("manor"))
					{
						manorId = Integer.parseInt(manor.getAttributes().getNamedItem("id").getNodeValue());
						for (Node seed = manor.getFirstChild(); seed != null; seed = seed.getNextSibling())
							if (seed.getNodeName().equals("seed"))
							{
								SeedData seedInstance = parseNode(seed);
								if (seedInstance != null)
								{
									_seeds.put(seedInstance._id, seedInstance);
								}
							}
					}
			}
	}

	private SeedData parseNode(Node node)
	{
		int seedId = Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
		int level = Integer.parseInt(node.getAttributes().getNamedItem("level").getNodeValue());
		int cropId = Integer.parseInt(node.getAttributes().getNamedItem("cropId").getNodeValue());
		int matureId = Integer.parseInt(node.getAttributes().getNamedItem("matureId").getNodeValue());
		int type1R = Integer.parseInt(node.getAttributes().getNamedItem("reward1").getNodeValue());
		int type2R = Integer.parseInt(node.getAttributes().getNamedItem("reward2").getNodeValue());
		int manorId = this.manorId;
		if (ItemTable.getInstance().getTemplate(seedId) == null || ItemTable.getInstance().getTemplate(cropId) == null)
		{
			_log.warn("No item for seed " + seedId);
			return null;
		}
		boolean isAlt = Boolean.parseBoolean(node.getAttributes().getNamedItem("isAlt").getNodeValue());
		int limitSeeds = Integer.parseInt(node.getAttributes().getNamedItem("limitSeeds").getNodeValue());
		int limitCrops = Integer.parseInt(node.getAttributes().getNamedItem("limitCrops").getNodeValue());
		SeedData seed = new SeedData(level, cropId, matureId);
		seed.setData(seedId, type1R, type2R, manorId, isAlt, limitSeeds, limitCrops);

		return seed;
	}
}
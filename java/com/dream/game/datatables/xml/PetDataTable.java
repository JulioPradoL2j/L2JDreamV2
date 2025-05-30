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

import com.dream.Config;
import com.dream.game.model.L2PetData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PetDataTable
{
	private static enum L2Pet
	{
		WOLF(PET_WOLF_ID, 2375, new int[]
		{
			2515
		}, false),

		HATCHLING_WIND(HATCHLING_WIND_ID, 3500, new int[]
		{
			4038
		}, false),
		HATCHLING_STAR(HATCHLING_STAR_ID, 3501, new int[]
		{
			4038
		}, false),
		HATCHLING_TWILIGHT(HATCHLING_TWILIGHT_ID, 3502, new int[]
		{
			4038
		}, false),

		STRIDER_WIND(STRIDER_WIND_ID, 4422, new int[]
		{
			5168,
			5169
		}, true),
		STRIDER_STAR(STRIDER_STAR_ID, 4423, new int[]
		{
			5168,
			5169
		}, true),
		STRIDER_TWILIGHT(STRIDER_TWILIGHT_ID, 4424, new int[]
		{
			5168,
			5169
		}, true),

		WYVERN(WYVERN_ID, 5249, new int[]
		{
			6316
		}, true),

		GREAT_WOLF(GREAT_WOLF_ID, 10163, new int[]
		{
			9668
		}, true),
		BABY_BUFFALO(BABY_BUFFALO_ID, 6648, new int[]
		{
			7582
		}, false),
		BABY_KOOKABURRA(BABY_KOOKABURRA_ID, 6649, new int[]
		{
			7582
		}, false),
		BABY_COUGAR(BABY_COUGAR_ID, 6650, new int[]
		{
			7582
		}, false),

		SIN_EATER(SIN_EATER_ID, 4425, new int[]
		{
			2515
		}, false);

		private final int _npcId;
		private final int _controlItemId;
		private final int[] _foodIds;
		private final boolean _isMountabe;

		private L2Pet(int npcId, int controlItemId, int[] foodIds, boolean isMountabe)
		{
			_npcId = npcId;
			_controlItemId = controlItemId;
			_foodIds = foodIds;
			_isMountabe = isMountabe;
		}

		public int getControlItemId()
		{
			return _controlItemId;
		}

		public int[] getFoodIds()
		{
			return _foodIds;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public boolean isMountable()
		{
			return _isMountabe;
		}
	}

	private final static Logger _log = Logger.getLogger(PetDataTable.class.getName());

	private static PetDataTable _instance;

	public final static int PET_WOLF_ID = 12077;

	public final static int HATCHLING_WIND_ID = 12311;

	public final static int HATCHLING_STAR_ID = 12312;

	public final static int HATCHLING_TWILIGHT_ID = 12313;

	public final static int STRIDER_WIND_ID = 12526;

	public final static int STRIDER_STAR_ID = 12527;

	public final static int STRIDER_TWILIGHT_ID = 12528;

	public final static int RED_STRIDER_WIND_ID = 16038;

	public final static int RED_STRIDER_STAR_ID = 16039;

	public final static int RED_STRIDER_TWILIGHT_ID = 16040;

	public final static int WYVERN_ID = 12621;

	public final static int BABY_BUFFALO_ID = 12780;

	public final static int BABY_KOOKABURRA_ID = 12781;

	public final static int BABY_COUGAR_ID = 12782;

	public final static int IMPROVED_BABY_BUFFALO_ID = 16034;
	public final static int IMPROVED_BABY_KOOKABURRA_ID = 16035;

	public final static int IMPROVED_BABY_COUGAR_ID = 16036;

	public final static int SIN_EATER_ID = 12564;
	public final static int BLACK_WOLF_ID = 16030;
	public final static int WGREAT_WOLF_ID = 16037;
	public final static int GREAT_WOLF_ID = 16025;
	public final static int FENRIR_WOLF_ID = 16041;
	public final static int WFENRIR_WOLF_ID = 16042;

	public final static int PURPLE_HORSE_ID = 13130;

	private static HashMap<Integer, HashMap<Integer, L2PetData>> petTable;

	public final static int[] EMPTY_INT =
	{
		0
	};

	public static int[] getFoodItemId(int npcId)
	{
		for (L2Pet pet : L2Pet.values())
			if (pet.getNpcId() == npcId)
				return pet.getFoodIds();

		return EMPTY_INT;
	}

	public static PetDataTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new PetDataTable();
		}

		return _instance;
	}

	public static int getItemIdByPetId(int npcId)
	{
		for (L2Pet pet : L2Pet.values())
			if (pet.getNpcId() == npcId)
				return pet.getControlItemId();

		return 0;
	}

	public static int getPetIdByItemId(int itemId)
	{
		for (L2Pet pet : L2Pet.values())
			if (pet.getControlItemId() == itemId)
				return pet.getNpcId();

		return 0;
	}

	public static boolean isBaby(int npcId)
	{
		return npcId > 12779 && npcId < 12783;
	}

	public static boolean isHatchling(int npcId)
	{
		return npcId > 12310 && npcId < 12314;
	}

	public static boolean isImprovedBaby(int npcId)
	{
		return IMPROVED_BABY_BUFFALO_ID == npcId || IMPROVED_BABY_KOOKABURRA_ID == npcId || IMPROVED_BABY_COUGAR_ID == npcId;
	}

	public static boolean isMountable(int npcId)
	{
		for (L2Pet pet : L2Pet.values())
			if (pet.getNpcId() == npcId)
				return pet.isMountable();

		return false;
	}

	public static boolean isPet(int npcId)
	{
		for (L2Pet pet : L2Pet.values())
			if (pet.getNpcId() == npcId)
				return true;

		return false;
	}

	public static boolean isPetFood(int itemId)
	{
		switch (itemId)
		{
			case 2515:
			case 4038:
			case 5168:
			case 5169:
			case 6316:
			case 7582:
				return true;
			default:
				return false;
		}
	}

	public static boolean isPetFood(int npcId, int itemId)
	{
		for (L2Pet pet : L2Pet.values())
			if (pet.getNpcId() == npcId)
			{
				for (int id : pet.getFoodIds())
					if (id == itemId)
						return true;
			}

		return false;
	}

	public static boolean isPetItem(int itemId)
	{
		for (L2Pet pet : L2Pet.values())
			if (pet.getControlItemId() == itemId)
				return true;

		return false;
	}

	public static boolean isSinEater(int npcId)
	{
		return npcId == 12564;
	}

	public static boolean isStrider(int npcId)
	{
		return npcId > 12525 && npcId < 12529;
	}

	public static boolean isWolf(int npcId)
	{
		return npcId == 12077;
	}

	public static boolean isWyvern(int npcId)
	{
		return npcId == 12621;
	}

	private PetDataTable()
	{
		petTable = new HashMap<>();
	}

	public void addPetData(L2PetData petData)
	{
		Map<Integer, L2PetData> h = petTable.get(petData.getPetID());

		if (h == null)
		{
			HashMap<Integer, L2PetData> statTable = new HashMap<>();
			statTable.put(petData.getPetLevel(), petData);
			petTable.put(petData.getPetID(), statTable);
			return;
		}

		h.put(petData.getPetLevel(), petData);
	}

	public void addPetData(L2PetData[] petLevelsList)
	{
		for (L2PetData element : petLevelsList)
		{
			addPetData(element);
		}
	}

	public L2PetData getPetData(int petID, int petLevel)
	{
		try
		{
			return petTable.get(petID).get(petLevel);
		}
		catch (NullPointerException npe)
		{
			return null;
		}
	}

	public void loadPetsData()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/pet_stats.xml");
		if (!f.exists())
		{
			_log.warn("pet_stats.xml could not be loaded: file not found");
			return;
		}
		int k = 0;
		try
		{
			InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if (d.getNodeName().equalsIgnoreCase("pet"))
						{
							int petId, petLevel;
							k++;

							petId = Integer.valueOf(d.getAttributes().getNamedItem("typeID").getNodeValue());
							petLevel = Integer.valueOf(d.getAttributes().getNamedItem("level").getNodeValue());

							// build the petdata for this level
							L2PetData petData = new L2PetData();

							petData.setPetID(petId);
							petData.setPetLevel(petLevel);
							petData.setPetMaxExp(Integer.valueOf(d.getAttributes().getNamedItem("expMax").getNodeValue()));
							petData.setPetMaxHP(Integer.valueOf(d.getAttributes().getNamedItem("hpMax").getNodeValue()));
							petData.setPetMaxMP(Integer.valueOf(d.getAttributes().getNamedItem("mpMax").getNodeValue()));
							petData.setPetPAtk(Integer.valueOf(d.getAttributes().getNamedItem("patk").getNodeValue()));
							petData.setPetPDef(Integer.valueOf(d.getAttributes().getNamedItem("pdef").getNodeValue()));
							petData.setPetMAtk(Integer.valueOf(d.getAttributes().getNamedItem("matk").getNodeValue()));
							petData.setPetMDef(Integer.valueOf(d.getAttributes().getNamedItem("mdef").getNodeValue()));
							petData.setPetAccuracy(Integer.valueOf(d.getAttributes().getNamedItem("acc").getNodeValue()));
							petData.setPetEvasion(Integer.valueOf(d.getAttributes().getNamedItem("evasion").getNodeValue()));
							petData.setPetCritical(Integer.valueOf(d.getAttributes().getNamedItem("crit").getNodeValue()));
							petData.setPetSpeed(Integer.valueOf(d.getAttributes().getNamedItem("speed").getNodeValue()));
							petData.setPetAtkSpeed(Integer.valueOf(d.getAttributes().getNamedItem("atk_speed").getNodeValue()));
							petData.setPetCastSpeed(Integer.valueOf(d.getAttributes().getNamedItem("cast_speed").getNodeValue()));
							petData.setPetMaxFeed(Integer.valueOf(d.getAttributes().getNamedItem("feedMax").getNodeValue()));
							petData.setPetFeedNormal(Integer.valueOf(d.getAttributes().getNamedItem("feednormal").getNodeValue()));
							petData.setPetFeedBattle(Integer.valueOf(d.getAttributes().getNamedItem("feedbattle").getNodeValue()));
							petData.setPetMaxLoad(Integer.valueOf(d.getAttributes().getNamedItem("loadMax").getNodeValue()));
							petData.setPetRegenHP(Integer.valueOf(d.getAttributes().getNamedItem("hpregen").getNodeValue()));
							petData.setPetRegenMP(Integer.valueOf(d.getAttributes().getNamedItem("mpregen").getNodeValue()));
							petData.setPetRegenMP(Integer.valueOf(d.getAttributes().getNamedItem("mpregen").getNodeValue()));
							petData.setOwnerExpTaken(Float.valueOf(d.getAttributes().getNamedItem("owner_exp_taken").getNodeValue()));

							// if its the first data for this petid, we initialize its level HashMap
							if (!petTable.containsKey(petId))
							{
								petTable.put(petId, new HashMap<>());
							}

							petTable.get(petId).put(petLevel, petData);
							petData = null;
						}
				}
		}
		catch (SAXException e)
		{
			_log.error("Error while creating table", e);
		}
		catch (IOException e)
		{
			_log.error("Error while creating table", e);
		}
		catch (ParserConfigurationException e)
		{
			_log.error("Error while creating table", e);
		}

		_log.info("Pet Data: Loaded " + petTable.size() + " pets with " + k + " stats.");
	}
}
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dream.Config;
import com.dream.game.model.L2EnchantSkillLearn;
import com.dream.game.model.L2PledgeSkillLearn;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2SkillLearn;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.ClassId;
import com.dream.util.L2Collections;
import com.dream.util.LinkedBunch;

public class SkillTreeTable
{
	public static final int NORMAL_ENCHANT_COST_MULTIPLIER = 1;

	public static final int NORMAL_ENCHANT_BOOK = 6622;

	private final static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static SkillTreeTable _instance;

	public static SkillTreeTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillTreeTable();
		}

		return _instance;
	}

	private Map<ClassId, Map<Integer, L2SkillLearn>> _skillTrees;
	private List<L2SkillLearn> _fishingSkillTrees;
	private List<L2SkillLearn> _expandDwarfCraftSkillTrees;
	private List<L2PledgeSkillLearn> _pledgeSkillTrees;

	private List<L2EnchantSkillLearn> _enchantSkillTrees;

	private SkillTreeTable()
	{
		int classId = 0;
		int count = 0;

		Map<Integer, L2SkillLearn> map;
		int parentClassId;
		L2SkillLearn skillLearn;

		DocumentBuilderFactory factory0 = DocumentBuilderFactory.newInstance();
		factory0.setValidating(false);
		factory0.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT + "/data/xml/player/skills/skill_tree.xml");
		if (!file.exists())
		{
			_log.warn("skill_tree.xml could not be loaded: file not found");
			return;
		}
		try
		{
			InputSource in4 = new InputSource(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			in4.setEncoding("UTF-8");
			Document doc = factory0.newDocumentBuilder().parse(in4);
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if (d.getNodeName().equalsIgnoreCase("skill"))
						{
							map = new Hashtable<>(5, 0.9f);
							classId = Integer.valueOf(d.getAttributes().getNamedItem("class_id").getNodeValue());
							parentClassId = Integer.valueOf(d.getAttributes().getNamedItem("parent_id").getNodeValue());
							if (parentClassId != -1)
							{
								Map<Integer, L2SkillLearn> parentMap = getSkillTrees().get(ClassId.values()[parentClassId]);
								map.putAll(parentMap);
							}
							int prevSkillId = -1;

							for (Node t = d.getFirstChild(); t != null; t = t.getNextSibling())
								if (t.getNodeName().equalsIgnoreCase("data"))
								{
									int id = Integer.valueOf(t.getAttributes().getNamedItem("skill_id").getNodeValue());
									int lvl = Integer.valueOf(t.getAttributes().getNamedItem("level").getNodeValue());
									String name = String.valueOf(t.getAttributes().getNamedItem("name").getNodeValue());
									int cost = Integer.valueOf(t.getAttributes().getNamedItem("sp").getNodeValue());
									int minLvl = Integer.valueOf(t.getAttributes().getNamedItem("min_level").getNodeValue());

									if (prevSkillId != id)
									{
										prevSkillId = id;
									}
									skillLearn = new L2SkillLearn(id, lvl, minLvl, name, cost, 0, 0);
									map.put(SkillTable.getSkillHashCode(id, lvl), skillLearn);
									count++;
								}
							getSkillTrees().put(ClassId.values()[classId], map);
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

		_log.info("Skill Tree: Loaded " + count + " skills.");

		int count2 = 0;
		int count3 = 0;

		_fishingSkillTrees = new ArrayList<>();
		_expandDwarfCraftSkillTrees = new ArrayList<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/skills/fishing_skill_tree.xml");
		if (!f.exists())
		{
			_log.warn("fishing_skill_tree.xml could not be loaded: file not found");
			return;
		}
		try
		{
			InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			in.setEncoding("UTF-8");
			Document doc = factory.newDocumentBuilder().parse(in);
			int prevSkillId = -1;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if (d.getNodeName().equalsIgnoreCase("SkillTree"))
						{
							int id = Integer.valueOf(d.getAttributes().getNamedItem("skill_id").getNodeValue());
							int lvl = Integer.valueOf(d.getAttributes().getNamedItem("level").getNodeValue());
							String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
							int cost = Integer.valueOf(d.getAttributes().getNamedItem("sp").getNodeValue());
							int minLvl = Integer.valueOf(d.getAttributes().getNamedItem("min_level").getNodeValue());
							int costId = Integer.valueOf(d.getAttributes().getNamedItem("costid").getNodeValue());
							int costCount = Integer.valueOf(d.getAttributes().getNamedItem("cost").getNodeValue());
							int isDwarven = Integer.valueOf(d.getAttributes().getNamedItem("isfordwarf").getNodeValue());

							if (prevSkillId != id)
							{
								prevSkillId = id;
							}

							L2SkillLearn skill = new L2SkillLearn(id, lvl, minLvl, name, cost, costId, costCount);

							if (isDwarven == 0)
							{
								_fishingSkillTrees.add(skill);
							}
							else
							{
								_expandDwarfCraftSkillTrees.add(skill);
							}
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

		count2 = _fishingSkillTrees.size();
		count3 = _expandDwarfCraftSkillTrees.size();

		int count4 = 0;
		int count6 = 0;
		_enchantSkillTrees = new ArrayList<>();
		DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
		factory2.setValidating(false);
		factory2.setIgnoringComments(true);
		File f2 = new File(Config.DATAPACK_ROOT + "/data/xml/player/skills/enchant_skill_tree.xml");
		if (!f2.exists())
		{
			_log.warn("enchant_skill_tree.xml could not be loaded: file not found");
			return;
		}
		try
		{
			InputSource in2 = new InputSource(new InputStreamReader(new FileInputStream(f2), "UTF-8"));
			in2.setEncoding("UTF-8");
			Document doc2 = factory2.newDocumentBuilder().parse(in2);
			for (Node n2 = doc2.getFirstChild(); n2 != null; n2 = n2.getNextSibling())
				if (n2.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d2 = n2.getFirstChild(); d2 != null; d2 = d2.getNextSibling())
						if (d2.getNodeName().equalsIgnoreCase("enchant"))
						{
							int minSkillLvl = 0;
							int id = Integer.valueOf(d2.getAttributes().getNamedItem("id").getNodeValue());
							String name = String.valueOf(d2.getAttributes().getNamedItem("name").getNodeValue());
							int baseLvl = Integer.valueOf(d2.getAttributes().getNamedItem("base_lvl").getNodeValue());
							count6++;
							for (Node t2 = d2.getFirstChild(); t2 != null; t2 = t2.getNextSibling())
								if (t2.getNodeName().equalsIgnoreCase("data"))
								{
									int id1 = id;
									String name1 = name;
									int baseLvl1 = baseLvl;
									int lvl = Integer.valueOf(t2.getAttributes().getNamedItem("level").getNodeValue());
									// String type = String.valueOf(t2.getAttributes().getNamedItem("type").getNodeValue());
									int sp = Integer.valueOf(t2.getAttributes().getNamedItem("sp").getNodeValue());
									int exp = Integer.valueOf(t2.getAttributes().getNamedItem("exp").getNodeValue());
									byte rate76 = Byte.valueOf(t2.getAttributes().getNamedItem("rate76").getNodeValue());
									byte rate77 = Byte.valueOf(t2.getAttributes().getNamedItem("rate77").getNodeValue());
									byte rate78 = Byte.valueOf(t2.getAttributes().getNamedItem("rate78").getNodeValue());

									if (lvl == 101 || lvl == 141)
									{
										minSkillLvl = baseLvl1;
									}
									else
									{
										minSkillLvl = lvl - 1;
									}

									L2EnchantSkillLearn skill = new L2EnchantSkillLearn(id1, lvl, minSkillLvl, baseLvl1, name1, sp, exp, rate76, rate77, rate78);
									_enchantSkillTrees.add(skill);
								}
						}
				}
		}
		catch (SAXException e0)
		{
			_log.error("Error while creating table", e0);
		}
		catch (IOException e0)
		{
			_log.error("Error while creating table", e0);
		}
		catch (ParserConfigurationException e0)
		{
			_log.error("Error while creating table", e0);
		}

		count4 = _enchantSkillTrees.size();

		int count5 = 0;
		_pledgeSkillTrees = new ArrayList<>();
		DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
		factory1.setValidating(false);
		factory1.setIgnoringComments(true);
		File f1 = new File(Config.DATAPACK_ROOT + "/data/xml/player/skills/pledge_skill_tree.xml");
		if (!f1.exists())
		{
			_log.warn("pledge_skill_tree.xml could not be loaded: file not found");
			return;
		}
		try
		{
			InputSource in1 = new InputSource(new InputStreamReader(new FileInputStream(f1), "UTF-8"));
			in1.setEncoding("UTF-8");
			Document doc1 = factory1.newDocumentBuilder().parse(in1);
			int prevSkillId = -1;
			for (Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
				if (n1.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
						if (d1.getNodeName().equalsIgnoreCase("PledgeSkill"))
						{
							int id = Integer.valueOf(d1.getAttributes().getNamedItem("skill_id").getNodeValue());
							int lvl = Integer.valueOf(d1.getAttributes().getNamedItem("level").getNodeValue());
							String name = String.valueOf(d1.getAttributes().getNamedItem("name").getNodeValue());
							int baseLvl = Integer.valueOf(d1.getAttributes().getNamedItem("clan_lvl").getNodeValue());
							int sp = Integer.valueOf(d1.getAttributes().getNamedItem("repCost").getNodeValue());
							int itemId = Integer.valueOf(d1.getAttributes().getNamedItem("itemId").getNodeValue());

							if (prevSkillId != id)
							{
								prevSkillId = id;
							}

							L2PledgeSkillLearn skill = new L2PledgeSkillLearn(id, lvl, baseLvl, name, sp, itemId);
							_pledgeSkillTrees.add(skill);
						}
				}
		}
		catch (SAXException e1)
		{
			_log.error("Error while creating table", e1);
		}
		catch (IOException e1)
		{
			_log.error("Error while creating table", e1);
		}
		catch (ParserConfigurationException e1)
		{
			_log.error("Error while creating table", e1);
		}

		count5 = _pledgeSkillTrees.size();

		_log.info("Fishing Skill Tree: Loaded " + count2 + " general skills.");
		_log.info("Fishing Skill Tree: Loaded " + count3 + " dwarven skills.");
		_log.info("Enchant Skill Tree: Loaded " + count6 + " enchant skills.");
		_log.info("Enchant Skill Tree: Loaded " + count4 + " enchant skills.");
		_log.info("Pledge Skill Tree : Loaded " + count5 + " pledge skills.");
	}

	public Collection<L2SkillLearn> getAllowedSkills(ClassId classId)
	{
		return getSkillTrees().get(classId).values();
	}

	public L2EnchantSkillLearn[] getAvailableEnchantSkills(L2PcInstance cha)
	{
		List<L2EnchantSkillLearn> result = new ArrayList<>();
		List<L2EnchantSkillLearn> skills = new ArrayList<>();

		skills.addAll(_enchantSkillTrees);

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2EnchantSkillLearn temp : skills)
			if (76 <= cha.getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if (oldSkills[j].getLevel() == temp.getMinSkillLevel())
						{
							result.add(temp);
						}
					}

			}

		return result.toArray(new L2EnchantSkillLearn[result.size()]);
	}

	public L2PledgeSkillLearn[] getAvailablePledgeSkills(L2PcInstance cha)
	{
		LinkedBunch<L2PledgeSkillLearn> result = new LinkedBunch<>();
		List<L2PledgeSkillLearn> skills = _pledgeSkillTrees;

		if (skills == null)
		{
			_log.warn("No clan skills defined!");
			return new L2PledgeSkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getClan().getAllSkills();

		for (L2PledgeSkillLearn temp : skills)
			if (temp.getBaseLevel() <= cha.getClan().getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							result.add(temp);
						}
					}

				if (!knownSkill && temp.getLevel() == 1)
				{
					result.add(temp);
				}
			}
		return result.moveToArray(new L2PledgeSkillLearn[result.size()]);
	}

	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha)
	{
		LinkedBunch<L2SkillLearn> result = new LinkedBunch<>();

		Iterable<L2SkillLearn> iterable = cha.hasDwarvenCraft() ? L2Collections.concatenatedIterable(_fishingSkillTrees, _expandDwarfCraftSkillTrees) : _fishingSkillTrees;

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : iterable)
			if (temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							result.add(temp); // this is the next level of a skill that we know
						}
					}

				if (!knownSkill && temp.getLevel() == 1)
				{
					result.add(temp); // this is a new skill
				}
			}

		return result.moveToArray(new L2SkillLearn[result.size()]);
	}

	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha, ClassId classId)
	{
		LinkedBunch<L2SkillLearn> result = new LinkedBunch<>();
		Collection<L2SkillLearn> skills = getSkillTrees().get(classId).values();

		if (skills == null)
		{
			_log.warn("Skilltree for class " + classId + " is not defined !");
			return new L2SkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : skills)
			if (temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
					if (oldSkills[j] != null)
						if (oldSkills[j].getId() == temp.getId())
						{
							knownSkill = true;

							if (oldSkills[j].getLevel() == temp.getLevel() - 1)
							{
								result.add(temp); // this is the next level of a skill that we know
							}
						}

				if (!knownSkill)
					if (temp.getLevel() == 1 || Config.ALLOWED_SKILLS_LIST.contains(temp.getId()))
					{
						result.add(temp); // this is a new skill
					}
			}

		return result.moveToArray(new L2SkillLearn[result.size()]);
	}

	public int getEnchantSkillExpCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = Integer.MAX_VALUE;
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

		for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}

			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}

			if (76 > player.getLevel())
			{
				continue;
			}

			skillCost = enchantSkillLearn.getExp();
		}

		return skillCost;
	}

	public byte getEnchantSkillRate(L2PcInstance player, L2Skill skill)
	{
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

		for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}

			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}

			return enchantSkillLearn.getRate(player);
		}

		enchantSkillLearnList = null;

		return 0;
	}

	public int getEnchantSkillSpCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = Integer.MAX_VALUE;
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);

		for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}

			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}

			if (76 > player.getLevel())
			{
				continue;
			}

			skillCost = enchantSkillLearn.getSpCost();
		}
		return skillCost;
	}

	public int getExpertiseLevel(int grade)
	{
		if (grade <= 0)
			return 0;

		Map<Integer, L2SkillLearn> learnMap = getSkillTrees().get(ClassId.paladin);

		int skillHashCode = SkillTable.getSkillHashCode(239, grade);
		if (learnMap.containsKey(skillHashCode))
			return learnMap.get(skillHashCode).getMinLevel();

		_log.fatal("Expertise not found for grade " + grade);
		return 0;
	}

	public int getMinLevelForNewSkill(L2PcInstance cha)
	{
		int minLevel = 0;
		List<L2SkillLearn> skills = new ArrayList<>();

		skills.addAll(_fishingSkillTrees);

		if (cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}

		for (L2SkillLearn s : skills)
			if (s.getMinLevel() > cha.getLevel())
				if (minLevel == 0 || s.getMinLevel() < minLevel)
				{
					minLevel = s.getMinLevel();
				}

		return minLevel;
	}

	public int getMinLevelForNewSkill(L2PcInstance cha, ClassId classId)
	{
		int minLevel = 0;
		Collection<L2SkillLearn> skills = getSkillTrees().get(classId).values();

		if (skills == null)
		{
			_log.warn("Skilltree for class " + classId + " is not defined !");
			return minLevel;
		}

		for (L2SkillLearn temp : skills)
			if (temp.getMinLevel() > cha.getLevel() && temp.getSpCost() != 0)
				if (minLevel == 0 || temp.getMinLevel() < minLevel)
				{
					minLevel = temp.getMinLevel();
				}

		return minLevel;
	}

	public int getMinSkillLevel(int skillId, ClassId classId, int skillLvl)
	{
		Map<Integer, L2SkillLearn> map = getSkillTrees().get(classId);

		int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

		if (map.containsKey(skillHashCode))
			return map.get(skillHashCode).getMinLevel();

		return 0;
	}

	public int getMinSkillLevel(int skillId, int skillLvl)
	{
		int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);

		for (Map<Integer, L2SkillLearn> map : getSkillTrees().values())
			if (map.containsKey(skillHashCode))
				return map.get(skillHashCode).getMinLevel();
		return 0;
	}

	public int getSkillCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		ClassId classId = player.getSkillLearningClassId();
		int skillHashCode = SkillTable.getSkillHashCode(skill);

		if (getSkillTrees().get(classId).containsKey(skillHashCode))
		{
			L2SkillLearn skillLearn = getSkillTrees().get(classId).get(skillHashCode);
			if (skillLearn.getMinLevel() <= player.getLevel())
			{
				skillCost = skillLearn.getSpCost();
				if (!player.getClassId().equalsOrChildOf(classId))
				{
					if (skill.getCrossLearnAdd() < 0)
						return skillCost;

					skillCost += skill.getCrossLearnAdd();
					skillCost *= skill.getCrossLearnMul();
				}

				if (classId.getRace() != player.getRace() && !player.isSubClassActive())
				{
					skillCost *= skill.getCrossLearnRace();
				}

				if (classId.isMage() != player.getClassId().isMage())
				{
					skillCost *= skill.getCrossLearnProf();
				}
			}
		}

		return skillCost;
	}

	public Map<ClassId, Map<Integer, L2SkillLearn>> getSkillTrees()
	{
		if (_skillTrees == null)
		{
			_skillTrees = new HashMap<>();
		}

		return _skillTrees;
	}
}
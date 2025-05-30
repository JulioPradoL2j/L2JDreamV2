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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.dream.Config;
import com.dream.game.model.L2Augmentation;
import com.dream.game.model.L2Skill;
import com.dream.game.skills.Stats;
import com.dream.tools.random.Rnd;

public class AugmentationData
{
	public class AugmentationSkill
	{
		private final int _skillId;
		private final int _maxSkillLevel;
		private final int _augmentationSkillId;

		public AugmentationSkill(int skillId, int maxSkillLevel, int augmentationSkillId)
		{
			_skillId = skillId;
			_maxSkillLevel = maxSkillLevel;
			_augmentationSkillId = augmentationSkillId;
		}

		public int getAugmentationSkillId()
		{
			return _augmentationSkillId;
		}

		public L2Skill getSkill()
		{
			return SkillTable.getInstance().getInfo(_skillId, _maxSkillLevel);
		}
	}

	public class AugmentationStat
	{
		private final Stats _stat;
		private final int _singleSize;
		private final int _combinedSize;
		private final float _singleValues[];
		private final float _combinedValues[];

		public AugmentationStat(Stats stat, float sValues[], float cValues[])
		{
			_stat = stat;
			_singleSize = sValues.length;
			_singleValues = sValues;
			_combinedSize = cValues.length;
			_combinedValues = cValues;
		}

		public int getCombinedStatSize()
		{
			return _combinedSize;
		}

		public float getCombinedStatValue(int i)
		{
			if (i >= _combinedSize || i < 0)
				return _combinedValues[_combinedSize - 1];
			return _combinedValues[i];
		}

		public int getSingleStatSize()
		{
			return _singleSize;
		}

		public float getSingleStatValue(int i)
		{
			if (i >= _singleSize || i < 0)
				return _singleValues[_singleSize - 1];
			return _singleValues[i];
		}

		public Stats getStat()
		{
			return _stat;
		}
	}

	public class AugStat
	{
		private final Stats _stat;
		private final float _value;

		public AugStat(Stats stat, float value)
		{
			_stat = stat;
			_value = value;
		}

		public Stats getStat()
		{
			return _stat;
		}

		public float getValue()
		{
			return _value;
		}
	}

	private static class SingletonHolder
	{
		protected static final AugmentationData _instance = new AugmentationData();
	}

	private final static Logger _log = Logger.getLogger(AugmentationData.class.getName());

	private static final int STAT_START = 1;
	private static final int STAT_END = 14560;

	private static final int STAT_BLOCKSIZE = 3640;
	private static final int STAT_SUBBLOCKSIZE = 91;
	private static final int SKILLS_BLOCKSIZE = 178;
	private static final int BLUE_START = 0;
	private static final int BLUE_END = 16;
	private static final int PURPLE_START = 17;
	private static final int PURPLE_END = 123;

	private static final int RED_START = 124;
	private static final int RED_END = 177;
	private static final int BASESTAT_STR = 16341;
	private static final int BASESTAT_CON = 16342;

	private static final int BASESTAT_INT = 16343;
	private static final int BASESTAT_MEN = 16344;

	public static final AugmentationData getInstance()
	{
		return SingletonHolder._instance;
	}

	private final List<?>[] _augmentationStats;

	private AugmentationSkill[] _augmentationSkills;

	public AugmentationData()
	{
		_augmentationStats = new ArrayList[4];
		_augmentationStats[0] = new ArrayList<AugmentationStat>();
		_augmentationStats[1] = new ArrayList<AugmentationStat>();
		_augmentationStats[2] = new ArrayList<AugmentationStat>();
		_augmentationStats[3] = new ArrayList<AugmentationStat>();

		load();

		_log.info("Augmentation Data: Loaded: " + _augmentationStats[0].size() * 4 + " augmentation stats.");
		_log.info("Augmentation Data: Loaded: " + _augmentationSkills.length + " weapons skills.");
	}

	public L2Augmentation generateRandomAugmentation(int lifeStoneLevel, int lifeStoneGrade)
	{
		int stat34 = 0;
		boolean generateSkill = false;
		int resultColor = 0;
		boolean generateGlow = false;
		if (lifeStoneLevel > 10)
		{
			lifeStoneLevel = 10;
		}
		switch (lifeStoneGrade)
		{
			case 0:
				generateSkill = Rnd.get(100) < Config.AUGMENTATION_NG_SKILL_CHANCE;
				generateGlow = Rnd.get(100) < Config.AUGMENTATION_NG_GLOW_CHANCE;
				break;
			case 1:
				generateSkill = Rnd.get(100) < Config.AUGMENTATION_MID_SKILL_CHANCE;
				generateGlow = Rnd.get(100) < Config.AUGMENTATION_MID_GLOW_CHANCE;
				break;
			case 2:
				generateSkill = Rnd.get(100) < Config.AUGMENTATION_HIGH_SKILL_CHANCE;
				generateGlow = Rnd.get(100) < Config.AUGMENTATION_HIGH_GLOW_CHANCE;
				break;
			case 3:
				generateSkill = Rnd.get(100) < Config.AUGMENTATION_TOP_SKILL_CHANCE;
				generateGlow = Rnd.get(100) < Config.AUGMENTATION_TOP_GLOW_CHANCE;
				break;
		}

		if (!generateSkill && Rnd.get(100) < Config.AUGMENTATION_BASESTAT_CHANCE)
		{
			stat34 = Rnd.get(BASESTAT_STR, BASESTAT_MEN);
		}

		if (stat34 == 0 && !generateSkill)
		{
			resultColor = Rnd.get(0, 100);
			if (resultColor <= 15 * lifeStoneGrade + 40)
			{
				resultColor = 1;
			}
			else
			{
				resultColor = 0;
			}
		}
		else
		{
			resultColor = Rnd.get(0, 100);
			if (resultColor <= 10 * lifeStoneGrade + 5 || stat34 != 0)
			{
				resultColor = 3;
			}
			else if (resultColor <= 10 * lifeStoneGrade + 10)
			{
				resultColor = 1;
			}
			else
			{
				resultColor = 2;
			}
		}
		int stat12 = 0;
		if (stat34 == 0 && !generateSkill)
		{
			int temp = Rnd.get(2, 3);
			int colorOffset = resultColor * 10 * STAT_SUBBLOCKSIZE + temp * STAT_BLOCKSIZE + 1;
			int offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + colorOffset;

			stat34 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
			if (generateGlow && lifeStoneGrade >= 2)
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + lifeStoneGrade * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			else
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + Rnd.get(0, 1) * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		}
		else
		{
			int offset;
			if (!generateGlow)
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + 1;
			}
			else
			{
				offset = (lifeStoneLevel - 1) * STAT_SUBBLOCKSIZE + Rnd.get(0, 1) * STAT_BLOCKSIZE + (lifeStoneGrade + resultColor) / 2 * 10 * STAT_SUBBLOCKSIZE + 1;
			}
			stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		}

		L2Skill skill = null;

		if (generateSkill)
		{
			int skillOffset = (lifeStoneLevel - 1) * SKILLS_BLOCKSIZE;
			AugmentationSkill temp = null;
			switch (resultColor)
			{
				case 1:
				{
					temp = _augmentationSkills[skillOffset + Rnd.get(BLUE_START, BLUE_END)];
					skill = temp.getSkill();
					stat34 = temp.getAugmentationSkillId();
					break;
				}
				case 2:
				{
					temp = _augmentationSkills[skillOffset + Rnd.get(PURPLE_START, PURPLE_END)];
					skill = temp.getSkill();
					stat34 = temp.getAugmentationSkillId();
					break;
				}
				case 3:
				{
					temp = _augmentationSkills[skillOffset + Rnd.get(RED_START, RED_END)];
					skill = temp.getSkill();
					stat34 = temp.getAugmentationSkillId();
					break;
				}
			}
		}

		if (_log.isDebugEnabled())
		{
			_log.info("Augmentation success: stat12=" + stat12 + "; stat34=" + stat34 + "; resultColor=" + resultColor + "; level=" + lifeStoneLevel + "; grade=" + lifeStoneGrade);
		}

		return new L2Augmentation((stat34 << 16) + stat12, skill);
	}

	public List<AugStat> getAugStatsById(int augmentationId)
	{
		List<AugStat> temp = new ArrayList<>();
		int stats[] = new int[2];
		stats[0] = 0x0000FFFF & augmentationId;
		stats[1] = augmentationId >> 16;

		for (int i = 0; i < 2; i++)
			// its a stat
			if (stats[i] >= STAT_START && stats[i] <= STAT_END)
			{
				int block = 0;
				while (stats[i] > STAT_BLOCKSIZE)
				{
					stats[i] -= STAT_BLOCKSIZE;
					block++;
				}

				int subblock = 0;
				while (stats[i] > STAT_SUBBLOCKSIZE)
				{
					stats[i] -= STAT_SUBBLOCKSIZE;
					subblock++;
				}

				if (stats[i] < 14)
				{
					AugmentationStat as = (AugmentationStat) _augmentationStats[block].get(stats[i] - 1);
					temp.add(new AugStat(as.getStat(), as.getSingleStatValue(subblock)));
				}
				else
				{
					stats[i] -= 13;
					int x = 12;
					int rescales = 0;

					while (stats[i] > x)
					{
						stats[i] -= x;
						x--;
						rescales++;
					}
					AugmentationStat as = (AugmentationStat) _augmentationStats[block].get(rescales);
					if (rescales == 0)
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					}
					else
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2 + 1)));
					}

					as = (AugmentationStat) _augmentationStats[block].get(rescales + stats[i]);
					if (as.getStat() == Stats.CRITICAL_DAMAGE)
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
					}
					else
					{
						temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2)));
					}
				}
			}
			else if (stats[i] >= BASESTAT_STR && stats[i] <= BASESTAT_MEN)
			{
				switch (stats[i])
				{
					case BASESTAT_STR:
						temp.add(new AugStat(Stats.STAT_STR, 1.0f));
						break;
					case BASESTAT_CON:
						temp.add(new AugStat(Stats.STAT_CON, 1.0f));
						break;
					case BASESTAT_INT:
						temp.add(new AugStat(Stats.STAT_INT, 1.0f));
						break;
					case BASESTAT_MEN:
						temp.add(new AugStat(Stats.STAT_MEN, 1.0f));
						break;
				}
			}

		return temp;
	}

	@SuppressWarnings("unchecked")
	private final void load()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = new File(Config.DATAPACK_ROOT, "data/xml/stats/augmentation/augmentation_skillmap.xml");
			if (!file.exists())
			{
				if (_log.isDebugEnabled() || Config.DEBUG)
				{
					_log.info("The augmentation skillmap file is missing.");
				}
				return;
			}

			List<AugmentationSkill> list = new ArrayList<>();

			Document doc = factory.newDocumentBuilder().parse(file);
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if ("augmentation".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int skillId = 0, skillLevel = 0, augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());

							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								if ("skillId".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									skillId = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								}
								else if ("skillLevel".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									skillLevel = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
								}

							list.add(new AugmentationSkill(skillId, skillLevel, augmentationId));
						}
				}
			_augmentationSkills = list.toArray(new AugmentationSkill[list.size()]);
		}
		catch (Exception e)
		{
			_log.fatal("Error parsing augmentation_skillmap.xml.", e);
			return;
		}

		for (int i = 1; i < 5; i++)
		{
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);

				File file = new File(Config.DATAPACK_ROOT, "data/xml/stats/augmentation/augmentation_stats" + i + ".xml");
				if (!file.exists())
				{
					if (_log.isDebugEnabled() || Config.DEBUG)
					{
						_log.info("The augmentation stat data file " + i + " is missing.");
					}
					return;
				}

				Document doc = factory.newDocumentBuilder().parse(file);

				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
							if ("stat".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								String statName = attrs.getNamedItem("name").getNodeValue();
								float soloValues[] = null, combinedValues[] = null;

								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
									if ("table".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										String tableName = attrs.getNamedItem("name").getNodeValue();

										StringTokenizer data = new StringTokenizer(cd.getFirstChild().getNodeValue());
										List<Float> array = new ArrayList<>();
										while (data.hasMoreTokens())
										{
											array.add(Float.parseFloat(data.nextToken()));
										}

										if (tableName.equalsIgnoreCase("#soloValues"))
										{
											soloValues = new float[array.size()];
											int x = 0;
											for (float value : array)
											{
												soloValues[x++] = value;
											}
										}
										else
										{
											combinedValues = new float[array.size()];
											int x = 0;
											for (float value : array)
											{
												combinedValues[x++] = value;
											}
										}
									}
								((List<AugmentationStat>) _augmentationStats[i - 1]).add(new AugmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
							}
					}
			}
			catch (Exception e)
			{
				_log.fatal("Error parsing augmentation_stats" + i + ".xml.", e);
				return;
			}
		}
	}
}
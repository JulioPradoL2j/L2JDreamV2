package com.dream.game.datatables.sql;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2DropCategory;
import com.dream.game.model.L2DropData;
import com.dream.game.model.L2MinionData;
import com.dream.game.model.L2Skill;
import com.dream.game.model.base.ClassId;
import com.dream.game.skills.Stats;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.util.ResourceUtil;
import com.dream.util.StatsSet;
import com.dream.util.XMLDocumentFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NpcTable
{
	private static class SingletonHolder
	{
		protected static final NpcTable _instance = new NpcTable();
	}

	private final static Logger _log = Logger.getLogger(NpcTable.class.getName());

	public static final NpcTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<Integer, L2NpcTemplate> _npcs;

	private boolean _initialized = false;

	private Connection con;

	public NpcTable()
	{
		_npcs = new HashMap<>();
		restoreNpcData();
	}

	public void cleanUp()
	{
		_npcs.clear();
	}

	private boolean fillNpcTable(ResultSet NpcData) throws Exception
	{
		boolean loaded = false;
		while (NpcData.next())
		{
			StatsSet npcDat = new StatsSet();
			int id = NpcData.getInt("id");

			if (Config.ASSERT)
			{
				assert id < 1000000;
			}

			npcDat.set("npcId", id);
			npcDat.set("idTemplate", NpcData.getInt("idTemplate"));
			int level = NpcData.getInt("level");
			npcDat.set("level", level);
			npcDat.set("race", NpcData.getInt("race"));

			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate", 38);

			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("fcollision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("fcollision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			if (!Config.ALLOW_NPC_WALKERS && NpcData.getString("type").equalsIgnoreCase("L2NpcWalker"))
			{
				npcDat.set("type", "L2Npc");
			}
			else
			{
				npcDat.set("type", NpcData.getString("type"));
			}
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			npcDat.set("rewardExp", NpcData.getInt("exp"));
			npcDat.set("rewardSp", NpcData.getInt("sp"));
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));

			npcDat.set("baseSTR", NpcData.getInt("str"));
			npcDat.set("baseCON", NpcData.getInt("con"));
			npcDat.set("baseDEX", NpcData.getInt("dex"));
			npcDat.set("baseINT", NpcData.getInt("int"));
			npcDat.set("baseWIT", NpcData.getInt("wit"));
			npcDat.set("baseMEN", NpcData.getInt("men"));

			npcDat.set("baseHpMax", NpcData.getInt("hp"));
			npcDat.set("baseCpMax", 0);
			npcDat.set("baseMpMax", NpcData.getInt("mp"));
			npcDat.set("baseHpReg", NpcData.getFloat("hpreg") > 0 ? NpcData.getFloat("hpreg") : 1.5 + (level - 1) / 10.0);
			npcDat.set("baseMpReg", NpcData.getFloat("mpreg") > 0 ? NpcData.getFloat("mpreg") : 0.9 + 0.3 * ((level - 1) / 10.0));
			npcDat.set("basePAtk", NpcData.getInt("patk"));
			npcDat.set("basePDef", NpcData.getInt("pdef"));
			npcDat.set("baseMAtk", NpcData.getInt("matk"));
			npcDat.set("baseMDef", NpcData.getInt("mdef"));

			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));

			npcDat.set("isUndead", NpcData.getString("isUndead"));

			npcDat.set("absorb_level", NpcData.getString("absorb_level"));
			npcDat.set("absorb_type", NpcData.getString("absorb_type"));

			npcDat.set("ss", NpcData.getInt("ss"));
			npcDat.set("bss", NpcData.getInt("bss"));
			npcDat.set("ssRate", NpcData.getInt("ss_rate"));

			npcDat.set("AI", NpcData.getString("AI"));
			npcDat.set("drop_herbs", Boolean.valueOf(NpcData.getString("drop_herbs")));

			L2NpcTemplate template = new L2NpcTemplate(npcDat);
			template.addVulnerability(Stats.BOW_WPN_VULN, 1);
			template.addVulnerability(Stats.CROSSBOW_WPN_VULN, 1);
			template.addVulnerability(Stats.BLUNT_WPN_VULN, 1);
			template.addVulnerability(Stats.DAGGER_WPN_VULN, 1);

			_npcs.put(id, template);

			loaded = true;
		}
		return loaded;
	}

	public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		for (L2NpcTemplate t : _npcs.values())
			if (t.getLevel() == lvl && "L2Monster".equals(t.getType()))
			{
				list.add(t);
			}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public Set<Integer> getAllNpcOfAiType(String aiType)
	{
		return null;
	}

	public L2NpcTemplate[] getAllNpcOfClassType(String classType)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		for (L2NpcTemplate t : _npcs.values())
			if (t.getType().equalsIgnoreCase(classType))
			{
				list.add(t);
			}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public Set<Integer> getAllNpcOfL2jClass(Class<?> clazz)
	{
		return null;
	}

	public L2NpcTemplate[] getAllNpcStartingWith(String letter)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		for (L2NpcTemplate t : _npcs.values())
			if (t.getName().startsWith(letter) && "L2Npc".equals(t.getType()))
			{
				list.add(t);
			}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		for (L2NpcTemplate t : _npcs.values())
			if (t.getLevel() == lvl)
			{
				list.add(t);
			}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public Map<Integer, L2NpcTemplate> getAllTemplates()
	{
		return _npcs;
	}

	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}

	public L2NpcTemplate getTemplateByName(String name)
	{
		for (L2NpcTemplate npcTemplate : _npcs.values())
			if (npcTemplate.getName().equalsIgnoreCase(name))
				return npcTemplate;
		return null;
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public void reloadAll()
	{
		restoreNpcData();
	}

	public boolean reloadNpc(int id)
	{
		Connection con = null;
		boolean loaded = false;
		try
		{
			L2NpcTemplate old = getTemplate(id);
			Map<Integer, L2Skill> skills = new HashMap<>();

			if (old != null && old.getSkills() != null)
			{
				skills.putAll(old.getSkills());
			}

			List<L2DropCategory> categories = new LinkedList<>();

			if (old != null && old.getDropData() != null)
			{
				categories.addAll(old.getDropData());
			}

			List<ClassId> classIds = new ArrayList<>();

			if (old != null && old.getTeachInfo() != null)
			{
				classIds.addAll(old.getTeachInfo());
			}

			List<L2MinionData> minions = new ArrayList<>();

			if (old != null && old.getMinionData() != null)
			{
				minions.addAll(old.getMinionData());
			}

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString("id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "collision_radius", "collision_height", "level", "race", "sex", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level", "absorb_type", "ss", "bss", "ss_rate", "AI", "drop_herbs") + " FROM npc WHERE id=?");
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			loaded = fillNpcTable(rs);
			rs.close();
			st.close();

			if (Config.ALLOW_CUSTOM_NPC_TABLE)
				if (!loaded)
				{
					st = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString("id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "collision_radius", "collision_height", "level", "sex", "race", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level", "absorb_type", "ss", "bss", "ss_rate", "AI", "drop_herbs") + " FROM custom_npc WHERE id=?");
					st.setInt(1, id);
					rs = st.executeQuery();
					loaded = fillNpcTable(rs);
					rs.close();
					st.close();
				}

			L2NpcTemplate created = getTemplate(id);

			for (L2Skill skill : skills.values())
			{
				created.addSkill(skill);
			}

			for (ClassId classId : classIds)
			{
				created.addTeachInfo(classId);
			}

			for (L2MinionData minion : minions)
			{
				created.addRaidData(minion);
			}
		}
		catch (Exception e)
		{
			_log.warn("NPCTable: Could not reload data for NPC " + id + ": " + e, e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return loaded;
	}

	public void replaceTemplate(L2NpcTemplate npc)
	{
		_npcs.put(npc.getNpcId(), npc);
	}

	
	private void restoreNpcData()
	{
		con = null;

		try
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement;
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString("id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "race", "collision_radius", "collision_height", "level", "sex", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level", "absorb_type", "ss", "bss", "ss_rate", "AI", "drop_herbs") + " FROM npc");
				ResultSet npcdata = statement.executeQuery();

				fillNpcTable(npcdata);
				npcdata.close();
				statement.close();
				_log.info("Npc Data: Loaded " + _npcs.size() + " Npc Templates.");
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error creating NPC table: ", e);
			}

			if (Config.ALLOW_CUSTOM_NPC_TABLE)
			{
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(con);
					PreparedStatement statement;
					statement = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString("id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "race", "collision_radius", "collision_height", "level", "sex", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level", "absorb_type", "ss", "bss", "ss_rate", "AI", "drop_herbs") + " FROM custom_npc");
					ResultSet npcdata = statement.executeQuery();
					int npc_count = _npcs.size();
					fillNpcTable(npcdata);
					npcdata.close();
					statement.close();
					if (_npcs.size() > npc_count)
					{
						_log.info("Npc Data: Loaded " + (_npcs.size() - npc_count) + " Custom Npc Templates.");
					}
				}
				catch (Exception e)
				{
					_log.fatal("NPCTable: Error creating custom NPC table: ", e);
				}
			}

			try
			{
				final File f = new File("./data/xml/world/npc_skills.xml");
				final Document doc = XMLDocumentFactory.getInstance().loadDocument(f);

				final Node n = doc.getFirstChild();
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if (d.getNodeName().equalsIgnoreCase("npc_skills"))
					{
						NamedNodeMap attrs = d.getAttributes();

						int npcId = Integer.valueOf(attrs.getNamedItem("npc_id").getNodeValue());
						int skillId = Integer.parseInt(attrs.getNamedItem("skill_id").getNodeValue());
						int level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
						L2NpcTemplate npc = _npcs.get(npcId);

						if (npc == null)
						{
							continue;
						}

						if (skillId == 4416)
						{
							npc.setRace(level);
							continue;
						}

						L2Skill npcSkill = SkillTable.getInstance().getInfo(skillId, level);
						if (npcSkill == null)
						{
							continue;
						}

						npc.addSkill(npcSkill);
					}
			}
			catch (Exception e)
			{
				_log.warn("NPCTable: Error reading NPC skills table" + e);
			}

			try
			{
				PreparedStatement statement2 = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString("mobId", "itemId", "min", "max", "category", "chance") + " FROM droplist ORDER BY mobId, chance DESC");
				ResultSet dropData = statement2.executeQuery();
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;

				while (dropData.next())
				{
					int mobId = dropData.getInt("mobId");
					npcDat = _npcs.get(mobId);
					if (npcDat == null)
					{
						_log.fatal("NPCTable: Drop data for undefined NPC. npcId: " + mobId);
						continue;
					}
					dropDat = new L2DropData();

					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setChance(dropData.getInt("chance"));

					int category = dropData.getInt("category");

					npcDat.addDropData(dropDat, category);
				}

				dropData.close();
				statement2.close();
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error reading NPC drop data: ", e);
			}

			if (Config.ALLOW_CUSTOM_DROPLIST_TABLE)
			{
				try
				{
					PreparedStatement statement2 = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString("mobId", "itemId", "min", "max", "category", "chance") + " FROM custom_droplist ORDER BY mobId, chance DESC");
					ResultSet dropData = statement2.executeQuery();
					L2DropData dropDat = null;
					L2NpcTemplate npcDat = null;

					while (dropData.next())
					{
						int mobId = dropData.getInt("mobId");
						npcDat = _npcs.get(mobId);
						if (npcDat == null)
						{
							_log.fatal("NPCTable: Custom drop data for undefined NPC. npcId: " + mobId);
							continue;
						}
						dropDat = new L2DropData();

						dropDat.setItemId(dropData.getInt("itemId"));
						dropDat.setMinDrop(dropData.getInt("min"));
						dropDat.setMaxDrop(dropData.getInt("max"));
						dropDat.setChance(dropData.getInt("chance"));

						int category = dropData.getInt("category");

						npcDat.addDropData(dropDat, category);
					}

					dropData.close();
					statement2.close();
				}
				catch (Exception e)
				{
					_log.fatal("NPCTable: Error reading custom NPC drop data: ", e);
				}
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			int th = 0;
			File f = new File(Config.DATAPACK_ROOT + "/data/xml/player/skills/skill_learn.xml");
			if (!f.exists())
			{
				_log.error("skill_learn.xml could not be loaded: file not found");
				return;
			}
			try
			{
				InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
				in.setEncoding("UTF-8");
				Document doc = factory.newDocumentBuilder().parse(in);
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
					if (n.getNodeName().equalsIgnoreCase("list"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
							if (d.getNodeName().equalsIgnoreCase("learn"))
							{
								int npcId = Integer.valueOf(d.getAttributes().getNamedItem("npc_id").getNodeValue());
								int classId = Integer.valueOf(d.getAttributes().getNamedItem("class_id").getNodeValue());
								L2NpcTemplate npc = _npcs.get(npcId);

								if (npc == null)
								{
									_log.warn("NPCTable: Error getting NPC template ID " + npcId + " while trying to load skill trainer data.", new NullPointerException());
									continue;
								}

								npc.addTeachInfo(ClassId.values()[classId]);
								th++;
							}
					}
			}
			catch (SAXException e)
			{
				_log.error("NPCTable: Error reading NPC trainer data", e);
			}
			catch (IOException e)
			{
				_log.error("NPCTable: Error reading NPC trainer data", e);
			}
			catch (ParserConfigurationException e)
			{
				_log.error("NPCTable: Error reading NPC trainer data", e);
			}
			_log.info("Npc Data: Loaded " + th + " teachers.");

			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			int cnt = 0;
			File f1 = new File(Config.DATAPACK_ROOT + "/data/xml/world/minion.xml");
			if (!f1.exists())
			{
				_log.error("minion.xml could not be loaded: file not found");
				return;
			}
			try
			{
				InputSource in1 = new InputSource(new InputStreamReader(new FileInputStream(f1), "UTF-8"));
				in1.setEncoding("UTF-8");
				Document doc1 = factory1.newDocumentBuilder().parse(in1);
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				for (Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
					if (n1.getNodeName().equalsIgnoreCase("list"))
					{
						for (Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
							if (d1.getNodeName().equalsIgnoreCase("minion"))
							{
								int raidId = Integer.valueOf(d1.getAttributes().getNamedItem("boss_id").getNodeValue());
								int mid = Integer.valueOf(d1.getAttributes().getNamedItem("minion_id").getNodeValue());
								int mmin = Integer.valueOf(d1.getAttributes().getNamedItem("amount_min").getNodeValue());
								int mmax = Integer.valueOf(d1.getAttributes().getNamedItem("amount_max").getNodeValue());

								npcDat = _npcs.get(raidId);
								minionDat = new L2MinionData();

								minionDat.setMinionId(mid);
								minionDat.setAmountMin(mmin);
								minionDat.setAmountMax(mmax);
								npcDat.addRaidData(minionDat);
								cnt++;
								minionDat = null;
							}
					}
			}
			catch (SAXException e)
			{
				_log.error("Error loading minion data", e);
			}
			catch (IOException e)
			{
				_log.error("Error loading minion data", e);
			}
			catch (ParserConfigurationException e)
			{
				_log.error("Error loading minion data", e);
			}
			_log.info("Npc Data: Loaded " + cnt + " minions.");
		}
		catch (Exception e)
		{

		}
		finally
		{
			ResourceUtil.closeConnection(con);
		}

		_initialized = true;
	}

	
	public void saveNpc(StatsSet npc)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			Map<String, Object> set = npc.getSet();

			String name = "";
			String values = "";

			for (Object obj : set.keySet())
			{
				name = (String) obj;

				if (name.equalsIgnoreCase("npcId"))
				{
					continue;
				}

				if (values != "")
				{
					values += ", ";
				}

				values += name + " = '" + set.get(name) + "'";
			}

			String query = "UPDATE npc SET " + values + " WHERE id = ?";
			String query_custom = "UPDATE custom_npc SET " + values + " WHERE id = ?";

			try
			{
				PreparedStatement statement = con.prepareStatement(query);
				statement.setInt(1, npc.getInteger("npcId"));
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
			}

			try
			{
				PreparedStatement statement = con.prepareStatement(query_custom);
				statement.setInt(1, npc.getInteger("npcId"));
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
			}
		}
		catch (Exception e)
		{
			_log.warn("NPCTable: Could not store new NPC data in database: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
package com.dream.game.handler.admin;

import com.dream.L2DatabaseFactory;
import com.dream.game.access.gmHandler;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.BuyListTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2DropCategory;
import com.dream.game.model.L2DropData;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2MerchantInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.buylist.NpcBuyList;
import com.dream.game.model.buylist.Product;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Item;
import com.dream.util.StatsSet;
import com.dream.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class AdminEditNpc extends gmHandler
{
	private static final String[] commands =
	{
		"edit_npc",
		"save_npc",
		"show_droplist",
		"edit_drop",
		"add_drop",
		"del_drop",
		"showShop",
		"showShopList",
		"box_access",
		"close_window",
		"show_skilllist_npc",
		"add_skill_npc",
		"edit_skill_npc",
		"del_skill_npc",
		"load_npc",

	};

	
	private static void addDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("INSERT INTO droplist(mobId, itemId, min, max, category, chance) values(?,?,?,?,?,?)");
			statement.setInt(1, npcId);
			statement.setInt(2, itemId);
			statement.setInt(3, min);
			statement.setInt(4, max);
			statement.setInt(5, category);
			statement.setInt(6, chance);
			statement.execute();
			statement.close();

			reLoadNpcDropList(npcId);

			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><title>Add drop data complete!</title>");
			replyMSG.append("<body>");
			replyMSG.append("<center><button value=\"Continue add\" action=\"bypass -h admin_add_drop " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
			replyMSG.append("<br><br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
			replyMSG.append("</center></body></html>");

			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private static void addNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		Connection con = null;

		try
		{
			L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
			if (skillData == null)
			{

				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to Npc</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\"></center>");
				replyMSG.append("</body></html>");

				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
				return;
			}

			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("INSERT INTO npcskills(npcid, skillid, level) values(?,?,?)");
			statement.setInt(1, npcId);
			statement.setInt(2, skillId);
			statement.setInt(3, level);
			statement.execute();
			statement.close();

			reLoadNpcSkillList(npcId);

			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to Npc (" + npcId + ", " + skillId + ", " + level + ")</title>");
			replyMSG.append("<body>");
			replyMSG.append("<center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
			replyMSG.append("<br><br><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
			replyMSG.append("</center></body></html>");

			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private static void deleteDropData(L2PcInstance activeChar, int npcId, int itemId, int category)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			if (npcId > 0)
			{
				PreparedStatement statement2 = con.prepareStatement("DELETE FROM droplist WHERE mobId=? AND itemId=? AND category=?");
				statement2.setInt(1, npcId);
				statement2.setInt(2, itemId);
				statement2.setInt(3, category);
				statement2.execute();
				statement2.close();

				reLoadNpcDropList(npcId);

				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuilder replyMSG = new StringBuilder("<html><title>Delete drop data(" + npcId + ", " + itemId + ", " + category + ")complete</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\"></center>");
				replyMSG.append("</body></html>");

				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

	}

	
	private static void deleteNpcSkillData(L2PcInstance activeChar, int npcId, int skillId)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			if (npcId > 0)
			{
				PreparedStatement statement2 = con.prepareStatement("DELETE FROM npcskills WHERE npcid=? AND skillid=?");
				statement2.setInt(1, npcId);
				statement2.setInt(2, skillId);
				statement2.execute();
				statement2.close();

				reLoadNpcSkillList(npcId);

				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Delete Skill (" + npcId + ", " + skillId + ")</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\"></center>");
				replyMSG.append("</body></html>");

				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private static void reLoadNpcDropList(int npcId)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
			return;

		npcData.clearAllDropData();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			L2DropData dropData = null;

			npcData.clearAllDropData();

			PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString("mobId", "itemId", "min", "max", "category", "chance") + " FROM droplist WHERE mobId=?");
			statement.setInt(1, npcId);
			ResultSet dropDataList = statement.executeQuery();

			while (dropDataList.next())
			{
				dropData = new L2DropData();

				dropData.setItemId(dropDataList.getInt("itemId"));
				dropData.setMinDrop(dropDataList.getInt("min"));
				dropData.setMaxDrop(dropDataList.getInt("max"));
				dropData.setChance(dropDataList.getInt("chance"));

				int category = dropDataList.getInt("category");
				npcData.addDropData(dropData, category);
			}
			dropDataList.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private static void reLoadNpcSkillList(int npcId)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);

			L2Skill skillData = null;
			if (npcData.getSkills() != null)
			{
				npcData.getSkills().clear();
			}

			String _sql = "SELECT npcid, skillid, level FROM npcskills WHERE npcid=? AND (skillid NOT BETWEEN 4290 AND 4302)";

			PreparedStatement statement = con.prepareStatement(_sql);
			statement.setInt(1, npcId);
			ResultSet skillDataList = statement.executeQuery();

			while (skillDataList.next())
			{
				int idval = skillDataList.getInt("skillid");
				int levelval = skillDataList.getInt("level");
				skillData = SkillTable.getInstance().getInfo(idval, levelval);
				if (skillData != null)
				{
					npcData.addSkill(skillData);
				}
			}
			skillDataList.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private static void save_npc_property(L2PcInstance activeChar, String command)
	{
		String[] commandSplit = command.split(" ");

		if (commandSplit.length < 4)
			return;

		StatsSet newNpcData = new StatsSet();

		try
		{
			newNpcData.set("npcId", commandSplit[1]);

			String statToSet = commandSplit[2];
			String value = commandSplit[3];

			if (commandSplit.length > 4)
			{
				for (int i = 0; i < commandSplit.length - 3; i++)
				{
					value += " " + commandSplit[i + 4];
				}
			}

			if (statToSet.equals("templateId"))
			{
				newNpcData.set("idTemplate", Integer.valueOf(value));
			}
			else if (statToSet.equals("name"))
			{
				newNpcData.set("name", value);
			}
			else if (statToSet.equals("serverSideName"))
			{
				newNpcData.set("serverSideName", Integer.valueOf(value));
			}
			else if (statToSet.equals("title"))
			{
				newNpcData.set("title", value);
			}
			else if (statToSet.equals("serverSideTitle"))
			{
				newNpcData.set("serverSideTitle", Integer.valueOf(value) == 1 ? 1 : 0);
			}
			else if (statToSet.equals("collisionRadius"))
			{
				newNpcData.set("collision_radius", Integer.valueOf(value));
			}
			else if (statToSet.equals("collisionHeight"))
			{
				newNpcData.set("collision_height", Integer.valueOf(value));
			}
			else if (statToSet.equals("level"))
			{
				newNpcData.set("level", Integer.valueOf(value));
			}
			else if (statToSet.equals("sex"))
			{
				int intValue = Integer.valueOf(value);
				newNpcData.set("sex", intValue == 0 ? "male" : intValue == 1 ? "female" : "etc");
			}
			else if (statToSet.equals("type"))
			{
				Class.forName("com.dream.game.model.actor.instance." + value + "Instance");
				newNpcData.set("type", value);
			}
			else if (statToSet.equals("attackRange"))
			{
				newNpcData.set("attackrange", Integer.valueOf(value));
			}
			else if (statToSet.equals("hp"))
			{
				newNpcData.set("hp", Integer.valueOf(value));
			}
			else if (statToSet.equals("mp"))
			{
				newNpcData.set("mp", Integer.valueOf(value));
			}
			else if (statToSet.equals("hpRegen"))
			{
				newNpcData.set("hpreg", Integer.valueOf(value));
			}
			else if (statToSet.equals("mpRegen"))
			{
				newNpcData.set("mpreg", Integer.valueOf(value));
			}
			else if (statToSet.equals("str"))
			{
				newNpcData.set("str", Integer.valueOf(value));
			}
			else if (statToSet.equals("con"))
			{
				newNpcData.set("con", Integer.valueOf(value));
			}
			else if (statToSet.equals("dex"))
			{
				newNpcData.set("dex", Integer.valueOf(value));
			}
			else if (statToSet.equals("int"))
			{
				newNpcData.set("int", Integer.valueOf(value));
			}
			else if (statToSet.equals("wit"))
			{
				newNpcData.set("wit", Integer.valueOf(value));
			}
			else if (statToSet.equals("men"))
			{
				newNpcData.set("men", Integer.valueOf(value));
			}
			else if (statToSet.equals("exp"))
			{
				newNpcData.set("exp", Integer.valueOf(value));
			}
			else if (statToSet.equals("sp"))
			{
				newNpcData.set("sp", Integer.valueOf(value));
			}
			else if (statToSet.equals("pAtk"))
			{
				newNpcData.set("patk", Integer.valueOf(value));
			}
			else if (statToSet.equals("pDef"))
			{
				newNpcData.set("pdef", Integer.valueOf(value));
			}
			else if (statToSet.equals("mAtk"))
			{
				newNpcData.set("matk", Integer.valueOf(value));
			}
			else if (statToSet.equals("mDef"))
			{
				newNpcData.set("mdef", Integer.valueOf(value));
			}
			else if (statToSet.equals("pAtkSpd"))
			{
				newNpcData.set("atkspd", Integer.valueOf(value));
			}
			else if (statToSet.equals("aggro"))
			{
				newNpcData.set("aggro", Integer.valueOf(value));
			}
			else if (statToSet.equals("mAtkSpd"))
			{
				newNpcData.set("matkspd", Integer.valueOf(value));
			}
			else if (statToSet.equals("rHand"))
			{
				newNpcData.set("rhand", Integer.valueOf(value));
			}
			else if (statToSet.equals("lHand"))
			{
				newNpcData.set("lhand", Integer.valueOf(value));
			}
			else if (statToSet.equals("armor"))
			{
				newNpcData.set("armor", Integer.valueOf(value));
			}
			else if (statToSet.equals("runSpd"))
			{
				newNpcData.set("runspd", Integer.valueOf(value));
			}
			else if (statToSet.equals("factionId"))
			{
				newNpcData.set("faction_id", value);
			}
			else if (statToSet.equals("factionRange"))
			{
				newNpcData.set("faction_range", Integer.valueOf(value));
			}
			else if (statToSet.equals("isUndead"))
			{
				newNpcData.set("isUndead", Integer.valueOf(value) == 1 ? 1 : 0);
			}
			else if (statToSet.equals("absorbLevel"))
			{
				int intVal = Integer.valueOf(value);
				newNpcData.set("absorb_level", intVal < 0 ? 0 : intVal > 12 ? 0 : intVal);
			}
			else if (statToSet.equals("absorbType"))
			{
				int intValue = Integer.valueOf(value);
				newNpcData.set("absorb_type", intValue == 0 ? "LAST_HIT" : intValue == 1 ? "FULL_PARTY" : "PARTY_ONE_RANDOM");
			}
		}
		catch (Exception e)
		{
			_log.warn("Error saving new npc value: " + e);
		}

		NpcTable.getInstance().saveNpc(newNpcData);

		int npcId = newNpcData.getInteger("npcId");

		NpcTable.getInstance().reloadNpc(npcId);
		Show_Npc_Property(activeChar, NpcTable.getInstance().getTemplate(npcId), 1);
	}

	private static void Show_Npc_Property(L2PcInstance activeChar, L2NpcTemplate npc, int pageId)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		String content = HtmCache.getInstance().getHtm("data/html/admin/menus/submenus/editnpc_menu-" + pageId + ".htm");

		if (content != null)
		{
			adminReply.setHtml(content);
			adminReply.replace("%npcId%", String.valueOf(npc.getNpcId()));
			adminReply.replace("%templateId%", String.valueOf(npc.getIdTemplate()));
			adminReply.replace("%name%", npc.getName());
			adminReply.replace("%serverSideName%", npc.isServerSideName() ? "1" : "0");
			adminReply.replace("%title%", npc.getTitle());
			adminReply.replace("%serverSideTitle%", npc.isServerSideTitle() ? "1" : "0");
			adminReply.replace("%collisionRadius%", String.valueOf(npc.getCollisionRadius()));
			adminReply.replace("%collisionHeight%", String.valueOf(npc.getCollisionHeight()));
			adminReply.replace("%level%", String.valueOf(npc.getLevel()));
			adminReply.replace("%sex%", String.valueOf(npc.getSex()));
			adminReply.replace("%type%", String.valueOf(npc.getType()));
			adminReply.replace("%attackRange%", String.valueOf(npc.getBaseAtkRange()));
			adminReply.replace("%hp%", String.valueOf(npc.getBaseHpMax()));
			adminReply.replace("%mp%", String.valueOf(npc.getBaseMpMax()));
			adminReply.replace("%hpRegen%", String.valueOf(npc.getBaseHpReg()));
			adminReply.replace("%mpRegen%", String.valueOf(npc.getBaseMpReg()));
			adminReply.replace("%str%", String.valueOf(npc.getBaseSTR()));
			adminReply.replace("%con%", String.valueOf(npc.getBaseCON()));
			adminReply.replace("%dex%", String.valueOf(npc.getBaseDEX()));
			adminReply.replace("%int%", String.valueOf(npc.getBaseINT()));
			adminReply.replace("%wit%", String.valueOf(npc.getBaseWIT()));
			adminReply.replace("%men%", String.valueOf(npc.getBaseMEN()));
			adminReply.replace("%exp%", String.valueOf(npc.getRewardExp()));
			adminReply.replace("%sp%", String.valueOf(npc.getRewardSp()));
			adminReply.replace("%pAtk%", String.valueOf(npc.getBasePAtk()));
			adminReply.replace("%pDef%", String.valueOf(npc.getBasePDef()));
			adminReply.replace("%mAtk%", String.valueOf(npc.getBaseMAtk()));
			adminReply.replace("%mDef%", String.valueOf(npc.getBaseMDef()));
			adminReply.replace("%pAtkSpd%", String.valueOf(npc.getBasePAtkSpd()));
			adminReply.replace("%aggro%", String.valueOf(npc.getAggroRange()));
			adminReply.replace("%mAtkSpd%", String.valueOf(npc.getBaseMAtkSpd()));
			adminReply.replace("%rHand%", String.valueOf(npc.getRhand()));
			adminReply.replace("%lHand%", String.valueOf(npc.getLhand()));
			adminReply.replace("%armor%", String.valueOf(npc.getArmor()));
			adminReply.replace("%walkSpd%", String.valueOf(npc.getBaseWalkSpd()));
			adminReply.replace("%runSpd%", String.valueOf(npc.getBaseRunSpd()));
			adminReply.replace("%factionId%", npc.getFactionId() == null ? "" : npc.getFactionId());
			adminReply.replace("%factionRange%", String.valueOf(npc.getFactionRange()));
			adminReply.replace("%isUndead%", npc.isUndead() ? "1" : "0");
			adminReply.replace("%absorbLevel%", String.valueOf(npc.getAbsorbLevel()));
			adminReply.replace("%absorbType%", String.valueOf(npc.getAbsorbType()));
		}
		else
		{
			adminReply.setHtml("<html><body>File not found: data/html/admin/menus/submenus/editnpc-" + pageId + ".htm</body></html>");
		}

		activeChar.sendPacket(adminReply);
	}

	private static void showAddDropData(L2PcInstance activeChar, L2NpcTemplate npcData)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><title>Add dropdata to " + npcData.getName() + "(" + npcData.getNpcId() + ")</title>");
		replyMSG.append("<body>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Item-Id</td><td><edit var=\"itemId\" width=80></td></tr>");
		replyMSG.append("<tr><td>MIN</td><td><edit var=\"min\" width=80></td></tr>");
		replyMSG.append("<tr><td>MAX</td><td><edit var=\"max\" width=80></td></tr>");
		replyMSG.append("<tr><td>CATEGORY(sweep=-1)</td><td><edit var=\"category\" width=80></td></tr>");
		replyMSG.append("<tr><td>CHANCE(0-1000000)</td><td><edit var=\"chance\" width=80></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"SAVE\" action=\"bypass -h admin_add_drop " + npcData.getNpcId() + " $itemId $category $min $max $chance\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("<br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcData.getNpcId() + "\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());

		activeChar.sendPacket(adminReply);
	}

	
	private static void showEditDropData(L2PcInstance activeChar, int npcId, int itemId, int category)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("SELECT mobId, itemId, min, max, category, chance FROM droplist WHERE mobId=" + npcId + " AND itemId=" + itemId + " AND category=" + category);
			ResultSet dropData = statement.executeQuery();

			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			StringBuilder replyMSG = new StringBuilder("<html><title>the detail of dropdata: (" + npcId + " " + itemId + " " + category + ")</title>");
			replyMSG.append("<body>");

			if (dropData.next())
			{
				replyMSG.append("<table width=200>");
				replyMSG.append("<tr><td>Appertain of NPC</td><td>" + NpcTable.getInstance().getTemplate(dropData.getInt("mobId")).getName() + "</td></tr>");
				replyMSG.append("<tr><td>ItemName</td><td>" + ItemTable.getInstance().getTemplate(dropData.getInt("itemId")).getName() + "(" + dropData.getInt("itemId") + ")</td></tr>");
				replyMSG.append("<tr><td>Category</td><td>" + (category == -1 ? "sweep" : Integer.toString(category)) + "</td></tr>");
				replyMSG.append("<tr><td>MIN(" + dropData.getInt("min") + ")</td><td><edit var=\"min\" width=80></td></tr>");
				replyMSG.append("<tr><td>MAX(" + dropData.getInt("max") + ")</td><td><edit var=\"max\" width=80></td></tr>");
				replyMSG.append("<tr><td>CHANCE(" + dropData.getInt("chance") + ")</td><td><edit var=\"chance\" width=80></td></tr>");
				replyMSG.append("</table><br>");

				replyMSG.append("<center>");
				replyMSG.append("<button value=\"Save Modify\" action=\"bypass -h admin_edit_drop " + npcId + " " + itemId + " " + category + " $min $max $chance\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
				replyMSG.append("<br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + dropData.getInt("mobId") + "\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
				replyMSG.append("</center>");
			}

			dropData.close();
			statement.close();

			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());

			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private static void showNpcDropList(L2PcInstance activeChar, int npcId)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
			return;

		StringBuilder replyMSG = new StringBuilder("<html><title>NPC: " + npcData.getName() + "(" + npcData.getNpcId() + ") 's drop manage</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>Notes: click[drop_id]to show the detail of drop data,click[del] to delete the drop data!");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>npc_id itemId category</td><td>item[id]</td><td>type</td><td>del</td></tr>");


		L2Item itemTemplate;
		for (final L2DropCategory cat : npcData.getDropData())
		{
			for (final L2DropData drop : cat.getAllDrops())
			{
				itemTemplate = ItemTable.getInstance().getTemplate(drop.getItemId());
				if (itemTemplate == null)
				{
					continue;
				}
				replyMSG.append("<tr><td><a action=\"bypass -h admin_edit_drop " + npcData.getNpcId() + " " + drop.getItemId() + " " + cat.getCategoryType() + "\">" + npcData.getNpcId() + " " + drop.getItemId() + " " + cat.getCategoryType() + "</a></td>" + "<td>" + itemTemplate.getName() + "[" + drop.getItemId() + "]" + "</td><td>" + (drop.isQuestDrop() ? "Q" : cat.isSweep() ? "S" : "D") + "</td><td>" + "<a action=\"bypass -h admin_del_drop " + npcData.getNpcId() + " " + drop.getItemId() + " " + cat.getCategoryType() + "\">del</a></td></tr>");
			}
		}
		replyMSG.append("</table>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add DropData\" action=\"bypass -h admin_add_drop " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("<button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("</center></body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);

	}

	private static void showNpcSkillAdd(L2PcInstance activeChar, L2NpcTemplate npcData)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to " + npcData.getName() + "(ID:" + npcData.getNpcId() + ")</title>");
		replyMSG.append("<body>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>SkillId</td><td><edit var=\"skillId\" width=80></td></tr>");
		replyMSG.append("<tr><td>Level</td><td><edit var=\"level\" width=80></td></tr>");
		replyMSG.append("</table>");

		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc " + npcData.getNpcId() + " $skillId $level\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("<br><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcData.getNpcId() + "\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());

		activeChar.sendPacket(adminReply);
	}

	
	private static void showNpcSkillEdit(L2PcInstance activeChar, int npcId, int skillId)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills WHERE npcid=" + npcId + " AND skillid=" + skillId);
			ResultSet skillData = statement.executeQuery();

			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			StringBuffer replyMSG = new StringBuffer("<html><title>(NPC:" + npcId + "&nbsp;SKILL:" + skillId + ")</title>");
			replyMSG.append("<body>");

			if (skillData.next())
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillData.getInt("skillid"), skillData.getInt("level"));

				replyMSG.append("<table>");
				replyMSG.append("<tr><td>NPC</td><td>" + NpcTable.getInstance().getTemplate(skillData.getInt("npcid")).getName() + "</td></tr>");
				replyMSG.append("<tr><td>SKILL</td><td>" + skill.getName() + "(" + skillData.getInt("skillid") + ")</td></tr>");
				replyMSG.append("<tr><td>Lv(" + skill.getLevel() + ")</td><td><edit var=\"level\" width=50></td></tr>");
				replyMSG.append("</table>");

				replyMSG.append("<center>");
				replyMSG.append("<button value=\"Edit Skill\" action=\"bypass -h admin_edit_skill_npc " + npcId + " " + skillId + " $level\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
				replyMSG.append("<br><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\"  width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
				replyMSG.append("</center>");
			}

			skillData.close();
			statement.close();

			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());

			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private static void showNpcSkillList(L2PcInstance activeChar, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
			return;

		Map<Integer, L2Skill> skills = new HashMap<>();
		if (npcData.getSkills() != null)
		{
			skills = npcData.getSkills();
		}

		int _skillsize = Integer.valueOf(skills.size());

		int MaxSkillsPerPage = 10;
		int MaxPages = _skillsize / MaxSkillsPerPage;
		if (_skillsize > MaxSkillsPerPage * MaxPages)
		{
			MaxPages++;
		}

		if (page > MaxPages)
		{
			page = MaxPages;
		}

		int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = _skillsize;
		if (SkillsEnd - SkillsStart > MaxSkillsPerPage)
		{
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("");
		replyMSG.append("<html><title>" + npcData.getName() + " Skillist");
		replyMSG.append("&nbsp;(ID:" + npcData.getNpcId() + "&nbsp;Skills " + _skillsize + ")</title>");
		replyMSG.append("<body>");
		String pages = "<center><table width=270><tr>";
		for (int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			if (page == x)
			{
				pages += "<td>Page " + pagenr + "</td>";
			}
			else
			{
				pages += "<td><a action=\"bypass -h admin_show_skilllist_npc " + npcData.getNpcId() + " " + x + "\">Page " + pagenr + "</a></td>";
			}
		}
		pages += "</tr></table></center>";
		replyMSG.append(pages);

		replyMSG.append("<table width=270>");

		Set<?> skillset = skills.keySet();
		Iterator<?> skillite = skillset.iterator();
		Object skillobj = null;

		for (int i = 0; i < SkillsStart; i++)
			if (skillite.hasNext())
			{
				skillobj = skillite.next();
			}

		int cnt = SkillsStart;
		while (skillite.hasNext())
		{
			cnt++;
			if (cnt > SkillsEnd)
			{
				break;
			}
			skillobj = skillite.next();
			replyMSG.append("<tr><td><a action=\"bypass -h admin_edit_skill_npc " + npcData.getNpcId() + " " + skills.get(skillobj).getId() + "\">" + skills.get(skillobj).getName() + "&nbsp;[" + skills.get(skillobj).getId() + "]" + "</a></td>" + "<td>" + skills.get(skillobj).getLevel() + "</td>" + "<td><a action=\"bypass -h admin_del_skill_npc " + npcData.getNpcId() + " " + skillobj + "\">Delete</a></td></tr>");

		}
		replyMSG.append("</table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("<button value=\"Droplist\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\">");
		replyMSG.append("</center></body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);

	}

	private static void showShop(L2PcInstance activeChar, int npcId)
	{
		final List<NpcBuyList> buyLists = BuyListTable.getInstance().getBuyListsByNpcId(npcId);
		if (buyLists.isEmpty())
		{
			activeChar.sendMessage("No buyLists found for id: " + npcId + ".");
			return;
		}

		final StringBuilder replyMSG = new StringBuilder();
		StringUtil.append(replyMSG, "<html><title>Merchant Shop Lists</title><body>");

		if (activeChar.getTarget() instanceof L2MerchantInstance)
		{
			L2Npc merchant = (L2Npc) activeChar.getTarget();
			int taxRate = merchant.getCastle().getTaxPercent();

			StringUtil.append(replyMSG, "<center><font color=\"LEVEL\">", merchant.getName(), " (", Integer.toString(npcId), ")</font></center><br>Tax rate: ", Integer.toString(taxRate), "%");
		}

		StringUtil.append(replyMSG, "<table width=\"100%\">");

		for (NpcBuyList buyList : buyLists)
		{
			StringUtil.append(replyMSG, "<tr><td><a action=\"bypass -h admin_show_shoplist ", String.valueOf(buyList.getListId()), " 1\">Buylist id: ", String.valueOf(buyList.getListId()), "</a></td></tr>");
		}

		StringUtil.append(replyMSG, "</table></body></html>");

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(0);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private static void showShopList(L2PcInstance activeChar, int listId)
	{
		final NpcBuyList buyList = BuyListTable.getInstance().getBuyList(listId);
		if (buyList == null)
		{
			activeChar.sendMessage("BuyList template is unknown for id: " + listId + ".");
			return;
		}

		final StringBuilder replyMSG = new StringBuilder();
		replyMSG.append("<html><body><center><font color=\"LEVEL\">");
		replyMSG.append(NpcTable.getInstance().getTemplate(buyList.getNpcId()).getName());
		replyMSG.append(" (");
		replyMSG.append(buyList.getNpcId());
		replyMSG.append(") buylist id: ");
		replyMSG.append(buyList.getListId());
		replyMSG.append("</font></center><br><table width=\"100%\"><tr><td width=200>Item</td><td width=80>Price</td></tr>");

		for (Product product : buyList.getProducts())
		{
			replyMSG.append("<tr><td>");
			replyMSG.append(product.getItem().getName());
			replyMSG.append("</td><td>");
			replyMSG.append(product.getPrice());
			replyMSG.append("</td></tr>");
		}
		replyMSG.append("</table></body></html>");

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(0);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	
	private static void updateDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("UPDATE droplist SET min=?, max=?, chance=? WHERE mobId=? AND itemId=? AND category=?");
			statement.setInt(1, min);
			statement.setInt(2, max);
			statement.setInt(3, chance);
			statement.setInt(4, npcId);
			statement.setInt(5, itemId);
			statement.setInt(6, category);

			statement.execute();
			statement.close();

			PreparedStatement statement2 = con.prepareStatement("SELECT mobId FROM droplist WHERE mobId=? AND itemId=? AND category=?");
			statement2.setInt(1, npcId);
			statement2.setInt(2, itemId);
			statement2.setInt(3, category);

			ResultSet npcIdRs = statement2.executeQuery();
			if (npcIdRs.next())
			{
				npcId = npcIdRs.getInt("mobId");
			}
			npcIdRs.close();
			statement2.close();

			if (npcId > 0)
			{
				reLoadNpcDropList(npcId);

				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuilder replyMSG = new StringBuilder("<html><title>Drop data modify complete!</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\"></center>");
				replyMSG.append("</body></html>");

				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "unknown error!");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	private static void updateNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		Connection con = null;

		try
		{
			L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
			if (skillData == null)
			{
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Update Npc Skill Data</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\"></center>");
				replyMSG.append("</body></html>");

				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
				return;
			}

			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("UPDATE npcskills SET level=? WHERE npcid=? AND skillid=?");
			statement.setInt(1, level);
			statement.setInt(2, npcId);
			statement.setInt(3, skillId);

			statement.execute();
			statement.close();

			if (npcId > 0)
			{
				reLoadNpcSkillList(npcId);

				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Update Npc Skill Data</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=22 back=\"L2UI_CH3.bigbutton_over\" fore=\"L2UI_CH3.bigbutton\"></center>");
				replyMSG.append("</body></html>");

				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Unknown error");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public String[] getCommandList()
	{
		return commands;
	}

	@Override
	public void runCommand(L2PcInstance activeChar, String... params)
	{
		if (activeChar == null)
			return;

		String command = params[0];
		for (int x = 1; x < params.length; x++)
		{
			command += " " + params[x];
		}

		if (command.startsWith("showShop "))
		{
			String[] args = command.split(" ");
			if (args.length > 1)
			{
				showShop(activeChar, Integer.parseInt(command.split(" ")[1]));
			}
		}
		else if (command.startsWith("showShopList "))
		{
			String[] args = command.split(" ");
			if (args.length > 2)
			{
				showShopList(activeChar, Integer.parseInt(command.split(" ")[1]));
			}
		}
		else if (command.startsWith("edit_npc "))
		{
			try
			{
				String[] commandSplit = command.split(" ");
				int npcId = Integer.valueOf(commandSplit[1]);
				int pageId = 1;

				try
				{
					pageId = Integer.valueOf(commandSplit[2]);
				}
				catch (Exception e)
				{
				}

				L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
				Show_Npc_Property(activeChar, npc, pageId);
			}
			catch (Exception e)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Wrong usage: //edit_npc <npcId> [<pageId>]");
			}
		}
		else if (command.startsWith("load_npc"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			int id = 0;
			try
			{
				id = Integer.parseInt(st.nextToken());
			}
			catch (Exception e)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //load_npc <id>");
			}
			if (id > 0)
				if (NpcTable.getInstance().reloadNpc(id))
				{
					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Loaded NPC " + id);
				}
				else
				{
					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Error while loading NPC " + id);
				}
		}
		else if (command.startsWith("show_droplist "))
		{
			int npcId = 0;
			try
			{
				npcId = Integer.parseInt(command.substring(14).trim());
			}
			catch (Exception e)
			{
			}

			if (npcId > 0)
			{
				showNpcDropList(activeChar, npcId);
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //show_droplist <npc_id>");
			}
		}
		else if (command.startsWith("save_npc "))
		{
			try
			{
				save_npc_property(activeChar, command);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("show_skilllist_npc "))
		{
			StringTokenizer st = new StringTokenizer(command.substring(19), " ");
			try
			{
				int npcId = -1;
				int page = 0;
				if (st.countTokens() <= 2)
				{
					if (st.hasMoreTokens())
					{
						npcId = Integer.parseInt(st.nextToken());
					}
					if (st.hasMoreTokens())
					{
						page = Integer.parseInt(st.nextToken());
					}
				}

				if (npcId > 0)
				{
					showNpcSkillList(activeChar, npcId, page);
				}
				else
				{
					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //show_skilllist_npc <npc_id> <page>");
				}
			}
			catch (Exception e)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //show_skilllist_npc <npc_id> <page>");
			}
		}
		else if (command.startsWith("edit_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(15).trim(), " ");
				if (st.countTokens() == 2)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						showNpcSkillEdit(activeChar, npcId, skillId);
					}
					catch (Exception e)
					{
					}
				}
				else if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						int level = Integer.parseInt(st.nextToken());

						updateNpcSkillData(activeChar, npcId, skillId, level);
					}
					catch (Exception e)
					{
						_log.warn("edit_skill_npc parements error: " + command);
					}
				}
				else
				{
					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
			}
		}
		else if (command.startsWith("add_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(14).trim(), " ");
				if (st.countTokens() == 1)
				{
					try
					{
						String[] input = command.substring(14).split(" ");
						if (input.length < 1)
							return;
						npcId = Integer.parseInt(input[0]);
					}
					catch (Exception e)
					{
					}

					if (npcId > 0)
					{
						L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
						showNpcSkillAdd(activeChar, npcData);
					}
				}
				else if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						int level = Integer.parseInt(st.nextToken());

						addNpcSkillData(activeChar, npcId, skillId, level);
					}
					catch (Exception e)
					{
						_log.warn("add_skill_npc parements error: " + command);
					}
				}
				else
				{
					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //add_skill_npc <npc_id> [<level>]");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //add_skill_npc <npc_id> [<level>]");
			}
		}
		else if (command.startsWith("del_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				String[] input = command.substring(14).split(" ");
				if (input.length >= 2)
				{
					npcId = Integer.parseInt(input[0]);
					skillId = Integer.parseInt(input[1]);
				}
			}
			catch (Exception e)
			{
			}

			if (npcId > 0)
			{
				deleteNpcSkillData(activeChar, npcId, skillId);
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //del_skill_npc <npc_id> <skill_id>");
			}
		}
		else if (command.startsWith("edit_drop "))
		{
			int npcId = -1, itemId = 0, category = -1000;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(16).trim());
				if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						showEditDropData(activeChar, npcId, itemId, category);
					}
					catch (Exception e)
					{
					}
				}
				else if (st.countTokens() == 6)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						int min = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());
						int chance = Integer.parseInt(st.nextToken());

						updateDropData(activeChar, npcId, itemId, min, max, category, chance);
					}
					catch (Exception e)
					{
						_log.debug("edit_drop parements error: " + command);
					}
				}
				else
				{
					activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
			}
		}
		else if (command.startsWith("add_drop "))
		{

			int npcId = -1;
			try
			{
				String[] args = command.split(" ");
				if (args.length == 2)
				{
					try
					{
						npcId = Integer.parseInt(args[1]);
					}
					catch (Exception e)
					{
					}

					if (npcId > 0)
					{
						L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
						showAddDropData(activeChar, npcData);
					}
				}
				else
				{
					try
					{
						npcId = Integer.parseInt(args[1]);
						int itemId = Integer.parseInt(args[2]);
						int category = Integer.parseInt(args[3]);
						int min = Integer.parseInt(args[4]);
						int max = Integer.parseInt(args[5]);
						int chance = Integer.parseInt(args[6]);

						addDropData(activeChar, npcId, itemId, min, max, category, chance);
					}
					catch (Exception e)
					{
						activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
					}
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
			}
		}
		else if (command.startsWith("del_drop "))
		{
			int npcId = -1, itemId = -1, category = -1000;
			try
			{
				String[] input = command.substring(9).split(" ");
				if (input.length >= 3)
				{
					npcId = Integer.parseInt(input[0]);
					itemId = Integer.parseInt(input[1]);
					category = Integer.parseInt(input[2]);
				}
			}
			catch (Exception e)
			{
			}

			if (npcId > 0)
			{
				deleteDropData(activeChar, npcId, itemId, category);
			}
			else
			{
				activeChar.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Usage: //del_drop <npc_id> <item_id> <category>");
			}
		}
		return;
	}
}
package com.dream.game.util;

import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.datatables.sql.CharNameTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class PcAction
{
	private static Logger _log = Logger.getLogger(PcAction.class.getName());

	
	public static void addHeroStatus(L2PcInstance player, int days)
	{
		if (player == null)
			return;

		Connection con = null;
		try
		{

			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.DAY_OF_MONTH, days);

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE character_herolist SET enddate = ? WHERE charId = ?");
			statement.setLong(1, finishtime.getTimeInMillis());
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
			player.setHero(true);
			player.broadcastUserInfo();
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_YOU_ARE_HERO_NOW));
		}
		catch (SQLException e)
		{
			_log.warn("Hero Service:  Could not increase data");
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void addItem(L2PcInstance playable, int[][] items, String desc)
	{
		for (int item[] : items)
		{
			playable.addItem(desc, item[0], item[1], null, true);
		}
	}

	
	public static void admGiveHero(L2PcInstance player, boolean delete)
	{
		if (player == null)
			return;

		Connection con = null;
		try
		{
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.MONTH, 4);

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE character_herolist SET enddate = ? WHERE charId = ?");
			statement.setLong(1, delete ? 0 : finishtime.getTimeInMillis());
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static boolean changeName(L2PcInstance activeChar, String _name, String oldName)
	{
		L2PcInstance character = activeChar;

		if (character == null)
			return false;

		if (CharNameTable.getInstance().doesCharNameExist(_name.toLowerCase()))
		{
			character.sendMessage(Message.getMessage(character, Message.MessageId.MSG_NICK_EXIST));
			return false;
		}
		else if (!Util.isValidPlayerName(_name))
		{
			character.sendMessage(Message.getMessage(character, Message.MessageId.MSG_WRONG_NICK));
			return false;
		}
		L2World.getInstance().removeFromAllPlayers(character);
		character.changeName(_name);
		character.store();
		L2World.getInstance().addToAllPlayers(character);
		character.broadcastUserInfo();
		if (character.isInParty())
		{
			character.getParty().refreshPartyView();
		}
		if (character.getClan() != null)
		{
			character.getClan().broadcastClanStatus();
		}

		character.sendMessage(Message.getMessage(character, Message.MessageId.MSG_NICK_CHANGED));
		return true;
	}

	public static void changeTitle(L2PcInstance activeChar, String _title, String oldTitle)
	{
		L2PcInstance character = activeChar;

		if (character == null)
			return;

		if (!Util.isValidPlayerName(_title))
		{
			character.sendMessage(Message.getMessage(character, Message.MessageId.MSG_WRONG_TITLE));
			return;
		}
		character.setTitle(_title);
		character.sendPacket(SystemMessageId.TITLE_CHANGED);
		character.broadcastTitleInfo();
	}

	public static void clearRestartTask()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement("DELETE FROM global_tasks WHERE task = ?");
			statement.setString(1, "restart");
			statement.executeUpdate();
			statement.close();
			statement = con.prepareStatement("DELETE FROM global_tasks WHERE task = ?");
			statement.setString(1, "restartLogin");
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Could not delete restart task:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static synchronized void deleteHeroItems(L2PcInstance player)
	{
		L2ItemInstance[] items;
		InventoryUpdate iu;

		if (player == null)
			return;

		try
		{
			items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
			iu = new InventoryUpdate();
			for (L2ItemInstance item : items)
			{
				iu.addModifiedItem(item);
			}
			player.sendPacket(iu);

			items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
			iu = new InventoryUpdate();
			for (L2ItemInstance item : items)
			{
				iu.addModifiedItem(item);
			}
			player.sendPacket(iu);

			items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_HAIR);
			iu = new InventoryUpdate();
			for (L2ItemInstance item : items)
			{
				iu.addModifiedItem(item);
			}
			player.sendPacket(iu);

			items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_FACE);
			iu = new InventoryUpdate();
			for (L2ItemInstance item : items)
			{
				iu.addModifiedItem(item);
			}
			player.sendPacket(iu);

			for (L2ItemInstance item : player.getInventory().getAvailableItems(false))
			{
				if (item == null)
				{
					continue;
				}

				if (!item.isHeroItem())
				{
					continue;
				}

				player.destroyItem("HeroService", item, null, true);
				iu = new InventoryUpdate();
				iu.addRemovedItem(item);
				player.sendPacket(iu);
			}
			player.sendPacket(new UserInfo(player));
			player.broadcastUserInfo();
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	public static int getItemCount(L2Playable playable, int item_id)
	{
		return playable.getInventory().getItemByItemId(item_id) != null ? playable.getInventory().getItemByItemId(item_id).getCount() : 0;
	}

	public static void giveItems(L2PcInstance player, int itemId, int count)
	{
		L2PcInstance character = player;
		if (character == null)
			return;

		giveItems(character, itemId, count, 0);
	}

	public static void giveItems(L2PcInstance player, int itemId, int count, int enchantlevel)
	{
		L2PcInstance character = player;

		if (character == null)
			return;

		if (count <= 0)
			return;

		L2ItemInstance item = character.getInventory().addItem("PcAction", itemId, count, character, character.getTarget());

		if (item == null)
			return;
		if (enchantlevel > 0)
		{
			item.setEnchantLevel(enchantlevel);
		}
		if (itemId == 57)
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			msg.addNumber(count);
			character.sendPacket(msg);
		}
		else if (count > 1)
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			msg.addItemName(item);
			msg.addNumber(count);
			character.sendPacket(msg);
		}
		else
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1);
			msg.addItemName(item);
			character.sendPacket(msg);
		}
		character.getInventory().updateInventory(item);
	}

	public static boolean haveCountItem(L2PcInstance player, int id, int count)
	{
		return getItemCount(player, id) >= count;
	}

	public static boolean haveItem(L2Playable playable, int item_id, int count, boolean sendMessage)
	{
		long cnt = count - getItemCount(playable, item_id);
		if (cnt > 0)
		{
			if (sendMessage)
			{
				playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			return false;
		}
		return true;
	}

	public static boolean haveItem(L2Playable playable, int[] item, boolean sendMessage)
	{
		return haveItem(playable, item[0], item[1], sendMessage);
	}

	public static boolean haveItem(L2Playable playable, int[][] items, boolean sendMessage)
	{
		for (int item[] : items)
			if (!haveItem(playable, item, sendMessage))
				return false;

		return true;
	}

	public static void playSound(L2PcInstance player, String sound)
	{
		L2PcInstance character = player;

		if (character == null)
			return;

		character.sendPacket(new PlaySound(sound));
	}

	public static boolean removeItem(L2PcInstance player, int id, int count, String desc)
	{
		return player.destroyItemByItemId(desc, id, count, null, true);
	}

	public static boolean removeItem(L2PcInstance player, int[] item, String desc)
	{
		return removeItem(player, item[0], item[1], desc);
	}

	public static void removeItem(L2PcInstance playable, int[][] items, String desc)
	{
		for (int item[] : items)
		{
			removeItem(playable, item, desc);
		}
	}

	public static void spawnManager()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(50014);
		try
		{
			L2Spawn _npcSpawn;
			_npcSpawn = new L2Spawn(tmpl);
			_npcSpawn.setLocx(147462);
			_npcSpawn.setLocy(22384);
			_npcSpawn.setLocz(-1941);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(0);
			_npcSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle("Wedding Manager");
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
		}
		catch (Exception e)
		{
			_log.info("Wedding Instance [Can't spawn NPC]: exception: " + e.getMessage());
		}
	}

	
	public static void storeCharSex(L2PcInstance player, int mode)
	{
		L2PcInstance character = player;

		if (character == null)
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET sex = ? WHERE charId = ?");
			statement.setInt(1, character.getAppearance().getSex() ? 1 : 0);
			statement.setInt(2, character.getObjectId());
			statement.execute();
			statement.close();
			if (mode == 0)
			{
				character.sendMessage(Message.getMessage(character, Message.MessageId.MSG_SEX_CHANGE));
			}
		}
		catch (SQLException e)
		{
			_log.warn("StoreSex:  Could not save data");
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
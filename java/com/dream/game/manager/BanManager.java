package com.dream.game.manager;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.Announcements;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.restriction.AvailableRestriction;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.model.restriction.RestrictionBindClassException;
import com.dream.game.model.world.L2World;
import com.dream.game.network.AuthServerThread;
import com.dream.game.network.Disconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

public class BanManager
{
	public static Logger _logBan = Logger.getLogger("ban");

	private static BanManager _instance = null;

	private static void announce(String message)
	{
		if (Config.CLASSIC_ANNOUNCE_MODE)
		{
			Announcements.getInstance().announceToAll(message);
		}
		else
		{
			Announcements.getInstance().criticalAnnounceToAll(message);
		}
	}

	
	private static boolean banPlayer(String player, boolean ban)
	{
		boolean result = true;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET isBanned=? WHERE LOWER(char_name)=?");
			statement.setInt(1, ban ? 1 : 0);
			statement.setString(2, player.toLowerCase());
			statement.execute();
			if (statement.getUpdateCount() == 0)
			{
				result = false;
			}
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			result = false;
		}

		return result;
	}

	public static BanManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new BanManager();
		}
		return _instance;
	}

	public boolean banAccount(L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
			return false;

		if (activeChar == player)
			return false;

		String plName = player.getName();
		String acName = player.getAccountName();

		player.setAccountAccesslevel(-100);
		try
		{
			if (player.isOfflineTrade())
			{
				player.setOfflineTrade(false);
				player.standUp();
			}
			new Disconnection(player).defaultSequence(false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		if (activeChar != null)
		{
			activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_BAN_ACC_BREAK_RULE), acName));
			if (Config.BAN_ACCOUNT_LOG)
			{
				_logBan.info("Моderator [" + activeChar.getName() + "] unban account [" + acName + "], that belongs to the player [" + plName + "].");
			}
		}
		if (Config.ANNOUNCE_BAN_ACCOUNT)
		{
			announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_ACCOUNT_BLOCK), acName));
		}
		announce("Banned account " + acName + ". Breaking the rules");
		return true;
	}

	public boolean banAccount(L2PcInstance activeChar, String account)
	{
		if (activeChar == null)
			return false;

		try
		{
			AuthServerThread.getInstance().sendAccessLevel(account, -100);
			if (Config.BAN_ACCOUNT_LOG)
			{
				_logBan.info("Moderator [" + activeChar.getName() + "] banned account [" + account + "].");
			}
			if (Config.ANNOUNCE_UNBAN_ACCOUNT)
			{
				announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_ACCOUNT_UNBLOCK), account));
			}
			activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_BAN_ACC_BREAK_RULE), account));
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	public boolean banChar(L2PcInstance admin, String target)
	{
		if (admin == null || target == null)
			return false;

		L2PcInstance player = L2World.getInstance().getPlayer(target);
		if (player != null)
		{
			if (!player.banChar())
				return false;
		}
		else if (!banPlayer(target, true))
			return false;
		if (Config.BAN_CHAR_LOG)
		{
			_logBan.info("Gm " + admin.getName() + " locked character [" + target + "].");
		}
		if (Config.ANNOUNCE_BAN_CHAR)
		{
			announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAR_BLOCKED), target));
		}
		admin.sendMessage(String.format(Message.getMessage(admin, Message.MessageId.MSG_CHAR_SUCCESS_BAN), target));
		return true;
	}

	public boolean banChat(L2PcInstance activeChar, L2PcInstance player, String reason, int time)
	{
		if (activeChar == null || player == null || activeChar == player)
			return false;
		String message = null;
		if (time > 0)
		{
			message = String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAT_BLOCK_WITH_REASON), player.getName(), time, reason);
			try
			{
				ObjectRestrictions.getInstance().addRestriction(player, AvailableRestriction.PlayerChat);
				ObjectRestrictions.getInstance().timedRemoveRestriction(player.getObjectId(), AvailableRestriction.PlayerChat, time * 60000, player.getName() + " , You unlocked the chat");
			}
			catch (RestrictionBindClassException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			message = String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAT_BLOCK_FOREVER_WITH_REASON), player.getName(), reason);
			try
			{
				ObjectRestrictions.getInstance().addRestriction(player, AvailableRestriction.PlayerChat);
			}
			catch (RestrictionBindClassException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		if (Config.BAN_CHAT_LOG)
		{
			_logBan.info("Moderator [" + activeChar.getName() + "] banned chat player [" + player.getName() + (time > 0 ? "] on " + time + " minutes." : "] indefinited!"));
		}
		if (Config.ANNOUNCE_BAN_CHAT)
		{
			announce(message);
		}
		if (player._event != null)
		{
			player._event.remove(player);
		}
		String inf = time > 0 ? " for " + time + " minutes." : " forever!";
		activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_CHAT_BLOCK_FOR), player.getName() + inf));
		player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_YOUR_CHAT_BLOCK_FOR), time));
		return true;
	}

	public boolean banChat(String censor, L2PcInstance player, int time, String type)
	{
		if (player == null)
			return false;

		try
		{
			ObjectRestrictions.getInstance().addRestriction(player, AvailableRestriction.PlayerChat);
			ObjectRestrictions.getInstance().timedRemoveRestriction(player.getObjectId(), AvailableRestriction.PlayerChat, time * 60000, "chat lock is removed.");
		}
		catch (RestrictionBindClassException e)
		{
			return false;
		}

		_logBan.info("Censor [" + censor + "] banned chat player [" + player.getName() + "] on " + time + " minutes, the reason: " + type + ".");

		announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAR_CHAT_BLOCK), player.getName(), time));
		announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_BAN_REASON), type));
		return true;
	}

	public boolean banChatAll(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return false;

		try
		{
			ObjectRestrictions.getInstance().addGlobalRestriction(AvailableRestriction.GlobalPlayerChat);
			ObjectRestrictions.getInstance().timedRemoveRestriction(0, AvailableRestriction.GlobalPlayerChat, Config.GLOBAL_BAN_CHAT_TIME * 60000, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		if (Config.BAN_CHAT_LOG)
		{
			_logBan.info("Moderator [" + activeChar.getName() + "] established global lock chat " + Config.GLOBAL_BAN_CHAT_TIME + " minutes.");
		}
		announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_ADMIN_BLOCK_ALL_CHAT), Config.GLOBAL_BAN_CHAT_TIME));
		activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_ALL_CHAT_BLOCK), Config.GLOBAL_BAN_CHAT_TIME));
		return true;
	}

	public boolean gmAccess(L2PcInstance admin, String target)
	{
		if (admin == null || target == null)
			return false;

		try
		{
			AuthServerThread.getInstance().sendAccessLevel(target, 1);
			admin.sendMessage(String.format(Message.getMessage(admin, Message.MessageId.MSG_ACC_RECIVE_GM_ACCESS), target));
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@SuppressWarnings("null")
	public boolean jailPlayer(L2PcInstance activeChar, L2PcInstance player, int time, boolean auto)
	{
		if (!auto && activeChar == null)
			return false;
		if (player == null)
			return false;

		String moderator = activeChar == null ? "ServerGuard" : activeChar.getName();
		try
		{
			String inf = time > 0 ? " for " + time + " minutes." : " forever!";
			if (player._event != null)
			{
				player._event.remove(player);
			}
			player.setInJail(true, time);
			if (Config.JAIL_LOG)
			{
				_logBan.info("Моderator [" + moderator + "] posted in player [" + player.getName() + (time > 0 ? "] on " + time + " minutes." : "] forever!"));
			}
			if (Config.ANNOUNCE_JAIL)
			{
				announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAR_JAIL), player.getName(), player.getName(), inf));
			}
			if (!auto)
			{
				activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_SEND_IN_JAIL), player.getName() + inf));
			}
			player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_ADMIN_PUT_YOU_IN_JAIL), inf));
		}
		catch (NoSuchElementException e)
		{
			if (!auto)
			{
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_SET_ALL_ARG));
			}
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	public boolean jailPlayer(L2PcInstance activeChar, String player, int time)
	{
		if (activeChar == null)
			return false;

		boolean result = true;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE LOWER(char_name)=?");
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, 1);
			statement.setLong(5, time * 60000L);
			statement.setString(6, player.toLowerCase());
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();

			if (count == 0)
			{
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_CHAR_NOT_FOUND_IN_BASE));
				result = false;
			}
			else
			{
				String inf = time > 0 ? " for " + time + " minutes." : " forever!";
				if (Config.JAIL_LOG)
				{
					_logBan.info("Моderator [" + activeChar.getName() + "] posted in player [" + player + (time > 0 ? "(Offline)] on " + time + " minutes." : "(Offline)] forever!"));
				}
				if (Config.ANNOUNCE_JAIL)
				{
					announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAR_JAIL), player, time > 0 ? "for " + time + " minutes." : " forever!"));
				}
				activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_SEND_IN_JAIL), player + inf));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			result = false;
		}

		return result;
	}

	public boolean unBanAccount(L2PcInstance activeChar, String account)
	{
		if (activeChar == null)
			return false;

		try
		{
			AuthServerThread.getInstance().sendAccessLevel(account, 0);
			if (Config.BAN_ACCOUNT_LOG)
			{
				_logBan.info("Моderator [" + activeChar.getName() + "] removed the ban from your account [" + account + "].");
			}
			if (Config.ANNOUNCE_UNBAN_ACCOUNT)
			{
				announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_ACCOUNT_UNBLOCK), account));
			}
			activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_UNBAN_ACC), account));
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	public boolean unBanChar(L2PcInstance admin, String target)
	{
		if (admin == null || target == null)
			return false;
		if (!banPlayer(target, false))
			return false;
		if (Config.BAN_CHAR_LOG)
		{
			_logBan.info("Gm " + admin.getName() + " unblocked character [" + target + "].");
		}
		if (Config.ANNOUNCE_UNBAN_CHAR)
		{
			announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAR_UN_BLOCKED), target));
		}
		admin.sendMessage(String.format(Message.getMessage(admin, Message.MessageId.MSG_CHAR_SUCCESS_UNBAN), target));
		return true;
	}

	public boolean unBanChat(L2PcInstance activeChar, L2PcInstance player)
	{
		if (activeChar == null || player == null)
			return false;

		try
		{
			ObjectRestrictions.getInstance().removeRestriction(player, AvailableRestriction.PlayerChat);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		if (Config.BAN_CHAT_LOG)
		{
			_logBan.info("Moderator [" + activeChar.getName() + "] unban chat player [" + player.getName() + "].");
		}
		if (Config.ANNOUNCE_BAN_CHAT)
		{
			announce(String.format(Message.getMessage(null, Message.MessageId.ANNOUNCE_CHAR_CHAT_UNBLOCK), player.getName()));
		}
		activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_CHAT_UNBLOCK), player.getName()));
		player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_YOUR_CHAT_UNBLOCK));
		return true;
	}

	public boolean unBanChatAll(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return false;

		try
		{
			ObjectRestrictions.getInstance().removeGlobalRestriction(AvailableRestriction.GlobalPlayerChat);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		if (Config.BAN_CHAT_LOG)
		{
			_logBan.info("Moderator [" + activeChar.getName() + "] removed the global lock chat.");
		}
		announce(Message.getMessage(null, Message.MessageId.ANNOUNCE_ADMIN_UNBLOCK_ALL_CHAT));
		activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_ALL_CHAT_UNBLOCK));
		return true;
	}

	public boolean unJailPlayer(L2PcInstance activeChar, L2PcInstance player)
	{
		if (activeChar == null || player == null)
			return false;

		try
		{
			player.setInJail(false, 0);
			if (Config.JAIL_LOG)
			{
				_logBan.info("Moderator [" + activeChar.getName() + "] a player released [" + player.getName() + "] from jail.");
			}
			if (Config.ANNOUNCE_UNJAIL)
			{
				announce(String.format(Message.getMessage(null, Message.MessageId.MSG_RELEASE_FROM_JAIL), player.getName()));
			}
			activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_RELEASE_FROM_JAIL), player.getName()));
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_YOU_RELEASED_FROM_JAIL));
		}
		catch (NoSuchElementException e)
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_TARGET_NOT_FOUND));
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	public boolean unJailPlayer(L2PcInstance activeChar, String player)
	{
		if (activeChar == null)
			return false;

		boolean result = true;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE LOWER(char_name)=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, player.toLowerCase());
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
			if (count == 0)
			{
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_CHAR_NOT_FOUND_IN_BASE));
				result = false;
			}
			else
			{
				if (Config.JAIL_LOG)
				{
					_logBan.info("Моderator [" + activeChar.getName() + "] a player released [" + player + "(Offline)] from jail.");
				}

				if (Config.ANNOUNCE_UNJAIL)
				{
					announce(String.format(Message.getMessage(null, Message.MessageId.MSG_RELEASE_FROM_JAIL), player));
				}

				activeChar.sendMessage(String.format(Message.getMessage(activeChar, Message.MessageId.MSG_RELEASE_FROM_JAIL), player));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			result = false;
		}

		return result;
	}
}
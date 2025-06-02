package com.dream.game.network.clientpackets;

import com.dream.L2DatabaseFactory;
import com.dream.game.access.gmController;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.cache.HtmCache;
import com.dream.game.communitybbs.CommunityBoard;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.handler.IExItemHandler;
import com.dream.game.handler.IItemHandler;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.handler.ItemHandler;
import com.dream.game.handler.VoicedCommandHandler;
import com.dream.game.manager.BotsPreventionManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.model.entity.events.GameEventManager;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.GMViewPledgeInfo;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.tokenizer.CommandTokenizer;
import com.dream.tools.security.Base64;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestBypassToServer extends L2GameClientPacket
{
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}
	
	private static void playerHelp(L2PcInstance activeChar, String path)
	{
		if (path.indexOf("..") != -1)
			return;
		
		final StringTokenizer st = new StringTokenizer(path);
		final String[] cmd = st.nextToken().split("#");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/help/" + cmd[0]);
		if (cmd.length > 1)
		{
			html.setItemId(Integer.parseInt(cmd[1]));
		}
		html.disableValidation();
		activeChar.sendPacket(html);
	}
	
	private String _command = null;
	private static Connection con;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		if (activeChar.isDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		activeChar._bbsMultisell = 0;
		if (_command == null)
			return;
		if (_command.startsWith("admin_"))
		{
			if (activeChar.isGM())
			{
				gmController.getInstance().useCommand(activeChar, _command.substring(6).split(" "));
			}
			else
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			
		}
		else if (_command.startsWith("farm"))
		{
			final CommandTokenizer tokenizer = new CommandTokenizer(_command);
			final String param = tokenizer.getToken(1);
			switch (param.toLowerCase())
			{
				case "on":
					
					if (!activeChar.isAutoFarm())
					{
						activeChar.setAutoFarm(true);
						activeChar.sendMessage("Autofarm active.");
					}
					break;
				
				case "off":
					if (activeChar.isAutoFarm())
					{
						activeChar.setAutoFarm(false);
						activeChar.sendMessage("Autofarm stop.");
					}
					break;
			}
		}
		else if (_command.startsWith("item_"))
		{
			Pattern p = Pattern.compile("item_(\\d+) ?(.?+)");
			Matcher m = p.matcher(_command);
			if (m.find())
			{
				int objId = Integer.parseInt(m.group(1));
				if (m.groupCount() > 1)
				{
					_command = m.group(2);
				}
				else
				{
					_command = null;
				}
				L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objId);
				if (item == null)
					return;
				if (_command == null)
				{
					IItemHandler h = ItemHandler.getInstance().getItemHandler(item.getItemId());
					if (h == null)
						return;
					h.useItem(activeChar, item);
				}
				else
				{
					IExItemHandler handler = ItemHandler.getInstance().getExHandler(item.getItemId());
					if (handler == null)
						return;
					handler.useItem(activeChar, item, _command.split(" "));
				}
			}
			return;
		}
		else if (_command.startsWith("voice_"))
		{
			String command;
			String params = "";
			
			if (_command.indexOf(" ") != -1)
			{
				command = _command.substring(6, _command.indexOf(" "));
				params = _command.substring(_command.indexOf(" ") + 1);
			}
			else
			{
				command = _command.substring(6);
			}
			
			IVoicedCommandHandler vc = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			
			if (vc == null)
				return;
			
			vc.useVoicedCommand(command, activeChar, params);
			return;
		}
		else if (_command.startsWith("event"))
		{
			String eventName = _command.substring(6);
			int i = eventName.indexOf(" ");
			String cmd = "";
			String param = "";
			if (i != -1)
			{
				cmd = eventName.substring(i + 1);
				eventName = eventName.substring(0, i);
			}
			i = cmd.indexOf(" ");
			if (i != -1)
			{
				param = cmd.substring(i + 1);
				cmd = cmd.substring(0, i);
			}
			GameEvent evt = GameEventManager.getInstance().findEvent(eventName);
			if (evt != null)
			{
				evt.onCommand(activeChar, cmd, param);
			}
			return;
		}
		else if (_command.equals("come_here") && activeChar.isGM())
		{
			comeHere(activeChar);
		}
		else if (_command.startsWith("show_clan_info "))
		{
			activeChar.sendPacket(new GMViewPledgeInfo(ClanTable.getInstance().getClanByName(_command.substring(15)), activeChar));
		}
		else if (_command.startsWith("player_help "))
		{
			playerHelp(activeChar, _command.substring(12));
		}
		else if (_command.equalsIgnoreCase("pkrecovery"))
		{
			String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/recovery.htm");
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml(htmContent);
			html.replace("%question%", getPassKeyQuestion(activeChar));
			activeChar.sendPacket(html);
			html = null;
		}
		else if (_command.startsWith("pkset"))
		{
			StringTokenizer st = new StringTokenizer(_command, "]");
			
			if (st.countTokens() != 5)
			{
				activeChar.sendMessage("You have not entered all the data!");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/setup.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
			
			@SuppressWarnings("unused")
			String newCommand = st.nextToken();
			String pass1 = st.nextToken();
			pass1 = pass1.substring(1, pass1.length() - 1);
			String pass2 = st.nextToken();
			pass2 = pass2.substring(1, pass2.length() - 1);
			String question = st.nextToken();
			question = question.substring(1, question.length() - 1);
			String answer = st.nextToken();
			answer = answer.substring(1, answer.length());
			
			if (pass1 == null || pass2 == null || question == null || answer == null)
			{
				activeChar.sendMessage("Input error");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/setup.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
			
			if (!pass1.equals(pass2))
			{
				activeChar.sendMessage("You entered different passwords");
				activeChar.sendMessage("pass1 = " + pass1);
				activeChar.sendMessage("pass2 = " + pass2);
				activeChar.sendMessage("Question = " + question);
				activeChar.sendMessage("answer = " + answer);
				
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/setup.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
			insertPassKeyInformation(activeChar, pass1, question, answer);
			
			String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/login.htm");
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml(htmContent);
			html.replace("%player%", activeChar.getName());
			activeChar.sendPacket(html);
			html = null;
		}
		else if (_command.startsWith("pklogin"))
		{
			StringTokenizer st = new StringTokenizer(_command, " ");
			if (st.countTokens() != 2)
			{
				activeChar.sendMessage("You make a mistake when entering the password!");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/login.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
			@SuppressWarnings("unused")
			String newCommand = st.nextToken();
			String pass = st.nextToken();
			
			con = null;
			String query = "SELECT passkey FROM passkey WHERE charId = ?";
			String pwdindb = "error";
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query);
				ps.setInt(1, activeChar.getObjectId());
				ResultSet rs = ps.executeQuery();
				
				while (rs.next())
					pwdindb = rs.getString(1);
				
				rs.close();
				ps.close();
				ps = null;
				rs = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					con.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				con = null;
			}
			
			if (pwdindb.equals(encodePass(pass)))
			{
				activeChar.setTradeRefusal(false);
				activeChar.setExchangeRefusal(false);
				activeChar.setIsParalyzed(false);
				activeChar.setIsInvul(false);
				activeChar.setSecondRefusal(false);
			}
			else
			{
				activeChar.sendMessage("You have not entered the correct password.");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/login.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
		}
		else if (_command.startsWith("pkrec"))
		{
			StringTokenizer st = new StringTokenizer(_command, " ");
			if (st.countTokens() != 4)
			{
				activeChar.sendMessage("You make a mistake when entering data!");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/recovery.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%question%", getPassKeyQuestion(activeChar));
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
			
			@SuppressWarnings("unused")
			String newCommand = st.nextToken();
			String answer = st.nextToken();
			String pass1 = st.nextToken();
			String pass2 = st.nextToken();
			
			if (!pass1.equals(pass2))
			{
				activeChar.sendMessage("You entered different passwords.");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/recovery.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%question%", getPassKeyQuestion(activeChar));
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
			
			con = null;
			String query = "SELECT answer FROM passkey WHERE charId = ?";
			String anwindb = "error";
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query);
				ps.setInt(1, activeChar.getObjectId());
				ResultSet rs = ps.executeQuery();
				
				while (rs.next())
					anwindb = rs.getString(1);
				
				rs.close();
				ps.close();
				ps = null;
				rs = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					con.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				con = null;
			}
			
			if (anwindb.equals(answer))
			{
				updPassKey(activeChar, pass1);
				activeChar.sendMessage("You have successfully changed your password.");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/login.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
			}
			else
			{
				activeChar.sendMessage("You entered the wrong answer to your question");
				String htmContent = HtmCache.getInstance().getHtm("data/html/mods/passkey/recovery.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setHtml(htmContent);
				html.replace("%question%", getPassKeyQuestion(activeChar));
				html.replace("%player%", activeChar.getName());
				activeChar.sendPacket(html);
				html = null;
				return;
			}
			
		}
		else if (_command.startsWith("npc_"))
		{
			if (!activeChar.validateBypass(_command))
				return;
			
			int endOfId = _command.indexOf('_', 5);
			String id;
			if (endOfId > 0)
			{
				id = _command.substring(4, endOfId);
			}
			else
			{
				id = _command.substring(4);
			}
			
			try
			{
				L2Object object = null;
				int objectId = Integer.parseInt(id);
				
				if (activeChar.getTargetId() == objectId)
				{
					object = activeChar.getTarget();
				}
				if (object == null)
				{
					object = L2World.getInstance().findObject(objectId);
				}
				
				if (object instanceof L2Npc && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					try
					{
						((L2Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
					catch (NoSuchElementException nsee)
					{
						
					}
				}
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		
		else if (_command.equals("menu_select?ask=-16&reply=1"))
		{
			
			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
			{
				((L2Npc) object).onBypassFeedback(activeChar, _command);
			}
		}
		else if (_command.equals("menu_select?ask=-16&reply=2"))
		{
			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
			{
				((L2Npc) object).onBypassFeedback(activeChar, _command);
			}
		}
		
		else if (_command.startsWith("manor_menu_select?"))
		{
			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
			{
				((L2Npc) object).onBypassFeedback(activeChar, _command);
			}
		}
		else if (_command.startsWith("bbs_"))
		{
			CommunityBoard.getInstance().handleCommands(getClient(), _command);
		}
		else if (_command.startsWith("_bbs"))
		{
			CommunityBoard.getInstance().handleCommands(getClient(), _command);
		}
		else if (_command.startsWith("Quest "))
		{
			if (!activeChar.validateBypass(_command))
				return;
			
			String p = _command.substring(6).trim();
			int idx = p.indexOf(' ');
			if (idx < 0)
			{
				activeChar.processQuestEvent(p, "");
			}
			else
			{
				activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
			}
		}
		else if (_command.startsWith("OlympiadArenaChange"))
		{
			Olympiad.bypassChangeArena(_command, activeChar);
		}
		
		else if (_command.startsWith("report"))
		{
			BotsPreventionManager.getInstance().AnalyseBypass(_command, activeChar);
		}
	}
	
	private static void updPassKey(L2PcInstance player, String pass)
	{
		con = null;
		String query = "UPDATE passkey SET passkey = ? WHERE charId = ?";
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(query);
			st.setString(1, encodePass(pass));
			st.setInt(2, player.getObjectId());
			st.executeUpdate();
			st.close();
			st = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			con = null;
		}
	}
	
	private static String encodePass(String password)
	{
		String pass = "error";
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			pass = Base64.encodeBytes(hash);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return pass;
	}
	
	private static void insertPassKeyInformation(L2PcInstance player, String pass, String question, String answer)
	{
		con = null;
		String query = "INSERT INTO passkey (charId, passkey, question, answer) VALUES (?,?,?,?)";
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(query);
			st.setInt(1, player.getObjectId());
			st.setString(2, encodePass(pass));
			st.setString(3, question);
			st.setString(4, answer);
			st.execute();
			st.close();
			st = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			con = null;
		}
	}
	
	private static String getPassKeyQuestion(L2PcInstance player)
	{
		con = null;
		String query = "SELECT question FROM passkey WHERE charId = ?";
		String question = "error";
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(query);
			st.setInt(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			
			while (rs.next())
				question = rs.getString(1);
			
			rs.close();
			st.close();
			st = null;
			rs = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			con = null;
		}
		
		return question;
	}
	
	public static boolean getPassKeyEnable(L2PcInstance player)
	{
		con = null;
		String query = "SELECT COUNT(*) FROM passkey WHERE charId = ?";
		int count = 0;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(query);
			st.setInt(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			
			while (rs.next())
				count = rs.getInt(1);
			
			rs.close();
			st.close();
			st = null;
			rs = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			con = null;
		}
		
		if (count == 1)
			return true;
		return false;
	}
}
package com.dream.game.model.actor.instance;

import java.util.Calendar;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.xml.TeleportLocationTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.position.L2TeleportLocation;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.restriction.AvailableRestriction;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2TeleporterInstance extends L2NpcInstance
{
	private final static Logger _log = Logger.getLogger(L2TeleporterInstance.class.getName());
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;

	public L2TeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (SiegeManager.checkIfInZone(list.getLocX(), list.getLocY(), list.getLocZ()) && !player.isNoble())
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			if (TownManager.getInstance().townHasCastleInSiege(list.getLocX(), list.getLocY(), list.getLocZ()) && !player.isNoble())
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
				return;
			}
			if (list.isForNoble() && !player.isNoble())
			{
				String filename = "data/html/teleporter/nobleteleporter-no.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}

			if (player.isAlikeDead())
				return;

			int price = list.getPrice();

			if (!list.isForNoble() && price != 0)
			{
				Calendar cal = Calendar.getInstance();
				if (cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
				{
					price /= 2;
				}
			}

			if (!list.isForNoble() && (Config.FREE_TELEPORT && player.getLevel() >= Config.FREE_TELEPORT_MINLVL && player.getLevel() <= Config.FREE_TELEPORT_MAXLVL || player.reduceAdena("Teleport", list.getPrice(), this, true)))
			{
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
				if (Config.PLAYER_SPAWN_PROTECTION > 0 && !isInsidePeaceZone(player))
				{
					player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_SPAWN_PROTECTION), Config.PLAYER_SPAWN_PROTECTION));
				}
			}
			else if (list.isForNoble() && (Config.NOBLE_FREE_TELEPORT && player.getLevel() >= Config.NOBLE_FREE_TELEPORT_MINLVL && player.getLevel() <= Config.NOBLE_FREE_TELEPORT_MAXLVL || player.destroyItemByItemId("Noble Teleport", 6651, list.getPrice(), this, true)))
			{
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
				if (Config.PLAYER_SPAWN_PROTECTION > 0 && !isInsidePeaceZone(player))
				{
					player.sendMessage(String.format(Message.getMessage(player, Message.MessageId.MSG_SPAWN_PROTECTION), Config.PLAYER_SPAWN_PROTECTION));
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
		{
			filename = "" + npcId;
		}
		else
		{
			filename = npcId + "-" + val;
		}

		return "data/html/teleporter/" + filename + ".htm";
	}

	@Override
	public boolean canInteract(L2PcInstance player)
	{
		// Can't interact while casting a spell.
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			return false;

		// Can't interact while died.
		if (player.isDead() || player.isFakeDeath())
			return false;

		/* if (!Config.ALLOW_AIO_TELEPORT) { if (player.isAio()) return false; } */

		// Can't interact sitted.
		if (player.isSitting())
			return false;

		// Can't interact in shop mode, or during a transaction or a request.
		if (player.isInStoreMode() || player.isProcessingTransaction())
			return false;

		if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
			return false;

		return true;
	}

	@SuppressWarnings("null")
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (ObjectRestrictions.getInstance().checkRestriction(player, AvailableRestriction.PlayerTeleport))
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return;
		}

		int condition = validateCondition(player);

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();

		if (actualCommand.equalsIgnoreCase("goto"))
		{
			if (player.isImmobilized())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			int npcId = getTemplate().getNpcId();
			String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
			int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
			boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
			int compWinner = SevenSigns.getInstance().getCabalHighestScore();

			switch (npcId)
			{
				case 31103:
				case 31104:
				case 31105:
				case 31106:
				case 31107:
				case 31108:
				case 31109:
				case 31110:
				case 31120:
				case 31121:
				case 31122:
				case 31123:
				case 31124:
				case 31125:
					player.setIsIn7sDungeon(false);
					break;
				case 31095:
				case 31096:
				case 31097:
				case 31098:
				case 31099:
				case 31100:
				case 31101:
				case 31102:
				{
					boolean canPort = true;
					if (isSealValidationPeriod)
					{
						if (Config.ALT_STRICT_SEVENSIGNS)
							if (compWinner == SevenSigns.CABAL_DAWN && playerCabal != SevenSigns.CABAL_DAWN)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								canPort = false;
							}
							else if (compWinner == SevenSigns.CABAL_DUSK && playerCabal != SevenSigns.CABAL_DUSK)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								canPort = false;
							}
							else if (compWinner == SevenSigns.CABAL_NULL && playerCabal != SevenSigns.CABAL_NULL)
							{
								canPort = true;
							}
							else if (playerCabal == SevenSigns.CABAL_NULL)
							{
								canPort = false;
							}
					}
					else if (Config.ALT_STRICT_SEVENSIGNS)
						if (playerCabal == SevenSigns.CABAL_NULL)
						{
							canPort = false;
						}
					if (!canPort)
					{
						NpcHtmlMessage htmlNecro = new NpcHtmlMessage(getObjectId());
						filename += "necro_no.htm";
						htmlNecro.setFile(filename);
						player.sendPacket(htmlNecro);
						return;
					}
					player.setIsIn7sDungeon(true);
					break;
				}
				case 31114:
				case 31115:
				case 31116:
				case 31117:
				case 31118:
				case 31119:
				{
					boolean canPort = true;
					if (isSealValidationPeriod)
					{
						if (Config.ALT_STRICT_SEVENSIGNS)
							if (compWinner == SevenSigns.CABAL_DAWN && playerCabal != SevenSigns.CABAL_DAWN)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								canPort = false;
							}
							else if (compWinner == SevenSigns.CABAL_DUSK && playerCabal != SevenSigns.CABAL_DUSK)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								canPort = false;
							}
							else if (compWinner == SevenSigns.CABAL_NULL && playerCabal != SevenSigns.CABAL_NULL)
							{
								canPort = true;
							}
							else if (playerCabal == SevenSigns.CABAL_NULL)
							{
								canPort = false;
							}
					}
					else if (Config.ALT_STRICT_SEVENSIGNS)
						if (playerCabal == SevenSigns.CABAL_NULL)
						{
							canPort = false;
						}
					if (!canPort)
					{
						NpcHtmlMessage htmlCata = new NpcHtmlMessage(getObjectId());
						filename += "cata_no.htm";
						htmlCata.setFile(filename);
						player.sendPacket(htmlCata);
						return;
					}
					player.setIsIn7sDungeon(true);
					break;
				}
				case 35092:
				case 35134:
				case 35176:
				case 35218:
				case 35261:
				case 35308:
				case 35352:
				case 35497:
				case 35544:
				case 35093:
				case 35135:
				case 35177:
				case 35219:
				case 35262:
				case 35309:
				case 35353:
				case 35498:
				case 35545:
				case 35094:
				case 35136:
				case 35178:
				case 35220:
				case 35263:
				case 35310:
				case 35354:
				case 35499:
				case 35546:
				case 35264:
				case 35265:
				case 35500:
				case 35501:
				{
					if (CastleManager.getInstance().getCastle(this) != null && player.getClan() != null)
						if (getCastle().getOwnerId() == player.getClanId())
						{
							if (st.countTokens() <= 0)
								return;

							int val = Integer.parseInt(st.nextToken());
							L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
							if (list != null)
							{
								player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
							}
							else
							{
								_log.warn("No teleport destination with id:" + val);
							}
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
					break;
				}
			}

			if (st.countTokens() <= 0)
				return;

			int whereTo = Integer.parseInt(st.nextToken());
			if (condition == COND_REGULAR)
			{
				if (player != null)
				{
					doTeleport(player, whereTo);
				}
				return;
			}
			else if (condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0;
				if (st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}
				if (10 >= minPrivilegeLevel)
				{
					doTeleport(player, whereTo);
				}
				else
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_NOT_ACCESSABLE));
				}
				return;
			}
		}
		else if (command.startsWith("Chat"))
		{
			Calendar cal = Calendar.getInstance();
			int val = 0;

			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{

			}
			catch (NumberFormatException nfe)
			{

			}

			if (val == 1 && cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
			{
				showHalfPriceHtml(player);
				return;
			}
			showChatWindow(player, val);
		}
		super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if (condition > COND_ALL_FALSE)
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/teleporter/castleteleporter-busy.htm";
			}
			else if (condition == COND_OWNER)
			{
				filename = getHtmlPath(getNpcId(), 0);
			}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void showHalfPriceHtml(L2PcInstance player)
	{
		if (player == null)
			return;

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

		String content = HtmCache.getInstance().getHtm("data/html/teleporter/half/" + getNpcId() + ".htm");
		if (content == null)
		{
			content = HtmCache.getInstance().getHtmForce("data/html/teleporter/" + getNpcId() + "-1.htm");
		}

		html.setHtml(content);
		html.replace("%objectId%", getObjectId());
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private int validateCondition(L2PcInstance player)
	{
		if (CastleManager.getInstance().getCastle(this) == null)
			return COND_REGULAR;
		else if (player.getClan() != null)
			if (getCastle().getOwnerId() == player.getClanId())
				return COND_OWNER;
		return COND_ALL_FALSE;
	}

}
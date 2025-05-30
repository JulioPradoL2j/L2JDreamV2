package com.dream.game.model.actor.instance;

import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.skills.L2EffectType;

public class L2CastleMagicianInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	private static final boolean validateGateCondition(L2PcInstance clanLeader, L2PcInstance player)
	{
		if (clanLeader.isAlikeDead() || clanLeader.isInStoreMode() || clanLeader.isRooted() || clanLeader.isInCombat() || clanLeader.isInOlympiadMode() || clanLeader.isFestivalParticipant() || clanLeader.inObserverMode() || clanLeader.isInsideZone(L2Zone.FLAG_NOSUMMON) || clanLeader.isInsideZone(L2Zone.FLAG_NOLANDING))
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}

		if (player.isIn7sDungeon())
		{
			final int targetCabal = SevenSigns.getInstance().getPlayerCabal(clanLeader);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
			else
			{
				if (targetCabal == SevenSigns.CABAL_NULL)
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
		}

		return true;
	}

	public L2CastleMagicianInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
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
			showChatWindow(player, val);
			return;
		}
		else if (command.equals("gotoleader"))
		{
			if (player.getClan() != null)
			{
				L2PcInstance clanLeader = player.getClan().getLeader().getPlayerInstance();
				if (clanLeader == null)
					return;

				if (clanLeader.getFirstEffect(L2EffectType.CLAN_GATE) != null)
				{
					if (!validateGateCondition(clanLeader, player))
						return;

					player.teleToLocation(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ());
					return;
				}
				String filename = "data/html/castlemagician/magician-nogate.htm";
				showChatWindow(player, filename);
			}
			return;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/castlemagician/magician-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/castlemagician/magician-busy.htm";
			}
			else if (condition == COND_OWNER)
				if (val == 0)
				{
					filename = "data/html/castlemagician/magician.htm";
				}
				else
				{
					filename = "data/html/castlemagician/magician-" + val + ".htm";
				}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", String.valueOf(getName() + " " + getTitle()));
		player.sendPacket(html);
	}

	protected int validateCondition(L2PcInstance player)
	{
		if (getCastle() != null && player.getClan() != null)
		{
			if (getCastle().getSiege().getIsInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE;

			if (getCastle().getOwnerId() == player.getClanId())
				return COND_OWNER;
		}
		return COND_ALL_FALSE;
	}
}
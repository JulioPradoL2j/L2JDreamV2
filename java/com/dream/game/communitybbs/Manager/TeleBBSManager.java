package com.dream.game.communitybbs.Manager;

import java.util.StringTokenizer;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.xml.TeleportLocationTable;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.position.L2TeleportLocation;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.taskmanager.AttackStanceTaskManager;

/**
 * @author Matim
 * @version 1.0
 */
public class TeleBBSManager extends BaseBBSManager
{
	private static class SingletonHolder
	{
		protected static final TeleBBSManager INSTANCE = new TeleBBSManager();
	}

	private static void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
		}
		else
		{
			System.out.println("No teleport destination with id:" + val);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public static TeleBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private String msg;

	public boolean checkAllowed(L2PcInstance activeChar)
	{
		boolean ok = true;
		msg = null;

		if (msg != null)
		{
			activeChar.sendMessage(msg);
		}

		if (activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE && Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("TRADE"))
		{
			msg = "You can't use Community Buffer in private store!";
			ok = false;
		}
		if (activeChar.isSitting())
		{
			msg = "You can't use Community Buffer when you sit!";
			ok = false;
		}
		if (activeChar.isCastingNow())
		{
			msg = "You can't use Community Buffer when you cast!";
			ok = false;
		}
		if (activeChar.isDead())
		{
			msg = "You can't use Community Buffer when you dead!";
			ok = false;
		}
		if (activeChar.isInCombat())
		{
			msg = "You can't use Community Buffer when you in combat!";
			ok = false;
		}
		if (activeChar.isInJail() && Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("JAIL"))
		{
			msg = "You can't use Community Buffer when you in jail!";
			ok = false;
		}
		if (activeChar.getKarma() > 0 && Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("KARMA"))
		{
			msg = "You can't use Community Buffer when you have karma!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("CURSED") && activeChar.isCursedWeaponEquipped())
		{
			msg = "You can't use Community Buffer when you have Cursed Weapon!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("ATTACK") && AttackStanceTaskManager.getInstance().hasAttackStanceTask(activeChar))
		{
			msg = "You can't use Community Buffer when you Attack!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("SEVEN") && activeChar.isIn7sDungeon())
		{
			msg = "You can't use Community Buffer when you on 7 Signs!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("RB") && activeChar.isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			msg = "You can't use Community Buffer when you on Raid Zone!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("PVP") && activeChar.isInsideZone(L2Zone.FLAG_PVP))
		{
			msg = "You can't use Community Buffer when you on PvP Zone!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("PEACE") && activeChar.isInsideZone(L2Zone.FLAG_PEACE))
		{
			msg = "You can't use Community Buffer when you on Peace Zone!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("NOTINTOWN") && TownManager.getInstance().getTown(activeChar.getX(), activeChar.getY(), activeChar.getZ()) == null)
		{
			msg = "You can't use Community Buffer when you no in town!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("SIEGE") && activeChar.isInsideZone(L2Zone.FLAG_SIEGE))
		{
			msg = "You can't use Community Buffer when you on siege!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("OLYMPIAD") && (activeChar.isInOlympiadMode() || activeChar.isInsideZone(L2Zone.FLAG_STADIUM) || Olympiad.isRegistered(activeChar) || Olympiad.isRegisteredInComp(activeChar)))
		{
			msg = "You can't use Community Buffer when you on olympiad!";
			ok = false;
		}
		if (Config.COMMUNITY_GATEKEEPER_EXCLUDE_ON.contains("EVENT") && activeChar.isInFunEvent())
		{
			msg = "You can't use Community Buffer when you on event!";
			ok = false;
		}
		if (!ok)
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
		}

		return ok;

	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("_bbstele;"))
		{
			if (!checkAllowed(activeChar))
				return;

			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());

			doTeleport(activeChar, id);

			String filename = "data/html/communityboard/custom/gk.htm";
			String content = HtmCache.getInstance().getHtm(filename);

			separateAndSend(content, activeChar);
		}

	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{

	}
}
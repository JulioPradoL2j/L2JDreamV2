package com.dream.game.model.quest.pack.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.quest.Quest;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExShowScreenMessage;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.templates.chars.L2NpcTemplate;

public class IceFairySirra extends L2AttackableAIScript
{
	protected static Logger _log = Logger.getLogger(IceFairySirra.class.getName());
	private static final int STEWARD = 32029;
	private static final int SILVER_HEMOCYTE = 8057;
	private static L2PcInstance _player = null;
	protected List<L2Npc> _allMobs = new ArrayList<>();
	protected Future<?> _onDeadEventTask = null;

	public IceFairySirra()
	{
		super(-1, "IceFairySirra", "ai");
		int[] mob =
		{
			STEWARD,
			22100,
			22102,
			22104
		};
		registerMobs(mob);
		addEventId(STEWARD, Quest.QuestEventType.QUEST_START);
		addEventId(STEWARD, Quest.QuestEventType.ON_TALK);
		addEventId(STEWARD, Quest.QuestEventType.ON_FIRST_TALK);
		addKillId(29056);
		initial();
	}

	public boolean checkItems(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				L2ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				if (i == null || i.getCount() < 10)
					return false;
			}
		}
		else
			return false;
		return true;
	}

	public void cleanUp()
	{
		initial();
		cancelQuestTimer("30MinutesRemaining", null, _player);
		cancelQuestTimer("20MinutesRemaining", null, _player);
		cancelQuestTimer("10MinutesRemaining", null, _player);
		cancelQuestTimer("End", null, _player);
		for (L2Npc mob : _allMobs)
		{
			try
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "IceFairySirraManager: Failed deleting mob.", e);
			}
		}
		_allMobs.clear();
	}

	protected void closeGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				L2DoorInstance door = DoorTable.getInstance().getDoor(i);
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					_log.warning("IceFairySirraManager: Attempted to close undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "IceFairySirraManager: Failed closing door", e);
			}
		}
	}

	public void destroyItems(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				L2ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				pc.destroyItem("Hemocytes", i.getObjectId(), 10, null, false);
			}
		}
		else
		{
			cleanUp();
		}
	}

	public void doSpawns()
	{
		int[][] mobs =
		{
			{
				29060,
				105546,
				-127892,
				-2768
			},
			{
				29056,
				102779,
				-125920,
				-2840
			},
			{
				22100,
				111719,
				-126646,
				-2992
			},
			{
				22102,
				109509,
				-128946,
				-3216
			},
			{
				22104,
				109680,
				-125756,
				-3136
			}
		};
		L2Spawn spawnDat;
		L2NpcTemplate template;
		try
		{
			for (int i = 0; i < 5; i++)
			{
				template = NpcTable.getInstance().getTemplate(mobs[i][0]);
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(mobs[i][1]);
					spawnDat.setLocy(mobs[i][2]);
					spawnDat.setLocz(mobs[i][3]);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(60);
					_allMobs.add(spawnDat.doSpawn());
					spawnDat.stopRespawn();
				}
				else
				{
					_log.warning("IceFairySirraManager: Data missing in NPC table for ID: " + mobs[i][0]);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("IceFairySirraManager: Spawns could not be initialized: " + e);
		}
	}

	public L2Npc findTemplate(int npcId)
	{
		L2Npc npc = null;
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
			if (spawn != null && spawn.getNpcid() == npcId)
			{
				npc = spawn.getLastSpawn();
				break;
			}
		return npc;
	}

	public String getHtmlPath(int val)
	{
		String pom = "";

		pom = "32029-" + val;
		if (val == 0)
		{
			pom = "32029";
		}

		String temp = "data/html/default/" + pom + ".htm";

		return temp;
	}

	private void initial()
	{
		L2Npc steward = findTemplate(STEWARD);
		if (steward != null)
		{
			steward.setBusy(false);
		}
		openGates();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("check_condition"))
		{
			if (npc.isBusy())
				return super.onAdvEvent(event, npc, player);
			String filename = "";
			if (player.isInParty() && player.getParty().getPartyLeaderOID() == player.getObjectId())
			{
				if (checkItems(player) == true)
				{
					startQuestTimer("start", 100000, null, player);
					_player = player;
					destroyItems(player);
					player.getInventory().addItem("Scroll", 8379, 3, player, null);
					npc.setBusy(true);
					screenMessage(player, "Steward: Please wait a moment.", 100000);
					filename = getHtmlPath(3);
				}
				else
				{
					filename = getHtmlPath(2);
				}
			}
			else
			{
				filename = getHtmlPath(1);
			}
			sendHtml(npc, player, filename);
		}
		else if (event.equalsIgnoreCase("start"))
		{
			closeGates();
			doSpawns();
			startQuestTimer("Party_Port", 2000, null, player);
			startQuestTimer("End", 1802000, null, player);
		}
		else if (event.equalsIgnoreCase("Party_Port"))
		{
			teleportInside(player);
			screenMessage(player, "Steward: Please restore the Queen's appearance!", 10000);
			startQuestTimer("30MinutesRemaining", 300000, null, player);
		}
		else if (event.equalsIgnoreCase("30MinutesRemaining"))
		{
			screenMessage(player, "30 minute(s) are remaining.", 10000);
			startQuestTimer("20minutesremaining", 600000, null, player);
		}
		else if (event.equalsIgnoreCase("20MinutesRemaining"))
		{
			screenMessage(player, "20 minute(s) are remaining.", 10000);
			startQuestTimer("10minutesremaining", 600000, null, player);
		}
		else if (event.equalsIgnoreCase("10MinutesRemaining"))
		{
			screenMessage(player, "Steward: Waste no time! Please hurry!", 10000);
		}
		else if (event.equalsIgnoreCase("End"))
		{
			screenMessage(player, "Steward: Was it indeed too much to ask.", 10000);
			cleanUp();
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState("IceFairySirra") == null)
		{
			newQuestState(player);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		String filename = "";
		if (npc.isBusy())
		{
			filename = getHtmlPath(10);
		}
		else
		{
			filename = getHtmlPath(0);
		}
		sendHtml(npc, player, filename);
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		for (L2PcInstance pc : killer.getParty().getPartyMembers())
			if (pc.isInsideRadius(killer, 500, false, false))
				if (pc.getInventory().getItemByItemId(8180) == null)
				{
					pc.addItem("Raid", 8180, 1, null, true);
				}
		return null;
	}

	protected void openGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				L2DoorInstance door = DoorTable.getInstance().getDoor(i);
				if (door != null)
				{
					door.openMe();
				}
				else
				{
					_log.warning("IceFairySirraManager: Attempted to open undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "IceFairySirraManager: Failed closing door", e);
			}
		}
	}

	public void screenMessage(L2PcInstance player, String text, int time)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				pc.sendPacket(new ExShowScreenMessage(text, time));
			}
		}
		else
		{
			cleanUp();
		}
	}

	public void sendHtml(L2Npc npc, L2PcInstance player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void teleportInside(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				pc.teleToLocation(113533, -126159, -3488, false);
			}
		}
		else
		{
			cleanUp();
		}
	}
}
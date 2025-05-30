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
package com.dream.game.model.quest;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.manager.QuestManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.scripting.ManagedScript;
import com.dream.game.scripting.ScriptManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;
import com.dream.util.ArrayUtils;
import com.dream.util.ResourceUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

public class Quest extends ManagedScript
{
	public class DeSpawnScheduleTimerTask implements Runnable
	{
		L2Npc _npc = null;

		public DeSpawnScheduleTimerTask(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			_npc.onDecay();
		}
	}

	public static enum QuestEventType
	{
		ON_FIRST_TALK(false),
		QUEST_START(true),
		ON_TALK(true),
		ON_ATTACK(true),

		ON_KILL(true),
		ON_SPAWN(true),

		ON_SKILL_SEE(true),
		ON_SKILL_USE(true),
		ON_FACTION_CALL(true),

		ON_AGGRO_RANGE_ENTER(true),
		ON_SPELL_FINISHED(true),

		ON_INTENTION_CHANGE(true);

		private boolean _allowMultipleRegistration;

		QuestEventType(boolean allowMultipleRegistration)
		{
			_allowMultipleRegistration = allowMultipleRegistration;
		}

		public boolean isMultipleRegistrationAllowed()
		{
			return _allowMultipleRegistration;
		}
	}

	public class tmpOnAggroEnter implements Runnable
	{
		private final L2Npc _npc;
		private final L2PcInstance _pc;
		private final boolean _isPet;

		public tmpOnAggroEnter(L2Npc npc, L2PcInstance pc, boolean isPet)
		{
			_npc = npc;
			_pc = pc;
			_isPet = isPet;
		}

		@Override
		public void run()
		{
			String res = null;
			try
			{
				res = onAggroRangeEnter(_npc, _pc, _isPet);
			}
			catch (Exception e)
			{
				showError(_pc, e);
			}
			showResult(_pc, res);
		}
	}

	protected static Logger _log = Logger.getLogger(Quest.class.getName());

	private static Map<String, Quest> _allEventsS = new HashMap<>();

	private static Map<String, List<QuestTimer>> _allEventTimers = new ConcurrentHashMap<>();

	private static String[] _sealedQuests = {};

	static
	{
		_sealedQuests = ArrayUtils.add(_sealedQuests, "EnchantYouWear");
		_sealedQuests = ArrayUtils.add(_sealedQuests, "RecoverBrokenItem");
		_sealedQuests = ArrayUtils.add(_sealedQuests, "BossHunting");

	}

	public final static boolean contains(int[] array, int obj)
	{
		for (int element : array)
			if (element == obj)
				return true;
		return false;
	}

	public final static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
			if (element == obj)
				return true;
		return false;
	}

	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}

	
	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO character_quests (charId,name,var,value) VALUES (?,?,?,?)");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("could not insert char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	public static void deleteQuestInDb(QuestState qs)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("could not delete char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ? AND var = ?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("could not delete char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static Collection<Quest> findAllEvents()
	{
		return _allEventsS.values();
	}

	private static List<QuestTimer> getQuestTimers(String name)
	{
		return _allEventTimers.get(name);
	}

	
	public final static void playerEnter(L2PcInstance player)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;

			PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ?");
			PreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ? AND var = ?");

			statement = con.prepareStatement("SELECT name, value FROM character_quests WHERE charId = ? AND var = ?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				String questId = rs.getString("name");
				String statename = rs.getString("value");

				Quest q = QuestManager.getInstance().getQuest(questId);
				if (q == null)
				{
					if (_log.isDebugEnabled() || Config.DEBUG)
					{
						_log.info("Unknown quest " + questId + " for player " + player.getName());
					}
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestData.setInt(1, player.getObjectId());
						invalidQuestData.setString(2, questId);
						invalidQuestData.executeUpdate();
					}
					continue;
				}

				new QuestState(q, player, State.getStateId(statename));
			}
			rs.close();
			invalidQuestData.close();
			statement.close();

			statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE charId = ? AND var<>?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rs = statement.executeQuery();
			while (rs.next())
			{
				String questId = rs.getString("name");
				String var = rs.getString("var");
				String value = rs.getString("value");
				QuestState qs = player.getQuestState(questId);
				if (qs == null)
				{
					if (_log.isDebugEnabled() || Config.DEBUG)
					{
						_log.info("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
					}
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestDataVar.setInt(1, player.getObjectId());
						invalidQuestDataVar.setString(2, questId);
						invalidQuestDataVar.setString(3, var);
						invalidQuestDataVar.executeUpdate();
					}
					continue;
				}
				qs.setInternal(var, value);
			}
			rs.close();
			invalidQuestDataVar.close();
			statement.close();

		}
		catch (Exception e)
		{
			_log.warn("could not insert char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		for (String name : _allEventsS.keySet())
		{
			player.processQuestEvent(name, "enter");
		}
	}

	public static void updateQuestInDb(QuestState qs)
	{
		String val = State.getStateName(qs.getState());
		updateQuestVarInDb(qs, "<state>", val);
	}

	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE character_quests SET value = ? WHERE charId = ? AND name = ? AND var = ?");
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
			ResourceUtil.closeStatement(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			_log.warn("could not update char quest:", e);
		}
		finally
		{
			ResourceUtil.closeConnection(con);
		}
	}

	private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();

	private final int _questId;

	private final String _name;

	private final String _descr;

	private final byte _initialState = State.CREATED;

	public int[] questItemIds = null;

	public Quest(int questId, String name, String descr)
	{
		_questId = questId;
		_name = name;
		QuestMessage questName = QuestMessage.getQuestMessageById(questId);
		if (questName != null)
		{
			descr = questName.get();
		}
		_descr = descr;

		if (questId != 0)
		{
			QuestManager.getInstance().addQuest(Quest.this);
		}
		else
		{
			_allEventsS.put(name, this);
		}
		init_LoadGlobalData();
	}

	public L2NpcTemplate addAggroRangeEnterId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
	}

	public L2NpcTemplate addAttackId(int attackId)
	{
		return addEventId(attackId, Quest.QuestEventType.ON_ATTACK);
	}

	public L2NpcTemplate addEventId(int npcId, QuestEventType eventType)
	{
		try
		{
			L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			if (t != null)
			{
				t.addQuestEvent(eventType, this);
			}
			return t;
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
			return null;
		}
	}

	public L2NpcTemplate addFactionCallId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_FACTION_CALL);
	}

	public L2NpcTemplate addFirstTalkId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_FIRST_TALK);
	}

	public L2NpcTemplate addIntentionChange(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_INTENTION_CHANGE);
	}

	public L2NpcTemplate addKillId(int killId)
	{
		return addEventId(killId, Quest.QuestEventType.ON_KILL);
	}

	public L2NpcTemplate addSkillSeeId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SKILL_SEE);
	}

	public void addKillId(int... killIds)
	{
		for (int killId : killIds)
			addEventId(killId, Quest.QuestEventType.ON_KILL);
	}

	public L2NpcTemplate addSkillUseId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SKILL_USE);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				if (x == 0 && y == 0)
				{
					_log.fatal("Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}
				if (randomOffset)
				{
					int offset;

					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					offset *= Rnd.get(50, 100);
					x += offset;

					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z + 20);
				spawn.stopRespawn();
				result = spawn.spawnOne(isSummonSpawn);
				if (despawnDelay > 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(result), despawnDelay);
				}

				return result;
			}
		}
		catch (Exception e1)
		{
			_log.warn("Could not spawn Npc " + npcId);
		}

		return null;
	}

	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, false);
	}

	public L2Npc addSpawn(int npcId, L2Character cha, boolean isSummonSpawn)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, isSummonSpawn);
	}

	public L2NpcTemplate addSpawnId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SPAWN);
	}

	public L2NpcTemplate addSpellFinishedId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SPELL_FINISHED);
	}

	public L2NpcTemplate addStartNpc(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.QUEST_START);
	}

	public L2NpcTemplate addTalkId(int talkId)
	{
		return addEventId(talkId, Quest.QuestEventType.ON_TALK);
	}

	public void cancelQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		QuestTimer timer = getQuestTimer(name, npc, player);
		if (timer != null)
		{
			timer.cancel();
		}
	}

	public void cancelQuestTimers(String name)
	{
		List<QuestTimer> timers = getQuestTimers(name);
		if (timers == null)
			return;
		try
		{
			_rwLock.writeLock().lock();
			for (QuestTimer timer : timers)
				if (timer != null)
				{
					timer.cancel();
				}
		}
		finally
		{
			_rwLock.writeLock().unlock();
		}
	}

	
	public final void deleteAllGlobalQuestVars()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?");
			statement.setString(1, getName());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("could not delete global quest variables:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	
	public final void deleteGlobalQuestVar(String var)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("could not delete global quest variable:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public String getDescr()
	{
		return _descr;
	}

	public byte getInitialState()
	{
		return _initialState;
	}

	public String getName()
	{
		return _name;
	}

	public int getQuestIntId()
	{
		return _questId;
	}

	public QuestTimer getQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		List<QuestTimer> qt = getQuestTimers(name);
		if (qt == null || qt.isEmpty())
			return null;

		try
		{
			_rwLock.readLock().lock();
			for (QuestTimer timer : qt)
				if (timer != null)
					if (timer.isMatch(this, name, npc, player))
						return timer;
		}
		finally
		{
			_rwLock.readLock().unlock();
		}
		return null;
	}

	public L2PcInstance getRandomPartyMember(L2PcInstance player)
	{
		if (player == null)
			return null;
		if (player.getParty() == null || player.getParty().getPartyMembers().size() == 0)
			return player;
		L2Party party = player.getParty();
		int ntry = player.getParty().getMemberCount();
		for (;;)
		{
			if (ntry == 1)
			{
				break;
			}
			L2PcInstance pc = party.getPartyMembers().get(Rnd.get(party.getPartyMembers().size()));
			if (pc.isInsideRadius(player, 700, false, false))
				return pc;
			ntry--;
		}
		return player;
	}

	public L2PcInstance getRandomPartyMember(L2PcInstance player, String value)
	{
		return getRandomPartyMember(player, "cond", value);
	}

	public L2PcInstance getRandomPartyMember(L2PcInstance player, String var, String value)
	{
		if (player == null)
			return null;

		if (var == null)
			return getRandomPartyMember(player);

		QuestState temp = null;
		L2Party party = player.getParty();
		if (party == null || party.getPartyMembers().size() == 0)
		{
			temp = player.getQuestState(getName());
			if (temp != null && temp.get(var) != null && ((String) temp.get(var)).equalsIgnoreCase(value))
				return player;

			return null;
		}
		List<L2PcInstance> candidates = new ArrayList<>();

		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}

		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			temp = partyMember.getQuestState(getName());
			if (temp != null && temp.get(var) != null && ((String) temp.get(var)).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 700, true, false))
			{
				candidates.add(partyMember);
			}
		}
		if (candidates.size() == 0)
			return null;

		return candidates.get(Rnd.get(candidates.size()));
	}

	public L2PcInstance getRandomPartyMemberState(L2PcInstance player, byte state)
	{
		if (player == null)
			return null;

		QuestState temp = null;
		L2Party party = player.getParty();
		if (party == null || party.getPartyMembers().size() == 0)
		{
			temp = player.getQuestState(getName());
			if (temp != null && temp.getState() == state)
				return player;

			return null;
		}
		List<L2PcInstance> candidates = new ArrayList<>();

		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}

		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			temp = partyMember.getQuestState(getName());
			if (temp != null && temp.getState() == state && partyMember.isInsideRadius(target, 700, true, false))
			{
				candidates.add(partyMember);
			}
		}
		if (candidates.size() == 0)
			return null;

		return candidates.get(Rnd.get(candidates.size()));
	}

	public int[] getRegisteredItemIds()
	{
		return questItemIds;
	}

	@Override
	public ScriptManager<?> getScriptManager()
	{
		return QuestManager.getInstance();
	}

	@Override
	public String getScriptName()
	{
		return getName();
	}

	protected void init_LoadGlobalData()
	{

	}

	
	public final String loadGlobalQuestVar(String var)
	{
		String result = "";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
			statement.setString(1, getName());
			statement.setString(2, var);
			ResultSet rs = statement.executeQuery();
			if (rs.first())
			{
				result = rs.getString(1);
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("could not load global quest variable:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return result;
	}

	public QuestState newQuestState(L2PcInstance player)
	{
		QuestState qs = new QuestState(this, player, getInitialState());
		Quest.createQuestInDb(qs);
		return qs;
	}

	public final boolean notifyAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		String res = null;
		try
		{
			res = onAggroRangeEnter(npc, player, isPet);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}

	public final boolean notifyAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isPet, skill);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}

	public final boolean notifyDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		return showResult(qs.getPlayer(), res);
	}

	public final boolean notifyEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}

	public final boolean notifyExitQuest(L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onExitQuest(player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}

	public final boolean notifyFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isPet);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}

	public final boolean notifyFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		if (res != null && res.length() > 0)
			return showResult(player, res);

		player.sendPacket(ActionFailed.STATIC_PACKET);
		return true;
	}

	public final void notifyIntentionChange(L2Character npc, CtrlIntention newIntention)
	{
		try
		{
			onIntentionChange(npc, newIntention);
			return;
		}
		catch (Exception e)
		{
			_log.warn("Quest " + this.getClass().getSimpleName() + " IntentionChange error", e);
		}
	}

	public final boolean notifyKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		String res = null;
		try
		{
			res = onKill(npc, killer, isPet);
		}
		catch (Exception e)
		{
			return showError(killer, e);
		}
		return showResult(killer, res);
	}

	public final boolean notifySkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		String res = null;
		try
		{
			res = onSkillSee(npc, caster, skill, targets, isPet);
		}
		catch (Exception e)
		{
			return showError(caster, e);
		}
		return showResult(caster, res);
	}

	public final boolean notifySpawn(L2Npc npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch (Exception e)
		{
			_log.warn("", e);
			return true;
		}
		return false;
	}

	public final boolean notifySpellFinished(L2Npc instance, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(instance, player, skill);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}

	public final boolean notifyTalk(L2Npc npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs.getPlayer());
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs.getPlayer(), res);
	}

	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getName());
		if (qs != null)
			return onEvent(event, qs);

		return null;
	}

	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return null;
	}

	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		return null;
	}

	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		return onAttack(npc, attacker, damage, isPet);
	}

	public String onDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		if (killer instanceof L2Npc)
			return onAdvEvent("", (L2Npc) killer, qs.getPlayer());

		return onAdvEvent("", null, qs.getPlayer());
	}

	public String onDisconnect(QuestState qs, L2PcInstance player)
	{
		return null;
	}

	public String onEvent(String event, QuestState qs)
	{
		return null;
	}

	public String onEvent(String event, QuestState qs, boolean gmShopUser)
	{
		return onEvent(event, qs);
	}

	public String onExitQuest(L2PcInstance player)
	{
		return null;
	}

	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		return null;
	}

	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	public void onIntentionChange(L2Character cha, CtrlIntention newIntention)
	{

	}

	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		return null;
	}

	public String onLogin(QuestState qs, L2PcInstance player)
	{
		return null;
	}

	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		return null;
	}

	public String onSkillUse(L2Npc npc, L2PcInstance caster, L2Skill skill)
	{
		return null;
	}

	public String onSpawn(L2Npc npc)
	{
		return null;
	}

	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return null;
	}

	@Override
	public boolean reload()
	{
		unload();
		return super.reload();
	}

	public void removeQuestTimer(QuestTimer timer)
	{
		if (timer == null)
			return;
		List<QuestTimer> timers = getQuestTimers(timer.getName());
		if (timers == null)
			return;
		try
		{
			_rwLock.writeLock().lock();
			timers.remove(timer);
		}
		finally
		{
			_rwLock.writeLock().unlock();
		}
	}

	public void saveGlobalData()
	{

	}

	
	public final void saveGlobalQuestVar(String var, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name, var, value) VALUES (?, ?, ?)");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("could not insert global quest variable:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public void setActive(boolean status)
	{

	}

	@Deprecated
	public void setInitialState(QuestState qs)
	{

	}

	@Deprecated
	public void setState(QuestState qs)
	{

	}

	
	public boolean showError(L2PcInstance player, Throwable t)
	{
		if (getScriptFile() != null)
			if (getScriptFile() != null)
			{
				_log.warn(getScriptFile(), t);
			}

		if (player != null && player.isGM())
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			String res = "<html><title>Script Error</title><body>" + sw.toString() + "</body></html>";
			return showResult(player, res);
		}
		return false;
	}

	public String showHtmlFile(L2PcInstance player, String fileName)
	{
		return showHtmlFile(player, fileName, false);
	}

	@SuppressWarnings("null")
	public String showHtmlFile(L2PcInstance player, String fileName, boolean loadOnly)
	{
		String questId = getName();
		String content = HtmCache.getInstance().getQuestHtm(fileName, this);
		if (content != null)
		{
			content = content.replace("%playername%", player.getName());
			content = content.replace("%questname%", getName());
			if (player.getTarget() != null)
			{
				content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
			}
		}
		if (content != null && player != null && !loadOnly)
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}

		if (Config.SHOW_HTML_CHAT && player.isGM())
		{
			player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_None, "HTML", "scripts/" + questId + "/" + fileName));
		}

		return content;
	}

	public boolean showResult(L2PcInstance player, String res)
	{
		if (res == null)
			return true;

		if (res.endsWith(".htm"))
		{
			showHtmlFile(player, res);
		}
		else if (res.indexOf("<html>") != -1)
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.sendMessage(res);
		}
		return false;
	}

	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player)
	{
		startQuestTimer(name, time, npc, player, false);
	}

	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		List<QuestTimer> timers = getQuestTimers(name);
		if (timers == null)
		{
			timers = new ArrayList<>();
			timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			_allEventTimers.put(name, timers);
		}
		else if (getQuestTimer(name, npc, player) == null)
		{
			try
			{
				_rwLock.writeLock().lock();
				timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			}
			finally
			{
				_rwLock.writeLock().unlock();
			}
		}
	}

	@Override
	public boolean unload()
	{
		saveGlobalData();
		for (List<QuestTimer> timers : _allEventTimers.values())
		{
			for (QuestTimer timer : timers)
			{
				timer.cancel();
			}
		}
		_allEventTimers.clear();
		return QuestManager.getInstance().removeQuest(this);
	}
}
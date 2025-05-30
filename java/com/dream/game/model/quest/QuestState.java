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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.GameTimeController;
import com.dream.game.manager.QuestManager;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ExShowQuestMark;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.QuestList;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.TutorialCloseHtml;
import com.dream.game.network.serverpackets.TutorialEnableClientEvent;
import com.dream.game.network.serverpackets.TutorialShowHtml;
import com.dream.game.network.serverpackets.TutorialShowQuestionMark;
import com.dream.game.skills.Stats;
import com.dream.tools.random.Rnd;

public final class QuestState
{
	protected static Logger _log = Logger.getLogger(Quest.class.getName());

	public static void dropItem(L2MonsterInstance npc, L2PcInstance player, int itemId, int count)
	{
		npc.dropItem(player, itemId, count);
	}

	public static int getGameTicks()
	{
		return GameTimeController.getGameTicks();
	}

	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}

	private final String _questName;

	private Quest _quest;

	private final L2PcInstance _player;

	private byte _state;

	private Map<String, String> _vars;

	private boolean _isExitQuestOnCleanUp = false;

	private boolean _ignoreMe = false;

	@Deprecated
	QuestState(byte state)
	{
		_questName = null;
		_player = null;
		_ignoreMe = true;

	}

	QuestState(Quest quest, L2PcInstance player, byte state)
	{
		_quest = quest;
		_questName = quest.getName();
		_player = player;

		getPlayer().setQuestState(this);

		_state = state;
	}

	@Deprecated
	QuestState(String name, Quest quest)
	{
		_questName = quest.getName();
		_player = null;
		_ignoreMe = true;
	}

	public void addExpAndSp(int exp, int sp)
	{
		getPlayer().addExpAndSp((int) getPlayer().calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUESTS_REWARD_EXPSP, null, null), (int) getPlayer().calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUESTS_REWARD_EXPSP, null, null));
	}

	public void addNotifyOfDeath(L2Character character)
	{
		if (character == null)
			return;

		character.addNotifyQuestOfDeath(this);
	}

	public void addRadar(int x, int y, int z)
	{
		getPlayer().getRadar().addMarker(x, y, z);
	}

	public L2Npc addSpawn(int npcId)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, 0);
	}

	public L2Npc addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, despawnDelay);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn);
	}

	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha, true, 0);
	}

	public L2Npc addSpawn(int npcId, L2Character cha, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay);
	}

	public L2Npc addSpawn(int npcId, L2Character cha, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay);
	}

	public void clearRadar()
	{
		getPlayer().getRadar().removeAllMarkers();
	}

	public void closeTutorialHtml()
	{
		getPlayer().sendPacket(new TutorialCloseHtml());
	}

	public QuestState exitQuest(boolean repeatable)
	{
		if (isCompleted())
			return this;
		_quest.notifyExitQuest(getPlayer());

		setState(State.COMPLETED);

		int[] itemIdList = getQuest().getRegisteredItemIds();
		if (itemIdList != null)
		{
			for (int element : itemIdList)
			{
				takeItems(element, -1);
			}
		}
		if (repeatable)
		{
			getPlayer().delQuestState(getQuestName());
			Quest.deleteQuestInDb(this);

			_vars = null;
		}
		else
		{
			if (_vars != null)
			{
				for (String var : _vars.keySet())
				{
					unset(var);
				}
			}

			Quest.updateQuestInDb(this);
		}

		return this;
	}

	public QuestState exitQuest(int repeatable)
	{
		return exitQuest(repeatable > 0);
	}

	public Object get(String var)
	{
		if (_vars == null)
			return null;

		return _vars.get(var);
	}

	public int getEnchantLevel(int itemId)
	{
		L2ItemInstance enchanteditem = getPlayer().getInventory().getItemByItemId(itemId);

		if (enchanteditem == null)
			return 0;

		return enchanteditem.getEnchantLevel();
	}

	public int getInt(String var)
	{
		int varint = 0;

		try
		{
			varint = Integer.parseInt(_vars.get(var));
		}
		catch (Exception e)
		{
		}

		return varint;
	}

	public int getItemEquipped(int loc)
	{
		return getPlayer().getInventory().getPaperdollItemId(loc);
	}

	public L2PcInstance getPlayer()
	{
		return _player;
	}

	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}

	public int getQuestItemsCount(int itemId)
	{
		int count = 0;

		for (L2ItemInstance item : getPlayer().getInventory().getItems())
			if (item.getItemId() == itemId)
			{
				count += item.getCount();
			}

		return count;
	}

	public String getQuestName()
	{
		return _questName;
	}

	public final QuestTimer getQuestTimer(String name)
	{
		return getQuest().getQuestTimer(name, null, getPlayer());
	}

	public byte getState()
	{
		return _state;
	}

	public void giveAdena(int count, boolean hz)
	{
		rewardItems(57, count, 0);
	}

	public void giveItems(int itemId, int count)
	{
		giveItems(itemId, count, 0);
	}

	public void giveItems(int itemId, int count, int enchantlevel)
	{
		if (count <= 0)
			return;

		L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());

		if (item == null)
			return;
		if (enchantlevel > 0)
		{
			item.setEnchantLevel(enchantlevel);
		}

		if (itemId == 57)
		{
			getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));
		}
		else if (count > 1)
		{
			getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(count));
		}
		else
		{
			getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
		}
		getPlayer().getInventory().updateInventory(item);
	}

	public boolean isCompleted()
	{
		return getState() == State.COMPLETED;
	}

	public final boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}

	public boolean isMeIgnored()
	{
		return _ignoreMe;
	}

	public boolean isStarted()
	{
		return getState() == State.STARTED;
	}

	public void onTutorialClientEvent(int number)
	{
		getPlayer().sendPacket(new TutorialEnableClientEvent(number));
	}

	public void playSound(String sound)
	{
		getPlayer().sendPacket(new PlaySound(sound));
	}

	public void playTutorialVoice(String voice)
	{
		getPlayer().sendPacket(new PlaySound(2, voice, 0, 0, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ()));
	}

	public void removeRadar(int x, int y, int z)
	{
		getPlayer().getRadar().removeMarker(x, y, z);
	}

	public void rewardItems(int itemId, int count)
	{
		rewardItems(itemId, count, 0);
	}

	public void rewardItems(int itemId, int count, int enchantlevel)
	{
		if (count <= 0)
			return;

		if (itemId == 57)
		{
			count = (int) (count * Config.RATE_QUESTS_REWARD_ADENA);
		}
		else
		{
			count = (int) (count * Config.RATE_QUESTS_REWARD_ITEMS);
		}

		L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());

		if (item == null)
			return;
		if (enchantlevel > 0)
		{
			item.setEnchantLevel(enchantlevel);
		}

		if (itemId == 57)
		{
			getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));
		}
		else if (count > 1)
		{
			getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(count));
		}
		else
		{
			getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
		}
		getPlayer().getInventory().updateInventory(item);
	}

	public String set(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new ConcurrentHashMap<>();
		}

		if (val == null)
		{
			val = "";
		}

		String old = _vars.put(var, val);

		if (old != null)
		{
			Quest.updateQuestVarInDb(this, var, val);
		}
		else
		{
			Quest.createQuestVarInDb(this, var, val);
		}

		if (var == "cond")
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(val), previousVal);
			}
			catch (Exception e)
			{
				_log.info(getPlayer().getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e);
			}
		}
		return val;
	}

	private void setCond(int cond, int old)
	{
		int completedStateFlags = 0;

		if (cond == old)
			return;

		if (cond < 3 || cond > 31)
		{
			unset("__compltdStateFlags");
		}
		else
		{
			completedStateFlags = getInt("__compltdStateFlags");
		}

		if (completedStateFlags == 0)
		{
			if (cond > old + 1)
			{
				completedStateFlags = 0x80000001;

				completedStateFlags |= (1 << old) - 1;

				completedStateFlags |= 1 << cond - 1;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		else if (cond < old)
		{
			completedStateFlags &= (1 << cond) - 1;

			if (completedStateFlags == (1 << cond) - 1)
			{
				unset("__compltdStateFlags");
			}
			else
			{
				completedStateFlags |= 0x80000001;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		else
		{
			completedStateFlags |= 1 << cond - 1;
			set("__compltdStateFlags", String.valueOf(completedStateFlags));
		}
		getPlayer().sendPacket(new QuestList(getPlayer()));

		int questId = getQuest().getQuestIntId();
		if (questId > 0 && questId < 999 && cond > 0)
		{
			getPlayer().sendPacket(new ExShowQuestMark(questId));
		}
	}

	String setInternal(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new ConcurrentHashMap<>();
		}

		if (val == null)
		{
			val = "";
		}

		_vars.put(var, val);
		return val;
	}

	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}

	public Object setState(byte state)
	{
		if (_state != state)
		{
			_state = state;
			Quest.updateQuestInDb(this);
			getPlayer().sendPacket(new QuestList(getPlayer()));
		}
		return state;
	}

	public String showHtmlFile(String fileName)
	{
		return getQuest().showHtmlFile(getPlayer(), fileName);
	}

	public void showQuestionMark(int number)
	{
		getPlayer().sendPacket(new TutorialShowQuestionMark(number));
	}

	public void showTutorialHTML(String html)
	{
		String text = QuestManager.getInstance().getQuest("255_Tutorial").showHtmlFile(getPlayer(), html, true);
		if (text == null)
		{
			_log.warn("Missing html page " + html);
			text = "<html><body>File " + html + " not found or file is empty.</body></html>";
		}
		getPlayer().sendPacket(new TutorialShowHtml(text));
	}

	public void startQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), false);
	}

	public void startQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), false);
	}

	public void startRepeatingQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), true);
	}

	public void startRepeatingQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), true);
	}

	public void takeItems(int itemId, int count)
	{
		L2ItemInstance item = getPlayer().getInventory().getItemByItemId(itemId);

		if (item == null)
			return;

		if (count < 0 || count > item.getCount())
		{
			count = item.getCount();
		}

		if (itemId == 57)
		{
			getPlayer().reduceAdena("Quest", count, getPlayer(), true);
		}
		else
		{
			if (item.isEquipped())
			{
				L2ItemInstance[] unequiped = getPlayer().getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance itm : unequiped)
				{
					iu.addModifiedItem(itm);
				}
				getPlayer().sendPacket(iu);
				getPlayer().broadcastUserInfo();
			}
			getPlayer().destroyItemByItemId("Quest", itemId, count, getPlayer(), true);
		}
	}

	public String unset(String var)
	{
		if (_vars == null)
			return null;

		String old = _vars.remove(var);

		if (old != null)
		{
			Quest.deleteQuestVarInDb(this, var);
		}

		return old;
	}
}
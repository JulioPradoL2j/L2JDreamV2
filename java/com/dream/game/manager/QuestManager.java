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
package com.dream.game.manager;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.game.model.quest.Quest;
import com.dream.game.scripting.L2ScriptEngineManager;
import com.dream.game.scripting.ScriptManager;

import javolution.util.FastMap;

public class QuestManager extends ScriptManager<Quest>
{
	protected static Logger _log = Logger.getLogger(QuestManager.class.getName());

	private static QuestManager _instance;

	public static final QuestManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing Quest Data.");
			_instance = new QuestManager();
		}
		return _instance;
	}

	private Map<String, Quest> _quests = new FastMap<>();

	public QuestManager()
	{

	}

	public final void addQuest(Quest newQuest)
	{
		if (newQuest == null)
			throw new IllegalArgumentException("Quest argument cannot be null");

		Quest old = getQuests().get(newQuest.getName());

		if (old != null)
		{
			old.unload();
			_log.info("Replaced: (" + old.getName() + ":" + old.getScriptFile() + ") with a new version (" + newQuest.getName() + ":" + newQuest.getScriptFile() + ")");
		}
		getQuests().put(newQuest.getName(), newQuest);
	}

	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return _quests.values();
	}

	public final Quest getQuest(int questId)
	{
		for (Quest q : getQuests().values())
			if (q.getQuestIntId() == questId)
				return q;
		return null;
	}

	public final Quest getQuest(String name)
	{
		return getQuests().get(name);
	}

	public final Map<String, Quest> getQuests()
	{
		if (_quests == null)
		{
			_quests = new FastMap<>();
		}
		return _quests;
	}

	@Override
	public String getScriptManagerName()
	{
		return "QuestManager";
	}

	public final boolean reload(int questId)
	{
		Quest q = this.getQuest(questId);
		if (q == null)
			return false;

		return q.reload();
	}

	public final boolean reload(String questFolder)
	{
		Quest q = getQuest(questFolder);
		if (q == null)
			return false;
		return q.reload();
	}

	public final void reloadAllQuests()
	{
		_log.info("Reloading Server Scripts");
		try
		{
			for (Quest quest : _quests.values())
				if (quest != null)
				{
					quest.unload();
				}

			L2ScriptEngineManager.getInstance().loadScripts();
			QuestManager.getInstance().report();
		}
		catch (IOException e)
		{
			_log.warn("Failed loading scripts.cfg, no script going to be loaded");
		}
	}

	public final boolean removeQuest(Quest q)
	{
		return getQuests().remove(q.getName()) != null;
	}

	public final void report()
	{
		_log.info("Quest Data: Loaded " + getQuests().size() + " quest(s).");
	}

	public final void saveData()
	{
		for (Quest q : getQuests().values())
		{
			q.saveGlobalData();
		}
	}

	@Override
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
}
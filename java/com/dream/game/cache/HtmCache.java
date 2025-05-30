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
package com.dream.game.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.L2GameServer;
import com.dream.game.model.quest.Quest;
import com.dream.game.util.Util;
import com.dream.tools.cache.HtmFilter;
import com.dream.tools.cache.UnicodeReader;

public class HtmCache
{
	private static class SingletonHolder
	{
		protected static final HtmCache _instance = new HtmCache();
	}

	private static final Logger _log = Logger.getLogger(L2GameServer.class);

	private static final Map<Integer, String> _htmCache = new HashMap<>();

	private static final FileFilter _htmFilter = new HtmFilter();

	public static HtmCache getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Loads html file content to HtmCache.
	 * @param file : File to be cached.
	 * @return String : Content of the file.
	 */
	private static String loadFile(File file)
	{
		if (file.exists() && _htmFilter.accept(file) && !file.isDirectory())
		{
			try (FileInputStream fis = new FileInputStream(file);
				UnicodeReader ur = new UnicodeReader(fis, "ISO-8859-1");
				BufferedReader br = new BufferedReader(ur))
			{
				StringBuilder sb = new StringBuilder();
				String line;

				while ((line = br.readLine()) != null)
				{
					sb.append(line).append('\n');
				}

				String content = sb.toString().replaceAll("\r\n", "\n");
				sb = null;

				_htmCache.put(file.getPath().replace("\\", "/").hashCode(), content);
				return content;
			}
			catch (Exception e)
			{
				_log.warn("HtmCache: problem with loading file " + e);
			}
		}

		return null;
	}

	/**
	 * Parse given directory, all html files are loaded to HtmCache.
	 * @param dir : Directory to be parsed.
	 */
	private static void parseDir(File dir)
	{
		for (File file : dir.listFiles(_htmFilter))
			if (file.isDirectory())
			{
				parseDir(file);
			}
			else
			{
				loadFile(file);
			}
	}

	protected HtmCache()
	{
		reload();
	}

	/**
	 * Return content of html message given by filename.
	 * @param filename : Desired html filename.
	 * @return String : Returns content if filename exists, otherwise returns null.
	 */
	public String getHtm(String filename)
	{
		if (filename == null || filename.isEmpty())
			return "";

		String content = _htmCache.get(filename.hashCode());
		if (content == null)
		{
			content = loadFile(new File(filename));
		}

		return content;
	}

	/**
	 * Return content of html message given by filename. In case filename does not exist, returns notice.
	 * @param filename : Desired html filename.
	 * @return String : Returns content if filename exists, otherwise returns notice.
	 */
	public String getHtmForce(String filename)
	{
		String content = getHtm(filename);
		if (content == null)
		{
			content = "<html><body>My html is missing:<br>" + filename + "</body></html>";
			_log.warn("HtmCache: " + filename + " is missing.");
		}

		return content;
	}

	public String getQuestHtm(String fileName, Quest quest)
	{
		String result = null;
		String questFolder = null;
		if (quest.getScriptFile() != null)
		{
			questFolder = Util.getRelativePath(Config.DATAPACK_ROOT, new File(quest.getScriptFile()).getParentFile());
		}
		else
		{
			questFolder = "data/scripts/";
			if (quest.getQuestIntId() < 0 || quest.getQuestIntId() > 1000)
			{
				questFolder += "custom/";
			}
			else
			{
				questFolder += "quests/";
			}
			questFolder += quest.getName();
		}
		result = getHtm(questFolder + "/" + fileName);
		if (result == null)
		{
			result += "<html><body><br>File " + questFolder + "/" + fileName + " is missing</body></html>";
		}
		return result;
	}

	/**
	 * Check if an HTM exists and can be loaded. If so, it is loaded into HtmCache.
	 * @param path The path to the HTM
	 * @return true if the HTM can be loaded.
	 */
	public boolean isLoadable(String path)
	{
		return loadFile(new File(path)) != null;
	}

	/**
	 * Cleans HtmCache.
	 */
	public void reload()
	{
		_log.info("HtmCache: Cache cleared, had " + _htmCache.size() + " entries.");
		_htmCache.clear();
	}

	/**
	 * Reloads given directory. All sub-directories are parsed, all html files are loaded to HtmCache.
	 * @param path : Directory to be reloaded.
	 */
	public void reloadPath(String path)
	{
		parseDir(new File(path));
		_log.info("HtmCache: Reloaded specified " + path + " path.");
	}
}
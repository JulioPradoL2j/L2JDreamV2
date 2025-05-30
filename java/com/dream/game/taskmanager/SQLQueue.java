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
package com.dream.game.taskmanager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dream.L2DatabaseFactory;
import com.dream.game.taskmanager.tasks.ExclusiveTask;
import com.dream.sql.SQLQuery;

public final class SQLQueue extends ExclusiveTask
{
	private static SQLQueue _instance;

	private static Logger _log = Logger.getLogger(SQLQueue.class);

	protected static Connection getConnection() throws SQLException
	{
		return L2DatabaseFactory.getInstance().getConnection();
	}

	public static final SQLQueue getInstance()
	{
		if (_instance == null)
		{
			_instance = new SQLQueue();
		}

		return _instance;
	}

	private final List<SQLQuery> _query = new ArrayList<>();

	private boolean _running = false;

	private SQLQueue()
	{
		schedule(60000);
		_log.info("Initializing SQLQueue Manager.");
	}

	public void add(SQLQuery q)
	{
		synchronized (_query)
		{
			_query.add(q);
		}
	}

	private void flush()
	{
		Connection con = null;
		if (_running)
			return;
		try
		{
			_running = true;
			con = getConnection();
			for (SQLQuery q; (q = getNextQuery()) != null;)
			{
				try
				{
					q.execute(con);
				}
				catch (Exception e)
				{
					_log.warn("SQLQueue: Error executing " + q.getClass().getSimpleName(), e);
				}
			}
		}
		catch (SQLException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
			_running = false;
		}
	}

	private SQLQuery getNextQuery()
	{
		synchronized (_query)
		{
			if (_query.isEmpty())
				return null;
			return _query.remove(0);
		}
	}

	@Override
	protected void onElapsed()
	{
		flush();
		schedule(60000);
	}

	public synchronized void run()
	{
		flush();
	}
}
package com.dream.game.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class mysql
{
	private static Logger _log = Logger.getLogger(mysql.class.getName());

	/**
	 * Выполняет простой sql запросов, где ненужен контроль параметров<BR>
	 * ВНИМАНИЕ: В данном методе передаваемые параметры не проходят проверку на предмет SQL-инъекции!
	 * @param query Строка SQL запроса
	 * @return false в случае ошибки выполнения запроса либо true в случае успеха
	 */
	public static boolean set(String query)
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = DBFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate(query);
		}
		catch(Exception e)
		{
			_log.warning("Could not execute update '" + query + "': " + e);
			Thread.dumpStack();
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		return true;
	}

	public static Object get(String query)
	{
		Object ret = null;
		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DBFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery(query + " LIMIT 1");
			ResultSetMetaData md = rset.getMetaData();

			if(rset.next())
				if(md.getColumnCount() > 1)
				{
					ConcurrentHashMap<String, Object> tmp = new ConcurrentHashMap<>();
					for(int i = md.getColumnCount(); i > 0; i--)
						tmp.put(md.getColumnName(i), rset.getObject(i));
					ret = tmp;
				}
				else
					ret = rset.getObject(1);

		}
		catch(Exception e)
		{
			_log.warning("Could not execute query '" + query + "': " + e);
			Thread.dumpStack();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return ret;
	}

	public static ArrayList<HashMap<String, Object>> getAll(String query)
	{
		ArrayList<HashMap<String, Object>> ret = new ArrayList<>();
		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DBFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery(query);
			ResultSetMetaData md = rset.getMetaData();

			while(rset.next())
			{
				HashMap<String, Object> tmp = new HashMap<>();
				for(int i = md.getColumnCount(); i > 0; i--)
					tmp.put(md.getColumnName(i), rset.getObject(i));
				ret.add(tmp);
			}
		}
		catch(Exception e)
		{
			_log.warning("Could not execute query '" + query + "': " + e);
			Thread.dumpStack();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return ret;
	}

	public static ArrayList<Object> get_array(String query)
	{
		ArrayList<Object> ret = new ArrayList<>();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DBFactory.getInstance().getConnection();
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();
			ResultSetMetaData md = rset.getMetaData();

			while(rset.next())
				if(md.getColumnCount() > 1)
				{
					ConcurrentHashMap<String, Object> tmp = new ConcurrentHashMap<>();
					for(int i = 0; i < md.getColumnCount(); i++)
						tmp.put(md.getColumnName(i + 1), rset.getObject(i + 1));
					ret.add(tmp);
				}
				else
					ret.add(rset.getObject(1));
		}
		catch(Exception e)
		{
			_log.warning("Could not execute query '" + query + "': " + e);
			Thread.dumpStack();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return ret;
	}

	public static int simple_get_int(String ret_field, String table, String where)
	{
		String query = "SELECT " + ret_field + " FROM `" + table + "` WHERE " + where + " LIMIT 1;";

		int res = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DBFactory.getInstance().getConnection();
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();

			if(rset.next())
				res = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.warning("mSGI: Error in query '" + query + "':" + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		return res;
	}

	public static Integer[][] simple_get_int_array(String[] ret_fields, String table, String where)
	{
		long start = System.currentTimeMillis();

		String fields = null;
		for(String field : ret_fields)
			if(fields != null)
			{
				fields += ",";
				fields += "`" + field + "`";
			}
			else
				fields = "`" + field + "`";

		String query = "SELECT " + fields + " FROM `" + table + "` WHERE " + where;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		Integer res[][] = null;

		try
		{
			con = DBFactory.getInstance().getConnection();
			statement = con.prepareStatement(query);
			rset = statement.executeQuery();

			ArrayList<Integer[]> al = new ArrayList<>();
			int row = 0;
			while(rset.next())
			{
				Integer[] tmp = new Integer[ret_fields.length];
				for(int i = 0; i < ret_fields.length; i++)
					tmp[i] = rset.getInt(i + 1);
				al.add(row, tmp);
				row++;
			}

			res = al.toArray(new Integer[row][ret_fields.length]);
		}
		catch(Exception e)
		{
			_log.warning("mSGIA: Error in query '" + query + "':" + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		_log.fine("Get all rows in query '" + query + "' in " + (System.currentTimeMillis() - start) + "ms");
		return res;
	}
}
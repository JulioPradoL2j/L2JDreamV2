package com.dream;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

public final class L2AuthDatabaseFactory
{
	private static final Logger _log = Logger.getLogger(L2AuthDatabaseFactory.class);

	private static L2AuthDatabaseFactory _instance;

	public static void close(Connection con)
	{
		if (con == null)
			return;

		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			_log.warn("L2DatabaseFactory: Failed to close database connection!", e);
		}
	}

	public static L2AuthDatabaseFactory getInstance() throws SQLException
	{
		if (_instance == null)
		{
			_instance = new L2AuthDatabaseFactory();
		}

		return _instance;
	}

	public static String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
	{
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if (returnOnlyTopRecord)
		{
			mySqlTop1 = " Limit 1 ";
		}

		return "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;

	}

	public static String safetyString(String... whatToCheck)
	{
		String braceLeft = "`";
		String braceRight = "`";

		String result = "";
		for (String word : whatToCheck)
		{
			if (!result.isEmpty())
			{
				result += ", ";
			}

			result += braceLeft + word + braceRight;
		}

		return result;
	}

	private final BasicDataSource _source;

	private L2AuthDatabaseFactory() throws SQLException
	{
		try
		{
			_source = new BasicDataSource();
			_source.setDriverClassName(AuthConfig.DATABASE_DRIVER);
			_source.setUrl(AuthConfig.DATABASE_URL);
			_source.setUsername(AuthConfig.DATABASE_LOGIN);
			_source.setPassword(AuthConfig.DATABASE_PASSWORD);

			_source.setInitialSize(1);
			_source.setMaxActive(10);
			_source.setMaxIdle(10);
			_source.setMinIdle(1);
			_source.setMaxWait(-1L);

			_source.setDefaultAutoCommit(true);
			_source.setValidationQuery("SELECT 1");
			_source.setTestOnBorrow(false);
			_source.setTestWhileIdle(true);

			_source.setRemoveAbandoned(true);
			_source.setRemoveAbandonedTimeout(60);

			_source.setTimeBetweenEvictionRunsMillis(600 * 1000);
			_source.setNumTestsPerEvictionRun(10);
			_source.setMinEvictableIdleTimeMillis(60 * 1000L);

			_log.info("L2DatabaseFactory: Connected to database server");
		}
		catch (Exception e)
		{
			throw new SQLException("L2DatabaseFactory: Failed to init database connections: " + e, e);
		}
	}

	public int getBusyConnectionCount()
	{
		return _source.getNumActive();
	}

	public Connection getConnection()
	{
		return getConnection(null);
	}

	public Connection getConnection(Connection con)
	{
		while (con == null)
		{
			try
			{
				con = _source.getConnection();
			}
			catch (SQLException e)
			{
				_log.fatal("L2DatabaseFactory: Failed to retrieve database connection!", e);
			}
		}

		return con;
	}

	public int getIdleConnectionCount()
	{
		return _source.getNumIdle();
	}

	public void shutdown() throws Throwable
	{
		_source.close();
	}

}
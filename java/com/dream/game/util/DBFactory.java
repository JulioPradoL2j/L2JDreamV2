package com.dream.game.util;

import com.dream.Config;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBFactory
{
	private static DBFactory _instance;
	private ComboPooledDataSource _source;

	// список используемых на данный момент коннектов
	private final Hashtable<String, ThreadConnection> Connections = new Hashtable<>();

	static Logger _log = Logger.getLogger(DBFactory.class.getName());

	@SuppressWarnings("deprecation")
	public DBFactory() throws SQLException
	{
		_instance = this;
		try
		{
			if(Config.DATABASE_MAX_CONNECTIONS < 2)
			{
				Config.DATABASE_MAX_CONNECTIONS = 2;
				_log.warning("at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}

			Class.forName(Config.DATABASE_DRIVER).newInstance();

			if(Config.DEBUG)
				_log.fine("Database Connection Working");

			_source = new ComboPooledDataSource();
			_source.setDriverClass(Config.DATABASE_DRIVER); //loads the jdbc driver
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD); // the settings below are optional -- c3p0 can work with defaults
			_source.setAutoCommitOnClose(true);
			_source.setInitialPoolSize(1);
			_source.setMinPoolSize(1);
			_source.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);
			_source.setAcquireRetryAttempts(0);// try to obtain Connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(100);// 500 miliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more Connections at a time
			_source.setMaxStatements(100);
			_source.setIdleConnectionTestPeriod(Config.DATABASE_IDLE_TEST_PERIOD); // test idle connection every 1 minute
			_source.setMaxIdleTime(Config.DATABASE_MAX_IDLE_TIMEOUT); // remove unused connection after 10 minutes
			_source.setNumHelperThreads(5);
			_source.setBreakAfterAcquireFailure(false);

			/* Test the connection */
			_source.getConnection().close();
		}
		catch(SQLException x)
		{
			if(Config.DEBUG)
				_log.fine("Database Connection FAILED");
			// rethrow the exception
			throw x;
		}
		catch(Exception e)
		{
			if(Config.DEBUG)
				_log.fine("Database Connection FAILED");
			throw new SQLException("could not init DB connection:" + e);
		}
	}

	public static DBFactory getInstance() throws SQLException
	{
		if(_instance == null)
			new DBFactory();
		return _instance;
	}

	public ThreadConnection getConnection() throws SQLException
	{
		ThreadConnection connection;
		if(Config.USE_DATABASE_LAYER)
		{
			String key = generateKey();

			connection = Connections.get(key);
			if(connection == null)
				try
				{
		
					connection = new ThreadConnection(_source.getConnection());
				}
				catch(SQLException e)
				{
					_log.warning("Couldn't create connection. Cause: " + e.getMessage());
				}
			else

				connection.updateCounter();

			if(connection != null)
				synchronized (Connections)
				{
					Connections.put(key, connection);
				}
		}
		else
			connection = new ThreadConnection(_source.getConnection());
		return connection;
	}

	public Hashtable<String, ThreadConnection> getConnections()
	{
		return Connections;
	}

	public void shutdown()
	{
		_source.close();
		Connections.clear();
		try
		{
			DataSources.destroy(_source);
		}
		catch(SQLException e)
		{
			_log.log(Level.INFO, "", e);
		}
	}


	public String generateKey()
	{
		return String.valueOf(Thread.currentThread().hashCode());
	}
}
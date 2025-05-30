package com.dream.game.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created: 26.06.2007 0:32:02
 * @author Alexey Lahtadir <a href="mailto:alexey_lahtadir@mail.ru">mailto: alexey_lahtadir@mail.ru</a>
 */
public class DatabaseUtils
{

	/**
	 * Закрыть коннект
	 * @param conn - коннект к базе данных
	 */
	public static void closeConnection(ThreadConnection conn)
	{
		if (conn != null)
			conn.close();
	}

	/**
	 * Закрыть Statement
	 * @param stmt - Statement
	 */
	public static void closeStatement(FiltredStatementInterface stmt)
	{
		if (stmt != null)
			stmt.close();
	}

	/**
	 * Закрыть ResultSet
	 * @param rs - ResultSet
	 */
	public static void closeResultSet(ResultSet rs)
	{
		if (rs != null)
			try
			{
				rs.close();
			}
			catch (SQLException e)
			{
			}
	}

	/**
	 * Закрыть коннект, Statement и ResultSet
	 * @param conn - Connection
	 * @param stmt - Statement
	 * @param rs - ResultSet
	 */
	public static void closeDatabaseCSR(ThreadConnection conn, FiltredStatementInterface stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
		closeConnection(conn);
	}

	/**
	 * закрыть коннект, Statement
	 * @param conn - Connection
	 * @param stmt - Statement
	 */
	public static void closeDatabaseCS(ThreadConnection conn, FiltredStatementInterface stmt)
	{
		closeStatement(stmt);
		closeConnection(conn);
	}

	/**
	 * закрыть Statement и ResultSet
	 * @param stmt - Statement
	 * @param rs - ResultSet
	 */
	public static void closeDatabaseSR(FiltredStatementInterface stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
	}

}

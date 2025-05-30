package com.dream.game.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FiltredStatement implements FiltredStatementInterface
{
	private final Statement myStatement;

	public FiltredStatement(Statement statement)
	{
		myStatement = statement;
	}

	public int executeUpdate(String sql) throws SQLException
	{
		return myStatement.executeUpdate(sql);
	}

	@Override
	public void close()
	{
		try
		{
			myStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void addBatch(String sql) throws SQLException
	{
		myStatement.addBatch(sql);
	}

	public int[] executeBatch() throws SQLException
	{
		return myStatement.executeBatch();
	}

	public ResultSet executeQuery(String sql) throws SQLException
	{
		return myStatement.executeQuery(sql);
	}
}
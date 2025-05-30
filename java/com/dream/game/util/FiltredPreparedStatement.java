package com.dream.game.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// import javolution.text.TextBuilder;

public class FiltredPreparedStatement implements FiltredStatementInterface
{
	private final PreparedStatement myStatement;

	public FiltredPreparedStatement(PreparedStatement statement)
	{
		myStatement = statement;
	}

	public ResultSet executeQuery() throws SQLException
	{
		return myStatement.executeQuery();
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

	public boolean execute() throws SQLException
	{
		return myStatement.execute();
	}

	public ResultSet executeQuery(String sql) throws SQLException
	{
		return myStatement.executeQuery(sql);
	}

	public void setInt(int index, int val) throws SQLException
	{
		myStatement.setInt(index, val);
	}

	// private static final char _92 = 92;

	public void setString(int index, String val) throws SQLException
	{
		myStatement.setString(index, val);
	}

	public void setLong(int index, long val) throws SQLException
	{
		myStatement.setLong(index, val);
	}

	public void setNull(int index, int val) throws SQLException
	{
		myStatement.setNull(index, val);
	}

	public void setDouble(int index, double val) throws SQLException
	{
		myStatement.setDouble(index, val);
	}

	public void setBytes(int index, byte[] data) throws SQLException
	{
		myStatement.setBytes(index, data);
	}

	public int executeUpdate() throws SQLException
	{
		return myStatement.executeUpdate();
	}

	public void setBoolean(int index, boolean val) throws SQLException
	{
		myStatement.setBoolean(index, val);
	}

	public void setEscapeProcessing(boolean val) throws SQLException
	{
		myStatement.setEscapeProcessing(val);
	}

	public void setByte(int index, byte val) throws SQLException
	{
		myStatement.setByte(index, val);
	}
}
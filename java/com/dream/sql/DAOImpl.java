package com.dream.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DAOImpl
{
	public boolean delete(Connection con) throws SQLException;

	public boolean load(Connection con) throws SQLException;

	public boolean load(ResultSet rs);

	public boolean store(Connection con) throws SQLException;
}
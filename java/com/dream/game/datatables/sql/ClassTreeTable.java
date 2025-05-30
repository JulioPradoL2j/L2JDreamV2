package com.dream.game.datatables.sql;

import com.dream.L2DatabaseFactory;
import com.dream.game.model.base.ClassId;
import com.dream.util.ArrayUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClassTreeTable
{
	public class ClassInfo
	{
		public String name;
		public int parentClass;
	}

	private static class SingletonHolder
	{
		protected static final ClassTreeTable _instance = new ClassTreeTable();
	}

	public static final ClassTreeTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<Integer, ClassInfo> _classes = new HashMap<>();

	
	public ClassTreeTable()
	{
		try
		{
			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement("select * from class_list");
			ResultSet rs = stm.executeQuery();
			while (rs.next())
			{
				ClassInfo info = new ClassInfo();
				info.name = rs.getString("class_name");
				info.parentClass = rs.getInt("parent_id");
				_classes.put(rs.getInt("id"), info);
			}
			rs.close();
			stm.close();
			con.close();
		}
		catch (SQLException e)
		{

		}
	}

	public Collection<ClassInfo> getAllClasses()
	{
		return _classes.values();
	}

	public ClassId getClassId(String shortName)
	{
		for (int id : _classes.keySet())
			if (_classes.get(id).name.equals(shortName))
				return ClassId.values()[id];
		return ClassId.spellhowler;
	}

	public String[] getParentClasses(int classId)
	{
		String[] result = null;
		ClassInfo info = _classes.get(classId);
		while (info != null)
		{
			result = ArrayUtils.add(result, info.name);
			info = _classes.get(info.parentClass);
		}
		return result;
	}
}
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
package com.dream.game.datatables.sql;

import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.xml.HennaTable;
import com.dream.game.model.actor.instance.L2HennaInstance;
import com.dream.game.model.base.ClassId;
import com.dream.game.templates.item.L2Henna;
import com.dream.util.ResourceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class HennaTreeTable
{
	private static class SingletonHolder
	{
		protected static final HennaTreeTable _instance = new HennaTreeTable();
	}

	private static final Logger _log = Logger.getLogger(HennaTreeTable.class);

	public static final HennaTreeTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private Map<ClassId, List<L2HennaInstance>> _hennaTrees;

	
	public HennaTreeTable()
	{
		_hennaTrees = new HashMap<>();
		int classId = 0;
		int count = 0;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT class_name, id, parent_id FROM class_list ORDER BY id");
			ResultSet classlist = statement.executeQuery();
			List<L2HennaInstance> list;

			while (classlist.next())
			{
				list = new ArrayList<>();
				classId = classlist.getInt("id");
				PreparedStatement statement2 = con.prepareStatement("SELECT class_id, symbol_id FROM henna_trees WHERE class_id = ? ORDER BY symbol_id");
				statement2.setInt(1, classId);
				ResultSet hennatree = statement2.executeQuery();

				while (hennatree.next())
				{
					int id = hennatree.getInt("symbol_id");
					L2Henna template = HennaTable.getInstance().getTemplate(id);

					if (template == null)
					{
						hennatree.close();
						statement2.close();
						classlist.close();
						statement.close();
						return;
					}

					L2HennaInstance temp = new L2HennaInstance(template);
					temp.setSymbolId(id);
					temp.setItemIdDye(template.getDyeId());
					temp.setAmountDyeRequire(template.getAmountDyeRequire());
					temp.setPrice(template.getPrice());
					temp.setStatINT(template.getStatINT());
					temp.setStatSTR(template.getStatSTR());
					temp.setStatCON(template.getStatCON());
					temp.setStatMEM(template.getStatMEM());
					temp.setStatDEX(template.getStatDEX());
					temp.setStatWIT(template.getStatWIT());

					list.add(temp);

					temp = null;
					template = null;
				}
				_hennaTrees.put(ClassId.values()[classId], list);

				hennatree.close();
				hennatree = null;
				statement2.close();
				statement2 = null;

				count += list.size();

			}

			list = null;
			classlist.close();
			classlist = null;
			statement.close();
			statement = null;

		}
		catch (Exception e)
		{
			_log.warn("error while creating henna tree for classId " + classId + "  " + e);
			e.printStackTrace();
		}
		finally
		{
			ResourceUtil.closeConnection(con);
		}

		_log.info("HennaTree Data: Loaded " + count + " henna tree templates.");

	}

	public L2HennaInstance[] getAvailableHenna(ClassId classId)
	{
		List<L2HennaInstance> result = new ArrayList<>();
		List<L2HennaInstance> henna = _hennaTrees.get(classId);
		if (henna == null)
		{
			_log.warn("Hennatree for class " + classId + " is not defined !");

			return new L2HennaInstance[0];
		}

		for (L2HennaInstance temp : henna)
		{
			result.add(temp);
		}

		return result.toArray(new L2HennaInstance[result.size()]);
	}
}
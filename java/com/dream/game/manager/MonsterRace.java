package com.dream.game.manager;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.tools.random.Rnd;

public class MonsterRace
{
	private final static Logger _log = Logger.getLogger(MonsterRace.class);

	private static MonsterRace _instance;

	public static MonsterRace getInstance()
	{
		if (_instance == null)
		{
			_instance = new MonsterRace();
		}

		return _instance;
	}

	private final L2Npc[] _monsters;
	private Constructor<?> _constructor;
	private int[][] _speeds;

	private final int[] _first, _second;

	private MonsterRace()
	{
		_monsters = new L2Npc[8];
		_speeds = new int[8][20];
		_first = new int[2];
		_second = new int[2];
	}

	public int getFirstPlace()
	{
		return _first[0];
	}

	public L2Npc[] getMonsters()
	{
		return _monsters;
	}

	public int getSecondPlace()
	{
		return _second[0];
	}

	public int[][] getSpeeds()
	{
		return _speeds;
	}

	public void newRace()
	{
		int random = 0;

		for (int i = 0; i < 8; i++)
		{
			int id = 31003;
			random = Rnd.get(24);
			for (int j = i - 1; j >= 0; j--)
				if (_monsters[j].getTemplate().getNpcId() == id + random)
				{
					random = Rnd.get(24);
					continue;
				}
			try
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(id + random);
				_constructor = Class.forName("com.dream.game.model.actor.instance." + template.getType() + "Instance").getConstructors()[0];
				int objectId = IdFactory.getInstance().getNextId();
				_monsters[i] = (L2Npc) _constructor.newInstance(objectId, template);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
		newSpeeds();
	}

	public void newSpeeds()
	{
		_speeds = new int[8][20];
		int total = 0;
		_first[1] = 0;
		_second[1] = 0;
		for (int i = 0; i < 8; i++)
		{
			total = 0;
			for (int j = 0; j < 20; j++)
			{
				if (j == 19)
				{
					_speeds[i][j] = 100;
				}
				else
				{
					_speeds[i][j] = Rnd.get(60) + 65;
				}
				total += _speeds[i][j];
			}
			if (total >= _first[1])
			{
				_second[0] = _first[0];
				_second[1] = _first[1];
				_first[0] = 8 - i;
				_first[1] = total;
			}
			else if (total >= _second[1])
			{
				_second[0] = 8 - i;
				_second[1] = total;
			}
		}
	}
}
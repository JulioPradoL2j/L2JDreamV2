package com.dream.game.model.olympiad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.dream.Message;
import com.dream.Message.MessageId;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.olympiad.Olympiad.COMP_TYPE;
import com.dream.tools.random.Rnd;

public class OlympiadManager implements Runnable
{
	protected static final Logger _log = Logger.getLogger(OlympiadManager.class.getName());

	protected static OlympiadManager _instance;
	protected static final OlympiadStadium[] STADIUMS =
	{
		new OlympiadStadium(-120324, -225077, -3331),
		new OlympiadStadium(-102495, -209023, -3331),
		new OlympiadStadium(-120156, -207378, -3331),
		new OlympiadStadium(-87628, -225021, -3331),
		new OlympiadStadium(-81705, -213209, -3331),
		new OlympiadStadium(-87593, -207339, -3331),
		new OlympiadStadium(-93709, -218304, -3331),
		new OlympiadStadium(-77157, -218608, -3331),
		new OlympiadStadium(-69682, -209027, -3331),
		new OlympiadStadium(-76887, -201256, -3331),
		new OlympiadStadium(-109985, -218701, -3331),
		new OlympiadStadium(-126367, -218228, -3331),
		new OlympiadStadium(-109629, -201292, -3331),
		new OlympiadStadium(-87523, -240169, -3331),
		new OlympiadStadium(-81748, -245950, -3331),
		new OlympiadStadium(-77123, -251473, -3331),
		new OlympiadStadium(-69778, -241801, -3331),
		new OlympiadStadium(-76754, -234014, -3331),
		new OlympiadStadium(-93742, -251032, -3331),
		new OlympiadStadium(-87466, -257752, -3331),
		new OlympiadStadium(-114413, -213241, -3331)
	};

	public static OlympiadManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new OlympiadManager();
		}

		return _instance;
	}

	private final Map<Integer, OlympiadGame> _olympiadInstances;

	public OlympiadManager()
	{
		_olympiadInstances = new ConcurrentHashMap<>();
		_instance = this;
	}

	protected boolean existNextOpponents(List<L2PcInstance> list)
	{
		if (list == null)
			return false;
		if (list.size() == 0)
			return false;
		int loopCount = list.size() >> 1;

		if (loopCount < 1)
			return false;
		return true;

	}

	public Map<Integer, String> getAllTitles()
	{
		Map<Integer, String> titles = new HashMap<>();

		for (OlympiadGame instance : _olympiadInstances.values())
		{
			if (instance == null || instance._gamestarted != true)
			{
				continue;
			}
			titles.put(instance._stadiumID, instance.getTitle());
		}
		return titles;
	}

	protected OlympiadGame getOlympiadGame(int index)
	{
		if (_olympiadInstances != null && !_olympiadInstances.isEmpty())
			return _olympiadInstances.get(index);

		return null;
	}

	protected Map<Integer, OlympiadGame> getOlympiadGames()
	{
		return _olympiadInstances == null ? null : _olympiadInstances;
	}

	protected List<L2PcInstance> getRandomClassList(Map<Integer, List<L2PcInstance>> list, List<Integer> classList)
	{
		if (list == null || classList == null || list.size() == 0 || classList.size() == 0)
			return null;

		return list.get(classList.get(Rnd.nextInt(classList.size())));
	}

	protected List<L2PcInstance> nextOpponents(List<L2PcInstance> list)
	{
		List<L2PcInstance> opponents = new ArrayList<>();
		if (list.size() == 0)
			return opponents;
		int loopCount = list.size() / 2;

		int first;
		int second;

		if (loopCount < 1)
			return opponents;

		first = Rnd.get(list.size());
		opponents.add(list.get(first));
		list.remove(first);

		second = Rnd.get(list.size());
		opponents.add(list.get(second));
		list.remove(second);

		return opponents;

	}

	protected void removeGame(OlympiadGame game)
	{
		if (_olympiadInstances != null && !_olympiadInstances.isEmpty())
		{
			for (int i = 0; i < _olympiadInstances.size(); i++)
				if (_olympiadInstances.get(i) == game)
				{
					_olympiadInstances.remove(i);
				}
		}
	}

	@Override
	public synchronized void run()
	{
		if (Olympiad.getInstance().isOlympiadEnd())
			return;

		Map<Integer, OlympiadGameTask> _gamesQueue = new HashMap<>();
		while (Olympiad.inCompPeriod())
		{
			if (Olympiad.getNobleCount() == 0)
			{
				try
				{
					wait(60000);
				}
				catch (InterruptedException ex)
				{
				}
				continue;
			}

			int _gamesQueueSize = 0;

			List<Integer> readyClasses = Olympiad.hasEnoughRegisteredClassed();
			boolean readyNonClassed = Olympiad.hasEnoughRegisteredNonClassed();
			if (readyClasses != null || readyNonClassed)
			{
				for (int i = 0; i < STADIUMS.length; i++)
				{
					if (!existNextOpponents(Olympiad.getRegisteredNonClassBased()) && !existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
					{
						break;
					}
					if (STADIUMS[i].isFreeToUse())
					{
						if (i < STADIUMS.length / 2)
						{
							if (readyNonClassed && existNextOpponents(Olympiad.getRegisteredNonClassBased()))
							{
								try
								{
									_olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.NON_CLASSED, nextOpponents(Olympiad.getRegisteredNonClassBased())));
									_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (L2PcInstance player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage(Message.getMessage(player, MessageId.MSG_REG_FAIL));
											player.setIsInOlympiadMode(false);
											player.setIsOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									if (_gamesQueue.get(i) != null)
									{
										_gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									i--;
								}
							}
							else if (readyClasses != null && existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
							{
								try
								{
									_olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.CLASSED, nextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses))));
									_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (L2PcInstance player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage(Message.getMessage(player, MessageId.MSG_REG_FAIL));
											player.setIsInOlympiadMode(false);
											player.setIsOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									if (_gamesQueue.get(i) != null)
									{
										_gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									i--;
								}
							}
						}
						else if (readyClasses != null && existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
						{
							try
							{
								_olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.CLASSED, nextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses))));
								_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
								STADIUMS[i].setStadiaBusy();
							}
							catch (Exception ex)
							{
								if (_olympiadInstances.get(i) != null)
								{
									for (L2PcInstance player : _olympiadInstances.get(i).getPlayers())
									{
										player.sendMessage(Message.getMessage(player, MessageId.MSG_REG_FAIL));
										player.setIsInOlympiadMode(false);
										player.setIsOlympiadStart(false);
										player.setOlympiadSide(-1);
										player.setOlympiadGameId(-1);
									}
									_olympiadInstances.remove(i);
								}
								if (_gamesQueue.get(i) != null)
								{
									_gamesQueue.remove(i);
								}
								STADIUMS[i].setStadiaFree();

								i--;
							}
						}
						else if (readyNonClassed && existNextOpponents(Olympiad.getRegisteredNonClassBased()))
						{
							try
							{
								_olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.NON_CLASSED, nextOpponents(Olympiad.getRegisteredNonClassBased())));
								_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
								STADIUMS[i].setStadiaBusy();
							}
							catch (Exception ex)
							{
								if (_olympiadInstances.get(i) != null)
								{
									for (L2PcInstance player : _olympiadInstances.get(i).getPlayers())
									{
										player.sendMessage(Message.getMessage(player, MessageId.MSG_REG_FAIL));
										player.setIsInOlympiadMode(false);
										player.setIsOlympiadStart(false);
										player.setOlympiadSide(-1);
										player.setOlympiadGameId(-1);
									}
									_olympiadInstances.remove(i);
								}
								if (_gamesQueue.get(i) != null)
								{
									_gamesQueue.remove(i);
								}
								STADIUMS[i].setStadiaFree();

								i--;
							}
						}
					}
					else if (_gamesQueue.get(i) == null || _gamesQueue.get(i).isTerminated() || _gamesQueue.get(i)._game == null)
					{
						try
						{
							_olympiadInstances.remove(i);
							_gamesQueue.remove(i);
							STADIUMS[i].setStadiaFree();
							i--;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}

				_gamesQueueSize = _gamesQueue.size();
				for (int i = 0; i < _gamesQueueSize; i++)
				{
					if (_gamesQueue.get(i) != null && !_gamesQueue.get(i).isTerminated() && !_gamesQueue.get(i).isStarted())
					{
						Thread T = new Thread(_gamesQueue.get(i));
						T.start();
					}
					try
					{
						wait(1000);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			try
			{
				wait(30000);
			}
			catch (InterruptedException e)
			{
			}
		}
		boolean allGamesTerminated = false;

		while (!allGamesTerminated)
		{
			try
			{
				wait(30000);
			}
			catch (InterruptedException e)
			{
			}

			if (_gamesQueue.size() == 0)
			{
				allGamesTerminated = true;
			}
			else
			{
				for (OlympiadGameTask game : _gamesQueue.values())
				{
					allGamesTerminated = allGamesTerminated || game.isTerminated();
				}
			}
		}
		_gamesQueue.clear();
		_olympiadInstances.clear();
		Olympiad.clearRegistered();

		OlympiadGame._battleStarted = false;
	}
}
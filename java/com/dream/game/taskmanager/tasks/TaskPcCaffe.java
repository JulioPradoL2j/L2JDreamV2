package com.dream.game.taskmanager.tasks;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.Location;
import com.dream.game.network.serverpackets.ExPCCafePointInfo;
import com.dream.tools.random.Rnd;

public class TaskPcCaffe extends ExclusiveTask
{

	private static Logger _log = Logger.getLogger(TaskPcCaffe.class);

	private final Map<Integer, Location> _lastLoc = new HashMap<>();

	public TaskPcCaffe()
	{
		_log.info("PCCaffe: Task scheduled every " + Config.PC_CAFFE_INTERVAL + " minutes");
	}

	@Override
	public void onElapsed()
	{
		for (L2PcInstance pc : L2World.getInstance().getAllPlayers())
		{
			if (pc.isOfflineTrade() || pc.isAlikeDead() || pc.getLevel() < Config.PC_CAFFE_MIN_LEVEL || pc.getLevel() > Config.PC_CAFFE_MAX_LEVEL)
			{
				continue;
			}
			if (_lastLoc.containsKey(pc.getObjectId()))
			{
				try
				{
					Location l = _lastLoc.get(pc.getObjectId());
					Location l2 = pc.getLoc();
					if (Math.abs(l.getX() * l.getX() + l.getY() * l.getY() - (l2.getX() * l2.getX() + l2.getY() + l2.getY())) < 200)
					{
						continue;
					}
				}
				finally
				{
					_lastLoc.put(pc.getObjectId(), pc.getLoc());
				}
			}

			int score = Config.PC_CAFFE_MIN_SCORE + Rnd.get(Config.PC_CAFFE_MAX_SCORE - Config.PC_CAFFE_MIN_SCORE);
			if (score <= 0)
			{
				continue;
			}
			pc.setPcCaffePoints(pc.getPcCaffePoints() + score);
			pc.sendPacket(new ExPCCafePointInfo(pc, score, true, 24, false));
		}
		schedule(Config.PC_CAFFE_INTERVAL * 60000);
	}

}

package com.dream.game.skills.conditions;

import java.util.Arrays;
import java.util.List;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.skills.Env;
import com.dream.util.ArrayUtils;

/**
 * @author efireX
 */
public class ConditionPlayerClassId extends Condition
{
	private final int[] _classIds;

	public ConditionPlayerClassId(List<Integer> classId)
	{
		_classIds = ArrayUtils.toPrimitive(classId.toArray(new Integer[classId.size()]), 0);

		Arrays.sort(_classIds);
	}

	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;

		return Arrays.binarySearch(_classIds, ((L2PcInstance) env.player).getClassId().getId()) >= 0;
	}
}
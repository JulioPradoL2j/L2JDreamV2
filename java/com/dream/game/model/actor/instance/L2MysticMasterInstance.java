package com.dream.game.model.actor.instance;

import com.dream.game.model.base.ClassType;
import com.dream.game.templates.chars.L2NpcTemplate;

public class L2MysticMasterInstance extends L2VillageMasterInstance
{

	public L2MysticMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected ClassType getVillageMasterTeachType()
	{
		return ClassType.Mystic;
	}
}
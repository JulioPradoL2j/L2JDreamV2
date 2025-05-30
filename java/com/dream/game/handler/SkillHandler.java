package com.dream.game.handler;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.util.HandlerRegistry;
import com.dream.util.JarUtils;

public final class SkillHandler extends HandlerRegistry<L2SkillType, ISkillHandler> implements ISkillHandler
{
	private static SkillHandler _instance;

	public static SkillHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillHandler();
		}

		return _instance;
	}

	private final Map<Integer, ICustomSkillHandler> _customSkills = new HashMap<>();

	private SkillHandler()
	{
		try
		{
			for (String handler : JarUtils.enumClasses("com.dream.game.handler.skill"))
			{
				try
				{
					Class<?> _handler = Class.forName(handler);
					if (_handler != null && ISkillHandler.class.isAssignableFrom(_handler))
					{
						Constructor<?> ctor = _handler.getConstructor();
						if (ctor != null)
						{
							registerSkillHandler((ISkillHandler) ctor.newInstance());
						}
					}
				}
				catch (Exception e)
				{
					continue;
				}
			}
		}
		catch (Exception e)
		{

		}

		HandlerRegistry._log.info("Skill Handler: Loaded " + size() + " handler(s).");
	}

	public ISkillHandler getSkillHandler(L2SkillType skillType)
	{
		ISkillHandler handler = get(skillType);

		return handler == null ? this : handler;
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return null;
	}

	public void handleCustomSkill(L2Skill skill, L2Character caster, L2Character... targets)
	{
		ICustomSkillHandler h = _customSkills.get(skill.getId());
		if (h != null)
		{
			h.useSkill(caster, skill, targets);
		}
	}

	public void registerCustomSkill(ICustomSkillHandler handler)
	{
		for (int i : handler.getSkills())
		{
			_customSkills.put(i, handler);
		}
	}

	public void registerSkillHandler(ISkillHandler handler)
	{
		registerAll(handler, handler.getSkillIds());
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		skill.useSkill(activeChar, targets);
	}

}
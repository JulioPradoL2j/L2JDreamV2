package com.dream.game.handler;

import com.dream.Config;
import com.dream.game.handler.voiced.AioMenu;
import com.dream.game.handler.voiced.AutoFarm;
import com.dream.game.handler.voiced.Bank;
import com.dream.game.handler.voiced.ClassMaster;
import com.dream.game.handler.voiced.Configurator;
import com.dream.game.handler.voiced.Help;
import com.dream.game.handler.voiced.Offline;
import com.dream.game.handler.voiced.Roulette;
import com.dream.game.handler.voiced.Wedding;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class VoicedCommandHandler
{
	private final static Logger _log = Logger.getLogger(VoicedCommandHandler.class.getName());
	
	private static VoicedCommandHandler _instance;
	
	public static VoicedCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new VoicedCommandHandler();
		}
		return _instance;
	}
	
	private final Map<String, IVoicedCommandHandler> _datatable;
	
	private VoicedCommandHandler()
	{
		_datatable = new HashMap<>();
		if (Config.VOICED_HELP)
		{
			registerVoicedCommandHandler(new Help());
		}
		if (Config.VOICED_OFFLINE)
		{
			registerVoicedCommandHandler(new Offline());
		}
		if (Config.VOICED_WEDDING)
		{
			registerVoicedCommandHandler(new Wedding());
		}
		if (Config.VOICED_BANK)
		{
			registerVoicedCommandHandler(new Bank());
		}
		if (Config.VOICED_CONFIGURATOR)
		{
			registerVoicedCommandHandler(new Configurator());
		}
		if (Config.VOICED_CLASS_MASTER)
		{
			registerVoicedCommandHandler(new ClassMaster());
		}
		if (Config.VOICED_AIOX_COMMAND)
		{
			registerVoicedCommandHandler(new AioMenu());
		}
		
		if (Config.VOICED_AUTOFARM_COMMAND)
		{
			registerVoicedCommandHandler(new AutoFarm());
		}
		
		if (Config.VOICED_ROULETTE_COMMAND)
		{
			registerVoicedCommandHandler(new Roulette());
		}
		
		if (_datatable.size() > 0)
		{
			_log.info("Voiced Handler: Loaded " + _datatable.size() + " handler(s).");
		}
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		return _datatable.get(command);
	}
	
	public Map<String, IVoicedCommandHandler> getVoicedCommandHandlers()
	{
		return _datatable;
	}
	
	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String element : ids)
		{
			_datatable.put(element, handler);
		}
	}
	
}
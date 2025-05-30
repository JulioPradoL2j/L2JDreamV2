package com.dream.game.model.zone;

import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.manager.FourSepulchersManager;
import com.dream.game.manager.grandbosses.AntharasManager;
import com.dream.game.manager.grandbosses.BaiumManager;
import com.dream.game.manager.grandbosses.BossLair;
import com.dream.game.manager.grandbosses.FrintezzaManager;
import com.dream.game.manager.grandbosses.QueenAntManager;
import com.dream.game.manager.grandbosses.SailrenManager;
import com.dream.game.manager.grandbosses.ValakasManager;
import com.dream.game.manager.grandbosses.VanHalterManager;
import com.dream.game.manager.grandbosses.ZakenManager;
import com.dream.game.manager.lastimperialtomb.LastImperialTombManager;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Playable;

public class L2BossZone extends L2DefaultZone
{
	public BossLair _lair;
	
	public Boss getBoss()
	{
		return _boss;
	}
	
	public BossLair getLair()
	{
		return _lair;
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(this, FLAG_NOSUMMON, true);
		super.onEnter(character);
		if (_lair != null)
		{
			_lair.onEnter(character);
		}
		if (_boss == Boss.QUEENANT && character instanceof L2Playable)
			if (!character.getActingPlayer().isGM() && character.getActingPlayer().getLevel() > QueenAntManager.SAFE_LEVEL)
			{
				character.teleToLocation(TeleportWhereType.Town);
			}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(this, FLAG_NOSUMMON, false);
		super.onExit(character);
		if (_lair != null)
		{
			_lair.onExit(character);
		}
		
	}
	
	@Override
	protected void register()
	{
		if (_boss == null)
		{
			_boss = Boss.NONE;
		}
		switch (_boss)
		{
			case ANTHARAS:
				_lair = AntharasManager.getInstance();
				break;
			case BAIUM:
				_lair = BaiumManager.getInstance();
				break;
			case FRINTEZZA:
				_lair = FrintezzaManager.getInstance();
				break;
			case SAILREN:
				_lair = SailrenManager.getInstance();
				break;
			case VALAKAS:
				_lair = ValakasManager.getInstance();
				break;
			case VANHALTER:
				_lair = VanHalterManager.getInstance();
				break;
			case QUEENANT:
				_lair = QueenAntManager.getInstance();
			case FOURSEPULCHERS:
				_lair = FourSepulchersManager.getInstance().findMausoleum(getId());
				break;
			case LASTIMPERIALTOMB:
				_lair = LastImperialTombManager.getInstance();
				break;
			case ZAKEN:
				_lair = ZakenManager.getInstance();
				break;
			
		}
		if (_lair != null)
		{
			_lair.registerZone(this);
		}
	}
}
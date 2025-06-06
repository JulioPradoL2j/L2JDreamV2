package com.dream.game.handler.voiced;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.GameTimeController;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.manager.CoupleManager;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.L2FriendList;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.restriction.AvailableRestriction;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.model.world.L2World;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ConfirmDlg;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.SetupGauge;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.AbnormalEffect;
import com.dream.game.util.Broadcast;

public class Wedding implements IVoicedCommandHandler
{
	private static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final int _partnerx;
		private final int _partnery;
		private final int _partnerz;
		private final boolean _to7sDungeon;
		
		EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z, boolean to7sDungeon)
		{
			_activeChar = activeChar;
			_partnerx = x;
			_partnery = y;
			_partnerz = z;
			_to7sDungeon = to7sDungeon;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
				return;
			
			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);
			
			if (checkGoToLoveState(_activeChar) == null)
				return;
			
			try
			{
				_activeChar.setIsIn7sDungeon(_to7sDungeon);
				_activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}
	
	private static final String[] VOICED_COMMANDS =
	{
		"engage",
		"gotolove",
		"divorce"
	};
	
	public static L2PcInstance checkGoToLoveState(L2PcInstance activeChar)
	{
		Siege siege = SiegeManager.getSiege(activeChar);
		
		if (!activeChar.isMaried())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NO_PARTNER));
			return null;
		}
		else if (activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_ERROR_CONTACT_GM));
			return null;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (activeChar.inObserverMode())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (activeChar.isInJail() || activeChar.isInsideZone(L2Zone.FLAG_JAIL))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (siege != null && siege.getIsInProgress())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (activeChar.isInDuel())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_CURRENT_IN_COMBAT));
			return null;
		}
		else if (activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		else if (ObjectRestrictions.getInstance().checkRestriction(activeChar, AvailableRestriction.PlayerGotoLove))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return null;
		}
		L2PcInstance partner = L2World.getInstance().getPlayer(activeChar.getPartnerId());
		if (partner != null)
		{
			siege = SiegeManager.getSiege(partner);
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			return null;
		}
		if (partner.isInJail() || partner.isInsideZone(L2Zone.FLAG_JAIL))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isInOlympiadMode())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isDead())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.inObserverMode())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isInDuel())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isInFunEvent())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (DimensionalRiftManager.getInstance().checkIfInRiftZone(partner.getX(), partner.getY(), partner.getZ(), false))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isFestivalParticipant())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (siege != null && siege.getIsInProgress())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isCursedWeaponEquipped())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isInsideZone(L2Zone.FLAG_NOESCAPE) || partner.isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
			return null;
		}
		else if (partner.isIn7sDungeon() && !activeChar.isIn7sDungeon())
		{
			int playerCabal = SevenSigns.getInstance().getPlayerCabal(activeChar);
			boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
			int compWinner = SevenSigns.getInstance().getCabalHighestScore();
			
			if (isSealValidationPeriod)
			{
				if (playerCabal != compWinner)
				{
					activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
					return null;
				}
			}
			else if (playerCabal == SevenSigns.CABAL_NULL)
			{
				activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_NOT_AVAILABLE));
				return null;
			}
		}
		return partner;
	}
	
	public synchronized boolean divorce(L2PcInstance activeChar)
	{
		if (activeChar.getPartnerId() == 0)
			return false;
		
		int _partnerId = activeChar.getPartnerId();
		int _coupleId = activeChar.getCoupleId();
		int AdenaAmount = 0;
		
		if (activeChar.isMaried())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_YOU_DIVORCED));
			AdenaAmount = activeChar.getAdena() / 100 * Config.WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);
		}
		else
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_NO_PARTNER));
		}
		
		L2PcInstance partner = L2World.getInstance().getPlayer(_partnerId);
		if (partner != null)
		{
			partner.setPartnerId(0);
			if (partner.isMaried())
			{
				partner.sendMessage(Message.getMessage(partner, Message.MessageId.MSG_PARTNER_ASK_DIVORCE));
			}
			else
			{
				partner.sendMessage(Message.getMessage(partner, Message.MessageId.MSG_PARTNER_ASK_DIVORCE));
			}
			
			if (AdenaAmount > 0)
			{
				partner.addAdena("WEDDING", AdenaAmount, null, false);
			}
		}
		
		CoupleManager.getInstance().deleteCouple(_coupleId);
		return true;
	}
	
	public boolean engage(L2PcInstance activeChar)
	{
		if (activeChar.getTarget() == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_SELECT_A_TARGET));
			return false;
		}
		
		if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		
		if (activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_HAVE_PARTNER));
			if (Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				int skillId;
				int skillLevel = 1;
				
				if (activeChar.getLevel() > 40)
				{
					skillLevel = 2;
				}
				
				if (activeChar.isMageClass())
				{
					skillId = 4361;
				}
				else
				{
					skillId = 4362;
				}
				
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if (activeChar.getFirstEffect(skill) == null)
				{
					skill.getEffects(activeChar, activeChar);
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
				}
			}
			return false;
		}
		
		L2PcInstance ptarget = (L2PcInstance) activeChar.getTarget();
		
		if (ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		
		if (ptarget.isMaried())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_TARGET_IS_MARIED));
			return false;
		}
		
		if (ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_TARGET_IS_MARIED));
			return false;
		}
		
		if (ptarget.isEngageRequest())
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_TARGET_IS_MARIED));
			return false;
		}
		
		if (ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex() && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}
		
		if (!L2FriendList.isInFriendList(activeChar, ptarget))
		{
			activeChar.sendMessage(Message.getMessage(activeChar, Message.MessageId.MSG_PARTNER_MUST_BE_FRIEND));
			return false;
		}
		
		ptarget.setEngageRequest(true, activeChar.getObjectId());
		ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId());
		ptarget.sendPacket(dlg.addString(activeChar.getName() + Message.getMessage(activeChar, Message.MessageId.MSG_ASK_YOU_ENGAGE)));
		return true;
	}
	
	@Override
	public String getDescription(String command)
	{
		if (command.equals("engage"))
			return "Engagement with your beloved.";
		if (command.equals("divorce"))
			return "Allows you to get a divorce if you're married.";
		if (command.equals("gotolove"))
			return "Moves you to your wife.";
		return null;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public boolean goToLove(L2PcInstance activeChar)
	{
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isDead())
			return false;
		
		L2PcInstance partner = null;
		if ((partner = checkGoToLoveState(activeChar)) == null)
			return false;
		
		int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL * 1000;
		
		activeChar.sendMessage("After " + teleportTimer / 60000 + " min, you will move to partner.");
		activeChar.getInventory().reduceAdena("Wedding", Config.WEDDING_TELEPORT_PRICE, activeChar, null);
		
		activeChar.getAI().setIntention(CtrlIntention.IDLE);
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, 1050, 1, teleportTimer, 0, false), 810000);
		activeChar.sendPacket(new SetupGauge(0, teleportTimer));
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon()), teleportTimer));
		activeChar.forceIsCasting(GameTimeController.getGameTicks() + teleportTimer / GameTimeController.MILLIS_IN_TICK);
		return true;
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("engage"))
			return engage(activeChar);
		else if (command.startsWith("divorce"))
			return divorce(activeChar);
		else if (command.startsWith("gotolove"))
			return goToLove(activeChar);
		return false;
	}
}
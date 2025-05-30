package com.dream.game.network.clientpackets;

import com.dream.game.ai.CtrlEvent;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2SummonAI;
import com.dream.game.ai.NextAction;
import com.dream.game.ai.NextAction.NextActionCallback;
import com.dream.game.datatables.sql.PetSkillsTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.L2ManufactureList;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SiegeFlagInstance;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.model.actor.instance.L2StaticObjectInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ChairSit;
import com.dream.game.network.serverpackets.RecipeShopManageList;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2EffectType;

public class RequestActionUse extends L2GameClientPacket
{
	public static final int ACTION_SIT_STAND = 0;
	public static final int ACTION_MOUNT = 38;

	private int _actionId;
	private boolean _ctrlPressed, _shiftPressed;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
		if (activeChar.isFakeDeath() && _actionId != 0 || activeChar.isOutOfControl() || activeChar.isCastingNow())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final L2Summon pet = activeChar.getPet();
		final L2Object target = activeChar.getTarget();
		switch (_actionId)
		{
			case ACTION_SIT_STAND:
				if (activeChar.isSitting() || !activeChar.isMoving() || activeChar.isFakeDeath())
				{
					useSit(activeChar, target);
				}
				else
				{
					final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVE_TO, new NextActionCallback()
					{
						@Override
						public void doWork()
						{
							useSit(activeChar, target);
						}
					});

					activeChar.getAI().setNextAction(nextAction);
				}

				break;
			case 1:
				if (activeChar.isRunning())
				{
					activeChar.setWalking();
				}
				else
				{
					activeChar.setRunning();
				}
				break;
			case 10:
				activeChar.tryOpenPrivateSellStore(false);
				break;
			case 15:
			case 21:
				if (pet == null)
					return;

				if (pet instanceof L2SiegeSummonInstance && ((L2SiegeSummonInstance) pet).isOnSiegeMode())
				{
					activeChar.sendMessage("Impossible in siege mode.");
				}

				if (pet.isOutOfControl())
				{
					activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
					return;
				}

				if (pet instanceof L2SiegeSummonInstance)
				{
					((L2SiegeSummonInstance) pet).resetSiegeModeChange();
				}

				((L2SummonAI) pet.getAI()).notifyFollowStatusChange();
				break;
			case 16:
			case 22:
				if (target != null && pet != null && pet != target && activeChar != target && !pet.checkStartAttacking() && !pet.isOutOfControl())
				{
					if (pet instanceof L2PetInstance && pet.getLevel() - activeChar.getLevel() > 20)
					{
						activeChar.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
						return;
					}
					if (activeChar.isInOlympiadMode() && !activeChar.isOlympiadStart())
					{
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					if (!activeChar.allowPeaceAttack() && L2Character.isInsidePeaceZone(pet, target))
					{
						if (!activeChar.isInFunEvent() || !target.isInFunEvent())
						{
						activeChar.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
						return;
						}
					}
					if (pet.getNpcId() == 12564 || pet.getNpcId() == 12621)
					{
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					pet.setTarget(target);

					if (!target.isAutoAttackable(activeChar) && !_ctrlPressed && !(target instanceof L2NpcInstance))
					{
						pet.setFollowStatus(false);
						pet.getAI().setIntention(CtrlIntention.FOLLOW, target);
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
						return;
					}

					if (target.isAutoAttackable(activeChar) || _ctrlPressed)
					{
						if (target instanceof L2DoorInstance)
						{
							if (((L2DoorInstance) target).isAttackable(activeChar) && pet.getNpcId() != L2SiegeSummonInstance.SWOOP_CANNON_ID)
							{
								pet.getAI().setIntention(CtrlIntention.ATTACK, target);
							}
						}
						else if (pet.getNpcId() != L2SiegeSummonInstance.SIEGE_GOLEM_ID)
						{
							pet.getAI().setIntention(CtrlIntention.ATTACK, target);
						}
					}
					else
					{
						pet.setFollowStatus(false);
						pet.getAI().setIntention(CtrlIntention.FOLLOW, target);
					}
				}
				break;
			case 17:
			case 23:
				if (pet == null)
					return;

				if (pet.isOutOfControl())
				{
					activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
					return;
				}

				pet.getAI().setIntention(CtrlIntention.ACTIVE, null);
				break;

			case 19:
				if (pet != null && !pet.isOutOfControl())
					if (pet.isDead())
					{
						activeChar.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
					}
					else if (pet.isAttackingNow() || pet.isRooted() || pet.isInCombat())
					{
						activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
					}
					else if (pet instanceof L2PetInstance)
						if (!pet.isHungry())
						{
							if (pet.isInCombat())
							{
								activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
							}
							else
							{
								pet.unSummon(activeChar);
							}
						}
						else
						{
							activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
						}
				break;
			case ACTION_MOUNT:
				activeChar.mountPlayer(pet);
				break;
			case 28:
				activeChar.tryOpenPrivateBuyStore();
				break;
			case 32:
				if (pet instanceof L2SiegeSummonInstance)
				{
					((L2SiegeSummonInstance) pet).changeSiegeMode();
				}
			case 36:
				useSkill(4259, target);
				break;
			case 37:
				if (activeChar.isAlikeDead())
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != 0)
				{
					activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}

				if (activeChar.getCreateList() == null)
				{
					activeChar.setCreateList(new L2ManufactureList());
				}

				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 39:
				useSkill(4138, target);
				break;
			case 41:
				if (target != null && (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance))
				{
					if (pet instanceof L2SiegeSummonInstance && ((L2SiegeSummonInstance) pet).isOnSiegeMode())
					{
						useSkill(4230, target);
					}
					else
					{
						activeChar.sendMessage("Perhaps only in siege mode.");
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				}
				break;
			case 42:
				useSkill(4378, activeChar);
				break;
			case 43:
				useSkill(4137, target);
				break;
			case 44:
				useSkill(4139, target);
				break;
			case 45:
				useSkill(4025, activeChar);
				break;
			case 46:
				useSkill(4261, target);
				break;
			case 47:
				useSkill(4260, target);
				break;
			case 48:
				useSkill(4068, target);
				break;
			case 51:
				if (activeChar.isAlikeDead())
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != 0)
				{
					activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}

				if (activeChar.getCreateList() == null)
				{
					activeChar.setCreateList(new L2ManufactureList());
				}

				activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			case 52:
				if (pet != null && pet instanceof L2SummonInstance)
					if (pet.isOutOfControl())
					{
						activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
					}
					else if (pet.isAttackingNow() || pet.isInCombat())
					{
						activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
					}
					else if (pet.isDead())
					{
						activeChar.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
					}
					else
					{
						pet.unSummon(activeChar);
					}
				break;
			case 53:
				if (pet instanceof L2SiegeSummonInstance && ((L2SiegeSummonInstance) pet).isOnSiegeMode())
				{
					activeChar.sendMessage("Impossible in siege mode.");
					break;
				}
				else if (target != null && pet != null && pet != target && !pet.isMovementDisabled() && !pet.isOutOfControl())
				{
					pet.setFollowStatus(false);
					pet.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0));
				}
				if (pet instanceof L2SiegeSummonInstance)
				{
					((L2SiegeSummonInstance) pet).resetSiegeModeChange();
				}
				break;
			case 54:
				if (target != null && pet != null && pet != target && !pet.isMovementDisabled() && !pet.isOutOfControl())
				{
					pet.setFollowStatus(false);
					pet.getAI().setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0));
				}
				break;
			case 61:
				activeChar.tryOpenPrivateSellStore(true);
				break;
			case 1000:
				if (!(target instanceof L2DoorInstance))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}

				useSkill(4079, target);
				break;
			case 1001:
				useSkill(4139, pet);
				break;
			case 1003:
				useSkill(4710, target);
				break;
			case 1004:
				useSkill(4711, activeChar);
				break;
			case 1005:
				useSkill(4712, target);
				break;
			case 1006:
				useSkill(4713, activeChar);
				break;
			case 1007:
				useSkill(4699, activeChar);
				break;
			case 1008:
				useSkill(4700, activeChar);
				break;
			case 1009:
				useSkill(4701, target);
				break;
			case 1010:
				useSkill(4702, activeChar);
				break;
			case 1011:
				useSkill(4703, activeChar);
				break;
			case 1012:
				useSkill(4704, target);
				break;
			case 1013:
				useSkill(4705, target);
				break;
			case 1014:
				useSkill(4706, target);
				break;
			case 1015:
				useSkill(4707, target);
				break;
			case 1016:
				useSkill(4709, target);
				break;
			case 1017:
				useSkill(4708, target);
				break;
			case 1031:
				useSkill(5135, target);
				break;
			case 1032:
				useSkill(5136, target);
				break;
			case 1033:
				useSkill(5137, target);
				break;
			case 1034:
				useSkill(5138, target);
				break;
			case 1035:
				useSkill(5139, target);
				break;
			case 1036:
				useSkill(5142, target);
				break;
			case 1037:
				useSkill(5141, target);
				break;
			case 1038:
				useSkill(5140, target);
				break;
			case 1039:
				if (target instanceof L2DoorInstance)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}

				useSkill(5110, target);
				break;
			case 1040:
				if (target instanceof L2DoorInstance)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}

				useSkill(5111, target);
				break;
			default:
				_log.warn(activeChar.getName() + ": unhandled action type " + _actionId);
		}
	}

	public boolean useSit(L2PcInstance activeChar, L2Object target)
	{
		if (activeChar.getMountType() != 0)
			return false;

		if (target != null && !activeChar.isSitting() && target instanceof L2StaticObjectInstance && ((L2StaticObjectInstance) target).getType() == 1 && CastleManager.getInstance().getCastle(target) != null && activeChar.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
		{
			final ChairSit cs = new ChairSit(activeChar, ((L2StaticObjectInstance) target).getStaticObjectId());
			activeChar.sendPacket(cs);
			activeChar.sitDown();
			activeChar.broadcastPacket(cs);
			return false;
		}

		if (activeChar.isFakeDeath())
		{
			activeChar.stopEffects(L2EffectType.FAKE_DEATH);
		}
		else if (activeChar.isSitting())
		{
			activeChar.standUp();
		}
		else
		{
			activeChar.sitDown();
		}

		return true;
	}

	private void useSkill(int skillId, L2Object target)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isInStoreMode())
			return;

		L2Summon activeSummon = activeChar.getPet();

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendMessage("It is prohibited to use during trading.");
			return;
		}

		if (activeSummon != null)
		{
			if (activeSummon instanceof L2PetInstance && activeSummon.getLevel() - activeChar.getLevel() > 20)
			{
				activeChar.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
				return;
			}
			int lvl = PetSkillsTable.getInstance().getAvailableLevel(activeSummon, skillId);
			if (lvl == 0)
				return;
			if (activeSummon.isOutOfControl())
			{
				activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
				return;
			}

			L2Skill skill = SkillTable.getInstance().getInfo(skillId, lvl);
			if (skill == null)
				return;
			if (skill.isOffensive() && activeChar == target)
				return;

			activeSummon.setTarget(target);
			activeSummon.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
	}

}
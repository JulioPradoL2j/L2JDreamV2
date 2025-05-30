#Made by Emperorc
import sys
from java.lang import System
from com.dream import Config
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest import State
from com.dream.game.model.quest.jython import QuestJython as JQuest
from com.dream.game.network import SystemChatChannelId
from com.dream.game.network.serverpackets import CreatureSay
from com.dream.tools.random import Rnd
from com.dream.game.model.entity import GrandBossState
from com.dream.game.model.entity.GrandBossState import StateEnum

qn = "616_MagicalPowerOfFirePart2"

#NPC
Udan = 31379
Alter = 31558

#MOBS
Varka_Mobs = [ 21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, \
21362, 21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374, 21375 ]
Nastron = 25306

#ITEMS
Totem2 = 7243
Fire_Heart = 7244

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.gstate = GrandBossState(Nastron)	
     self.questItemIds = [Fire_Heart]
     test = self.loadGlobalQuestVar("616_respawn")
     if test.isdigit() :
        remain = long(test) - System.currentTimeMillis()
        if remain <= 0 :
           self.addSpawn(31558,142368,-82512,-6487,58000, False, 0, True)
        else :
           self.startQuestTimer("spawn_npc", remain, None, None)
     else :
        self.addSpawn(31558,142368,-82512,-6487,58000, False, 0, True)

 def onAdvEvent (self, event, npc, player) :
   if event == "Soul of Fire Nastron has despawned" :
       npc.reduceCurrentHp(9999999,npc,None)
       self.addSpawn(31558,142368,-82512,-6487,58000, False, 0, True)
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"The fetter strength is weaken Your consciousness has been defeated!"))
       return
   elif event == "spawn_npc" :
       self.addSpawn(31558,142368,-82512,-6487,58000, False, 0, True)
       return
   st = player.getQuestState(qn)
   if not st: return
   cond = st.getInt("cond")
   id = st.getInt("id")
   Green_Totem = st.getQuestItemsCount(Totem2)
   Heart = st.getQuestItemsCount(Fire_Heart)
   htmltext = event
   if event == "31379-04.htm" :
       if st.getPlayer().getLevel() >= 75 and st.getPlayer().getAllianceWithVarkaKetra() <= -2 :
           if Green_Totem :
                st.set("cond","1")
                st.set("id","1")
                st.setState(State.STARTED)
                st.playSound("ItemSound.quest_accept")
                htmltext = "31379-04.htm"
           else :
                htmltext = "31379-02.htm"
                st.exitQuest(1)
       else :
           htmltext = "31379-03.htm"
           st.exitQuest(1)
   elif event == "31379-08.htm" :
       if Heart:
           htmltext = "31379-08.htm"
           st.takeItems(Fire_Heart,-1)
           st.addExpAndSp(10000,1000)
           st.unset("id")
           st.unset("cond")
           st.playSound("ItemSound.quest_finish")
           st.exitQuest(1)
       else :
           htmltext = "31379-09.htm"
   elif event == "31558-02.htm" :
       if Green_Totem == 0 :
           htmltext = "31558-04.htm"
       else:
	   if self.gstate.getState() == StateEnum.INTERVAL :
              if System.currentTimeMillis() < self.gstate.getRespawnDate() :
                    return "<html><body><center><br>No time to call</body></html>"
           spawnedNpc = st.addSpawn(Nastron,142528,-82528,-6496)
           st.takeItems(Totem2,1)
           st.set("id","2")
           npc.deleteMe()
           st.set("cond","2")
           self.startQuestTimer("Soul of Fire Nastron has despawned",1200000,spawnedNpc,None)
           spawnedNpc.broadcastPacket(NpcSay(spawnedNpc.getObjectId(),0,spawnedNpc.getNpcId(),"The fire charm then is the flame and the lava strength! Opposes with it only has the blind alley!"))
   return htmltext

 def onTalk (self, npc, player):
    st = player.getQuestState(qn)
    htmltext = "<html><body><br><br>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
    if st :
        npcId = npc.getNpcId()
        cond = st.getInt("cond")
        id = st.getInt("id")
        Green_Totem = st.getQuestItemsCount(Totem2)
        Heart = st.getQuestItemsCount(Fire_Heart)
        if npcId == Udan :
            if st.getState()== State.CREATED :
                htmltext = "31379-01.htm"
            elif id == 1 or id == 2 :
                htmltext = "31379-05.htm"
            elif id == 3:
                if Heart :
                    htmltext = "31379-06.htm"
                else :
                    htmltext = "31379-07.htm"
        elif npcId == Alter :
            htmltext = "31558-01.htm"
    return htmltext

 def onKill(self,npc,player,isPet):
    npcId = npc.getNpcId()
    if npcId == Nastron :
        respawnMinDelay = 43200000  * int(Config.RAID_MIN_RESPAWN_MULTIPLIER)
        respawnMaxDelay = 129600000 * int(Config.RAID_MAX_RESPAWN_MULTIPLIER)
        respawn_delay = Rnd.get(respawnMinDelay,respawnMaxDelay)
        self.saveGlobalQuestVar("616_respawn", str(System.currentTimeMillis()+respawn_delay))
        self.startQuestTimer("spawn_npc", respawn_delay, None, None)
        self.cancelQuestTimer("Soul of Fire Nastron has despawned",npc,None)
        self.gstate.setRespawnDate(24*60*60000) 
        self.gstate.setState(StateEnum.INTERVAL)
        party = player.getParty()
        if party :
            PartyQuestMembers = []
            for player1 in party.getPartyMembers().toArray() :
                st1 = player1.getQuestState(qn)
                if st1 :
                    if st1.getState() == State.STARTED and (st1.getInt("cond") == 1 or st1.getInt("cond") == 2) :
                        PartyQuestMembers.append(st1)
            if len(PartyQuestMembers) == 0 : return
            st = PartyQuestMembers[Rnd.get(len(PartyQuestMembers))]
            if st.getQuestItemsCount(Totem2) > 0 :
                st.takeItems(Totem2,1)
            st.giveItems(Fire_Heart,1)
            st.set("cond","3")
            st.set("id","3")
            st.playSound("ItemSound.quest_middle")
        else :
            st = player.getQuestState(qn)
            if not st : return
            if st.getState() == State.STARTED and (st.getInt("cond") == 1 or st.getInt("cond") == 2) :
                if st.getQuestItemsCount(Totem2) > 0 :
                    st.takeItems(Totem2,1)
                st.giveItems(Fire_Heart,1)
                st.set("cond","3")
                st.set("id","3")
                st.playSound("ItemSound.quest_middle")
    elif npcId in Varka_Mobs :
        st = player.getQuestState(qn)
        if st :
            if st.getQuestItemsCount(Fire_Heart) :
                st.takeItems(Fire_Heart,-1)
            st.unset("cond")
            st.unset("id")
            st.exitQuest(1)
    return

QUEST = Quest(616,qn,"Magical Power of Fire - Part 2")

QUEST.addStartNpc(Udan)

QUEST.addTalkId(Udan)
QUEST.addTalkId(Alter)

QUEST.addKillId(Nastron)

for mobId in Varka_Mobs:
    QUEST.addKillId(mobId)
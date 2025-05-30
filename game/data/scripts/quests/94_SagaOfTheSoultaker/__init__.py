# Made by Emperorc - Retyped and adapted by lDante/Daniel 
import sys
from com.dream.game.ai import CtrlIntention
from com.dream.game.datatables.sql import SpawnTable
from com.dream.game.model.world import L2World
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest
from com.dream.game.network.serverpackets import NpcSay
from com.dream.game.network.serverpackets import MagicSkillUse
from com.dream.tools.random import Rnd

qn = "94_SagaOfTheSoultaker"

#MOBS

Archon_Minions = range(21646,21652)
Guardian_Angels = [27214, 27215, 27216]
Archon_Hellisha_Norm = [18212, 18214, 18215, 18216, 18218]
Mob = [27257, 27243, 27265]

MOBUM = 27257
MOBDOIS = 27243
MOBTRES = 27265

#LOCS

XZERO = 191046
XUM = 46087
XDOIS = 46066
YZERO = -40640
YUM = -36396
YDOIS = -36372
ZZERO = -3042
ZUM = -1685
ZDOIS = -1685

#NPC

HARDIN = 30832
BAVARIN = 31623
GREGORY = 31279
GREGORY1 = 31279
HINDEMITH = 31645
TABLET1 = 31646
TABLET2 = 31648
TABLET3 = 31650
TABLET4 = 31654
TABLET5 = 31655
TABLET6 = 31657
GREGORY2 = 31279


#ITEM

ITEMHOTSPRING = 7080
ITEMHOT1 = 7533
DIVINESTONE = 7081
HALISHAMARK = 7509
AMULET1 = 7292
AMULET2 = 7323
AMULET3 = 7354
AMULET4 = 7385
AMULET5 = 7416
AMULET6 = 7447
ITEMINICIAL = 7085
ITEM0 = 0
Items = [7080,7533,7081,7509,7292,7323,7354,7385,7416,7447,7085,0]

#CLASSES

classid = 95
prevclass = 0x0d
QuestClass = 0x0d

PartyQuestMembers = []


qn = "94_SagaOfTheSoultaker"
Spawn_List = []

class Quest (JQuest) :

 def __init__(self,id,name,descr): 
     JQuest.__init__(self,id,name,descr)
     questItemIds = [7080,7533,7081,7509,7292,7323,7354,7385,7416,7447,7085,0]+[ITEMINICIAL,ITEMHOTSPRING,ITEMHOT1,AMULET1,AMULET2,AMULET3,AMULET4,AMULET5,AMULET6,HALISHAMARK,DIVINESTONE,ITEM0]	  

 def FindTemplate (self, npcId) :
    for spawn in SpawnTable.getInstance().getSpawnTable().values():
        if spawn.getNpcid() == npcId:
            npcinstance = spawn.getLastSpawn()
            break
    return npcinstance	
	
 def FindSpawn (self, player, npcObjectId) :
    for mobId, playerName, mob in Spawn_List:
        if mobId == npcObjectId and playerName == player.getName():
            return mob
    return None

 def DeleteSpawn(self, st,mobId) :
    name = st.getPlayer().getName()
    for npcId,playerName,mob in Spawn_List:
        if (mobId,name) ==  (npcId,playerName):
            Spawn_List.remove([mobId,name,mob])
            mob.decayMe()
            return
    return

 def giveHallishaMark(self, st2) :
     if st2.getInt("spawned") == 0 :
        if st2.getQuestItemsCount(HALISHAMARK) >= 700:
            st2.takeItems(HALISHAMARK,20)
            xx = int(st2.getPlayer().getX())
            yy = int(st2.getPlayer().getY())
            zz = int(st2.getPlayer().getZ())
            Archon = st2.addSpawn(MOBDOIS,xx,yy,zz)
            ArchonId = Archon.getObjectId()
            st2.startQuestTimer("Archon Hellisha has despawned",600000,Archon)
            Archon.broadcastPacket(NpcSay(Archon.getObjectId(),0,Archon.getNpcId(),"Only engaged in the battle to bar their choice. Perhaps you should regret."))				
            Archon.addDamageHate(st2.getPlayer(),0,99999)			
            Archon.getAI().setIntention(CtrlIntention.ATTACK,st2.getPlayer(),None)		
        else :
            st2.giveItems(HALISHAMARK,1)
     return

 def onAdvEvent (self,event,npc, player) :
   st = player.getQuestState(qn)
   if not st: return
   htmltext = ""  # simple initialization...if none of the events match, return nothing.  
   cond = st.getInt("cond")
   id = st.getInt("id")
   player = st.getPlayer()
   if event == "accept" :
       st.setState(State.STARTED)   
       st.set("cond","1")
       st.playSound("ItemSound.quest_accept")
       st.giveItems(ITEMINICIAL,1)
       htmltext = "0-03.htm"
   elif event == "0-1" :
       if player.getLevel() < 76 :
           htmltext = "0-02.htm"
           st.exitQuest(1)
       else :
           htmltext = "0-05.htm"
   elif event == "0-2" :
       if player.getLevel() >= 76 :
           st.set("cond","0")
           htmltext = "0-07.htm"
           st.takeItems(ITEMINICIAL,-1)
           st.addExpAndSp(2299404,0)
           st.giveItems(57,5000000)
           st.giveItems(6622,1)
           player.setClassId(classid)
           st.exitQuest(1)
           if not player.isSubClassActive() and player.getBaseClass() == prevclass :
               player.setBaseClass(classid)
           player.broadcastUserInfo()
       else :
           st.takeItems(ITEMINICIAL,-1)
           st.playSound("ItemSound.quest_middle")
           st.set("cond","20")
           htmltext = "0-08.htm"
		   
   elif event == "1-3" :
       st.set("cond","3")
       htmltext = "1-05.htm"
   elif event == "1-4" :
       st.set("cond","4")
       st.takeItems(ITEMHOTSPRING,1)
       st.takeItems(ITEM0,1)		   
       if ITEMINICIAL != 0 :
           st.takeItems(ITEMINICIAL,1)
       st.giveItems(ITEMHOT1,1)
       htmltext = "1-06.htm" 
   elif event == "2-1" :
       st.set("cond","2")
       htmltext = "2-05.htm"
   elif event == "2-2" :
       st.set("cond","5")
       st.takeItems(ITEMHOT1,1)
       st.giveItems(AMULET1,1)
       htmltext = "2-06.htm"
   elif event == "3-5" :
       htmltext = "3-07.htm"
   elif event == "3-6" :
       st.set("cond","11")
       htmltext = "3-02.htm"
   elif event == "3-7" :
       st.set("cond","12")
       htmltext = "3-03.htm"
   elif event == "3-8" :
       st.set("cond","13")
       st.takeItems(DIVINESTONE,1)
       st.giveItems(AMULET4,1)
       htmltext = "3-08.htm"
   elif event == "4-1" :
       htmltext = "4-010.htm"
   elif event == "4-2" :
       st.giveItems(AMULET6,1)
       st.set("cond","18")
       st.playSound("ItemSound.quest_middle")
       htmltext = "4-011.htm"
   elif event == "4-3" :
       st.giveItems(AMULET6,1)
       st.set("cond","18")
       st.set("Quest0","0")
       st.set("Quest1","0")	 
       npc.deleteMe()
       st.playSound("ItemSound.quest_middle")
       return
   elif event == "5-1" :
       st.set("cond","6")
       st.takeItems(AMULET1,1)
       st.playSound("ItemSound.quest_middle")
       htmltext =  "5-02.htm"
   elif event == "6-1" :
       st.set("cond","8")
       st.takeItems(AMULET2,1)
       st.playSound("ItemSound.quest_middle")
       htmltext =  "6-03.htm"
   elif event == "7-1" :
       if st.getInt("spawned") == 1 :
           htmltext = "7-03.htm"
       elif st.getInt("spawned") == 0 :
           Mob_1 = st.addSpawn(MOBUM,XZERO,YZERO,ZZERO)	   
           st.set("spawned","1")
           st.startQuestTimer("Mob_1 Timer 1",500,Mob_1)
           st.startQuestTimer("Mob_1 has despawned",300000,Mob_1)
           Mob_1.addDamageHate(player,0,99999)
           Mob_1.getAI().setIntention(CtrlIntention.ATTACK,player,None)		   
           htmltext = "7-02.htm"
       else :
           htmltext = "7-04.htm"
   elif event == "7-2" :
       st.set("cond","10")
       st.takeItems(AMULET3,1)
       st.set("spawned","0")	   
       st.playSound("ItemSound.quest_middle")
       htmltext = "7-06.htm"
   elif event == "8-1" :
       st.set("cond","14")
       st.takeItems(AMULET4,1)
       st.playSound("ItemSound.quest_middle")
       htmltext = "8-02.htm"
   elif event == "9-1" :
       st.set("cond","17")
       st.takeItems(AMULET5,1)
       st.playSound("ItemSound.quest_middle")
       htmltext = "9-03.htm"
   elif event == "10-1" :
       if st.getInt("Quest0") == 0 and not player.isInParty() :
           Mob_2 = st.addSpawn(MOBTRES,XUM,YUM,ZUM)
           Mob_3 = st.addSpawn(HINDEMITH,XDOIS,YDOIS,ZDOIS)
           st.set("Mob_2",str(Mob_2.getObjectId()))
           st.set("Mob_3",str(Mob_3.getObjectId()))
           st.set("Quest0","0")
           st.set("Quest1","45")
           st.startQuestTimer("Mob_3 Timer 1",500,Mob_2)
           st.startQuestTimer("Mob_3 has despawned",59000,Mob_2)
           st.startQuestTimer("Mob_2 Timer 1",500,Mob_3)
           st.startQuestTimer("Mob_2 has despawned",60000,Mob_3)
           htmltext = "10-02.htm"
       elif st.getInt("Quest1") == 45 :
           htmltext = "10-03.htm"
       else :
           htmltext = "10-04.htm"
   elif event == "10-2" :
       st.set("cond","19")
       st.takeItems(AMULET6,1)
       st.playSound("ItemSound.quest_middle")
       htmltext = "10-06.htm"
   elif event == "11-9" :
       st.set("cond","15")
       htmltext = "11-03.htm"
   elif event == "Mob_1 Timer 1" :
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),""+st.getPlayer().getName()+", Pursued to here! However, I jumped out of the Banshouren boundaries! You look at the giant as the sign of power!"))
       return
   elif event == "Mob_1 has despawned" :
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"... Oh ... good! So it was ... let's begin!"))
       npc.deleteMe()	   
       st.set("spawned","0")
       return
   elif event == "Archon of Hellisha has despawned" :
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"This is a waste of time.. Say goodbye...!"))   
       npc.deleteMe()
       st.set("spawned","0")
       return
   elif event == "Mob_3 Timer 1" :  
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"The human nation was foolish to try and fight a giant's strength."))
       return
   elif event == "Mob_3 has despawned" :
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Must...Retreat... Too...Strong."))   
       npc.deleteMe()
       return
   elif event == "Mob_2 Timer 1" :
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"...That is the enemy"))      
       st.startQuestTimer("Mob_2 Timer 2",1500,npc)
       if st.getInt("Quest1") == 45 :
           st.set("Quest1","0")
       return
   elif event == "Mob_2 Timer 2" :
       npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"...Goodness! "+st.getPlayer().getName()+" you are still looking?"))      
       st.startQuestTimer("Mob_2 Timer 3",10000,npc)
       return
   elif event == "Mob_2 Timer 3" :
       if st.getInt("Quest0") == 0 :
           st.startQuestTimer("Mob_2 Timer 3",13000,npc)
           if st.getRandom(2) == 0 :
               npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),""+st.getPlayer().getName()+" ... Not just to whom the victory. Only personnel involved in the fighting are eligible to share in the victory."))      
           else :
               npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Your sword is not an ornament. Don't you think "+st.getPlayer().getName()+"?"))      
       return
   elif event == "Mob_2 has despawned" :
       st.set("Quest1",str(st.getInt("Quest1")+1))
       if st.getInt("Quest0") == 1 or st.getInt("Quest0") == 2 or st.getInt("Quest1") > 3 :
           st.set("Quest0","0")
           if st.getInt("Quest0") == 1 :
               npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Goodness! I no longer sense a battle there now."))      
           else :
               npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"let..."))      
           npc.deleteMe()
       else :
           st.startQuestTimer("Mob_2 has despawned",1000,npc)
       return
   return htmltext

 def onTalk (self,npc,player):
    htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
    st = player.getQuestState(qn)
    if st :
      npcId = npc.getNpcId()
      cond = st.getInt("cond")
      if player.getClassId().getId() == prevclass :
          if cond == 0 :
              if npcId == HARDIN:
                  htmltext = "0-01.htm"
          elif cond == 1 :
              if npcId == HARDIN :
                  htmltext = "0-04.htm"
              elif npcId == GREGORY :
                  htmltext = "2-01.htm"
          elif cond == 2 :
              if npcId == GREGORY :
                  htmltext = "2-02.htm"
              elif npcId == BAVARIN :
                  htmltext = "1-01.htm"
          elif cond == 3 :
              if npcId == BAVARIN :
                  if st.getQuestItemsCount(ITEMHOTSPRING) : 
                      if 0 == 0 :
                          htmltext = "1-03.htm"
                      elif st.getQuestItemsCount(0) :
                          htmltext = "1-03.htm"
                      else :
                          htmltext = "1-02.htm"
                  else :
                      htmltext = "1-02.htm"
          elif cond == 4 :
              if npcId == BAVARIN :
                  htmltext = "1-04.htm"
              elif npcId == GREGORY :
                  htmltext = "2-03.htm"
          elif cond == 5 :
              if npcId == GREGORY :
                  htmltext = "2-04.htm"
              elif npcId == TABLET1 :
                  htmltext = "5-01.htm"
          elif cond == 6 :
              if npcId == TABLET1 :
                  htmltext = "5-03.htm"
              elif npcId == TABLET2 :
                  htmltext = "6-01.htm"
          elif cond == 7 :
              if npcId == TABLET2 :
                  htmltext = "6-02.htm"
          elif cond == 8 and not player.isInParty() :
              if npcId == TABLET2 :
                  htmltext = "6-04.htm"
              elif npcId == TABLET3 :
                  htmltext = "7-01.htm"
          elif cond == 9 :
              if npcId == TABLET3 :
                  htmltext = "7-05.htm"
          elif cond == 10 :
              if npcId == TABLET3 :
                  htmltext = "7-07.htm"
              elif npcId == GREGORY1 :
                  htmltext = "3-01.htm"
          elif cond == 11 or cond == 12 :
              if npcId == GREGORY1 :
                  if st.getQuestItemsCount(DIVINESTONE) :
                      htmltext = "3-05.htm"
                  else :
                      htmltext = "3-04.htm"
          elif cond == 13 :
              if npcId == GREGORY1 :
                  htmltext = "3-06.htm"
              elif npcId == TABLET4 :
                  htmltext = "8-01.htm"
          elif cond == 14 :
              if npcId == TABLET4 :
                  htmltext = "8-03.htm"
              elif npcId == GREGORY2 :
                  htmltext = "11-01.htm"
          elif cond == 15 :
              if npcId == GREGORY2 :
                  htmltext = "11-02.htm"
              elif npcId == TABLET5 :
                  htmltext = "9-01.htm"
          elif cond == 16 :
              if npcId == TABLET5 :
                  htmltext = "9-02.htm"
          elif cond == 17 :
              if npcId == TABLET5 :
                  htmltext = "9-04.htm"
              elif npcId == TABLET6 :
                  htmltext = "10-01.htm"   
              if npcId == HINDEMITH :
                  if st.getInt("Quest0") == 0 :
                      htmltext = "4-04.htm"
                  elif st.getInt("Quest0") == 1 :
                      htmltext = "4-06.htm"
                  elif st.getInt("Quest0") == 3 :
                      st.set("Quest0","0")					  
                      st.set("Quest1","0")					  
                      npc.deleteMe()	
          elif cond == 18 :
              if npcId == TABLET6 :
                  htmltext = "10-05.htm"
          elif cond == 19 :
              if npcId == TABLET6 :
                  htmltext = "10-07.htm"
              if npcId == HARDIN :
                  htmltext = "0-06.htm"
          elif cond == 20 :
              if npcId == HARDIN :
                  if player.getLevel() >= 76 :
                      st.set("cond","0")
                      htmltext = "0-07.htm"
                      st.addExpAndSp(2299404,0)
                      st.giveItems(57,5000000)
                      st.giveItems(6622,1)
                      player.setClassId(classid)
                      if not player.isSubClassActive() and player.getBaseClass() == prevclass :
                          player.setBaseClass(classid)
                      player.broadcastUserInfo()
                  else :
                      htmltext = "0-010.htm"
    return htmltext

 def onFirstTalk (self,npc,player):
    htmltext = ""
    st = player.getQuestState(qn)
    npcId = npc.getNpcId()
    if st :
      cond = st.getInt("cond")
      if npcId == HINDEMITH :
          if cond == 17 :
              st2 = player.getQuestState(qn)
              if st2 :
                  if st == st2 :
                      if st.getInt("Tab") == 1 :
                          if st.getInt("Quest0") == 0 :
                              htmltext = "4-04.htm"
                          elif st.getInt("Quest0") == 1 :
                              htmltext = "4-06.htm"
                      else :
                          if st.getInt("Quest0") == 0 :
                              htmltext = "4-01.htm"
                          elif st.getInt("Quest0") == 1 :
                              htmltext = "4-03.htm"
                  else:
                      if st.getInt("Tab") == 1 :
                          if st.getInt("Quest0") == 0 :
                              htmltext = "4-05.htm"
                          elif st.getInt("Quest0") == 1 :
                              htmltext = "4-07.htm"
                      else :
                          if st.getInt("Quest0") == 0 :
                              htmltext = "4-02.htm"
          elif cond == 18 :
              htmltext = "4-08.htm"
    return htmltext

 def onAttack (self, npc, player, damage, isPet, skill):
   st = player.getQuestState(qn)
   if st :
    npcId = npc.getNpcId()
    maxHp = npc.getMaxHp()
    nowHp = npc.getStatus().getCurrentHp()
    cond = st.getInt("cond")   
    if cond == 17 :
        if npcId == MOBTRES :
            st2 = player.getQuestState(qn)
            if not player.isInParty():		
                if st == st2 :
                    st.set("Quest0",str(st.getInt("Quest0")+1))
                    if st.getInt("Quest0") == 1 :
                        npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),""+st.getPlayer().getName()+". Defeat...by...retaining...and...Mo...Hacker"))
                    if nowHp < maxHp*0.95 and st.getInt("Quest0") > 15 :
                        st.set("Quest0","1")					
                        npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"....! Fight...Defeat...It...Fight...Defeat...It..."))
                        npc.deleteMe()				
                        if st.getQuestTimer("Mob_3 has despawned") :				    
                           st.getQuestTimer("Mob_3 has despawned").cancel()
                        st.set("Tab","1")							   
            else :
                st.set("Quest0","3")
                st.set("Mob_2","0")				
                st.set("Mob_3","0")								
                npc.deleteMe()
                if st.getQuestTimer("Mob_3 has despawned") :				
                   st.getQuestTimer("Mob_3 has despawned").cancel()					
   return

 def onKill(self,npc,player,isPet):
    npcId = npc.getNpcId()
    st = player.getQuestState(qn)
    if npcId in Archon_Minions :
        party = player.getParty()
        if party :
            PartyQuestMembers = []
            for partyMember in party.getPartyMembers().toArray() :
                    pst = partyMember.getQuestState(qn)
                    if pst :
                        if partyMember.getClassId().getId() == prevclass:					
                            if pst.getInt("cond") == 15 :
                                PartyQuestMembers.append(pst)
            if len(PartyQuestMembers) > 0 :
                st2 = PartyQuestMembers[Rnd.get(len(PartyQuestMembers))]
                st2.getQuest().giveHallishaMark(st2)
        else :
                st1 = player.getQuestState(qn)
                if st1 :
                    if player.getClassId().getId() == prevclass:
                        if st1.getInt("cond") == 15 :
                           st1.getQuest().giveHallishaMark(st1)
    elif npcId in Archon_Hellisha_Norm :
        party = player.getParty()
        if party :
            PartyQuestMembers = []
            for partyMember in party.getPartyMembers().toArray() :
                    pst = partyMember.getQuestState(qn)
                    if pst :
                        if partyMember.getClassId().getId() == prevclass:					
                            if pst.getInt("cond") == 15 :
						        npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ..."))							
						        pst.giveItems(AMULET5,1)
						        pst.takeItems(HALISHAMARK,-1)
						        pst.playSound("ItemSound.quest_middle")
						        pst.set("cond","16")
        else :
            st1 = player.getQuestState(qn)
            if st1 :
                if player.getClassId().getId() == prevclass:
                    if st1.getInt("cond") == 15 :
                        npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ..."))					
                        st1.giveItems(AMULET5,1)
                        st1.takeItems(HALISHAMARK,-1)
                        st1.set("cond","16")
                        st1.playSound("ItemSound.quest_middle")
    elif npcId in Guardian_Angels :
            st1 = player.getQuestState(qn)
            if st1 :
                if player.getClassId().getId() == prevclass :
                    if st1.getInt("cond") == 6 :
                        if st1.getInt("kills") < 9 :
                            st1.set("kills",str(st1.getInt("kills")+1))
                        else :
                            st1.playSound("ItemSound.quest_middle")
                            st1.giveItems(AMULET2,1)
                            st1.set("cond","7")
    elif st :
        cond = st.getInt("cond")
        if npcId == MOBUM :
            st2 = player.getQuestState(qn)
            if st2 :
                if not player.isInParty():
                    if st == st2 :
                        npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"let..."))										
                        st.giveItems(AMULET3,1)				
                        st.set("cond","9")
                        st.playSound("ItemSound.quest_middle")
                        if st2.getQuestTimer("Mob_1 has despawned") :
                            st2.getQuestTimer("Mob_1 has despawned").cancel()
                else :
                    st.set("spawned","0")
                    npc.deleteMe()
                    if st.getQuestTimer("Mob_1 has despawned") :				    
                       st.getQuestTimer("Mob_1 has despawned").cancel()
        elif npcId == MOBDOIS :
            if cond == 15 :
                st2 = player.getQuestState(qn)
                if st2 :
                    if not player.isInParty():
                        if st == st2 :
                            npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ..."))										
                            st.giveItems(AMULET5,1)
                            st.takeItems(HALISHAMARK,-1)
                            st.set("cond","16")
                            st.playSound("ItemSound.quest_middle")
                        else :
						    npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Why do you interfere others' battles?"))										
                    if st2.getQuestTimer("Archon Hellisha has despawned") :
                        st2.getQuestTimer("Archon Hellisha has despawned").cancel()
        if npcId == MOBTRES :
            st2 = player.getQuestState(qn)
            if st2 :
                if not player.isInParty():
                    if st == st2 :
                        st.set("Quest0","1")
                        st.playSound("ItemSound.quest_middle")
                if st2.getQuestTimer("Mob_1 has despawned") :
                    st2.getQuestTimer("Mob_1 has despawned").cancel()						
    else :
        if npcId == MOBUM :
            st = player.getQuestState(qn)
            if st:
                if st.getQuestTimer("Mob_1 has despawned") :
                    st.getQuestTimer("Mob_1 has despawned").cancel()
                    DeleteSpawn(st,st.getInt("Mob_1"))

        elif npcId == MOBDOIS :
            st = player.getQuestState(qn)
            if st:
                if st.getQuestTimer("Archon Hellisha has despawned") :
                    st.getQuestTimer("Archon Hellisha has despawned").cancel()
                DeleteSpawn(st,st.getInt("Archon"))
				
        elif npcId ==MOBTRES :
            st = player.getQuestState(qn)
            if st:
                if st.getQuestTimer("Archon Hellisha has despawned") :
                    st.getQuestTimer("Archon Hellisha has despawned").cancel()
                DeleteSpawn(st,st.getInt("Mob_3"))				
    return

QUEST = Quest(94,qn,"Saga of the Soultaker")

QUEST.addStartNpc(HARDIN)

QUEST.addAttackId(MOBTRES)

QUEST.addTalkId(HARDIN)
QUEST.addTalkId(BAVARIN)
QUEST.addTalkId(GREGORY)
QUEST.addTalkId(GREGORY1)
QUEST.addTalkId(HINDEMITH)
QUEST.addTalkId(TABLET1)
QUEST.addTalkId(TABLET2)
QUEST.addTalkId(TABLET3)
QUEST.addTalkId(TABLET4)
QUEST.addTalkId(TABLET5)
QUEST.addTalkId(TABLET6)
QUEST.addTalkId(GREGORY2)

QUEST.addKillId(21646)
QUEST.addKillId(21647)
QUEST.addKillId(21648)
QUEST.addKillId(21649)
QUEST.addKillId(21650)
QUEST.addKillId(21651)
QUEST.addKillId(21652)
QUEST.addKillId(27214)
QUEST.addKillId(27215)
QUEST.addKillId(27216)
QUEST.addKillId(18212)
QUEST.addKillId(18214)
QUEST.addKillId(18215)
QUEST.addKillId(18216)
QUEST.addKillId(18218)
QUEST.addKillId(MOBUM)
QUEST.addKillId(MOBDOIS)
QUEST.addKillId(MOBTRES)
# Created by DrLecter, based on DraX' scripts
# This script is part of the L2J Datapack Project
# Visit us at http://www.l2jdp.com/
# See readme-dp.txt and gpl.txt for license and distribution details
# Let us know if you did not receive a copy of such files.
import sys
from com.dream.game.model.quest        import State
from com.dream.game.model.quest        import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "7002_ClassMaster"
#Items
ADENA    = 57
MEDAL    = 6392
GMEDAL    = 6393
BOOK_GIANT    = 6622
GOLDBAR    = 3470

#[],[],[],[],[],[],[],[],[],[]
#1st = requeriment
#2st = requeriment
#3st = requeriment
#4st = requeriment
#5st = requeriment
#6st = reward
#7st = reward
#8st = reward
#9st = reward
#10st = reward
#example
#[MEDAL],[GMEDAL],[],[],[],[BOOK_GIANT],[],[],[],[]
#Need 1 Glitenring medal and 1 medal to accept classes and receive Book Gigant reward
#To edit the amount of reward or requirement you need to go to the def change option below, you will find this:
#def change(st,player,newclass,items,items1,items2,items3,items4,rewards,rewards1,rewards2,rewards3,rewards4) :
#   for item in items :
#      st.takeItems(item,1) -> Requeriment
#   for item1 in items1 :
#      st.takeItems(item1,1)  -> Requeriment    
#   for item2 in items2 :
#      st.takeItems(item2,1)  -> Requeriment    	  
#   for item3 in items3 :
#      st.takeItems(item3,1)  -> Requeriment    	
#   for item4 in items4 :
#      st.takeItems(item4,1)  -> Requeriment    	
#   for reward in rewards :
#      st.giveItems(reward,1)  -> Reward    	  	  
#   for reward1 in rewards1 :
#      st.giveItems(reward1,1)  -> Reward    	  	    
#   for reward2 in rewards2 :
#      st.giveItems(reward2,1)  -> Reward    	  	    	  
#   for reward3 in rewards3 :
#      st.giveItems(reward3,1)  -> Reward    	  	    	
#   for reward4 in rewards4 :
#      st.giveItems(reward4,1)  -> Reward    	  	    	
#Class Master
NPCS=[50032]
#Reward Item
SHADOW_WEAPON_COUPON_CGRADE = 8870
#event:[newclass,req_class,req_race,required_level,low_ni,low_i,ok_ni,ok_i,req_item,req_item1,req_item2,req_item3,req_item4,reward_1,reward_2,reward_3,reward_4,reward_5]
#low_ni : level too low, and you dont have quest item
#low_i: level too low, despite you have the item
#ok_ni: level ok, but you don't have quest item
#ok_i: level ok, you got quest item, class change takes place
CLASSES = {
    "GL":[2,1,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Gladiador
    "WL":[3,1,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Warlord
    "PL":[5,4,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Paladin
    "DA":[6,4,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Dark Avenger
    "TH":[8,7,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Treasure Hunter
    "HE":[9,7,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Hawkeye
    "HS":[12,11,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Sorcerer
    "HN":[13,11,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Necromancer
    "HW3":[14,11,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Warlock
    "BI":[16,15,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Bishop
    "PH":[17,15,0,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Prophet	
    "TK":[20,19,1,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Temple Knight
    "SS":[21,19,1,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #SwordSinger
    "PW":[23,22,1,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Plains Walker
    "SR":[24,22,1,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Silver Ranger
    "EW":[27,26,1,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #SpellSinger
    "ES":[28,26,1,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Elemental Summoner
    "EE":[30,29,1,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Elven Elder
    "SK":[33,32,2,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Shillen Knight
    "BD":[34,32,2,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Blade Dancer
    "AW":[36,35,2,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Abbys Walker
    "PR":[37,35,2,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Phantom Ranger	
    "SH":[40,39,2,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #SpellHowler
    "PS":[41,39,2,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Phantom Summoner
    "SE":[43,42,2,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Shillen Elder
    "DE":[46,45,3,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Destroyer
    "TY":[48,47,3,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Tyrant
    "OL":[51,50,3,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Overlord
    "WC":[52,50,3,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Warcryer
    "WS":[57,56,4,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Warsmith
    "BH":[55,54,4,40,"19","20","21","22",[],[],[],[],[],[],[],[],[],[]], #Bounty Hunter	
    }
#Messages
default = "No Quest"

def change(st,player,newclass,items,items1,items2,items3,items4,rewards,rewards1,rewards2,rewards3,rewards4) :
   for item in items :
      st.takeItems(item,1)
   for item1 in items1 :
      st.takeItems(item1,1)    
   for item2 in items2 :
      st.takeItems(item2,1)    	  
   for item3 in items3 :
      st.takeItems(item3,1)    	
   for item4 in items4 :
      st.takeItems(item4,1)    	
   for reward in rewards :
      st.giveItems(reward,1)    	  	  
   for reward1 in rewards1 :
      st.giveItems(reward1,1)  
   for reward2 in rewards2 :
      st.giveItems(reward2,1)  	  
   for reward3 in rewards3 :
      st.giveItems(reward3,1)  	
   for reward4 in rewards4 :
      st.giveItems(reward4,1)  	
   st.playSound("ItemSound.quest_fanfare_2")
   player.setClassId(newclass)
   player.setBaseClass(newclass)
   player.broadcastUserInfo()
   return

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc,player) :
   npcId    = npc.getNpcId()
   htmltext = default
   suffix = ''
   st = player.getQuestState(qn)
   if not st : return
   race     = player.getRace().ordinal()
   classid  = player.getClassId().getId()
   level    = player.getLevel()
   if npcId not in NPCS : return
   if not event in CLASSES.keys() :
     return event
   else :
     newclass,req_class,req_race,required_level,low_ni,low_i,ok_ni,ok_i,req_item,req_item1,req_item2,req_item3,req_item4,reward_1,reward_2,reward_3,reward_4,reward_5 = CLASSES[event]
     if race == req_race and classid == req_class :
        item = True
        for i in req_item :
            if st.getQuestItemsCount(i) < 1:
               item = False
        item1 = True
        for i in req_item1 :
            if st.getQuestItemsCount(i) < 1:
               item1 = False		
        item2 = True			   
        for i in req_item2 :
            if st.getQuestItemsCount(i) < 1:
               item2 = False	
        item3 = True			   
        for i in req_item3 :
            if st.getQuestItemsCount(i) < 1:
               item3 = False	
        item4 = True			   
        for i in req_item4 :
            if st.getQuestItemsCount(i) < 1:
               item4 = False	
        reward = True			   
        for i in reward_1 :
            if not st.getQuestItemsCount(i):
               reward = False				   
        reward1 = True			   
        for i in reward_2 :
            if not st.getQuestItemsCount(i):
               reward1 = False				   
        reward2 = True			   
        for i in reward_3 :
            if not st.getQuestItemsCount(i):
               reward2 = False				   			   
        reward3 = True			   
        for i in reward_4 :
            if not st.getQuestItemsCount(i):
               reward3 = False				   			   
        reward4 = True			   
        for i in reward_5 :
            if not st.getQuestItemsCount(i):
               reward4 = False				   			   
        if level < required_level :
           suffix = "-"+low_i+".htm"
           if not item :
              suffix = "-"+low_ni+".htm"
        else :
           if not item :
              suffix = "-"+ok_ni+".htm"
           else :
              suffix = "-"+ok_i+".htm"
              change(st,player,newclass,req_item,req_item1,req_item2,req_item3,req_item4,reward_1,reward_2,reward_3,reward_4,reward_5)			  
              st.giveItems(SHADOW_WEAPON_COUPON_CGRADE,15)
     st.exitQuest(1)
     htmltext = str(npcId)+suffix
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   race = player.getRace().ordinal()
   classId = player.getClassId()
   id = classId.getId()
   htmltext = default
   if st.getPlayer().isAio() :
      st.exitQuest(1)
      return "50032-23.htm"
   if npcId in NPCS :
     htmltext = "50032"
     if race in [0,1,2,3,4] :    # All Races
       if id == 1 :       # Human Warrior
         return htmltext+"-01.htm"
       if id == 4 :       # Human Knight
         return htmltext+"-02.htm"		 
       if id == 7 :       # Human Rogue
         return htmltext+"-03.htm"		 
       if id == 11 :       # Human Wizard
         return htmltext+"-04.htm"		 
       if id == 15 :      # Human Cleric
         return htmltext+"-05.htm"
       if id == 19 :      # Elven knight
         return htmltext+"-06.htm"		 
       if id == 22 :      # Elven Scout
         return htmltext+"-07.htm"	 
       if id == 26 :       # Elven Wizard
         return htmltext+"-08.htm"
       if id == 29 :     # Elven Oracle
         return htmltext+"-09.htm"
       if id == 32 :     # Palus Knight
         return htmltext+"-10.htm"
       if id == 35 :     # Assassin
         return htmltext+"-11.htm"		 
       if id == 39 :     # Dark Wizard
         return htmltext+"-12.htm"
       if id == 42 :     # Shillien Oracle
         return htmltext+"-13.htm"
       if id == 45 :     # Orc Raider
         return htmltext+"-14.htm"
       if id == 47 :     # Orc Monk
         return htmltext+"-15.htm"
       if id == 50 :     # Orc Shaman
         return htmltext+"-16.htm"		 		 		 		 
       if id == 56 :     # Scavenger
         return htmltext+"-17.htm"		
       if id == 54 :     # Artisan
         return htmltext+"-18.htm"				 
       elif classId.level() == 0 :            # first occupation change not made yet
         htmltext += "-76.htm"
       elif classId.level() >= 2 :            # second/third occupation change already made
         htmltext += "-77.htm"
       else :
         htmltext += "-78.htm"                # other conditions
     else :
       htmltext += "-78.htm"                  # other races
   st.exitQuest(1)
   return htmltext

QUEST   = Quest(7002,qn,"custom")

for npc in NPCS:
    QUEST.addStartNpc(npc)
    QUEST.addTalkId(npc)
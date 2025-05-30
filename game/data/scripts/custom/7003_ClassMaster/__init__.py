# Created by DrLecter, based on DraX' scripts
# This script is part of the L2J Datapack Project
# Visit us at http://www.l2jdp.com/
# See readme-dp.txt and gpl.txt for license and distribution details
# Let us know if you did not receive a copy of such files.
import sys
from com.dream.game.model.quest        import State
from com.dream.game.model.quest        import QuestState
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "7003_ClassMaster"
#Items
ADENA = 57
MEDAL = 6392
GMEDAL = 6393
BOOK_GIANT = 6622
GOLDBAR = 3470

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
NPCS=[50032]
#event:[newclass,req_class,req_race,required_level,low_ni,low_i,ok_ni,ok_i,req_item,req_item1,req_item2,req_item3,req_item4,reward_1,reward_2,reward_3,reward_4,reward_5]
#low_ni : level too low, and you dont have quest item
#low_i: level too low, despite you have the item
#ok_ni: level ok, but you don't have quest item
#ok_i: level ok, you got quest item, class change takes place
CLASSES = {
    "DL":[88,2,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Duelist
    "DD":[89,3,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Dreadnought
    "PH":[90,5,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Phoenix Knight
    "HK":[91,6,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Hell Knight
    "AD":[93,8,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Adventurer
    "SG":[92,9,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Sagittarius
    "AR":[94,12,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Archmage
    "SO":[95,13,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Soultaker
    "AL":[96,14,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Arcana Lord
    "CA":[97,16,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Cardinal
    "PP":[98,17,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Hierophant
    "ET":[99,20,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Evas Templar
    "SM":[100,21,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Sword Muse
    "WR":[101,23,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Wind Rider
    "MS":[102,24,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Moonlight Sentinel
    "MM":[103,27,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Mystic Muse
    "EM":[104,28,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Elemental Master
    "ES":[105,30,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Evas Saints
    "ST":[106,33,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Shillien Templar
    "SD":[107,34,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Spectral Dancer
    "GH":[108,36,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Ghost Hunter
    "GS":[109,37,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Ghost Sentinel
    "SS":[110,40,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Storm Screamer
    "SP":[111,41,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Spectral Master
    "SH":[112,43,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Shillien Saint
    "TT":[113,46,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Titan
    "GK":[114,48,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Grand Khavatari
    "DO":[115,51,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Dominator
    "DM":[116,52,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Doomcryer
    "FS":[117,55,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Fortune Seeker
    "MA":[118,57,76,"32","33","34","35",[],[],[],[],[],[],[],[],[],[]], #Maestro
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

def change1(st,player,newclass,items,items1,items2,items3,items4,rewards,rewards1,rewards2,rewards3,rewards4) :
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
     newclass,req_class,required_level,low_ni,low_i,ok_ni,ok_i,req_item,req_item1,req_item2,req_item3,req_item4,reward_1,reward_2,reward_3,reward_4,reward_5 = CLASSES[event]
     if classid == req_class :
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
              if player.isSubClassActive() :
                 change1(st,player,newclass,req_item,req_item1,req_item2,req_item3,req_item4,reward_1,reward_2,reward_3,reward_4,reward_5)			  
                 st.giveItems(BOOK_GIANT,1)
              else :
                 change(st,player,newclass,req_item,req_item1,req_item2,req_item3,req_item4,reward_1,reward_2,reward_3,reward_4,reward_5)			  
                 st.giveItems(BOOK_GIANT,1)	 
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
      return "50032-36.htm"
   if npcId in NPCS :
     htmltext = "50032"
     if race in [0,1,2,3,4] :    # All Races
       if id == 2 :       # Duelist
         return htmltext+"-01.htm"
       if id == 3 :       # Dreadnought
         return htmltext+"-02.htm"		 
       if id == 5 :       # Phoenix Knight
         return htmltext+"-03.htm"		 
       if id == 6 :       # Hell Knight
         return htmltext+"-04.htm"		 
       if id == 8 :      # Adventurer
         return htmltext+"-05.htm"
       if id == 9 :      # Sagittarius
         return htmltext+"-06.htm"		 
       if id == 12 :      # Archmage
         return htmltext+"-07.htm"	 
       if id == 13 :       # Soultaker
         return htmltext+"-08.htm"
       if id == 14 :     # Arcana Lord
         return htmltext+"-09.htm"
       if id == 16 :     # Cardinal
         return htmltext+"-10.htm"
       if id == 17 :     # Hierophant
         return htmltext+"-11.htm"		 
       if id == 20 :     # Evas Templar
         return htmltext+"-12.htm"
       if id == 21 :     # Sword Muse
         return htmltext+"-13.htm"
       if id == 23 :     # Wind Rider
         return htmltext+"-14.htm"
       if id == 24 :     # Moonlight Sentinel
         return htmltext+"-15.htm"
       if id == 27 :     # Mystic Muse
         return htmltext+"-16.htm"		 		 		 		 
       if id == 28 :     # Elemental Master
         return htmltext+"-17.htm"		
       if id == 30 :     # Evas Saints
         return htmltext+"-18.htm"				 
       if id == 33 :     # Shillien Templar
         return htmltext+"-19.htm"	
       if id == 34 :     # Spectral Dancer
         return htmltext+"-20.htm"	
       if id == 36 :     # Ghost Hunter
         return htmltext+"-21.htm"	
       if id == 37 :     # Ghost Sentinel
         return htmltext+"-22.htm"	
       if id == 40 :     # Storm Screamer
         return htmltext+"-23.htm"	
       if id == 41 :     # Spectral Master
         return htmltext+"-24.htm"	
       if id == 43 :     # Shillien Saint
         return htmltext+"-25.htm"	
       if id == 46 :     # Titan
         return htmltext+"-26.htm"	
       if id == 48 :     # Grand Khavatari
         return htmltext+"-27.htm"	
       if id == 51 :     # Dominator
         return htmltext+"-28.htm"	
       if id == 52 :     # Doomcryer
         return htmltext+"-29.htm"	
       if id == 55 :     # Fortune Seeker
         return htmltext+"-30.htm"	
       if id == 57 :     # Maestro
         return htmltext+"-31.htm"	
       elif classId.level() == 0 :            # first occupation change not made yet
         htmltext += "-76.htm"
       elif classId.level() >= 2 :            # second/third occupation change already made
         htmltext += "-77.htm"
       else :
         htmltext += "-78.htm"                # other conditions
     else :
       htmltext += "-76.htm"                  # other races
   st.exitQuest(1)
   return htmltext

QUEST   = Quest(7003,qn,"custom")

for npc in NPCS:
    QUEST.addStartNpc(npc)
    QUEST.addTalkId(npc)
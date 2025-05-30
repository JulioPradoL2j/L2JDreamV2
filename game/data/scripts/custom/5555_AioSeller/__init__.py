import sys
from com.dream.game.model.actor.instance import L2PcInstance
from com.dream.game.model.actor import L2Npc
from java.util import Iterator
from com.dream import L2DatabaseFactory
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream import Config
from com.dream.game.datatables.xml import SkillTable
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_AioSeller"

NPC=[50033]

#Quantidade de dias e item para a opcao 1.
DIAS_AIO1= 30
Moeda1= 9215
ItemCount1=30
#Quantidade de dias e item para a opcao 2.
DIAS_AIO2= 60
Moeda2= 9215
ItemCount2=55
#Quantidade de dias e item para a opcao 3.
DIAS_AIO3= 90
Moeda3= 9215
ItemCount3=70

ClassIds=0,10,18,25,31,38,44,49,53
Race=0,1,2,3,4
MinLevel=1
MaxLevel=1

#Id Reward Item AIO
AIO_DUAL_ID = 9999

QuestId     = 5555
QuestName   = "AioSeller"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Aio Seller Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    race = player.getRace().ordinal()
    classId = player.getClassId()
    id = classId.getId()	
    Lvl = st.getPlayer().getLevel()	
	
    if not st: return

    def doAio(player,days) :
     st.getPlayer().setAio(True)
     st.player.setEndTime("aio", days);
     st.giveItems(AIO_DUAL_ID,1)	
     st.player.getAppearance().setNameColor(Config.AIO_NCOLOR)
     st.player.getAppearance().setTitleColor(Config.AIO_TCOLOR)
     st.player.rewardAioSkills()
     st.player.sendSkillList()
     delexp = (st.player.getStat().getExp() - st.player.getStat() .getExpForLevel(81))
     st.player.getStat().addExp(-delexp)
	 
    if event == "1" :
      if not st.getPlayer().isAio():	
       if (player.getLevel() >= MinLevel) and (player.getLevel() <= MaxLevel) :	  
        if race in Race and id in ClassIds :
         if st.getQuestItemsCount(Moeda1) >= ItemCount1 :
            st.takeItems(Moeda1,ItemCount1)			  
            doAio(st.player, DIAS_AIO1)				  
            st.player.sendMessage("Congratulations you become a Aio buffer!")			  
            return "Thanks.htm"			  
         else:
          return "NoItem.htm"
        else:
         return "NoClass.htm"
       else:
         return "NoLvl.htm"
      else:
         return "Aio.htm"	
		 
    if event == "2" :
      if not st.getPlayer().isAio():
       if (player.getLevel() >= MinLevel) and (player.getLevel() <= MaxLevel) :	  
        if race in Race and id in ClassIds :
         if st.getQuestItemsCount(Moeda2) >= ItemCount2 :
            st.takeItems(Moeda2,ItemCount2)			  
            doAio(st.player, DIAS_AIO2)				  
            st.player.sendMessage("Congratulations you become a Aio buffer!")			  
            return "Thanks.htm"			  
         else:
          return "NoItem.htm"
        else:
         return "NoClass.htm"
       else:
         return "NoLvl.htm"
      else:
         return "Aio.htm"	
		 
    if event == "3" :
      if not st.getPlayer().isAio():	
       if (player.getLevel() >= MinLevel) and (player.getLevel() <= MaxLevel) :	  
        if race in Race and id in ClassIds :
         if st.getQuestItemsCount(Moeda3) >= ItemCount3 :
            st.takeItems(Moeda3,ItemCount3)			  
            doAio(st.player, DIAS_AIO3)				  
            st.player.sendMessage("Congratulations you become a Aio buffer!")			  
            return "Thanks.htm"			  
         else:
          return "NoItem.htm"
        else:
         return "NoClass.htm"
       else:
         return "NoLvl.htm"
      else:
         return "Aio.htm"	
		 
    if event == "4" :
      if st.getPlayer().isAio():	
       if st.getPlayer().getLevel() >= 1:	 	  			  		 							  		 							  			  		 					  			  		 				
                 st.getPlayer().setAio(False)			  			  		 							  		 							  			  		 					  			  		 				
                 st.takeItems(Config.AIO_DUAL_ID,1)			  			  		 							  		 							  			  		 					  			  		 				
                 st.player.setEndTime("aio", 0);			  			  		 							  		 							  			  		 					  			  		 				
                 st.player.lostAioSkills();			  			  		 							  		 							  			  		 					  			  		 				
                 st.player.sendSkillList()			  			  		 							  		 							  			  		 					  			  		 				
                 st.player.getAppearance().setNameColor(0xffffff)			  			  		 							  		 							  			  		 					  			  		 				
                 st.player.getAppearance().setTitleColor(0xffffff)			  			  		 							  		 							  			  		 					  			  		 				  			  		 							  		 							  			  		 					  			  		 					  
                 st.player.sendMessage("You Lost Aio Status!")	
                 delexp = (st.player.getStat().getExp() - st.player.getStat().getExpForLevel(1))	
                 st.player.getStat().addExp(-delexp)				 
                 return "Thanks.htm"			  
       else:
         return "NoLvl.htm"
      else:
         return "NoAio.htm"			 
		 
    return htmltext		
	
 def onTalk (self,npc,player):
		htmltext = "<html><head><body>You can't buy while you are</body></html>"
		st = player.getQuestState(qn)
		if not st : return htmltext
		npcId = npc.getNpcId()		
		if st.player.getPvpFlag() > 0 :#Player Flag
			htmltext = "Flag.htm"
			st.exitQuest(1)
		elif st.player.getKarma() > 0 :#Player com Karma
			st.exitQuest(1)
			htmltext = "Karma.htm"
		elif st.player.isInCombat() :#Player modo de Combate
			st.exitQuest(1)
			htmltext = "Combat.htm"
		else:
			
			return InitialHtml
		return htmltext

QUEST       = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 QUEST.addTalkId(npcId)	
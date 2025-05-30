import sys
from com.dream.game.model.actor.instance import L2PcInstance
from com.dream.game.model.actor import L2Npc
from java.util import Iterator
from com.dream import L2DatabaseFactory
from com.dream.game.model.quest import State
from com.dream.game.model.quest import QuestState
from com.dream import Config
from com.dream.game.model.quest.jython import QuestJython as JQuest
from com.dream.game.datatables.xml import SkillTable
from com.dream.game.model.quest.jython import QuestJython as JQuest

qn = "5555_VipSeller"

NPC=[50033]

#Moeda, quantidade de dias e item para a opcao 1.
DIAS_VIP1= 30
Moeda1= 9215
ItemCount1= 30
#Moeda, quantidade de dias e item para a opcao 2.
DIAS_VIP2= 60
Moeda2= 9215
ItemCount2= 55
#Moeda, quantidade de dias e item para a opcao 3.
DIAS_VIP3= 90
Moeda3= 9215
ItemCount3= 70


QuestId     = 5555
QuestName   = "VipSeller"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "Vip Seller Enabled..."

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onAdvEvent (self,event,npc,player) :
    htmltext = event
    st = player.getQuestState(qn)
    if not st: return

    def doVip(player,days) :
     st.getPlayer().setVip(True)
     st.player.setEndTime("vip", days);
     #st.player.getAppearance().setNameColor(Config.VIP_NCOLOR)
     #st.player.getAppearance().setTitleColor(Config.VIP_TCOLOR)

    if event == "1" :
      if not st.getPlayer().isVip():	
       if st.getPlayer().getLevel() >= 1:	 
        if st.getQuestItemsCount(Moeda1) >= ItemCount1 :
                  htmltext=event			 
                  st.takeItems(Moeda1,ItemCount1)			  
                  doVip(st.player, DIAS_VIP1)				  
                  st.player.sendMessage("Congratulations you become a Player Vip!")			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "noLvl.htm"
      else:
         return "Vip.htm"			 
		 
    if event == "2" :
      if not st.getPlayer().isVip():	
       if st.getPlayer().getLevel() >= 1:	 
        if st.getQuestItemsCount(Moeda2) >= ItemCount2 :
                  htmltext=event			 
                  st.takeItems(Moeda2,ItemCount2)			  
                  doVip(st.player, DIAS_VIP2)				  
                  st.player.sendMessage("Congratulations you become a Player Vip!")			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "noLvl.htm"
      else:
         return "Vip.htm"			 

    if event == "3" :
      if not st.getPlayer().isVip():	
       if st.getPlayer().getLevel() >= 1:	 
        if st.getQuestItemsCount(Moeda3) >= ItemCount3 :
                  htmltext=event			 
                  st.takeItems(Moeda3,ItemCount3)			  
                  doVip(st.player, DIAS_VIP3)				  
                  st.player.sendMessage("Congratulations you become a Player Vip!")			  
                  return "Thanks.htm"			  
        else:
         return "NoItem.htm"
       else:
         return "noLvl.htm"
      else:
         return "Vip.htm"			 
		 
    if event == "4" :
      if st.getPlayer().isVip():	
       if st.getPlayer().getLevel() >= 1:	 
                 htmltext=event			 
                 st.getPlayer().setVip(False)			  			  		 							  		 							  			  		 					  			  		 				
                 st.player.setEndTime("vip", 0);			  			  		 							  		 							  			  		 					  			  		 				
                 #st.player.getAppearance().setNameColor(0xffffff)			  			  		 							  		 							  			  		 					  			  		 				
                 #st.player.getAppearance().setTitleColor(0xffff77)			  			  		 							  		 							  			  		 					  			  		 				
                 st.player.sendMessage("You Lost Vip Status!")		  		 					  			  		 					    			  		 							  		 							  			  		 					  			  		 					  
                 return "Thanks.htm"			  
       else:
         return "noLvl.htm"
      else:
         return "NoVip.htm"			 
		 
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

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 QUEST.addTalkId(npcId) 
import glob
import os
import sys
import time

java_bin = "/home/pgarcia/jdk1.8.0_45/bin/java -jar metastone.jar"
decks = ["Midrange Druid Season 18",
"Midrange Hunter Season 18",
"Aggro Paladin Season 18",
"Shadow Madness Priest Season 18",
"Oil Rogue Season 18",
"Mech Shaman Season 18",
"Warlock MalyLock Season 18",
"Control Warrior Season 18"]

ais = ["FlatMonteCarlo",
"GreedyOptimizeTurn",
"GreedyOptimizeMove",
"PlayRandomBehaviour"
]

numberOfGames = 10

for i in range(0,len(decks)):
	for j in range(i,len(decks)):
		for k in range(0,len(ais)):
			for l in range(k,len(ais)):
				deck1 = decks[i]
				deck2 = decks[j]
				AI1 = ais[k]
				AI2 = ais[l]
				params = "\""+deck1+"\" \""+deck2+"\" "+AI1+" "+AI2
				filename = params.replace(" ", "_").replace("\"","")+".txt"
				launchline = "("+java_bin+" "+params+" "+str(numberOfGames) +")>"+filename
				os.system(launchline)
				print launchline
package es.ugr.gpstone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.FlatMonteCarlo;
import net.demilich.metastone.game.behaviour.GreedyOptimizeMove;
import net.demilich.metastone.game.behaviour.GreedyOptimizeTurn;
import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardProxy;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.statistics.GameStatistics;
import net.demilich.metastone.gui.deckbuilder.DeckProxy;
import net.demilich.metastone.gui.gameconfig.GameConfig;
import net.demilich.metastone.gui.gameconfig.PlayerConfig;
import net.demilich.metastone.gui.simulationmode.SimulationResult;



public class CommandLineLauncher {
	private  int gamesCompleted;
	private  long lastUpdate;

	private  SimulationResult result;
	
	private class PlayGameTask implements Callable<Void> {

		private final GameConfig gameConfig;

		public PlayGameTask(GameConfig gameConfig) {
			this.gameConfig = gameConfig;
		}

		@Override
		public Void call() throws Exception {
			//System.out.println("Launching new game");
			PlayerConfig playerConfig1 = gameConfig.getPlayerConfig1();
			PlayerConfig playerConfig2 = gameConfig.getPlayerConfig2();

			Player player1 = new Player(playerConfig1);
			Player player2 = new Player(playerConfig2);

			GameContext newGame = new GameContext(player1, player2, new GameLogic());
			newGame.play();
			

			onGameComplete(gameConfig, newGame);
			newGame.dispose();

			return null;
		}

	}
	
	private void onGameComplete(GameConfig gameConfig, GameContext context) {
		long timeStamp = System.currentTimeMillis();
		gamesCompleted++;
		synchronized (result) {
			result.getPlayer1Stats().merge(context.getPlayer1().getStatistics());
			result.getPlayer2Stats().merge(context.getPlayer2().getStatistics());
		}
		System.out.print(gamesCompleted+" ");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try{
			//MassTest mt = new MassTest();
			new CardProxy();
			//mt.testRandomMassPlay();	
			CommandLineLauncher cll = new CommandLineLauncher();
			long time = System.currentTimeMillis();
			cll.launch(1, "Legendary GvG Ramp Druid", "Legendary GvG Ramp Druid","FlatMonteCarlo","PlayRandomBehaviour");

		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
		
	public void launch(int numGames, String player1deck, String player2deck, String player1AI, String player2AI) throws Exception{
		GameConfig gameConfig = generateGameConfig(numGames, player1deck, player2deck,player1AI,player2AI);
		result = new SimulationResult(gameConfig);

		gamesCompleted = 0;

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				int cores = Runtime.getRuntime().availableProcessors();
				System.out.println("Starting simulation on " + cores + " cores");
				ExecutorService executor = Executors.newFixedThreadPool(cores);
				// ExecutorService executor =
				// Executors.newSingleThreadExecutor();

				List<Future<Void>> futures = new ArrayList<Future<Void>>();
				// send initial status update
				// queue up all games as tasks
				lastUpdate = System.currentTimeMillis();
				for (int i = 0; i < gameConfig.getNumberOfGames(); i++) {
					
					PlayGameTask task = new PlayGameTask(gameConfig);
					Future<Void> future = executor.submit(task);
					futures.add(future);
					
				}

				executor.shutdown();
				boolean completed = false;
				while (!completed) {
					completed = true;
					for (Future<Void> future : futures) {
						if (!future.isDone()) {
							completed = false;
							continue;
						}
						try {
							future.get();
						} catch (InterruptedException | ExecutionException e) {
							System.out.println("EXCEPTION" +e.getMessage());
							e.printStackTrace();
							System.exit(-1);
						}
					}
					futures.removeIf(future -> future.isDone());
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				result.calculateMetaStatistics();
				long endtime = System.currentTimeMillis();
				System.out.println("Simulation finished in "+(endtime - lastUpdate)+" millis.");
				System.out.println("PLAYER "+gameConfig.getPlayerConfig1().getName());
				System.out.println(result.getPlayer1Stats());
				System.out.println("PLAYER "+gameConfig.getPlayerConfig2().getName());
				System.out.println(result.getPlayer2Stats());
			}
		});
		//t.setDaemon(true);
		t.start();

		
		
	

	}
	
	private static PlayerConfig generatePlayerConfig(String deckName, String behaviour) throws Exception{
		DeckProxy dProxy = new DeckProxy();
		dProxy.loadDecks();
		List<Deck> ds = dProxy.getDecks();
		/*for(Deck theD:ds){
			System.out.println(theD.getName()+"-----"+theD.getFilename());
		}*/
		Deck d = dProxy.getDeckByName(deckName);//DeckFactory.getRandomDeck(HeroClass.DRUID);

		if(d == null)
			throw new Exception("Deck with name "+deckName+" does not exist;");

		HeroClass heroClass = d.getHeroClass();
		
		IBehaviour bev = getBehaviour(behaviour);
		if(bev == null)
			throw new Exception("Behaviour "+behaviour+" does not exist");
		PlayerConfig pc = new PlayerConfig(d, bev);
		pc.setHeroCard(getHeroCardForClass(heroClass));
		return pc;
	}
	
	private static IBehaviour getBehaviour(String behaviour){
		IBehaviour bev = null;
		if(behaviour.compareTo("FlatMonteCarlo")== 0){
			bev = new FlatMonteCarlo(1000);//= new PlayRandomBehaviour();
			System.out.println("FlatMonteCarlo");
		}else if(behaviour.compareTo("GreedyOptimizeTurn")==0){
			IGameStateHeuristic heuristic = new WeightedHeuristic();
			bev = new GreedyOptimizeTurn(heuristic);
			System.out.println("GreedyOptimizeTurn");
		}else if(behaviour.compareTo("GreedyOptimizeMove")==0){
			IGameStateHeuristic heuristic = new WeightedHeuristic();
			bev = new GreedyOptimizeMove(heuristic);
			System.out.println("GreedyOptimizeMove");
		}else if(behaviour.compareTo("PlayRandomBehaviour")==0){
			
			bev = new PlayRandomBehaviour();
			System.out.println("PlayRandomBehaviour");
	
		}
		
		return bev;
	}
	
	private static GameConfig generateGameConfig(int numGames, String player1deck, String player2deck, String player1AI, String player2AI) throws Exception{
		PlayerConfig playerConfig1 = generatePlayerConfig(player1deck,player1AI);
		playerConfig1.setName("P1_"+player1deck+"_"+player1AI);
		

		PlayerConfig playerConfig2 = generatePlayerConfig(player2deck,player2AI);
		playerConfig2.setName("P2_"+player2deck+"_"+player2AI);		
		
		//playerConfig1.build();
		//playerConfig2.build();
		
		Player player1 = new Player(playerConfig1);
		Player player2 = new Player(playerConfig2);
		
		GameConfig gameConfig = new GameConfig();
		gameConfig.setNumberOfGames(numGames);
		gameConfig.setPlayerConfig1(playerConfig1);
		gameConfig.setPlayerConfig2(playerConfig2);
		
		return gameConfig;
	}
	
	private static void test() throws Exception{
		PlayerConfig playerConfig1 = generatePlayerConfig("Legendary GvG Ramp Druid","montecarlo");
		playerConfig1.setName("Player 1");
		

		PlayerConfig playerConfig2 = generatePlayerConfig("Legendary GvG Ramp Druid","other");
		playerConfig2.setName("Player 2");			
		
		//playerConfig1.build();
		//playerConfig2.build();
		
		Player player1 = new Player(playerConfig1);
		Player player2 = new Player(playerConfig2);
		
		GameConfig gameConfig = new GameConfig();
		gameConfig.setNumberOfGames(100);
		gameConfig.setPlayerConfig1(playerConfig1);
		gameConfig.setPlayerConfig2(playerConfig2);

		GameContext newGame = new GameContext(player1, player2, new GameLogic());
		//newGame.init();
		newGame.play();

		System.out.println("WINNER IS "+newGame.getWinningPlayerId());
		SimulationResult sresult = new SimulationResult(gameConfig);
		sresult.calculateMetaStatistics();
		GameStatistics p1stats = sresult.getPlayer1Stats();
		GameStatistics p2stats = sresult.getPlayer2Stats();

		
		System.out.println("ENDING");
		System.out.println(p1stats.toString());
		System.out.println(p2stats.toString());
		
		System.out.println(newGame.getPlayer1().getStatistics());//of the last game. Use the MERGE from SimulationResult after
		newGame.dispose();
		
	}
	
	protected static HeroCard getHeroCardForClass(HeroClass heroClass) {
		for (Card card : CardCatalogue.getHeroes()) {
			HeroCard heroCard = (HeroCard) card;
			if (heroCard.getHeroClass() == heroClass) {
				return heroCard;
			}
		}
		return null;
	}
	
	

}

package org.openRealmOfStars.game;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.openRealmOfStars.game.States.AITurnView;
import org.openRealmOfStars.game.States.BattleView;
import org.openRealmOfStars.game.States.CreditsView;
import org.openRealmOfStars.game.States.FleetView;
import org.openRealmOfStars.game.States.GalaxyCreationView;
import org.openRealmOfStars.game.States.LoadGameView;
import org.openRealmOfStars.game.States.MainMenu;
import org.openRealmOfStars.game.States.PlanetBombingView;
import org.openRealmOfStars.game.States.PlanetView;
import org.openRealmOfStars.game.States.PlayerSetupView;
import org.openRealmOfStars.game.States.ResearchView;
import org.openRealmOfStars.game.States.ShipDesignView;
import org.openRealmOfStars.game.States.ShipView;
import org.openRealmOfStars.game.States.StarMapView;
import org.openRealmOfStars.gui.scrollPanel.SpaceScrollBarUI;
import org.openRealmOfStars.player.PlayerInfo;
import org.openRealmOfStars.player.PlayerList;
import org.openRealmOfStars.player.SpaceRace;
import org.openRealmOfStars.player.combat.Combat;
import org.openRealmOfStars.player.fleet.Fleet;
import org.openRealmOfStars.player.message.Message;
import org.openRealmOfStars.player.message.MessageType;
import org.openRealmOfStars.player.ship.Ship;
import org.openRealmOfStars.player.ship.ShipDesign;
import org.openRealmOfStars.player.ship.ShipStat;
import org.openRealmOfStars.starMap.GalaxyConfig;
import org.openRealmOfStars.starMap.StarMap;
import org.openRealmOfStars.starMap.planet.Planet;

/**
 * 
 * Open Realm of Stars game project
 * Copyright (C) 2016  Tuomo Untinen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Contains runnable main method which runs the Game class.
 * 
 */

public class Game extends JFrame implements ActionListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Game Title show in various places
   */
  public static final String GAME_TITLE = "Open Realm of Stars";

  /**
   * Game version number
   */
  public static final String GAME_VERSION = "0.0.5Alpha";

  /**
   * Animation timer used for animation
   */
  private Timer animationTimer;

  
  /**
   * Star map for the game
   */
  private StarMap starMap = null;
  
  /**
   * List of players
   */
  public PlayerList players;
  
  
  
  /**
   * Current Game state
   */
  public GameState gameState;
  
  
  /**
   * Planet view Panel and handling planet
   */
  public PlanetView planetView;

  /**
   * Planet bombing view Panel
   */
  public PlanetBombingView planetBombingView;

  /**
   * Fleet view Panel and handling the fleet
   */
  public FleetView fleetView;

  /**
   * Main menu for the game
   */
  public MainMenu mainMenu;

  /**
   * Galaxy Creation view
   */
  public GalaxyCreationView galaxyCreationView;

  /**
   * Player Setup view
   */
  public PlayerSetupView playerSetupView;
  
  /**
   * Load Game View
   */
  public LoadGameView loadGameView;

  /**
   * AI Turn view
   */
  public AITurnView aiTurnView;

  /**
   * Credits for the game
   */
  public CreditsView creditsView;

  /**
   * StarMap view for the game
   */
  public StarMapView starMapView;

  /**
   * Combat view for the game
   */
  public BattleView combatView;

  /**
   * Research view for the game
   */
  public ResearchView researchView;

  /**
   * Ship view for the game
   */
  public ShipView shipView;

  /**
   * Ship design view for the game
   */
  public ShipDesignView shipDesignView;
  
  /**
   * Galaxy config for new game
   */
  public GalaxyConfig galaxyConfig;
  
  /**
   * Get Star map
   * @return StarMap
   */
  public StarMap getStarMap() {
    return starMap;
  }

  /**
   * Contructor of Game class
   */
  public Game() {
    // Set look and feel match on CrossPlatform Look and feel
    try {
      UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
    } catch (Exception e) {
              e.printStackTrace();
    }
    UIManager.put("ScrollBarUI", SpaceScrollBarUI.class.getName());
    setTitle(GAME_TITLE+" "+GAME_VERSION);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);         
    addWindowListener(new GameWindowListener());
    setSize(1024, 768);
    setLocationRelativeTo(null)    ;
    animationTimer = new Timer(75,this);
    animationTimer.setActionCommand(GameCommands.COMMAND_ANIMATION_TIMER);
    animationTimer.start();

    changeGameState(GameState.MAIN_MENU);

    // Add new KeyEventDispatcher
    KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    kfm.addKeyEventDispatcher(new GameKeyAdapter(this));
    setResizable(false);

    this.setVisible(true);
    

  }
  
  /**
   * Cause Fleet to make a move
   * @param info Player who owns the fleet
   * @param fleet Fleet to move
   * @param nx New coordinate x axel
   * @param ny new coordinate y axel
   */
  public void fleetMakeMove(PlayerInfo info, Fleet fleet, int nx, int ny) {
    if (getStarMap().isValidCoordinate(nx, ny) && fleet.movesLeft > 0 
        && !getStarMap().isBlocked(nx, ny)) {
      Combat combat = getStarMap().fightWithFleet(nx, ny, fleet,info);          
      if (combat != null) {
        fleet.movesLeft--;
        starMapView.readyToMove = false;
        changeGameState(GameState.COMBAT, combat);
      } else {
        fleet.setPos(nx, ny);
        fleet.movesLeft--;
        getStarMap().doFleetScanUpdate(info,fleet,null);
        starMapView.updatePanels();
        if (info.isHuman()) {
          getStarMap().setDrawPos(fleet.getX(),fleet.getY());
        }
        starMapView.readyToMove = false;
      }
    }
  }

  
  /**
   * Show planet view panel
   * @param planet Planet to show
   */
  public void showPlanetView(Planet planet) {
    planetView = new PlanetView(planet, this);
    this.getContentPane().removeAll();
    this.add(planetView);
    this.validate();
  }
  /**
   * Show planet bombing view panel
   * @param planet Planet to show
   */
  public void showPlanetBombingView(Planet planet,Fleet fleet) {
    planetBombingView = new PlanetBombingView(planet, fleet,
        starMap.getCurrentPlayerInfo(), players.getCurrentPlayer(), this);
    this.getContentPane().removeAll();
    this.add(planetBombingView);
    this.validate();
  }

  /**
   * Show fleet view panel
   * @param planet Planet to show
   */
  public void showFleetView(Planet planet,Fleet fleet) {
    fleetView = new FleetView(planet,fleet,
        players.getCurrentPlayerInfo().Fleets(),players.getCurrentPlayerInfo(), this);
    this.getContentPane().removeAll();
    this.add(fleetView);
    this.validate();
  }

  /**
   * Show Star Map panels
   */
  public void showStarMap() {
    starMapView = new StarMapView(starMap, players, this);
    this.getContentPane().removeAll();
    this.add(starMapView);
    this.validate();
    starMapView.setAutoFocus(false);
    focusOnMessage(true);
  }

  /**
   * Show Combat
   */
  public void showCombat(Combat combat) {
    if (combat == null) {
      // This is fixed combat
      combatView = new BattleView(
          players.getCurrentPlayerInfo().Fleets().getByIndex(0), 
          players.getCurrentPlayerInfo(), 
          players.getPlayerInfoByIndex(1).Fleets().getByIndex(0), 
          players.getPlayerInfoByIndex(1),starMap, this);
    } else {
      combatView = new BattleView(combat, starMap, this);
    }
    this.getContentPane().removeAll();
    this.add(combatView);
    this.validate();
  }

  /**
   * Show Research panels
   * @param focusMsg which tech should have focus on show up. Can be null.
   */
  public void showResearch(Message focusMsg) {
    String focusTech = null;
    if (focusMsg != null && focusMsg.getType() == MessageType.RESEARCH &&
        focusMsg.getMatchByString() != null) {
      focusTech = focusMsg.getMatchByString();
    }
    researchView = new ResearchView(players.getCurrentPlayerInfo(),
        starMap.getTotalProductionByPlayerPerTurn(Planet.PRODUCTION_RESEARCH,
            players.getCurrentPlayer()),focusTech, this);
    this.getContentPane().removeAll();
    this.add(researchView);
    this.validate();
  }

  /**
   * Show Ship panels
   */
  public void showShipView() {
    shipView = new ShipView(players.getCurrentPlayerInfo(), this);
    this.getContentPane().removeAll();
    this.add(shipView);
    this.validate();
  }

  /**
   * Show Ship design panels
   * @param oldDesign to copy to new one. Can be null.
   */
  public void showShipDesignView(ShipDesign oldDesign) {
    shipDesignView = new ShipDesignView(players.getCurrentPlayerInfo(), oldDesign, this);
    this.getContentPane().removeAll();
    this.add(shipDesignView);
    this.validate();
  }

  /**
   * Show main menu panel
   */
  public void showMainMenu() {
    mainMenu = new MainMenu(this);
    this.getContentPane().removeAll();
    this.add(mainMenu);
    this.validate();
  }

  /**
   * Show Galaxy creation panel
   */
  public void showGalaxyCreation() {
    galaxyCreationView = new GalaxyCreationView(galaxyConfig,this);
    galaxyConfig = galaxyCreationView.getConfig();
    this.getContentPane().removeAll();
    this.add(galaxyCreationView);
    this.validate();
  }

  /**
   * Show Player setup panel
   */
  public void showPlayerSetup() {
    playerSetupView = new PlayerSetupView(galaxyConfig,this);
    this.getContentPane().removeAll();
    this.add(playerSetupView);
    this.validate();
  }

  /**
   * Show Load Gamve view panel
   */
  public void showLoadGame() {
    loadGameView = new LoadGameView(this);
    this.getContentPane().removeAll();
    this.add(loadGameView);
    this.validate();
  }

  /**
   * Save game for certain file name
   * @param filename File name
   */
  public void saveGame(String filename) {
    if (starMap != null) {
      String folderName = "saves";
      File folder = new File(folderName);
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(folderName+"/"+filename);
      try {
        FileOutputStream os = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        try(DataOutputStream dos = new DataOutputStream(bos)) {
          starMap.saveGame(dos);
        } catch (IOException e) {
          System.out.println(e.getMessage());
        }
      } catch (FileNotFoundException e) {
        System.err.println("File could not be write: "+folderName+"/"+filename+
                           "! "+e.getMessage());
      }

    }
  }

  /**
   * Load game from certain file name
   * @param filename File name
   * @return true if successful
   */
  public boolean loadGame(String filename) {
    String folderName = "saves";
    File file = new File(folderName+"/"+filename);
    try (FileInputStream is = new FileInputStream(file)){
      BufferedInputStream bis = new BufferedInputStream(is);
      DataInputStream dis = new DataInputStream(bis);
      starMap = new StarMap(dis);
      players = starMap.getPlayerList();
      starMap.updateStarMapOnLoadGame();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Show AI Turn view
   */
  public void showAITurnView() {
    aiTurnView = new AITurnView(this, this);
    this.getContentPane().removeAll();
    this.add(aiTurnView);
    this.validate();
  }

  /**
   * Show credits panel
   */
  public void showCredits() {
    try {
      creditsView = new CreditsView(this, GAME_TITLE, GAME_VERSION);
    } catch (IOException e) {
      System.out.println("Could not show credits: "+e.getMessage());
      System.exit(0);
    }
    this.getContentPane().removeAll();
    this.add(creditsView);
    this.validate();
  }

  /**
   * Change game state so that focus is also changed to target message
   * @param newState Game State where to change
   * @param focusMessage Focused message, can be also null
   */
  public void changeGameState(GameState newState, Message focusMessage) {
    changeGameState(newState, focusMessage, null);
  }

  /**
   * Change game state so that there is custom object given as a parameter
   * @param newState Game State where to change
   * @param dataObject Depends on which state is changed
   */
  public void changeGameState(GameState newState, Object dataObject) {
    changeGameState(newState, null, dataObject);
  }

  /**
   * Change game state so that focus is also changed to target message.
   * There is also possibility to give data object which depends on new gamestate.
   * @param newState Game State where to change
   * @param focusMessage Focused message, can be also null
   * @param dataObject Depends on which state is changed
   */
  private void changeGameState(GameState newState, Message focusMessage, Object dataObject) {
    gameState = newState;
    switch (gameState) {
    case AITURN: showAITurnView(); break;
    case MAIN_MENU: showMainMenu(); break;
    case GALAXY_CREATION: showGalaxyCreation(); break;
    case PLAYER_SETUP: showPlayerSetup(); break;
    case LOAD_GAME: showLoadGame(); break;
    case NEW_GAME: { 
      players = new PlayerList();
      for (int i=0;i<galaxyConfig.getMaxPlayers();i++) {
        PlayerInfo info = new PlayerInfo(galaxyConfig.getRace(i));
        info.setEmpireName(galaxyConfig.getPlayerName(i));
        if (i==0) {
          info.setHuman(true);
        }
        players.addPlayer(info);
      }
      starMap = new StarMap(galaxyConfig,players);
      starMap.updateStarMapOnStartGame();
      players.setCurrentPlayer(0);
      starMapView = null;
      combatView = null;
      researchView = null;
      shipView = null;
      shipDesignView = null;
      changeGameState(GameState.STARMAP);
      break;
    }
    case PLANETBOMBINGVIEW: {
      boolean changed = false; 
      if (dataObject instanceof Planet) {
        Planet planet = (Planet) dataObject;
        Fleet fleet = starMap.getFleetByCoordinate(planet.getX(), planet.getY());
        if (fleet != null) {
          showPlanetBombingView(planet,fleet);
          changed = true;
        }
      }
      if (!changed) {
        changeGameState(GameState.STARMAP);
      }
      break;
    }
    case CREDITS: showCredits(); break;
    case STARMAP: showStarMap(); break;
    case COMBAT: {
      if (dataObject instanceof Combat) {
       showCombat((Combat) dataObject); 
      } else {
        showCombat(null);
      }
      break;
    }
    case RESEARCHVIEW: showResearch(focusMessage); break;
    case VIEWSHIPS: showShipView(); break;
    case SHIPDESIGN: {
        if (shipView != null && shipView.isCopyClicked()) {
         showShipDesignView(shipView.getSelectedShip());
        } else {
          showShipDesignView(null);
        }
       break;
    }
    case PLANETVIEW: {
      if (focusMessage != null) {
        Planet planet = starMap.getPlanetByCoordinate(focusMessage.getX(), focusMessage.getY());
        if (planet != null) {
          starMap.setCursorPos(focusMessage.getX(), focusMessage.getY());
          starMap.setDrawPos(focusMessage.getX(), focusMessage.getY());
          showPlanetView(planet);
        }
      } else if (starMapView.getStarMapMouseListener().getLastClickedPlanet()!=null) {
       showPlanetView(starMapView.getStarMapMouseListener().getLastClickedPlanet());
      }
      break;
    }
    case FLEETVIEW: {
      if (starMapView.getStarMapMouseListener().getLastClickedFleet()!=null) {
        Fleet fleet = starMapView.getStarMapMouseListener().getLastClickedFleet();
        Planet planet = starMap.getPlanetByCoordinate(fleet.getX(), fleet.getY());
        showFleetView(planet,fleet);
      }
      break;
    }
    }    
  }
  
  /**
   * Change game state and show new panel/screen
   * @param newState Game State where to change
   */
  public void changeGameState(GameState newState) {
    changeGameState(newState,null);
  }
  
  /**
   * Main method to run the game
   * @param args from Command line
   */
  public static void main(String[] args) {
    new Game();

  }

  /**
   * Focus on active message
   * @param mapOnly focus only message which move map
   */
  public void focusOnMessage(boolean mapOnly) {
    Message msg = players.getCurrentPlayerInfo().getMsgList().getMsg();
    if (msg.getType() == MessageType.RESEARCH && !mapOnly) {
      changeGameState(GameState.RESEARCHVIEW, msg);
    }
    if (msg.getType() == MessageType.PLANETARY) {
      starMap.setCursorPos(msg.getX(), msg.getY());
      starMap.setDrawPos(msg.getX(), msg.getY());
      Planet planet = starMap.getPlanetByCoordinate(msg.getX(), msg.getY());
      if (planet != null) {
        starMapView.setShowPlanet(planet);
        starMapView.getStarMapMouseListener().setLastClickedPlanet(planet);
      }
    }
    if (msg.getType() == MessageType.FLEET) {
      starMap.setCursorPos(msg.getX(), msg.getY());
      starMap.setDrawPos(msg.getX(), msg.getY());
      Fleet fleet = players.getCurrentPlayerInfo().Fleets().
          getByName(msg.getMatchByString());
      if (fleet != null) {
        starMapView.setShowFleet(fleet);
        starMapView.getStarMapMouseListener().setLastClickedFleet(fleet);
        starMapView.getStarMapMouseListener().setLastClickedPlanet(null);
      }
    }
    if ((msg.getType() == MessageType.CONSTRUCTION ||
        msg.getType() == MessageType.POPULATION) && (!mapOnly)) {
      changeGameState(GameState.PLANETVIEW, msg);
    }
  }
  
  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (gameState == GameState.STARMAP && starMapView != null) {
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_END_TURN)) {
        saveGame("autosave.save");
        changeGameState(GameState.AITURN);
      }else if (arg0.getActionCommand().equals(GameCommands.COMMAND_FOCUS_MSG)) {
        focusOnMessage(false);
      } else if (arg0.getActionCommand().equals(GameCommands.COMMAND_PREV_TARGET)) {
        if (starMapView.getStarMapMouseListener().getLastClickedFleet() != null) {
          Fleet fleet = players.getCurrentPlayerInfo().Fleets().getPrev();
          if (fleet != null) {
            starMap.setCursorPos(fleet.getX(), fleet.getY());
            starMap.setDrawPos(fleet.getX(), fleet.getY());
            starMapView.setShowFleet(fleet);
            starMapView.getStarMapMouseListener().setLastClickedFleet(fleet);
            starMapView.getStarMapMouseListener().setLastClickedPlanet(null);
          }          
        }
      } else if (arg0.getActionCommand().equals(GameCommands.COMMAND_NEXT_TARGET)) {
        if (starMapView.getStarMapMouseListener().getLastClickedFleet() != null) {
          Fleet fleet = players.getCurrentPlayerInfo().Fleets().getNext();
          if (fleet != null) {
            starMap.setCursorPos(fleet.getX(), fleet.getY());
            starMap.setDrawPos(fleet.getX(), fleet.getY());
            starMapView.setShowFleet(fleet);
            starMapView.getStarMapMouseListener().setLastClickedFleet(fleet);
            starMapView.getStarMapMouseListener().setLastClickedPlanet(null);
          }          
        }
      } else {
        if (arg0.getActionCommand().equalsIgnoreCase(
            GameCommands.COMMAND_ANIMATION_TIMER) ) {
          // Handle double click state changes
          if (starMapView.getStarMapMouseListener().isDoubleClicked()) {
            if (starMapView.getStarMapMouseListener().getLastClickedPlanet() != null) {
              changeGameState(GameState.PLANETVIEW);
            } else if (starMapView.getStarMapMouseListener().getLastClickedFleet() != null) {
              changeGameState(GameState.FLEETVIEW);
            }
          }
          if (!starMapView.getStarMapMouseListener().isDoubleClicked() 
              && starMapView.getStarMapMouseListener().isMoveClicked()) {
            if (starMapView.getStarMapMouseListener().getLastClickedFleet() != null) {
              starMapView.getStarMapMouseListener().getLastClickedFleet().setRoute(null);
              starMapView.getStarMapMouseListener().setMoveClicked(false);
              fleetMakeMove(players.getCurrentPlayerInfo(), 
                  starMapView.getStarMapMouseListener().getLastClickedFleet(),
                  starMapView.getStarMapMouseListener().getMoveX(), 
                  starMapView.getStarMapMouseListener().getMoveY());
            }
          }
        }
        starMapView.handleActions(arg0);
        if (starMapView.isAutoFocus() && arg0.getActionCommand().equals(GameCommands.COMMAND_END_TURN)) {
          starMapView.setAutoFocus(false);
          focusOnMessage(true);
        }
      }
    }
    if (gameState == GameState.COMBAT && combatView != null) {
      if (combatView.isCombatEnded() &&
          arg0.getActionCommand().equals(GameCommands.COMMAND_END_BATTLE_ROUND)) {
        changeGameState(GameState.STARMAP);
      } else {
        combatView.handleActions(arg0);
      }
    }
    if (gameState == GameState.PLANETBOMBINGVIEW && planetBombingView != null) {
        planetBombingView.handleAction(arg0);
    }
    if (gameState == GameState.AITURN && aiTurnView != null) {
      aiTurnView.handleActions(arg0);
    }
    if (gameState == GameState.CREDITS) {
      if (arg0.getActionCommand().equalsIgnoreCase(
          GameCommands.COMMAND_ANIMATION_TIMER)) {
        creditsView.updateTextArea();
      }
      if (arg0.getActionCommand().equalsIgnoreCase(
          GameCommands.COMMAND_OK)) {
        creditsView = null;
        changeGameState(GameState.MAIN_MENU);
      }
      return;
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_VIEW_PLANET) &&
        starMapView.getStarMapMouseListener().getLastClickedPlanet() != null) {
      changeGameState(GameState.PLANETVIEW);
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_VIEW_FLEET) &&
        starMapView.getStarMapMouseListener().getLastClickedFleet() != null) {
      changeGameState(GameState.FLEETVIEW);
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_VIEW_STARMAP)) {
      if (gameState == GameState.PLANETVIEW) {
        planetView = null;
      }
      if (gameState == GameState.FLEETVIEW) {
        fleetView = null;
      }
      if (gameState == GameState.RESEARCHVIEW) {
        researchView = null;
      }
      changeGameState(GameState.STARMAP);
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_VIEW_RESEARCH)) {
      changeGameState(GameState.RESEARCHVIEW);
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_SHIPS)) {
      changeGameState(GameState.VIEWSHIPS);
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_SHIPDESIGN)) {
      shipView.setCopyClicked(false);
      changeGameState(GameState.SHIPDESIGN);
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_BATTLE)) {
      //changeGameState(GameState.COMBAT);
      changeGameState(GameState.PLANETBOMBINGVIEW, starMap.getPlanetList().get(0));
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_SHIPDESIGN_DONE)) {
      if (shipDesignView != null && shipDesignView.isDesignOK()) {
        shipDesignView.keepDesign();
        changeGameState(GameState.VIEWSHIPS);
      }
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_COPY_SHIP)) {
      shipView.setCopyClicked(true);
      changeGameState(GameState.SHIPDESIGN);
    }
    if (gameState == GameState.RESEARCHVIEW && researchView != null) {
      // Research View
      researchView.handleAction(arg0);
    }
    if (gameState == GameState.VIEWSHIPS && shipView != null) {
      // View Ship
      shipView.handleAction(arg0);
    }
    if (gameState == GameState.SHIPDESIGN && shipDesignView != null) {
      // Ship Design View
      shipDesignView.handleAction(arg0);
    }
    if (gameState == GameState.PLANETVIEW && planetView != null) {
      // Planet view
      planetView.handleAction(arg0);
    }
    if (gameState == GameState.FLEETVIEW && fleetView != null) {
      // Fleet view
      if (arg0.getActionCommand().equals(GameCommands.COMMAND_COLONIZE)) {
        Ship ship = fleetView.getFleet().getColonyShip();
        if (ship != null && fleetView.getPlanet() != null &&
            fleetView.getPlanet().getPlanetPlayerInfo() == null) {
          // Make sure that ship is really colony and there is planet to colonize
          fleetView.getPlanet().setPlanetOwner(players.getCurrentPlayer(), 
              players.getCurrentPlayerInfo());
          if (players.getCurrentPlayerInfo().getRace() == SpaceRace.MECHIONS) {
            fleetView.getPlanet().setWorkers(Planet.PRODUCTION_WORKERS, ship.getColonist());
          } else {
            fleetView.getPlanet().setWorkers(Planet.PRODUCTION_FOOD, ship.getColonist());
          }
          // Remove the ship and show the planet view you just colonized
          fleetView.getFleet().removeShip(ship);
          ShipStat stat = starMap.getCurrentPlayerInfo()
              .getShipStatByName(ship.getName());
          stat.setNumberOfInUse(stat.getNumberOfInUse()-1);
          fleetView.getFleetList().recalculateList();
          starMapView.getStarMapMouseListener().setLastClickedFleet(null);
          starMapView.getStarMapMouseListener().setLastClickedPlanet(fleetView.getPlanet());
          changeGameState(GameState.PLANETVIEW);
        }
      }
      if (arg0.getActionCommand().equals(GameCommands.COMMAND_CONQUEST)) {
        changeGameState(GameState.PLANETBOMBINGVIEW, fleetView.getPlanet());
      }
      fleetView.handleAction(arg0);
    }
    if (gameState == GameState.GALAXY_CREATION) {
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_CANCEL)) {
        changeGameState(GameState.MAIN_MENU);
      } else if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_NEXT)) {
        changeGameState(GameState.PLAYER_SETUP);
      } else {
        galaxyCreationView.handleActions(arg0);
      }
    } else if (gameState == GameState.PLAYER_SETUP) {
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_CANCEL)) {
        playerSetupView.getNamesToConfig();
        changeGameState(GameState.GALAXY_CREATION);
      } else if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_NEXT)) {
        playerSetupView.getNamesToConfig();
        changeGameState(GameState.NEW_GAME);
      } else {
        playerSetupView.handleActions(arg0);
      }
    }
    if (gameState == GameState.LOAD_GAME && loadGameView != null) {
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_CANCEL)) {
        changeGameState(GameState.MAIN_MENU);
      } else if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_NEXT)) {
        if (loadGameView.getSelectedSaveFile() != null) {
          if (loadGame(loadGameView.getSelectedSaveFile())) {
            changeGameState(GameState.STARMAP);
          }
        }
      }
      
    }
    if (gameState == GameState.MAIN_MENU) {
      // Main menu
      if (arg0.getActionCommand().equalsIgnoreCase(
          GameCommands.COMMAND_CONTINUE_GAME)) {
          changeGameState(GameState.LOAD_GAME);
      }
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_NEW_GAME)) {
        changeGameState(GameState.GALAXY_CREATION);
      }
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_CREDITS)) {
        changeGameState(GameState.CREDITS);
      }
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_QUIT_GAME)) {
        System.exit(0);
      }
    }
  }

}

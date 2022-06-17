package com.game.Sokoban;

import android.graphics.Rect;

public class GameGrid
{
  static public final int WEST            = 2;
  static public final int EAST            = 3;
  static public final int NORTH           = 0;
  static public final int SOUTH           = 1;

  static public final int WALL            = 0x00000001;
  static public final int GOAL            = 0x00000002;
  static public final int CRATE           = 0x00000200;
  static public final int FLOOR           = 0x00000000;
  static public final int SOKOBAN         = 0x00000100;
  static public final int OVER_MAP        = 0x0000ff00;
  static public final int UNDER_MAP       = 0x000000ff;

  static public final int PLACED_CRATE    = CRATE  +  GOAL;
  static public final int SOKOBAN_ON_GOAL = SOKOBAN  +  GOAL;

  private int player_at_position_x;
  private int player_at_position_y;
  private int moves;
  private final int[] map;
  private final int map_width;
  private final int map_height;
  private final Rect affected_grid;

  /**
   *
   * @param width
   * @param height
   */
  public GameGrid(int width, int height) {
    map_width = width;
    map_height = height;

    player_at_position_x = 0;
    player_at_position_y = 0;

    map = new int[map_width * map_height];
    affected_grid = new Rect(0,0,0,0);
  }

  public int getMapWidth() { return map_width; }
  public int getMapHeight() { return map_height; }
  public int getMoves() { return moves; }

  public String serialize() {
    StringBuilder str = new StringBuilder();
    for(int height_index = 0; height_index < map_height; height_index ++ ) {
      for(int width_index = 0; width_index < map_width; width_index ++ ) {

        int map_code = map[(height_index * map_width)  +  width_index];
        if(width_index == player_at_position_x && height_index == player_at_position_y) {
          map_code = map_code | SOKOBAN;
        }
        switch(map_code) {
          case WALL: str.append("#"); break;
          case GOAL: str.append("X"); break;
          case CRATE: str.append("C"); break;
          case SOKOBAN: str.append("P"); break;
          case PLACED_CRATE: str.append("!"); break;
          case SOKOBAN_ON_GOAL: str.append("?"); break;
          default:
            str.append(" ");
        }
      }
      str.append("\n");
    }
    return str.toString();
  }

  public boolean movePlayer(int direction) {
    switch(direction) {
      case NORTH: return tryMovingMan(0,-1);
      case EAST: return tryMovingMan(1,0);
      case WEST: return tryMovingMan(-1,0);
      case SOUTH: return tryMovingMan(0,1);
    }
    return false;
  }

  public int getTile(int postX, int postY) {
    if (postX == player_at_position_x && postY == player_at_position_y) {
      return SOKOBAN;
    }
    return getTileOnMap(postX,postY);
  }

  public int getTileOnMap(int postX, int postY) {
    int id_postX = postY * map_width  +  postX;
    if (id_postX >= map_width * map_height) {
      return FLOOR;
    }
    if((map[id_postX] & UNDER_MAP) == map[id_postX]) {
      return map[id_postX];
    } else {
      return map[id_postX] & OVER_MAP;
    }
  }

  public Rect lastAffectedArea() {
    return affected_grid;
  }

  /**
   *
   * @return boolean
   */
  public boolean gameWon() {
    for(int index = 0; index < map_width * map_height; index ++ ) {
      if((map[index] & UNDER_MAP) == GOAL && map[index] != PLACED_CRATE) {
        return false;
      }
    }
    return true;
  }

  /**
   * inseret un tuile dans une cell
   * @param postX
   * @param postY
   * @param tile
   */
  public void setTile(int postX, int postY, int tile) {
    int id_postX = (postY * map_width)  +  postX;
    if (id_postX < map_width * map_height) {
      if ((tile & OVER_MAP) == SOKOBAN) {
        player_at_position_x = postX;
        player_at_position_y = postY;
        tile -= SOKOBAN;
      } 
      map[id_postX] = ((tile & OVER_MAP) | map[id_postX] & OVER_MAP) | ((tile & UNDER_MAP) | map[id_postX] & UNDER_MAP);
    }
  }


  /**
   *
   * @param postX
   * @param postY
   */
  private void clearOverTile(int postX, int postY) {
    int id_postX = postY * map_width  +  postX;
    map[id_postX] = map[id_postX] & UNDER_MAP;
  }

  /**
   * Deplacement du mini personnage
   * @param move_axe_x
   * @param move_axe_y
   * @return
   */
  private boolean tryMovingMan(int move_axe_x, int move_axe_y) {
    int new_axe_x = player_at_position_x  +  move_axe_x;
    int new_axe_y = player_at_position_y  +  move_axe_y;
    if(validSpace(new_axe_x, new_axe_y, move_axe_x, move_axe_y)) {
      affected_grid.set(
        new_axe_x > player_at_position_x ? player_at_position_x - 1: new_axe_x - 1,
        new_axe_y > player_at_position_y ? player_at_position_y - 1: new_axe_y - 1,
        new_axe_x < player_at_position_x ? player_at_position_x  +  1: new_axe_x  +  1,
        new_axe_y < player_at_position_y ? player_at_position_y  +  1: new_axe_y  +  1
      );
      displaceCrates(new_axe_x, new_axe_y, move_axe_x, move_axe_y);
      player_at_position_x  += move_axe_x;
      player_at_position_y  += move_axe_y;
      moves++;
      return true;
    }
    return false;
  }

  /**
   * Determiner les cellules valides
   * @param postX position de base du careau suivant l'axe X
   * @param postY position de base du careau suivant l'axe y
   * @param valid_postX nouvelle position en axe X
   * @param valid_postY nouvelle position en axe y
   * @return boolean
   */
  private boolean validSpace(int postX, int postY, int valid_postX, int valid_postY) {
    if(postX >= map_width || postX < 0 || postY >= map_height || postY < 0) {
      return false;
    }
    if(getTile(postX,postY) == WALL) {
      return false;
    }
    if(getTile(postX,postY) == CRATE) {
      int dest = getTile(postX + valid_postX,postY + valid_postY);

      return dest == FLOOR || dest == GOAL;
    }
    return true;
  }

  /**
   *
   * @param postX position de base du careau suivant l'axe X
   * @param postY position de base du careau suivant l'axe y
   * @param valid_postX nouvelle position en axe X
   * @param valid_postY nouvelle position en axe y
   */
  private void displaceCrates(int postX, int postY, int valid_postX, int valid_postY) {
    if(getTileOnMap(postX,postY) == CRATE) {
      clearOverTile(postX,postY);
      setTile(postX + valid_postX,postY + valid_postY,CRATE);
    }
  }

}

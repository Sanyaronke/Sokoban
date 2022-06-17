package com.game.Sokoban;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.StringReader;

public class SokobanView extends View {
  private static final int TILE_SIZE = 60;
  private final Drawable sokoban;
  private final Drawable wall;
  private final Drawable crate;
  private final Drawable goal;
  private final Drawable floor;
  private int tiles_wide;
  private int tiles_high;
  private boolean tall;
  private int grid_x_lower_bound;
  private int grid_y_lower_bound;
  private int current_level;
  private final Point drag_start;
  private final Point drag_stop;
  private final MapList map_list;
  private GameGrid grid;
  private final PersistentStore store;

  @SuppressLint("UseCompatLoadingForDrawables")
  public SokobanView(GameOption context) {
    super(context);
    store = new PersistentStore(context);
    floor = getResources().getDrawable(R.drawable.wood);
    sokoban = getResources().getDrawable(R.drawable.sokoban);
    wall = getResources().getDrawable(R.drawable.wall);
    crate = getResources().getDrawable(R.drawable.crate);
    goal = getResources().getDrawable(R.drawable.goal);
    map_list = new MapList(getResources().openRawResource(R.raw.sokoban));
    drag_start = new Point();
    drag_stop = new Point();
  }

  public SokobanView(GameOption context, int level) {
    this(context);
    current_level = level;
    loadGame();
  }

  public SokobanView(Context context) {
    this((GameOption) context);
  }

  public GameGrid getGrid() { return grid; }
  public int getCurrentLevel() { return current_level; }

  public void retryLevel() {
    selectMap(current_level);
  }
  public void skipLevel() {
    nextLevel();
  }
  public void backToMenu() { // for debugging
    selectMap(11);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    updateStatusBar();
    int a_wid    = grid.getMapWidth();
    int a_height = grid.getMapHeight();
    Drawable tile;

    for(int x = 0; x < a_wid; x++) {
      for(int y = 0; y < a_height; y++) {
        tile = tileForLocation(x, y);
        int left, top, right, bottom;

        int x_offset;
        int y_offset;
        if(tall) {
          x_offset = (tiles_wide * TILE_SIZE - (a_height * TILE_SIZE)) / 2;
          y_offset = (tiles_high * TILE_SIZE - (a_wid * TILE_SIZE)) / 2;
        } else {
          x_offset = (tiles_wide * TILE_SIZE - (a_wid * TILE_SIZE)) / 2;
          y_offset = (tiles_high * TILE_SIZE - (a_height * TILE_SIZE)) / 2;
        }

        left = grid_x_lower_bound + y * TILE_SIZE + x_offset;
        top = grid_y_lower_bound + x * TILE_SIZE + y_offset;

        right = grid_x_lower_bound + y * TILE_SIZE + TILE_SIZE + x_offset;
        bottom = grid_y_lower_bound + x * TILE_SIZE + TILE_SIZE + y_offset;

        tile.setBounds(left, top, right, bottom);
        tile.draw(canvas);
      }
    }
  }

  @Override
  protected void onSizeChanged(int width, int height, int old_width, int old_height) {
    tiles_wide = width / TILE_SIZE;
    tiles_high = height / TILE_SIZE;
    tall = tiles_high > tiles_wide;
    int side_border_width;
    int side_border_height;

    if (tall) {
      side_border_width = (tiles_wide - grid.getMapHeight()) / 2;
      side_border_height = (tiles_high - grid.getMapWidth()) / 2;
    } else {
      side_border_width = (tiles_wide - grid.getMapWidth()) / 2;
      side_border_height = (tiles_high - grid.getMapHeight()) / 2;
    }

    grid_x_lower_bound = side_border_width;
    grid_y_lower_bound = side_border_height;
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch(event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        drag_start.set((int) event.getX(), (int) event.getY());
        break;
      case MotionEvent.ACTION_UP:
        drag_stop.set((int) event.getX(), (int) event.getY());
        touchMove();
        break;
    }
    return true;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    switch (keyCode) {
    case KeyEvent.KEYCODE_DPAD_UP:
      doMove(GameGrid.NORTH);
      break;
    case KeyEvent.KEYCODE_DPAD_DOWN:
      doMove(GameGrid.SOUTH);
      break;
    case KeyEvent.KEYCODE_DPAD_RIGHT:
      doMove(GameGrid.EAST);
      break;
    case KeyEvent.KEYCODE_DPAD_LEFT:
      doMove(GameGrid.WEST);
      break;
    default:
      return super.onKeyDown(keyCode, event);
    }
    return true;
  }

  protected void touchMove() {
    int delta_x = drag_stop.x - drag_start.x;
    int delta_y = drag_stop.y - drag_start.y;
    if (Math.abs(delta_x) < 10 && Math.abs(delta_y) < 10) {
      return;
    }
    if(Math.abs(delta_x) > Math.abs(delta_y)) {
      if(delta_x < 0) {
        doMove(GameGrid.EAST);
      } else {
        doMove(GameGrid.WEST);
      }
    } else {
      if(delta_y < 0) {
        doMove(GameGrid.NORTH);
      } else {
        doMove(GameGrid.SOUTH);
      }
    }
  }

  protected void doMove(int direction) {

    if (tall) {
      switch( direction ) {
        case GameGrid.SOUTH: direction = GameGrid.EAST; break;
        case GameGrid.NORTH: direction = GameGrid.WEST; break;
        case GameGrid.EAST: direction = GameGrid.NORTH; break;
        case GameGrid.WEST: direction = GameGrid.SOUTH; break;
      }
    }

    grid.movePlayer( direction );
    invalidate();
    updateStatusBar();

    if(grid.gameWon()) {
      levelWon();
      nextLevel();
    }
  }

  protected Drawable tileForLocation(int postX, int postY) {
    switch(grid.getTile(postX, postY)) {
      case GameGrid.FLOOR: return floor;
      case GameGrid.SOKOBAN: return sokoban;
      case GameGrid.WALL: return wall;
      case GameGrid.GOAL: return goal;
      case GameGrid.CRATE: return crate;
    }
    return floor;
  }


  protected void loadGame() {
    message("Loading game.");
    if (current_level == -1) {
      String saved_game = ((GameOption) getContext()).getSavedGame();
      if (saved_game == null) {
        message("No saved game found. Starting from first level.");
        current_level = 0;
        loadGame();
      } else {
        current_level = ((GameOption) getContext()).getSavedLevel();
        message("Saved game found for level #" + current_level);
        grid = new MapList(new StringReader(saved_game)).selectMap(0);
      }
    } else {
      message("Loading level #" + current_level);
      selectMap(current_level);
    }
  }

  protected void selectMap(int level) {
    grid = map_list.selectMap(level);
    Log.d("VIEW_SELECT_MAP", Integer.toString(grid.getMapWidth()));
    current_level = level;
    updateStatusBar();
    invalidate();
  }

  protected void levelWon() {
    store.addScore("main", current_level, grid.getMoves());
  }

  protected void nextLevel() {
    selectMap(current_level + 1);
  }

  protected void message(String message) {
    Log.d("SOKO", message);
  }

  protected void updateStatusBar() {
    ((GameOption) getContext()).setStatusBar(
      "Level: " + (current_level + 1) +
      " | Moves: " + grid.getMoves()
    );
  }
}


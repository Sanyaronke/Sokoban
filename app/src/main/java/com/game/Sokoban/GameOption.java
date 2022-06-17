package com.game.Sokoban;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Log;

public class GameOption extends Activity
{
    SokobanView sokoban_view;
    TextView status_bar;

    private static final String PREF_SAVED_GAME = "saved_game";
    private static final String PREF_CURRENT_LEVEL = "current_level";
    protected static final String KEY_LEVEL = "level";
    protected static final int LEVEL_CONTINUE = -1;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        ViewGroup frame;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        frame = (ViewGroup) findViewById(R.id.game_frame);
        status_bar = (TextView) findViewById(R.id.game_status_bar);
        sokoban_view = new SokobanView(this, getIntent().getIntExtra(KEY_LEVEL, 0));
        sokoban_view.setFocusable(true);
        sokoban_view.setFocusableInTouchMode(true);
        frame.addView(sokoban_view);
    }
    
    @Override
    protected void onPause() {
      super.onPause();
      saveGame();
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      saveGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu, menu);
      return true;
    }
    
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
        case R.id.retry_level:
          sokoban_view.retryLevel();
          return true;
        case R.id.skip_level:
          sokoban_view.skipLevel();
          return true;
        case R.id.win_level:
          sokoban_view.backToMenu();
          return true;
      }
      return false;
    }


    public String getSavedGame() {
      return getPreferences(MODE_PRIVATE).getString(PREF_SAVED_GAME, null);
    }

    public int getSavedLevel() {
      return getPreferences(MODE_PRIVATE).getInt(PREF_CURRENT_LEVEL, 0);
    }

    public void setStatusBar(String message) {
      status_bar.setText(message);
    }

    protected void saveGame() {
      d();
      getPreferences(MODE_PRIVATE).edit().
        putString(PREF_SAVED_GAME, sokoban_view.getGrid().serialize()).
        putInt(PREF_CURRENT_LEVEL, sokoban_view.getCurrentLevel()).
        apply();
    }

    protected void d() {
      Log.d("SOKO", "Saving game");
    }

}

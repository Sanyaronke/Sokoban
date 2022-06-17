package com.game.Sokoban;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class PlayerScore extends ListActivity
{

  private PersistentStore store;
  private SimpleCursorAdapter adapter;
  private static final String[] SCORE_FIELDS = new String[] { "levelset", "nice_level", "best_score" };
  private static final int[] SCORE_VIEWS = new int[] { R.id.score_level_set, R.id.score_level, R.id.high_score_moves };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    store = new PersistentStore(this);
    adapter = new SimpleCursorAdapter(this, R.layout.score_item, store.getScores(), SCORE_FIELDS, SCORE_VIEWS);
    getListView().addHeaderView(getLayoutInflater().inflate(R.layout.score_header, null));
    setListAdapter(adapter);
  }

}

package com.game.Sokoban;

import java.io.*;
import java.util.*;

public class MapList
{
  private final BufferedReader data;
  private final List<MapRecord> map_list;

  public MapList(InputStream input) {
    this(new InputStreamReader(input));
  }

  public MapList(Reader reader) {
    data = new BufferedReader(reader); 
    map_list = new ArrayList<MapRecord>();
    readData();
  }

  public GameGrid selectMap(int map) {
    MapRecord record = map_list.get(map);
    GameGrid grid = new GameGrid(record.getWidth(), record.getHeight());

    int x = 0;
    int y = 0;
    char[] data = record.getData().toCharArray();

    for(int index = 0; index < record.getData().length();index++) {
      if(data[index] == '\n') { x = 0; y ++; }
      else { grid.setTile(x, y, tileType(data[index])); x ++; }
    }
    
    return grid;
  }

  /**
   *
   * @param cellItem
   * @return int
   */
  protected int tileType(char cellItem) {
    switch(cellItem) {
      case '#': return GameGrid.WALL;
      case 'X': return GameGrid.GOAL;
      case 'C': return GameGrid.CRATE;
      case 'P': return GameGrid.SOKOBAN;
      case '!': return GameGrid.PLACED_CRATE;
      case '?': return GameGrid.SOKOBAN_ON_GOAL;
      default:  return GameGrid.FLOOR;
    }
  }

  protected void readData() {
    String line;
    String newMap;
    MapRecord record = new MapRecord();

    try {
      line = data.readLine();
      while(line != null) {
        if(line.charAt(0) == ';') {
          newMap = record.getData();
          if(newMap.length() > 0) {
            map_list.add(record);
          }
          record = new MapRecord();
        } else {
          record.addLine(line);
        }
        line = data.readLine();
      }
      newMap = record.getData();
      if(newMap.length() > 0) {
        map_list.add(record);
      }
    } catch(IOException except) {
      // TODO
    }
  }

  private static class MapRecord {
    private int width;
    private int height;
    private String data;
    private final StringBuilder data_builder;

    public MapRecord() {
      width = 0;
      height = 0;
      data = null;
      data_builder = new StringBuilder();
    }

    public String getData() {
      data = data == null ? data_builder.toString() : data;
      return data;
    }

    /**
     *
     * @return int
     */
    public int getWidth() { return width; }

    /**
     *
     * @return int
     */
    public int getHeight() { return height; }


    public void addLine(String line) {
      data_builder.append(line).append("\n");
      width = Math.max(line.length(), width);
      height += 1;
    }
  }
}

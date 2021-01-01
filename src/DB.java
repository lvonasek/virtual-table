import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DB
{
  private static final String SERVER = "";
  private static final String DATABASE = "";
  private static final String USENAME = "";
  private static final String PASSWORD = "";
  public static final String GAME_ADMIN_PASSWORD = "admin";
  public static final String GAME_ROOM_NAME = "";

  public class Card
  {
    public String front;
    public String back;
    public float x;
    public float y;
    public float viewX;
    public float viewY;
    public long timestamp;
  }
  
  public class Player
  {
    public String name;
    public String password;
    public String note;
    public float x1;
    public float y1;
    public float x2;
    public float y2;
  }
   
  private MysqlDataSource dataSource;

  public DB()
  {
    System.out.println("Connecting to DB");
    dataSource = new MysqlDataSource();
    dataSource.setUser(USENAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setServerName(SERVER);
    dataSource.setDatabaseName(DATABASE);
  }

  public void execute(String command)
  {
    try
    {
      Connection conn = dataSource.getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute(command);
      stmt.close();
      conn.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public String getCardsTableName()
  {
    if (GAME_ROOM_NAME.length() == 0)
    {
      return "`cards`";
    }
    else
    {
      return "`room_" + GAME_ROOM_NAME + "_cards`"; 
    }
  }
  
  public String getPlayersTableName()
  {
    if (GAME_ROOM_NAME.length() == 0)
    {
      return "`players`";
    }
    else
    {
      return "`room_" + GAME_ROOM_NAME + "_players`"; 
    }
  }

  public ArrayList<Card> readCards()
  {
    ArrayList<Card> cards = new ArrayList<Card>();
    try
    {
      Connection conn = dataSource.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * FROM " + getCardsTableName() + " ORDER BY `timestamp` ASC");
      while (rs.next()){
        Card c = new Card();
        c.front = rs.getString("front");
        c.back = rs.getString("back");
        c.x = rs.getFloat("x");
        c.y = rs.getFloat("y");
        c.viewX = c.x;
        c.viewY = c.y;
        c.timestamp = rs.getLong("timestamp");
        cards.add(c);
      }
      rs.close();
      stmt.close();
      conn.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      cards = null;
    }
    return cards;
  }
  
  public ArrayList<Player> readPlayers()
  {
    ArrayList<Player> players = new ArrayList<Player>();
    try
    {
      Connection conn = dataSource.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * FROM " + getPlayersTableName());
      while (rs.next()){
        Player p = new Player();
        p.name = rs.getString("name");
        p.password = rs.getString("password");
        p.note = rs.getString("note");
        p.x1 = rs.getFloat("x1");
        p.y1 = rs.getFloat("y1");
        p.x2 = rs.getFloat("x2");
        p.y2 = rs.getFloat("y2");
        players.add(p);
      }
      rs.close();
      stmt.close();
      conn.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      players = null;
    }
    return players;
  }
}

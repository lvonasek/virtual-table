import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class GUI extends JComponent implements KeyListener, MouseListener, MouseMotionListener, Runnable
{
  private final String CHARS = " qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789+-*/,.:?!@#$%^&*()=<>";
  
  private DB db;
  private boolean drag, swapped, previews;
  private HashMap<String, Image> images;
  private ArrayList<DB.Card> cards;
  private ArrayList<DB.Player> players;
  private float mx, my;
  private DB.Card selected;
  private String id, command;
  private int loaded;
  private int scale;
  private float align, zoom;
  
  public GUI(ArrayList<DB.Card> cards, ArrayList<DB.Player> players, String id, DB db, int scale)
  {
    this.db = db;
    this.cards = cards;
    this.players = players;
    this.id = id;
    this.scale = scale;
    images = new HashMap<String, Image>();
    command = null;
    selected = null;
    drag = false;
    previews = true;
    swapped = false;
    align = 0;
    zoom = 1;
    
    addComponentListener(new ComponentListener() {

      @Override
      public void componentHidden(ComponentEvent arg0)
      {
      }

      @Override
      public void componentMoved(ComponentEvent arg0)
      {
      }

      @Override
      public void componentResized(ComponentEvent arg0)
      {
        float aspect = 1366.0f / 768.0f;  
        Rectangle b = arg0.getComponent().getBounds();
        int w = b.width;
        int h = (int)(b.width / aspect);
        if (h > b.height)
        {
          w = (int)(b.height * aspect);
          h = b.height;
        }
        int x = Math.abs(b.width - w) / 2;
        int y = Math.abs(b.height - h) / 2;
        if ((x != 0) || (y != 0))
        {
          arg0.getComponent().setBounds(x, y, w, h); 
        }
      }

      @Override
      public void componentShown(ComponentEvent arg0)
      { 
      }
    });
  }
  
  public void draw(Graphics g, String path, float x, float y, float w, float h, boolean drawRect)
  {
    try 
    {
      path = path.replaceAll("#", "");
      if (!images.containsKey(path))
      {
        if (loaded > 5)
        {
          return;
        }
        System.out.println("Loading " + path);
        Image image = ImageIO.read(new File(path));
        if (scale > 0)
        {
          image = image.getScaledInstance(image.getWidth(null) / scale, image.getHeight(null) / scale, Image.SCALE_SMOOTH); 
        }
        images.put(path, image);
        loaded++;
      }
      if (swapped)
      {
        y = getHeight() - y - h;
      }
      g.setColor(Color.BLACK);
      g.drawImage(images.get(path), (int)x, (int)y, (int)w, (int)h, null);
      if (drawRect)
      {
        g.drawRect((int)x, (int)y, (int)w, (int)h); 
      }
    } catch (Exception e)
    {
      System.err.println(path + " not found!");
    }
  }

  public void paint(Graphics g)
  {
    loaded = 0;
    float w = getWidth();
    float h = getHeight();
    draw(g, "data/table.jpg", 0, 0, w, h, false);

    if (players != null)
    {
      g.setFont(new Font("serif", Font.BOLD, (int)(h / 40)));
      FontMetrics metrics = g.getFontMetrics(g.getFont());
      for (DB.Player player : players)
      {
        int x1 = (int)(player.x1 * w);
        int y1 = (int)(player.y1 * h);
        int x2 = (int)(player.x2 * w);
        int y2 = (int)(player.y2 * h);
        if (swapped)
        {
          g.drawRect(x1, (int)h - y2, x2 - x1, y2 - y1);
        }
        else
        {
          g.drawRect(x1, y1, x2 - x1, y2 - y1); 
        }
        
        boolean top = (player.y1 + player.y2) * 0.5f < 0.5f; 
        String text = player.note;
        int tw = metrics.stringWidth(text);
        int th = metrics.getHeight() / 2;
        if (swapped)
        {
          if (top) {
            g.drawString(text, (x1 + x2) / 2 - tw / 2, (int)h - y2 - th / 2); 
          } else {
            g.drawString(text, (x1 + x2) / 2 - tw / 2, (int)h - y1 + 3 * th / 2); 
          }
        }
        else
        {
          if (top) {
            g.drawString(text, (x1 + x2) / 2 - tw / 2, y2 + 3 * th / 2);
          } else {
            g.drawString(text, (x1 + x2) / 2 - tw / 2, y1 - th / 2);
          } 
        }
      }
    }
    
    if (!drag)
    {
      selected = null;
    }
    String selectedImage = null;
    if (cards != null)
    {
      for (DB.Card card : cards)
      {
        boolean moving = false;
        if (!drag || card != selected)
        {
          if (Math.abs(card.viewX - card.x) > 0.0001f)
          {
            if (Math.abs(card.viewY - card.y) > 0.0001f)
            {
              card.viewX = card.viewX * 0.8f + card.x * 0.2f;
              card.viewY = card.viewY * 0.8f + card.y * 0.2f;
              moving = true;
            }  
          }
        }
        
        float z = card.back.contains("/0.") ? 1 : zoom;
        float c = Math.min(w, h) / 100;
        float cw = c * card.w * z;
        float ch = c * card.h * z;
        float x = card.x * w;
        float y = card.y * h;
        String owner = null;
        for (DB.Player player : players)
        {
          int x1 = (int)(player.x1 * w);
          int y1 = (int)(player.y1 * h);
          int x2 = (int)(player.x2 * w);
          int y2 = (int)(player.y2 * h);
          if ((x1 < x) && (y1 < y) && (x2 > x) && (y2 > y))
          {
            owner = player.name;
          }
        }
        String image = owner == null || owner.compareToIgnoreCase(id) == 0 ? card.front : card.back;
        if (card.isDice())
        {
          image = card.back;
        }
        x = card.viewX * w;
        y = card.viewY * h;
        if ((align > 0) && (!card.isDice()) && !moving)
        {
          x = (int)(x * align / w) / (align / w);
          y = (int)(y * align / h) / (align / h); 
        }
        float x1 = x - cw;
        float y1 = y - ch;
        float x2 = x + cw;
        float y2 = y + ch;
        String file = card.back.replaceAll("#", "");
        draw(g, image, x1, y1, x2 - x1, y2 - y1, !file.endsWith(".png"));
        
        if (!drag && !card.front.contains("/0."))
        {
          if ((x1 < mx) && (y1 < my) && (x2 > mx) && (y2 > my))
          {
            selected = card;
            selectedImage = image;
          } 
        }
      }
    }
    
    if (previews)
    {
      if (selectedImage != null)
      {
        if (selectedImage.contains("/Image_"))
        {
          h *= 0.75f;
          w = 3.0f * h / 5.0f;
          draw(g, selectedImage, getWidth() - w + 1, 0, w, h, false); 
        }
      }      
    }
  }
  
  public void update(ArrayList<DB.Player> players, ArrayList<DB.Card> cards)
  {
    if (cards == null) return;
    if (cards.size() == 0) return;
    if (players == null) return;
    if (players.size() == 0) return;
    
    if (players != null)
    {
      HashMap<String, DB.Player> last = new HashMap<String, DB.Player>();
      for (DB.Player p : this.players)
      {
        last.put(p.name, p);
      }
      for (DB.Player p : players)
      {
        if (last.containsKey(p.name))
        {
          DB.Player patch = last.get(p.name);
          if (patch.name.compareToIgnoreCase(id) == 0)
          {
            p.note = patch.note;
          }
        }
      }
      this.players = players;
    }
    if (cards != null && !drag)
    {
      HashMap<String, DB.Card> last = new HashMap<String, DB.Card>();
      for (DB.Card c : this.cards)
      {
        last.put(c.front, c);
      }
      for (DB.Card c : cards)
      {
        if (last.containsKey(c.front) && (c.timestamp > 1000000))
        {
          DB.Card patch = last.get(c.front);
          if (patch.timestamp > c.timestamp)
          {
            c.back = patch.back;
            c.x = patch.x;
            c.y = patch.y;
            c.timestamp = patch.timestamp;
          }
          else if (patch.timestamp < c.timestamp)
          {
            c.viewX = patch.viewX;
            c.viewY = patch.viewY;
            c.timestamp = patch.timestamp;
          }
        }
      }
      this.cards = cards;
    }
    repaint();
  }

  @Override
  public void mouseDragged(MouseEvent event)
  {
    Point mouse = getMousePosition();
    if (mouse != null)
    {
      mx = mouse.x;
      my = mouse.y;
      if (swapped)
      {
        my = getHeight() - my;
      }
    }
    if (drag && selected != null)
    {
      selected.viewX = event.getX() / (float)getWidth();
      selected.viewY = event.getY() / (float)getHeight();
      if (swapped)
      {
        selected.viewY = 1.0f - selected.viewY;
      }
      selected.timestamp = System.currentTimeMillis();
      cards.remove(selected);
      cards.add(selected);
    }
    repaint();
  }

  @Override
  public void mouseMoved(MouseEvent event)
  {
    Point mouse = getMousePosition();
    if (mouse != null)
    {
      mx = mouse.x;
      my = mouse.y;
      if (swapped)
      {
        my = getHeight() - my;
      }
    }
    repaint();
  }

  @Override
  public void mouseClicked(MouseEvent event)
  {
  }

  @Override
  public void mouseEntered(MouseEvent event)
  {
  }

  @Override
  public void mouseExited(MouseEvent event)
  {
  }

  @Override
  public void mousePressed(MouseEvent event)
  {
    drag = true;
  }

  @Override
  public void mouseReleased(MouseEvent event)
  {
    if (drag && selected != null)
    {
      if (selected.isDice())
      {
        boolean inverted = selected.back.contains("inverted"); 
        int number = new Random(System.currentTimeMillis()).nextInt(6) + 1;
        if (inverted)
        {
          selected.back = String.format("data/common/%d.png", number);  
        }
        else
        {
          selected.back = String.format("data/common/%d_inverted.png", number);
        }
      }
      selected.x = selected.viewX;
      selected.y = selected.viewY;
      String x = (selected.x + "").replace(',', '.');
      String y = (selected.y + "").replace(',', '.');
      String command = "UPDATE " + db.getCardsTableName() + " SET `x`=" + x + ",`y`=" + y + ",`timestamp`=" + selected.timestamp + ",`back`='" + selected.back + "' WHERE `front`='" + selected.front + "'";
      db.execute(command);
    }
    drag = false;
    repaint();
  }

  @Override
  public void run()
  {
    while (true)
    {
      try
      {
        Thread.sleep(50);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      repaint();
    }
  }

  @Override
  public void keyPressed(KeyEvent event)
  {
  }

  @Override
  public void keyReleased(KeyEvent event)
  {
  }

  @Override
  public void keyTyped(KeyEvent event)
  {
    DB.Player me = null;
    for (DB.Player player : players)
    {
      if (player.name.compareToIgnoreCase(id) == 0)
      {
        me = player;
      }
    }
    
    if (me != null)
    {
      boolean updated = false;
      char c = event.getKeyChar();
      if (c == 8)
      {
        if (me.note.length() > 0)
        {
          me.note = me.note.substring(0, me.note.length() - 1);
          updated = true;
        }
      } else if (CHARS.indexOf(c) >= 0) {
        me.note = me.note + c;
        updated = true;
      }
      
      if (updated)
      {
        if (me.note.length() >= 24)
        {
          me.note = me.note.substring(0, 23);
        }
        command = "UPDATE `players` SET `note`='" + me.note + "' WHERE `name`='" + id + "'";
      }
    }
  }
  
  public String getCommand()
  {
    String output = command;
    command = null;
    return output;
  }
  
  public void setAlign(float value) {
    align = value;
  }
  
  public void setPreviews(boolean on) {
    previews = on;
  }
  
  public void setSwapped(boolean on) {
    swapped = on;
  }
  
  public void setZoom(float value) {
    zoom = value;
  }
}

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Main
{ 
  private static DB db;
  private static GUI gui;
  private static JFrame window;
  private static int scale;
  
  public static void main(String[] args) throws Exception
  {
    db = new DB();
    scale = getScale(args);
    window = new JFrame();
    new LoginDialog(window).setVisible(true);
  }
  
  private static void createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    
    //file items
    JMenu file = new JMenu("File");
    JMenuItem exitButton = new JMenuItem("Exit");
    exitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        System.exit(0);
      }
    });
    file.add(exitButton);
    menuBar.add(file);
    
    //view items
    JMenu view = new JMenu("View");
    final JCheckBoxMenuItem mirrorButton = new JCheckBoxMenuItem("Vertical mirror of table");
    mirrorButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        gui.setSwapped(mirrorButton.getState());
      }
    });
    view.add(mirrorButton);
    final JCheckBoxMenuItem previewsButton = new JCheckBoxMenuItem("Show card under mouse cursor");
    previewsButton.setState(true);
    previewsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        gui.setPreviews(previewsButton.getState());
      }
    });
    view.add(previewsButton);
    final JCheckBoxMenuItem snadTOGrid = new JCheckBoxMenuItem("Snap to grid");
    snadTOGrid.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        gui.setAlign(snadTOGrid.getState() ? 60 : 0);
      }
    });
    view.add(snadTOGrid);
    menuBar.add(view);
    
    //zoom items
    JMenu zoom = new JMenu("Size");
    for (int i = 50; i <= 200; i += 25) {
      final float value = i * 0.01f;
      JMenuItem zoomButton = new JMenuItem(i + "%");
      zoomButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
          gui.setZoom(value);
        }
      });
      zoom.add(zoomButton); 
    }
    menuBar.add(zoom);

    window.setJMenuBar(menuBar);
    window.setTitle("Virtual Table");
}
  
  private static int getScale(String[] args)
  {
    int output = 0;
    if (args != null && args.length > 0)
    {
      for (int i = 0; i < args[0].length(); i++)
      {
        char c = args[0].charAt(i);
        if (c >= '0' && c <= '9')
        {
          output = 10 * output + c - '0'; 
        }  
      }
    }
    return output;
  }
  
  private static class LoginDialog extends JDialog implements KeyListener
  {
    private JButton btnLogin;
    
    public LoginDialog(JFrame parent)
    {
        super(parent, "Login", true);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
        
        final JLabel lbMessage = new JLabel("Please enter your login data");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(lbMessage, cs);
        
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(new JLabel(" "), cs);
        
        JLabel lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);
 
        final JTextField tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);
        tfUsername.addKeyListener(this);
 
        JLabel lbPassword = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 3;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);
 
        final JPasswordField pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 3;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        pfPassword.addKeyListener(this);

        btnLogin = new JButton("Login");
        btnLogin.addKeyListener(this);
        btnLogin.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
              boolean valid = false;
              
              //admin mode
              if ("admin".compareToIgnoreCase(tfUsername.getText()) == 0)
              {
                if (DB.GAME_ADMIN_PASSWORD.compareToIgnoreCase(pfPassword.getText()) == 0)
                {
                  window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                  window.setBounds(40, 40, 640, 360);
                  window.setVisible(true);
                  dispose();
                  valid = true;
                  
                  JPanel panel = new JPanel(new GridBagLayout());
                  GridBagConstraints cs = new GridBagConstraints();
                  final JLabel feedback = new JLabel("");
                  panel.add(feedback, cs);
                  int index = 1;
                  
                  File dir = new File("./sql");
                  for (final File file : dir.listFiles())
                  {
                    if (!file.isDirectory())
                    {
                      JButton button = new JButton(file.getName());
                      button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0)
                        {
                          try
                          {
                            Random rnd = new Random(System.currentTimeMillis());
                            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                            Scanner sc = new Scanner(fis);
                            while (sc.hasNext()) {
                              String line = sc.nextLine();
                              while (line.contains("%RND%"))
                              {
                                int value = rnd.nextInt(10000);
                                line = line.replaceFirst("%RND%", "" + value);
                              }
                              line = line.replaceAll("%CARDS%", db.getCardsTableName());
                              line = line.replaceAll("%PLAYERS%", db.getPlayersTableName());
                              db.execute(line);
                            }
                            sc.close();
                            fis.close();
                            feedback.setText(file + " - success");
                          } catch (Exception e)
                          {
                            feedback.setText(file + "\n" + e.toString());
                            e.printStackTrace();
                          }
                        }
                      });
                      cs.gridx = 0;
                      cs.gridy = index++;
                      panel.add(button, cs);
                    }
                  }
                  window.add(panel);
                }
              }
              //game mode
              else
              {
                ArrayList<DB.Card> cards = db.readCards();
                ArrayList<DB.Player> players = db.readPlayers();
                for (DB.Player player : players)
                {
                  if (player.name.compareToIgnoreCase(tfUsername.getText()) == 0)
                  {
                    if (player.password.compareToIgnoreCase(pfPassword.getText()) == 0)
                    {
                      gui = new GUI(cards, players, tfUsername.getText(), db, scale);
                      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                      window.setBounds(40, 40, 640, 360);
                      window.setExtendedState(window.getExtendedState()|JFrame.MAXIMIZED_BOTH);
                      window.addKeyListener(gui);
                      window.addMouseListener(gui);
                      window.addMouseMotionListener(gui);
                      window.getContentPane().add(gui);
                      window.setVisible(true);
                      createMenuBar();
                      dispose();
                      UpdateThread();
                      valid = true;
                    }
                  }
                } 
              }
              
              if (!valid)
              {
                lbMessage.setText("Wrong login data!");
              }
            }
        });
        JPanel bp = new JPanel();
        bp.add(btnLogin);
 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
    
    @Override
    public void keyPressed(KeyEvent event) {
    }

    @Override
    public void keyReleased(KeyEvent event) {
    }

    @Override
    public void keyTyped(KeyEvent event) {
      if (event.getKeyChar() == 10) {
        btnLogin.doClick();
      }
    }
  }
  
  private static void UpdateThread()
  {
    new Thread(gui).start();
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (true)
        {
          try
          {
            Thread.sleep(500);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          String cmd = gui.getCommand();
          if (cmd != null)
          {
            db.execute(cmd);
          }
          gui.update(db.readPlayers(), db.readCards());
          System.gc();
        }
      }
    }).start();
  }
}

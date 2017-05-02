package jtorrent.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

import jtorrent.client.Client;
import jtorrent.common.JTorrent;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GUI class for peer
 * @author Jeffrey Hensel
 *
 */
public class GUI implements ActionListener{
  
  private String CLIENT_NAME = "Peer Service";
  private String hostName = "http://172.17.96.1";
  private int portNumber = 222;

  private JList<String> hostList;
  private DefaultListModel<String> hostLM;
  private File selectedFile;
  private String[] hostListInfo;
  public static final String refreshTAG = "Refresh List";
  private static final String ANNOUNCE_PATH = "/announce"; // HTTP url path for announce requests
  private static final String NEW_TORRENT_PATH = "/new_torrent";
  private static final String IMPORT_TORRENT = "Import Torrent";
  private static final String NEW_TORRENT = "Create Torrent";
  private static final String UPDATE_SERVER = "Update Server Info";
  private ConcurrentLinkedQueue<Client> clientList = new ConcurrentLinkedQueue<Client>();
  private JTextField portField;
  private JTextField serverField;
  
  
  /**
   * Single extra thread which keeps the right hand list updated based on the clientList object.
   */
  public class RefreshTorrents implements Runnable {
    /**
     * Main refresh method which is called every ~1 sec
     */
    public void doRefresh(){
      
      Object[] curClients =  clientList.toArray();
      hostListInfo = new String[curClients.length];
      for(int i = 0; i < curClients.length; i++){
        if (!hostLM.getElementAt(i).equals(((Client)curClients[i]).getTorrent().getName() + 
            " Current State: " + ((Client)curClients[i]).getState().name())){
          hostLM.setElementAt(((Client)curClients[i]).getTorrent().getName() + 
              " Current State: " + ((Client)curClients[i]).getState().name(), i);
        }
      }
    }
    
    public void run() {
      // TODO Auto-generated method stub
      while (true){
        doRefresh();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
    }
  }
  
	/**
	 * Main creator for the GUI
	 */
  public GUI(){
		
    JFrame guiFrame = new JFrame(CLIENT_NAME);
    guiFrame.setLayout(new GridLayout(0,2,25,25));
    guiFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("tmpicon.png"));
    
    hostLM = new DefaultListModel();
    // Add current host points
    hostList = new JList(hostLM);
    
    JPanel westPanel = new JPanel();
    JButton searchFiles = new JButton(NEW_TORRENT);
    JButton updateButton = new JButton(UPDATE_SERVER);
    JButton importTor = new JButton(IMPORT_TORRENT);
    portField = new JTextField(20);
    portField.setText("Enter Port");
    serverField = new JTextField(20);
    serverField.setText("Enter Server IP");
    
//    JButton refreshButton = new JButton(refreshTAG);
    westPanel.add(searchFiles);
//    westPanel.add(refreshButton);
    westPanel.add(importTor);
    westPanel.add(updateButton);
    westPanel.add(serverField);
    westPanel.add(portField);


    //hostList.addListSelectionListener(this);
    searchFiles.addActionListener(this);
    updateButton.addActionListener(this);
//    refreshButton.addActionListener(this);
    importTor.addActionListener(this);
    
    guiFrame.add(westPanel);
    guiFrame.add(new JScrollPane(hostList));
    
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //guiFrame.getContentPane().add(emptyLable, BorderLayout.CENTER);
    guiFrame.pack();
    guiFrame.setVisible(true);
    guiFrame.setSize(new Dimension(1000, 500));
		
    
    //Create auto-refresher for the right panel
    (new Thread(new RefreshTorrents())).start();
		
    //System.out.println("Ping time: " + msPing(hostName, portNumber) + " MS\n");
	}
	
	/** Pings address 10 times then averages ping
	* @return msPing
	*
	*/
	public long msPing(String addr, int port){
		long avgMSPing = 0;
		long start = 0;
		long end = 0;
		long curTotal = 0;
		Socket sc = null;
		InetSocketAddress ieSocket; 
		for (int i = 0; i < 5; i++){
			start = System.currentTimeMillis();
			try {
				InetAddress ia = InetAddress.getByName(addr);
				ia.isReachable(1000);// 1 sec timeout
			} catch (Exception e) {
				System.out.println("Failed to Ping:" + addr + " on port: " + port + "\n");
			}
			end = System.currentTimeMillis();
			curTotal += (end - start);
			
		}
		avgMSPing = (curTotal / 5);
		return avgMSPing;
	}
	
	
	public void updateList(){
		// Clear the current list
		for (int i = 0; i < hostLM.size(); i++){
			hostLM.remove(i);
		}
		// Request new list from server
		try {
		  URL url = new URL(hostName + ":" + portNumber + ANNOUNCE_PATH);
		  BufferedReader in = new BufferedReader(
        new InputStreamReader(url.openStream()));

//      String inputLine;
//      while ((inputLine = in.readLine()) != null)
//          System.out.println(inputLine);
//      in.close();
      
      hostListInfo = new String[20];
      
      String inputLine;
      int tmp = 0;
      while ((inputLine = in.readLine()) != null){
        hostListInfo[tmp] = inputLine;
        System.out.println("Added: " + hostListInfo[tmp] + " at index: " + tmp); 
        System.out.println(inputLine);
        tmp++;
      }
      in.close();
      
      
//      for (int i = 0; i < listSize; i++){
//        fromServer = in.readLine();
//        out.println("Next");
//        hostListInfo[i] = fromServer;
//        System.out.println("Added: " + hostListInfo[i] + " at index: " + i);
//        hostLM.addElement(hostListInfo[i]);
//      }
//      HttpURLConnection connection = null;
//      connection = (HttpURLConnection) url.openConnection();
//      connection.setRequestMethod("GET");
//      connection.setRequestProperty("Content-Type", 
//          "application/octet-stream");
//      connection.setUseCaches(false);
//      connection.connect();
//      
//      ObjectOutputStream wr = new ObjectOutputStream (
//            connection.getOutputStream());
//      wr.writeObject(/*m*/"test");
//      wr.close();
//      connection.setConnectTimeout(10);
//      connection.setReadTimeout(10);
//      connection.getResponseMessage();
//      connection.getInputStream();
			
		} catch (Exception e) {
			System.out.println("Caught exception");
		}
	}
	
	
	/**
	 * Select new file to become a new torrent.
	 */
	public void selectBehavior(){
	  JFileChooser fc = new JFileChooser();
    
    
    fc.setCurrentDirectory(new File(System.getProperty("user.home")));
    fc.setDialogTitle("Select File to Upload");
    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fc.setAcceptAllFileFilterUsed(false);

    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      selectedFile = fc.getSelectedFile();
      try {
        System.out.println("Creating metainfo");
        System.out.println("internet addr: " + InetAddress.getLocalHost().getHostAddress());
        Metainfo m = Metainfo.createTorrentFromFile(selectedFile, 
            InetAddress.getLocalHost().getHostAddress());

        // Make http request to tracker server
        System.out.println("Sending to: " + hostName + ":" + portNumber + NEW_TORRENT_PATH);
        URL url = new URL(hostName + ":" + portNumber + NEW_TORRENT_PATH);
        HttpURLConnection connection = null;

        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", 
            "application/octet-stream");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.connect();
        
        ObjectOutputStream wr = new ObjectOutputStream (
              connection.getOutputStream());
        wr.writeObject(m);
        wr.close();
        connection.setConnectTimeout(1);
        connection.setReadTimeout(1);
//        connection.getResponseMessage();
        connection.getResponseCode();
        connection.disconnect();
        System.out.println("Sent info");
        System.out.println("Setting up seeder");
        JTorrent tor = new JTorrent(m, true);
        System.out.println("New Torrent written to: " + m.writeToFile());
        Client cli = Client.newSeeder(Inet4Address.getLocalHost(), 
            portNumber, tor, selectedFile);
        cli.start();
        clientList.add(cli);
        hostLM.addElement(cli.getTorrent().getName() + " Current State: " + cli.getState().name());
        System.out.println("Became a seeder");
      } catch (Exception ex){
        ex.printStackTrace();
      }
        
        
      System.out.println("getCurrentDirectory(): " + fc.getCurrentDirectory());
      System.out.println("getSelectedFile() : " + selectedFile);
    } else {
      System.out.println("No Selection ");
    }
	}
	
	/**
	 * Do behavior for import. This imports a new torrent to the GUI to be seeded/leech.
	 */
	public void importBehavior(){
	  try {
      JFileChooser fc2 = new JFileChooser();

      fc2.setCurrentDirectory(new File(System.getProperty("user.home")));
      fc2.setDialogTitle("Select File to Import");
      fc2.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fc2.setAcceptAllFileFilterUsed(false);

      if (fc2.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        selectedFile = fc2.getSelectedFile();
        Metainfo m = Metainfo.readFromFile(selectedFile.getAbsolutePath());
        JTorrent tor = new JTorrent(m, true);
        Client cli = Client.newLeecher(Inet4Address.getLocalHost(), 
            portNumber, tor);
        cli.start();
        clientList.add(cli);
        hostLM.addElement(cli.getTorrent().getName() + " Current State: " + cli.getState().name());
        System.out.println("getCurrentDirectory(): " + fc2.getCurrentDirectory());
        System.out.println("getSelectedFile() : " + selectedFile);
      } else {
        System.out.println("No Selection ");
      }
    } catch (Exception ex){
      ex.printStackTrace();
    }
	}
	
	/**
	 * Listen to object/button presses.
	 */
	public void actionPerformed(ActionEvent e){
	  System.out.println(e.getActionCommand());
		
	  switch(e.getActionCommand()){
	    case UPDATE_SERVER:
	      try {
  	      hostName = serverField.getText();
  	      portNumber = Integer.parseInt(portField.getText());
	      } catch (Exception ee){}
	      System.out.println("Host name: " + hostName + " on port: " + portNumber);
				break;
			case NEW_TORRENT:
				selectBehavior();
				break;
			case IMPORT_TORRENT:
			  importBehavior();
			  break;
      case refreshTAG:
        updateList();
        break;
	  }
		
	}
}

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
  private String hostName = "http://10.21.76.195";
  private int portNumber = 4930;

  private JList hostList;
  private DefaultListModel hostLM;
  private File selectedFile;
  private String[] hostListInfo;
  public static final String refreshTAG = "Refresh List";
  private static final String ANNOUNCE_PATH = "/announce"; // HTTP url path for announce requests
  private static final String NEW_TORRENT_PATH = "/new_torrent";
  private static final String IMPORT_TORRENT = "Import Torrent";
  private ConcurrentLinkedQueue<Client> clientList = new ConcurrentLinkedQueue<Client>();
	
  public void GUI(){
		
    JFrame guiFrame = new JFrame(CLIENT_NAME);
    guiFrame.setLayout(new GridLayout(0,2,25,25));
    guiFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("tmpicon.png"));
    
    hostLM = new DefaultListModel();
    // Add current host points
    hostList = new JList(hostLM);
    
    JPanel westPanel = new JPanel();
    JButton searchFiles = new JButton("Select File");
    JButton getFile = new JButton("Get File");
    JButton importTor = new JButton(IMPORT_TORRENT);
    
    JButton refreshButton = new JButton(refreshTAG);
    westPanel.add(searchFiles);
    westPanel.add(getFile);
    westPanel.add(refreshButton);
    westPanel.add(importTor);



    //hostList.addListSelectionListener(this);
    searchFiles.addActionListener(this);
    getFile.addActionListener(this);
    refreshButton.addActionListener(this);
    
    guiFrame.add(westPanel);
    guiFrame.add(new JScrollPane(hostList));
    
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //guiFrame.getContentPane().add(emptyLable, BorderLayout.CENTER);
    guiFrame.pack();
    guiFrame.setVisible(true);
    guiFrame.setSize(new Dimension(800, 500));
		
		
		
		try {
			Socket serverSoc = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(serverSoc.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(serverSoc.getInputStream()));
			
			String toSend = "";
			String fromServer = "";
			File tmp = new File("C:\\");
			System.out.println(tmp.getFreeSpace());
			
			out.println("List Size");
			fromServer = in.readLine();
			int listSize = Integer.parseInt(fromServer);
			System.out.println("Hosts found: " + listSize);
			hostListInfo = new String[listSize];
			
			out.println("List");
			for (int i = 0; i < listSize; i++){
				fromServer = in.readLine();
				out.println("Next");
				hostListInfo[i] = fromServer;
				System.out.println("Added: " + hostListInfo[i] + " at index: " + i);
				hostLM.addElement(hostListInfo[i]);
			}
			
			//hostList = new JList(hostLM);
			
			
			while ((fromServer = in.readLine()) != null){
				if (fromServer.equals("Bye."))
					break;
				if (!fromServer.contains("null"))
					System.out.println(fromServer);
				//out.println("List");
				out.println("Finished");
			}
			
		} catch (Exception e) {
			System.out.println("Caught exception");
		}
		/*
        try {
			Socket kkSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(kkSocket.getInputStream()));
			System.out.println("AFTER1");
            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;
                
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
		
		*/
		System.out.println("Ping time: " + msPing(hostName, portNumber) + " MS\n");
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
	
	
	public String getNextResponse(String prev, BufferedReader in){
		String toRet = "";
		try {
			while ((toRet = in.readLine()).equals(prev)){}
		} catch (Exception e) {}
		return toRet;
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
	
	
	public void actionPerformed(ActionEvent e){
		System.out.println(e.getActionCommand());
		
		switch(e.getActionCommand()){
			case "Select Host":
				System.out.println("Selected host: " + hostList.getSelectedValue());
				break;
			case "Select File":
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
            JTorrent tor = new JTorrent(m, true);
            Client cli = Client.newSeeder(InetAddress.getByName(InetAddress.getLocalHost().getHostName()), 
                portNumber, tor, selectedFile);
            cli.start();
            clientList.add(cli);
						// Make http request to tracker server
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
		        connection.setConnectTimeout(10);
		        connection.setReadTimeout(10);
		        connection.getResponseMessage();
		        System.out.println("Sent info");
					} catch (Exception ex){
					  ex.printStackTrace();
					}
				    
				    
					System.out.println("getCurrentDirectory(): " + fc.getCurrentDirectory());
					System.out.println("getSelectedFile() : " + selectedFile);
				} else {
					System.out.println("No Selection ");
				}
				
				break;
			case IMPORT_TORRENT:
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
            Client cli = Client.newLeecher(InetAddress.getByName(InetAddress.getLocalHost().getHostName()), 
                portNumber, tor);
            cli.start();
            clientList.add(cli);
            System.out.println("getCurrentDirectory(): " + fc2.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + selectedFile);
          } else {
            System.out.println("No Selection ");
          }
			  } catch (Exception ex){
          ex.printStackTrace();
        }
			  
				break;
			case refreshTAG:
				updateList();
				break;
		}
		
	}
}
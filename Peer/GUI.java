
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * GUI for peer service.
 * @author Jeffrey Hensel
 * Peer GUI this is the user level interface
 */
public class GUI implements ActionListener{
	
  // gui string
	private String CLIENT_NAME = "Peer Service";
	// IP of major host
	private String hostName = "127.0.0.1";
	// Port to connect on
  private int portNumber = 222;
  // List to be made for right list
	private JList hostList;
	// Used for host list
	private DefaultListModel hostLM;
	// What file to be transfered
	private File selectedFile;
	// List string info
	private String[] hostListInfo;
	// Refresh button string
	public static final String refreshTAG = "Refresh List";
	
	/**
	 * Public GUI for the peer service
	 */
	public void GUI(){
		
		JFrame guiFrame = new JFrame(CLIENT_NAME);
		guiFrame.setLayout(new GridLayout(0,2,25,25));
		guiFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("tmpicon.png"));
		
		hostLM = new DefaultListModel();
		// Add current host points
		hostList = new JList(hostLM);
		
		JPanel westPanel = new JPanel();
		JButton searchFiles = new JButton("Select File");
		JButton selectHost = new JButton("Select Host");
		
		JButton refreshButton = new JButton(refreshTAG);
		westPanel.add(searchFiles);
		westPanel.add(selectHost);
		westPanel.add(refreshButton);
		
		
		
		//hostList.addListSelectionListener(this);
		searchFiles.addActionListener(this);
		selectHost.addActionListener(this);
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
	
	/**
	 * Gets next in from server
	 * @param prev
	 * @param in
	 * @return
	 */
	public String getNextResponse(String prev, BufferedReader in){
		String toRet = "";
		try {
			while ((toRet = in.readLine()).equals(prev)){}
		} catch (Exception e) {}
		return toRet;
	}
	
	/**
	 * Update list from main host
	 */
	public void updateList(){
		// Clear the current list
		for (int i = 0; i < hostLM.size(); i++){
			hostLM.remove(i);
		}
		// Request new list from server
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
			
			
			while ((fromServer = in.readLine()).equals("null")){
				if (fromServer == "Bye.")
					break;
				if (!fromServer.contains("null"))
					System.out.println(fromServer);
				//out.println("List");
				out.println("Finished");
			}
			
		} catch (Exception e) {
			System.out.println("Caught exception");
		}
	}
	
	/**
	 * Listener for button presses
	 */
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
					System.out.println("getCurrentDirectory(): " + fc.getCurrentDirectory());
					System.out.println("getSelectedFile() : " + selectedFile);
				} else {
					System.out.println("No Selection ");
				}
				
				break;
			case refreshTAG:
				updateList();
				break;
		}
		
	}
}
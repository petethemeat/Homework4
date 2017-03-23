import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Server {

	public static Inventory inv = new Inventory();

	public static OrderTable orders = new OrderTable();
	
	public static LinkedList<Command> queue = new LinkedList<Command>();

	public static ArrayList<String[]> servers = new ArrayList<String[]>();

	private static Integer clock = 0; 

	public static Integer myID;

	public static Semaphore s = new Semaphore(1);

	public static Semaphore t = new Semaphore(1);

	public static void main(String[] args) {

		clock = 0;
		s = new Semaphore(1);
		t = new Semaphore(1);

		Scanner sc = new Scanner(System.in);
		myID = sc.nextInt();
		int numServer = sc.nextInt();
		String inventoryPath = sc.next();

		System.out.println("[DEBUG] my id: " + myID);
		System.out.println("[DEBUG] numServer: " + numServer);
		System.out.println("[DEBUG] inventory path: " + inventoryPath);

		servers = new ArrayList<>();

		for (int i = 0; i < numServer; i++) {
			// TODO: parse inputs to get the ips and ports of servers
			String str = sc.next();
			System.out.println("address for server " + i + ": " + str);

			String[] elements = str.split(":");
			servers.add(elements);
		}

		sc.close();

		inv = new Inventory();

		// parse the inventory file

		File inventoryFile = new File("input/inventory.txt");

		try {
			Scanner fileReader = new Scanner(inventoryFile);

			while (fileReader.hasNextLine()) {
				String entry = fileReader.nextLine();
				if (entry.contentEquals(""))
					break;
				String[] tokens = entry.split(" ");
				inv.insert(tokens[0], Integer.parseInt(tokens[1]));
			}

			fileReader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File not found: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	
			try {
				ServerSocket tcpListener = new ServerSocket(Integer.parseInt(servers.get(myID-1)[1]));
				System.out.println("Server host: "+servers.get(myID-1)[0]+" ,port number: "+servers.get(myID-1)[1]);
				Socket dataSocket = new Socket();

				System.out.println("Server started to listen Tcp Port");
				while ((dataSocket = tcpListener.accept()) != null) {
					Thread serverThread = new Thread(new ServerThread(inv,orders,dataSocket));
					serverThread.start();	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

		
		
	}

	public static void enqueue(Command com) {

		try {
			s.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < queue.size(); i++) {

			Command temp = queue.get(i);

			if (temp.timeStamp > com.timeStamp) {
				queue.add(i, com);
				s.release(1);
				return;
			}

			else if (temp.timeStamp == com.timeStamp) {
				if (temp.serverId > com.serverId) {
					queue.add(i, com);
					s.release(1);
					return;
				}
			}
		}

		queue.add(com);
		s.release(1);
	}

	public static Command dequeue() {
		try {
			s.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Command com = queue.removeFirst();

		//Server.class.notifyAll();

		s.release(1);
		return com;
	}

	public static Command getFirst() {
		return queue.getFirst();
	}

	public static synchronized void checkQueue(Command com) {
		Command temp;
		while ((temp = queue.getFirst()) != com) {
			try {
				Server.class.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static int getClock() {
		try {
			t.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int temp = clock;
		t.release(1);

		return temp;
	}

	public static int incrementAndGetClock() {
		try {
			t.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int temp = clock;
		clock++;
		t.release(1);

		return temp;

	}

	public static void updateClock(int timeStamp) {
		try {
			t.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clock = Integer.max(clock, timeStamp) + 1;
		t.release(1);
	}

}


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.*;



public class ServerThread implements Runnable {

	Inventory inv;
	OrderTable orders;
	DatagramSocket udpsocket;
	DatagramPacket udprPacket;
	Socket tcpsocket;


	public ServerThread(Inventory inv, OrderTable orders, Socket tcpsocket) {
		this.inv = inv;
		this.orders = orders;
		this.tcpsocket = tcpsocket;
	}

	public void run() {
		System.out.println("Thread is started..");
		String received = null;
		try {

				System.out.println("Tcp starts");
				Scanner sc = new Scanner(tcpsocket.getInputStream());
				PrintWriter tcpOutput = new PrintWriter(tcpsocket.getOutputStream());

				String command = sc.nextLine();
				Scanner st = new Scanner(command);
				
				String tag = st.next();
				
				if(tag.contentEquals("server"))
				{
					int serverId = Integer.parseInt(st.next());
					int timeStamp = Integer.parseInt(st.next());
					String com = sc.nextLine();
					
					processServerRequest(new Command(serverId, timeStamp, com), tcpOutput);
				}
				
				else if(tag.contentEquals("purchase") || tag.contentEquals("cancel"))
				{
					
					Command com = new Command(Server.myID, Server.incrementAndGetClock(), command);
					
					Server.enqueue(com);		//Push request onto own queue
					
					sendServerRequest(com);		//Send a request to every server. Wait for reply from all other servers
					
					Server.checkQueue(com);		//Wait for own request to be at the head of the queue
					
					String response = processCommand(st, tag);	//Enter critical section
					sendClientResponse(response, tcpOutput);
					
					Server.dequeue();			//Remove request from own queue
					
					notifyServer();				//Notify other servers to remove request from their queues
							
				}
				
				else if(tag.contentEquals("dequeue"))
				{
					Command request = Server.getFirst();
					Scanner s = new Scanner(request.command);
					processCommand(s, s.next());
					
					Server.dequeue();
				}
				
				else
				{

					String response = processCommand(st, tag);
					sendClientResponse(response, tcpOutput);
					
				}
				
				tcpOutput.flush();

			    System.out.println("Tcp ends");
	
			
		} catch (IOException e) {
			System.out.println("Error: " + e);
		}
		System.out.println("Thread is finished..");
	}

	private String processCommand(Scanner st, String tag){

		String response = "";

		if(tag.contentEquals("purchase")){
			System.out.println("Purchase is processing..");
			
			String username = st.next();
			String productName = st.next();
			int quantity = Integer.parseInt(st.next());
			
			Inventory.Product p;
			if ((p = inv.getProduct(productName)) == null) {
				response = "Not Available- We do not sell this product";

			} else if (!inv.purchase(p, quantity)) {
				response = "Not Available - Not enough item ";

			} else {
				int orderId = orders.order(username,productName, quantity);
				response = "You order has been placed, " + orderId + " " + username + " "
						+ productName + " " + quantity;

			}

		}
		
		else if(tag.contentEquals("cancel"))
		{
			System.out.println("Cancel is processing..");
			int orderId = Integer.parseInt(st.next());
			if(orders.cancel(orderId, inv)){
				response = "Order "+orderId+" is canceled";
			}
			else{
				response = ""+orderId+" not found,no such order";
			}
		}

		
		else if(tag.contentEquals("search"))
		{
			System.out.println("Search is processing..");
			String user = st.next();
			OrderTable.Order[] userOrders = orders.search(user);
			
			if(userOrders.length == 0)
			{
				response = "No order found for " + user;
			}
			
			StringBuffer sb = new StringBuffer();
			
			for(OrderTable.Order order : userOrders)
			{
				sb.append(order.getOrderId()+ " " + order.getProductName() + " " + order.getQuantity() + "\n");
			}
			
			response = sb.toString();
			
		}
		
		else if(tag.contentEquals("list"))
		{
			System.out.println("List is processing..");
			
			ArrayList<Inventory.Product> inventory = inv.list();
			
			StringBuffer sb = new StringBuffer();
			
			for(Inventory.Product product : inventory)
			{
				sb.append(product.getName() + " "+ product.getQuantity());
			}
			response = sb.toString();
		}
		
		return response;
		
	}
	
	private void sendClientResponse(String response, PrintWriter pOut)
	{
		pOut.println(response);
	}
	
	private void processServerRequest(Command com, PrintWriter pOut)
	{
		Server.enqueue(com);
		pOut.println("Command accepted");
	}
	
	private void sendServerRequest(Command com)
	{
		String request = com.serverId + " " + com.timeStamp + " " + com.command;
		
		for(String[] server : Server.servers)
		{
			try {
				Socket clientSocket = new Socket(server[0],Integer.parseInt(server[1]));
				PrintWriter pOut = new PrintWriter(clientSocket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				pOut.println(request);
				pOut.flush();
				
				//TODO We need to set a time out rate for this line
				in.readLine();
				
				in.close();
				clientSocket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchElementException e){
				return;
			}
		}
	}
	
	private void notifyServer()
	{
		for(String[] server : Server.servers)
		{
			try {
				Socket clientSocket = new Socket(server[0],Integer.parseInt(server[1]));
				PrintWriter pOut = new PrintWriter(clientSocket.getOutputStream());

				
				pOut.println("dequeue");
				pOut.flush();
				
				//TODO We need to set a time out rate for this line
				
				clientSocket.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchElementException e){
				return;
			}
		}
	}
}
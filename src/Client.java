import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;

public class Client {

	private static class Server {
		String hostAddress;
		int port;

		public Server(String hostAddress, int port) {
			this.hostAddress = hostAddress;
			this.port = port;
		}
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int numServer = sc.nextInt();

		LinkedList<Client.Server> serverQueue = new LinkedList<Client.Server>();

		for (int i = 0; i < numServer; i++) {
			// TODO: parse inputs to get the ips and ports of servers
			String[] tokens = sc.next().split(":");
			Client.Server server = new Client.Server(tokens[0], Integer.parseInt(tokens[1]));
			serverQueue.add(server);
		}

		while (sc.hasNextLine()) {
			String cmd = sc.nextLine();
			System.out.println(cmd);
			String[] tokens = cmd.split(" ");

			String[] approvedTokens = { "purchase", "cancel", "search", "list" };

			if (Arrays.asList(approvedTokens).contains(tokens[0])) {
				TCPrequest(serverQueue, cmd, tokens[0]);
			} else {
				System.out.println("ERROR: No such command");
			}
		}
	}

	private static void TCPrequest(LinkedList<Server> serverQueue, String cmd, String token) {
		int len = serverQueue.size();
		for (int i = 0; i < len; i++) {
			try {
				Client.Server currentServer = serverQueue.get(i);
				System.out.println("host: "+currentServer.hostAddress+" , port: "+currentServer.port);
				Socket clientSocket = new Socket(currentServer.hostAddress, currentServer.port);
				clientSocket.setSoTimeout(50000);
				PrintWriter pOut = new PrintWriter(clientSocket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				pOut.println(cmd);
				pOut.flush();

		
				String response = in.readLine();
				
				if(token.equals("list"))
				{
					Scanner s = new Scanner(response);
					while(s.hasNext())
					{
						System.out.print(s.next() + " ");
						System.out.println(s.next());
					}
				}
				else if(token.equals("search"))
				{
					Scanner s = new Scanner(response);
					while(s.hasNext())
					{
						System.out.print(s.next() + " ");
						System.out.print(s.next() + " ");
						System.out.println(s.next());
					}
				}
				else
				{
					System.out.println(response);
				}

				in.close();
				clientSocket.close();
				break;
			} catch (InterruptedIOException iioe) {
				System.out.println("Server timed out!!");
				
			} catch (java.net.ConnectException e){
				continue;
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NoSuchElementException e) {
				return;
			} 
		}

	}
}


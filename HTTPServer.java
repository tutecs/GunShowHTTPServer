// The html server
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HTTPServer
{
	private static int port = 8080;
	public static void main(String[] args)
	{
		try
		{
			ServerSocket listenSocket = new ServerSocket(port);
			while(true)
			{
				Socket connectionSocket = listenSocket.accept();
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				String request = inFromClient.readLine();
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				String[] gucciGangParts = request.split(" ");
				System.out.printf("Received request: %s From: %s\n", request, connectionSocket.getInetAddress().getHostName());
				if(gucciGangParts.length == 3)
				{
					if(gucciGangParts[0].equals("GET") && gucciGangParts[1].equals("/"))
					{
						String message = getPage("index.html");
						System.out.printf("Sending %s to \n%s\n", "index.html", connectionSocket.getInetAddress().getHostName());
						outToClient.writeBytes(message);
					}
					if(gucciGangParts[0].equals("POST") && gucciGangParts[1].equals("/submit.html"))
					{
						boolean noBlank = true;
						String s = null;
						int contentLength = 0;
						while(noBlank)
						{
							s = inFromClient.readLine();
							System.out.println(s);
							if(s.toLowerCase().contains("content-length"))
								contentLength = Integer.valueOf(s.split(" ")[1]);
							
							if(s.equals(""))
								noBlank = false;
						}
						char[] issue = new char[contentLength];
						inFromClient.read(issue, 0, contentLength);
						String issueString = new String(issue);
						String message = issueString.substring(6);
						// write message to issue file.
						// possible format:
						// id:date:host:resolved:issue:solution
					}
				}
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}	
	}
	private static storeIssue(String host, String issue)
	{
		
	}
	private static String getPage(String webPage)
	{
		String body = getHTML(webPage);
		String header = "HTTP/1.0 200 OK\n Server: GucciGang/1.0 Java/9.0.0\n";
		SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
		Date current_time = new Date();
		String date = formatter.format(current_time).toString();
		int bodyLength = body.getBytes().length;
		header = header + date + "\nContent-type: text/html; charset=utf-8\nContent-Length: ";
		int headerLength = header.getBytes().length;
		header = header + String.valueOf(bodyLength);
		String headerBody = header + "\n\n" + body;
		return headerBody;
	}
	private static String getHTML(String filename)
	{
		try
		{
			String body = new String(Files.readAllBytes(Paths.get(filename)));
			return body;
		}
		catch (FileNotFoundException ex)
		{
			System.out.printf("Unable to open file: %s", filename);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return "THIS WAS NOT PART OF GOD'S PLAN";
	}
}

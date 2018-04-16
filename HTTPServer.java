// The html server
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;

public class HTTPServer
{
	private static int port = 8080;
	private static int currentId = getCurrentId();
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
						String s = "";
						String host = "";
						int contentLength = 0;
						while(noBlank)
						{
							s = inFromClient.readLine();
							System.out.println(s);
							if(s.toLowerCase().contains("content-length"))
								contentLength = Integer.valueOf(s.split(" ")[1]);
							if(s.toLowerCase().contains("host"))
								host = s.split(" ")[1];
							if(s.equals(""))
								noBlank = false;
						}
						char[] issue = new char[contentLength];
						inFromClient.read(issue, 0, contentLength);
						for(int i = 0; i < issue.length; i++)
						{
							if(issue[i] == '+')
								issue[i] = ' ';
							if(issue[i] == '%')
							{
								String hex = "" + issue[i+1] + issue[i+2];
								char newChar = (char) (Integer.parseInt(hex, 16));
								issue[i] = newChar;
								issue[i+1] = Character.MIN_VALUE;
								issue[i+2] = Character.MIN_VALUE;
								i += 2;
							}
						}
						String issueString = String.valueOf(issue);
						String message = issueString.substring(6);
						System.out.println(message);
						if(message.equals(""))
						{
							message = refreshPage("index.html");
							System.out.printf("Sending %s to \n%s\n", "index.html", connectionSocket.getInetAddress().getHostName());
							outToClient.writeBytes(message);
						}
						else
						{
							storeIssue(host,message);
							updateId();
							message = refreshPage("index.html");
							System.out.println(message);
							System.out.printf("Sending %s to \n%s\n", "index.html", connectionSocket.getInetAddress().getHostName());
							outToClient.writeBytes(message);
						}
					}
				}
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}	
	}

	private static void storeIssue(String host, String issue)
	{
		try
		{
			// format id::date::host::issue::resolved::solution
			FileWriter writer = new FileWriter("issues.txt", true);
			PrintWriter printer = new PrintWriter(writer);
			SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
			Date current_time = new Date();
			String date = formatter.format(current_time).toString();
			String id = String.valueOf(currentId++);
			String resolved = "false";
			String solution = "";
			printer.printf("%s::%s::%s::%s::%s::%s\n", id, date, host, issue, resolved, solution);
			printer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private static String refreshPage(String webPage)
	{
		String header = "HTTP/1.1 303 See Other\n";
		header = header + "Location: /\n\n\n";
		System.out.println(header);
		return header;
	}
	private static String getPage(String webPage)
	{
		try
		{
			String body = getHTML(webPage);
			Scanner sc = new Scanner(new File("issues.txt"));
			ArrayList<String> issues = new ArrayList<String>();
			while(sc.hasNextLine())
			{
				issues.add(sc.nextLine());
			}
			StringBuffer bodyBuff = new StringBuffer(body);
			
			int comment = bodyBuff.indexOf("<!-- Oh hi mark -->") - 1;
			String input;
			for(int i = 0; i < issues.size(); i++)
			{
				System.out.println(issues.get(i));
				String[] split = issues.get(i).split("::");
				System.out.println(split.length);
				input = "<tr>";
				bodyBuff.insert(comment+1, input);
				comment += input.length();
				for(int j = 0; j < split.length; j++)
				{
					if(j == 10)
					{
						// input = "<td>"+getResolvedForm(split[j])+"</td>";
					}
					else
					{
						input = "<td><p>"+split[j]+"</p></td>";
					}
					bodyBuff.insert(comment+1, input);	 
					comment += input.length();
				}
				input = "</tr>";
				bodyBuff.insert(comment+1,input);
				comment += input.length();
			}
			body = bodyBuff.toString();
			
			
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
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	// private static String getResolvedForm(String currentVal)
	// {
	// 	String html = "<form action= action='/submit_resolved.html' method='post'>"
    // 						+ "<select name='resolved' id='resolved' onchange='this.form.submit()'>";
	// 	String 
    //     					+	"<option value='solved'>Solved</option>"
    //     					+	"<option value='unsolved'>UnSolved</option>"
    // 						+ "</select>"
	// 					+ "</form>";
	// 	return html;
	// }
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
	private static int getCurrentId()
	{
		String id = getHTML("id.txt");
		id = id.trim();
		int currentId = Integer.valueOf(id);
		return currentId;
	}
	private static void updateId()
	{
		try
		{
			PrintWriter	writer = new PrintWriter("id.txt");
			writer.printf("%d", currentId);
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}

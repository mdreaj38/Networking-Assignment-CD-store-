import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.Date;
import java.util.StringTokenizer;

public class store{
    public static void main(String[] args)  throws Exception{
        ServerSocket serverSocket = null;
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader br_socket = null; //Socket
        BufferedReader br_file = null; // file reader
        File file =new File("db_store.txt");
        br_file = new BufferedReader(new FileReader(file));

        int store_port=Integer.parseInt(args[0]);// convert to integer
        int bank_port=Integer.parseInt(args[2]);//convert to integer
        String bank_host_ip=args[1];

        String stock="";
        String t="";
        while((t=br_file.readLine())!=null) {
            stock+=t+'-';//store info read and save to stock
        }
        br_file.close();
        int lenlen=0;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            serverSocket = new ServerSocket(store_port);
        } catch (Exception e) {
            e.printStackTrace();
        }
            System.out.println("Server Started....");
            try{
                socket = serverSocket.accept();
                System.out.println("Server accepted...");
                br_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String temp="",s="";// s=request_message
                while ((temp=br_socket.readLine())!=null && temp.length()!=0){
                    s+=temp;
                }
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                if(s.toUpperCase().startsWith("GET")) {
                    String _send = "";
                    /*
                    forming the html format to show in browser
                    */
                    _send +="<!DOCTYPE html>"+"\n"+"<html>"+"\n"+"<body>";
                    _send += "<table align='center' style='width:50%;margin-top:10%' border='1px'>"+'\n';
                    _send +=  "<tr style='background-color:gray'>"+'\n'+"<th>Item No</th><th>Item Name</th><th>Price</th></tr>";

                    String[] bla =stock.split("-");
                    for(String i: bla) {
                        String[] x =i.split("\\s+"); //split based on  SPACE
                        _send += "<tr align='center'>"+'\n';
                        _send += "<td>"+x[0]+"</td>"+'\n';
                        _send += "<td>"+x[1]+"</td>"+'\n';
                        _send += "<td>"+x[3]+"</td>"+'\n';
                        _send += "</tr>"+'\n';
                    }
                    _send += "</table>"+'\n';
                    file = new File("index.html");
                    br_file = new BufferedReader(new FileReader(file));// file reader

                    while ((s = br_file.readLine()) != null) {
                        _send += s + '\n'; // load index.html and sent to browser
                    }
                    _send+= "</body>" +'\n'+"</html>";
                    //next 5 lines are header
                    printWriter.println("HTTP/1.1 200 OK");
                    printWriter.println("Connection : keep-alive");
                    printWriter.println("Date: " + new Date().toString());
                    printWriter.println("Server: Custom Java HTTP Server");
                    printWriter.println("Content-Length: " +(int)_send.length());
                    printWriter.println();
                    printWriter.flush();
                    printWriter.println(_send); //sent message
                    printWriter.flush();
                }
            }catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    String _form = "", s;
                    while ((s = br_socket.readLine()) != null) {// reading the post method
                        if (s.startsWith("Content-Length:")) {
                            lenlen = Integer.parseInt(s.substring(16, s.length()));
                        }
                        if (s.length() == 0) break;
                        _form += s;
                    }
                    char cc;
                    s = "";
                    for (int i = 0; i < lenlen; i++) {
                        cc = (char) br_socket.read();
                        s = s + cc;
                    }
                    String[] extract = s.split("\\W+");/*Extracting data based on any non-word character*/
                    String Tosend = extract[0];
                    try {
                        System.out.println(extract[0]+" <> "+extract[1]);
                        Tosend = extract[1] + ' ' ;
                        Tosend += extract[3] + ' ' + extract[5] + ' ' + extract[7] + ' ' + extract[9] + ' ' + extract[11];
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Socket bsoc = new Socket(bank_host_ip,bank_port);
                    BufferedReader Bin = new BufferedReader(new InputStreamReader(bsoc.getInputStream()));
                    PrintWriter Bout = new PrintWriter(bsoc.getOutputStream());
                    Bout.println(Tosend); // sent to the bank server
                    Bout.flush();
                    String verdict = Bin.readLine();// receive verdict from the bank server

                    //forming header
                    printWriter.println("HTTP/1.1 200 OK");
                    printWriter.println("Connection : keep-alive");
                    printWriter.println("Date: " + new Date().toString());
                    printWriter.println("Server: Custom Java HTTP Server");
                    // forming html
                    String ss="<!DOCTYPE html>"+"\n"+"<html>"+"\n"+"<body>"+'\n';
                    ss += "<div style='background-color:red;margin-top:20%'>"+'\n';
                    ss += "<h1 align='center'>"+'\n';
                    ss += verdict+"</h1>";
                    ss += "</div>"+'\n';
                    ss += "</body>"+'\n';
                    ss += "</html>"+'\n';
                    printWriter.println("Content-Length: " +(int)ss.length());
                    printWriter.println();
                    printWriter.println(ss); //sent to the browser
                    printWriter.flush();
                    printWriter.close();
                    socket.close();
                    br_file.close();
                    br_socket.close();
                    System.out.println("closed");
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
    }
}
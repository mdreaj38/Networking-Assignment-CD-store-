import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.Date;
import java.util.StringTokenizer;

public class bank{
    public static void main(String[] args) {
        int port=0;
        port = Integer.parseInt(args[0]);
        try{
            ServerSocket serverConnect = new ServerSocket(port);
            while (true)
            {
                System.out.println("Started.............");
                Socket connect = serverConnect.accept();
                System.out.println("accepted.........");

                BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                PrintWriter out = new PrintWriter(connect.getOutputStream(),true);
                BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream());

                String info_from_store = in.readLine();//receive data from store.java
                String[] extract = info_from_store.split("\\s+");//regular expression to split data based on space
                String f_name = "",l_name="",p_code="",item_no = "",cred_num = "";
                try{
                    f_name = extract[0];
                    l_name = extract[1];
                    p_code = extract[2];
                    item_no = extract[4];
                    cred_num=extract[5];
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                int quantity=Integer.parseInt(extract[3]);
                BufferedReader br_file = null; // file reader
                File file = new File("database.txt");
                br_file = new BufferedReader(new FileReader(file));// file reader
                String temp="";
                String [] check;
                int flag=0,current_amount=0;
                while ((temp=br_file.readLine())!=null)
                {
                    check=temp.split("\\s+");/*split based on space */
                    if(check[0].equals(f_name) && check[1].equals(l_name) && check[2].equals(p_code) && check[3].equals(cred_num))//check validity of user
                    {
                        flag=1;
                        current_amount = Integer.parseInt(check[5]); // convert to integer
                        break;
                    }
                }
                String verdict="";
                if(flag==1) {
                    file=new File("db_store.txt");
                    br_file = new BufferedReader(new FileReader(file));// file reader
                    temp="";
                    int flag2=0,item_price=0;
                    while ((temp=br_file.readLine())!=null) {
                        check=temp.split("\\s+");/*split based on space */
                        if(check[0].equals(item_no))
                        {
                            item_price=Integer.parseInt(check[3]);
                            flag2=1;
                        }
                    }
                    if(flag2==1){
                        if(item_price*quantity<=current_amount) {
                           // System.out.println("Transaction Successful");
                            verdict="Transaction Successful";
                            file = new File("database.txt");
                            br_file = new BufferedReader(new FileReader(file));// file reader
                            String save_to_write="";
                            temp="";

                            while((temp=br_file.readLine())!=null){
                                check=temp.split("\\s+");
                                if(check[0].equals(f_name) && check[1].equals(l_name) && check[2].equals(p_code) && check[3].equals(cred_num)){
                                    int t = Integer.parseInt(check[5]);
                                    t = t-item_price*quantity;
                                    temp=check[0]+" "+check[1]+" "+check[2]+" "+check[3]+" "+check[4]+" "+t;//update the credit of user
                                }
                                temp=temp+'\n';
                                save_to_write+=temp;
                            }
                            FileWriter fw = new FileWriter(file);
                            fw.write(save_to_write);//updated data writen
                            fw.close();
                            br_file.close();
                        }
                        else {
                            verdict = "Your account does not have sufficient credit for the requested transaction";
                        }
                    }
                    else{
                        verdict = "Item doesn't belong to this store";
                    }
                }
                else {
                    verdict = "The user information entered is invalid.";
                }
                out.println(verdict);
                out.flush();
                br_file.close();
                connect.close();
                in.close();
                out.close();
                dataOut.close();
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
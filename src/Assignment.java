import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Assignment
{

    private static String FilePath;

    Assignment(String path) {
        FilePath = path;
    }

    Assignment(){
        FilePath = "E://assignment_test.txt";
    }


    public static void main(String[] args) {
        int choice,check,size;
        String key;
        Scanner sc = new Scanner(System.in);
        System.out.println("Specify the path of data store.For default enter def.");
        String path = sc.next();
        if(path.equals("def"))
        {
            Assignment a = new Assignment();
        }
        else
        {
            Assignment a = new Assignment(path);
        }
        while(true)
        {
            System.out.println("1.Create");
            System.out.println("2.Read");
            System.out.println("3.Delete");
            System.out.println("4.Exit");
            System.out.println("Enter your option:");
            choice = sc.nextInt();
            switch(choice)
            {
                case 1: check = FileSize();
                    size=0;
                    if(check==0)
                    {
                        System.out.println("Enter the key to add to data store:");
                        key = sc.next();
                        if(key.length()<=32)
                        {
                            check = KeyExist(key);
                            if(check==1)
                            {
                                JSONObject obj_check = new JSONObject();
                                System.out.println("Enter number of elements you want to enter in value");
                                int n = sc.nextInt();
                                for(int i=0;i<n;i++)
                                {
                                    System.out.println("Enter the key & value:");
                                    String k = sc.next();
                                    String value = sc.next();
                                    obj_check.put(k,value);
                                    size += k.length() + value.length();
                                }
                                if(size<=(1024*16)) //Json object capped at 16KB
                                {
                                    System.out.println("Do you want to set  Time-To-Live property?(Y/N)");
                                    char ch = sc.next().charAt(0);
                                    long ttl=-1; //ttl=-1 for no time-to-live property
                                    Date date = new Date();
                                    long time = date.getTime();
                                    if((ch=='y')||(ch=='Y'))
                                    {
                                        System.out.println("Enter Time-To-Live:");
                                        ttl = sc.nextLong();
                                        time += (ttl * 1000);
                                    }
                                    Timestamp ts = new Timestamp(time);
                                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
                                    create(key,obj_check,timestamp,ttl);
                                }
                            }
                            else
                                System.err.println("Key already exist in data store");
                        }
                        else
                            System.err.println("Size of the key is greater than 32 characters");
                    }
                    else
                        System.err.println("Size of the file is exceeding 1GB");
                    break;
                case 2:System.out.println("Enter the key to read :");
                    key = sc.next();
                    check = KeyExist(key);
                    if(check==0)
                        read(key);
                    else
                        System.err.println("Key does not exist in data store");
                    break;
                case 3:System.out.println("Enter the key to delete:");
                    key = sc.next();
                    check = KeyExist(key);
                    if(check==0) {
                        delete(key);
                        System.out.println("Key deleted");
                    }
                    else
                        System.err.println("Key does not exist in data store");
                    break;
                case 4:System.out.println("Exit");
                    System.exit(0);
                    break;
                default: System.out.println("Invalid choice");
                    break;
            }
        }
    }

    //Existed key in data store
    public static int KeyExist(String key)
    {
        File data = new File(FilePath);
        String s;
        int f=1;
        try(BufferedReader reader = new BufferedReader(new FileReader(data)))
        {
            Date date = new Date();
            long time = date.getTime();
            while((s=reader.readLine())!=null)
            {
                JSONObject obj =  (JSONObject) new JSONParser().parse(s);
                String check_key = (String) obj.get("key");
                String timestamp = (String) obj.get("time");
                long ttl = (long) obj.get("ttl");
                Timestamp t = Timestamp.valueOf(timestamp);
                long time_check = t.getTime();
                if((key.equals(check_key))&&(time>time_check)&&(ttl==-1))
                {
                    f=0;
                    break;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return f;
    }

    //Size of the file
    public static int FileSize()
    {
        int f=1;
        File data = new File(FilePath);
        try(BufferedReader reader = new BufferedReader(new FileReader(data)))
        {
            if(data.length()<(1024*1024*1024))//1 GB=1024*1024*1024 bytes
                f=0;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return f;
    }

    //Create operation
    public static void create(String key,JSONObject obj_check,String timestamp,long ttl)
    {
        File data = new File(FilePath);
        try(FileWriter writer = new FileWriter(data,true);
            BufferedReader reader = new BufferedReader(new FileReader(data));)
        {
            JSONObject obj = new JSONObject();
            obj.put("key", key);
            obj.put("value", obj_check);
            obj.put("time", timestamp);
            obj.put("ttl", ttl);
            writer.write(obj.toString());
            writer.flush();
            writer.write("\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    //Read operation
    public static void read(String key)
    {
        File data = new File(FilePath);
        String s;
        try(BufferedReader reader = new BufferedReader(new FileReader(data));)
        {
            while((s=reader.readLine())!=null)
            {
                JSONObject obj =  (JSONObject) new JSONParser().parse(s);
                String read_key = (String) obj.get("key");
                if(key.equals(read_key))
                {
                    JSONObject value = (JSONObject) obj.get("value");
                    System.out.println("Key: " + key +"\nValue: " + value);
                    break;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    //Delete operation
    public static void delete(String key)
    {
        File data = new File(FilePath);
        File temp = new File("temp.txt");
        String s;
        try(BufferedReader reader = new BufferedReader(new FileReader(data));
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp)))
        {
            while((s=reader.readLine())!=null)
            {
                JSONObject obj =  (JSONObject) new JSONParser().parse(s);
                String delete_key = (String) obj.get("key");
                if(!key.equals(delete_key))
                {
                    writer.write(s);
                    writer.write("\n");
                }
            }
            writer.close();
            reader.close();
            data.delete();
            temp.renameTo(data);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}


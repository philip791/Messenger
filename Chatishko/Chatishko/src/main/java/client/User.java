package client;
import java.net.*;
import java.io.*;
import java.util.*;

public class User
{
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private UserInterface cg;

    private String server, username;
    private int port;

    User(String server, int port, String username)
    {
        this(server, port, username, null);
    }

    User(String server, int port, String username, UserInterface cg)
    {
        this.server = server;
        this.port = port;
        this.username = username;
        this.cg = cg;
    }

    public boolean start()
    {
        try
        {
            socket = new Socket(server, port);
        }
        catch(Exception ec)
        {
            display("Ошибка подключения к серверу:" + ec);
            return false;
        }

        String msg = "Подключено " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO)
        {
            display("Ошибка создания потоков ввода вывода: " + eIO);
            return false;
        }

        new ListenFromServer().start();
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO)
        {
            display("Ошибка входа: " + eIO);
            disconnect();
            return false;
        }
        return true;
    }

    //кидаем сообщение в консоль если есть интерфейс - кидаем туда
    private void display(String msg) {
        if(cg == null)
            System.out.println(msg);
        else
            cg.append(msg + "\n");
    }
    //отправляем сообщение
    void sendMessage(Message msg)
    {
        try
        {
            sOutput.writeObject(msg);
        }
        catch(IOException e)
        {
            display("Ошибка записи на сервер: " + e);
        }
    }
 //пользователь отключается
    private void disconnect()
    {
        try
        {
            if(sInput != null) sInput.close();
        }
        catch(Exception e)
        {}
        try
        {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e)
        {}
        try
        {
            if(socket != null) socket.close();
        }
        catch(Exception e)
        {}

        if(cg != null)
            cg.connectionFailed();

    }

    public static void main(String[] args)
    {
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Аноним";
        System.out.println(args.length);

        switch(args.length)
        {
            case 3:
                serverAddress = args[2];
            case 2:
                try
                {
                    portNumber = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    System.out.println("Не верный номер порта.");
                    System.out.println("Правильный ввод: > java User [username] [portNumber] [serverAddress]");
                    return;
                }
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Правильный ввод: > java User [username] [portNumber] {serverAddress]");
                return;
        }
        User User = new User(serverAddress, portNumber, userName);
        if(!User.start())
            return;

        Scanner scan = new Scanner(System.in);
        while(true)
        {
            System.out.print("> ");
            String msg = scan.nextLine();
            if(msg.equalsIgnoreCase("EXIT"))
            {
                User.sendMessage(new Message(Message.EXIT, ""));
                break;
            }
            else if(msg.equalsIgnoreCase("ONLINE"))
            {
                User.sendMessage(new Message(Message.ONLINE, ""));
            }
            else
            {
                User.sendMessage(new Message(Message.MESSAGE, msg));
            }
        }
        User.disconnect();
    }

    //прослушивание действий из сервера
    class ListenFromServer extends Thread
    {

        public void run()
        {
            while(true)
            {
                try
                {
                    String msg = (String) sInput.readObject();
                    if(cg == null)
                    {
                        System.out.println(msg);
                        System.out.print("> ");
                    }
                    else
                    {
                        cg.append(msg);
                    }
                }
                catch(IOException e)
                {
                    display("Сервер закрыл соединение: " + e);
                    if(cg != null)
                        cg.connectionFailed();
                    break;
                }
                catch(ClassNotFoundException e2)
                {
                }
            }
        }
    }
}


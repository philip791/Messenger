package server;
import client.Message;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server
{
    //уникальный айди для подключенного пользователя
    private static int uniqueId;
    //список всех подключенный пользователей
    private ArrayList<UserThread> al;

    //private ServerInterface sg;
    private SimpleDateFormat sdf;
    private int port;
    private boolean keepGoing;

    //public Server(int port)
   // {
     //   this(port);
   // }

    //задаем значение порта и готовим поле для принятия пользователей
    public Server(int port)
    {
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        al = new ArrayList<UserThread>();
    }

    //стартуем сервер, задав значение переменной которая будет отвечать за продолжение работы
    public void start()
    {
        keepGoing = true;
        try
        {
            //задаем сервер сокет порт на который пользователь сможет подключится и связаться с сервером
            ServerSocket serverSocket = new ServerSocket(port);
            //пока работаем в бесконечном режиме, ожидая людей - добавляем в массив пользователей и запускаем их работу
            //получив пользователя - создаем сокет соединение для клиентов, который и будет связываться с сервером
            //прерываем работу сервера - если переменная отвечающая за работу примет значение false при закрытии сервера.
            while(keepGoing)
            {
                display("Сервер ожидает пользователей. Порт: " + port + ".");
                Socket socket = serverSocket.accept();
                if(!keepGoing)
                    break;
                UserThread t = new UserThread(socket);
                al.add(t);
                t.start();
            }
            try
            {
                //закрываем серверсокетное соединие
                serverSocket.close();
                //проходимся по всем клиентам и закрываем их соединия: поток ввода, поток вывода, сокет
                for(int i = 0; i < al.size(); ++i)
                {
                    UserThread tc = al.get(i);
                    try
                    {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE)
                    {
                    }
                }
            }
            catch(Exception e)
            {
                display("Ошибка закрытия сервера и клиента: " + e);
            }
        }
        catch (IOException e)
        {
            String msg = sdf.format(new Date()) + " Ошибка при создании нового сервер сокета: " + e + "\n";
            display(msg);
        }
    }

    //остановка сервера
    protected void stop()
    {
        keepGoing = false;
        try
        {
            new Socket("localhost", port);
        }
        catch(Exception e)
        {
        }
    }

    //выводим в консоль сервера сообщения
    private void display(String msg)
    {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);
    }


    //броадкаст для сервера, постоянное отслеживание прихода сообщений от клиентов и отображение в консоль сервера
    private synchronized void broadcast(String message)
    {
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        System.out.print(messageLf);

        for(int i = al.size(); --i >= 0;)
        {
            UserThread ct = al.get(i);
            if(!ct.writeMsg(messageLf))
            {
                al.remove(i);
                display("Отключенный пользователь: " + ct.username + " удалён из списка.");
            }
        }
    }

    //удаляем человека из списка людей онлайн
    synchronized void remove(int id)
    {
        for(int i = 0; i < al.size(); ++i)
        {
            UserThread ct = al.get(i);
            if(ct.id == id)
            {
                al.remove(i);
                return;
            }
        }
    }

    public static void main(String[] args)
    {
        int portNumber = 1500;
        switch(args.length)
        {
            case 1:
                try
                {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e)
                {
                    System.out.println("Не верный номер порта");
                    System.out.println("Правильный ввод: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Правильный ввод: > java Server [portNumber]");
                return;

        }
        Server server = new Server(portNumber);
        //старт сервека
        server.start();
    }

    //класс человека, реализация многопоточности
    class UserThread extends Thread
    {
        //сокет, потоки ввода вывода
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        Message cm;
        String date;

        UserThread(Socket socket)
        {
            id = ++uniqueId;
            this.socket = socket;
            System.out.println("Попытка создать потоки ввода и вывода");
            try
            {
                //подключаем человека к чату
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                display(username + " подключился.");
            }
            catch (IOException e)
            {
                display("Ошибка создания потоков ввода вывода: " + e);
                return;
            }
            catch (ClassNotFoundException e)
            {
            }
            date = new Date().toString() + "\n";
        }

        //запуск просмотра человека на отправку сообщения
        public void run() {
            boolean keepGoing = true;
            while(keepGoing)
            {
                try
                {
                    //считывание сообщения через поток ввода
                    cm = (Message) sInput.readObject();
                }
                catch (IOException e)
                {
                    display(username + " Ошибка чтения потоков: " + e);
                    break;
                }
                catch(ClassNotFoundException e2)
                {
                    break;
                }
                String message = cm.getMessage();

                //определяем тип сообщения
                switch(cm.getType())
                {
                    //сообщение обычное - кидаем в историю чата в консоль сервера
                    //сообщение о выходе - кикаем юзера с чата
                    //сообщение о онлайне - выводим список авторизованных пользователей
                    case Message.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case Message.EXIT:
                        display(username + " отключился.");
                        keepGoing = false;
                        break;
                    case Message.ONLINE:
                        writeMsg("Список подключенных пользователей: " + sdf.format(new Date()) + "\n");
                        for(int i = 0; i < al.size(); ++i)
                        {
                            UserThread ct = al.get(i);
                            writeMsg((i+1) + ") " + ct.username + " с " + ct.date);
                        }
                        break;
                }
            }
            //в конце работы удаяем пользователя из ОНЛАЙН списака и закрываем соединение
            remove(id);
            close();
        }

        private void close()
        {
            try
            {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e)
            {}
            try
            {
                if(sInput != null) sInput.close();
            }
            catch(Exception e)
            {};
            try
            {
                if(socket != null) socket.close();
            }
            catch (Exception e)
            {}
        }

        //когда пользователь отправляет сообщение кидаем в консоль сервера - историю чата либо закрываем соедиение с
        //клиентом, если он соедиение сервера упало
        private boolean writeMsg(String msg)
        {
            if(!socket.isConnected())
            {
                close();
                return false;
            }
            try
            {
                sOutput.writeObject(msg);
            }
            catch(IOException e)
            {
                display("Ошибка отправления сообщения " + username);
                display(e.toString());
            }
            return true;
        }
    }
}



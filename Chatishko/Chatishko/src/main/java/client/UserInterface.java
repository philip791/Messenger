package client;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class UserInterface extends JFrame implements ActionListener {
    private static final long serialVersionUID = -2465929623630458639L;
    private String defaultHost;
    private JTextArea ta;
    private JTextField tfServer;
    private JTextField tfPort;
    private JLabel label;
    private JButton online;
    private JTextField tf;
    private boolean connected;
    private JButton exit;
    private User User;
    private int defaultPort;
    private JButton login;

    UserInterface(String host, int port) {
        super("Чат Морозова Филиппа Сергеевича Проект на КПП");
        this.defaultPort = port;
        this.defaultHost = host;

        //верхняя - северная часть для лэйаута окна
        JPanel northPanel = new JPanel(new GridLayout(3, 1));
        JPanel serverAndPort = new JPanel(new GridLayout(1, 5));
        this.tfServer = new JTextField(host);
        //прикол пустой строки для того чтобы в поле записалось "строчное" выражение порта
        this.tfPort = new JTextField(""+port);
        serverAndPort.add(new JLabel("Адрес сервера:  "));
        serverAndPort.add(this.tfServer);
        serverAndPort.add(new JLabel("Номер порта:  "));
        serverAndPort.add(this.tfPort);
        northPanel.add(serverAndPort);
        this.label = new JLabel("Введите ниже имя пользователя", 0);
        northPanel.add(this.label);
        this.tf = new JTextField("Морозко");
        this.tf.setBackground(Color.WHITE);
        northPanel.add(this.tf);
        this.add(northPanel, "North");
        this.ta = new JTextArea("Добро пожаловать в чат\n", 80, 50);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(this.ta));
        this.ta.setEditable(false);
        this.add(centerPanel, "Center");
        this.login = new JButton("Вход");
        this.login.addActionListener(this);
        this.exit = new JButton("Выход");
        this.exit.addActionListener(this);
        this.exit.setEnabled(false);
        this.online = new JButton("Онлайн");
        this.online.addActionListener(this);
        this.online.setEnabled(false);
        JPanel southPanel = new JPanel();
        southPanel.add(this.login);
        southPanel.add(this.exit);
        southPanel.add(this.online);
        this.add(southPanel, "South");
        this.setDefaultCloseOperation(3);
        this.setSize(600, 600);
        this.setVisible(true);
        this.tf.requestFocus();
    }

    //добавление на текстарею
    void append(String str) {
        this.ta.append(str);
        this.ta.setCaretPosition(this.ta.getText().length() - 1);
    }

    //при выходе из чата или других ошибках возвращаемся к начальному виду приложения
    void connectionFailed() {
        this.login.setEnabled(true);
        this.exit.setEnabled(false);
        this.online.setEnabled(false);
        this.label.setText("Имя пользователя внизу");
        this.tf.setText("Морозко");
        this.tfPort.setText("" + this.defaultPort);
        this.tfServer.setText(this.defaultHost);
        this.tfServer.setEditable(true);
        this.tfPort.setEditable(true);
        this.tf.removeActionListener(this);
        this.connected = false;
    }


    //отслеживание событий на окне
    public void actionPerformed(ActionEvent e) {
        //источник события
        Object o = e.getSource();
        if (o == this.exit) {
            this.User.sendMessage(new Message(2, ""));
        } else if (o == this.online) {
            this.User.sendMessage(new Message(0, ""));
        } else if (this.connected) {
            this.User.sendMessage(new Message(1, this.tf.getText()));
            this.tf.setText("");
        } else {
            if (o == this.login) {
                String username = this.tf.getText().trim();
                if (username.length() == 0) {
                    return;
                }

                String server = this.tfServer.getText().trim();
                if (server.length() == 0) {
                    return;
                }

                String portNumber = this.tfPort.getText().trim();
                if (portNumber.length() == 0) {
                    return;
                }

                boolean var6 = false;

                int port;
                try {
                    port = Integer.parseInt(portNumber);
                } catch (Exception var8) {
                    return;
                }

                this.User = new User(server, port, username, this);
                if (!this.User.start()) {
                    return;
                }

                this.tf.setText("");
                this.label.setText("Пробейте сообщение внизу");
                this.connected = true;
                this.login.setEnabled(false);
                this.exit.setEnabled(true);
                this.online.setEnabled(true);
                this.tfServer.setEditable(false);
                this.tfPort.setEditable(false);
                this.tf.addActionListener(this);
            }

        }
    }

    public static void main(String[] args) {
        new UserInterface("localhost", 1500);
    }
}


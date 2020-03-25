/* Alexandros Florides */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Client {

    String serverAddress;
    Scanner reader;
    PrintWriter writer;

    JFrame frame = new JFrame("Player");
    JPanel panel = new JPanel(new GridBagLayout());
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);

    JButton whoisin = new JButton("whoisin");
    JButton ball = new JButton("ball");
    JButton pass = new JButton("pass");
    JLabel passlabel = new JLabel("Pass to player:");
    JTextField passtextfield = new JTextField(10);
    JButton commands = new JButton("commands");
    JButton logout = new JButton("logout");


    public Client(String serverAddress) {
        this.serverAddress = serverAddress;

        // All of GUI's components
        textField.setText("Enter command here:");
        Font font1 = new Font("SansSerif", Font.BOLD, 20);
        textField.setFont(font1);
        messageArea.setFont(font1);
        whoisin.setFont(font1);
        ball.setFont(font1);
        pass.setFont(font1);
        passlabel.setFont(font1);
        passtextfield.setFont(font1);
        commands.setFont(font1);
        logout.setFont(font1);

        DefaultCaret caret = (DefaultCaret) messageArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(whoisin, c);

        c.gridx = 1;
        panel.add(ball, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(passlabel, c);

        c.gridx = 1;
        panel.add(passtextfield, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(pass, c);

        c.gridx = 1;
        c.gridy = 2;
        panel.add(commands, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        panel.add(logout, c);

        //ActionListener for each button and textfield
        whoisin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writer.println("WHOISIN");
            }
        });

        ball.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writer.println("BALL");
            }
        });

        pass.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writer.println("PASS " + passtextfield.getText());
            }
        });

        commands.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writer.println("COMMANDS");
            }
        });

        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writer.println("LOGOUT");
            }
        });

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.WEST);
        frame.add(panel);
        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writer.println(textField.getText());
                textField.setText("Enter command here:");
            }
        });

        textField.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField.setText("");
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {
                textField.setText("Enter command here:");
            }
        });
    }
    private void run() throws IOException {
        try {
            //Create new socket to communicate with the server
            Socket socket = new Socket(serverAddress, 8888);
            reader = new Scanner(socket.getInputStream());
            writer = new PrintWriter(socket.getOutputStream(), true);

            //Infinite loop to get user's input and receive data
            while (true) {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Player - " + line.substring(13));
                    textField.setEditable(true);
                    passtextfield.setEditable(true);
                    } else if (line.startsWith("MESSAGE")) {
                        messageArea.append(line.substring(8) + "\n");
                        System.out.println(line.substring(8));
                    } else if (line.startsWith("PASS")) {
                        messageArea.append(line.substring(5) + "\n");
                        System.out.println(line.substring(5));
                    } else if (line.startsWith("WHOISIN")) {
                        messageArea.append("Players online:" + "\n");
                        messageArea.append("    " + line.substring(8) + "\n");
                        System.out.println("Players online:");
                        System.out.println("    " + line.substring(8));
                    } else if (line.startsWith("COMMANDS")) {
                        messageArea.append(line.substring(9) + "\n");
                        System.out.println(line.substring(9));
                    } else if (line.startsWith("LOGOUT")) {
                        socket.close();
                        frame.setVisible(false);
                        frame.dispose();
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        return;
                    }
                }

            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            Client client = new Client("localhost");
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setVisible(true);
            client.run();
        }
    }
}

/* Alexandros Florides */
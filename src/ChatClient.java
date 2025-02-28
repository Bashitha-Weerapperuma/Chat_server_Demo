import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField textField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(10, 40);
    private JList<String> clientsList = new JList<>(new DefaultListModel<>());
    private JCheckBox broadcastCheckbox = new JCheckBox("Broadcast");
    private DefaultListModel<String> clientsModel = (DefaultListModel<String>) clientsList.getModel();
    private boolean isBroadcast = true;

    public ChatClient() {
        frame.setLayout(new BorderLayout());
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Message Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        textField.setEditable(false);
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(broadcastCheckbox, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Chat Display Panel
        messageArea.setEditable(false);
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Client List Panel
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBorder(BorderFactory.createTitledBorder("Connected Users"));
        clientPanel.add(new JScrollPane(clientsList), BorderLayout.CENTER);
        frame.add(clientPanel, BorderLayout.EAST);

        // Action Listeners
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });

        broadcastCheckbox.addActionListener(e -> isBroadcast = broadcastCheckbox.isSelected());
    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter Server IP Address:",
                "Server Connection",
                JOptionPane.QUESTION_MESSAGE);
    }

    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter Your Screen Name:",
                "User Registration",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            String line = in.readLine();
            if (line == null) continue;

            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("CHECK")) {
                out.println(isBroadcast);
            } else if (line.startsWith("CLIENTS")) {
                out.println(clientsList.getSelectedValue());
            } else if (line.startsWith("REMOVE")) {
                clientsModel.removeElement(line.substring(7));
            } else {
                clientsModel.addElement(line);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setVisible(true);
        client.run();
    }
}

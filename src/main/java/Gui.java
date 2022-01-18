import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class Gui {
    private server server;
    private JFrame frame;

    public Gui(server server){
        JFrame frame = new JFrame();

        this.server = server;
        this.frame = frame;

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Secure Indexes");
        frame.setPreferredSize(new Dimension(550, 300));
        frame.setLocation(600, 400);
        frame.setVisible(true);

        loginScreen();
    }

    private void loginScreen(){

        JPanel mainLoginPnl = new JPanel();
        mainLoginPnl.setLayout(new BoxLayout(mainLoginPnl, BoxLayout.Y_AXIS));


        JPanel loginPnl = new JPanel(new GridBagLayout());
        loginPnl.setBounds(0,0,600, 300);
        //loginPnl.setBackground(Color.RED);
        loginPnl.setBorder(BorderFactory.createEmptyBorder(30, 30, 0, 30));


        JPanel loginTxtPnl = new JPanel(new BorderLayout());
        loginTxtPnl.setBounds(0,300, 600, 100);
        loginTxtPnl.setBorder(BorderFactory.createEmptyBorder(30, 30, 0, 30));
        //loginTxtPnl.setBackground(Color.BLUE);

        GridBagConstraints c = new GridBagConstraints();

        JLabel nameLbl = new JLabel("Username: ");
        c.gridx = 0;
        c.gridy = 0;
        loginPnl.add(nameLbl, c);

        JTextField nameFld = new JTextField();
        nameFld.setPreferredSize(new Dimension(150,25));
        c.gridx = 1;
        c.gridy = 0;
        loginPnl.add(nameFld, c);

        JLabel passwordLbl = new JLabel("Password: ");
        c.gridx = 0;
        c.gridy = 1;
        loginPnl.add(passwordLbl, c);

        JPasswordField passwordFld = new JPasswordField();
        passwordFld.setPreferredSize(new Dimension(150,25));
        c.gridx = 1;
        c.gridy = 1;
        loginPnl.add(passwordFld, c);

        JLabel outputLbl = new JLabel();
        outputLbl.setVerticalAlignment(JLabel.TOP);
        loginTxtPnl.add(outputLbl);

        JButton loginBtn = new JButton("Log in");
        loginBtn.addActionListener(e -> {
            if(nameFld.getText().isBlank() || passwordFld.getPassword().length < 8){
                outputLbl.setText("Username missing or password is too short (minimum 8 characters)");
                return;
            }
            System.out.println(passwordFld.getPassword());
            frame.remove(mainLoginPnl);
            frame.repaint();
            mainScreen(new client(nameFld.getText(), new String(passwordFld.getPassword()), server.getS(), server.getR()));
        });

        c.gridx = 2;
        c.gridy = 0;
        loginPnl.add(loginBtn, c);

        mainLoginPnl.add(loginPnl);
        mainLoginPnl.add(loginTxtPnl);

        frame.add(mainLoginPnl, BorderLayout.WEST);
        frame.pack();
    }

    private void mainScreen(client client) {
        JPanel mainPnl = new JPanel(new GridBagLayout());
        GridBagConstraints mp = new GridBagConstraints();

        JPanel firstSectionPnl = new JPanel(new GridBagLayout());
        GridBagConstraints fsp = new GridBagConstraints();

        JLabel searchLbl = new JLabel("Search: ");
        fsp.gridx = 0;
        fsp.gridy = 0;
        firstSectionPnl.add(searchLbl, fsp);

        JTextField searchFld = new JTextField();
        searchFld.setPreferredSize(new Dimension(150,25));
        fsp.gridx = 1;
        fsp.gridy = 0;
        firstSectionPnl.add(searchFld, fsp);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> {
            if(searchFld.getText().isBlank()){
                return;
            }
            client.search(server, searchFld.getText());
        });
        fsp.gridx = 2;
        fsp.gridy = 0;
        firstSectionPnl.add(searchBtn, fsp);

        JLabel dateLbl = new JLabel("Search by date: ");
        fsp.gridx = 0;
        fsp.gridy = 1;
        firstSectionPnl.add(dateLbl, fsp);

        JPanel secondSectionPnl = new JPanel();
        JLabel uploadTextLbl = new JLabel("Or upload file(s)");
        secondSectionPnl.add(uploadTextLbl);

        JFileChooser fileChooser = new JFileChooser(new File("./src/main/resources/allImages/"));
        fileChooser.setMultiSelectionEnabled(true);

        JButton uploadBtn = new JButton("Choose file(s)");
        uploadBtn.addActionListener(e -> {
            int returnValue = fileChooser.showOpenDialog(null);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                Arrays.asList(files).forEach(x -> {
                    if (x.isFile()) {
                        client.upload(server,x);
                    }
                });
            }
        });
        secondSectionPnl.add(uploadBtn);

        mp.gridx = 0;
        mp.gridy = 0;
        mainPnl.add(firstSectionPnl, mp);

        mp.gridx = 0;
        mp.gridy = 1;
        mainPnl.add(secondSectionPnl,mp);

        frame.add(mainPnl, BorderLayout.CENTER);
        frame.pack();

    }
}

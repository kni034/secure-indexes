package Client;

import Server.authenticator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.FutureTask;

public class Gui{
    private final authenticator auth;
    private final JFrame frame;

    public Gui(authenticator auth){
        JFrame frame = new JFrame();

        this.auth = auth;
        this.frame = frame;

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Secure Indexes");
        //frame.setPreferredSize(new Dimension(550, 300));
        frame.setLocation(600, 400);
        frame.setVisible(true);

        loginScreen();
    }

    private void loginScreen(){

        JPanel mainLoginPnl = new JPanel();
        mainLoginPnl.setLayout(new BoxLayout(mainLoginPnl, BoxLayout.Y_AXIS));

        JLabel welcomeTextLbl = new JLabel("Log in to your account");
        welcomeTextLbl.setBorder(BorderFactory.createEmptyBorder(30,30,0,30));
        welcomeTextLbl.setFont(new Font(welcomeTextLbl.getFont().getName(), Font.PLAIN, 16));
        mainLoginPnl.add(welcomeTextLbl);


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
        outputLbl.setBorder(BorderFactory.createEmptyBorder(0,30,30,30));
        loginTxtPnl.add(outputLbl);

        JButton loginBtn = new JButton("Log in");
        loginBtn.addActionListener(e -> {
            if(nameFld.getText().isBlank() || passwordFld.getPassword().length < 8){
                outputLbl.setText("Username missing or password is too short (minimum 8 characters)");
                frame.pack();
                return;
            }
            client client = new client(nameFld.getText(), new String(passwordFld.getPassword()), auth, auth.getS(), auth.getR());
            if(client.connect()){
                frame.remove(mainLoginPnl);
                frame.repaint();
                mainScreen(client);
            }
            else {
                outputLbl.setText("Wrong username or password");
                frame.pack();
                return;
            }

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
        JPanel mainPnl = new JPanel();
        mainPnl.setLayout(new BoxLayout(mainPnl, BoxLayout.Y_AXIS));

        System.out.println("You are logged in as: " + client.getUid());

        JPanel firstSectionPnl = new JPanel();
        firstSectionPnl.setLayout(new BoxLayout(firstSectionPnl, BoxLayout.X_AXIS));
        firstSectionPnl.setBorder(BorderFactory.createEmptyBorder(30,30,0,30));

        JLabel searchLbl = new JLabel("Search: ");
        firstSectionPnl.add(searchLbl);

        JTextField searchFld = new JTextField();


        firstSectionPnl.add(searchFld);

        JButton searchBtn = new JButton("Search by word");
        searchBtn.addActionListener(e -> {
            if(searchFld.getText().isBlank()){
                return;
            }
            client.search(searchFld.getText());
        });

        firstSectionPnl.add(searchBtn);

        JPanel secondSectionPnl = new JPanel();
        secondSectionPnl.setLayout(new BoxLayout(secondSectionPnl, BoxLayout.X_AXIS));
        secondSectionPnl.setBorder(BorderFactory.createEmptyBorder(30,30,0,30));

        JLabel dateLbl = new JLabel("Search by date: ");
        secondSectionPnl.add(dateLbl);

        JTextField fromDateFld = new JTextField("DD:MM:YYYY");
        secondSectionPnl.add(fromDateFld);

        JLabel toDateLbl = new JLabel(" to date: ");
        toDateLbl.setVisible(false);
        secondSectionPnl.add(toDateLbl);

        JTextField toDateFld = new JTextField("DD:MM:YYYY");
        toDateFld.setVisible(false);
        secondSectionPnl.add(toDateFld);

        JPanel thirdSectionPnl = new JPanel();
        thirdSectionPnl.setLayout(new BoxLayout(thirdSectionPnl, BoxLayout.X_AXIS));
        JLabel searchRangeLbl = new JLabel("Search in a range of dates: ");
        thirdSectionPnl.add(searchRangeLbl);

        JCheckBox rangeChb = new JCheckBox();
        rangeChb.addItemListener(e -> {
            if(e.getStateChange() == 1) {
                toDateFld.setVisible(true);
                toDateLbl.setVisible(true);
                frame.pack();
            }
            else{
                toDateFld.setVisible(false);
                toDateLbl.setVisible(false);
            }
        });
        thirdSectionPnl.add(rangeChb);

        JButton searchDateBtn = new JButton("Search by date");
        searchDateBtn.addActionListener(e -> {
            boolean firstFieldValid = !(fromDateFld.getText().equals("DD:MM:YYYY") || fromDateFld.getText().isBlank());
            boolean secondFieldValid = !(toDateFld.getText().equals("DD:MM:YYYY") || toDateFld.getText().isBlank());

            try {
            //first date field is not empty or unchanged, and checkbox is not selected
            if(firstFieldValid && !rangeChb.isSelected()){
                String[] from = fromDateFld.getText().split(":",-1);

                String fromDay = from[0].equals("") ? "" : "d" + from[0];
                String fromMonth = from[1].equals("") ? "" : "m" + from[1];
                String fromYear = from[2].equals("") ? "" : "y" + from[2];

                String fromString = fromDay + fromMonth + fromYear;

                System.out.println(fromString);

                client.search(fromString);
                return;
            }
            //first and second date field is not empty or unchanged and checkbox is selected
            else if(rangeChb.isSelected() && firstFieldValid && secondFieldValid){
                    client.searchRange(fromDateFld.getText(), toDateFld.getText());
                    return;
                }

            }catch (Exception ex){
                System.out.println("Write date(s) on the correct format and try again");
                return;
            }
            System.out.println("Write date(s) on the correct format and try again");
        });
        thirdSectionPnl.add(searchDateBtn);


        JPanel fourthSectionPnl = new JPanel();
        fourthSectionPnl.setLayout(new BoxLayout(fourthSectionPnl, BoxLayout.X_AXIS));
        fourthSectionPnl.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));

        JLabel uploadTextLbl = new JLabel("Or upload file(s): ");
        fourthSectionPnl.add(uploadTextLbl);

        JFileChooser fileChooser = new JFileChooser(new File("./src/main/resources/allImages/"));
        fileChooser.setMultiSelectionEnabled(true);

        JCheckBox extraWordsChb = new JCheckBox("Add extra words");


        JPanel fifthSectionPnl = new JPanel();
        fifthSectionPnl.setLayout(new BoxLayout(fifthSectionPnl, BoxLayout.X_AXIS));
        fifthSectionPnl.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));

        JLabel extraWordsLbl = new JLabel();
        extraWordsLbl.setVisible(false);
        fifthSectionPnl.add(extraWordsLbl);

        JTextField extraWordsFld = new JTextField();
        extraWordsFld.setVisible(false);
        fifthSectionPnl.add(extraWordsFld);

        JButton extraWordsBtn = new JButton("Add");
        extraWordsBtn.setVisible(false);

        fifthSectionPnl.add(extraWordsBtn);


        JButton uploadBtn = new JButton("Choose file(s)");

        ArrayList<File> extraWordFiles = new ArrayList<>();

        uploadBtn.addActionListener(e -> {
            int returnValue = fileChooser.showOpenDialog(null);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();

                Arrays.asList(files).forEach(x -> {
                    if (x.isFile() && !extraWordsChb.isSelected()) {
                        client.upload(x, new String[0]);
                    }
                    else if(x.isFile()){
                        extraWordsLbl.setVisible(true);
                        extraWordsLbl.setText("Extra words for "+x.getName() + ": ");
                        extraWordsFld.setVisible(true);
                        extraWordsBtn.setVisible(true);

                        extraWordFiles.add(x);

                    }
                });
                System.out.println("Uploaded " + files.length + " files");
            }
        });
        fourthSectionPnl.add(uploadBtn);

        extraWordsChb.addActionListener(e -> {

            if(!extraWordsChb.isSelected() && extraWordFiles.isEmpty()){
                extraWordsLbl.setVisible(false);
                extraWordsFld.setVisible(false);
                extraWordsBtn.setVisible(false);
            }

        });

        fourthSectionPnl.add(extraWordsChb);

        extraWordsBtn.addActionListener(e -> {

            if(extraWordFiles.isEmpty()){
                System.out.println("Choose file(s) first");
                return;
            }

            String[] words = extraWordsFld.getText().split(",");
            File f = extraWordFiles.remove(extraWordFiles.size() -1);

            client.upload(f, words);

            if(extraWordFiles.isEmpty()){
                extraWordsChb.setSelected(false);

                extraWordsLbl.setVisible(false);
                extraWordsFld.setVisible(false);
                extraWordsBtn.setVisible(false);
                return;
            }

            extraWordsLbl.setVisible(true);
            extraWordsLbl.setText("Extra words for "+extraWordFiles.get(extraWordFiles.size()-1).getName() + ": ");
            extraWordsFld.setVisible(true);
            extraWordsBtn.setVisible(true);

        });


        JPanel sixthSectionPnl = new JPanel();
        sixthSectionPnl.setLayout(new BoxLayout(sixthSectionPnl, BoxLayout.X_AXIS));
        sixthSectionPnl.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));



        JButton helpBtn = new JButton("Help");
        helpBtn.addActionListener(e -> {
            System.out.println("------------------------------------------------ Help ------------------------------------------------");
            System.out.println("Search for keywords in your stored pictures and videos!");
            System.out.println("Most files have keywords for the place name the file was created, the date, name of the file and objects in pictures.");
            System.out.println("Examples of keywords are: marineholmenp-sone, thormøhlensgate, møhlenpris, bergenhus, bergen, vestland, 5058, norway, y2017, m09, d25, monday, d25m09y2017, \n" +
                    "m09y2017, d25m09, høst, autumn, pond.jpg, pond, Water, Plant, Sky, Water resources, Plant community, Leaf, Natural landscape, Natural environment, Branch, Lacustrine plain");
            System.out.println("You can also search for files created between 2 dates");
            System.out.println("The date format is DD:MM:YYYY, and you can exclude DD and MM if you want");
            System.out.println("Example of a search for all videos/pictures taken between may 2020 and february 2021: ':5:2020' - ':2:2021'");
            System.out.println("Downloaded files are stored in your personal folder in the clientFiles folder");
            System.out.println("You upload files by pressing the upload button (choose multiple files by holding Ctrl while selecting)");
            System.out.println("------------------------------------------------ Help ------------------------------------------------");

        });
        sixthSectionPnl.add(helpBtn);

        JButton logoutBtn = new JButton("Log out");
        logoutBtn.addActionListener(e -> {
            frame.remove(mainPnl);
            frame.repaint();
            loginScreen();
        });
        sixthSectionPnl.add(logoutBtn);

        mainPnl.add(firstSectionPnl);
        mainPnl.add(secondSectionPnl);
        mainPnl.add(thirdSectionPnl);
        mainPnl.add(fourthSectionPnl);
        mainPnl.add(fifthSectionPnl);
        mainPnl.add(sixthSectionPnl);

        frame.add(mainPnl, BorderLayout.WEST);
        frame.pack();

    }
}

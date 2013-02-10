/*
計算機プログラミング最終課題

IMAPのメールサーバーに一定時間ごとにアクセスして新着メールの有無を確かめるプログラム
～～GUI部分～～
*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MailCheckGUI extends JFrame implements ActionListener {
    public static MailCheck mc;
    public static MailCheckGUI mgui;

    MainPanel mp;

    JMenuItem pref;
    JMenuItem quit;

    MailCheckGUI () {
        super("IMAP Mail Check");
        mp = new MainPanel();

        //メニューをつくる
        JMenuBar mbar = new JMenuBar();
        JMenu mcommand = new JMenu("File");
        pref = new JMenuItem("Preferences");
        pref.addActionListener(this);
        quit = new JMenuItem("Quit");
        quit.addActionListener(this);
        mcommand.add(pref);
        mcommand.addSeparator();
        mcommand.add(quit);
        mbar.add(mcommand);
        setJMenuBar(mbar);

        addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent we) {
            //ウインドウを閉じたら終了させる
                System.exit(0);
            }
        });
        getContentPane().add(mp);
    }

    public static void main (String args[]) {
        mgui = new MailCheckGUI();
        mgui.setSize(100, 150);
        mgui.show();
        mc = new MailCheck("", "", "", 143, 10);
    }

    public void actionPerformed (ActionEvent ae) {
        if (ae.getSource() == quit) {
        //メニューでQuitが押されたとき
            System.exit(0);
        } else if (ae.getSource() == pref) {
        //Preferrencesが押されたとき
            SubWindow sw = new SubWindow();
            //すでに一度設定されているときはその設定を表示する
            if (mc.Server != "") {
                sw.txtServer.setText(mc.Server);
            }            
            if (mc.UserName != "") {
                sw.txtUserName.setText(mc.UserName);
            }
            if (Integer.toString(mc.Port) != "" && mc.Port != 0) {
                sw.txtPort.setText(Integer.toString(mc.Port));
            }
            if (Integer.toString(mc.Interval) != "" && mc.Interval != 0) {
                sw.txtInterval.setText(Integer.toString(mc.Interval));
            }
            sw.show();
        }
    }

class MainPanel extends JPanel {
    //新着があるときのアイコン
    Image mail1;
    //ないときのアイコン
    Image mail2;
    //現在のアイコン
    Image CurrentImage;

    int All;
    int Recent;

    MainPanel () {
        setBackground(Color.white);
        Toolkit tk = Toolkit.getDefaultToolkit();
        mail1 = tk.getImage("MAIL1.GIF");
        mail2 = tk.getImage("MAIL2.GIF");
        //はじめは新着がないことにする
        CurrentImage = mail2;
    }

    public void ChangeGraphics (int n, int AllMsg, int RecentMsg) {
        if (n == 1) {
        //新着があるとき
            CurrentImage = mail1;
        } else if (n == 0)  {
        //ないとき
            CurrentImage = mail2;
        }
        All = AllMsg;
        Recent = RecentMsg;
        setTitle(Integer.toString(RecentMsg) + "/" + Integer.toString(AllMsg));
        paintComponent(getGraphics());
    }
    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        g.drawImage(CurrentImage, 0, 0, this);
        g.drawString("All:" + All + " Recent:" + Recent, 5, 95);
    }
}//MainPanelの終わり

class SubWindow extends JFrame implements ActionListener {
    JButton btnOK;
    JButton btnCancel;
    JTextField txtServer;
    JTextField txtPort;
    JTextField txtUserName;
    JPasswordField txtPassword;
    JTextField txtInterval;

    SubWindow () {
        setSize(300, 200);
        setTitle("メールサーバーの設定");

        //サーバーとポートの設定
        JPanel p1 = new JPanel();
        JLabel lbl1 = new JLabel("Server : ");
        txtServer = new JTextField("mail.ecc.u-tokyo.ac.jp");
        JLabel lbl2 = new JLabel("Port : ");
        txtPort = new JTextField("143");
        p1.add(lbl1);
        p1.add(txtServer);
        p1.add(lbl2);
        p1.add(txtPort);

        //ユーザー名の設定
        JPanel p2 = new JPanel();
        JLabel lbl3 = new JLabel("UserName : ");
        txtUserName = new JTextField(10);
        p2.add(lbl3);
        p2.add(txtUserName);

        //パスワードの設定
        JPanel p3 = new JPanel();
        JLabel lbl4 = new JLabel("Password : ");
        txtPassword = new JPasswordField(16);
        p3.add(lbl4);
        p3.add(txtPassword);

        //チェックする間隔の設定
        JPanel p4 = new JPanel();
        JLabel lbl5 = new JLabel("Interval(min) : ");
        txtInterval = new JTextField("5", 2);
        p4.add(lbl5);
        p4.add(txtInterval);

        JPanel p5 = new JPanel();
        btnOK = new JButton("OK");
        btnOK.addActionListener(this);
        p5.add(btnOK);
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        p5.add(btnCancel);

        getContentPane().setLayout(new GridLayout(5, 1));
        getContentPane().add(p1);
        getContentPane().add(p2);
        getContentPane().add(p3);
        getContentPane().add(p4);
        getContentPane().add(p5);
    }
    public void actionPerformed (ActionEvent ae) {
        if (ae.getSource() == btnOK) {
        //OKボタンが押されたときは設定を保存してウインドウを閉じる
            mc.setOption(txtServer.getText(), txtUserName.getText(), new String(txtPassword.getPassword()), Integer.parseInt(txtPort.getText()), Integer.parseInt(txtInterval.getText()));
            dispose();
        } else if (ae.getSource() == btnCancel) {
        //Cancelボタンの時は設定を反映せずに閉じる
            dispose();
        }
    }
}//SubWindowの終わり
}//MailCheckGUIの終わり

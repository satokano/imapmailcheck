/*
�v�Z�@�v���O���~���O�ŏI�ۑ�

IMAP�̃��[���T�[�o�[�Ɉ�莞�Ԃ��ƂɃA�N�Z�X���ĐV�����[���̗L�����m���߂�v���O����
�`�`GUI�����`�`
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

        //���j���[������
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
            //�E�C���h�E�������I��������
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
        //���j���[��Quit�������ꂽ�Ƃ�
            System.exit(0);
        } else if (ae.getSource() == pref) {
        //Preferrences�������ꂽ�Ƃ�
            SubWindow sw = new SubWindow();
            //���łɈ�x�ݒ肳��Ă���Ƃ��͂��̐ݒ��\������
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
    //�V��������Ƃ��̃A�C�R��
    Image mail1;
    //�Ȃ��Ƃ��̃A�C�R��
    Image mail2;
    //���݂̃A�C�R��
    Image CurrentImage;

    int All;
    int Recent;

    MainPanel () {
        setBackground(Color.white);
        Toolkit tk = Toolkit.getDefaultToolkit();
        mail1 = tk.getImage("MAIL1.GIF");
        mail2 = tk.getImage("MAIL2.GIF");
        //�͂��߂͐V�����Ȃ����Ƃɂ���
        CurrentImage = mail2;
    }

    public void ChangeGraphics (int n, int AllMsg, int RecentMsg) {
        if (n == 1) {
        //�V��������Ƃ�
            CurrentImage = mail1;
        } else if (n == 0)  {
        //�Ȃ��Ƃ�
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
}//MainPanel�̏I���

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
        setTitle("���[���T�[�o�[�̐ݒ�");

        //�T�[�o�[�ƃ|�[�g�̐ݒ�
        JPanel p1 = new JPanel();
        JLabel lbl1 = new JLabel("Server : ");
        txtServer = new JTextField("mail.ecc.u-tokyo.ac.jp");
        JLabel lbl2 = new JLabel("Port : ");
        txtPort = new JTextField("143");
        p1.add(lbl1);
        p1.add(txtServer);
        p1.add(lbl2);
        p1.add(txtPort);

        //���[�U�[���̐ݒ�
        JPanel p2 = new JPanel();
        JLabel lbl3 = new JLabel("UserName : ");
        txtUserName = new JTextField(10);
        p2.add(lbl3);
        p2.add(txtUserName);

        //�p�X���[�h�̐ݒ�
        JPanel p3 = new JPanel();
        JLabel lbl4 = new JLabel("Password : ");
        txtPassword = new JPasswordField(16);
        p3.add(lbl4);
        p3.add(txtPassword);

        //�`�F�b�N����Ԋu�̐ݒ�
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
        //OK�{�^���������ꂽ�Ƃ��͐ݒ��ۑ����ăE�C���h�E�����
            mc.setOption(txtServer.getText(), txtUserName.getText(), new String(txtPassword.getPassword()), Integer.parseInt(txtPort.getText()), Integer.parseInt(txtInterval.getText()));
            dispose();
        } else if (ae.getSource() == btnCancel) {
        //Cancel�{�^���̎��͐ݒ�𔽉f�����ɕ���
            dispose();
        }
    }
}//SubWindow�̏I���
}//MailCheckGUI�̏I���

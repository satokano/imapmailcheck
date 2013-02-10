/*
�׻����ץ���ߥ󥰺ǽ�����

IMAP�Υ᡼�륵���С��˰�����֤��Ȥ˥����������ƿ���᡼���̵ͭ��Τ����ץ����
�������å���ʬ����

*/


import java.net.*;
import java.io.*;

//�����С�������������ǡ�����ʬ�Ϥ���Ȥ��˻Ȥ�
import java.util.StringTokenizer;

//������֤��Ȥ˥����С��˥����󤷤ƥ����å�����Τ�Runnable��implement����
public class MailCheck implements Runnable {
    Socket sock;
    BufferedReader in;
    PrintWriter out;

    //��Ȥ��ɤ��ޤǿʤ�������������ֹ�
    int StatusNumber;

    //tag���ֹ档�����С��˥��ޥ�ɤ���������Ȥ�,ɬ����Ƭ��Tag��Ĥ��ʤ���Ф����ʤ�
    int TagNumber;

    //IMAP�����С�����������ʸ����
    String SendString = "";

    //public String UserName;
    String UserName;
    public String Password;
    public String Server;
    public int Port;
    public int Interval;

    //������֤��Ȥ˥����å����뤿��˥���åɤ���
    Thread th;

    MailCheck (String tempServer, String tempUserName, String tempPassword, int tempPort, int tempInterval) {
        //�ǽ�ξ��֤�0
        StatusNumber = 0;

        //�桼����̾�ȥѥ����
        UserName = tempUserName;
        Password = tempPassword;

        //��ؤΥ����С��˥�����������Ȥ����̤�mail.ecc.u-tokyo.ac.jp�Ȥ���Ф褤
        //�����������stone��ͳ�ǻȤ��Ȥ���localhost�ˤ���
        Server = tempServer;

        //imap��143��
        //�������Ȥ�stone��Ȥä���³����Ȥ���993��
        Port = tempPort;

        //���������å�����ֳ�
        Interval = tempInterval;

        //Interval�ǻ��ꤵ�줿�ֳ֤��Ȥ˥����å����뤿�᥹��åɤ�Ĥ���
        th = new Thread(this);
    }

    //������֤��Ȥ˿���᡼���̵ͭ��Τ���륵�֥롼����
    public void run () {
        while (true) {
            try {
                //Intervalʬ�������꡼��
                Thread.sleep(Interval * 60000L);
            } catch (InterruptedException ie) {
                System.out.println("��������å����InterruptedException");
            }
            try {
                //imap�����Ф��̿����뤿��Υ����å�
                sock = new Socket(Server, Port);
            } catch (UnknownHostException uhe) {
                System.out.println("�ۥ��Ȥ����Ĥ���ޤ���");
                System.exit(1);
            } catch (IOException ioe) {
                System.out.println("�����åȤ���Ȥ���IOException�Ǥ�");
                System.exit(1);
            }

            try {
                //�������������åȤ��������ѤΥ��ȥ꡼������
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                //�������������åȤ�������ѤΥ��ȥ꡼�������ʼ�ưŪ��flush�����
                out = new PrintWriter(sock.getOutputStream(), true);
            } catch (IOException ioe) {
                System.out.println("�����åȤ��饹�ȥ꡼�����Ȥ���IOException�Ǥ�");
                System.exit(1);
            }

            //IMAP�����С����������Ƥ���ǡ�����������륹��å�
            ListeningThread thInput = new ListeningThread(in);
            thInput.start();
        }
    }

    //���ߤξ��֤˱�����Ŭ�ڤʥ��ޥ�ɤ���������
    public void SendCommand () {
        switch (StatusNumber) {
        //�ޤ�������
        case 1:
            SendString = getTag() + " LOGIN " + UserName + " " + Password;
            StatusNumber++;
            break;
        //�᡼��ܥå�����INBOX��Read-only�����򤹤�
        case 3:
            SendString = getTag() + " EXAMINE \"INBOX\"";
            StatusNumber++;
            break;
        //����å��������ȿ����å����������������
        case 5:
            //RECENT��Ȥ���UNSEEN��Ȥ����¤ä�����UNSEEN�ˤ���
            SendString = getTag() + " STATUS \"INBOX\" (MESSAGES UNSEEN)";
            StatusNumber++;
            break;
        //�������Ȥ���
        case 7:
            SendString = getTag() + " LOGOUT";
            StatusNumber++;
            break;    
        }
        out.println(SendString);
        //�ǥХå�����Ȥ��Ϥ����Υ����Ȥ�Ϥ����ȥ����С��ȤΤ���꤬ɽ�������
        //System.out.println(SendString);
    }
        
    //tag��so�Τ��Ȥ˿�����Ĥ�����Ρ��Ȥ������Ȥˤ���
    private String getTag() {
        TagNumber++;
        return "so" + Integer.toString(TagNumber);
    }

    //�����С���桼����̾�ʤɤ����ꤹ��
    public void setOption (String tempServer, String tempUserName, String tempPassword, int tempPort, int tempInterval) {
        Server = tempServer;
        UserName = tempUserName;
        Password = tempPassword;
        Port = tempPort;
        Interval = tempInterval;
        if (th.isAlive() == false) {
            th.start();
        }
    }

//�����С�����Υǡ�����������륹��å�
class ListeningThread extends Thread {
    BufferedReader is;

    ListeningThread(BufferedReader is) {
        this.is = is;
    }

    public void run() {
        //���������ǡ��������Ƥ��ǧ����Ȥ��˻Ȥ�
        String FirstToken;
        String SecondToken;
        //MESSAGES�Ȥ���ʸ����ΰ��֤򵭲�����
        int index1;
        //UNSEEN�Ȥ���ʸ����ΰ��֤򵭲�����
        int index2;
        //�Ǹ�ˤǤƤ���)�ΰ��֤򵭲�����
        int index3;

        //����å��������ȿ����å����������Ǽ�����ѿ�
        int NumOfMsg;
        int NumOfRecent;

        try {
            while (true) {
                String message = is.readLine();
                if (message != null) {

                    //�ǥХå�����Ȥ��Ϥ����Υ����Ȥ�Ϥ����ȥ����С��ȤΤ���꤬ɽ�������
                    //System.out.println(message);

                    //���������ǡ�����Ⱦ�Ѷ���Ƕ��ڤ�
                    StringTokenizer st = new StringTokenizer(message, " ");


                    /*
                    �ʲ���switchʸ��Ǥϸ��ߤξ��֤˱�����Ŭ�ڤʼ��Υ��ޥ�ɤ��������Ƥ��롣
                    �����ǡ������С�����α����ˤ�äơ����ޥ�ɤ��������ä������Ԥ��ä���Ƚ�ꤷ�Ƥ��롣

                    ����ʤ顢IMAP�ε��ʤˤ������äơ��㤨��LOGIN�η�̤�Ƚ�ꤹ��ˤϡ�
                    �����С�����α�����
                        "����������������" "OK��NO��BAD�ʤ�" "������"
                    �ȤʤäƤ��뤳�Ȥ��ǧ���������ܤΥȡ�����OK�ˤʤäƤ������������Ƚ�ꤹ��٤��Ǥ��롣
                    �������������Ǥϡ���������˴ޤޤ�Ƥ���ȡ�������ڤ�Ф���
                    ���Υȡ������ʸ�����������Ƚ�ꤷ�Ƥ��롣

                    ������ȸ�̩��Ƚ��ϤǤ��ʤ������ʤ������������Ȥ�����
                    �㤨��case 0�λ���
                        st.nextToken() == "*"
                    �Ȥ�������ɾ���������Ȥ���true�ˤʤ餺��
                    false�ˤʤäƤ��ޤ��Ȥ������ݤ�����������Ǥ��롣
                        System.out.println(st.nextToken().getBytes());
                        System.out.println("*".getBytes());
                    �ʤɤ��Ƥߤơ���ʬ�ʤ�˸�����Ĵ�٤��Τ�������ɤ狼��ʤ��ä���
                    */

                    switch (StatusNumber) {
                    //��³����Ⱥǽ��* OK�ǤϤ��ޤ�ʸ���󤬤���
                    case 0:
                        if (st.nextToken().length() == 1) {
                            StatusNumber++;
                            SendCommand();
                        }
                        break;

                    //LOGIN�η�̤�����
                    //�����ʤ�
                    //so1 OK LOGIN completed
                    //���Ԥʤ�
                    //so1 NO Bad LOGIN user name and/or password
                    case 2:
                        if (st.nextToken().length() >= 3 && st.nextToken().length() == 2 && st.nextToken().length() == 5) {
                        //����������λ�
                            StatusNumber++;
                            SendCommand();
                        } else {
                        //���Ԥλ�
                            System.out.println(message);
                            System.exit(1);
                        }
                        break;

                    //EXAMINE�η�̤�����
                    case 4:
                        if (st.nextToken().length() >= 3) {
                            StatusNumber++;
                            SendCommand();
                        }
                        break;

                    //STATUS�η�̤�����
                    //���������¹Ԥ����Ϥ�
                    case 6:
                        FirstToken = st.nextToken();
                        //�����ܤϤ����ˤ�*1ʸ��������Ϥ�

                        SecondToken = st.nextToken();
                        //�����ܤϤ����ˤ�STATUS�Ȥ���6ʸ��������Ϥ�

                        if (FirstToken.length() != 1) {
                        //�����ܤϤ������¹Ԥ����Ϥ�
                            StatusNumber++;
                            SendCommand();
                        } else if (FirstToken.length() == 1 && SecondToken.length() == 6) {
                        //�����ܤϤ������¹Ԥ����Ϥ������������å��������ȿ����å����������������
                            index1 = message.indexOf("MESSAGES");
                            index2 = message.indexOf("UNSEEN");
                            index3 = message.lastIndexOf(")");
                            NumOfMsg = Integer.parseInt(message.substring(index1 + 9, index2 - 1));
                            NumOfRecent = Integer.parseInt(message.substring(index2 + 7, index3));
                            if (NumOfRecent >= 1) {
                                MailCheckGUI.mgui.mp.ChangeGraphics(1, NumOfMsg, NumOfRecent);
                            } else {
                                MailCheckGUI.mgui.mp.ChangeGraphics(0, NumOfMsg, NumOfRecent);
                            }
                        }
                        break;

                    //LOGOUT�η�̤�����
                    case 8:
                        if (st.nextToken().length() >= 3) {
                            //�����åȤ��Ĥ���
                            in.close();
                            out.close();
                            sock.close();
                        }
                        //�Ϥ���ξ��֤��ᤷ�Ƥ���
                        StatusNumber = 0;
                        stop();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("��������åɤ�IOException");
        }
    }
}//ListeningThread�ν����
}//MailCheck�ν����

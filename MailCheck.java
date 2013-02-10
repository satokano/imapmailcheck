/*
計算機プログラミング最終課題

IMAPのメールサーバーに一定時間ごとにアクセスして新着メールの有無を確かめるプログラム
〜〜ロジック部分〜〜

*/


import java.net.*;
import java.io.*;

//サーバーから受信したデータを分析するときに使う
import java.util.StringTokenizer;

//一定時間ごとにサーバーにログインしてチェックするのでRunnableをimplementする
public class MailCheck implements Runnable {
    Socket sock;
    BufferedReader in;
    PrintWriter out;

    //作業がどこまで進んだか記憶する番号
    int StatusNumber;

    //tagの番号。サーバーにコマンドを送信するとき,必ず先頭にTagをつけなければいけない
    int TagNumber;

    //IMAPサーバーに送信する文字列
    String SendString = "";

    //public String UserName;
    String UserName;
    public String Password;
    public String Server;
    public int Port;
    public int Interval;

    //一定時間ごとにチェックするためにスレッドを作る
    Thread th;

    MailCheck (String tempServer, String tempUserName, String tempPassword, int tempPort, int tempInterval) {
        //最初の状態は0
        StatusNumber = 0;

        //ユーザー名とパスワード
        UserName = tempUserName;
        Password = tempPassword;

        //大学のサーバーにアクセスするとき普通はmail.ecc.u-tokyo.ac.jpとすればよい
        //ただし自宅でstone経由で使うときはlocalhostにする
        Server = tempServer;

        //imapは143番
        //ただし家でstoneを使って接続するときは993番
        Port = tempPort;

        //新着をチェックする間隔
        Interval = tempInterval;

        //Intervalで指定された間隔ごとにチェックするためスレッドをつくる
        th = new Thread(this);
    }

    //一定時間ごとに新着メールの有無を確かめるサブルーチン
    public void run () {
        while (true) {
            try {
                //Interval分だけスリープ
                Thread.sleep(Interval * 60000L);
            } catch (InterruptedException ie) {
                System.out.println("新着チェック中のInterruptedException");
            }
            try {
                //imapサーバと通信するためのソケット
                sock = new Socket(Server, Port);
            } catch (UnknownHostException uhe) {
                System.out.println("ホストが見つかりません");
                System.exit(1);
            } catch (IOException ioe) {
                System.out.println("ソケットを作るときのIOExceptionです");
                System.exit(1);
            }

            try {
                //作成したソケットから入力用のストリームを作成
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                //作成したソケットから出力用のストリームを作成（自動的にflushする）
                out = new PrintWriter(sock.getOutputStream(), true);
            } catch (IOException ioe) {
                System.out.println("ソケットからストリームを作るときのIOExceptionです");
                System.exit(1);
            }

            //IMAPサーバーから送られてくるデータを受信するスレッド
            ListeningThread thInput = new ListeningThread(in);
            thInput.start();
        }
    }

    //現在の状態に応じて適切なコマンドを送信する
    public void SendCommand () {
        switch (StatusNumber) {
        //まずログイン
        case 1:
            SendString = getTag() + " LOGIN " + UserName + " " + Password;
            StatusNumber++;
            break;
        //メールボックスのINBOXをRead-onlyで選択する
        case 3:
            SendString = getTag() + " EXAMINE \"INBOX\"";
            StatusNumber++;
            break;
        //全メッセージ数と新着メッセージ数を取得する
        case 5:
            //RECENTを使うかUNSEENを使うか迷ったが、UNSEENにした
            SendString = getTag() + " STATUS \"INBOX\" (MESSAGES UNSEEN)";
            StatusNumber++;
            break;
        //ログアウトする
        case 7:
            SendString = getTag() + " LOGOUT";
            StatusNumber++;
            break;    
        }
        out.println(SendString);
        //デバッグするときはここのコメントをはずすとサーバーとのやり取りが表示される
        //System.out.println(SendString);
    }
        
    //tagはsoのあとに数字をつけたもの、ということにする
    private String getTag() {
        TagNumber++;
        return "so" + Integer.toString(TagNumber);
    }

    //サーバーやユーザー名などを設定する
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

//サーバーからのデータを受信するスレッド
class ListeningThread extends Thread {
    BufferedReader is;

    ListeningThread(BufferedReader is) {
        this.is = is;
    }

    public void run() {
        //受信したデータの内容を確認するときに使う
        String FirstToken;
        String SecondToken;
        //MESSAGESという文字列の位置を記憶する
        int index1;
        //UNSEENという文字列の位置を記憶する
        int index2;
        //最後にでてくる)の位置を記憶する
        int index3;

        //全メッセージ数と新着メッセージ数を格納する変数
        int NumOfMsg;
        int NumOfRecent;

        try {
            while (true) {
                String message = is.readLine();
                if (message != null) {

                    //デバッグするときはここのコメントをはずすとサーバーとのやり取りが表示される
                    //System.out.println(message);

                    //受信したデータを半角空白で区切る
                    StringTokenizer st = new StringTokenizer(message, " ");


                    /*
                    以下のswitch文中では現在の状態に応じて適切な次のコマンドを送信している。
                    ここで、サーバーからの応答によって、コマンドが成功だったか失敗だったか判定している。

                    本来なら、IMAPの規格にしたがって、例えばLOGINの結果を判定するには、
                    サーバーからの応答が
                        "前に送信したタグ" "OK、NO、BADなど" "コメント"
                    となっていることを確認し、二番目のトークンがOKになっていれば成功、と判定するべきである。
                    しかし、ここでは、応答の中に含まれているトークンを切り出し、
                    そのトークンの文字数を数えて判定している。

                    これだと厳密な判定はできないが、なぜこうしたかというと
                    例えばcase 0の時に
                        st.nextToken() == "*"
                    という式を評価させたときにtrueにならず、
                    falseになってしまうという現象が起きたからである。
                        System.out.println(st.nextToken().getBytes());
                        System.out.println("*".getBytes());
                    などを試してみて、自分なりに原因を調べたのだが、結局わからなかった。
                    */

                    switch (StatusNumber) {
                    //接続すると最初に* OKではじまる文字列がくる
                    case 0:
                        if (st.nextToken().length() == 1) {
                            StatusNumber++;
                            SendCommand();
                        }
                        break;

                    //LOGINの結果がくる
                    //成功なら
                    //so1 OK LOGIN completed
                    //失敗なら
                    //so1 NO Bad LOGIN user name and/or password
                    case 2:
                        if (st.nextToken().length() >= 3 && st.nextToken().length() == 2 && st.nextToken().length() == 5) {
                        //これは成功の時
                            StatusNumber++;
                            SendCommand();
                        } else {
                        //失敗の時
                            System.out.println(message);
                            System.exit(1);
                        }
                        break;

                    //EXAMINEの結果がくる
                    case 4:
                        if (st.nextToken().length() >= 3) {
                            StatusNumber++;
                            SendCommand();
                        }
                        break;

                    //STATUSの結果がくる
                    //ここは二回実行されるはず
                    case 6:
                        FirstToken = st.nextToken();
                        //一度目はここには*1文字が入るはず

                        SecondToken = st.nextToken();
                        //一度目はここにはSTATUSという6文字が入るはず

                        if (FirstToken.length() != 1) {
                        //二度目はここが実行されるはず
                            StatusNumber++;
                            SendCommand();
                        } else if (FirstToken.length() == 1 && SecondToken.length() == 6) {
                        //一度目はここが実行されるはず。ここで総メッセージ数と新着メッセージ数を取得する
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

                    //LOGOUTの結果がくる
                    case 8:
                        if (st.nextToken().length() >= 3) {
                            //ソケットを閉じる
                            in.close();
                            out.close();
                            sock.close();
                        }
                        //はじめの状態に戻しておく
                        StatusNumber = 0;
                        stop();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("受信スレッドのIOException");
        }
    }
}//ListeningThreadの終わり
}//MailCheckの終わり

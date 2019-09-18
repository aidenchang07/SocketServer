package com.example.socketserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/**
 * Created by AidenChang 2019/09/11
 */
public class TCPSocketActivity extends AppCompatActivity {

    private static final String TAG = "TEST";
    private static final int SERVER_PORT = 9700;
    private String currentIP;

    private Button sendBtn;
    private Button connectBtn;
    private Button startServerBtn;
    private Button closeServerBtn;
    private Button udpTestBtn;
    private EditText ipEt;
    private EditText textEt;
    private TextView ipTv;
    private Handler handler;

    private ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpsocket);

        init();

    }

    @Override
    protected void onDestroy() {
//        connectedThread.cancel();
        super.onDestroy();
    }

    private void init() {
        handler = new Handler();
        findView();
        setListener();
        getIp();
    }

    private void getIp() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        try {
            // Get ip address
            currentIP = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                    .getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ipTv.setText("Current IP: " + currentIP);
    }

    private void findView() {
        sendBtn = findViewById(R.id.btn_send_tcp);
        connectBtn = findViewById(R.id.btn_connect);
        textEt = findViewById(R.id.et_text);
        ipEt = findViewById(R.id.et_ip_tcp);
        ipTv = findViewById(R.id.tv_ip_tcp);
        startServerBtn = findViewById(R.id.btn_start_server_tcp);
        closeServerBtn = findViewById(R.id.btn_close_server_tcp);
        udpTestBtn = findViewById(R.id.btn_udp_test);
    }

    private void setListener() {
        sendBtn.setOnClickListener(new SendBtnOnClickListener());
        connectBtn.setOnClickListener(new ConnectBtnOnClickListener());
        startServerBtn.setOnClickListener(new StartServerBtnOnClickListener());
        closeServerBtn.setOnClickListener(new CloseServerBtnOnClickListener());
        udpTestBtn.setOnClickListener(new UDPTestBtnOnClickListener());
    }

    private void connected(Socket socket) {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    class ServerSocketThread extends Thread {

        ServerSocket serverSocket;
        Socket socket;
//        String message;
        //        PrintWriter printWriter;
//        DataInputStream dataInputStream;

        ServerSocketThread() {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
//                serverSocket.bind(new InetSocketAddress(SERVER_PORT));
            } catch (BindException e) {
                Toast.makeText(TCPSocketActivity.this, "端口已經被其他服務器進程佔用 !", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(TCPSocketActivity.this, "Could not listen on port: " + SERVER_PORT, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {

            Log.d(TAG, "伺服器已啟動 !");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TCPSocketActivity.this, "伺服器已啟動 !", Toast.LENGTH_SHORT).show();
                }
            });

            try {

                // Waiting for Client
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(TCPSocketActivity.this, "waiting for client", Toast.LENGTH_SHORT).show();
//                    }
//                });

                while (true) {
                    Log.d(TAG, "等待連線...");
                    socket = serverSocket.accept();
                    Log.d(TAG, "連線成功 !");
                    connected(socket);

//                    socket.getOutputStream().write(msg.getBytes());

//                    dataInputStream = new DataInputStream(socket.getInputStream());
//                    printWriter = new PrintWriter(String.valueOf(socket.getInputStream()));

//                    message = dataInputStream.readUTF();

                    // Show Message
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(TCPSocketActivity.this, "message received from client: " + message, Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }
            } catch (IOException e) {
                Log.d(TAG, "Socket連線有問題 !");
                e.printStackTrace();
            }
        }

    }

    class ClientSocketThread extends Thread {
        String ipAddress;

        ClientSocketThread(String ip) {

            this.ipAddress = ip;

//            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//            int ipInt = wifiInfo.getIpAddress();
//            try {
//                // Get ip address
//                ipAddress = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
//                        .getHostAddress();
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
//            Log.d(TAG, ipAddress);
//            Toast.makeText(TCPSocketActivity.this, "yoyo: " + ipAddress, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void run() {
            try {
                Socket clientSocket = new Socket(ipAddress, SERVER_PORT);
                connected(clientSocket);
                Log.d(TAG, "連接成功! 輸入 IP 為：" + ipAddress);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TCPSocketActivity.this, "連接成功! 輸入 IP 為：" + ipAddress, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                Log.d(TAG, "連接失敗! 輸入 IP 為：" + ipAddress);
                e.printStackTrace();
            }
        }
    }

    class ConnectedThread extends Thread {
        public volatile boolean exit = false;
        private Socket socket;
        private InputStream in;
        private OutputStream out;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private BufferedReader inputBufferedReader;
        private BufferedWriter outputBufferedWriter;
        private BufferedInputStream bufferedInputStream;

        private String readString;
        byte[] buffer = new byte[1024];
        int bytes;

        ConnectedThread(Socket socket) {
            this.socket = socket;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
//                dataInputStream = new DataInputStream(socket.getInputStream());
//                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                inputBufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(in)));
                outputBufferedWriter = new BufferedWriter( new OutputStreamWriter(new DataOutputStream(out)));
//                bufferedInputStream = new BufferedInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {

            while (!exit) {
                read();
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void read() {

            try {
//                bytes = in.read(buffer);
//
//                Log.d(TAG, "server read: " + new String(buffer, 0, bytes));
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(TCPSocketActivity.this, "server read: " + new String(buffer, 0, bytes), Toast.LENGTH_SHORT).show();
//                    }
//                });


//                bytes = dataInputStream.read(buffer);
//                Log.d(TAG, "server read: " + inputBufferedReader.readLine());

//                while ((readString = inputBufferedReader.readLine()) != null) {
//                    Log.d(TAG, "server read: " + readString);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(TCPSocketActivity.this, "server read: " + readString, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }

                if ((readString = inputBufferedReader.readLine()) != null) {
                    Log.d(TAG, "server read: " + readString);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TCPSocketActivity.this, "server read: " + readString, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void write(final String s) {
//            try {
//                out.write(buffer);
//                Log.d(TAG, "server write: " + new String(buffer));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            try {

//                out.write(s.getBytes());
//                out.flush();
//                Log.d(TAG, "server write: " + s);

                outputBufferedWriter.write(s);
                outputBufferedWriter.newLine();
                outputBufferedWriter.flush();

//                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void cancel() {

            connectedThread.exit = true;
            Log.d(TAG, "already closed");

//            try {
//                socket.close();
//                connectedThread.exit = true;
//                Log.d(TAG, "client close");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

    }

//    public static class MessageSender extends AsyncTask<String, Void, String> {
//
//        Socket socket;
//        String ip,message;
//        DataOutputStream dataOutputStream;
//        PrintWriter printWriter;
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            ip = params[0];
//            message = params[1];
//            Log.d(TAG, "ip: " + ip);
//            Log.d(TAG, "message: " + message);
//
//            try {
//                socket = new Socket(ip, SERVER_PORT);
////                printWriter = new PrintWriter(socket.getOutputStream());
////                printWriter.write(message);
////                printWriter.flush();
//                dataOutputStream = new DataOutputStream(socket.getOutputStream());
//                dataOutputStream.writeUTF(message);
//
////                printWriter.close();
//                dataOutputStream.close();
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }

    // 連接至 Server
    private class ConnectBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            new ClientSocketThread(String.valueOf(ipEt.getText())).start();
        }
    }

    // 發送訊息
    private class SendBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
//            MessageSender messageSender = new MessageSender();
//            messageSender.execute(String.valueOf(ipEt.getText()), String.valueOf(textEt.getText()));
//

//            String data = "hihi " + new Random().nextInt();
//            new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("hihi " + new Random().nextInt());
                }
            }).start();

//            Toast.makeText(TCPSocketActivity.this, "data sent", Toast.LENGTH_SHORT).show();
        }
    }

    // 建立 Server
    private class StartServerBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
//            Thread thread = new Thread(new ServerSocketThread());
//            thread.start();

            new ServerSocketThread().start();

        }
    }

    // 關閉 Server
    private class CloseServerBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            connectedThread.cancel();
        }
    }

    // go to UDPSocketActivity
    private class UDPTestBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(TCPSocketActivity.this, UDPSocketActivity.class));
        }
    }
}

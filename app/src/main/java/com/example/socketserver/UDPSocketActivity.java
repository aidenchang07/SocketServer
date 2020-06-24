package com.example.socketserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DialogTitle;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class UDPSocketActivity extends AppCompatActivity {
    private static final String TAG = UDPSocketActivity.class.getSimpleName();
    private static final int SERVER_PORT = 9700;

    private Button sendBtn;
    private Button startServerBtn;
    private Button closeServerBtn;
    private EditText ipEt;
    private TextView ipTv;
    private String currentIP;
    
    private ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udpsocket);

        findView();
        setListener();
        getIp();
    }

    private void findView() {
        sendBtn = findViewById(R.id.btn_send_udp);
        startServerBtn = findViewById(R.id.btn_start_server_udp);
        closeServerBtn = findViewById(R.id.btn_close_server_udp);
        ipEt = findViewById(R.id.et_ip_udp);
        ipTv = findViewById(R.id.tv_ip_udp);
    }

    private void setListener() {
        sendBtn.setOnClickListener(new SendBtnOnClickListener());
        startServerBtn.setOnClickListener(new StartServerBtnOnClickListener());
        closeServerBtn.setOnClickListener(new CloseServerBtnOnClickListener());
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

    private static class ServerThread extends AsyncTask<Void, Void, Void> {

        private volatile boolean exit = false;
        private WeakReference<UDPSocketActivity> weakActivity;
        private DatagramSocket server;
        private DatagramPacket packet;
        private ToastManager toastManager;

        ServerThread(UDPSocketActivity activity) {
            // 防止Memory Leaks(記憶體泄漏)
            this.weakActivity = new WeakReference<>(activity);
            toastManager = new ToastManager(weakActivity.get());
            try {
                byte[] buffer = new byte[1024]; // 接收只顯示此長度內的內容, 超過此長度就不顯示, 不會crash
                Log.v(TAG, "ServerThread buffer.length: " + buffer.length);
                this.server = new DatagramSocket(SERVER_PORT);

                if (server.isClosed()) {
                    toastManager.showShortToastInThread("Server未正常開啟");
                }
                toastManager.showShortToastInThread("Server開啟");

                packet = new DatagramPacket(buffer, buffer.length);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            while (!exit) {
                try {
                    Log.d(TAG, "doInBackground 接收中...");
                    server.receive(packet);
                    Log.d(TAG, "doInBackground 接收成功!");
                    Log.d(TAG, "doInBackground 接收到資料為: " + new String(packet.getData(), 0, packet.getLength()) + " packet.getLength(): " + packet.getLength());
                    toastManager.showLongToastInThread(new String(packet.getData(), 0, packet.getLength()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "doInBackground server: " + server + " isClose?: " + server.isClosed());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // 防止Memory Leaks(記憶體泄漏)
            final UDPSocketActivity activity = weakActivity.get();
            if (activity == null
                    || activity.isFinishing()
                    || activity.isDestroyed()) {
                // If the activity is gone, it will end.
                Log.d(TAG, "ServerThread onPostExecute Activity is gone");
                return;
            }

            if (!server.isClosed()) {
                toastManager.showShortToastInThread("Server未正常關閉");
                return;
            }
            toastManager.showShortToastInThread("Server關閉");
        }

        void cancel() {
            // 使線程的While迴圈結束
            exit = true;
            // 因receive是阻塞的，所以需直接使用close關閉
            server.close();
        }
    }

    private static class ClientThread extends AsyncTask<String, Void, Boolean> {

        private WeakReference<UDPSocketActivity> weakActivity;
        private DatagramSocket clientSocket;
        private InetAddress addressInet;
        private ToastManager toastManager;

        ClientThread(UDPSocketActivity activity) {
            // 防止Memory Leaks(記憶體泄漏)
            this.weakActivity = new WeakReference<>(activity);
            toastManager = new ToastManager(weakActivity.get());
            try {
                this.clientSocket = new DatagramSocket(SERVER_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String addressString = params[0];
            String sendString = params[1];

            try {
                addressInet = InetAddress.getByName(addressString);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "doInBackground sendString: " + sendString + " sendString.getBytes().length: " + sendString.getBytes().length);
            DatagramPacket sendPacket = new DatagramPacket(sendString.getBytes(), sendString.length(), addressInet, SERVER_PORT);

            try {
                // send message
                clientSocket.send(sendPacket);
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            // 防止Memory Leaks(記憶體泄漏)
            final UDPSocketActivity activity = weakActivity.get();
            if (activity == null
                    || activity.isFinishing()
                    || activity.isDestroyed()) {
                // If the activity is gone, it will end.
                Log.d(TAG, "ClientThread onPostExecute Activity is gone");
                return;
            }

            if (!result) {
                toastManager.showShortToastInThread("傳送失敗");
            }
            toastManager.showShortToastInThread("傳送成功");
        }
    }



    private class SendBtnOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            String message = "hihi packet " + new Random().nextInt();

            // 有輸入IP地址才送出封包
            if (String.valueOf(ipEt.getText()).length() > 0) {
                new ClientThread(UDPSocketActivity.this).execute(String.valueOf(ipEt.getText()), message);
            } else {
                Toast.makeText(UDPSocketActivity.this, "請輸入IP地址", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class StartServerBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Toast.makeText(UDPSocketActivity.this, "StartServerBtn", Toast.LENGTH_SHORT).show();

            if (serverThread == null) {
                Log.d(TAG, "onClick Server開啟");
                serverThread = new ServerThread(UDPSocketActivity.this);
                serverThread.execute();
            } else {
                Toast.makeText(UDPSocketActivity.this, "Server已開啟了", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick Server已開啟了");
            }
        }
    }

    private class CloseServerBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (serverThread != null) {
                Log.d(TAG, "OnClick Server關閉");
                serverThread.cancel();
                serverThread = null;
            } else {
                Toast.makeText(UDPSocketActivity.this, "Server均已關閉", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "OnClick Server均已關閉");
            }
        }
    }
}

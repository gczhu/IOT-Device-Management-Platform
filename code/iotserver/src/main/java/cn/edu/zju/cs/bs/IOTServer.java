package cn.edu.zju.cs.bs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 祝广程
 * @version 1.0
 */

public class IOTServer {

    public static void main(String[] args) throws IOException {
        int port = 1883;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("MQTT Broker started on port " + port);

            ExecutorService executorService = Executors.newCachedThreadPool();

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // 创建新的 WorkerThread 处理客户端连接
                WorkerThread workerThread = new WorkerThread(clientSocket);
                executorService.submit(workerThread);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

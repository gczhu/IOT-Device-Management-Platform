package cn.edu.zju.cs.bs;

import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IOTClient {
    public static void main(String[] args) {
//        Properties properties = null;
//        try {
//            properties = new Properties();
//            FileInputStream in = new FileInputStream("src\\main\\resources\\iot.properties");
//            properties.load(in);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        final String mqttServer = properties.getProperty("server");
//        final String topic = properties.getProperty("topic");
//        final String clientPrefix = properties.getProperty("prefix");
//        final String url = properties.getProperty("url");
//        final String username = properties.getProperty("username");
//        final String password = properties.getProperty("password");

        final String mqttServer = "tcp://iotserver:1883";
        final String topic = "testapp";
        final String clientPrefix = "device";
        final String url = "jdbc:mysql://mysql:3306/iot_dmp?useSSL=false&allowPublicKeyRetrieval=true";
        final String username = "root";
        final String password = "zgc112912";

        List<Integer> startedDevices = new ArrayList<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            ExecutorService executorService = Executors.newCachedThreadPool();
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                String sql = "SELECT * FROM device";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        int deviceId = resultSet.getInt("device_id");
                        if (!startedDevices.contains(deviceId)) {
                            startedDevices.add(deviceId);
                            WorkerThread thread = new WorkerThread();
                            thread.setDeviceId(deviceId);
                            thread.setMqttServer(mqttServer);
                            thread.setTopic(topic);
                            thread.setClientPrefix(clientPrefix);
                            executorService.submit(thread);
                        }
                    }
                    // 关闭线程池，等待所有线程执行完毕
                    executorService.shutdown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);

        // 关闭定时任务
        // scheduler.shutdown();
    }
}

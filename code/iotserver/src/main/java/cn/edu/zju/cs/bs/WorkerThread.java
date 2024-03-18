package cn.edu.zju.cs.bs;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
class WorkerThread extends Thread {
    private Socket clientSocket;
    private boolean running = true;
    // MySQL Configuration

    private static final String URL = "jdbc:mysql://mysql:3306/iot_dmp?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "zgc112912";
    public WorkerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            int read = is.read(buffer);

            byte[] connectAck = {0x20, 0x02, 0x00, 0x00};
            os.write(connectAck);
            os.flush();

            while (running) {
                read = is.read(buffer);
                if (read == -1) {
                    break;
                }
                int messageType = (buffer[0] >> 4) & 0x0f;
                if (messageType == 0x03) { // PUBLISH message
                    String content = readMqttString(buffer, 2);
                    // 查找 JSON 数据的起始位置
                    int jsonStartIndex = content.indexOf("{");
                    // 查找 JSON 数据的终止位置
                    int jsonEndIndex = -1;
                    if (jsonStartIndex != -1) {
                        jsonEndIndex = findMatchingBrace(content, jsonStartIndex);

                        if (jsonEndIndex != -1) {
                            String jsonContent = content.substring(jsonStartIndex, jsonEndIndex + 1);
                        }
                    }
                    String topic = "";
                    String jsonMessage = "";
                    if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                        // 提取 topic
                        topic = content.substring(1, jsonStartIndex).trim();
                        // 提取 JSON 消息体
                        jsonMessage = content.substring(jsonStartIndex, jsonEndIndex + 1);
                        System.out.println("Received message on topic " + topic + ": " + jsonMessage);

                        try {
                            // 提取数据
                            JSONObject jsonData = JSONObject.parseObject(jsonMessage);
                            int alert = jsonData.getIntValue("alert");
                            String clientId = jsonData.getString("clientId");
                            // 使用正则表达式提取deviceId
                            int deviceId = extractDeviceId(clientId);
                            String info = jsonData.getString("info");
                            Double latitude = jsonData.getDouble("lat");
                            Double longitude = jsonData.getDouble("lng");
                            String timestamp = jsonData.getString("timestamp");
                            // 将时间戳字符串转换为 LocalDateTime
                            LocalDateTime dateTime = convertTimestampToDateTime(timestamp);
                            // 格式化为 "yyyy/MM/dd HH:mm:ss" 格式的字符串
                            String time = formatDateTime(dateTime);
                            int value = jsonData.getIntValue("value");
                            // 更新数据库
                            // 插入MQTT消息
                            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                                connection.setAutoCommit(false);
                                // The SQL query to insert data into the table
                                String sql = "INSERT INTO m_q_t_t_msg (device_id, info, value, alert, longitude, latitude, timestamp, time) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                                    // Set parameters for the PreparedStatement
                                    preparedStatement.setInt(1, deviceId);
                                    preparedStatement.setString(2, info);
                                    preparedStatement.setInt(3, value);
                                    preparedStatement.setInt(4, alert);
                                    preparedStatement.setDouble(5, longitude);
                                    preparedStatement.setDouble(6, latitude);
                                    preparedStatement.setString(7, timestamp);
                                    preparedStatement.setString(8, time);

                                    // Execute the update
                                    int rowsAffected = preparedStatement.executeUpdate();

                                    // Check the result
                                    if (rowsAffected > 0) {
                                        System.out.println("Data inserted successfully.");
                                    } else {
                                        System.out.println("Failed to insert data.");
                                    }

                                    sql = "UPDATE device SET state = ?, longitude = ?, latitude = ?" +
                                            ", value = ? WHERE device_id = ?";
                                    PreparedStatement preparedStatement1 = connection.prepareStatement(sql);
                                    // Set parameters for the PreparedStatement
                                    if (alert == 1) {
                                        preparedStatement1.setInt(1, 2);
                                    } else {
                                        preparedStatement1.setInt(1, 1);
                                    }
                                    preparedStatement1.setDouble(2, longitude);
                                    preparedStatement1.setDouble(3, latitude);
                                    preparedStatement1.setInt(4, value);
                                    preparedStatement1.setInt(5, deviceId);

                                    // Execute the update
                                    int rowsAffected1 = preparedStatement1.executeUpdate();

                                    // Check the result
                                    if (rowsAffected1 > 0) {
                                        System.out.println("Data updated successfully.");
                                    } else {
                                        System.out.println("Failed to update data.");
                                    }
                                    connection.commit();
                                } catch (SQLException e) {
                                    connection.rollback();
                                    e.printStackTrace();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            int qosLevel = (buffer[0] >> 1) & 0x03;
                            // Handle QoS levels 1 and 2 acknowledgment
                            if (qosLevel == 1) {
                                // Send PUBACK acknowledgment
                                byte[] pubAck = {0x40, 0x02, buffer[2], buffer[3]};
                                os.write(pubAck);
                                os.flush();
                                System.out.println("Sent PUBACK acknowledgment");
                            } else if (qosLevel == 2) {
                                // Handle QoS level 2 acknowledgment
                                int packetIdentifier = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);

                                // Handle PUBREC
                                byte[] pubRec = {0x50, 0x02, buffer[2], buffer[3]};
                                os.write(pubRec);
                                os.flush();
                                System.out.println("Sent PUBREC acknowledgment");
                                // Wait for PUBREL
                                read = is.read(buffer);
                                if (read != -1) {
                                    int messageTypePubRel = (buffer[0] >> 4) & 0x0F;
                                    int packetIdentifierPubRel = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
                                    if (messageTypePubRel == 0x06 && packetIdentifierPubRel == packetIdentifier) {
                                        // Handle PUBREL
                                        byte[] pubRel = {0x62, 0x02, buffer[2], buffer[3]};
                                        os.write(pubRel);

                                        // Wait for PUBCOMP
                                        read = is.read(buffer);
                                        if (read != -1) {
                                            int messageTypePubComp = (buffer[0] >> 4) & 0x0F;
                                            int packetIdentifierPubComp = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);

                                            if (messageTypePubComp == 0x07 && packetIdentifierPubComp == packetIdentifier) {
                                                // Handle PUBCOMP
                                                byte[] pubComp = {0x70, 0x02, buffer[2], buffer[3]};
                                                os.write(pubComp);
                                                os.flush();
                                                System.out.println("Sent PUBREL acknowledgment");
                                            } else {
                                                // Handle error - unexpected message type or packet identifier
                                                System.out.println("Error: Unexpected PUBCOMP message");
                                            }
                                        } else {
                                            // Handle error - PUBCOMP not received
                                            System.out.println("Error: PUBCOMP not received");
                                        }
                                    } else {
                                        // Handle error - unexpected message type or packet identifier
                                        System.out.println("Error: Unexpected PUBREL message");
                                    }
                                } else {
                                    // Handle error - PUBREL not received
                                    System.out.println("Error: PUBREL not received");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Illegal JSON data.");
                        }
                    } else {
                        System.out.println("JSON data not found in the string.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String readMqttString(byte[] buffer, int offset) {
        int stringLength = ((buffer[offset] & 0xff) << 8) | (buffer[offset + 1] & 0xff);
        byte[] stringData = new byte[stringLength];
        System.arraycopy(buffer, offset + 2, stringData, 0, stringLength);
        return new String(stringData);
    }

    private static int findMatchingBrace(String content, int startIndex) {
        int count = 0;
        for (int i = startIndex; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1; // Matching '}' not found
    }

    private static int extractDeviceId(String clientId) {
        // 定义正则表达式
        String regex = ".*?(\\d+)";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(clientId);

        // 判断是否匹配到
        if (matcher.find()) {
            // 使用 group(1) 获取匹配到的整数部分
            String deviceIdString = matcher.group(1);
            // 将字符串转换为整数
            return Integer.parseInt(deviceIdString);
        } else {
            // 如果未匹配到整数，可以根据实际需求返回默认值或抛出异常等
            throw new IllegalArgumentException("No deviceId found in clientId");
        }

    }

    private static LocalDateTime convertTimestampToDateTime(String timestamp) {
        long epochMillis = Long.parseLong(timestamp);
        Instant instant = Instant.ofEpochMilli(epochMillis);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return dateTime.format(formatter);
    }

}

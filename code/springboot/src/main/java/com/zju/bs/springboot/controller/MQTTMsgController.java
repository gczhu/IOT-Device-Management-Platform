package com.zju.bs.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zju.bs.springboot.entity.Device;
import com.zju.bs.springboot.entity.MQTTMsg;
import com.zju.bs.springboot.entity.MQTTMsgInfo;
import com.zju.bs.springboot.entity.Message;
import com.zju.bs.springboot.mapper.DeviceMapper;
import com.zju.bs.springboot.mapper.MQTTMsgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 祝广程
 * @version 1.0
 */

@RestController
@RequestMapping("/message")
@CrossOrigin
public class MQTTMsgController {
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private MQTTMsgMapper mqttMsgMapper;

    @GetMapping("/searchMessage")
    public Message searchMessage(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int limit,
                                @RequestParam(defaultValue = "") String username,
                                @RequestParam(defaultValue = "") String deviceName,
                                @RequestParam(defaultValue = "") String state,
                                @RequestParam(defaultValue = "") String startDate,
                                @RequestParam(defaultValue = "") String endDate) {
        try {
            LambdaQueryWrapper<Device> deviceQueryWrapper = new LambdaQueryWrapper<>();
            List<MQTTMsgInfo> mqttMsgInfos = new ArrayList<>();
            if (username.length() == 0) {
                return new Message(false, "查询消息列表失败", 20001)
                        .data("items", new ArrayList<MQTTMsgInfo>())
                        .data("total", 0);
            } else {
                deviceQueryWrapper.eq(Device::getUsername, username);
            }
            if (deviceName.length() != 0) {
                deviceQueryWrapper.like(Device::getName, deviceName);
            }
            List<Device> devices = deviceMapper.selectList(deviceQueryWrapper);
            for (int i = 0; i < devices.size(); i++) {
                Device device = devices.get(i);
                LambdaQueryWrapper<MQTTMsg> MQTTMsgQueryWrapper = new LambdaQueryWrapper<>();
                MQTTMsgQueryWrapper.eq(MQTTMsg::getDevice_id, device.getDevice_id());
                if (state.length() != 0) {
                    MQTTMsgQueryWrapper.eq(MQTTMsg::getAlert, state);
                }
                if (startDate.length() != 0) {
                    // 使用 SimpleDateFormat 进行格式转换
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为UTC
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    try {
                        // 将输入日期字符串解析为 Date 对象
                        Date start = inputFormat.parse(startDate);
                        // 使用 Calendar 进行日期和时间的操作
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(start);
                        // 将时间部分设置为00:00:00
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        // 获取处理后的 Date 对象
                        startDate = outputFormat.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (endDate.length() != 0) {
                    // 使用 SimpleDateFormat 进行格式转换
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // 设置时区为UTC
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    try {
                        // 将输入日期字符串解析为 Date 对象
                        Date end = inputFormat.parse(endDate);
                        // 使用 Calendar 进行日期和时间的操作
                        Calendar calendar = Calendar.getInstance();
                        // 使用 Calendar 进行日期和时间的操作
                        calendar.setTime(end);
                        // 将时间部分设置为00:00:00
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        // 获取处理后的 Date 对象
                        endDate = outputFormat.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (startDate.length() != 0 && endDate.length() != 0) {
                    MQTTMsgQueryWrapper.le(MQTTMsg::getTime, endDate);
                    MQTTMsgQueryWrapper.ge(MQTTMsg::getTime, startDate);
                } else if (startDate.length() != 0) {
                    MQTTMsgQueryWrapper.ge(MQTTMsg::getTime, startDate);
                } else if (endDate.length() != 0) {
                    MQTTMsgQueryWrapper.le(MQTTMsg::getTime, endDate);
                }
                List<MQTTMsg> mqttMsgs = mqttMsgMapper.selectList(MQTTMsgQueryWrapper);
                for (int j = 0; j < mqttMsgs.size(); j++) {
                    MQTTMsg mqttMsg = mqttMsgs.get(j);
                    MQTTMsgInfo mqttMsgInfo = new MQTTMsgInfo(mqttMsg.getMsg_id(), device.getName(), mqttMsg.getInfo(), mqttMsg.getValue(),
                            mqttMsg.getAlert(), mqttMsg.getLongitude(), mqttMsg.getLatitude(), mqttMsg.getTime());
                    mqttMsgInfos.add(mqttMsgInfo);
                }
            }
            // 使用 Comparator 对象按照 time 属性进行排序
            Collections.sort(mqttMsgInfos, new Comparator<MQTTMsgInfo>() {
                @Override
                public int compare(MQTTMsgInfo msg1, MQTTMsgInfo msg2) {
                    return msg2.getTime().compareTo(msg1.getTime());
                }
            });
            List<MQTTMsgInfo> showMQTTMsgInfos = mqttMsgInfos.subList(Math.max(limit * (page - 1), 0), Math.min(limit * page, mqttMsgInfos.size()));
            return new Message(true, "查询成功", 20000)
                    .data("items", showMQTTMsgInfos)
                    .data("total", mqttMsgInfos.size());
        } catch (Exception e) {
            return new Message(false, "查询消息列表失败", 20001)
                    .data("items", new ArrayList<MQTTMsgInfo>())
                    .data("total", 0);
        }
    }
}

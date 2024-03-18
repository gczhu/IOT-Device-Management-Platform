package com.zju.bs.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zju.bs.springboot.entity.*;
import com.zju.bs.springboot.mapper.DeviceMapper;
import com.zju.bs.springboot.mapper.MQTTMsgMapper;
import com.zju.bs.springboot.mapper.UserMapper;
import com.zju.bs.springboot.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 祝广程
 * @version 1.0
 */

@RestController
@RequestMapping("/device")
@CrossOrigin
public class DeviceController {
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private MQTTMsgMapper mqttMsgMapper;

    @PostMapping("/createDevice")
    public Message createDevice(@RequestBody CreateInfo createInfo) {
        // 设置默认值
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String description = createInfo.getDescription().length() == 0 ? "无" : createInfo.getDescription();
        Device device = new Device(createInfo.getName(), createInfo.getType(), 1, description,
                119.9, 30.1, createInfo.getUsername(), 40, sdf.format(new Date()));

        try {
            int res = deviceMapper.insert(device);
            if (res > 0) {
                return new Message(true, "设备创建成功", 20000);
            } else {
                return new Message(false, "设备创建失败", 20001);
            }
        } catch (Exception e) {
            return new Message(false, "该设备已存在", 20001);
        }
    }

    @PostMapping("/deleteDevice")
    public Message deleteDevice(int device_id) {
        try {
            LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Device::getDevice_id, device_id);
            int res = deviceMapper.delete(queryWrapper);
            if (res > 0) {
                return new Message(true, "设备删除成功", 20000);
            } else {
                return new Message(false, "设备删除失败", 20001);
            }
        } catch (Exception e) {
            return new Message(false, "设备删除失败", 20001);
        }
    }

    @PostMapping("/modifyDevice")
    public Message modifyDevice(@RequestBody ModifyInfo modifyInfo) {
        // 设置默认值
        int device_id = modifyInfo.getDevice_id();
        String name = modifyInfo.getName();
        String type = modifyInfo.getType();
        String description = modifyInfo.getDescription().length() == 0 ? "无" : modifyInfo.getDescription();
        String username = modifyInfo.getUsername();

        try {
            LambdaQueryWrapper<Device> deviceQueryWrapper1 = new LambdaQueryWrapper<>();
            deviceQueryWrapper1.eq(Device::getUsername, username);
            List<Device> devices = deviceMapper.selectList(deviceQueryWrapper1);
            int index = -1;
            for (int i = 0; i < devices.size(); i++) {
                Device device = devices.get(i);
                if (device.getDevice_id() == device_id) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                return new Message(false, "设备信息修改失败", 20001);
            } else {
                Device old = devices.get(index);
                Device device = new Device(device_id, name, type, old.getState(), description, old.getLongitude(),
                        old.getLatitude(), username, old.getValue(), old.getCreated_time());
                LambdaQueryWrapper<Device> deviceQueryWrapper2 = new LambdaQueryWrapper<>();
                deviceQueryWrapper2.eq(Device::getDevice_id, device_id);
                int res = deviceMapper.update(device, deviceQueryWrapper2);
                if (res > 0) {
                    return new Message(true, "设备信息修改成功", 20000);
                } else {
                    return new Message(false, "设备信息修改失败", 20001);
                }
            }
        } catch (Exception e) {
            return new Message(false, "设备名重复", 20001);
        }
    }

    @GetMapping("/getDataList")
    public Message getDataList(int device_id) {
        try {
            // 获取当前日期的字符串表示
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String currentTime = sdf.format(new Date());
            String currentDate = currentTime.substring(0, 10);
            String currentHour = currentTime.substring(11, 13);

            // 构建小时数列表
            int hour = Integer.parseInt(currentHour);
            hour = Integer.parseInt(currentHour);
            List<String> hourList1 = new ArrayList<>();
            int flag = 1;
            for (int i = 0; i <= 12; i++) {
                String s = Integer.toString(hour);
                if (hour >= 0 && hour <= 9) {
                    s = "0" + Integer.toString(hour);
                }
                if (flag == 1) {
                    hourList1.add(currentDate + " " + s + ":00:00");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    // 获取减去一天后的日期部分
                    SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy/MM/dd");
                    String previousDate = dateOnlyFormat.format(calendar.getTime());
                    hourList1.add(previousDate + " " + s + ":00:00");
                }
                hour -= 2;
                if (hour < 0) {
                    hour += 24;
                    flag = 0;
                }
            }

            hourList1.set(0, currentTime);
            Collections.reverse(hourList1);

            // 获取从当前小时开始，往前每隔两小时的设备数据
            List<Integer> dataList = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                LambdaQueryWrapper<MQTTMsg> MQTTMsgQueryWrapper = new LambdaQueryWrapper<>();
                MQTTMsgQueryWrapper.eq(MQTTMsg::getDevice_id, device_id);
                MQTTMsgQueryWrapper.le(MQTTMsg::getTime, hourList1.get(i));
                MQTTMsgQueryWrapper.gt(MQTTMsg::getTime, hourList1.get(i - 1));
                MQTTMsgQueryWrapper.orderByDesc(MQTTMsg::getTime);
                List<MQTTMsg> mqttMsgs = mqttMsgMapper.selectList(MQTTMsgQueryWrapper);
                if (mqttMsgs.size() == 0) {
                    dataList.add(0);
                } else {
                    dataList.add(mqttMsgs.get(0).getValue());
                }
            }

            return new Message(true, "设备数据列表获取成功", 20000)
                    .data("dataList", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(false, "设备数据列表获取失败", 20001);
        }
    }

    @GetMapping("/searchDevice")
    public Message searchDevice(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int limit,
                                @RequestParam(defaultValue = "") String username,
                                @RequestParam(defaultValue = "") String id,
                                @RequestParam(defaultValue = "") String name,
                                @RequestParam(defaultValue = "") String type,
                                @RequestParam(defaultValue = "") String state,
                                @RequestParam(defaultValue = "") String startDate,
                                @RequestParam(defaultValue = "") String endDate,
                                @RequestParam(defaultValue = "+id") String sort) {
        try {
            LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
            if (username.length() == 0) {
                return new Message(false, "查询设备信息失败", 20001)
                        .data("items", new ArrayList<Device>())
                        .data("total", 0);
            } else {
                queryWrapper.eq(Device::getUsername, username);
            }
            if (id.length() != 0) {
                queryWrapper.eq(Device::getDevice_id, Integer.parseInt(id));
            }
            if (name.length() != 0) {
                queryWrapper.like(Device::getName, name);
            }
            if (type.length() != 0) {
                queryWrapper.eq(Device::getType, type);
            }
            if (state.length() != 0) {
                queryWrapper.eq(Device::getState, state);
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
                queryWrapper.le(Device::getCreated_time, endDate);
                queryWrapper.ge(Device::getCreated_time, startDate);
            } else if (startDate.length() != 0) {
                queryWrapper.ge(Device::getCreated_time, startDate);
            } else if (endDate.length() != 0) {
                queryWrapper.le(Device::getCreated_time, endDate);
            }
            if (sort.equals("+id")) {
                queryWrapper.orderByAsc(Device::getDevice_id);
            } else {
                queryWrapper.orderByDesc(Device::getDevice_id);
            }
            List<Device> devices = deviceMapper.selectList(queryWrapper);
            List<Device> showDevices = devices.subList(Math.max(limit * (page - 1), 0), Math.min(limit * page, devices.size()));
            return new Message(true, "查询成功", 20000)
                    .data("items", showDevices)
                    .data("total", devices.size());
        } catch (Exception e) {
            return new Message(false, "请输入正确的ID", 20001)
                    .data("items", new ArrayList<Device>())
                    .data("total", 0);
        }
    }

    @GetMapping("/getRoute")
    public Message getRoute(String device_name) {
        try {
            LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Device::getName, device_name);
            List<Device> devices = deviceMapper.selectList(queryWrapper);
            Device device = devices.get(0);
            LambdaQueryWrapper<MQTTMsg> msgQueryWrapper = new LambdaQueryWrapper<>();
            msgQueryWrapper.eq(MQTTMsg::getDevice_id, device.getDevice_id());
            List<MQTTMsg> mqttMsgs = mqttMsgMapper.selectList(msgQueryWrapper);

            // 使用 Comparator 对象按照 time 属性进行排序
            Collections.sort(mqttMsgs, new Comparator<MQTTMsg>() {
                @Override
                public int compare(MQTTMsg msg1, MQTTMsg msg2) {
                    return msg1.getTime().compareTo(msg2.getTime());
                }
            });

            // 过滤保留时间间隔大于等于一个小时的记录
            List<MQTTMsg> filteredMqttMsgs = new ArrayList<>();
            for (MQTTMsg msg : mqttMsgs) {
                if (filteredMqttMsgs.isEmpty()) {
                    filteredMqttMsgs.add(msg);  // 保留第一条消息
                } else {
                    // 计算时间差
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    long timeDifference = dateFormat.parse(msg.getTime()).getTime() - dateFormat.parse(filteredMqttMsgs.get(filteredMqttMsgs.size() - 1).getTime()).getTime();
                    if (timeDifference >= 600000) {
                        filteredMqttMsgs.add(msg);
                    }
                }
            }

            return new Message(true, "设备路线查询成功", 20000)
                    .data("items", filteredMqttMsgs);
        } catch (Exception e) {
            return new Message(false, "设备路线查询失败", 20001);
        }
    }
}

package com.zju.bs.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zju.bs.springboot.entity.Device;
import com.zju.bs.springboot.entity.MQTTMsg;
import com.zju.bs.springboot.entity.Message;
import com.zju.bs.springboot.entity.User;
import com.zju.bs.springboot.mapper.DeviceMapper;
import com.zju.bs.springboot.mapper.MQTTMsgMapper;
import com.zju.bs.springboot.mapper.UserMapper;
import com.zju.bs.springboot.utils.JwtUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author 祝广程
 * @version 1.0
 */

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private MQTTMsgMapper MQTTmsgMapper;

    @ApiOperation("用户登录认证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名"),
            @ApiImplicitParam(name = "password", value = "密码"),
            @ApiImplicitParam(name = "email", value = "邮箱"),
    })
    @PostMapping("/login")
    public Message loginIdentify(@RequestBody User user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        queryWrapper.eq(User::getPassword, user.getPassword());

        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) {
            return new Message(false, "密码错误", 20001);
        } else {
            String token = JwtUtils.generateToken(user.getUsername());
            return new Message(true, "认证成功", 20000).data("token", token);
        }
    }

    @GetMapping("/find")
    public Message findUser(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);

        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) {
            return new Message(false, "用户名不存在", 20001);
        } else {
            return new Message(true, "用户名存在", 20000);
        }
    }

    @GetMapping("/checkUsername")
    public Message checkUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);

        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) {
            return new Message(true, "用户名可用", 20000);
        } else {
            return new Message(false, "用户名重复", 20001);
        }
    }

    @GetMapping("/checkEmail")
    public Message checkEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);

        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) {
            return new Message(true, "邮箱地址可用", 20000);
        } else {
            return new Message(false, "邮箱地址重复", 20001);
        }
    }

    @GetMapping("/info")
    public Message userInfo(String token) {
        // 获取当前日期的字符串表示
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentTime = sdf.format(new Date());
        String currentDate = currentTime.substring(0, 10);
        String currentHour = currentTime.substring(11, 13);

        String username = JwtUtils.getClaimsByToken(token).getSubject();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);

        List<User> users = userMapper.selectList(queryWrapper);
        User user = users.get(0);
        if (users.isEmpty()) {
            return new Message(false, "用户信息获取失败", 20001);
        } else {
            LambdaQueryWrapper<Device> deviceQueryWrapper1 = new LambdaQueryWrapper<>();
            deviceQueryWrapper1.eq(Device::getUsername, username);
            List<Device> devices = deviceMapper.selectList(deviceQueryWrapper1);
            int deviceNum = devices.size();

            LambdaQueryWrapper<Device> deviceQueryWrapper2 = new LambdaQueryWrapper<>();
            deviceQueryWrapper2.eq(Device::getUsername, username);
            deviceQueryWrapper2.eq(Device::getState, 1);
            int onlineNum = deviceMapper.selectCount(deviceQueryWrapper2);

            LambdaQueryWrapper<Device> deviceQueryWrapper3 = new LambdaQueryWrapper<>();
            deviceQueryWrapper3.eq(Device::getUsername, username);
            deviceQueryWrapper3.eq(Device::getState, 2);
            int alertNum = deviceMapper.selectCount(deviceQueryWrapper3);

            int MQTTMsgNum = 0;
            for (int i = 0; i < deviceNum; i++) {
                LambdaQueryWrapper<MQTTMsg> msgQueryWrapper = new LambdaQueryWrapper<>();
                msgQueryWrapper.eq(MQTTMsg::getDevice_id, devices.get(i).getDevice_id());
                // 添加条件，确保time属性的日期部分与当前日期相同
                msgQueryWrapper.apply("LEFT(time, 10) = {0}", currentDate);
                int num = MQTTmsgMapper.selectCount(msgQueryWrapper);
                MQTTMsgNum += num;
            }

            // 构建日期列表
            List<String> dateList = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate today = LocalDate.parse(currentDate, formatter);
                dateList.add(today.minusDays(i).format(formatter));
            }

            // 获取从当日开始，往前一周每天的设备数量和数据量列表
            List<Integer> deviceNumList = new ArrayList<>();
            List<Integer> MQTTMsgNumList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LambdaQueryWrapper<Device> deviceQueryWrapper = new LambdaQueryWrapper<>();
                deviceQueryWrapper.eq(Device::getUsername, username);
                // 添加条件，确保time属性的日期部分与当前日期相同
                deviceQueryWrapper.apply("LEFT(created_time, 10) <= {0}", dateList.get(i));
                deviceNumList.add(deviceMapper.selectList(deviceQueryWrapper).size());
                int count = 0;
                for (int j = 0; j < deviceNum; j++) {
                    LambdaQueryWrapper<MQTTMsg> msgQueryWrapper = new LambdaQueryWrapper<>();
                    msgQueryWrapper.eq(MQTTMsg::getDevice_id, devices.get(j).getDevice_id());
                    // 添加条件，确保time属性的日期部分与当前日期相同
                    msgQueryWrapper.apply("LEFT(time, 10) = {0}", dateList.get(i));
                    count += MQTTmsgMapper.selectCount(msgQueryWrapper);
                }
                MQTTMsgNumList.add(count);
            }

            // 构建小时数列表
            int hour = Integer.parseInt(currentHour);
            List<String> hourList = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                hourList.add(Integer.toString(hour) + ":00");
                hour -= 2;
                if (hour < 0) {
                    hour += 24;
                }
            }
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

            Collections.reverse(hourList);
            Collections.reverse(hourList1);

            // 获取从当前小时开始，往前每隔两小时的在线数量和报警数量列表
            List<Integer> onlineNumList = new ArrayList<>();
            List<Integer> alertNumList = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                int onlineCount = 0;
                int alertCount = 0;
                for (int j = 0; j < deviceNum; j++) {
                    Device device = devices.get(j);
                    LambdaQueryWrapper<MQTTMsg> MQTTMsgQueryWrapper = new LambdaQueryWrapper<>();
                    MQTTMsgQueryWrapper.eq(MQTTMsg::getDevice_id, device.getDevice_id());
                    MQTTMsgQueryWrapper.le(MQTTMsg::getTime, hourList1.get(i));
                    MQTTMsgQueryWrapper.gt(MQTTMsg::getTime, hourList1.get(i - 1));
                    MQTTMsgQueryWrapper.orderByDesc(MQTTMsg::getTime);
                    List<MQTTMsg> mqttMsgs = MQTTmsgMapper.selectList(MQTTMsgQueryWrapper);
                    if (!mqttMsgs.isEmpty()) {
                        MQTTMsg mqttMsg = mqttMsgs.get(0);
                        if (mqttMsg.getAlert() != 1) {
                            onlineCount++;
                        } else {
                            alertCount++;
                        }
                    }
                }
                onlineNumList.add(onlineCount);
                alertNumList.add(alertCount);
            }

            // 转换日期列表中日期的格式
            dateList = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate today = LocalDate.parse(currentDate, formatter);
                formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                dateList.add(today.minusDays(i).format(formatter));
            }

            int sensorNum = 0;
            int actuatorNum = 0;
            int embeddedNum = 0;
            int communicationNum = 0;
            int controllerNum = 0;
            // 统计设备种类
            for (int i = 0; i < deviceNum; i++) {
                Device device = devices.get(i);
                switch (device.getType()) {
                    case "传感器设备":
                        sensorNum++;
                        break;
                    case "执行器设备":
                        actuatorNum++;
                        break;
                    case "嵌入式设备":
                        embeddedNum++;
                        break;
                    case "通讯设备":
                        communicationNum++;
                        break;
                    case "控制器设备":
                        controllerNum++;
                        break;
                    default:
                        System.out.println("未知设备");
                        break;
                }
            }

            // 获取用户的MQTTMsg列表
            List<MQTTMsg> MQTTMsgList = new ArrayList<>();
            for (int i = 0; i < deviceNum; i++) {
                Device device = devices.get(i);
                LambdaQueryWrapper<MQTTMsg> MQTTMsgQueryWrapper = new LambdaQueryWrapper<>();
                MQTTMsgQueryWrapper.eq(MQTTMsg::getDevice_id, device.getDevice_id());
                MQTTMsgList.addAll(MQTTmsgMapper.selectList(MQTTMsgQueryWrapper));
            }

            // 使用 Comparator 对象按照 time 属性进行排序
            Collections.sort(MQTTMsgList, new Comparator<MQTTMsg>() {
                @Override
                public int compare(MQTTMsg msg1, MQTTMsg msg2) {
                    return msg2.getTime().compareTo(msg1.getTime());
                }
            });

            return new Message(true, "用户信息获取成功", 20000)
                    .data("username", user.getUsername())
                    .data("email", user.getEmail())
                    .data("deviceNum", deviceNum)
                    .data("onlineNum", onlineNum)
                    .data("alertNum", alertNum)
                    .data("MQTTMsgNum", MQTTMsgNum)
                    .data("dateList", dateList)
                    .data("hourList", hourList)
                    .data("deviceNumList", deviceNumList)
                    .data("onlineNumList", onlineNumList)
                    .data("alertNumList", alertNumList)
                    .data("MQTTMsgNumList", MQTTMsgNumList)
                    .data("sensorNum", sensorNum)
                    .data("actuatorNum", actuatorNum)
                    .data("embeddedNum", embeddedNum)
                    .data("communicationNum", communicationNum)
                    .data("controllerNum", controllerNum)
                    .data("MQTTMsgList", MQTTMsgList)
                    .data("deviceList", devices);
        }
    }

    @PostMapping("/register")
    public Message userRegister(@RequestBody User user) {
        if (userMapper.insert(user) > 0) {
            return new Message(true, "注册成功", 20000);
        } else {
            return new Message(false, "注册失败", 20001);
        }
    }

    @PostMapping("/logout")
    public Message logout() {
        return new Message(true, "登出成功", 20000);
    }

}

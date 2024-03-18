package com.zju.bs.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.bs.springboot.entity.MQTTMsg;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 祝广程
 * @version 1.0
 */
@Mapper
public interface MQTTMsgMapper extends BaseMapper<MQTTMsg> {
}

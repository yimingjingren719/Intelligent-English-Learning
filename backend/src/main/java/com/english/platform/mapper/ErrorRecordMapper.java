package com.english.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.platform.entity.ErrorRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErrorRecordMapper extends BaseMapper<ErrorRecord> {
}

package com.yunji.titanrtx.manager.dao.mapper.report;

import com.yunji.titanrtx.manager.dao.entity.data.BeaconAlertEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * BeaconCollectorMapper
 *
 * @author leihz
 * @since 2020-05-11 2:54 下午
 */
@Mapper
public interface BeaconCollectorMapper {

    List<BeaconAlertEntity> findAlertsBetweenTime(@Param("startTime") String startTime,
                                                  @Param("endTime") String endTime,
                                                  @Param("alertId") String alertId);

}

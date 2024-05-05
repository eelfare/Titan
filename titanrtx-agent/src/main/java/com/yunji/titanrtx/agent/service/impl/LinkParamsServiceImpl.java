package com.yunji.titanrtx.agent.service.impl;

import com.yunji.titanrtx.agent.mapper.LinkParamsMapper;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.common.domain.task.ParamRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.yunji.titanrtx.common.u.LogU.info;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 5/3/2020 4:14 下午
 * @Version 1.0
 */
@Service("http")
@Slf4j
public class LinkParamsServiceImpl implements ParamsService {
    private static final int MIN_SELECT_SIZE = 10;

    @Resource
    private LinkParamsMapper mapper;

    @Override
    public List<String> selectParamsByLinkId(Integer linkId) {
        return mapper.selectParamsByLinkId(linkId);
    }

    @Override
    public List<String> selectParamsBatch(List<Integer> ids) {
        return mapper.selectParamsBatch(ids);
    }

    /**
     * 由于使用的是 mysql between 操作,其会包含 start 和 end 的值,因此需要 end-1.
     */
    @Override
    public List<String> selectParamsByRange(int linkId, ParamRange range) {
        long st = System.currentTimeMillis();
        //500,600
        //todo 当 end 为 1， 出现  LinkId [8963] 根据 range (start=0, end=1). 查询到的数据为空。
        //根据 0 -1 也是能查到1条数据的。
        //todo betwwen 0 and 0 的情况，查不到数据
        int end = range.getEnd() - 1;
        if (end <= range.getStart()) {
            end = end + 1;
        }

        List<String> res = mapper.selectParamsByRange(linkId, range.getStart(), end);
        if (res == null || res.size() == 0) {
            log.warn("LinkId [{}] 根据 between {}->{} 查询到的数据为空。", linkId, range.getStart(), end);
            return res;
        }
        if (range.getStart() == 0) {
            String param = mapper.selectParamRandom(linkId);
            res.add(param);
        }

        info("linkId:{} 查询数据范围:{}, 耗时:{}ms", linkId, range.getEnd() - range.getStart(), (System.currentTimeMillis() - st));
        return res;
    }
}

package com.yunji.titanrtx.cia.agent.log.disruptor.handler;

import com.lmax.disruptor.WorkHandler;
import com.yunji.titanrtx.cia.agent.annotation.StreamHandler;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.MateLog;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.ParamLog;
import com.yunji.titanrtx.common.u.DateU;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessLogHandler implements WorkHandler<AccessLog> {

    private StreamHandler streamHandler;

    private Material<MateLog> mateLogMaterial;

    private Material<ParamLog> paramLogMaterial;


    public AccessLogHandler(StreamHandler streamHandler, Material<MateLog> mateLogMaterial, Material<ParamLog> paramLogMaterial) {
        this.streamHandler = streamHandler;
        this.mateLogMaterial = mateLogMaterial;
        this.paramLogMaterial = paramLogMaterial;
    }

    @Override
    public void onEvent(AccessLog event) {
        log.debug("AccessLogHandler:{}......................................", event);
        try {
            AccessLog formatLog = streamHandler.onEvent(event);
            if (null != formatLog) {
                mateLogMaterial.push(dividedMetaLog(formatLog));
                ParamLog paramLog = dividedParamLog(formatLog);
                if (null != paramLog) paramLogMaterial.push(paramLog);
            }
        } catch (Exception e) {
            log.error(e.getMessage() + ", time: " + (event != null ? event.getTime() : "-1"));
        }

    }


    private ParamLog dividedParamLog(AccessLog formatLog) {
        if (formatLog.getParam() == null) return null;
        ParamLog paramLog = new ParamLog();
        paramLog.setDomain(formatLog.getDomain());
        paramLog.setPath(formatLog.getPath());
        paramLog.setTime(formatLog.getTime());
        paramLog.setParam(formatLog.getParam());
        paramLog.setElapsed(formatLog.getElapsed());
        paramLog.setRespCode(formatLog.getRespCode());
        return paramLog;
    }

    private MateLog dividedMetaLog(AccessLog formatLog) {
        MateLog mateLog = new MateLog();
        mateLog.setDomain(formatLog.getDomain());
        mateLog.setPath(formatLog.getPath());
        mateLog.setTime(formatLog.getTime());
        mateLog.setElapsed(formatLog.getElapsed());
        mateLog.setRequestTimes(1);
        long respCode = formatLog.getRespCode();
        if (respCode == 200) {
            mateLog.setSuccessTimes(1);
        }
        return mateLog;
    }
}

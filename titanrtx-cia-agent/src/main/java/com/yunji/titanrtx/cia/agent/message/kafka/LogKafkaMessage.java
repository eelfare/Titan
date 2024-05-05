package com.yunji.titanrtx.cia.agent.message.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunji.entryfilter.EntryFilter;
import com.yunji.titanrtx.cia.agent.annotation.StreamHandler;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.DisruptorMain;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.MateLog;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.ParamLog;
import com.yunji.titanrtx.cia.agent.log.disruptor.factory.AccessLogFactory;
import com.yunji.titanrtx.cia.agent.log.disruptor.factory.MetaLogFactory;
import com.yunji.titanrtx.cia.agent.log.disruptor.factory.ParamLogFactory;
import com.yunji.titanrtx.cia.agent.log.disruptor.handler.AccessLogHandler;
import com.yunji.titanrtx.cia.agent.log.disruptor.handler.MateLogHandler;
import com.yunji.titanrtx.cia.agent.log.disruptor.handler.ParamLogHandler;
import com.yunji.titanrtx.cia.agent.log.disruptor.producer.*;
import com.yunji.titanrtx.cia.agent.message.MessageListener;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-04-10T10:55:39.000Z %{host} [10/Apr/2019:18:55:39 +0800] marketing.yunjiglobal.com /yunjimarketingapp/app/queryNeedShowRedDotForFullCoupon.json?fullcouponUserIds=YJ208613995cad2bd5f567f83fa8eff8f5&strVersion=0&appCont=1&ticket=ticket%7C3542380_da12547aa6efb2fc894a137efe3a96fa - 200 172.22.19.28:8313 200 0.010 0.010
 */

/**
 * 更新: 腾讯云收集 ng 日志采用的是 filebeat,目前发送到 kafka 上面的消息格式如下:
 * <pre>
 * {
 * 	"@timestamp": "2019-09-02T08:53:58.667Z",
 * 	"@metadata": {
 * 		"beat": "filebeat",
 * 		"type": "_doc",
 * 		"version": "7.2.0",
 * 		"topic": "ng_log"
 *        },
 * 	"agentHost": "10.0.52.230",
 * 	"log": {
 * 		"offset": 914789,
 * 		"file": {
 * 			"path": "/var/log/nginx/indexpopweb.log.6"
 *        }
 *    },
 * 	"message": "[01/Sep/2019:03:01:30 +0800] ys.yunjiglobal.com /indexpopweb/app/getShopRecommendInfo.json?shopId=16954342&strVersion=0&appCont=1&ticket=ticket%7C16954342_933f6b0dc5be5ebd97cf09a48fe3b682 - 200 10.0.107.110:8466 200 0.010 0.010"
 * }
 * </pre>
 */
@Slf4j
@Component
public class LogKafkaMessage implements MessageListener {

    private static final String FORMAT = "dd/MMM/YYYY:HH:mm:ss";

    @Resource
    private StreamHandler streamHandler;

    @Value("${influxDB.rpName}")
    private String rpName;

    @Value("${limit.slotSize:20}")
    private int slotSize;

    private ThreadLocal<AtomicInteger> processCounter = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    @Resource
    private StoreService storeService;

    private Material<AccessLog> accessLogMaterial;

    @PostConstruct
    @Override
    public void init() {
        log.info("Slot size: {}", slotSize);

        DisruptorMain<MateLog> mateMain = new DisruptorMain<>();
//        MateLogStoreProducer mateProducer = new MateLogStoreProducer(storeService, rpName);
        MateLogBatchStoreProducer mateProducer = new MateLogBatchStoreProducer(storeService, rpName);
        Material<MateLog> mateLogMaterial =
                new MateLogProducer(mateMain.init(new MetaLogFactory(), "mateLogHandlerThread",
                        new MateLogHandler(mateProducer),
                        new MateLogHandler(mateProducer),
                        new MateLogHandler(mateProducer)));


        DisruptorMain<ParamLog> paramMain = new DisruptorMain<>();
        ParamLogStoreProducer paramProducer = new ParamLogStoreProducer(storeService, rpName);
        Material<ParamLog> paramLogMaterial =
                new ParamLogProducer(paramMain.init(new ParamLogFactory(), "paramsLogHandlerThread",
                        new ParamLogHandler(paramProducer),
                        new ParamLogHandler(paramProducer),
                        new ParamLogHandler(paramProducer)));

        DisruptorMain<AccessLog> accessMain = new DisruptorMain<>();
        accessLogMaterial =
                new AccessLogProducer(accessMain.init(new AccessLogFactory(), "accessLogHandlerThread",
                        new AccessLogHandler(streamHandler, mateLogMaterial, paramLogMaterial),
                        new AccessLogHandler(streamHandler, mateLogMaterial, paramLogMaterial),
                        new AccessLogHandler(streamHandler, mateLogMaterial, paramLogMaterial)));

    }

    /**
     * 消费文件的日志
     */
    public void consumeFileMessage(String naLog) {
        log.debug("接收到File日志信息:{}......................................", naLog);
        AccessLog accessLog = formatLog(naLog, false);
        if (null != accessLog) {
            accessLogMaterial.push(accessLog);
        }
    }


    //    @KafkaListener(topics = {"titanrtx_top_link"})
    @KafkaListener(topics = {"${kafka.consumer.topic}"})
    @Override
    public void doListener(String naLog) {
        int counter = processCounter.get().incrementAndGet();
        if (counter % slotSize == 0) {
            log.debug("接收到日志信息:{}......................................", naLog);
            AccessLog accessLog = formatLog(naLog, false);
            if (null != accessLog && !EntryFilter.parse(accessLog.getPath())) {
                accessLogMaterial.push(accessLog);
            }
            processCounter.get().set(0);
        }
    }

    private AccessLog formatLog(String ngLog, boolean failFast) {
        if (StringUtils.isBlank(ngLog)) return null;
        try {
            JSONObject jsonObject = JSON.parseObject(ngLog);
            String message = jsonObject.getString("message");

            String[] portion = message.split(" ");
            AccessLog accessLog = new AccessLog();
            accessLog.setTime(nanoRandomTime(DateU.minuteTime(portion[0].replace("[", ""), Locale.ENGLISH, FORMAT)));
            accessLog.setDomain(portion[2]);
            String[] uriParams = portion[3].split("\\?");
            accessLog.setPath(uriParams[0]);
            if (uriParams.length == 2) {
                accessLog.setParam(uriParams[1]);
            }
            accessLog.setRespCode(Integer.parseInt(portion[5]));
            String respTime;
            // 添加cookie数据采集
            if (portion.length == 11) {
                if (portion[8].startsWith("ticket") && !portion[8].startsWith("ticket|-")) {
                    String temp = "ticket=" + portion[8];
                    accessLog.setParam(StringUtils.isEmpty(accessLog.getParam())
                            ? temp : accessLog.getParam() + "&" + temp);
                }
                respTime = portion[9];
            } else if (portion.length == 12) { // 主要增加对小店项目的支持
                if (portion[9].startsWith("{") && portion[9].endsWith("}")) {
                    String temp = portion[9].replaceAll("\\\\x22", "\"");
                    accessLog.setParam(temp);
                    System.out.println(accessLog.getParam());
                }
                if (portion[8].startsWith("token") && !portion[8].startsWith("token=-")) {
                    accessLog.setParam(StringUtils.isEmpty(accessLog.getParam())
                            ? GlobalConstants.PARAMS_HEADER_SEGMENT + portion[8]
                            : accessLog.getParam() + GlobalConstants.PARAMS_HEADER_SEGMENT + portion[8]);
                }
                respTime = portion[9];
            } else {
                respTime = portion[8];
            }
            boolean numeric = StringUtils.isNumeric(respTime);
            if (numeric) {
                accessLog.setElapsed(Double.valueOf(respTime));
            }
            return accessLog;
//        } catch (ParseException e) {
        } catch (Exception e) {
            log.debug("Format nginx log got error,cause " + e.getMessage());
            if (failFast) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return null;
    }


/*
    private AccessLog formatLog(String log) {
        if (StringUtils.isBlank(log))return null;
        String[] portion = log.split(" ");
        AccessLog accessLog = new AccessLog();
        try {
            accessLog.setTime(nanoRandomTime(DateU.minuteTime(portion[0].replace("[",""), Locale.ENGLISH,FORMAT)));
            accessLog.setDomain(portion[2]);
            String[] uriParams =  portion[3].split("\\?");
            accessLog.setPath(uriParams[0]);
            if (uriParams.length == 2) {
                accessLog.setParam(uriParams[1]);
            }
            accessLog.setRespCode(Integer.valueOf(portion[5]));
            String respTime = portion[9];
            boolean numeric = StringUtils.isNumeric(portion[9]);
            if (numeric){
                accessLog.setElapsed(Double.valueOf(respTime));
            }
            return accessLog;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }*/


    private long nanoRandomTime(long systemTimeMillis) {
        return Long.valueOf(systemTimeMillis + CommonU.random(6));
    }

    public static void main(String[] args) {
        LogKafkaMessage logKafkaMessage = new LogKafkaMessage();
//        logKafkaMessage.formatLog("{\"@timestamp\":\"2019-09-02T08:53:58.669Z\",\"@metadata\":{\"beat\":\"filebeat\",\"type\":\"_doc\",\"version\":\"7.2.0\",\"topic\":\"ng_log\"},\"log\":{\"file\":{\"path\":\"/var/log/nginx/yunjiysapp.log.3\"},\"offset\":918565},\"message\":\"[01/Sep/2019:18:49:17 +0800] ys.yunjiglobal.com /yunjiysapp/app/queryRecommendScore.json?itemId=219783&appCont=1&ticket=ticket|3485383_d9591dbd63fe7f0e26d6f19adb304f86 MISS 200 10.0.121.246:8271 200 0.006 0.006\",\"agentHost\":\"10.0.52.230\"}");
//        logKafkaMessage.formatLog("{\"@timestamp\":\"2019-09-02T08:53:58.667Z\",\"@metadata\":{\"beat\":\"filebeat\",\"type\":\"_doc\",\"version\":\"7.2.0\",\"topic\":\"ng_log\"},\"agentHost\":\"10.0.52.230\",\"log\":{\"offset\":914789,\"file\":{\"path\":\"/var/log/nginx/indexpopweb.log.6\"}},\"message\":\"[01/Sep/2019:03:01:30 +0800] ys.yunjiglobal.com /indexpopweb/app/getShopRecommendInfo.json?shopId=16954342&strVersion=0&appCont=1&ticket=ticket%7C16954342_933f6b0dc5be5ebd97cf09a48fe3b682 - 200 10.0.107.110:8466 200 0.010 0.010\"}");
//        logKafkaMessage.formatLog("{\"@metadata\":{\"beat\":\"filebeat\",\"topic\":\"ng_log\",\"type\":\"_doc\",\"version\":\"7.2.0\"},\"input\":{\"type\":\"log\"},\"@timestamp\":\"2019-11-26T10:00:30.424Z\",\"ecs\":{\"version\":\"1.0.0\"},\"message\":\"[27/Nov/2019:10:15:38 +0800] buy.yunjiglobal.com /yunjiorderbuy/order/confirm.json?version=0&appCont=1&shopId=17238762&content=276753%3APOPB192232453%3A1%3A%3A%3A%3A%3B276753%3APOPB192232457%3A1%3A%3A%3A%3A%3B276753%3APOPB192232455%3A1%3A%3A%3A%3A%3B276753%3APOPB192232445%3A1%3A%3A%3A%3A%3B276753%3APOPB192232446%3A1%3A%3A%3A%3A%3B276753%3APOPB192232441%3A1%3A%3A%3A%3A&isCompress=0&newerZoneUserType=1&t=1574820938952&checkedYE=false&addressId=4131500 - 200 10.0.109.94:8598 200 0.067 0.067 ticket|17238762_3af751f6fc88a6b7830c4685cc9b304a\",\"host\":{\"name\":\"TXIDC-nginx-recruit7\"},\"log\":{\"file\":{\"path\":\"\\/var\\/log\\/nginx\\/error.log\"},\"offset\":698392161},\"agentHost\":\"10.0.54.201\",\"agent\":{\"id\":\"9360fd05-f4b0-4abd-b75b-a1017d83a57e\",\"hostname\":\"TXIDC-nginx-recruit7\",\"type\":\"filebeat\",\"ephemeral_id\":\"e148a224-049b-43a8-be3c-78886e78c1ce\",\"version\":\"7.2.0\"}}");
//        logKafkaMessage.formatLog("{\"@metadata\":{\"beat\":\"filebeat\",\"topic\":\"ng_log\",\"type\":\"_doc\",\"version\":\"7.2.0\"},\"input\":{\"type\":\"log\"},\"@timestamp\":\"2019-11-26T10:00:30.424Z\",\"ecs\":{\"version\":\"1.0.0\"},\"message\":\"[07/Feb/2020:01:23:54 +0800] buy.yunjiglobal.com /yunjiorderbuy/order/confirm.json?version=0&appCont=1&shopId=2313640&content=331887%3APOPB193086230%3A1&isCompress=0&newerZoneUserType=0&t=1581009834717&checkedYE=false -  ticket|2313640_413c85d9908e2937913ff07abc95046a 200 10.0.130.85:8598 200 0.049 0.049\",\"host\":{\"name\":\"TXIDC-nginx-recruit7\"},\"log\":{\"file\":{\"path\":\"\\/var\\/log\\/nginx\\/error.log\"},\"offset\":698392161},\"agentHost\":\"10.0.54.201\",\"agent\":{\"id\":\"9360fd05-f4b0-4abd-b75b-a1017d83a57e\",\"hostname\":\"TXIDC-nginx-recruit7\",\"type\":\"filebeat\",\"ephemeral_id\":\"e148a224-049b-43a8-be3c-78886e78c1ce\",\"version\":\"7.2.0\"}}");
//        logKafkaMessage.formatLog("{\"@metadata\":{\"beat\":\"filebeat\",\"topic\":\"ng_log\",\"type\":\"_doc\",\"version\":\"7.2.0\"},\"input\":{\"type\":\"log\"},\"@timestamp\":\"2019-11-26T10:00:30.424Z\",\"ecs\":{\"version\":\"1.0.0\"},\"message\":\"[07/Feb/2020:01:18:00 +0800] buy.yunjiglobal.com /yunjiorderbuy/join/device/fingerprint.json?uaToken=122%23gRDFbE5yEExpNDpZyDpJEJponDJE7SNEEP7ZpJRgu1PP%2BBNLpCXF%2Bu5Ad4yL7AlpEPGZEJ7BuDPE%2BBNPpC76EJponDEL7SNEEyGrpJ%2Bgu4Ep%2BFQLpoGUEELWn4Ch7gNFPezgpHALSsFkb58cNQbMd0LATlUdtMUQ%2B3u8oybMzW4HVn%2BUFa2uebVa8oL6J%2FMEyF3DWgfoEEpanSp1ul5EDPXT8oLUJNIWLvDn7W3bD93xnSL1elbEELXZ8oL6JNEEyBfDqMAbEEpangL4ul0EDLVr8CpUJ4bEyF3mqWivDEpxdMt1uO8EEt%2BY8CLUqNI%2FDlhF7ZNbxxbSKSR84YR4ClWrQIQ56Axtyckh%2Fw63Q%2BA3pkon6%2FtbfjWHPxnz3ewEB3Fap8W0izt3tiDddWlBp%2BEIFbwTZnzWLOWpyZRbLENVeWE4hgK1tpri8NwezGL1tQ1Ii%2B8soZDjwD%2BZybxG8Z61X9EPhxr5EVXoI8FZsYUWFrj5%2BITMci3vRnOWaWKQSAvNrvYoVVuBsE4QNZuM%2BR0Hl21NUHJ0tP9%2B8ygEv6OK6JOh3nAJF8MZekMS1m7iQUn5tC9asqyJKLaYjJQI09V2Kn9SC1bFwAq4ZQaF5F%2F2av44KI9wkwNayhStsx1Mg6z1zPwdVJp1EoGB1xllJhvbWBX7m2BL0JobnJa6I%2F9WKcEHIZUFewl2maqke2bThjFqDMc4cxSK6UpEzH2K8J%3D%3D&webUmidToken=T7267CB58FA0970EBAA1CFE38997AB0069170986E5AD853E610576D97C5&platform=2 -  ticket|18612570_60e65bfa4140faaec604393528242c1e 200 10.0.109.141:8598 200 0.001 0.002\",\"host\":{\"name\":\"TXIDC-nginx-recruit7\"},\"log\":{\"file\":{\"path\":\"\\/var\\/log\\/nginx\\/error.log\"},\"offset\":698392161},\"agentHost\":\"10.0.54.201\",\"agent\":{\"id\":\"9360fd05-f4b0-4abd-b75b-a1017d83a57e\",\"hostname\":\"TXIDC-nginx-recruit7\",\"type\":\"filebeat\",\"ephemeral_id\":\"e148a224-049b-43a8-be3c-78886e78c1ce\",\"version\":\"7.2.0\"}}");
//        logKafkaMessage.formatLog("{\"@metadata\":{\"beat\":\"filebeat\",\"topic\":\"ng_log\",\"type\":\"_doc\",\"version\":\"7.2.0\"},\"input\":{\"type\":\"log\"},\"@timestamp\":\"2019-11-26T10:00:30.424Z\",\"ecs\":{\"version\":\"1.0.0\"},\"message\":\"[07/Feb/2020:15:28:17 +0800] buy.yunjiglobal.com /yunjiorderbuy/join/device/fingerprint.json?deviceToken=Mze9RfocRRDZ3tlGRZMZo_QOytx455JGR1dF5cFZyZyWo_Dj57QRoZeVoc%2BE5cJ_RZsnotAvoZZWoRSvoRSvo1gF5cB4jcMR5Kg4yRZj5Kg4R_Dv5FoqZ7dSV5gSZRDoWZSZj1y0&platform=3 - 200 10.0.108.251:8598 200 - 0.001 0.002\",\"host\":{\"name\":\"TXIDC-nginx-recruit7\"},\"log\":{\"file\":{\"path\":\"\\/var\\/log\\/nginx\\/error.log\"},\"offset\":698392161},\"agentHost\":\"10.0.54.201\",\"agent\":{\"id\":\"9360fd05-f4b0-4abd-b75b-a1017d83a57e\",\"hostname\":\"TXIDC-nginx-recruit7\",\"type\":\"filebeat\",\"ephemeral_id\":\"e148a224-049b-43a8-be3c-78886e78c1ce\",\"version\":\"7.2.0\"}}");
//        logKafkaMessage.formatLog("{\"@metadata\":{\"beat\":\"filebeat\",\"topic\":\"ng_log\",\"type\":\"_doc\",\"version\":\"7.2.0\"},\"input\":{\"type\":\"log\"},\"@timestamp\":\"2019-11-26T10:00:30.424Z\",\"ecs\":{\"version\":\"1.0.0\"},\"message\":\"[27/Feb/2020:21:32:52 +0800] item.yunjiglobal.com /yunjiitemapp/app/global/getShareAssemblyPriceSwitch.json?appCont=1&strVersion=0&ticket=ticket%7C20082907_85245816a7d7f055527924bcc87905e6 - 200 10.0.132.143:8265 200 0.002 0.002\",\"host\":{\"name\":\"TXIDC-nginx-recruit7\"},\"log\":{\"file\":{\"path\":\"\\/var\\/log\\/nginx\\/error.log\"},\"offset\":698392161},\"agentHost\":\"10.0.54.201\",\"agent\":{\"id\":\"9360fd05-f4b0-4abd-b75b-a1017d83a57e\",\"hostname\":\"TXIDC-nginx-recruit7\",\"type\":\"filebeat\",\"ephemeral_id\":\"e148a224-049b-43a8-be3c-78886e78c1ce\",\"version\":\"7.2.0\"}}");
//        logKafkaMessage.formatLog("{\"@metadata\":{\"beat\":\"filebeat\",\"topic\":\"ng_log\",\"type\":\"_doc\",\"version\":\"7.2.0\"},\"input\":{\"type\":\"log\"},\"@timestamp\":\"2019-11-26T10:00:30.424Z\",\"ecs\":{\"version\":\"1.0.0\"},\"message\":\"[25/Mar/2020:00:02:11 +0800] xd.yunjiweidian.com /xdwxapp/api/order/getOrderPackages?orderId=30760208 - 200 10.0.113.99:8562 200 token=472F906E840EC97CC0E8E0AA98E2A3EC28664B091DEDA3F584EEA1AC9A800C9D 0.005 0.006\",\"host\":{\"name\":\"TXIDC-nginx-recruit7\"},\"log\":{\"file\":{\"path\":\"\\/var\\/log\\/nginx\\/error.log\"},\"offset\":698392161},\"agentHost\":\"10.0.54.201\",\"agent\":{\"id\":\"9360fd05-f4b0-4abd-b75b-a1017d83a57e\",\"hostname\":\"TXIDC-nginx-recruit7\",\"type\":\"filebeat\",\"ephemeral_id\":\"e148a224-049b-43a8-be3c-78886e78c1ce\",\"version\":\"7.2.0\"}}");
        logKafkaMessage.formatLog("{\"@metadata\":{\"beat\":\"filebeat\",\"topic\":\"ng_log\",\"type\":\"_doc\",\"version\":\"7.2.0\"},\"input\":{\"type\":\"log\"},\"@timestamp\":\"2019-11-26T10:00:30.424Z\",\"ecs\":{\"version\":\"1.0.0\"},\"message\":\"[25/Mar/2020:18:54:13 +0800] xd.yunjiweidian.com /xdwxapp/api/order/orderList - 200 10.0.112.31:8562 200 token=A8F735E6013B7DAD996E5A668E4EDD3E0B239712292886C6CD005DD0BAD585E8 {\\x22lastOrderId\\x22:\\x22\\x22,\\x22page\\x22:1,\\x22rows\\x22:10,\\x22searchParam\\x22:\\x22\\x22,\\x22status\\x22:[10]} 0.004 0.005\",\"host\":{\"name\":\"TXIDC-nginx-recruit7\"},\"log\":{\"file\":{\"path\":\"\\/var\\/log\\/nginx\\/error.log\"},\"offset\":698392161},\"agentHost\":\"10.0.54.201\",\"agent\":{\"id\":\"9360fd05-f4b0-4abd-b75b-a1017d83a57e\",\"hostname\":\"TXIDC-nginx-recruit7\",\"type\":\"filebeat\",\"ephemeral_id\":\"e148a224-049b-43a8-be3c-78886e78c1ce\",\"version\":\"7.2.0\"}}", true);
    }
}

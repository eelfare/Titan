package com.yunji.titanrtx.agent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;


public class TitanxAgentApplicationTests {

    @Test
    public void contextLoads() {
        UnpooledByteBufAllocator aDefault = UnpooledByteBufAllocator.DEFAULT;
        ByteBuf byteBuf = aDefault.directBuffer(10);
        byteBuf.setLong(0,2);
    }

}


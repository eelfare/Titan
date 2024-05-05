package com.yunji.titanrtx.manager.dao.bos.http;

import lombok.Data;

import java.io.Serializable;

@Data
public class PairBo implements Serializable {

    private int code;

    private int times;

    private String codeRate;


}

package com.yunji.titanrtx.common.domain.auto;

import lombok.Data;

/**
 * @author jingf
 */
@Data
public class BatchDeploy extends AbstractDeploy {
    /**
     * 批次ID
     */
    Integer batchId;
    public boolean isBatch() {
        return true;
    }
}

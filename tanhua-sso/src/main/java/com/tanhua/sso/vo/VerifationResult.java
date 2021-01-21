package com.tanhua.sso.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类用于封装校验码结果
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifationResult {

    private boolean verification;
}
package com.tanhua.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question extends BasePojo {

    private Long id;
    /**
     * 用户问题
     */
    private Long userId;
    /**
     * 用户id
     */
    private String txt;

}
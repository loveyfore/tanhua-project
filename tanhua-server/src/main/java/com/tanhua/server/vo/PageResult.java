package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {

    /**
     * 总记录数
     */
    private Integer counts = 0;
    /**
     * 页大小
     */
    private Integer pagesize = 0;
    /**
     * 总页数
     */
    private Integer pages = 0;
    /**
     * 当前页码
     */
    private Integer page = 0;
    /**
     * 列表
     * Collections.emptyList();为空列表进行填充   -->[]
     */
    private List<?> items = Collections.emptyList();

}
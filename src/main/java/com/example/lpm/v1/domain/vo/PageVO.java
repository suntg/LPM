package com.example.lpm.v1.domain.vo;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页参数")
public class PageVO<T> implements Serializable {
    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private long total;

    /**
     * 当前页结果
     */
    @Schema(description = "当前页结果")
    private List<T> rows;

}

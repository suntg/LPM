package com.example.lpm.v1.domain.query;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页参数")
public class PageQuery implements Serializable {

    @Schema(description = "页数")
    private Integer pageNum = 1;

    @Schema(description = "行数")
    private Integer pageSize = 10;

    /*@Schema(description = "排序字段, KEY：字段；VALUE：DESC/ASC")
    private LinkedHashMap<String, String> sortedFields;*/
}

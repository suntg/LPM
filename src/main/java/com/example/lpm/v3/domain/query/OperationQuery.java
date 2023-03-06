package com.example.lpm.v3.domain.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationQuery {

    private String requestUri;

    private String ip;

}

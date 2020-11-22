package com.maolabs.dynamodb.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class Filme {
    private Integer ano;
    private String titulo;
    private Map<String, Object> infoMap;
}

package com.maolabs.dynamodb.model.request;


import com.maolabs.dynamodb.model.Filme;
import lombok.Data;

import java.util.Map;

@Data
public class FilmeRequest {
    private Integer ano;
    private String titulo;
    private Map<String, Object> info;

    public Filme toModel() {
        return Filme.builder()
                .ano(this.ano)
                .titulo(this.titulo)
                .infoMap(this.info)
                .build();
    }
}

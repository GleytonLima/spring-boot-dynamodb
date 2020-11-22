package com.maolabs.dynamodb.controller;

import com.maolabs.dynamodb.model.request.CriarTabelaRequest;
import com.maolabs.dynamodb.model.request.DeletarTabelaRequest;
import com.maolabs.dynamodb.service.TabelaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tabelas")
public class TabelaController {

    @Autowired
    TabelaService tabelaService;

    @GetMapping
    public ResponseEntity listar() {
        var resultado = tabelaService.listarTabelas();
        if (resultado.isLeft()) {
            return ResponseEntity.ok(resultado.left());
        }
        return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
    }

    @PostMapping
    public ResponseEntity criar(@RequestBody CriarTabelaRequest criarTabelaRequest) {
        var resultado = tabelaService.criarTabela(criarTabelaRequest.getNome());
        if (resultado.isLeft()) {
            return ResponseEntity.ok(resultado.left());
        }
        return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
    }

    @DeleteMapping
    public ResponseEntity deletar(@RequestBody DeletarTabelaRequest criarTabelaRequest) {
        var resultado = tabelaService.deletarTabela(criarTabelaRequest.getNome());
        if (resultado.isLeft()) {
            return ResponseEntity.ok(resultado.left());
        }
        return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
    }
}

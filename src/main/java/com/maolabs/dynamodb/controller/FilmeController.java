package com.maolabs.dynamodb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maolabs.dynamodb.model.request.FilmeRequest;
import com.maolabs.dynamodb.service.FilmeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/filmes")
public class FilmeController {

    @Autowired
    FilmeService filmeService;

    @Autowired
    ObjectMapper objectMapper;

    @PostMapping("/carregar")
    public String carregarDados() {
        // TODO: Ajustar para passar o arquivo na requisição
        try {
            filmeService.carregarDadosDeFilmesDeArquivo();
            return "Carregado com sucesso";
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao carregar arquivo " + e.getLocalizedMessage();
        }
    }

    @GetMapping
    public ResponseEntity pesquisarPorAnoTitulo(@RequestParam Integer ano, @RequestParam String titulo) {
        var resultado = filmeService.pesquisarPorAnoTitulo(ano, titulo);
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().attributes());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/pesquisar-por-ano")
    public ResponseEntity listarPorAno(@RequestParam Integer ano) {
        var resultado = filmeService.pesquisarPorAno(ano);
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().stream().map(item -> item.attributes()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/pesquisar-por-ano-titulo-inicio-fim")
    public ResponseEntity listarPorAnoTituloInicialFinal(@RequestParam Integer ano, @RequestParam String de, @RequestParam String ate) {
        var resultado = filmeService.pesquisarPorAnoTituloInicioFim(ano, de, ate);
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().stream().map(item -> item.attributes()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/pesquisar-entre-anos")
    public ResponseEntity listarEntreAnos(@RequestParam Integer anoInicial, @RequestParam Integer anoFinal) {
        var resultado = filmeService.pesquisarEntreAnos(anoInicial, anoFinal);
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().stream().map(item -> item.attributes()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity cadastrar(@RequestBody FilmeRequest filmeRequest) {
        var resultado = filmeService.cadastrar(filmeRequest.toModel());
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity atualizar(@RequestBody FilmeRequest filmeRequest) {
        var resultado = filmeService.atualizar(filmeRequest.toModel());
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().attributes());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/remove-primeiro-ator-se-filme-tem-mais-tres")
    public ResponseEntity removerPrimeiroAutor(@RequestBody FilmeRequest filmeRequest) {
        var resultado = filmeService.removePrimeiroAtorDoFilmeSePossuiMaisDeTresAtores(filmeRequest.toModel());
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().attributes());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/incrementar-nota")
    public ResponseEntity incrementarNota(@RequestBody FilmeRequest filmeRequest) {
        var resultado = filmeService.incrementarNota(filmeRequest.toModel());
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().attributes());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity deletar(@RequestBody FilmeRequest filmeRequest) {
        var resultado = filmeService.deletar(filmeRequest.toModel());
        if (resultado.isRight()) {
            return ResponseEntity.badRequest().body(resultado.right().getLocalizedMessage());
        }
        if (resultado.left().isPresent()) {
            return ResponseEntity.ok(resultado.left().get().attributes());
        }
        return ResponseEntity.notFound().build();
    }
}

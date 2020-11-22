package com.maolabs.dynamodb.service;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maolabs.dynamodb.model.Filme;
import com.maolabs.dynamodb.util.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class FilmeService {

    private final DynamoDB dynamoDB;

    private final String TABELA_FILMES_NOME = "Movies";

    public FilmeService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    private Table getTable() {
        return dynamoDB.getTable(TABELA_FILMES_NOME);
    }

    public void carregarDadosDeFilmesDeArquivo() throws IOException {
        Table table = getTable();
        final File file = new File("moviedata.json");
        JsonParser parser = new JsonFactory().createParser(file);
        JsonNode rootNode = new ObjectMapper().readTree(parser);
        Iterator<JsonNode> iter = rootNode.iterator();
        ObjectNode currentNode;
        while (iter.hasNext()) {
            currentNode = (ObjectNode) iter.next();
            int year = currentNode.path("year").asInt();
            String title = currentNode.path("title").asText();
            try {
                table.putItem(new Item().withPrimaryKey("year", year, "title",
                        title).withJSON("info",
                        currentNode.path("info").toString()));
                log.info("PutItem succeeded: " + year + " " + title);
            } catch (Exception e) {
                log.error("Unable to add movie: " + year + " " + title);
                log.error(e.getMessage());
                break;
            }
        }
        parser.close();
    }

    public Either<Item, Exception> pesquisarPorAnoTitulo(Integer ano, String titulo) {
        var table = getTable();

        var getItemSpec = new GetItemSpec()
                .withPrimaryKey(
                        "year", ano,
                        "title", titulo);
        try {
            log.info("Tentando recuperar item...");
            Item outcome = table.getItem(getItemSpec);
            log.info("Item recuperado com sucesso: " + outcome);
            return Either.left(outcome);
        } catch (Exception e) {
            log.error("Não foi possível recuperar item: " + ano + " " + titulo);
            log.error(e.getMessage());
            return Either.right(e);
        }

    }


    public Either<List<Item>, Exception> pesquisarPorAno(Integer ano) {
        var table = getTable();

        var nameMap = new HashMap<String, String>();
        nameMap.put("#yr", "year");

        var valueMap = new HashMap<String, Object>();
        valueMap.put(":yyyy", ano);

        QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#yr = :yyyy").withNameMap(nameMap)
                .withValueMap(valueMap);
        try {
            log.info("Tentando listar itens por ano...");
            return executarPesquisa(table, querySpec);
        } catch (Exception e) {
            log.error("Não foi possível listar itens do ano: " + ano);
            log.error(e.getMessage());
            return Either.right(e);
        }

    }


    public Either<List<Item>, Exception> pesquisarPorAnoTituloInicioFim(Integer ano, String de, String ate) {
        var table = getTable();

        var nameMap = new HashMap<String, String>();
        nameMap.put("#yr", "year");

        var valueMap = new HashMap<String, Object>();
        valueMap.put(":yyyy", ano);
        valueMap.put(":letter1", de);
        valueMap.put(":letter2", ate);

        QuerySpec querySpec = new QuerySpec()
                .withProjectionExpression("#yr, title, info.genres, info.actors[0]")
                .withKeyConditionExpression("#yr = :yyyy and title between :letter1 and :letter2")
                .withNameMap(nameMap)
                .withValueMap(valueMap);
        try {
            log.info("Tentando listar itens por ano e titulo inicial e final...");
            return executarPesquisa(table, querySpec);
        } catch (Exception e) {
            log.error("Não foi possível listar itens do ano e titulo inicial e final: " + ano);
            log.error(e.getMessage());
            return Either.right(e);
        }

    }

    /**
     * Segundo a documentação, esse método pesquisa todos os registros da tabela para em seguida
     * descartar os que não se enquadram no filtro
     * Também é possível usar a operação Scan com quaisquer índices secundários criados na
     * tabela.
     *
     * @param anoInicial
     * @param anoFinal
     * @return
     */
    public Either<List<Item>, Exception> pesquisarEntreAnos(Integer anoInicial, Integer anoFinal) {
        var table = getTable();

        var nameMap = new HashMap<String, String>();
        nameMap.put("#yr", "year");

        var valueMap = new HashMap<String, Object>();
        valueMap.put(":start_yr", anoInicial);
        valueMap.put(":end_yr", anoFinal);

        ScanSpec scanSpec = new ScanSpec().withProjectionExpression("#yr, title, info.rating")
                .withFilterExpression("#yr between :start_yr and :end_yr").withNameMap(nameMap)
                .withValueMap(valueMap);
        try {
            log.info("Tentando listar itens por ano e titulo com spec...");
            var query = table.scan(scanSpec);
            Iterable<Item> iterable = () -> query.iterator();
            final List<Item> items = StreamSupport
                    .stream(iterable.spliterator(), false)
                    .collect(Collectors.toList());
            return Either.left(items);
        } catch (Exception e) {
            log.error("Não foi possível listar itens do ano e titulo inicial e final: " + anoInicial + " " + anoFinal);
            log.error(e.getMessage());
            return Either.right(e);
        }

    }

    private Either<List<Item>, Exception> executarPesquisa(Table table, QuerySpec querySpec) {
        final ItemCollection<QueryOutcome> query = table.query(querySpec);
        Iterable<Item> iterable = () -> query.iterator();
        final List<Item> items = StreamSupport
                .stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
        return Either.left(items);
    }

    public Either<Item, Exception> cadastrar(Filme filme) {
        var table = getTable();

        var putItemSpec = new PutItemSpec()
                .withItem(new Item()
                        .withPrimaryKey(
                                "year", filme.getAno(),
                                "title", filme.getTitulo())
                        .withMap("info", filme.getInfoMap()))
                .withReturnValues(ReturnValue.ALL_OLD);
        try {
            log.info("Cadastrando novo filme...");
            PutItemOutcome outcome = table.putItem(putItemSpec);
            log.info("Item cadastrado com sucesso:\n" + outcome.getPutItemResult());
            return Either.left(outcome.getItem());
        } catch (Exception e) {
            log.error("Não foi possível cadastrar filme: " + filme.getTitulo() + " " + filme.getAno());
            log.error(e.getMessage());
            return Either.right(e);
        }
    }

    public Either<Item, Exception> atualizar(Filme filme) {
        var table = getTable();
        final Object actors = filme.getInfoMap().get("actors");
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey(
                        "year", filme.getAno(),
                        "title", filme.getTitulo())
                .withUpdateExpression("set info.rating = :r, info.plot=:p, info.actors=:a")
                .withValueMap(
                        new ValueMap()
                                .withNumber(":r", Integer.parseInt(filme.getInfoMap().get("rating").toString()))
                                .withString(":p", filme.getInfoMap().get("plot").toString())
                                .withList(":a", (List) actors))
                .withReturnValues(ReturnValue.UPDATED_NEW);
        return atualizarFilme(filme, table, updateItemSpec);
    }

    public Either<Item, Exception> deletar(Filme filme) {
        var table = getTable();
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(
                        new PrimaryKey(
                                "year", filme.getAno(),
                                "title", filme.getTitulo()))
                .withConditionExpression("info.rating <= :val")
                .withValueMap(new ValueMap().withNumber(":val", 5.0))
                .withReturnValues(ReturnValue.ALL_OLD);
        try {
            log.info("Tentando deletar condicionalmente...");
            final DeleteItemOutcome deleteItemOutcome = table.deleteItem(deleteItemSpec);
            log.info("Item deletado com sucesso");
            return Either.left(deleteItemOutcome.getItem());
        } catch (Exception e) {
            log.error("Não foi possível deletar o item: " + filme.getAno() + " " + filme.getTitulo());
            log.error(e.getMessage());
            return Either.right(e);
        }
    }

    public Either<Item, Exception> incrementarNota(Filme filme) {
        var table = getTable();

        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(
                "year", filme.getAno(),
                "title", filme.getTitulo())
                .withUpdateExpression("set info.rating = info.rating + :val")
                .withValueMap(new ValueMap().withNumber(":val", Integer.parseInt(filme.getInfoMap().get("rating").toString())))
                .withReturnValues(ReturnValue.UPDATED_NEW);
        try {
            log.info("Incrementando um contador atômico...");
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            log.info("Item atualizado com sucesso:\n" +
                    outcome.getItem().toJSONPretty());
            return Either.left(outcome.getItem());
        } catch (Exception e) {
            log.error("Unable to update item: " + filme.getAno() + " " + filme.getTitulo());
            log.error(e.getMessage());
            return Either.right(e);
        }
    }

    public Either<Item, Exception> removePrimeiroAtorDoFilmeSePossuiMaisDeTresAtores(Filme filme) {
        var table = getTable();
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey(
                        "year", filme.getAno(),
                        "title", filme.getTitulo())
                .withUpdateExpression("remove info.actors[0]")
                .withConditionExpression("size(info.actors) > :num")
                .withValueMap(new ValueMap().withNumber(":num", 3))
                .withReturnValues(ReturnValue.UPDATED_NEW);
        return atualizarFilme(filme, table, updateItemSpec);
    }

    private Either<Item, Exception> atualizarFilme(Filme filme, Table table, UpdateItemSpec updateItemSpec) {
        try {
            log.info("Atualizando filme...");
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            log.info("Item atualizado com sucesso:\n" + outcome.getUpdateItemResult());
            return Either.left(outcome.getItem());
        } catch (Exception e) {
            log.error("Não foi possível atualizar filme: " + filme.getTitulo() + " " + filme.getAno());
            log.error(e.getMessage());
            return Either.right(e);
        }
    }


}

package com.maolabs.dynamodb.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.maolabs.dynamodb.util.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class TabelaService {

    private final DynamoDB dynamoDB;

    public TabelaService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public Either<String, Exception> criarTabela(String nomeTabela) {
        try {
            log.info("Tentando criar tabela");
            Table table = dynamoDB.createTable(nomeTabela,
                    Arrays.asList(new KeySchemaElement("year", KeyType.HASH), // Partition
                            // key
                            new KeySchemaElement("title", KeyType.RANGE)), // Sort key
                    Arrays.asList(new AttributeDefinition("year", ScalarAttributeType.N),
                            new AttributeDefinition("title", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            table.waitForActive();
            log.info("Sucesso. Status da Tabela" +
                    table.getDescription().getTableStatus());
            return Either.left(table.getDescription().getTableStatus());
        } catch (Exception e) {
            log.info("Não foi possível criar a tabela ");
            log.info(e.getMessage());
            return Either.right(e);
        }
    }

    public Either<List<String>, Exception> listarTabelas() {
        try {
            log.info("Tentando listar tabelas");
            var tables = dynamoDB.listTables();
            var resultList = new ArrayList<String>();
            tables.forEach(table -> {
                resultList.add(table.getTableName());
            });
            return Either.left(resultList);
        } catch (Exception e) {
            log.info("Não foi possível listar tabelas: ");
            log.info(e.getMessage());
            return Either.right(e);
        }
    }

    public Either<String, Exception> deletarTabela(String nomeTabela) {
        Table table = dynamoDB.getTable(nomeTabela);
        try {
            log.info("Tentando deletar a tabela; aguarde...");
            final DeleteTableResult delete = table.delete();
            table.waitForDelete();
            log.info("Deletado com sucesso.");
            return Either.left(nomeTabela);
        }
        catch (Exception e) {
            log.error("Não foi possível deletar a tabela: ");
            log.error(e.getMessage());
            return Either.right(e);
        }
    }

}

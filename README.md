# Criando uma API com Spring Boot e DynamoDB

Exercício seguindo Guia do Desenvolvedor disponível em https://docs.aws.amazon.com/pt_br/amazondynamodb/latest/developerguide/dynamodb-dg.pdf

Clone o projeto e em seguida execute:

```shell script
./mvnw spring-boot:run
```

Adicionei na pasta raiz o arquivo _dynamoDB.postman_collection.json para testar a API com o Postman (https://www.postman.com/)

# DynamoDB

Existem três principais formas de executar o Dynamodb na sua máquina. A Amazon disponibiliza o passo a passo em https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html

É necessário configurar credenciais e região de login no ambiente local usando AWS CLI (https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.CLI.html)

Utilize o comando configure para definir as credenciais (pode ser qualquer valor):

```shell script
aws configure
```

Este projeto já vem com o Dynamodb embutido (foi adicionada no pom.xml a dependência, o plugin e para funcionar
foi necessário, ainda, incluir os arquivos do sqlite4 na pasta native-libs como no exemplo em https://github.com/eugenp/tutorials/tree/master/aws.

## Dynamodb com Docker

Para executar este projeto com docker, altere o profile no arquivo application.properties para:

``` properties
spring.profiles.active=dev-local-docker-ou-jar
```
E execute um container docker com a imagem oficial:

```shell script
docker run --name meu-dynamodb-local -p 8000:8000 amazon/dynamodb-local
```

## Com arquivo jar (mais leve)

É possível executar o Dynamodb com um arquivo jar disponibilizado no link https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html.

Baixe o Dynamodb, extraia o conteúdo e execute o comando:

```shell script
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb
```

Ajuste o arquivo application.properties para:

``` properties
spring.profiles.active=dev-local-docker-ou-jar
```
Execute o projeto.

## Com o serviço na nuvem da AWS

É possível executar o projeto utilizando uma tabela no próprio
serviço da Amazon.

Se você já possui uma conta na AWS, o básico para conseguir o acesso à nuvem é criar um usuário 
por meio do Console do IAM, e incluí-lo num grupo 
com permissão no seu serviço do DynamoDB. Na tela de gerenciamento de usuários, normalmente,
é disponibilizado um arquivo csv com credenciais de acesso.
Utilize as credenciais para configurar sua máquina com o comando "configure" do aws cli, por exemplo: 

```shell script
aws configure
AWS Access Key ID [****************eyId]: AAAAAXBLABLABLA
AWS Secret Access Key [****************3456]: 6GXXAZBLABLABLA
Default region name [us-west-1]: us-east-1
Default output format [None]: json
```
No windows, o comando gera uma pasta .aws com arquivos com esses
dados de credenciais.

Após você configurar as credenciais, o SDK da AWS vai buscá-la para executar o Dynamodb.
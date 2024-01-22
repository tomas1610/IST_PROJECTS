# DistLedger

Distributed Systems Project 2022/2023

## Authors

**Group A67**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name              |           User                       |              Email                               |
|--------|-------------------|--------------------------------------|--------------------------------------------------|
| 99340  | Tomás Marques     | <https://github.com/tomas1610>       | <mailto:tomasduarte1610@tecnico.ulisboa.pt>      |
| 99300  | Pedro Rodrigues   | <https://github.com/PedroDRodrigues> | <mailto:pedro.dias.rodrigues@tecnico.ulisboa.pt> |
| 80949  | Ramiro Gomes      | <https://github.com/ramiromgomes>    | <mailto:ramiro.m.gomes@tecnico.ulisboa.pt>       |

## Getting Started

The overall system is made up of several modules. The main server is the _DistLedgerServer_. The clients are the _User_ 
and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/DistLedger) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.

## Testing

## Solução proposta

Como a operação só é aceite por um gestor de réplica (já prevenido de mensagens duplicadas), o TS que o gestor de réplica atribui à operação será único, logo pode ser usado como identificador único da operação.

Decidimos entao que iriamos criar um prevTS (TS que representa o nº do pedido do cliente) e um valueTS (o replicaTS após a execucao do pedido) como atributo de cada operação. Isto iria permitir-nos que assim que recebamos novas operações através do pedido gossip do administrador, seja possível à replica receber as operações, acompanhar a evolução do nosso cliente, e atualizar o seu valueTS e replicaTS.

A replicaTS é criada através do auxilio de um HashMap<ServerInfo,Integer>, que irá ser atualizado através do método Lookup.

Nesta entrega, o sistema funciona da mesma forma que nas entregas anteriores, com apenas uma ligeira diferença, as réplicas podem estar desatualizadas, visto que as operações só são propagadas quando o admin o solicita. Para ver se uma réplica esta atualizada devemos fazer uma verificação estabelecida pela gossip architecture, op.prevTS <= valueTS, isto é o servidor tem já executou todas as operações até ao momento em que esta nova foi requisitada, pelo que tem a capacidade de responder imediatamente.

O sistema é capaz de receber novos pedidos de escrita mesmo estando desatualizado, marcando essas operações como instavéis e não as executando no momento. No entanto, caso receba um pedido de leitura, o servidor deve bloquear-se até estar atualizado, e aí sim responde ao cliente.

## Algoritmo Gossip

Quando o admin solicita um gossip, deve passar como argumento o qualificador da réplica que irá realizar o pedido e enviar o seu ledger às outras réplicas.

Por sua vez, a réplica irá fazer uma simples verificação (checkState()), que irá determinar se alguma das outras réplicas esta inativa no momento, caso esteja o gossip deve falhar.

Caso passe esta verificação, a réplica deverá executar um pedido de lookup, e para todos as outras réplicas, a remetente irá enviar o conteúdo do seu ledger, tendo início no index igual ao valor do replicaTS correpondente à réplica destinatária do pedido Gossip, até ao final do ledger.

Ao receber, iremos filtrar o conteúdo recebido, e caso encontremos alguma operação já conhecida pelo servidor, ignoramo-la.
No caso de ser uma nova operação, a réplica atualiza o seu replicaTS, através da comparação com a operationTS (operationTS[i] > replicaTS[i]), e por seguida irá intrepertar a mensagem da operação, executá-la e verificar a possibilidade de um incremento do seu replicaValueTS.

Ao fim de as operações serem lidas e adicionadas, a replica irá ordenar o ledger por ordem de operation.prevTS, e verifica se ainda existem operações que não foram executadas, e que já têm possibilidade de o ser. Após isso, responde à outra réplica.

## Casos de Coerência Fraca

No desenvolvimento da nossa ideia final para o projeto, não estaria a ser possível, manter todas as réplicas coerentes, caso os gossips não fossem executados por ordem de atualização, a réplica que devia executar o gossip deveria ser sempre na maior parte dos casos, a réplica mais antiga, para evitar perdas de informação, e perda de servidores por serem considerados desatualizados, pois seria impossível os mesmos garantirem estabilidade.
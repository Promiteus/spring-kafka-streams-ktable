## Spring Boot и Kafka Streams. Применение KTable и local storage для группировки поступающих данных по стоимости

###Стеки:  
Java 17, Spring Boot 3, Maven, Kafka Streams, Docker, Docker Compose.  

### Запуск проекта  

1. В корне проекта вызвать команду:  

```
docker-compose up -d
```
Результат - сервисы zookeeper и broker kafka должны быть запущены без ошибок:  
```
roman@roman-MS-7C83:~/IdeaProjects/kafka/kafka-streams-2$ docker-compose ps
  Name               Command            State                                         Ports                                       
----------------------------------------------------------------------------------------------------------------------------------
broker      /etc/confluent/docker/run   Up      0.0.0.0:9092->9092/tcp,:::9092->9092/tcp, 0.0.0.0:9101->9101/tcp,:::9101->9101/tcp
zookeeper   /etc/confluent/docker/run   Up      0.0.0.0:2181->2181/tcp,:::2181->2181/tcp, 2888/tcp, 3888/tcp   
```  

2. Собрать проект (вызвать в корне команду):  
```
mvn clean package
```

3. Запустить проект:  
```
mvn spring-boot:run
```


### Описание проекта
Проект описывает небольшой фрагмент аналитики данных, где через конечную точку в топик Kafka отправляется пачка данных о продажах. Эти данные попадают в топологию обработки, где они группируются, суммируются по стоимости и сохраняются в локальном хранилище потока. Далее эти данные можно получить запросом из второй конечной точки - точки запроса результатов группировки (результат берется из локального хранилища потока).  

В качестве данных здесь выступает класс Purchase:
```
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase implements Serializable {
    private String id;
    private String name;
    private double price;
    private String currency = "RUR";
    private long timestamp = new Date().getTime();
}
```

Конечные точки отправки данных и запроса результатов группировки находятся в папке postman в виде одноименной коллекции.
* Отправить данные о покупках в топик Kafka:  
> POST http://localhost:8080/api/purchases  

Тело запроса:  
```
[
    {"id": 1, "name": "шариковая ручка", "price": 30},
    {"id": 2, "name": "напиток cola", "price": 85},
    {"id": 3, "name": "шариковая ручка", "price": 30},
    {"id": 4, "name": "книга", "price": 1230},
    {"id": 5, "name": "тетрадь", "price": 18},
    {"id": 6, "name": "тетрадь", "price": 22},
    {"id": 7, "name": "пакет-майка", "price": 5}
]
```  
* Получить результаты группировки по полю name с суммированием цен товаров:  
> GET http://localhost:8080/api/purchases/groups  

После отправки данных о покупках вы увидите в терминале (с некоторой задержкой) результат:  
```
...
Message sent successfully. Offset: 0
Message sent successfully. Offset: 1
Message sent successfully. Offset: 2
Message sent successfully. Offset: 3
Message sent successfully. Offset: 4
Message sent successfully. Offset: 5
Message sent successfully. Offset: 6
[purchases_topic]: шариковая ручка, Purchase(id=1, name=шариковая ручка, price=30.0, currency=RUR, timestamp=1726015858329)
[purchases_topic]: напиток cola, Purchase(id=2, name=напиток cola, price=85.0, currency=RUR, timestamp=1726015858329)
[purchases_topic]: шариковая ручка, Purchase(id=3, name=шариковая ручка, price=30.0, currency=RUR, timestamp=1726015858329)
[purchases_topic]: книга, Purchase(id=4, name=книга, price=1230.0, currency=RUR, timestamp=1726015858329)
[purchases_topic]: тетрадь, Purchase(id=5, name=тетрадь, price=18.0, currency=RUR, timestamp=1726015858329)
[purchases_topic]: тетрадь, Purchase(id=6, name=тетрадь, price=22.0, currency=RUR, timestamp=1726015858329)
[purchases_topic]: пакет-майка, Purchase(id=7, name=пакет-майка, price=5.0, currency=RUR, timestamp=1726015858329)
[purchases_table]: шариковая ручка, Purchase(id=1, name=шариковая ручка, price=60.0, currency=RUR, timestamp=1726015858329)
[purchases_table]: тетрадь, Purchase(id=5, name=тетрадь, price=40.0, currency=RUR, timestamp=1726015858329)
[purchases_table]: напиток cola, Purchase(id=2, name=напиток cola, price=85.0, currency=RUR, timestamp=1726015858329)
[purchases_table]: книга, Purchase(id=4, name=книга, price=1230.0, currency=RUR, timestamp=1726015858329)
[purchases_table]: пакет-майка, Purchase(id=7, name=пакет-майка, price=5.0, currency=RUR, timestamp=1726015858329)
...

```  
Где [purchases_topic] это данные, которые пришли от POST запроса с измененным ключом (группируемый ключ поле name класса Purchase) в топологии. Метка [purchases_table] - результаты группировки по полю name.  

Если вызвать запрос GET http://localhost:8080/api/purchases/groups , то будет получен результат группировки как из терминала для метки [purchases_table]:  
```
[
    {
        "id": "5",
        "name": "тетрадь",
        "price": 40.0,
        "currency": "RUR",
        "timestamp": 1726015858329
    },
    {
        "id": "1",
        "name": "шариковая ручка",
        "price": 60.0,
        "currency": "RUR",
        "timestamp": 1726015858329
    },
    {
        "id": "4",
        "name": "книга",
        "price": 1230.0,
        "currency": "RUR",
        "timestamp": 1726015858329
    },
    {
        "id": "2",
        "name": "напиток cola",
        "price": 85.0,
        "currency": "RUR",
        "timestamp": 1726015858329
    },
    {
        "id": "7",
        "name": "пакет-майка",
        "price": 5.0,
        "currency": "RUR",
        "timestamp": 1726015858329
    }
]
```  

Все последующие вызовы будут суммировать и группировать поступающие данные для новых и старых ключей.  

### Визуализация процессов в кластере Kafka  

Для визуализации процессов вписан сервис kafka-ui в файл docker-compose.yml:  
```
kafka-ui:
    container_name: kafka-ui
    image: provectuslabs/kafka-ui:latest
    ports:
      - 8082:8080
    environment:
      DYNAMIC_CONFIG_ENABLED: 'true'
```

Если приложение было запущено в первый раз, то вам нужно зайти в веб-интерфейс сервиса kafka-ui по ссылке: http://localhost:8082/  
Для связи сервиса с брокером нужно указать следующие параметры:  
1. Cluster name - Можете указать просто как "Kafka Cluster"

2. Bootstrap Servers - Суда вам внужно вписать host PLAINTEXT://kafka, port 29092 ну или другое наименование в зависимости от вашей конфигурации параметра "KAFKA_ADVERTISED_LISTENERS" в kafka image. Соотвественно если у вас поднято несколько реплик кафки, вам нужно их всех вписать. Apache рекомендуют иметь 3 ноды с kafka на вашем проекте.

3. Metrics  
   3.1 metrics type -> JMX  
   3.2 port -> 9101 ну или как вы указали в своей конфигурации KAFKA_JMX_PORT брокера
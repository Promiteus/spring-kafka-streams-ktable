## Spring Boot и Kafka Streams. Применение KTable и local storage для группировки поступающих данных по стоимости

###Стеки:  
Java 17, Spring Boot 3, Kafka Streams, Docker, Docker Compose.  

### Запуск проекта  




### Описание проекта
Данный проект описывает небольшой фрагмент аналитики данных, где через конечную точку в топик Kafka отправляется пачка данных о продажах. Эти данные попадают в топологию обработки, где они группируются, суммируются по стоимости и сохраняются в локальном хранилище потока. Далее эти данные можно получить запросом из второй конечной точки - точки запроса результатов группировки (результат берется из локального хранилища потока).  

В качестве данные здесь выступает класс Purchase:
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
* Отправить данные о покупка в топик Kafka:  
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

После отправки данных о покупках вы увидите в терминале (с некоторой задержкой) результа:  
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

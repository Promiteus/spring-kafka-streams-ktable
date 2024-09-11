### Spring Boot и Kafka Streams. Применение KTable и local storage для группировки поступающих данных по стоимости
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
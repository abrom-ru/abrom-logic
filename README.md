# ПО для управления умным домом на базе контроллера [wirenboard](https://wirenboard.com/)

## На данный момент поддерживается следующий функционал:

* Настройка правил через встроенный интерфейс wirenboard'a
* Перенос настроек на новые устройства посредством переноса конфигурационного файла.
* Установка ПО через терминал wirenboard'a
* Обновление ПО через терминал wirenboard'a
* Удаление ПО через терминал wirenboard'a

## Список правил настраиваемых через веб-интерфейс:

Тип HEAT - управление тёплым полом/батареями

| название поля | описание                                                               |
|---------------|------------------------------------------------------------------------|
| relays        | реле управление обогревателями/клапанами                               |
| floor temp    | датчик температуры пола (опционально)                                  |
| room temp     | датчик температуры в комнате (опционально)                             |
| inverted      | инвертировано ли управление обогревателями/клапанами (1 - да, 0 - нет) |
| is water      | водяная ли система отопления (1 - да, 0 - нет)                         |

Тип IRCONDITIONER - обычный ик кондиционер с возможностью подогрева
1-5 ROM охлаждение
6 ROM выключение
7-11 Нагрев

| название поля    | описание                                      |
|------------------|-----------------------------------------------|
| device           | название устройства                           |
| outside temp     | температура на улице (опционально)            |
| room temp        | датчик температуры в комнате (опционально)    |
| conditioner temp | датчик температуры кондиционера (опционально) |

Тип SMART_CONDITIONER - кондиционер с обратной связью

| название поля    | описание                                      |
|------------------|-----------------------------------------------|
| device           | название устройства                           |
| outside temp     | температура на улице (опционально)            |
| room temp        | датчик температуры в комнате (опционально)    |
| conditioner temp | датчик температуры кондиционера (опционально) |

# Кнопки:

тип BUTTON

| название поля   | описание                                             |
|-----------------|------------------------------------------------------|
| check condition | условие для срабатывание кнопки, если нет физической |
| button topic    | топик кнопки                                         |
| tap type        | тип нажатия для переключения                         |
| output topic    | топики устройств которые переключаем                 |
| on condition    | условие для проверки на включение (выражение)        |
| on delay        | задержка на включение в милисекундах                 |
| off delay       | задержка на выключение в милисекундах                |
| on value        | значение посылаемое при включение (выражение)        |
| off value       | значение посылаемое при выключение (выражение)       |

## Типы нажатий

| тип    | описание                                        |
|--------|-------------------------------------------------|
| SINGLE | одинарное нажатие                               |
| DOUBLE | двойное нажатие                                 |
| TRIPLE | тройное нажатие                                 |
| HOLD   | зажатие                                         |
| CLICK  | одинарное нажатие(срабатывает при других типах) |

# Кастомные правила:

тип CUSTOM

| название поля | описание                                                   |
|---------------|------------------------------------------------------------|
| in with state | 1 если у правило есть вкл/выкл состояние и 0 иначе         |
| on condition  | условие для включения (выражение)                          |
| off condition | условие для выключения (выражение)                         |
| on delay      | задержка на включение в секундах                           |
| off delay     | задержка на выключение в секундах                          |
| on message    | сообщения при выполнение условия на включение (выражение)  |
| off message   | сообщения при выполнение условия на выключение (выражение) |
| output topics | топики для отправления значений                            |

# Светодиодная лента

тип LED

| название поля | описание                                |
|---------------|-----------------------------------------|
| button        | топик кнопки                            |
| switch        | топик переключателя ленты (опционально) |
| led           | топик ленты                             |

## Режимы

| название режима   | описание             |
|-------------------|----------------------|
| одинарное нажатие | включение/выключение |
| зажатие           | диммирование         |

# RGB лента

тип RGB

| название поля | описание                           |
|---------------|------------------------------------|
| button        | топик кнопки                       |
| tap type      | тип нажатия(такие же как в кнопке) |
| red           | топик красного цвета               |
| green         | топик зелёного цвета               |
| blue          | топик синего цвета                 |

# Телеграм оповещение:

тип TELEGRAM

| название поля  | описание                         |
|----------------|----------------------------------|
| keys           | ключи от pushme бота             |
| send condition | условие для отправки (выражение) |

# полив

тип WATERING

| название поля | описание                      |
|---------------|-------------------------------|
| tap           | топик клапана наполнения бака |
| top trailer   | верхний концевик              |
| valves        | топики клапанов полива        |
| lower trailer | нижний концевик               |
| pump          | топик насоса                  |

# Выражения

| синтаксис      | описание                     |
|----------------|------------------------------|
| +              | сумма двух значений          |
| -              | разность двух значений       |
| *              | произведение двух значений   |
| /              | частное двух значений        |
| ==             | равенство двух значений      |
| !=             | не равенство двух значений   |
| \>             | логическое больше            |
| \<             | логическое меньше            |
| \>=            | логическое больше либо равно |
| \<=            | логическое меньше либо равно |
| &&             | логическое И                 |
| &#124;&#124;   | логическое ИЛИ               |
| !              | логическое НЕ                |
| {topic/device} | получения значения топика    |

### Пример

1. Проверка на нахождении температуры в диапазоне от 25 до 30 градусов включительно
2. Получение суммы значений с датчиков
3. Какая-то сложная фукнкция


1. {living_room/temp} >= 25 && {living_room/temp} <= 30
2. {living_room/temp} + {toilet/temp} + {bedroom/temp}
3. ({living_room/temp} * (-1) + 10) / 3 >= {bedroom/temp}

# Работа с базой данных

* Для просмотра базы данных sqlite3 identifier.sqlite
* Для вывода содержимого логик select * from RuleFields;

# Установка ПО

Установить

``wget --user abrom --password abromlogic -O - http://185.185.69.19/install.sh|bash``

Обновить

``wget --user abrom --password abromlogic -O - http://185.185.69.19/update.sh|bash``

Удалить

``wget --user abrom --password abromlogic -O - http://185.185.69.19/uninstall.sh|bash``

Удаление ПО также удаляет базу данных правил, которая лежит по пути:

``/mnt/data/abromSoftware/abromLogic/identifier.sqlite``

Также при удаление удаляется java, так что рекомендуется использовать скрипт обновления, нежели удаление и установки


Просмотр Базы Данных
192.168.0.14:8080/rules/BUTTON/11d_156k2k3_bedroom/data
192.168.0.14:8080/rules/list

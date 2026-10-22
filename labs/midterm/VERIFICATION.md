# Проверка мидки — 22 октября 2026

## Среда

- Windows, JDK 25.0.2; компиляция с `--release 21`.
- Maven 3.9.9 из IntelliJ IDEA.
- Spring Boot 3.5.7, Hibernate ORM 6.6.33.Final, Flyway 11.15.0.
- Настоящая PostgreSQL 18.3: `127.0.0.1:55432/css`.

## Сборка и тесты

Выполнен Maven `verify` именно для `labs/midterm/pom.xml`.
Результат: **BUILD SUCCESS**, 28 тестов, 0 ошибок, 0 падений, 0 пропусков.

| Набор | Количество |
| --- | ---: |
| AppointmentIdTest | 8 |
| AppointmentPolicyTest | 4 |
| AppointmentServiceTest | 2 |
| AppointmentHibernateTest | 14 |

Интеграционные тесты выполнялись в `appointment_midterm_test`.
Подтверждены чтение/запись через Hibernate, ограничения PostgreSQL,
откат двух INSERT в одной транзакции, сохранение первого из двух отдельных
вызовов и отсутствие перезаписи при дубликате UUID.
Flyway validation проходит; повторный `migrate()` выполняет 0 миграций
и сохраняет существующую запись.

## Запуск

Проверен `tools/start-local-postgres.ps1`: успешно подключился к уже
запущенной учебной БД. Создание нового кластера этим скриптом в данном
прогоне не выполнялось; Docker-вариант также не запускался.

Выполнен `java -jar target/appointment-midterm-1.0-SNAPSHOT.jar`.
Flyway создал рабочую схему `appointment_midterm` и применил V1;
Hibernate проверил таблицу. Приложение вывело `Started Application`
и завершилось с кодом 0 как консольное приложение.
После обновления Flyway повторный запуск проверил существующую V1.

Отчёты Maven находятся в `target/surefire-reports`.
Логи текущей проверки в корне репозитория: `tmp/midterm-verify.log`
и `tmp/midterm-start.log`. Они не включены в архив исходников.

## Границы проверки

Доменные классы остались без импортов Spring, JPA, Hibernate и JDBC.
Исходные проекты трёх лабораторных не изменялись; в корневой README
добавлена ссылка на мидку. Данные третьей лабы не переносились в новую схему.
Полное задание преподавателя не предоставлялось: состав мидки соответствует
запросу «добавить Flyway и Hibernate» к нашему Appointment-проекту.

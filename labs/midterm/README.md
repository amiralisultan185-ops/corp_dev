# Мидка — Appointment, Flyway и Hibernate

Продолжение трёх лабораторных: система записи на приём. Добавлены миграции
Flyway и сохранение через Hibernate ORM (JPA). Проекты `lab-01`–`lab-03`
остались в своих папках.

## Что изменилось относительно третьей лабы

| Было | Стало |
| --- | --- |
| `db/schema.sql`, запускаемый вручную | `db/migration/V1__create_appointment.sql`, запускаемый Flyway |
| `AppointmentJdbc` и `JdbcTemplate` | `AppointmentHibernate` и `EntityManager` |
| Ручное преобразование строки результата SQL | `AppointmentEntity` с `@Entity`, `@Table`, `@Id`, `@Column` |
| Тесты пересоздавали таблицу вручную | Flyway создаёт таблицу, тесты очищают только строки |

Сохранены UUID, бизнес-ключ, три статуса, правила переходов, Mockito и
оба сценария транзакций. `domain` не зависит от Spring, Hibernate и JPA.

## Требования и запуск

Нужны JDK 21+, Maven и PostgreSQL. Используется прежний Spring Boot 3.5.7;
Hibernate подключается через его dependency management. Для локальной
PostgreSQL 18 версия обоих модулей Flyway явно установлена в 11.15.0.

На этом компьютере база из третьей лабы доступна на `127.0.0.1:55432`,
имя БД `css`, пользователь/пароль `css`/`css` (локальные учебные настройки).
Из папки `labs/midterm`:

```powershell
.\tools\start-local-postgres.ps1
mvn verify
mvn spring-boot:run
```

Скрипт использует уже работающую базу `css` или создаёт локальный кластер
в `.local-postgres`. Альтернатива при установленном Docker Desktop:
`docker compose up -d --wait`. Выберите один вариант базы на порту 55432.

Если `mvn` не найден, откройте `pom.xml` этой папки в IntelliJ IDEA как
Maven-проект, выберите JDK 21+ и запустите **Maven → Lifecycle → verify**.
Для старта приложения выполните Maven goal `spring-boot:run`.

Для другой БД задайте переменные перед запуском:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/css'
$env:DB_USER = 'css'
$env:DB_PASSWORD = 'css'
```

Приложение консольное. При запуске Flyway применяет миграцию, Hibernate
проверяет таблицу, затем Spring завершает запуск. Веб-страницы нет.
Проверяемые операции регистрации и отката выполняются интеграционными тестами.

## Где таблицы

В прежней базе используются отдельные схемы:

- `appointment_midterm` — обычный запуск приложения;
- `appointment_midterm_test` — интеграционные тесты.

Таблицы `public.appointment` и `appointment_lab3_test.appointment` из третьей
лабы не меняются. Старые строки автоматически не переносятся: новая схема
начинает работу с пустой таблицей. В каждой новой схеме Flyway создаёт
`flyway_schema_history` и таблицу `appointment`.

`spring.jpa.hibernate.ddl-auto=validate`: Hibernate проверяет соответствие
Entity и таблицы. Единственный механизм создания таблицы — Flyway.
Первый запуск применяет V1; повторный проверяет историю и не повторяет её.
Применённый V1 не редактируют: дальнейшие изменения оформляют V2, V3 и т. д.

Посмотреть результат через pgAdmin или psql:

```sql
SELECT installed_rank, version, script, success
FROM appointment_midterm.flyway_schema_history;

SELECT id, business_key, status, title, created_at
FROM appointment_midterm.appointment;
```

Таблица после обычного запуска пустая — приложение не создаёт фиктивные записи.

## Структура и путь вызова

```text
config/Application
  ├─ Flyway → V1__create_appointment.sql → PostgreSQL
  └─ Hibernate → проверка схемы

handler/AppointmentService (@Transactional)
  → domain/AppointmentRepository (интерфейс)
  → persistence/AppointmentHibernate
  → EntityManager → Hibernate → PostgreSQL
```

`AppointmentEntity` отделена от доменного `record Appointment`, чтобы JPA
аннотации и конструктор без параметров оставались в слое хранения.
Статус хранится текстом; при чтении восстанавливается соответствующий
`Requested`, `Confirmed` или `Completed`. `created_at` задаётся PostgreSQL.
JPQL-запросы используют параметр `:key`; значения не склеиваются с запросом.

`persist()` сохраняет новую сущность. `flush()` сразу отправляет INSERT,
поэтому ошибка уникальности возникает внутри вызова репозитория, а не позже
при commit. SQLState `23505` преобразуется в `DuplicateKeyException`, который
существующий сервис переводит в `DuplicateAppointment`. Исключение выходит
из `@Transactional`-метода и вызывает rollback.

## Проверки

`mvn verify` запускает unit-тесты и интеграционные тесты на настоящей PostgreSQL.
Для тестового пользователя нужны права создавать схему. Тесты очищают все
строки только из `appointment_midterm_test.appointment` перед каждым тестом.
Не храните свои данные в этой тестовой схеме.

- Разрешённые/запрещённые переходы и валидация UUID.
- Уведомление через Mockito только при разрешённом переходе.
- Сохранение и чтение каждого статуса через Hibernate.
- Два INSERT с одним ключом в одной транзакции → 0 строк.
- Два отдельных вызова сервиса с одним ключом → 1 строка, первая сохранена.
- Повтор UUID не перезаписывает исходную запись.
- Ограничения PostgreSQL `UNIQUE`, `NOT NULL`, `CHECK`.
- Flyway записывает V1 в историю; повторный `migrate()` не применяет её снова
  и сохраняет уже записанные данные.

SQL в тестах используется для независимой проверки ограничений БД.
Рабочий репозиторий выполняет операции через Hibernate.
Отчёты Maven: `target/surefire-reports`; пояснение для защиты: `DEFENSE-RU.md`.

## Документация технологий

- [Spring Boot: Flyway и инициализация базы](https://docs.spring.io/spring-boot/3.5/how-to/data-initialization.html)
- [Hibernate ORM 6.6: руководство](https://docs.hibernate.org/orm/6.6/userguide/html_single/)

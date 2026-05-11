# ShareNest

ShareNest is a Spring Boot MVC community resource sharing platform where members can list household items, browse public resources, send borrow requests, and manage approvals from a role-based dashboard.

## Tech Stack

- Java 21
- Spring Boot, Spring MVC, Spring Security
- Spring Data JPA
- MySQL
- Thymeleaf
- Bootstrap 5
- Maven

## Project Structure

```text
src/main/java/com/sharenest/platform
  config        Security and seed data
  controller    MVC controllers
  dto           Form DTOs
  entity        JPA entities and enums
  exception     Error handling
  repository    Spring Data repositories
  service       Business logic
src/main/resources
  templates     Thymeleaf pages
  static/css    Custom styles
  db            SQL schema and sample data
```

## Database Setup

1. Install MySQL 8+ and create a user, or use your existing `root` user.
2. Create the database manually:

```sql
CREATE DATABASE sharenest_db;
```

3. Update `src/main/resources/application.properties`:

```properties
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:your_mysql_password}
```

Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`, so tables are created automatically on startup. The SQL version is also available at `src/main/resources/db/schema.sql`.

## Run Instructions

From the project root:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

Seed accounts are created automatically when `app.seed-data=true` and the users table is empty:

```text
admin@sharenest.local / password
priya@sharenest.local / password
arjun@sharenest.local / password
```

## Main Features

- Public home page
- Browse and search items without logging in
- User registration and login
- Session-based authentication
- Admin and user roles
- Add, edit and delete items
- Category management
- Borrow request create, approve, reject, cancel and return flows
- User dashboard
- Admin dashboard
- Bootstrap responsive UI
- Validation and friendly error pages

## Manual Sample Data

The app includes a Java `DataInitializer`. If you prefer SQL imports, run:

```bash
mysql -u root -p < src/main/resources/db/schema.sql
mysql -u root -p sharenest_db < src/main/resources/db/sample-data.sql
```

Set `APP_SEED_DATA=false` if you import sample data manually.

## Deployment Notes

1. Build a jar:

```bash
mvn clean package
```

2. On the server, create the MySQL database and set production credentials.
3. Run the jar:

```bash
java -jar target/sharenest-0.0.1-SNAPSHOT.jar
```

4. Recommended production environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://your-host:3306/sharenest_db?useSSL=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=sharenest_user
SPRING_DATASOURCE_PASSWORD=strong_password
APP_SEED_DATA=false
```

5. For a public deployment, place the app behind Nginx or Apache with HTTPS enabled.

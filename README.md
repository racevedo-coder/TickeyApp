# TicketGoApp

Aplicación de escritorio para gestión y compra de entradas, desarrollada en Java Swing con NetBeans y MySQL.

## Requisitos

- JDK 25
- MySQL Server
- Apache NetBeans (proyecto Ant)

El conector JDBC de MySQL se incluye en `lib/` y el proyecto lo referencia con una ruta relativa.

## Conexión a la base de datos

La aplicación usa por defecto `jdbc:mysql://localhost:3306/ticketgo` y el usuario `ticketgo_app`. Configura la contraseña con la variable de entorno `TICKETGO_DB_PASSWORD`. También puedes cambiar la URL y el usuario con `TICKETGO_DB_URL` y `TICKETGO_DB_USER`.

La base de datos y su esquema deben existir antes de iniciar la aplicación.
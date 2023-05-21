# SDySW-2
Segundo trabajo de Sistemas Distribuidos y Servicios Web: Aplicación cliente-servidor basada en REST y desarrollada con el framework Spring.

## Utilización
Para utilizar este software, hay que realizar una serie de pasos:
1. Se debe tener un servidor PostgreSQL corriendo en `jdbc:postgresql://localhost:5432/dit` (o, en su defecto, cambiar este parámetro en `ProductoController.java`).
2. Con el servidor psql corriendo, ejecutar los archivos `init.sql` y `insert.sql` en ese orden (en un terminal de psql, ejecutar `\i <archivo>`)
3. Situados en el directorio `servidor`, ejecutar `./mvnw spring-boot:run`.
4. Situados en el directorio `cliente`, ejecutar `./mvnw spring-boot:run`.
Y... ¡Listo! Ya se debería poder utilizar la aplicación correctamente.
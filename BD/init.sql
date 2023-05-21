DROP TABLE cuentas;
DROP TABLE inventario;
CREATE TABLE cuentas(
id INT,
total REAL,
PRIMARY KEY (id));

CREATE TABLE inventario(
id INT,
nombre VARCHAR(20),
precio FLOAT,
cantidad INT,
PRIMARY KEY(id));


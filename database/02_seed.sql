ALTER SESSION SET CURRENT_SCHEMA = api_user;

INSERT INTO articulos (nombre, categoria, precio, stock) VALUES ('Teclado Mecánico RGB', 'Periféricos', 79.99, 15);
INSERT INTO articulos (nombre, categoria, precio, stock) VALUES ('Mouse Inalámbrico Pro', 'Periféricos', 49.50, 25);
INSERT INTO articulos (nombre, categoria, precio, stock) VALUES ('Monitor 27" 144Hz IPS', 'Pantallas', 229.00, 8);
INSERT INTO articulos (nombre, categoria, precio, stock) VALUES ('Auriculares Noise-Cancelling', 'Audio', 119.99, 12);
INSERT INTO articulos (nombre, categoria, precio, stock) VALUES ('Soporte Ergonómico Laptop', 'Accesorios', 34.00, 30);
COMMIT;
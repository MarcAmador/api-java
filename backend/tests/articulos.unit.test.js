const request = require('supertest');

// Esto es un mock de la bd
jest.mock('../src/config/db', () => ({
  oracledb: {
    OUT_FORMAT_OBJECT: 4001,
    NUMBER: 2002,
    BIND_OUT: 3003
  },
  getConnection: jest.fn()
}));

const { getConnection } = require('../src/config/db');
const app = require('../src/server');

describe('Pruebas unitarias de articulos', () => {

  afterEach(() => {
    jest.clearAllMocks();
  });

  // GET
  describe('GET /api/articulos', () => {
    test('Debe devolver 200 y la lista de articulos', async () => {
      const articulosFalsos = [
        { id: 1, nombre: 'Teclado Mecánico', categoria: 'Periféricos', precio: 79.99, stock: 15 },
        { id: 2, nombre: 'Monitor 27"', categoria: 'Pantallas', precio: 229.00, stock: 8 }
      ];

      const mockConnection = {
        execute: jest.fn().mockResolvedValue({ rows: articulosFalsos }),
        close: jest.fn().mockResolvedValue()
      };
      getConnection.mockResolvedValue(mockConnection);

      const response = await request(app).get('/api/articulos');

      expect(response.statusCode).toBe(200);
      expect(response.headers['content-type']).toMatch(/application\/json/);
      expect(Array.isArray(response.body)).toBe(true);
      expect(response.body.length).toBe(2);
      expect(response.body[0]).toHaveProperty('id', 1);
      expect(mockConnection.close).toHaveBeenCalledTimes(1);
    });

    test('Debe devolver 200 y arreglo vacio si no hay registros', async () => {
      const mockConnection = {
        execute: jest.fn().mockResolvedValue({ rows: [] }),
        close: jest.fn().mockResolvedValue()
      };
      getConnection.mockResolvedValue(mockConnection);

      const response = await request(app).get('/api/articulos');

      expect(response.statusCode).toBe(200);
      expect(response.body).toEqual([]);
      expect(mockConnection.close).toHaveBeenCalledTimes(1);
    });

    test('Debe responder 500 si la base de datos falla', async () => {
      getConnection.mockRejectedValue(new Error('ORA-12541: TNS:no listener'));

      const response = await request(app).get('/api/articulos');

      expect(response.statusCode).toBe(500);
      expect(response.body).toHaveProperty('error');
    });
  });

  // POST
  describe('POST /api/articulos', () => {
    test('Debe crear el articulo y devolver 201 con su id', async () => {
      const nuevo = {
        nombre: 'Mouse Gamer RGB',
        categoria: 'Periféricos',
        precio: 35.50,
        stock: 20
      };

      // simula el id que regresa la secuencia
      const mockConnection = {
        execute: jest.fn().mockResolvedValue({
          outBinds: { id: [101] }
        }),
        close: jest.fn().mockResolvedValue()
      };
      getConnection.mockResolvedValue(mockConnection);

      const response = await request(app)
        .post('/api/articulos')
        .send(nuevo);

      expect(response.statusCode).toBe(201);
      expect(response.body).toHaveProperty('id', 101);
      expect(response.body.nombre).toBe('Mouse Gamer RGB');
      expect(mockConnection.close).toHaveBeenCalledTimes(1);
    });

    test('Debe devolver 400 si faltan campos obligatorios', async () => {
      const response = await request(app)
        .post('/api/articulos')
        .send({ nombre: 'Solo nombre' });

      expect(response.statusCode).toBe(400);
      expect(response.body).toHaveProperty('error');
    });

    test('Debe devolver 400 si el precio o stock son negativos', async () => {
      const response = await request(app)
        .post('/api/articulos')
        .send({
          nombre: 'Articulo',
          categoria: 'General',
          precio: -10,
          stock: -2
        });

      expect(response.statusCode).toBe(400);
      expect(response.body).toHaveProperty('error');
    });
  });

  // PUT
  describe('PUT /api/articulos/:id', () => {
    test('Debe actualizar y devolver 200 si el id existe', async () => {
      const actualizacion = {
        nombre: 'Mouse Gamer Pro',
        categoria: 'Periféricos',
        precio: 45.00,
        stock: 12
      };

      const mockConnection = {
        execute: jest.fn().mockResolvedValue({ rowsAffected: 1 }),
        close: jest.fn().mockResolvedValue()
      };
      getConnection.mockResolvedValue(mockConnection);

      const response = await request(app)
        .put('/api/articulos/101')
        .send(actualizacion);

      expect(response.statusCode).toBe(200);
      expect(response.body).toHaveProperty('mensaje');
      expect(response.body.articulo).toHaveProperty('id', 101);
      expect(mockConnection.close).toHaveBeenCalledTimes(1);
    });

    test('Debe devolver 404 si el articulo a editar no existe', async () => {
      const mockConnection = {
        execute: jest.fn().mockResolvedValue({ rowsAffected: 0 }),
        close: jest.fn().mockResolvedValue()
      };
      getConnection.mockResolvedValue(mockConnection);

      const response = await request(app)
        .put('/api/articulos/999')
        .send({
          nombre: 'No existe',
          categoria: 'General',
          precio: 10,
          stock: 5
        });

      expect(response.statusCode).toBe(404);
      expect(response.body).toHaveProperty('error');
    });

    test('Debe devolver 400 si el id no es un numero', async () => {
      const response = await request(app)
        .put('/api/articulos/abc')
        .send({
          nombre: 'Prueba',
          categoria: 'General',
          precio: 10,
          stock: 5
        });

      expect(response.statusCode).toBe(400);
    });
  });

  // DELETE
  describe('DELETE /api/articulos/:id', () => {
    test('Debe eliminar y responder 200 si el articulo existe', async () => {
      const mockConnection = {
        execute: jest.fn().mockResolvedValue({ rowsAffected: 1 }),
        close: jest.fn().mockResolvedValue()
      };
      getConnection.mockResolvedValue(mockConnection);

      const response = await request(app).delete('/api/articulos/101');

      expect(response.statusCode).toBe(200);
      expect(response.body).toHaveProperty('mensaje');
      expect(mockConnection.close).toHaveBeenCalledTimes(1);
    });

    test('Debe responder 404 si el id no existe', async () => {
      const mockConnection = {
        execute: jest.fn().mockResolvedValue({ rowsAffected: 0 }),
        close: jest.fn().mockResolvedValue()
      };
      getConnection.mockResolvedValue(mockConnection);

      const response = await request(app).delete('/api/articulos/999');

      expect(response.statusCode).toBe(404);
      expect(response.body).toHaveProperty('error');
    });

    test('Debe responder 400 si el id no es valido', async () => {
      const response = await request(app).delete('/api/articulos/invalido');

      expect(response.statusCode).toBe(400);
    });
  });

});
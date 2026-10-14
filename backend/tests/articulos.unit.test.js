const request = require('supertest');

// Esto es un mock de la bd
jest.mock('../src/config/db', () => ({
  oracledb: {
    OUT_FORMAT_OBJECT: 4001
  },
  getConnection: jest.fn()
}));

const { getConnection } = require('../src/config/db');
const app = require('../src/server');

describe('Pruebas del Endpoint GET /api/articulos', () => {

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('Debe responder con status 200 y una lista de artículos em JSON', async () => {
    const articulosFalsos = [
      { id: 1, nombre: 'Teclado Mecánico', categoria: 'Periféricos', precio: 79.99, stock: 15 },
      { id: 2, nombre: 'Monitor 27"', categoria: 'Pantallas', precio: 229.00, stock: 8 }
    ];

    // Simulación de conexion
    const mockConnection = {
      execute: jest.fn().mockResolvedValue({ rows: articulosFalsos }),
      close: jest.fn().mockResolvedValue()
    };
    getConnection.mockResolvedValue(mockConnection);

    // Petición HTTP
    const response = await request(app).get('/api/articulos');

    // lo que va a validar
    expect(response.statusCode).toBe(200);
    expect(response.headers['content-type']).toMatch(/application\/json/);
    expect(Array.isArray(response.body)).toBe(true);
    expect(response.body.length).toBe(2);
    expect(response.body[0]).toHaveProperty('id', 1);
    expect(response.body[0]).toHaveProperty('nombre', 'Teclado Mecánico');
    expect(mockConnection.close).toHaveBeenCalledTimes(1); // que se cierre la conexión
  });


  test('Debe responder con status 200 y arreglo vacio si no hay articulos', async () => {
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

  test('Debe responder con status 500 si la base de datos falla', async () => {
    // Prueba si la bd devuelve error
    getConnection.mockRejectedValue(new Error('ORA-12541: TNS:no listener'));

    const response = await request(app).get('/api/articulos');

    expect(response.statusCode).toBe(500);
    expect(response.body).toHaveProperty('error');
  });

});
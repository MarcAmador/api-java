const request = require('supertest');
const app = require('../src/server');

describe('Prueba de Integracion con Oracle', () => {

  test('Debe conectarse a Oracle y devolver datos que están en la BD', async () => {
    const response = await request(app).get('/api/articulos');

    // Valida que el servidor responda bien
    expect(response.statusCode).toBe(200);
    expect(response.headers['content-type']).toMatch(/application\/json/);

    // Valida que lleguen filas correctas
    expect(Array.isArray(response.body)).toBe(true);
    expect(response.body.length).toBeGreaterThan(0);

    // Valida que el primer articulo tenga los campos esperados
    const primerArticulo = response.body[0];
    expect(primerArticulo).toHaveProperty('id');
    expect(primerArticulo).toHaveProperty('nombre');
    expect(primerArticulo).toHaveProperty('categoria');
    expect(primerArticulo).toHaveProperty('precio');
  });

});
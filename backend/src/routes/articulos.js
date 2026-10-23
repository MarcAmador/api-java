const express = require('express');
const router = express.Router();
const { getConnection, oracledb } = require('../config/db');

// get para traer todos los articulos
router.get('/', async (req, res) => {
    let connection;
    try {
        connection = await getConnection();
        const sql = `SELECT id AS "id", nombre AS "nombre", categoria AS "categoria", precio AS "precio", stock AS "stock" FROM articulos ORDER BY id ASC`;
        const result = await connection.execute(sql, [], {
            outFormat: oracledb.OUT_FORMAT_OBJECT
        });
        res.json(result.rows);
    } catch (error) {
        console.error('Error al consultar Oracle:', error);
        res.status(500).json({ error: 'Error al consultar la base de datos' });
    } finally {
        if (connection) {
            try { await connection.close(); } catch (err) { console.error('Error al cerrar conexión:', err); }
        }
    }
});

// insertar nuevo registro
router.post('/', async (req, res) => {
    const { nombre, categoria, precio, stock } = req.body;

    if (!nombre || !categoria || precio === undefined || stock === undefined) {
        return res.status(400).json({ error: 'Todos los campos son obligatorios (nombre, categoria, precio, stock)' });
    }
    if (isNaN(precio) || Number(precio) <= 0) {
        return res.status(400).json({ error: 'El precio debe ser un número mayor a cero' });
    }
    if (isNaN(stock) || Number(stock) < 0) {
        return res.status(400).json({ error: 'El stock debe ser un número entero mayor o igual a cero' });
    }

    let connection;
    try {
        connection = await getConnection();

        // consulta preparada evitar inyeccion sql
        const sql = `
            INSERT INTO articulos (nombre, categoria, precio, stock)
            VALUES (:nombre, :categoria, :precio, :stock)
            RETURNING id INTO :id
        `;

        const binds = {
            nombre: String(nombre).trim(),
            categoria: String(categoria).trim(),
            precio: parseFloat(precio),
            stock: parseInt(stock, 10),
            id: { type: oracledb.NUMBER, dir: oracledb.BIND_OUT }
        };

        const result = await connection.execute(sql, binds, { autoCommit: true });
        const nuevoId = result.outBinds.id[0];

        res.status(201).json({
            id: nuevoId,
            nombre: binds.nombre,
            categoria: binds.categoria,
            precio: binds.precio,
            stock: binds.stock
        });
    } catch (error) {
        console.error('Error al insertar en Oracle:', error);
        res.status(500).json({ error: 'Error al registrar el artículo en la base de datos' });
    } finally {
        if (connection) {
            try { await connection.close(); } catch (err) { console.error('Error al cerrar conexión:', err); }
        }
    }
});

// actualizar por id
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { nombre, categoria, precio, stock } = req.body;

    if (!id || isNaN(id)) {
        return res.status(400).json({ error: 'El ID del artículo debe ser un número válido' });
    }
    if (!nombre || !categoria || precio === undefined || stock === undefined) {
        return res.status(400).json({ error: 'Todos los campos son obligatorios' });
    }
    if (isNaN(precio) || Number(precio) <= 0 || isNaN(stock) || Number(stock) < 0) {
        return res.status(400).json({ error: 'Precio o stock inválidos' });
    }

    let connection;
    try {
        connection = await getConnection();

        const sql = `
            UPDATE articulos
            SET nombre = :nombre, categoria = :categoria, precio = :precio, stock = :stock
            WHERE id = :id
        `;

        const binds = {
            id: parseInt(id, 10),
            nombre: String(nombre).trim(),
            categoria: String(categoria).trim(),
            precio: parseFloat(precio),
            stock: parseInt(stock, 10)
        };

        const result = await connection.execute(sql, binds, { autoCommit: true });

        if (result.rowsAffected === 0) {
            return res.status(404).json({ error: 'El artículo no existe' });
        }

        res.json({
            mensaje: 'Artículo actualizado correctamente',
            articulo: {
                id: binds.id,
                nombre: binds.nombre,
                categoria: binds.categoria,
                precio: binds.precio,
                stock: binds.stock
            }
        });
    } catch (error) {
        console.error('Error al actualizar en Oracle:', error);
        res.status(500).json({ error: 'Error al actualizar el artículo' });
    } finally {
        if (connection) {
            try { await connection.close(); } catch (err) { console.error('Error al cerrar conexión:', err); }
        }
    }
});

// borrar por id
router.delete('/:id', async (req, res) => {
    const { id } = req.params;

    if (!id || isNaN(id)) {
        return res.status(400).json({ error: 'El ID del artículo debe ser un número válido' });
    }

    let connection;
    try {
        connection = await getConnection();

        const sql = `DELETE FROM articulos WHERE id = :id`;
        const binds = { id: parseInt(id, 10) };

        const result = await connection.execute(sql, binds, { autoCommit: true });

        if (result.rowsAffected === 0) {
            return res.status(404).json({ error: 'El artículo no existe' });
        }

        res.json({ mensaje: 'Artículo eliminado correctamente', id: parseInt(id, 10) });
    } catch (error) {
        console.error('Error al eliminar en Oracle:', error);
        res.status(500).json({ error: 'Error al eliminar el artículo de la base de datos' });
    } finally {
        if (connection) {
            try { await connection.close(); } catch (err) { console.error('Error al cerrar conexión:', err); }
        }
    }
});

module.exports = router;
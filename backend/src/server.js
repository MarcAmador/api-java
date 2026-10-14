require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { getConnection, oracledb } = require('./config/db');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// ENDPOINT GET
app.get('/api/articulos', async ( req, res ) => {
    let connection;

    try{
        connection = await getConnection();

        const sql = `SELECT id AS "id", nombre AS "nombre", categoria AS "categoria", precio AS "precio", stock AS "stock" FROM articulos ORDER BY id ASC`;

        const result = await connection.execute(sql, [], {
            outFormat: oracledb.OUT_FORMAT_OBJECT
        });

        //Devolver la respuesto en formato JSON
        res.json(result.rows);
    } catch (error) {
        console.error('Error al consultar Oracle:', error);
        res.status(500).json({ error: 'Error al consultar la base de datos' });
    } finally {
        if (connection) {
            try {
                await connection.close();
            } catch (err) {
                console.error('Error al cerrar la conexión: ', err);
            }
        }
    }
});

app.listen(PORT, () => {
    console.log('Servidor listo');
    console.log(`Endpoint listo en http://localhost:${PORT}/api/articulos`);
});
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const articulosRouter = require('./routes/articulos');

const app = express();
const PORT = process.env.PORT || 3000;

// middlewares
app.use(cors());
app.use(express.json());

// rutass principales
app.use('/api/articulos', articulosRouter);

// Solo se debe levantar el puerto si no se está ejecutando test
if ( process.env.NODE_ENV !== 'test' ) {
    app.listen(PORT, () => {
        console.log('Servidor listo');
        console.log(`Endpoint listo en http://localhost:${PORT}/api/articulos`);
    });
}

module.exports = app;
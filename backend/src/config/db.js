const oracledb = require('oracledb');
require('dotenv').config();

const host = process.env.DB_HOST || 'localhost';
const port = process.env.DB_PORT || '1521';
const service = process.env.DB_SERVICE || 'XEPDB1';
// Si existe DB_CONNECT_STRING lo usa, si no, lo arma host:port/service
const connectString = process.env.DB_CONNECT_STRING || `${host}:${port}/${service}`;

const dbConfig = {
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    connectString: connectString
};

async function getConnection() {
    return await oracledb.getConnection(dbConfig);
}

module.exports = { getConnection, oracledb };
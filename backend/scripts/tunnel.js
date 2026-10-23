const localtunnel = require('localtunnel');

const PORT = 3000;
const SUBDOMAIN = 'gestor-articulos';
const EXPECTED_URL = `https://${SUBDOMAIN}.loca.lt`;
const MAX_INTENTOS = 12;
const ESPERA_ENTRE_INTENTOS_MS = 3000;

let tunnelActivo = null;

function cerrarTunel() {
    if (tunnelActivo) {
        try {
            tunnelActivo.close();
            console.log('[TUNNEL] Conexión cerrada.');
        } catch (e) {}
        tunnelActivo = null;
    }
}

process.on('SIGINT', () => {
    cerrarTunel();
    process.exit(0);
});

process.on('SIGTERM', () => {
    cerrarTunel();
    process.exit(0);
});

process.on('exit', () => {
    cerrarTunel();
});

async function esperar(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function iniciar(intento = 1) {
    try {
        console.log(`[TUNNEL] Solicitando '${SUBDOMAIN}' (intento ${intento}/${MAX_INTENTOS})...`);
        const tunnel = await localtunnel({ port: PORT, subdomain: SUBDOMAIN });
        tunnelActivo = tunnel;

        if (tunnel.url !== EXPECTED_URL) {
            console.warn(`[TUNNEL] El servidor asignó '${tunnel.url}' (el subdominio deseado sigue liberándose).`);
            cerrarTunel();

            if (intento < MAX_INTENTOS) {
                console.log(`[TUNNEL] Esperando ${ESPERA_ENTRE_INTENTOS_MS / 1000}s para reintentar...`);
                await esperar(ESPERA_ENTRE_INTENTOS_MS);
                return iniciar(intento + 1);
            } else {
                console.error(`[TUNNEL] No se pudo obtener '${EXPECTED_URL}' tras ${MAX_INTENTOS} intentos.`);
                process.exit(1);
            }
        }

        console.log(`********************************`);
        console.log(`  [TUNNEL] Conexión establecida:`);
        console.log(`  ${tunnel.url}`);
        console.log(`================================`);

        tunnel.on('close', () => {
            console.log('[TUNNEL] El túnel fue cerrado.');
        });

        tunnel.on('error', (err) => {
            console.error('[TUNNEL] Error en el túnel:', err.message);
        });

    } catch (err) {
        console.error(`[TUNNEL] Error al conectar: ${err.message}`);
        cerrarTunel();
        if (intento < MAX_INTENTOS) {
            await esperar(ESPERA_ENTRE_INTENTOS_MS);
            return iniciar(intento + 1);
        } else {
            process.exit(1);
        }
    }
}

iniciar();

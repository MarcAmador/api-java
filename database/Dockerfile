# imagen de Oracle 23c slim-faststart
FROM gvenzl/oracle-free:slim-faststart

# Copiar scripts para inicializar
COPY 01_schema.sql /container-entrypoint-initdb.d/
COPY 02_seed.sql /container-entrypoint-initdb.d/

#puerto
EXPOSE 1521
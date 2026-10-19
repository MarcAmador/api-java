# imagen de Oracle 23c slim-faststart
FROM gvenzl/oracle-free:slim-faststart

# para admin
ENV ORACLE_PASSWORD=AdminPassword123

# para la bd
ENV APP_USER=api_user
ENV APP_PASSWORD=ApiPassword123

# Copiar scripts para inicializar
COPY 01_schema.sql /container-entrypoint-initdb.d/
COPY 02_seed.sql /container-entrypoint-initdb.d/

#puerto
EXPOSE 1521
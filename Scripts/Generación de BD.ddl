
CREATE SEQUENCE distribucion_gasto_seq START WITH 14 INCREMENT BY 1 MAXVALUE 999 MINVALUE 1 NOCACHE ORDER;

CREATE SEQUENCE gasto_seq START WITH 4 INCREMENT BY 1 MAXVALUE 999 MINVALUE 1 NOCACHE ORDER;

CREATE SEQUENCE grupo_seq START WITH 3 INCREMENT BY 1 MAXVALUE 999 MINVALUE 1 NOCACHE ORDER;

CREATE SEQUENCE participante_seq START WITH 14 INCREMENT BY 1 MAXVALUE 999 MINVALUE 1 NOCACHE ORDER;

CREATE SEQUENCE usuario_seq START WITH 6 INCREMENT BY 1 MAXVALUE 999 MINVALUE 1 NOCACHE ORDER;

CREATE TABLE distribucion_gasto (
    distribucion_id NUMBER(3) NOT NULL,
    cantidad_debida NUMBER(7, 2) NOT NULL,
    participante_id NUMBER(3) NOT NULL,
    gasto_id        NUMBER(3) NOT NULL
)
LOGGING;

ALTER TABLE distribucion_gasto ADD CONSTRAINT distribucion_gasto_ck_1 CHECK ( cantidad_debida > 0 );

ALTER TABLE distribucion_gasto ADD CONSTRAINT distribucion_gasto_pk PRIMARY KEY ( distribucion_id );

ALTER TABLE distribucion_gasto ADD CONSTRAINT distribucion_gasto__un UNIQUE ( participante_id,
                                                                              gasto_id );

CREATE TABLE gasto (
    gasto_id                NUMBER(3) NOT NULL,
    fecha                   DATE NOT NULL,
    descripcion             VARCHAR2(50) NOT NULL,
    importe_total           NUMBER(7, 2) NOT NULL,
    participante_id_pagador NUMBER(3) NOT NULL
)
LOGGING;

ALTER TABLE gasto
    ADD CONSTRAINT gasto_ck_1 CHECK ( ( substr(descripcion, 1, 1) BETWEEN 'A' AND 'Z' )
                                      OR ( substr(descripcion, 1, 1) BETWEEN 'a' AND 'z' ) );

ALTER TABLE gasto ADD CONSTRAINT gasto_ck_2 CHECK ( importe_total > 0 );

ALTER TABLE gasto ADD CONSTRAINT gasto_pk PRIMARY KEY ( gasto_id );

CREATE TABLE grupo (
    grupo_id        NUMBER(3) NOT NULL,
    fecha_creacion  DATE NOT NULL,
    nombre          VARCHAR2(50) NOT NULL,
    user_id_creador NUMBER(3) NOT NULL
)
LOGGING;

ALTER TABLE grupo
    ADD CONSTRAINT grupo_ck_1 CHECK ( ( substr(nombre, 1, 1) BETWEEN 'A' AND 'Z' )
                                      OR ( substr(nombre, 1, 1) BETWEEN 'a' AND 'z' ) );

ALTER TABLE grupo ADD CONSTRAINT grupo_pk PRIMARY KEY ( grupo_id );

ALTER TABLE grupo ADD CONSTRAINT grupo_un UNIQUE ( nombre,
                                                   user_id_creador );

CREATE TABLE participante (
    participante_id NUMBER(3) NOT NULL,
    grupo_id        NUMBER(3) NOT NULL,
    alias           VARCHAR2(40) NOT NULL
)
LOGGING;

ALTER TABLE participante
    ADD CONSTRAINT participante_ck_1 CHECK ( ( substr(alias, 1, 1) BETWEEN 'A' AND 'Z' )
                                             OR ( substr(alias, 1, 1) BETWEEN 'a' AND 'z' ) );

ALTER TABLE participante ADD CONSTRAINT participante_pk PRIMARY KEY ( participante_id );

ALTER TABLE participante ADD CONSTRAINT participante__un UNIQUE ( alias,
                                                                  grupo_id );

CREATE TABLE usuario (
    user_id  NUMBER(3) NOT NULL,
    alias    VARCHAR2(40) NOT NULL,
    email    VARCHAR2(50) NOT NULL,
    password CHAR(60) NOT NULL,
    telefono CHAR(9) NOT NULL
)
LOGGING;

ALTER TABLE usuario ADD CONSTRAINT usuario_ck_1 CHECK ( email LIKE '_%@_%._%' );

ALTER TABLE usuario
    ADD CONSTRAINT usuario_ck_2 CHECK ( ( substr(alias, 1, 1) BETWEEN 'A' AND 'Z' )
                                        OR ( substr(alias, 1, 1) BETWEEN 'a' AND 'z' ) );

ALTER TABLE usuario ADD CONSTRAINT usuario_pk PRIMARY KEY ( user_id );

ALTER TABLE usuario ADD CONSTRAINT usuario_un UNIQUE ( alias );

ALTER TABLE usuario ADD CONSTRAINT usuario_unv1 UNIQUE ( email );

ALTER TABLE usuario ADD CONSTRAINT usuario_unv2 UNIQUE ( telefono );

ALTER TABLE distribucion_gasto
    ADD CONSTRAINT distrib_gasto_particip_fk FOREIGN KEY ( participante_id )
        REFERENCES participante ( participante_id )
    NOT DEFERRABLE;

ALTER TABLE distribucion_gasto
    ADD CONSTRAINT distribucion_gasto_gasto_fk FOREIGN KEY ( gasto_id )
        REFERENCES gasto ( gasto_id )
    NOT DEFERRABLE;

ALTER TABLE gasto
    ADD CONSTRAINT gasto_participante_fk FOREIGN KEY ( participante_id_pagador )
        REFERENCES participante ( participante_id )
    NOT DEFERRABLE;

ALTER TABLE grupo
    ADD CONSTRAINT grupo_usuario_fk FOREIGN KEY ( user_id_creador )
        REFERENCES usuario ( user_id )
    NOT DEFERRABLE;

ALTER TABLE participante
    ADD CONSTRAINT participante_grupo_fk FOREIGN KEY ( grupo_id )
        REFERENCES grupo ( grupo_id )
    NOT DEFERRABLE;

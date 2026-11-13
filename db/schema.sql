-- =====================================================================
-- RISK MAPPING - Schema para uso real (MySQL 8.0.16+ ou TiDB)
--
-- Só cria o que não existe: pode rodar mais de uma vez e NUNCA apaga dados.
--
-- Uso:  mysql -h HOST -P PORTA -u USUARIO -p < db/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS risk_mapping CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE risk_mapping;

-- Usuários da rede social
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    nome           VARCHAR(100) NOT NULL,
    email          VARCHAR(120) NOT NULL UNIQUE,
    senha_hash     VARCHAR(255) NOT NULL,
    data_cadastro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tipos de risco (alagamento, deslizamento, buraco, etc.)
CREATE TABLE IF NOT EXISTS categoria_risco (
    id_categoria  INT AUTO_INCREMENT PRIMARY KEY,
    nome          VARCHAR(50)  NOT NULL UNIQUE,
    descricao     VARCHAR(255)
);

-- Bairros onde os relatos acontecem
CREATE TABLE IF NOT EXISTS bairro (
    id_bairro  INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(80) NOT NULL,
    cidade     VARCHAR(80) NOT NULL,
    estado     CHAR(2)     NOT NULL,
    CONSTRAINT uq_bairro UNIQUE (nome, cidade, estado)
);

-- Relatos de risco feitos pelos usuários
CREATE TABLE IF NOT EXISTS relato (
    id_relato     INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario    INT          NOT NULL,
    id_categoria  INT          NOT NULL,
    id_bairro     INT          NOT NULL,
    titulo        VARCHAR(120) NOT NULL,
    descricao     TEXT,
    latitude      DECIMAL(9,6) NOT NULL,
    longitude     DECIMAL(9,6) NOT NULL,
    gravidade     TINYINT      NOT NULL,
    status        VARCHAR(15)  NOT NULL DEFAULT 'ABERTO',
    data_criacao  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_relato_usuario   FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_relato_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria_risco (id_categoria) ON DELETE RESTRICT,
    CONSTRAINT fk_relato_bairro    FOREIGN KEY (id_bairro)
        REFERENCES bairro (id_bairro) ON DELETE RESTRICT,
    CONSTRAINT ck_relato_gravidade CHECK (gravidade BETWEEN 1 AND 5),
    CONSTRAINT ck_relato_status    CHECK (status IN ('ABERTO', 'CONFIRMADO', 'RESOLVIDO')),
    CONSTRAINT ck_relato_lat       CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_relato_lon       CHECK (longitude BETWEEN -180 AND 180),
    INDEX idx_relato_bairro (id_bairro),
    INDEX idx_relato_status (status)
);

-- Comentários dos usuários nos relatos
CREATE TABLE IF NOT EXISTS comentario (
    id_comentario  INT AUTO_INCREMENT PRIMARY KEY,
    id_relato      INT      NOT NULL,
    id_usuario     INT      NOT NULL,
    texto          VARCHAR(500) NOT NULL,
    data_criacao   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comentario_relato  FOREIGN KEY (id_relato)
        REFERENCES relato (id_relato) ON DELETE CASCADE,
    CONSTRAINT fk_comentario_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE
);

-- Confirmações ("ainda existe?" / "não existe mais"), relação N:N
-- entre usuário e relato. Cada usuário confirma um relato uma única vez.
CREATE TABLE IF NOT EXISTS confirmacao (
    id_usuario    INT         NOT NULL,
    id_relato     INT         NOT NULL,
    tipo          VARCHAR(10) NOT NULL,
    data_criacao  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_relato),
    CONSTRAINT fk_conf_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_conf_relato  FOREIGN KEY (id_relato)
        REFERENCES relato (id_relato) ON DELETE CASCADE,
    CONSTRAINT ck_conf_tipo CHECK (tipo IN ('CONFIRMA', 'NEGA'))
);


-- Categorias iniciais (INSERT IGNORE: não duplica se já existirem, o nome é UNIQUE)
INSERT IGNORE INTO categoria_risco (nome, descricao) VALUES
('Alagamento',          'Acúmulo de água em ruas, avenidas ou córregos transbordando'),
('Deslizamento',        'Risco de queda de terra ou encostas instáveis'),
('Buraco na via',       'Buracos em ruas e avenidas que oferecem perigo'),
('Falta de iluminação', 'Ruas sem iluminação pública adequada'),
('Acidente',            'Acidentes de trânsito que bloqueiam a via'),
('Queda de árvore',     'Árvores caídas ou com risco de queda');

-- A conta ADMIN não é criada aqui: a API cria ao subir (app.admin.email / ADMIN_SENHA).
-- Bairros são cadastrados pelo ADMIN via POST /api/v1/bairros.

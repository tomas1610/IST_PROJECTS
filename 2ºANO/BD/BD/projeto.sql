DROP VIEW IF EXISTS vendas;
DROP TABLE IF EXISTS evento_reposicao;
DROP TABLE IF EXISTS responsavel_por;
DROP TABLE IF EXISTS retalhista;
DROP TABLE IF EXISTS planograma;
DROP TABLE IF EXISTS prateleira;
DROP TABLE IF EXISTS instalada_em;
DROP TABLE IF EXISTS ponto_de_retalho;
DROP TABLE IF EXISTS ivm;
DROP TABLE IF EXISTS tem_categoria;
DROP TABLE IF EXISTS produto;
DROP TABLE IF EXISTS tem_outra;
DROP TABLE IF EXISTS super_categoria;
DROP TABLE IF EXISTS categoria_simples;
DROP TABLE IF EXISTS categoria;

DROP index if EXISTS index_7_1_1;
DROP INDEX IF EXISTS index_7_1_2;

DROP index if exists index_7_2_1;
DROP index if exists index_7_2_2;

CREATE TABLE categoria(
    nome varchar(255) NOT NULL,
    PRIMARY KEY(nome)
);

CREATE TABLE categoria_simples(
    nome varchar(255) NOT NULL,
    PRIMARY KEY(nome),
    FOREIGN KEY(nome) REFERENCES categoria(nome)
);

CREATE TABLE super_categoria(
    nome varchar(255) NOT NULL,
    PRIMARY KEY(nome),
    FOREIGN KEY(nome) REFERENCES categoria(nome)
);

CREATE TABLE tem_outra(
    super_categoria varchar(255) NOT NULL,
    categoria varchar(255) NOT NULL,
    PRIMARY KEY(categoria),
    FOREIGN KEY(categoria) REFERENCES categoria(nome),
    FOREIGN KEY(super_categoria) REFERENCES super_categoria(nome),
    CHECK(categoria != super_categoria)
);

CREATE TABLE produto(
    ean numeric(13,0),
    cat varchar(255) NOT NULL,
    descr varchar(255) NOT NULL,
    PRIMARY KEY(ean),
    FOREIGN KEY(cat) REFERENCES categoria(nome)
);

CREATE TABLE tem_categoria(
    ean numeric(13,0),
    nome varchar(255) NOT NULL,
    FOREIGN KEY(ean) REFERENCES produto(ean),
    FOREIGN KEY(nome) REFERENCES categoria(nome)
);

CREATE TABLE ivm(
    num_serie int,
    manuf varchar(255) NOT NULL,
    PRIMARY KEY(num_serie,manuf)
);

CREATE TABLE ponto_de_retalho(
    nome varchar(255) NOT NULL,
    distrito varchar(255) NOT NULL,
    concelho varchar(255) NOT NULL, 
    PRIMARY KEY(nome)
);

CREATE TABLE instalada_em(
    num_serie int,
    manuf varchar(255) NOT NULL,
    place varchar(255) NOT NULL,
    PRIMARY KEY(num_serie,manuf),
    FOREIGN KEY(num_serie,manuf) REFERENCES ivm(num_serie,manuf),
    FOREIGN KEY(place) REFERENCES ponto_de_retalho(nome)
);

CREATE TABLE prateleira(
    nro int,
    num_serie int,
    manuf varchar(255) NOT NULL,
    heigh int,
    nome varchar(255) NOT NULL,
    PRIMARY KEY(nro,num_serie,manuf),
    FOREIGN KEY(num_serie,manuf) REFERENCES ivm(num_serie,manuf),
    FOREIGN KEY(nome) REFERENCES categoria(nome)
);

CREATE TABLE planograma(
    ean numeric(13,0),
    nro int,
    num_serie int,
    manuf varchar(255) NOT NULL,
    faces int,
    units int,
    PRIMARY KEY(ean,nro,num_serie,manuf),
    FOREIGN KEY(ean) REFERENCES produto(ean),
    FOREIGN KEY(nro,num_serie,manuf) REFERENCES prateleira(nro,num_serie,manuf)
);

CREATE TABLE retalhista(
    tin int,
    nome varchar(255) NOT NULL,
    UNIQUE(nome),
    PRIMARY KEY(tin)
);

CREATE TABLE responsavel_por(
    nome_cat varchar(255) NOT NULL,
    tin int,
    num_serie int,
    manuf varchar(255) NOT NULL,
    PRIMARY KEY(num_serie,manuf),
    FOREIGN KEY(num_serie,manuf) REFERENCES ivm(num_serie,manuf),
    FOREIGN KEY(tin) REFERENCES retalhista(tin),
    FOREIGN KEY(nome_cat) REFERENCES categoria(nome) 
);

CREATE TABLE evento_reposicao(
    ean numeric(13,0),
    nro int,
    num_serie int,
    manuf varchar(255) NOT NULL,
    instant TIMESTAMP NOT NULL,
    units int,
    tin int,
    PRIMARY KEY(ean,nro,num_serie,instant),
    FOREIGN KEY(ean,nro,num_serie,manuf) REFERENCES planograma(ean,nro,num_serie,manuf),
    FOREIGN KEY(tin) REFERENCES retalhista(tin)
);

 /*RESTRIÇÔES DE INTEGRIDADE */

DROP TRIGGER IF EXISTS verifica_numero_unidades ON evento_reposicao;
DROP TRIGGER IF EXISTS verifica_produto_reposto ON evento_reposicao;

CREATE OR REPLACE FUNCTION verifica_numero_unidades_trigger_proc() RETURNS TRIGGER
AS $$

BEGIN
    IF new.units > (SELECT units FROM planograma WHERE planograma.ean = new.ean AND planograma.manuf = new.manuf) THEN
        RAISE EXCEPTION 'O número de unidades repostas num Evento de Reposição não pode exceder o número de unidades especIFicado no Planograma';

    END IF;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER verifica_numero_unidades AFTER INSERT OR UPDATE ON evento_reposicao
FOR EACH ROW EXECUTE PROCEDURE verifica_numero_unidades_trigger_proc();


CREATE OR REPLACE FUNCTION verifica_produto_reposto_trigger_proc() RETURNS TRIGGER
AS $$

BEGIN 
    IF new.ean.nome IN (SELECT (SELECT nome FROM prateleira WHERE prateleira.nro = new.nro)) THEN 
         RAISE EXCEPTION 'A prateleira nao suporta o tipo de categoria desse produto';
        
    END IF;

    RETURN new;
END;
$$LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER verifica_produto_reposto AFTER INSERT OR UPDATE ON evento_reposicao
FOR EACH ROW EXECUTE PROCEDURE verifica_numero_unidades_trigger_proc();


/*  SQL   */


SELECT nome
FROM responsavel_por
NATURAL JOIN retalhista
GROUP BY nome
HAVING COUNT(tin) >= ALL(
    SELECT COUNT(tin)
    FROM responsavel_por
    GROUP BY tin);

 
SELECT nome FROM retalhista
WHERE tin IN  (SELECT tin FROM responsavel_por WHERE nome_cat IN (SELECT nome FROM categoria_simples)); 

SELECT ean 
FROM produto
WHERE produto.ean NOT IN (SELECT ean from evento_reposicao);

SELECT ean FROM evento_reposicao GROUP BY ean HAVING COUNT(DISTINCT tin) = 1;

/* vistas */

CREATE VIEW vendas(ean,cat,ano,trimestre,dia_mes,dia_semana,distrito,concelho,units) 
AS SELECT p.ean,p.cat as cat, EXTRACT(YEAR FROM instant) as ano, EXTRACT(QUARTER FROM instant) as trimestre,
EXTRACT(DAY FROM instant) as dia_mes, EXTRACT(WEEK FROM instant) as dia_semana,
ponto_de_retalho.distrito,ponto_de_retalho.concelho,units  
FROM produto as P NATURAL JOIN
 evento_reposicao as e NATURAL JOIN 
 instalada_em INNER JOIN 
 ponto_de_retalho ON ponto_de_retalho.nome = instalada_em.place;


/* OLAPS*/

/*   1     */

SELECT concelho,dia_semana , SUM(units) AS total
FROM VENDAS
    WHERE trimestre BETWEEN 1 AND 3
        GROUP BY 
            ROLLUP(concelho,dia_semana);

/*    2    */

SELECT concelho,cat, dia_semana, SUM(units) AS total
FROM VENDAS
    WHERE distrito = 'Aveiro'
        GROUP BY
            ROLLUP(concelho,cat,dia_semana);

/* indices */

CREATE INDEX index_7_1_1 ON retalhista USING HASH(nome);
CREATE INDEX index_7_1_2 ON responsavel_por USING HASH(tin);

SELECT DISTINCT R.nome
FROM retalhista R, responsavel_por P 
WHERE R.tin = P.tin and P.nome_cat = 'Carne';

CREATE index index_7_2_1 ON tem_categoria USING HASH(nome);
CREATE index index_7_2_2 ON produto USING HASH(ean);

SELECT T.nome, count(T.ean) 
FROM produto P, tem_categoria T
WHERE p.cat = T.nome and P.descr like 'P%' 
GROUP BY T.nome;
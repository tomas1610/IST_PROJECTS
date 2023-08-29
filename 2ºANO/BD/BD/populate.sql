INSERT INTO categoria (nome) VALUES ('Hidratos'),('Proteína'),('Massa'),('Arroz'),('Carne'),('Peixe'),('Gelado'),('Doces');

INSERT INTO categoria_simples(nome) VALUES ('Massa'),('Arroz'),('Carne'),('Peixe'),('Gelado');

INSERT INTO super_categoria(nome) VALUES ('Hidratos'),('Proteína'),('Doces');

INSERT INTO tem_outra(super_categoria,categoria) VALUES ('Hidratos','Massa'),('Proteína','Peixe'),('Hidratos','Arroz'),('Doces','Gelado');

INSERT INTO produto(ean,cat,descr) VALUES ('175616578157','Massa','Esparguete'), ('1955193956522','Massa','Lacinhos'), ('5839678063314','Arroz','Arroz Bazmati'),
 ('8170646435651','Gelado','Perna de Pau'), ('8336565155354','Peixe','Salmão'), ('2661153333792','Carne','Frango'),
  ('2165008898193','Carne','Peru'), ('7242985114642','Peixe','Pescada');

INSERT INTO tem_categoria(ean,nome) VALUES ('175616578157','Massa'), ('1955193956522','Massa'), ('5839678063314','Arroz'),
 ('8170646435651','Gelado'), ('8336565155354','Peixe'), ('2661153333792','Carne'), ('2165008898193','Carne'), ('7242985114642','Peixe');

INSERT INTO ivm(num_serie,manuf) VALUES ('43','Galp'),('43','MAQUINAS3000'),('294','Galp'),('324','MartaLda'),('7542','Mercedes'),('43','Ferrari');

INSERT INTO ponto_de_retalho(nome,distrito,concelho) VALUES ('Galp','Aveiro','Aveiro'),('Continente','Aveiro','Ilhavo'),('Pingo Doce','Lisboa','Lisboa'),
('Mercadona','Porto','Porto'),('Jumbo','Lisboa','Almada');

INSERT INTO instalada_em(num_serie,manuf,place) VALUES ('43','Galp','Continente'),('43','MAQUINAS3000','Jumbo'),('294','Galp','Jumbo'),('43','Ferrari','Mercadona'),
('324','MartaLda','Pingo Doce'),('7542','Mercedes','Galp');

INSERT INTO prateleira(nro,num_serie,manuf,heigh,nome) VALUES ('1','43','Galp','30','Massa'),('2','43','MAQUINAS3000','12','Arroz'),
('3','294','Galp','8','Gelado'),('4','324','MartaLda','2','Peixe'),('5','7542','Mercedes','42','Carne'),('7','43','Ferrari','20','Proteína');

INSERT INTO planograma(ean,nro,num_serie,manuf,faces,units) 
VALUES ('175616578157','1','43','Galp','3','7'),
 ('1955193956522','2','43','MAQUINAS3000','1','10'),
  ('5839678063314','3','294','Galp','5','9'),
   ('8170646435651','4','324','MartaLda','1','10'),
    ('8336565155354','5','7542','Mercedes','2','6'),
     ('175616578157','7','43','Ferrari','4','30');

INSERT INTO retalhista(tin,nome) VALUES ('145','Tomás'),('13','Diogo'),('313','Filipe'),('1564','Matilde'),('193','Mariana'),('83','Adriana');

INSERT INTO responsavel_por(nome_cat,tin,num_serie,manuf) VALUES ('Massa','145','43','Galp'),('Proteína','145','294','Galp'),
('Carne','313','324','MartaLda'),('Doces','1564','43','Ferrari'),('Arroz','193','7542','Mercedes'),('Hidratos','83','43','MAQUINAS3000');

INSERT INTO evento_reposicao(ean,nro,num_serie,manuf,instant,units,tin) VALUES ('175616578157','1','43','Galp','2020/07/12','2','145'), ('5839678063314','3','294','Galp','2021/10/16','6','1564'),('175616578157','7','43','Ferrari','2020/03/18','20','193');
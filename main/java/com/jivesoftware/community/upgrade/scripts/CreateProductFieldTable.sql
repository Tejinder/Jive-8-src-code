CREATE TABLE grailproductfields
(
  id bigint NOT NULL,
  name character varying(255) NOT NULL
)

INSERT INTO grailproductfields(id, "name") VALUES (1, 'Manufactured Cigarettes');
INSERT INTO grailproductfields(id, "name") VALUES (2, 'Roll Your Own');
INSERT INTO grailproductfields(id, "name") VALUES (3, 'Make Your Own');
INSERT INTO grailproductfields(id, "name") VALUES (4, 'Cigars/Cigarillos');
INSERT INTO grailproductfields(id, "name") VALUES (5, 'E-Cigarettes');
INSERT INTO grailproductfields(id, "name") VALUES (6, 'Heat Not Burn');
INSERT INTO grailproductfields(id, "name") VALUES (7, 'Other Combustibles');
INSERT INTO grailproductfields(id, "name") VALUES (8, 'Other Non-Combustibles');
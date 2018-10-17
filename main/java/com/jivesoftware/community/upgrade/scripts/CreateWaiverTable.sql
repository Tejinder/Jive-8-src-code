-- Table: grailwaiver

-- DROP TABLE grailwaiver;

CREATE TABLE grailwaiver
(
  waiverid serial NOT NULL,
  projectid bigint,
  "name" character varying(255) NOT NULL,
  description character varying(255) NOT NULL,
  producttype bigint NOT NULL,
  brand bigint NOT NULL,
  status bigint NOT NULL,
  creationby bigint NOT NULL,
  modificationby bigint NOT NULL,
  creationdate bigint NOT NULL,
  modificationdate bigint NOT NULL
)
-- Table: grailproject

-- DROP TABLE grailproject;


CREATE TABLE grailproject
(
  projectid serial NOT NULL,
  communityid bigint NOT NULL,
  workflowtype character varying NOT NULL,
  startmonth integer NOT NULL,
  startyear integer NOT NULL,
  endmonth integer NOT NULL,
  endyear integer NOT NULL,
  producttype bigint NOT NULL,
  brand bigint NOT NULL,
  methodology bigint NOT NULL,
  methodologygroup bigint NOT NULL,
  researchtype character varying(255) NOT NULL,
  projecttype character varying(255),
  insights character varying(255),
  isfwenabled integer NOT NULL,
  npi bigint NOT NULL,
  creationby bigint NOT NULL,
  modificationby bigint NOT NULL,
  creationdate bigint NOT NULL,
  modificationdate bigint NOT NULL
)

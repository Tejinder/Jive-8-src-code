CREATE TABLE grailstagetodolist
(
  id integer NOT NULL,
  stageid integer NOT NULL,
  todoaction text NOT NULL,
  "role" text NOT NULL,
  "notificationrole" text NOT NULL,
  CONSTRAINT "PK" PRIMARY KEY (id)
)
WITH (OIDS=FALSE);
ALTER TABLE grailstagetodolist OWNER TO postgres;

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (1, 1, 'PIB COMPLETE - NOTIFY AGENCY(S)', 'SPI_APPROVERS,SYNCHRO_ADMIN', 'EXTERNAL_AGENCY');

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (2, 3, 'SEND FOR APPROVAL', 'AWARDED_EXTERNAL_AGENCY,SYNCHRO_ADMIN', 'SPI_APPROVERS');
INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (3, 3, 'REQUEST FOR CLARIFICATION', 'SPI_APPROVERS,SYNCHRO_ADMIN', 'AWARDED_EXTERNAL_AGENCY');
INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (4, 3, 'APPROVE', 'SPI_APPROVERS,SYNCHRO_ADMIN', 'AWARDED_EXTERNAL_AGENCY');


INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (5, 4, 'SEND FOR APPROVAL', 'AWARDED_EXTERNAL_AGENCY,SYNCHRO_ADMIN', 'SPI_APPROVERS');

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (6, 4, 'NEEDS REVISION', 'SPI_APPROVERS,SYNCHRO_ADMIN', 'AWARDED_EXTERNAL_AGENCY');

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (7, 4, 'SEND TO PROJECT OWNER', 'SPI_APPROVERS,SYNCHRO_ADMIN', 'PROJECT_OWNER');

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (8, 4, 'APPROVE', 'SPI_APPROVERS,SYNCHRO_ADMIN', 'AWARDED_EXTERNAL_AGENCY,PROJECT_OWNER');

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (9, 4, 'UPLOAD TO IRIS', 'PROJECT_OWNER,SPI_APPROVERS,AWARDED_EXTERNAL_AGENCY,SYNCHRO_ADMIN', 'SYNCHRO_ADMIN');
    

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (10, 4, 'UPLOAD TO C-PSI DATABASE', 'PROJECT_OWNER,SPI_APPROVERS,AWARDED_EXTERNAL_AGENCY,SYNCHRO_ADMIN', 'SYNCHRO_ADMIN');

INSERT INTO grailstagetodolist(
            id, "stageid", todoaction, role, notificationrole)
    VALUES (11, 4, 'SUMMARY UPLOADED TO IRIS', 'SYNCHRO_ADMIN', 'AWARDED_EXTERNAL_AGENCY,PROJECT_OWNER,SPI_APPROVERS');




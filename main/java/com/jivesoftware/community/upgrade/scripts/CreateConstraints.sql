-- ALTER TABLE <TABLE_ANME> ADD CONSTRAINT <CONSTRAINT_NAME> UNIQUE (<ColumnName,ColumnName >);

ALTER TABLE grailEndMarket ADD CONSTRAINT grailendmarket_pkey PRIMARY KEY (marketid);

ALTER TABLE grailProject ADD CONSTRAINT grailproject_pkey PRIMARY KEY (projectid);
ALTER TABLE grailProject ADD CONSTRAINT grailproject_projectid_key UNIQUE (projectid, communityid);

ALTER TABLE grailProject ADD FOREIGN KEY (projectid) REFERENCES grailProject;

ALTER TABLE grailProjectEndMarket ADD FOREIGN KEY (projectid) REFERENCES grailProject;
ALTER TABLE grailProjectEndMarket ADD FOREIGN KEY (marketID) REFERENCES grailEndMarket;

ALTER TABLE grailProjectEndMarket ADD CONSTRAINT grailProEndMarket_mkID_key UNIQUE (projectid, marketID);


-- CONSTRAINTS ---

ALTER TABLE grailAgencyEvaluationRatings ADD CONSTRAINT grailagencyeval_unique_key UNIQUE (projectID, agencyID);
ALTER TABLE grailCoordinationDetails ADD CONSTRAINT grailcoordagency_unique_key UNIQUE (projectID, agencyID);
ALTER TABLE grailCAFieldWorkMapping ADD CONSTRAINT grailcafwmapping_unique_key UNIQUE (projectID, coAgencyID, fwAgencyID);
ALTER TABLE grailEndMarketDetails ADD CONSTRAINT grailendmarketdetails_unique_key UNIQUE (projectID, endMarketID);
ALTER TABLE grailFMActuals ADD CONSTRAINT grailfmactuals_unique_key UNIQUE (projectID, budgetYear,budgetApproverID);
ALTER TABLE grailFMCoPlan ADD CONSTRAINT grailfmcoplan_unique_key UNIQUE (projectID, budgetYear,budgetApproverID);
ALTER TABLE grailFMForecast ADD CONSTRAINT grailfmforecast_unique_key UNIQUE (projectID, budgetYear,budgetApproverID);
ALTER TABLE grailFMPrePlan ADD CONSTRAINT grailfmpreplan_unique_key UNIQUE (projectID, budgetYear,budgetApproverID);
ALTER TABLE grailProjectBudgetApprovers ADD CONSTRAINT grailbudapprovers_unique_key UNIQUE (projectID, approverID);
ALTER TABLE grailProjectEndMarket ADD CONSTRAINT grailmarkdetails_unique_key UNIQUE (projectID, marketID);
ALTER TABLE grailResearchCommission ADD CONSTRAINT grailrdcommission_unique_key UNIQUE (projectID, approverID);
ALTER TABLE grailWaiver ADD CONSTRAINT grailwaiver_pkey PRIMARY KEY (waiverid);
ALTER TABLE grailWaiver ADD CONSTRAINT grailwaiver_projectid_key UNIQUE (projectid, name);
ALTER TABLE grailWaiverApprovers ADD CONSTRAINT grailwaiverapprove_unique_key UNIQUE (waiverID, approverID);
ALTER TABLE grailWaiverEndMarket ADD CONSTRAINT grailwaivermarket_unique_key UNIQUE (waiverID, marketID);

-- INDEX --

CREATE INDEX grailproject_idx ON grailproject USING btree (projectid, communityid, startyear, endyear, producttype, brand);
CREATE INDEX grailproject_idx ON grailproject USING btree (projectid, communityid, startyear, endyear, producttype, brand);
CREATE INDEX grailwaiver_idx  ON grailwaiver  USING btree  (waiverid, projectid, name, description);


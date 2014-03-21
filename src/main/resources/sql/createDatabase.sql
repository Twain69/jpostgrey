-- Table: greylist
-- DROP TABLE greylist

CREATE TABLE greylist ( 
clientaddress character varying(15) NOT NULL, 
sender character varying(255) NOT NULL, 
recipient character varying(255) NOT NULL, 
connectcount bigint NOT NULL,
firstconnect timestamp without time zone NOT NULL, 
lastconnect timestamp without time zone NOT NULL
)
WITH ( OIDS=FALSE ); 

ALTER TABLE greylist OWNER TO jpostgrey;

-- Index: idx
-- DROP INDEX idx;
CREATE INDEX greylist_idx ON greylist USING btree (clientaddress, sender, recipient);



CREATE TABLE whitelist (
pattern character varying(255) NOT NULL,
comment character varying(255) NOT NULL
);

-- Index: idx
-- DROP INDEX idx;
CREATE INDEX whitelist_idx ON whitelist USING btree (pattern);
-- CREATE DATABASE gncompass_core;
-- USE gncompass_core;
-- CREATE USER apiaccess WITH PASSWORD 'EntranceLesterFrenchPortal';
-- GRANT INSERT,SELECT,DELETE,UPDATE ON ALL TABLES IN SCHEMA public TO apiaccess;
-- GRANT SELECT,USAGE ON ALL SEQUENCES IN SCHEMA public TO apiaccess;

-- Privilege legend:
--  r -- SELECT ("read")
--  w -- UPDATE ("write")
--  a -- INSERT ("append")
--  d -- DELETE
--  D -- TRUNCATE
--  x -- REFERENCES
--  t -- TRIGGER
--  X -- EXECUTE
--  U -- USAGE
--  C -- CREATE
--  c -- CONNECT
--  T -- TEMPORARY
--  arwdDxt -- ALL PRIVILEGES (for tables, varies for other objects)

-- Database selection
USE gncompass_core;

-- ----------------------------------------------------------------
-- CLEAN EXISTING STATE
-- ----------------------------------------------------------------

-- Drop trigger sequencing

DROP TRIGGER IF EXISTS borrowers_type_insert_ck;
DROP TRIGGER IF EXISTS borrowers_type_update_ck;
DROP TRIGGER IF EXISTS investors_type_insert_ck;
DROP TRIGGER IF EXISTS investors_type_update_ck;
DROP TRIGGER IF EXISTS investors_pay_day_insert_ck;
DROP TRIGGER IF EXISTS investors_pay_day_update_ck;
DROP TRIGGER IF EXISTS loanfrequencies_details_insert_ck;
DROP TRIGGER IF EXISTS loanfrequencies_details_update_ck;
DROP TRIGGER IF EXISTS loanamortizations_months_insert_ck;
DROP TRIGGER IF EXISTS loanamortizations_months_update_ck;
DROP TRIGGER IF EXISTS investments_max_amortization_insert_ck;
DROP TRIGGER IF EXISTS investments_max_amortization_update_ck;
DROP TRIGGER IF EXISTS banktransfers_type_insert_ck;
DROP TRIGGER IF EXISTS banktransfers_type_update_ck;
DROP TRIGGER IF EXISTS investmentreturns_type_insert_ck;
DROP TRIGGER IF EXISTS investmentreturns_type_update_ck;
DROP TRIGGER IF EXISTS loanpayments_type_insert_ck;
DROP TRIGGER IF EXISTS loanpayments_type_update_ck;
DROP TRIGGER IF EXISTS adjustments_type_insert_ck;
DROP TRIGGER IF EXISTS adjustments_type_update_ck;

-- Drop table sequencing

DROP TABLE IF EXISTS Transactions CASCADE;
DROP TABLE IF EXISTS LoanFulfillments CASCADE;
DROP TABLE IF EXISTS InvestmentFunds CASCADE;
DROP TABLE IF EXISTS Adjustments CASCADE;
DROP TABLE IF EXISTS LoanPayments CASCADE;
DROP TABLE IF EXISTS InvestmentReturns CASCADE;
DROP TABLE IF EXISTS BankTransfers CASCADE;
DROP TABLE IF EXISTS TransactionDetails CASCADE;
DROP TABLE IF EXISTS TransactionTypes CASCADE;
DROP TABLE IF EXISTS InvestmentBuckets CASCADE;
DROP TABLE IF EXISTS Loans CASCADE;
DROP TABLE IF EXISTS BankConnections CASCADE;
DROP TABLE IF EXISTS Banks CASCADE;
DROP TABLE IF EXISTS InvestmentPools CASCADE;
DROP TABLE IF EXISTS Investments CASCADE;
DROP TABLE IF EXISTS LoanAmortizations CASCADE;
DROP TABLE IF EXISTS LoanFrequencies CASCADE;
DROP TABLE IF EXISTS AssessmentFiles CASCADE;
DROP TABLE IF EXISTS Assessments CASCADE;
DROP TABLE IF EXISTS AssessmentStatuses CASCADE;
DROP TABLE IF EXISTS Ratings CASCADE;
DROP TABLE IF EXISTS UserSessions CASCADE;
DROP TABLE IF EXISTS Investors CASCADE;
DROP TABLE IF EXISTS Borrowers CASCADE;
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Countries CASCADE;
DROP TABLE IF EXISTS Regions CASCADE;
DROP TABLE IF EXISTS Currencies CASCADE;
DROP TABLE IF EXISTS UserTypes CASCADE;

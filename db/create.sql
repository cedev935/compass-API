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
-- CREATE TABLES
-- ----------------------------------------------------------------

-- The UserTypes table
CREATE TABLE UserTypes(
  id int NOT NULL,
  name varchar(20) NOT NULL,
  CONSTRAINT usertypes_id_pk PRIMARY KEY (id)
);

-- The Currencies table
CREATE TABLE Currencies(
  id int NOT NULL,
  code varchar(3) NOT NULL,
  name varchar(64) NOT NULL,
  rate numeric(10,5) NOT NULL,
  CONSTRAINT currencies_id_pk PRIMARY KEY (id),
  CONSTRAINT currencies_code_ak UNIQUE (code)
);

-- The Regions table
CREATE TABLE Regions(
  id int NOT NULL,
  name varchar(64) NOT NULL,
  currency int NOT NULL,
  CONSTRAINT regions_id_pk PRIMARY KEY (id),
  CONSTRAINT regions_currency_fk FOREIGN KEY (currency) REFERENCES Currencies (id)
);

-- The Countries table
CREATE TABLE Countries(
  id int NOT NULL,
  code varchar(2) NOT NULL,
  name varchar(55) NOT NULL,
  region int NOT NULL,
  enabled smallint NOT NULL DEFAULT 1,
  CONSTRAINT countries_id_pk PRIMARY KEY (id),
  CONSTRAINT countries_region_fk FOREIGN KEY (region) REFERENCES Regions (id),
  CONSTRAINT countries_code_ak UNIQUE (code)
);

-- The Users table
CREATE TABLE Users(
  id int NOT NULL AUTO_INCREMENT,
  type int NOT NULL,
  password binary(60) NOT NULL,
  password_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  name varchar(50) NOT NULL,
  enabled smallint NOT NULL DEFAULT 1,
  flags smallint NOT NULL DEFAULT 0,
  address1 varchar(40) NOT NULL,
  address2 varchar(40),
  address3 varchar(40),
  city varchar(30) NOT NULL,
  province varchar(30),
  post_code varchar(15),
  country int NOT NULL,
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT users_id_pk PRIMARY KEY (id),
  CONSTRAINT users_type_fk FOREIGN KEY (type) REFERENCES UserTypes (id),
  CONSTRAINT users_country_fk FOREIGN KEY (country) REFERENCES Countries (id),
  CONSTRAINT users_id_type_ak UNIQUE (id, type)
);

-- The Borrowers table
CREATE TABLE Borrowers(
  id int NOT NULL,
  reference binary(16) NOT NULL,
  email varchar(100) NOT NULL,
  type int NOT NULL DEFAULT 1,
  phone varchar(25) NOT NULL,
  employer varchar(60) NOT NULL,
  job_title varchar(60) NOT NULL,
  loan_cap numeric(19,4),
  CONSTRAINT borrowers_id_pk PRIMARY KEY (id),
  CONSTRAINT borrowers_id_type_fk FOREIGN KEY (id, type) REFERENCES Users (id, type),
  CONSTRAINT borrowers_reference_ak UNIQUE (reference),
  CONSTRAINT borrowers_email_ak UNIQUE (email)
);

-- The Borrowers triggers
delimiter $
CREATE TRIGGER borrowers_type_insert_ck BEFORE INSERT ON Borrowers
FOR EACH ROW
BEGIN
  IF NEW.type<>1
  THEN
    SET NEW.type=1;
  END IF;
END$
CREATE TRIGGER borrowers_type_update_ck BEFORE UPDATE ON Borrowers
FOR EACH ROW
BEGIN
  IF NEW.type<>1
  THEN
    SET NEW.type=1;
  END IF;
END$
delimiter ;

-- The Investors table
CREATE TABLE Investors(
  id int NOT NULL,
  reference binary(16) NOT NULL,
  email varchar(100) NOT NULL,
  type int NOT NULL DEFAULT 2,
  pay_day int NOT NULL,
  CONSTRAINT investors_id_pk PRIMARY KEY (id),
  CONSTRAINT investors_id_type_fk FOREIGN KEY (id, type) REFERENCES Users (id, type),
  CONSTRAINT investors_reference_ak UNIQUE (reference),
  CONSTRAINT investors_email_ak UNIQUE (email)
);

-- The Investors triggers
delimiter $
CREATE TRIGGER investors_type_insert_ck BEFORE INSERT ON Investors
FOR EACH ROW
BEGIN
  IF NEW.type<>2
  THEN
    SET NEW.type=2;
  END IF;
END$
CREATE TRIGGER investors_type_update_ck BEFORE UPDATE ON Investors
FOR EACH ROW
BEGIN
  IF NEW.type<>2
  THEN
    SET NEW.type=2;
  END IF;
END$
CREATE TRIGGER investors_pay_day_insert_ck BEFORE INSERT ON Investors
FOR EACH ROW
BEGIN
  IF NEW.pay_day<1 OR NEW.pay_day>31
  THEN
    SIGNAL SQLSTATE '40000' SET message_text = 'pay day constraint is out of range';
  END IF;
END$
CREATE TRIGGER investors_pay_day_update_ck BEFORE UPDATE ON Investors
FOR EACH ROW
BEGIN
  IF NEW.pay_day<1 OR NEW.pay_day>31
  THEN
    SIGNAL SQLSTATE '40001' SET message_text = 'pay day constraint is out of range';
  END IF;
END$
delimiter ;

-- The UserSessions table
CREATE TABLE UserSessions(
  id int NOT NULL AUTO_INCREMENT,
  user_id int NOT NULL,
  device_id binary(16) NOT NULL,
  session_key binary(16) NOT NULL,
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  accessed timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT usersessions_id_pk PRIMARY KEY (id),
  CONSTRAINT usersessions_user_id_fk FOREIGN KEY (user_id) REFERENCES Users (id)
);

-- The Ratings table
CREATE TABLE Ratings(
  id int NOT NULL AUTO_INCREMENT,
  code varchar(2) NOT NULL,
  name varchar(30) NOT NULL,
  loan_rate numeric(6,5) NOT NULL,
  return_rate numeric(6,5) NOT NULL,
  initial_cap numeric(19,4) NOT NULL,
  CONSTRAINT ratings_id_pk PRIMARY KEY (id),
  CONSTRAINT ratings_code_ak UNIQUE (code)
);

-- The AssessmentStatuses table
CREATE TABLE AssessmentStatuses(
  id int NOT NULL,
  name varchar(30) NOT NULL,
  CONSTRAINT assessmentstatuses_id_pk PRIMARY KEY (id)
);

-- The Assessments table
CREATE TABLE Assessments(
  id int NOT NULL AUTO_INCREMENT,
  reference binary(16) NOT NULL,
  borrower int NOT NULL,
  registered timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status int NOT NULL,
  rating int,
  CONSTRAINT assessments_id_pk PRIMARY KEY (id),
  CONSTRAINT assessments_borrower_fk FOREIGN KEY (borrower) REFERENCES Borrowers (id),
  CONSTRAINT assessments_status_fk FOREIGN KEY (status) REFERENCES AssessmentStatuses (id),
  CONSTRAINT assessments_rating_fk FOREIGN KEY (rating) REFERENCES Ratings (id),
  CONSTRAINT assessments_reference_ak UNIQUE (reference)
);

-- The AssessmentFiles table
CREATE TABLE AssessmentFiles(
  id int NOT NULL AUTO_INCREMENT,
  assessment int NOT NULL,
  bucket varchar(128) NOT NULL,
  filename varchar(128) NOT NULL,
  type varchar(128) NOT NULL,
  uploaded timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT assessmentfiles_id_pk PRIMARY KEY (id),
  CONSTRAINT assessmentfiles_assessment_fk FOREIGN KEY (assessment) REFERENCES Assessments (id),
  CONSTRAINT assessmentfiles_file_ak UNIQUE (assessment, bucket, filename)
);

-- The LoanFrequencies table
CREATE TABLE LoanFrequencies(
  id int NOT NULL,
  name varchar(30) NOT NULL,
  days int,
  per_month int,
  CONSTRAINT loanfrequencies PRIMARY KEY (id)
);

-- The LoanFrequencies triggers
delimiter $
CREATE TRIGGER loanfrequencies_details_insert_ck BEFORE INSERT ON LoanFrequencies
FOR EACH ROW
BEGIN
  IF ((NEW.days IS NULL AND NEW.per_month IS NULL)
      OR (NEW.days IS NOT NULL AND NEW.per_month IS NOT NULL)
      OR (NEW.days IS NOT NULL AND NEW.days<=0))
  THEN
    SIGNAL SQLSTATE '41000' SET message_text = 'only days or per_month can be initialized';
  END IF;
END$
CREATE TRIGGER loanfrequencies_details_update_ck BEFORE UPDATE ON LoanFrequencies
FOR EACH ROW
BEGIN
  IF ((NEW.days IS NULL AND NEW.per_month IS NULL)
      OR (NEW.days IS NOT NULL AND NEW.per_month IS NOT NULL)
      OR (NEW.days IS NOT NULL AND NEW.days<=0))
  THEN
    SIGNAL SQLSTATE '41001' SET message_text = 'only days or per_month can be initialized';
  END IF;
END$
delimiter ;

-- The LoanAmortizations table
CREATE TABLE LoanAmortizations(
  id int NOT NULL,
  name varchar(30) NOT NULL,
  months int NOT NULL,
  CONSTRAINT loanamortizations_id_pk PRIMARY KEY (id)
);

-- The LoanAmortizations triggers
delimiter $
CREATE TRIGGER loanamortizations_months_insert_ck BEFORE INSERT ON LoanAmortizations
FOR EACH ROW
BEGIN
  IF NEW.months<=0
  THEN
    SIGNAL SQLSTATE '42000' SET message_text = 'number of months must be positive';
  END IF;
END$
CREATE TRIGGER loanamortizations_months_update_ck BEFORE UPDATE ON LoanAmortizations
FOR EACH ROW
BEGIN
  IF NEW.months<=0
  THEN
    SIGNAL SQLSTATE '42001' SET message_text = 'number of months must be positive';
  END IF;
END$
delimiter ;

-- The Investments table
CREATE TABLE Investments(
  id int NOT NULL AUTO_INCREMENT,
  reference binary(16) NOT NULL,
  investor int NOT NULL,
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  max_amortization int NOT NULL,
  CONSTRAINT investments_id_pk PRIMARY KEY (id),
  CONSTRAINT investments_investor_fk FOREIGN KEY (investor) REFERENCES Investors (id),
  CONSTRAINT investments_reference_ak UNIQUE (reference)
);

-- The Investments triggers
delimiter $
CREATE TRIGGER investments_max_amortization_insert_ck BEFORE INSERT ON Investments
FOR EACH ROW
BEGIN
  IF NEW.max_amortization<=0
  THEN
    SIGNAL SQLSTATE '43000' SET message_text = 'max amortization length must be positive';
  END IF;
END$
CREATE TRIGGER investments_max_amortization_update_ck BEFORE UPDATE ON Investments
FOR EACH ROW
BEGIN
  IF NEW.max_amortization<=0
  THEN
    SIGNAL SQLSTATE '43001' SET message_text = 'max amortization length must be positive';
  END IF;
END$
delimiter ;

-- The InvestmentPools table
CREATE TABLE InvestmentPools(
  id int NOT NULL AUTO_INCREMENT,
  investment int NOT NULL,
  rating int NOT NULL,
  CONSTRAINT investmentpools_id_pk PRIMARY KEY (id),
  CONSTRAINT investmentpools_investment_fk FOREIGN KEY (investment) REFERENCES Investments (id),
  CONSTRAINT investmentpools_rating_fk FOREIGN KEY (rating) REFERENCES Ratings (id)
);

-- The Banks table
CREATE TABLE Banks(
  id int NOT NULL AUTO_INCREMENT,
  code int NOT NULL,
  name varchar(128) NOT NULL,
  country int NOT NULL,
  enabled smallint NOT NULL DEFAULT 1,
  CONSTRAINT banks_code_pk PRIMARY KEY (id),
  CONSTRAINT banks_country_fk FOREIGN KEY (country) REFERENCES Countries (id)
);

-- The BankConnections table
CREATE TABLE BankConnections(
  id int NOT NULL AUTO_INCREMENT,
  reference binary(16) NOT NULL,
  user_id int NOT NULL,
  login_id binary(16) NOT NULL,
  enabled smallint NOT NULL DEFAULT 1,
  institution int NOT NULL,
  transit int NOT NULL,
  account int NOT NULL,
  CONSTRAINT bankconnections_id_pk PRIMARY KEY (id),
  CONSTRAINT bankconnections_user_id_fk FOREIGN KEY (user_id) REFERENCES Users (id),
  CONSTRAINT bankconnections_institution_fk FOREIGN KEY (institution) REFERENCES Banks (id),
  CONSTRAINT bankconnections_reference_ak UNIQUE (reference)
);

-- The Loans table
CREATE TABLE Loans(
  id int NOT NULL AUTO_INCREMENT,
  reference binary(16) NOT NULL,
  borrower int NOT NULL,
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  bank int NOT NULL,
  principal numeric(19,4) NOT NULL,
  rating int NOT NULL,
  rate numeric(6,5) NOT NULL,
  amortization int NOT NULL,
  frequency int NOT NULL,
  start_date date,
  CONSTRAINT loans_id_pk PRIMARY KEY (id),
  CONSTRAINT loans_borrower_fk FOREIGN KEY (borrower) REFERENCES Borrowers (id),
  CONSTRAINT loans_bank_fk FOREIGN KEY (bank) REFERENCES BankConnections (id),
  CONSTRAINT loans_rating_fk FOREIGN KEY (rating) REFERENCES Ratings (id),
  CONSTRAINT loans_amortization_fk FOREIGN KEY (amortization) REFERENCES LoanAmortizations (id),
  CONSTRAINT loans_frequency_fk FOREIGN KEY (frequency) REFERENCES LoanFrequencies (id),
  CONSTRAINT loans_reference_ak UNIQUE (reference)
);

-- The InvestmentBuckets table
CREATE TABLE InvestmentBuckets(
  id int NOT NULL AUTO_INCREMENT,
  amount numeric(19,4) NOT NULL,
  loan int NOT NULL,
  pool int NOT NULL,
  CONSTRAINT investmentbuckets_id_pk PRIMARY KEY (id),
  CONSTRAINT investmentbuckets_loan_fk FOREIGN KEY (loan) REFERENCES Loans (id),
  CONSTRAINT investmentbuckets_pool_fk FOREIGN KEY (pool) REFERENCES InvestmentPools (id)
);

-- The TransactionTypes table
CREATE TABLE TransactionTypes(
  id int NOT NULL,
  name varchar(20) NOT NULL,
  CONSTRAINT transactiontypes_id_pk PRIMARY KEY (id)
);

-- The TransactionDetails table
CREATE TABLE TransactionDetails(
  id bigint NOT NULL AUTO_INCREMENT,
  type int NOT NULL,
  amount numeric(19,4) NOT NULL,
  CONSTRAINT transactiondetails_id_pk PRIMARY KEY (id),
  CONSTRAINT transactiondetails_type_fk FOREIGN KEY (type) REFERENCES TransactionTypes (id),
  CONSTRAINT transactiondetails_id_type_ak UNIQUE (id, type)
);

-- The BankTransfers table
CREATE TABLE BankTransfers(
  id bigint NOT NULL,
  type int NOT NULL DEFAULT 1,
  bank int NOT NULL,
  CONSTRAINT banktransfers_id_pk PRIMARY KEY (id),
  CONSTRAINT banktransfers_id_type_fk FOREIGN KEY (id, type) REFERENCES TransactionDetails (id, type),
  CONSTRAINT banktransfers_bank_fk FOREIGN KEY (bank) REFERENCES BankConnections (id)
);

-- The BankTransfers triggers
delimiter $
CREATE TRIGGER banktransfers_type_insert_ck BEFORE INSERT ON BankTransfers
FOR EACH ROW
BEGIN
  IF NEW.type<>1
  THEN
    SET NEW.type=1;
  END IF;
END$
CREATE TRIGGER banktransfers_type_update_ck BEFORE UPDATE ON BankTransfers
FOR EACH ROW
BEGIN
  IF NEW.type<>1
  THEN
    SET NEW.type=1;
  END IF;
END$
delimiter ;

-- The InvestmentReturns table
CREATE TABLE InvestmentReturns(
  id bigint NOT NULL,
  type int NOT NULL DEFAULT 2,
  bucket int NOT NULL,
  interest numeric(19,4) NOT NULL,
  CONSTRAINT investmentreturns_id_pk PRIMARY KEY (id),
  CONSTRAINT investmentreturns_id_type_fk FOREIGN KEY (id, type) REFERENCES TransactionDetails (id, type),
  CONSTRAINT investmentreturns_bucket_fk FOREIGN KEY (bucket) REFERENCES InvestmentBuckets (id)
);

-- The InvestmentReturns triggers
delimiter $
CREATE TRIGGER investmentreturns_type_insert_ck BEFORE INSERT ON InvestmentReturns
FOR EACH ROW
BEGIN
  IF NEW.type<>2
  THEN
    SET NEW.type=2;
  END IF;
END$
CREATE TRIGGER investmentreturns_type_update_ck BEFORE UPDATE ON InvestmentReturns
FOR EACH ROW
BEGIN
  IF NEW.type<>2
  THEN
    SET NEW.type=2;
  END IF;
END$
delimiter ;

-- The LoanPayments table
CREATE TABLE LoanPayments(
  id bigint NOT NULL,
  type int NOT NULL DEFAULT 3,
  loan int NOT NULL,
  interest numeric(19,4) NOT NULL,
  due_date date NOT NULL,
  CONSTRAINT loanpayments_id_pk PRIMARY KEY (id),
  CONSTRAINT loanpayments_id_type_fk FOREIGN KEY (id, type) REFERENCES TransactionDetails (id, type),
  CONSTRAINT loanpayments_loan_fk FOREIGN KEY (loan) REFERENCES Loans (id)
);

-- The LoanPayments triggers
delimiter $
CREATE TRIGGER loanpayments_type_insert_ck BEFORE INSERT ON LoanPayments
FOR EACH ROW
BEGIN
  IF NEW.type<>3
  THEN
    SET NEW.type=3;
  END IF;
END$
CREATE TRIGGER loanpayments_type_update_ck BEFORE UPDATE ON LoanPayments
FOR EACH ROW
BEGIN
  IF NEW.type<>3
  THEN
    SET NEW.type=3;
  END IF;
END$
delimiter ;

-- The Adjustments table
CREATE TABLE Adjustments(
  id bigint NOT NULL,
  type int NOT NULL DEFAULT 4 CHECK (type = 4),
  CONSTRAINT adjustments_id_pk PRIMARY KEY (id),
  CONSTRAINT adjustments_id_type_fk FOREIGN KEY (id, type) REFERENCES TransactionDetails (id, type)
);

-- The Adjustments triggers
delimiter $
CREATE TRIGGER adjustments_type_insert_ck BEFORE INSERT ON Adjustments
FOR EACH ROW
BEGIN
  IF NEW.type<>4
  THEN
    SET NEW.type=4;
  END IF;
END$
CREATE TRIGGER adjustments_type_update_ck BEFORE UPDATE ON Adjustments
FOR EACH ROW
BEGIN
  IF NEW.type<>4
  THEN
    SET NEW.type=4;
  END IF;
END$
delimiter ;

-- The InvestmentFunds table
CREATE TABLE InvestmentFunds(
  id bigint NOT NULL,
  type int NOT NULL DEFAULT 5,
  pool int NOT NULL,
  CONSTRAINT investmentfunds_id_pk PRIMARY KEY (id),
  CONSTRAINT investmentfunds_id_type_fk FOREIGN KEY (id, type) REFERENCES TransactionDetails (id, type),
  CONSTRAINT investmentfunds_pool_fk FOREIGN KEY (pool) REFERENCES InvestmentPools (id)
);

-- The InvestmentFunds triggers
delimiter $
CREATE TRIGGER investmentfunds_type_insert_ck BEFORE INSERT ON InvestmentFunds
FOR EACH ROW
BEGIN
  IF NEW.type<>5
  THEN
    SET NEW.type=5;
  END IF;
END$
CREATE TRIGGER investmentfunds_type_update_ck BEFORE UPDATE ON InvestmentFunds
FOR EACH ROW
BEGIN
  IF NEW.type<>5
  THEN
    SET NEW.type=5;
  END IF;
END$
delimiter ;

-- The LoanFulfillments table
CREATE TABLE LoanFulfillments(
  id bigint NOT NULL,
  type int NOT NULL DEFAULT 6,
  loan int NOT NULL,
  CONSTRAINT loanfulfillments_id_pk PRIMARY KEY (id),
  CONSTRAINT loanfulfillments_id_type_fk FOREIGN KEY (id, type) REFERENCES TransactionDetails (id, type),
  CONSTRAINT loanfulfillments_loan_fk FOREIGN KEY (loan) REFERENCES Loans (id)
);

-- The LoanPayments triggers
delimiter $
CREATE TRIGGER loanfulfillments_type_insert_ck BEFORE INSERT ON LoanFulfillments
FOR EACH ROW
BEGIN
  IF NEW.type<>6
  THEN
    SET NEW.type=6;
  END IF;
END$
CREATE TRIGGER loanfulfillments_type_update_ck BEFORE UPDATE ON LoanFulfillments
FOR EACH ROW
BEGIN
  IF NEW.type<>6
  THEN
    SET NEW.type=6;
  END IF;
END$
delimiter ;

-- The Transactions table
CREATE TABLE Transactions(
  id bigint NOT NULL AUTO_INCREMENT,
  reference binary(16) NOT NULL,
  registered timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  user_id int NOT NULL,
  description text NOT NULL,
  item bigint NOT NULL,
  balance numeric(19,4) NOT NULL,
  CONSTRAINT transactions_id_pk PRIMARY KEY (id),
  CONSTRAINT transactions_user_id_fk FOREIGN KEY (user_id) REFERENCES Users (id),
  CONSTRAINT transactions_item_fk FOREIGN KEY (item) REFERENCES TransactionDetails (id),
  CONSTRAINT transactions_reference_ak UNIQUE (reference)
);

-- ----------------------------------------------------------------
-- INSERT DATA
-- ----------------------------------------------------------------

-- UserTypes
INSERT INTO UserTypes (id, name)
VALUES (1, 'Borrowers');

INSERT INTO UserTypes (id, name)
VALUES (2, 'Investors');

-- Currencies
INSERT INTO Currencies (id, code, name, rate)
VALUES (1, 'CAD', 'Canadian Dollar', 1.00000);

-- Regions
INSERT INTO Regions (id, name, currency)
VALUES (1, 'Canada', 1);

-- Countries
INSERT INTO Countries (id, code, name, region)
VALUES (1, 'CA', 'Canada', 1);

-- AssessmentStatuses
INSERT INTO AssessmentStatuses (id, name)
VALUES (1, 'Started');

INSERT INTO AssessmentStatuses (id, name)
VALUES (2, 'Pending');

INSERT INTO AssessmentStatuses (id, name)
VALUES (3, 'Approved');

INSERT INTO AssessmentStatuses (id, name)
VALUES (4, 'Rejected');

-- LoanFrequencies
INSERT INTO LoanFrequencies (id, name, per_month)
VALUES (1, 'Monthly', 1);

INSERT INTO LoanFrequencies (id, name, per_month)
VALUES (2, 'Semi-monthly', 2);

INSERT INTO LoanFrequencies (id, name, days)
VALUES (3, 'Bi-weekly', 14);

INSERT INTO LoanFrequencies (id, name, days)
VALUES (4, 'Weekly', 7);

-- LoanAmortizations
INSERT INTO LoanAmortizations (id, name, months)
VALUES (1, '6 months', 6);

INSERT INTO LoanAmortizations (id, name, months)
VALUES (2, '1 year', 12);

INSERT INTO LoanAmortizations (id, name, months)
VALUES (3, '2 years', 24);

INSERT INTO LoanAmortizations (id, name, months)
VALUES (4, '3 years', 36);

INSERT INTO LoanAmortizations (id, name, months)
VALUES (5, '4 years', 48);

INSERT INTO LoanAmortizations (id, name, months)
VALUES (6, '5 years', 60);

-- Ratings
INSERT INTO Ratings (id, code, name, loan_rate, return_rate, initial_cap)
VALUES (1, 'A+', 'Very strong rating', 0.039, 0.029, 50000.00);

INSERT INTO Ratings (id, code, name, loan_rate, return_rate, initial_cap)
VALUES (2, 'A', 'Strong rating', 0.059, 0.039, 40000.00);

INSERT INTO Ratings (id, code, name, loan_rate, return_rate, initial_cap)
VALUES (3, 'A-', 'Great rating', 0.079, 0.049, 30000.00);

INSERT INTO Ratings (id, code, name, loan_rate, return_rate, initial_cap)
VALUES (4, 'B+', 'Good rating', 0.099, 0.059, 20000.00);

INSERT INTO Ratings (id, code, name, loan_rate, return_rate, initial_cap)
VALUES (5, 'B', 'Average rating', 0.119, 0.069, 15000.00);

-- Banks
INSERT INTO Banks (id, code, name, country)
VALUES (1, 1, 'Bank of Montreal', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (2, 2, 'Scotiabank', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (3, 3, 'Royal Bank of Canada', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (4, 4, 'TD Canada Trust', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (5, 6, 'National Bank of Canada', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (6, 10, 'CIBC', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (7, 10, 'Simplii', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (8, 16, 'HSBC', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (9, 39, 'Laurentian Bank of Canada', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (10, 219, 'ATB Financial', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (11, 614, 'Tangerine', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (12, 809, 'Vancity', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (13, 809, 'Coast Capital', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (14, 815, 'Desjardins', 1);

INSERT INTO Banks (id, code, name, country)
VALUES (15, 837, 'Meridian Credit Union', 1);

-- TransactionTypes
INSERT INTO TransactionTypes (id, name)
VALUES (1, 'Bank Transfers');

INSERT INTO TransactionTypes (id, name)
VALUES (2, 'Investment Returns');

INSERT INTO TransactionTypes (id, name)
VALUES (3, 'Loan Payments');

INSERT INTO TransactionTypes (id, name)
VALUES (4, 'Adjustments');

INSERT INTO TransactionTypes (id, name)
VALUES (5, 'Investment Funds');

INSERT INTO TransactionTypes (id, name)
VALUES (6, 'Loan Fulfillments');

-- ============================================================
-- V1__init.sql
-- Oracle DDL — Banking Credit Application Schema
-- Hibernate 6 / Oracle 21c+ (FREEPDB1)
-- UUID columns: RAW(16) — Hibernate 6 OracleDialect default
-- ============================================================

-- ============================================================
-- CUSTOMERS  (base table for JOINED inheritance)
-- ============================================================
CREATE TABLE CUSTOMERS (
    ID                   RAW(16)        NOT NULL,
    CUSTOMER_TYPE_CODE   VARCHAR2(10)   NOT NULL,
    PHONE_NUMBER         VARCHAR2(20),
    EMAIL                VARCHAR2(150)  NOT NULL,
    ADDRESS              VARCHAR2(500),
    IS_ACTIVE            NUMBER(1, 0)   NOT NULL,
    CREATED_DATE         TIMESTAMP      NOT NULL,
    UPDATED_DATE         TIMESTAMP,
    DELETED_DATE         TIMESTAMP,
    CONSTRAINT PK_CUSTOMERS PRIMARY KEY (ID),
    CONSTRAINT UQ_CUSTOMERS_EMAIL UNIQUE (EMAIL),
    CONSTRAINT CK_CUSTOMERS_IS_ACTIVE CHECK (IS_ACTIVE IN (0, 1))
);

-- ============================================================
-- INDIVIDUAL_CUSTOMERS  (JOINED child)
-- ============================================================
CREATE TABLE INDIVIDUAL_CUSTOMERS (
    ID            RAW(16)       NOT NULL,
    NATIONAL_ID   VARCHAR2(11)  NOT NULL,
    FIRST_NAME    VARCHAR2(50)  NOT NULL,
    LAST_NAME     VARCHAR2(50)  NOT NULL,
    DATE_OF_BIRTH DATE,
    MOTHER_NAME   VARCHAR2(100),
    FATHER_NAME   VARCHAR2(100),
    CONSTRAINT PK_INDIVIDUAL_CUSTOMERS PRIMARY KEY (ID),
    CONSTRAINT FK_IND_CUST_CUSTOMER
        FOREIGN KEY (ID) REFERENCES CUSTOMERS (ID),
    CONSTRAINT UQ_INDIVIDUAL_CUSTOMERS_NATIONAL_ID UNIQUE (NATIONAL_ID)
);

-- ============================================================
-- CORPORATE_CUSTOMERS  (JOINED child)
-- ============================================================
CREATE TABLE CORPORATE_CUSTOMERS (
    ID                          RAW(16)       NOT NULL,
    COMPANY_NAME                VARCHAR2(100) NOT NULL,
    TAX_NUMBER                  VARCHAR2(10)  NOT NULL,
    TAX_OFFICE                  VARCHAR2(100),
    COMPANY_REGISTRATION_NUMBER VARCHAR2(50),
    AUTHORIZED_PERSON_NAME      VARCHAR2(200),
    COMPANY_FOUNDATION_DATE     DATE,
    CONSTRAINT PK_CORPORATE_CUSTOMERS PRIMARY KEY (ID),
    CONSTRAINT FK_CORP_CUST_CUSTOMER
        FOREIGN KEY (ID) REFERENCES CUSTOMERS (ID),
    CONSTRAINT UQ_CORPORATE_CUSTOMERS_TAX_NUMBER UNIQUE (TAX_NUMBER)
);

-- ============================================================
-- CREDIT_TYPES
-- ============================================================
CREATE TABLE CREDIT_TYPES (
    ID                   RAW(16)          NOT NULL,
    NAME                 VARCHAR2(100)    NOT NULL,
    DESCRIPTION          VARCHAR2(500),
    CUSTOMER_TYPE        VARCHAR2(20)     NOT NULL,
    MIN_AMOUNT           NUMBER(18, 2)    NOT NULL,
    MAX_AMOUNT           NUMBER(18, 2)    NOT NULL,
    MIN_TERM             NUMBER(10, 0)    NOT NULL,
    MAX_TERM             NUMBER(10, 0)    NOT NULL,
    BASE_INTEREST_RATE   NUMBER(5, 2)     NOT NULL,
    PARENT_CREDIT_TYPE_ID RAW(16),
    CREATED_DATE         TIMESTAMP        NOT NULL,
    UPDATED_DATE         TIMESTAMP,
    DELETED_DATE         TIMESTAMP,
    CONSTRAINT PK_CREDIT_TYPES PRIMARY KEY (ID),
    CONSTRAINT FK_CREDIT_TYPE_PARENT
        FOREIGN KEY (PARENT_CREDIT_TYPE_ID) REFERENCES CREDIT_TYPES (ID),
    CONSTRAINT CK_CREDIT_TYPES_CUSTOMER_TYPE
        CHECK (CUSTOMER_TYPE IN ('INDIVIDUAL', 'CORPORATE'))
);

-- ============================================================
-- CREDIT_APPLICATIONS
-- STATUS_CODE ordinal: 0=PENDING, 1=APPROVED, 2=REJECTED
-- ============================================================
CREATE TABLE CREDIT_APPLICATIONS (
    ID               RAW(16)        NOT NULL,
    CUSTOMER_ID      RAW(16)        NOT NULL,
    CREDIT_TYPE_ID   RAW(16)        NOT NULL,
    REQUESTED_AMOUNT NUMBER(18, 2)  NOT NULL,
    REQUESTED_TERM   NUMBER(10, 0)  NOT NULL,
    APPROVED_AMOUNT  NUMBER(18, 2),
    APPROVED_TERM    NUMBER(10, 0),
    INTEREST_RATE    NUMBER(5, 2),
    MONTHLY_PAYMENT  NUMBER(18, 2),
    TOTAL_PAYMENT    NUMBER(18, 2),
    STATUS_CODE      NUMBER(3, 0)   NOT NULL,
    REJECTION_REASON VARCHAR2(500),
    CREATED_DATE     TIMESTAMP      NOT NULL,
    UPDATED_DATE     TIMESTAMP,
    DELETED_DATE     TIMESTAMP,
    CONSTRAINT PK_CREDIT_APPLICATIONS PRIMARY KEY (ID),
    CONSTRAINT FK_CREDIT_APP_CUSTOMER
        FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMERS (ID),
    CONSTRAINT FK_CREDIT_APP_CREDIT_TYPE
        FOREIGN KEY (CREDIT_TYPE_ID) REFERENCES CREDIT_TYPES (ID),
    CONSTRAINT CK_CREDIT_APP_STATUS_CODE
        CHECK (STATUS_CODE IN (0, 1, 2))
);

-- ============================================================
-- APPLICATION_USERS
-- PASSWORD_HASH / PASSWORD_SALT: RAW(64) — Oracle RAW type
-- ============================================================
CREATE TABLE APPLICATION_USERS (
    ID            RAW(16)       NOT NULL,
    CUSTOMER_ID   RAW(16),
    EMAIL         VARCHAR2(150) NOT NULL,
    PASSWORD_HASH RAW(64),
    PASSWORD_SALT RAW(64),
    IS_ACTIVE     NUMBER(1, 0)  NOT NULL,
    ROLE          VARCHAR2(20)  NOT NULL,
    CREATED_DATE  TIMESTAMP     NOT NULL,
    UPDATED_DATE  TIMESTAMP,
    DELETED_DATE  TIMESTAMP,
    CONSTRAINT PK_APPLICATION_USERS PRIMARY KEY (ID),
    CONSTRAINT UQ_APPLICATION_USERS_EMAIL UNIQUE (EMAIL),
    CONSTRAINT CK_APPLICATION_USERS_IS_ACTIVE CHECK (IS_ACTIVE IN (0, 1)),
    CONSTRAINT CK_APPLICATION_USERS_ROLE
        CHECK (ROLE IN ('CUSTOMER', 'ADMIN', 'OFFICER'))
);

-- ============================================================
-- Indexes for common FK / filter columns
-- ============================================================
CREATE INDEX IDX_CREDIT_APP_CUSTOMER_ID   ON CREDIT_APPLICATIONS (CUSTOMER_ID);
CREATE INDEX IDX_CREDIT_APP_STATUS_CODE   ON CREDIT_APPLICATIONS (STATUS_CODE);
CREATE INDEX IDX_CREDIT_TYPE_PARENT_ID    ON CREDIT_TYPES (PARENT_CREDIT_TYPE_ID);
CREATE INDEX IDX_APP_USERS_CUSTOMER_ID    ON APPLICATION_USERS (CUSTOMER_ID);

-- ============================================================
-- CORPORATE_CUSTOMERS: Ticaret Sicil No (tradeRegistrationNumber)
-- ============================================================
-- Yeni zorunlu (NOT NULL) ve benzersiz (UNIQUE) kolon. hbm2ddl.auto=validate
-- olduğundan kolon mevcut olmalıdır. companyRegistrationNumber'dan (COMPANY_
-- REGISTRATION_NUMBER) ayrı bir alandır.
--
-- Geriye dönük güvenlik: kolon önce nullable eklenir, mevcut satırlar benzersiz bir
-- placeholder ile doldurulur, ardından NOT NULL + UNIQUE uygulanır (dev'de tablo
-- boşsa UPDATE 0 satır etkiler).
-- ============================================================

ALTER TABLE CORPORATE_CUSTOMERS ADD (TRADE_REGISTRATION_NUMBER VARCHAR2(20));

UPDATE CORPORATE_CUSTOMERS
   SET TRADE_REGISTRATION_NUMBER = 'TSN' || LPAD(TO_CHAR(ROWNUM), 10, '0')
 WHERE TRADE_REGISTRATION_NUMBER IS NULL;

ALTER TABLE CORPORATE_CUSTOMERS MODIFY (TRADE_REGISTRATION_NUMBER VARCHAR2(20) NOT NULL);

ALTER TABLE CORPORATE_CUSTOMERS
    ADD CONSTRAINT UQ_CORP_CUST_TRADE_REG_NO UNIQUE (TRADE_REGISTRATION_NUMBER);

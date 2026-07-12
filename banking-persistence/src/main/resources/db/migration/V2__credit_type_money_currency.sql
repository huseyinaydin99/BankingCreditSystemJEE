-- ============================================================
-- CREDIT_TYPES: Money value object para birimi kolonları
-- ============================================================
-- minimumAmount / maximumAmount alanları BigDecimal'dan Money value object'ine
-- yükseltildi. Money hem tutarı (mevcut MIN_AMOUNT / MAX_AMOUNT kolonları) hem de
-- para birimini taşır; bu göç para birimi kolonlarını ekler.
--
-- Mevcut satırlar için varsayılan 'TRY' atanır; ardından NOT NULL kısıtı uygulanır.
-- Hibernate hbm2ddl.auto=validate olduğundan bu kolonlar zorunludur; aksi halde
-- CreditType embedded eşlemesi şema doğrulamasında başarısız olur.
-- ============================================================

ALTER TABLE CREDIT_TYPES ADD (
    MIN_CURRENCY   VARCHAR2(3),
    MAX_CURRENCY   VARCHAR2(3)
);

UPDATE CREDIT_TYPES SET MIN_CURRENCY = 'TRY', MAX_CURRENCY = 'TRY';

ALTER TABLE CREDIT_TYPES MODIFY (
    MIN_CURRENCY   VARCHAR2(3)   NOT NULL,
    MAX_CURRENCY   VARCHAR2(3)   NOT NULL
);

-- ============================================================
-- CREDIT_APPLICATIONS: onay iş akışı + Money para birimi kolonları
-- ============================================================
-- 1) Finansal tutarlar (approvedAmount / monthlyPayment / totalPayment) Money value
--    object'ine yükseltildi; her biri için para birimi kolonu eklenir. Tümü nullable'dır
--    çünkü yalnızca onay adımında dolar.
-- 2) CreditApplicationStatus enum'una UNDER_REVIEW (ordinal 3) ve CANCELLED (ordinal 4)
--    eklendi; STATUS_CODE CHECK kısıtı yeni ordinal değerlerini kapsayacak şekilde
--    yeniden oluşturulur.
--
-- Hibernate hbm2ddl.auto=validate olduğundan currency kolonları zorunludur; aksi halde
-- CreditApplication embedded eşlemesi şema doğrulamasında başarısız olur.
-- ============================================================

ALTER TABLE CREDIT_APPLICATIONS ADD (
    APPROVED_CURRENCY         VARCHAR2(3),
    MONTHLY_PAYMENT_CURRENCY  VARCHAR2(3),
    TOTAL_PAYMENT_CURRENCY    VARCHAR2(3)
);

-- STATUS_CODE ordinal aralığını genişlet: 0=PENDING, 1=APPROVED, 2=REJECTED,
-- 3=UNDER_REVIEW, 4=CANCELLED
ALTER TABLE CREDIT_APPLICATIONS DROP CONSTRAINT CK_CREDIT_APP_STATUS_CODE;

ALTER TABLE CREDIT_APPLICATIONS ADD CONSTRAINT CK_CREDIT_APP_STATUS_CODE
    CHECK (STATUS_CODE IN (0, 1, 2, 3, 4));

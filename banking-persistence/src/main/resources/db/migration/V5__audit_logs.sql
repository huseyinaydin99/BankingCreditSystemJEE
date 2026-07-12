-- ============================================================
-- AUDIT_LOGS — denetim izi (kim, ne zaman, ne yaptı)
-- ============================================================
-- State-değiştiren command'lar AuditBehavior tarafından buraya yazılır.
-- ID + CREATED_DATE/UPDATED_DATE/DELETED_DATE, BaseEntity mapped-superclass'tan gelir
-- (hbm2ddl.auto=validate olduğundan tümü mevcut olmalıdır). Soft delete kullanılmaz.
-- OLD_VALUE/NEW_VALUE: JSON gövdeleri (CLOB). PERFORMED_BY: kullanıcı UUID'si (RAW(16)).
-- ============================================================

CREATE TABLE AUDIT_LOGS (
    ID            RAW(16)         NOT NULL,
    ENTITY_ID     VARCHAR2(100),
    ENTITY_TYPE   VARCHAR2(100),
    ACTION        VARCHAR2(20)    NOT NULL,
    PERFORMED_BY  RAW(16),
    PERFORMED_AT  TIMESTAMP       NOT NULL,
    IP_ADDRESS    VARCHAR2(45),
    OLD_VALUE     CLOB,
    NEW_VALUE     CLOB,
    CREATED_DATE  TIMESTAMP       NOT NULL,
    UPDATED_DATE  TIMESTAMP,
    DELETED_DATE  TIMESTAMP,
    CONSTRAINT PK_AUDIT_LOGS PRIMARY KEY (ID),
    CONSTRAINT CK_AUDIT_LOGS_ACTION CHECK (ACTION IN ('CREATE', 'UPDATE', 'DELETE'))
);

CREATE INDEX IDX_AUDIT_LOGS_ENTITY      ON AUDIT_LOGS (ENTITY_TYPE, ENTITY_ID);
CREATE INDEX IDX_AUDIT_LOGS_PERFORMED   ON AUDIT_LOGS (PERFORMED_BY, PERFORMED_AT);

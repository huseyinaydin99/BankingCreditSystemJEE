package tr.com.huseyinaydin.sharedkernel.audit;

/**
 * Denetim izinde kaydedilen işlem türü. State-değiştiren command'lar bu türlerden
 * birine eşlenir (bkz. AuditBehavior).
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE
}

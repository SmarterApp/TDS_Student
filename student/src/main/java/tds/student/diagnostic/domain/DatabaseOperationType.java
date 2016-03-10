package tds.student.diagnostic.domain;

public enum DatabaseOperationType {

    READ("read"),
    WRITE("write"),
    VERIFY("verify");

    private final String type;

    DatabaseOperationType(final String type) {
        this.type = type;
    }

    @SuppressWarnings("unused")
    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type;
    }

}

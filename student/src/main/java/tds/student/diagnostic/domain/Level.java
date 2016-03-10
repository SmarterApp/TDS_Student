package tds.student.diagnostic.domain;

public enum Level {

    LEVEL_0(0),
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5)
    ;

    private final Integer level;

    Level(final Integer level) {
        this.level = level;
    }

    @SuppressWarnings("unused")
    public Integer getLevel() {
        return this.level;
    }

    @Override
    public String toString() {
        return this.level.toString();
    }

}

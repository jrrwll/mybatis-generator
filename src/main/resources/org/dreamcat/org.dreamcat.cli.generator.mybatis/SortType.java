package $condition_package;

/**
 * This class was generated by $generator_name
 *
 * @author $username
 * @version $date
 * @see <a href="https://github.com/jrrwll">Jerry Will's Github</a>
 */
public enum SortType {
    ASC("asc"),
    DESC("desc");

    private String value;

    SortType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
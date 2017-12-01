class DataTypeField {

    static final String NAME = "name";
    static final String FORMAT = "format";
    private Pair<String, String> nameValue = null;
    private Pair<String, String> formatSourceValue = null;
    private String formatDataPointValue = null;
    private String column = null;
    public DataTypeField(String name, Pair<String, String> format, String dbColumn) {
        this.setNameValue(Pair.create(NAME, name));
        this.setFormatSourceValue(Pair.create(FORMAT, format.first));
        this.setFormatDataPointValue(format.second);
        this.setColumn(dbColumn);
    }

    public Pair<String, String> getNameValue() {
        return nameValue;
    }

    public void setNameValue(Pair<String, String> value) {
        this.nameValue = value;
    }

    public Pair<String, String> getFormatSourceValue() {
        return formatSourceValue;
    }

    public void setFormatSourceValue(Pair<String, String> value) {
        this.formatSourceValue = value;
    }

    public String getFormatDataPointValue() {
        return formatDataPointValue;
    }

    public void setFormatDataPointValue(String value) {
        this.formatDataPointValue = value;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String name) {
        this.column = name;
    }
}
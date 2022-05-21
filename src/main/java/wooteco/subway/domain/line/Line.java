package wooteco.subway.domain.line;

public class Line {

    private Long id;
    private final String name;
    private final String color;
    private int extraFare;

    public Line(String name, String color, int extraFare) {
        this.name = name;
        this.color = color;
        this.extraFare = extraFare;
    }

    public Line(Long id, String name, String color, int extraFare) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.extraFare = extraFare;
    }

    public boolean isSameId(Line line) {
        return id.equals(line.getId());
    }

    public boolean isSameName(Line line) {
        return name.equals(line.getName());
    }

    public boolean isSameColor(Line line) {
        return color.equals(line.getColor());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getExtraFare() {
        return extraFare;
    }
}

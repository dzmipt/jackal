package dz.jackal;

public enum Icon {
    SEA("sea"),
    SHIP("ship"),
    CLOSED("closed"),
    LAND("land"),
    MOVE("move"),
    KNIGHT("knight"),
    ICE("ice"),
    MOUNTAIN("mountain")
    ;

    private String location;
    Icon(String location) {
        this.location = location;
    }
    public String getLocation() {return location;}
}

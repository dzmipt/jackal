package dz.jackal;

public enum Icon {
    SEA("sea"),
    SHIP("ship"),
    CLOSED("closed"),
    LAND("land"),
    MOVE("move"),
    KNIGHT("knight"),
    ICE("ice"),
    JUNGLE2 ("jungle2"),
    DESERT ("desert"),
    SWAMP ("swamp"),
    MOUNTAIN("mountain"),
    WOMAN ("woman"),
    FORT ("fort"),
    BALLOON ("balloon"),
    CANNON ("cannon"),
    CANNIBAL ("cannibal"),
    TRAP ("trap"),
    CROCODILE ("crocodile")
    ;

    private String location;
    Icon(String location) {
        this.location = location;
    }
    public String getLocation() {return location;}
}

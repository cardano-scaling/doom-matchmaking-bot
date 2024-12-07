package fi.sundae.bot.tournament;

public enum Region {
  AFRICA("af-south-1", "Africa"),
  EUROPE("eu-central-1", "Europe"),
  SOUTH_AMERICA("sa-east-1", "South America"),
  US_EAST("us-east-1", "US East"),
  US_WEST("us-west-2", "US West");

  private final String REGION_NAME;
  private final String PRETTY_NAME;

  Region(String regionName, String PrettyName) {
    this.REGION_NAME = regionName;
    this.PRETTY_NAME = PrettyName;
  }

  public static Region fromRegionName(String name) {
    for (Region r : Region.values()) {
      if (name.equals(r.REGION_NAME)) return r;
    }
    return null;
  }

  public String getPrettyName() {
    return PRETTY_NAME;
  }

  public String getRegionName() {
    return REGION_NAME;
  }
}

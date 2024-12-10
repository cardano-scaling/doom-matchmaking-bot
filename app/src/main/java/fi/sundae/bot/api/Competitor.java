package fi.sundae.bot.api;

public class Competitor {
  private String pkh;
  private int kills;

  public Competitor() {}

  public void setPkh(String pkh) {
    this.pkh = pkh;
  }

  public String getPkh() {
    return pkh;
  }

  public void setKills(int kills) {
    this.kills = kills;
  }

  public int getKills() {
    return kills;
  }
}

package fi.sundae.bot.api;

public class Competitor {
  private String pkh;
  private int killCount;

  public Competitor() {}

  public void setPkh(String pkh) {
    this.pkh = pkh;
  }

  public String getPkh() {
    return pkh;
  }

  public void setKillCount(int killCount) {
    this.killCount = killCount;
  }

  public int getKillCount() {
    return killCount;
  }
}

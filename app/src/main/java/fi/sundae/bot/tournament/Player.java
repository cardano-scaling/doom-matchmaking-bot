package fi.sundae.bot.tournament;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Player {
  @SerializedName("auth_provider")
  private String authProvider;

  @SerializedName("auth_provider_id")
  private String authProviderId;

  @SerializedName("auth_wallet")
  private String authWallet;

  @SerializedName("auth_name")
  private String authName;

  @SerializedName("auth_email")
  private String authEmail;

  @SerializedName("auth_avatar")
  private String authAvatar;

  @SerializedName("linked_wallet_stake_address")
  private String linkedWalletStakeAddress;

  @SerializedName("linked_discord_account")
  private DiscordAccount linkedDiscordAccount;

  public String getAuthProviderId() {
    return authProviderId;
  }

  public void setAuthProviderId(String authProviderId) {
    this.authProviderId = authProviderId;
  }

  public String getAuthWallet() {
    return authWallet;
  }

  public void setAuthWallet(String authWallet) {
    this.authWallet = authWallet;
  }

  public String getAuthName() {
    return authName;
  }

  public void setAuthName(String authName) {
    this.authName = authName;
  }

  public String getAuthEmail() {
    return authEmail;
  }

  public void setAuthEmail(String authEmail) {
    this.authEmail = authEmail;
  }

  public String getAuthAvatar() {
    return authAvatar;
  }

  public void setAuthAvatar(String authAvatar) {
    this.authAvatar = authAvatar;
  }

  public String getLinkedWalletStakeAddress() {
    return linkedWalletStakeAddress;
  }

  public void setLinkedWalletStakeAddress(String linkedWalletStakeAddress) {
    this.linkedWalletStakeAddress = linkedWalletStakeAddress;
  }

  public DiscordAccount getLinkedDiscordAccount() {
    return linkedDiscordAccount;
  }

  public void setLinkedDiscordAccount(DiscordAccount linkedDiscordAccount) {
    this.linkedDiscordAccount = linkedDiscordAccount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Player player = (Player) o;
    return Objects.equals(authProvider, player.authProvider)
        && Objects.equals(authProviderId, player.authProviderId)
        && Objects.equals(authWallet, player.authWallet)
        && Objects.equals(authName, player.authName)
        && Objects.equals(authEmail, player.authEmail)
        && Objects.equals(authAvatar, player.authAvatar)
        && Objects.equals(linkedWalletStakeAddress, player.linkedWalletStakeAddress)
        && Objects.equals(linkedDiscordAccount, player.linkedDiscordAccount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        authProvider,
        authProviderId,
        authWallet,
        authName,
        authEmail,
        authAvatar,
        linkedWalletStakeAddress,
        linkedDiscordAccount);
  }

  @Override
  public String toString() {
    return "Player{"
        + "authProvider='"
        + authProvider
        + '\''
        + ", authProviderId='"
        + authProviderId
        + '\''
        + ", authWallet='"
        + authWallet
        + '\''
        + ", authName='"
        + authName
        + '\''
        + ", authEmail='"
        + authEmail
        + '\''
        + ", authAvatar='"
        + authAvatar
        + '\''
        + ", linkedWalletStakeAddress='"
        + linkedWalletStakeAddress
        + '\''
        + ", linkedDiscordAccount="
        + linkedDiscordAccount
        + '}';
  }

  public static class DiscordAccount {
    private String id;
    private String name;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      DiscordAccount other = (DiscordAccount) obj;
      return other.id.equals(this.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, name);
    }
  }
}

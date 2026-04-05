package pl.ldz.chat.security.enums;

public enum UserRoles {
  VIEWER("VIEWER"),
  USER("USER"),
  ADMIN("ADMIN"),
  AUDITOR("AUDITOR")
  ;

  private final String name;

  private UserRoles(String name) { this.name = name; }

  public String getName() { return name; }

}

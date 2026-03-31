package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "messages")
public class Message extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id")
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "personal_chat_id")
  private PersonalChat personalChat;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "edited", nullable = false)
  private boolean edited = false;

  @Column(name = "deleted", nullable = false)
  private boolean deleted = false;

  public Message() {}

  public User getSender() { return sender; }
  public void setSender(User sender) { this.sender = sender; }

  public ChatRoom getChatRoom() { return chatRoom; }
  public void setChatRoom(ChatRoom chatRoom) { this.chatRoom = chatRoom; }

  public PersonalChat getPersonalChat() { return personalChat; }
  public void setPersonalChat(PersonalChat personalChat) { this.personalChat = personalChat; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }

  public boolean isEdited() { return edited; }
  public void setEdited(boolean edited) { this.edited = edited; }

  public boolean isDeleted() { return deleted; }
  public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

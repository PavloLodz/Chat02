package pl.ldz.chat.entity.base;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AbstractEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Version
  private Long version;

  @CreatedDate
  @Column(updatable = false)
  private Instant creationTimestamp;

  @LastModifiedDate
  private Instant updateTimestamp;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AbstractEntity that)) return false;
    return id != null && id.equals(that.getId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

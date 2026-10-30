package vn.io.thinz.ecommerce.shared.infrastructure.persistence;

import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.SuperBuilder;

@MappedSuperclass 
@SuperBuilder 
@Getter 
@Setter 
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties (ignoreUnknown = true)
public abstract class BaseEntity {

  @Id 
  @GeneratedValue (strategy = GenerationType.UUID)
  private String id;

  private Instant createdAt;
  
  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  public BaseEntity (){}

  @PrePersist 
  protected void onCreate(){
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate 
  protected void onUpdate(){
    this.updatedAt = Instant.now();
  }
  
}

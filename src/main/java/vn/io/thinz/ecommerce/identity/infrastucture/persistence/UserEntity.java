package vn.io.thinz.ecommerce.identity.infrastucture.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import vn.io.thinz.ecommerce.shared.infrastructure.persistence.BaseEntity;

@Getter 
@Setter 
@Entity 
@Table(name = "users")
@SuperBuilder 
@NoArgsConstructor 
public class UserEntity extends BaseEntity{

  private String email;
  
  private String hashPassword;

  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String avatarUrl;
  private String address;
}

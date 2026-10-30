package vn.io.thinz.ecommerce.identity.domain.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.SuperBuilder;
import vn.io.thinz.ecommerce.shared.infrastructure.persistence.BaseEntity;

@SuperBuilder 
@Entity 
@Getter 
@Setter 
public class User extends BaseEntity {

  private String email;
  
  private String hashPassword;

  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String avatarUrl;
  private String address;
  
}

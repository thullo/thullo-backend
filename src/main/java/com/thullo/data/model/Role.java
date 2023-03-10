package com.thullo.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long roleId;

  @NaturalId
  @Column(length = 60)
  private String name;

  @ManyToMany(mappedBy = "roles")
  @JsonManagedReference
  private Collection<User> users;

  @ManyToMany
  @JoinTable(name = "roles_privileges",
          joinColumns = @JoinColumn(name = "role_id"),
          inverseJoinColumns = @JoinColumn(name = "privilege_id"))
  private List<Privilege> privileges;

  @CreationTimestamp
  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  public Role(String name) {
    this.name = name;
  }
}

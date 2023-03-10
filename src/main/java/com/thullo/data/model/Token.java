package com.thullo.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.thullo.data.model.TokenType.REFRESH;
import static com.thullo.util.AppConstants.EXPIRATION;


@Getter
@Setter
@Entity
@NoArgsConstructor
public class Token extends RepresentationModel<Token> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;

    @OneToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "FK_VERIFY_USER"))
    private User user;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    private LocalDateTime expiryDate;

    private String tokenType;

    public Token(String token, User user, String tokenType) {
        this.token = token;
        this.user = user;
        this.tokenType = tokenType;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    private LocalDateTime calculateExpiryDate(long expiryTimeInHours){
        return LocalDateTime.now().plusHours(expiryTimeInHours);
    }

    public void updateToken(String code){
        this.token = code;
        this.tokenType = REFRESH.toString();
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public Token(User user){
        this.token = UUID.randomUUID().toString();
        this.tokenType = REFRESH.toString();
        this.expiryDate = calculateExpiryDate(EXPIRATION);
        this.user = user;
    }

    public void updateToken(String code, String tokenType){
        this.token = code;
        this.tokenType = tokenType;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }
}

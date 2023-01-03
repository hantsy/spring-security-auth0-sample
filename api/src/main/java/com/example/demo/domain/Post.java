package com.example.demo.domain;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts")
public class Post extends AbstractAuditableEntity<Long> implements Serializable {

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

}

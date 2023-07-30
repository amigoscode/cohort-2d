package com.amigoscode.cohort2d.onlinebookstore.author;

import com.amigoscode.cohort2d.onlinebookstore.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "author",
        uniqueConstraints = @UniqueConstraint(columnNames = {"firstName", "lastName"})
)
public class Author {

    @Id
    @SequenceGenerator(
            name = "author_id_seq",
            sequenceName = "author_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "author_id_seq"
    )
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @EqualsAndHashCode.Exclude
    @ManyToMany(
            mappedBy = "authors",
            fetch = FetchType.LAZY
    )
    private Set<Book> books = new HashSet<>();


    public Author(Long id, @NotBlank String firstName, @NotBlank String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

}

package org.book.catalogue.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(schema = "catalogue", name = "t_book")
@NoArgsConstructor
@AllArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{catalog.errors.null}")
    @NotBlank(message = "{catalog.errors.blank}")
    @Size(min = 3, max = 50, message = "{catalog.errors.size}")
    @Column(name = "c_name", unique = true, nullable = false)
    private String name;

    @NotNull(message = "{catalog.errors.null}")
    @NotBlank(message = "{catalog.errors.blank}")
    @Size(min = 3, max = 50, message = "{catalog.errors.size}")
    @Column(name = "c_author", nullable = false)
    private String author;

    @NotBlank(message = "{catalog.errors.blank}")
    @Size(max = 1000, message = "{catalog.errors.details.size}")
    @Column(name = "c_details", length = 1000)
    private String details;
}

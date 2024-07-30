package org.book.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReviewEntity {

    @Id
    private UUID id;

    private Long bookId;

    private int rating;

    private String review;

    private String userId;
}

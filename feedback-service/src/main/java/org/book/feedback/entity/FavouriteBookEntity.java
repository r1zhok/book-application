package org.book.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteBookEntity {

    @Id
    private UUID id;

    private Long bookId;

    private String userId;
}

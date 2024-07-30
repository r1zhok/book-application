package org.book.feedback.controller.payload;

import jakarta.validation.constraints.NotNull;

public record NewFavouriteBookPayload(

        @NotNull(message = "{feedback.payload.favourite.book_id.is_null.error}")
        Long bookId
) {
}

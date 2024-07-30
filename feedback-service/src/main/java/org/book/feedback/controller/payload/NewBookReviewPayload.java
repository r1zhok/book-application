package org.book.feedback.controller.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewBookReviewPayload(

        @NotNull(message = "{feedback.payload.review.book_id.is_null.error}")
        Long bookId,

        @NotNull(message = "{feedback.payload.review.rating.is_null.error}")
        @Min(value = 1, message = "{feedback.payload.review.rating.is_too_small.error")
        @Max(value = 5, message = "{feedback.payload.review.rating.is_too_big.error}")
        Integer rating,

        @Size(max = 1000, message = "{feedback.payload.review.too_big.error}")
        String review
) {
}

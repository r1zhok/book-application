package org.book.customer.client.payload;

public record NewBookReviewPayload(Long bookId, Integer rating, String review) {
}

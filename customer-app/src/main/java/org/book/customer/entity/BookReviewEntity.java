package org.book.customer.entity;

import java.util.UUID;

public record BookReviewEntity(UUID id, Long bookId, int rating, String review) {
}

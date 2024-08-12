package org.book.manager.controller;

import org.book.manager.client.BooksRestClient;
import org.book.manager.controller.payload.NewBookPayload;
import org.book.manager.entity.BookEntity;
import org.book.manager.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping(("catalogue/books/{bookId:\\d+}"))
public class BookController {

    private final BooksRestClient booksRestClient;

    @ModelAttribute("book")
    public BookEntity book(@PathVariable("bookId") Long bookId) {
        return this.booksRestClient.findBook(bookId)
                .orElseThrow(() -> new NoSuchElementException("Книжка не найдена"));
    }

    @GetMapping
    public String getBook() {
        return "catalogue/books/book";
    }

    @GetMapping("edit")
    public String getBookEditPage() {
        return "catalogue/books/edit";
    }

    @PostMapping("edit")
    public String updateBook(@ModelAttribute(name = "book", binding = false) BookEntity book,
                             NewBookPayload payload, Model model) {
        try {
            this.booksRestClient.updateBook(book.id(), payload.name(), payload.author(), payload.details());
            return "redirect:/catalogue/books/%d".formatted(book.id());
        } catch (BadRequestException exception) {
            model.addAttribute("payload", payload);
            model.addAttribute("errors", exception.getErrors());
            return "catalogue/books/edit";
        }
    }

    @PostMapping("delete")
    public String deleteProduct(@ModelAttribute("book") BookEntity book) {
        this.booksRestClient.deleteBook(book.id());
        return "redirect:/catalogue/books/list";
    }
}

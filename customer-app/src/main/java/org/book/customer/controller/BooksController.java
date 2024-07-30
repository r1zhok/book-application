package org.book.customer.controller;

import lombok.RequiredArgsConstructor;
import org.book.customer.client.BooksClient;
import org.book.customer.client.FavouriteBooksClient;
import org.book.customer.entity.FavouriteBookEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/books")
public class BooksController {

    private final BooksClient booksClient;

    private final FavouriteBooksClient favouriteBooksClient;

    @GetMapping("list")
    public Mono<String> getBooksList(@RequestParam(name = "filter", required = false) String filter,
                                     Model model) {
        model.addAttribute("filter", filter);
        return this.booksClient.findAllBooks(filter)
                .collectList()
                .doOnNext(books -> model.addAttribute("books", books))
                .thenReturn("customer/books/list");
    }

    @GetMapping("favourites")
    public Mono<String> getFavouriteBooksPage(Model model,
                                              @RequestParam(name = "filter", required = false) String filter) {
        model.addAttribute("filter", filter);
        return this.favouriteBooksClient.findFavouriteBooks()
                .map(FavouriteBookEntity::bookId)
                .collectList()
                .flatMap(favouriteBooks -> this.booksClient.findAllBooks(filter)
                        .filter(book -> favouriteBooks.contains(book.id()))
                        .collectList()
                        .doOnNext(books -> model.addAttribute("books", books)))
                .thenReturn("customer/books/favourites");
    }
}

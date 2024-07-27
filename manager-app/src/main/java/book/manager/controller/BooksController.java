package book.manager.controller;

import book.manager.client.BooksRestClient;
import book.manager.controller.payload.NewBookPayload;
import book.manager.entity.BookEntity;
import book.manager.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("catalogue/books")
public class BooksController {

    private final BooksRestClient booksRestClient;

    @GetMapping(value = "/list")
    public String getAllBooks(Model model, @RequestParam(name = "filter", required = false) String filter) {
        model.addAttribute("books", this.booksRestClient.findAllBooks(filter));
        model.addAttribute("filter", filter);
        return "catalogue/books/list";
    }

    @GetMapping("create")
    public String createNewBookPage() {
        return "catalogue/books/new_book";
    }

    @PostMapping("/create")
    public String createBook(NewBookPayload payload, Model model) {
        try {
            BookEntity book = this.booksRestClient.createBook(payload.name(), payload.author(), payload.details());
            return "redirect:/catalogue/books/%d".formatted(book.id());
        } catch (BadRequestException exception) {
            model.addAttribute("payload", payload);
            model.addAttribute("errors", exception.getErrors());
            return "catalogue/books/new_book";
        }
    }
}

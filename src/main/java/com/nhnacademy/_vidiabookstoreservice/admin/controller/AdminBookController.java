package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final AdminBookService adminBookService;
    private final BookService bookService;

    @GetMapping("/search")
    public ResponseEntity<AdminIsbnSearchResponse> searchBookByIsbn(
            @RequestParam @ISBN(type = ISBN.Type.ANY) String isbn
    ) {
        AdminIsbnSearchResponse response = adminBookService.processIsbnSearch(isbn);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createBook (
            @Valid @RequestBody BookCreateRequest request
    ) {

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{book-id}")
    public ResponseEntity<Void> updateBook (
            @PathVariable("book-id") Long bookId,
            @Valid @RequestBody BookUpdateRequest request
    ) {


        return ResponseEntity.ok().build();
    }
}

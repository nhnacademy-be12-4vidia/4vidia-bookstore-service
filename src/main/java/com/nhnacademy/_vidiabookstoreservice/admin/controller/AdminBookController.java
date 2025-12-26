package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.impl.MinioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final AdminBookService adminBookService;
    private final BookService bookService;
    private final MinioService minioService;

    @GetMapping("/search")
    public ResponseEntity<AdminIsbnSearchResponse> searchBookByIsbn(
            @RequestParam @ISBN(type = ISBN.Type.ANY) String isbn
    ) {
        AdminIsbnSearchResponse response = adminBookService.processIsbnSearch(isbn);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/augment")
    public ResponseEntity<AdminIsbnSearchResponse> augmentBookInfo(
            @RequestParam @ISBN(type = ISBN.Type.ANY) String isbn
    ) {
        AdminIsbnSearchResponse response = adminBookService.augmentBookInfo(isbn);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createBook (
            @RequestPart("request") @Valid BookCreateRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        bookService.createBook(request, thumbnail);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{book-id}")
    public ResponseEntity<Void> updateBook (
            @PathVariable("book-id") Long bookId,
            @RequestPart("request") @Valid BookUpdateRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
    ) {
        bookService.updateBook(bookId, request, thumbnail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{book-id}/isbn")
    public ResponseEntity<String> getBookIsbn(
            @PathVariable("book-id") Long bookId
    ) {
        String isbn = bookService.getBookIsbnById(bookId);
        return ResponseEntity.ok(isbn);
    }

    @PostMapping("/images")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("image") MultipartFile image
    ) {
        String imageUrl = minioService.upload(image);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }
}

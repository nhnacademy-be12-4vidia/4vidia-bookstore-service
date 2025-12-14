package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.enums.SummaryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "book_review_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookReviewSummary {

    @Id
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    @Column(nullable = false)
    private boolean dirty;

    @Column(name = "dirty_count", nullable = false)
    private int dirtyCount;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private SummaryStatus status;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    @Column(name = "last_review_id")
    private Long lastReviewId;

    @Column(name = "last_summarized_at")
    private LocalDate lastSummarizedAt;

    @Builder
    public BookReviewSummary(Long bookId) {
        this.bookId = bookId;
        this.dirty = false;
        this.dirtyCount = 0;
        this.status = SummaryStatus.READY;
        this.failCount = 0;
    }

    public void markDirty() {
        this.dirty = true;
        this.dirtyCount += 1;
    }

    public void markRunning() {
        this.status = SummaryStatus.RUNNING;
    }

    public void markSuccess(String summaryText, Long lastReviewId) {
        this.summaryText = summaryText;
        this.lastReviewId = lastReviewId;
        this.lastSummarizedAt = LocalDate.now();
        this.dirty = false;
        this.status = SummaryStatus.READY;
        this.failCount = 0;
        this.dirtyCount = 0;
    }

    public void markFail() {
        this.status = SummaryStatus.FAILED;
        this.failCount += 1;
        this.status = SummaryStatus.READY;
    }
}



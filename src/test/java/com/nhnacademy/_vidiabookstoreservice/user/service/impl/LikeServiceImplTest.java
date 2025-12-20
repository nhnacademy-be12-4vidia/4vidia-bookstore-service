package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.Like;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.UserLikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.already.LikedAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.LikeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.LikeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Test
    @DisplayName("좋아요 리스트 조회 성공(by userId) - 좋아요 리스트 있음")
    void getLikes_success() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        Book book = mock(Book.class);
        given(book.getStockStatus()).willReturn(StockStatus.IN_STOCK);

        Like like1 = mock(Like.class);
        Like like2 = mock(Like.class);
        Like like3 = mock(Like.class);
        given(like1.getBook()).willReturn(book);
        given(like2.getBook()).willReturn(book);
        given(like3.getBook()).willReturn(book);

        List<Like> likeList = List.of(like1, like2, like3);
        given(likeRepository.findAllByUser_UserId(userId)).willReturn(likeList);

        List<LikeResponse> result = likeService.getLikes(userId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        verify(likeRepository).findAllByUser_UserId(userId);
    }

    @Test
    @DisplayName("좋아요 리스트 조회 성공(by userId) - 좋아요 리스트 없음")
    void getLikes_success_empty() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        given(likeRepository.findAllByUser_UserId(userId)).willReturn(new ArrayList<>());

        List<LikeResponse> result = likeService.getLikes(userId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);

        verify(likeRepository).findAllByUser_UserId(userId);
    }

    @Test
    @DisplayName("좋아요 리스트 조회 실패(by userId) - 유저 없음")
    void getLikes_fail_userNotFound() {
        Long userId = 1L;

        given(userRepository.existsById(userId)).willReturn(false);

        assertThatThrownBy(() -> likeService.getLikes(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("좋아요 리스트 조회 성공(by userId, bookIds)")
    void getLikeIdList_success() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        Book book1 = mock(Book.class);
        Book book2 = mock(Book.class);
        Book book3 = mock(Book.class);
        List<Long> bookIds = List.of(10L, 11L, 12L);
        given(book1.getId()).willReturn(bookIds.get(0));
        given(book2.getId()).willReturn(bookIds.get(1));
        given(book3.getId()).willReturn(bookIds.get(2));

        Like like1 = mock(Like.class);
        Like like2 = mock(Like.class);
        Like like3 = mock(Like.class);
        given(like1.getBook()).willReturn(book1);
        given(like2.getBook()).willReturn(book2);
        given(like3.getBook()).willReturn(book3);

        List<Like> userLikeList = List.of(like1, like2, like3);
        given(likeRepository.findAllByUser_UserIdAndBook_IdIn(userId, bookIds))
                .willReturn(userLikeList);

        List<UserLikeResponse> result = likeService.getLikeIdList(userId, bookIds);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(UserLikeResponse::bookId)
                .containsExactly(10L, 11L, 12L);

        verify(likeRepository).findAllByUser_UserIdAndBook_IdIn(userId, bookIds);
    }

    @Test
    @DisplayName("좋아요 리스트 조회 실패(by userId, bookIds) - 유저 없음")
    void getLikeIdList_fail_userNotFound() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(false);

        assertThatThrownBy(() -> likeService.getLikeIdList(userId, anyList()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("좋아요 등록 성공")
    void addLike_success() {
        Long userId = 1L;
        User user = mock(User.class);

        Long bookId = 10L;
        Book book = mock(Book.class);

        given(userRepository.existsById(userId)).willReturn(true);
        given(bookRepository.existsById(bookId)).willReturn(true);
        given(likeRepository.existsByUser_UserIdAndBook_Id(userId, bookId)).willReturn(false);

        given(userRepository.getReferenceById(userId)).willReturn(user);
        given(bookRepository.getReferenceById(bookId)).willReturn(book);

        likeService.addLike(userId, bookId);

        verify(likeRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 유저 없음")
    void addLike_fail_userNotFound() {
        Long userId = 1L;

        given(userRepository.existsById(userId)).willReturn(false);

        assertThatThrownBy(() -> likeService.addLike(userId, anyLong()))
                .isInstanceOf(UserNotFoundException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 책 없음")
    void addLike_fail_bookNotFound() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        Long bookId = 10L;
        given(bookRepository.existsById(bookId)).willReturn(false);

        assertThatThrownBy(() -> likeService.addLike(userId, bookId))
                .isInstanceOf(BookNotFoundException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 이미 좋아요 되있음")
    void addLike_fail_alreadyLiked() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        Long bookId = 10L;
        given(bookRepository.existsById(bookId)).willReturn(true);

        given(likeRepository.existsByUser_UserIdAndBook_Id(userId, bookId))
                .willReturn(true);

        assertThatThrownBy(() -> likeService.addLike(userId, bookId))
                .isInstanceOf(LikedAlreadyExistsException.class);

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 삭제 성공")
    void removeLike_success() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        Long bookId = 10L;
        given(bookRepository.existsById(bookId)).willReturn(true);

        Like like = mock(Like.class);
        given(likeRepository.findByUser_UserIdAndBook_Id(userId, bookId)).willReturn(Optional.of(like));

        likeService.removeLike(userId, bookId);
        verify(likeRepository, times(1)).delete(like);
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 유저 없음")
    void removeLike_fail_userNotFound() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(false);

        assertThatThrownBy(() -> likeService.removeLike(userId, anyLong()))
                .isInstanceOf(UserNotFoundException.class);

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 책 없음")
    void removeLike_fail_bookNotFound() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        Long bookId = 10L;
        given(bookRepository.existsById(bookId)).willReturn(false);

        assertThatThrownBy(() -> likeService.removeLike(userId, bookId))
                .isInstanceOf(BookNotFoundException.class);

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("좋아요 삭제 실패 - 좋아요 등록 안되있음")
    void removeLike_fail_likeNotFound() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);

        Long bookId = 10L;
        given(bookRepository.existsById(bookId)).willReturn(true);

        assertThatThrownBy(() -> likeService.removeLike(userId, bookId))
                .isInstanceOf(LikeNotFoundException.class);

        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("좋아요 전체 삭제 성공")
    void removeAllLike_success() {
        Long userId = 1L;
        given(userRepository.existsById(userId))
                .willReturn(true);

        List<Long> bookIds = List.of(10L, 11L, 12L);
        Like like1 = mock(Like.class);
        Like like2 = mock(Like.class);
        Like like3 = mock(Like.class);
        List<Like> likes = List.of(like1, like2, like3);
        given(likeRepository.findAllByUser_UserIdAndBook_IdIn(userId, bookIds))
                .willReturn(likes);

        likeService.removeAllLike(userId, bookIds);

        verify(likeRepository, times(1)).deleteAll(likes);
    }

    @Test
    @DisplayName("좋아요 전체 삭제 실패 - 유저 없음")
    void removeAllLike_fail_userNotFound() {
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(false);

        assertThatThrownBy(() -> likeService.removeAllLike(userId, anyList()))
                .isInstanceOf(UserNotFoundException.class);

        verify(likeRepository, never()).deleteAll(anyList());
    }
}
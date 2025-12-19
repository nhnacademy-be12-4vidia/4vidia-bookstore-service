package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
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
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class LikeServiceImpl implements LikeService {

    private final UserService userService;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    /**
     * 좋아요 리스트 조회
     * */
    @Override
    @Transactional(readOnly = true)
    public List<LikeResponse> getLikes(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        List<Like> likeList = likeRepository.findAllByUser_UserId(userId);
        return likeList.stream()
                .map(LikeResponse::fromEntity) // n+1 ?
                // 만약 Like → Book이 LAZY라면
                // Like 10개 가져오면 Book 조회 10번 나갈 수도 있음.
                // -> 해결: LikeRepository에서 fetch join 사용
                .toList();
    }


    /**
     * 좋아요 아이디 리스트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserLikeResponse> getLikeIdList(Long userId, List<Long> bookIds) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        List<Like> likes = likeRepository.findAllByUser_UserIdAndBook_IdIn(userId, bookIds);

        return likes.stream()
                .map(UserLikeResponse::fromEntity)
                .toList();
    }


    /**
     * 좋아요 등록
     * */
    @Override
    public void addLike(Long userId, Long bookId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        if (likeRepository.existsByUser_UserIdAndBook_Id(userId, bookId)) {
            throw new LikedAlreadyExistsException();
        }

        User user = userService.getProxyById(userId);
        Book book = bookRepository.getReferenceById(bookId);

        // 좋아요 저장
        Like like = Like.builder()
                .user(user)
                .book(book)
                .build();
        likeRepository.save(like);
    }

    /**
     * 좋아요 삭제
     * */
    @Override
    public void removeLike(Long userId, Long bookId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        Like like = likeRepository.findByUser_UserIdAndBook_Id(userId, bookId)
                .orElseThrow(LikeNotFoundException::new);

        likeRepository.delete(like);
    }

    /**
     * 좋아요 전체 삭제
     */
    @Override
    public void removeAllLike(Long userId, List<Long> bookIds) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        List<Like> likes = likeRepository.findAllByUser_UserIdAndBook_IdIn(userId, bookIds);
        likeRepository.deleteAll(likes);
    }
}
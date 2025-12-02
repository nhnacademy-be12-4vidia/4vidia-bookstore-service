package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.user.domain.Like;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.AlreadyLikedException;
import com.nhnacademy._vidiabookstoreservice.user.exception.LikeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.LikeRepository;
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
    private final BookService bookService;
    private final LikeRepository likeRepository;

    /**
     * 좋아요 여부확인
     * */
    @Override
    public boolean isLiked(Long userId, Long bookId) {
        return likeRepository.existsByUser_UserIdAndBook_Id(userId, bookId);
    }

    /**
     * 좋아요 리스트 조회
     * */
    @Override
    @Transactional(readOnly = true)
    public List<LikeResponse> getLikes(Long userId) {
        userService.getUserById(userId);

        List<Like> likeList = likeRepository.findAllByUser_UserId(userId);
        return likeList.stream()
                .map(LikeResponse::fromEntity) // n+1 ?
                // 만약 Like → Book이 LAZY라면
                // Like 10개 가져오면 Book 조회 10번 나갈 수도 있음.
                // -> 해결: LikeRepository에서 fetch join 사용
                .toList();
    }

    /**
     * 좋아요 등록
     * */
    @Override
    public void addLike(Long userId, Long bookId) {
        if (isLiked(userId, bookId)) {
            throw new AlreadyLikedException();
        }

        User user = userService.getProxyById(userId);
        Book book = bookService.getProxyById(bookId);

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
        Like like = likeRepository.findByUser_UserIdAndBook_Id(userId, bookId)
                .orElseThrow(() -> new LikeNotFoundException());

        likeRepository.delete(like);
    }
    // todo : 이럴땐 어떻게하죠
    //  - 회원이 좋아요를 눌러놓음 -> 관리자가 등록된 책을 내림
    //  - 책이 사라지기 전에 -> 좋아요를 없애야함
    //  >> cascade를 쓰면 안됨??
    // JPA CascadeType.remove 이게 머에요? 이거 절대 쓰지말래요
    // DB on delete cascade 이게 정답이래요? 전 이거 말한건데. 이거 쓰면안되요? 쓰라고 만들어놓은거 아니에요?
    // -> 둘 다 아닌가요? 강사는 실무에서 둘 다 안쓴데요.
    // todo : 그런데 관리자 이쉐키가 -> 내려놓은 책을 다시 등록시키면??
    //  - if 책 내렸을때 좋아요 삭제해놨으면 -> then 좋아요 눌러논거 다 초기화된거에요?
    //  - 내 좋아요 어디갔어요? 문의ㄱ 내 좋아요 돌려놔요
}
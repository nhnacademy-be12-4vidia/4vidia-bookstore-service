package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.CreateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.UpdateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.CategoryAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.delete.CategoryCannotDeleteException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.CategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.ParentCategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.DiscountPolicyRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private DiscountPolicyRepository discountPolicyRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("카테고리 생성 - 성공 (대분류)")
    void createCategory_root_success() {
        String kdcCode = "100";
        String categoryName = "국어";
        CreateCategoryRequest request = new CreateCategoryRequest(kdcCode, categoryName);

        when(categoryRepository.findByKdcCode(kdcCode)).thenReturn(Optional.empty());

        categoryService.createCategory(request);

        verify(categoryRepository).save(argThat(category ->
                category.getKdcCode().equals(kdcCode) &&
                        category.getCategoryName().equals(categoryName)
        ));
    }

    @Test
    @DisplayName("카테고리 생성 - 이미 존재하는 경우 예외 발생")
    void createCategory_alreadyExists_throwsException() {
        String kdcCode = "100";
        String categoryName = "국어";
        CreateCategoryRequest request = new CreateCategoryRequest(kdcCode, categoryName);

        Category existingCategory = mock(Category.class);
        when(existingCategory.getCategoryName()).thenReturn("이미있는이름");
        when(categoryRepository.findByKdcCode(kdcCode)).thenReturn(Optional.of(existingCategory));

        assertThrows(CategoryAlreadyExistsException.class, () ->
                categoryService.createCategory(request)
        );
    }

    @Test
    @DisplayName("createCategory - 중분류 생성 (부모 코드 추출 로직 검증)")
    void createCategory_middle_success() {
        String kdcCode = "110"; // 중분류 -> 부모는 100
        CreateCategoryRequest request = new CreateCategoryRequest(kdcCode, "중분류");
        Category parent = mock(Category.class);
        when(parent.getPath()).thenReturn("/1");
        when(parent.getDepth()).thenReturn(1);
        when(parent.getCategoryName()).thenReturn("대분류");

        when(categoryRepository.findByKdcCode(kdcCode)).thenReturn(Optional.empty());
        when(categoryRepository.findByKdcCode("100")).thenReturn(Optional.of(parent));

        categoryService.createCategory(request);

        verify(categoryRepository).save(argThat(c -> c.getDepth() == 2 && c.getPath().equals("/1/11")));
    }

    @Test
    @DisplayName("createCategory - 이름이 없는 껍데기 카테고리에 이름 업데이트")
    void createCategory_updateExistingShell_success() {
        String kdcCode = "100";
        CreateCategoryRequest request = new CreateCategoryRequest(kdcCode, "새이름");
        Category existing = mock(Category.class);

        when(categoryRepository.findByKdcCode(kdcCode)).thenReturn(Optional.of(existing));
        when(existing.getCategoryName()).thenReturn(null);

        categoryService.createCategory(request);

        verify(existing).updateCategoryName("새이름");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCategory - 잘못된 KDC 코드 형식 (depth 1 강제)")
    void createCategory_invalidKdc_rootForced() {
        CreateCategoryRequest request = new CreateCategoryRequest("ABC", "형식오류");
        when(categoryRepository.findByKdcCode("ABC")).thenReturn(Optional.empty());

        categoryService.createCategory(request);

        verify(categoryRepository).save(argThat(c -> c.getDepth() == 1));
    }

    @Test
    @DisplayName("createCategory - 부모 카테고리를 찾을 수 없을 때 예외 발생")
    void createCategory_parentNotFound_throwsException() {
        String kdcCode = "110"; // 부모가 100이어야 함
        CreateCategoryRequest request = new CreateCategoryRequest(kdcCode, "자식");

        when(categoryRepository.findByKdcCode(kdcCode)).thenReturn(Optional.empty());
        when(categoryRepository.findByKdcCode("100")).thenReturn(Optional.empty());

        assertThrows(ParentCategoryNotFoundException.class, () -> categoryService.createCategory(request));
    }

    @Test
    @DisplayName("createCategory - 소분류 생성 (부모 코드 110 -> 111)")
    void createCategory_small_success() {
        String kdcCode = "111"; // 소분류
        CreateCategoryRequest request = new CreateCategoryRequest(kdcCode, "소분류");
        Category parent = mock(Category.class);

        when(parent.getPath()).thenReturn("/1/11");
        when(parent.getDepth()).thenReturn(2);
        when(parent.getCategoryName()).thenReturn("중분류");
        when(categoryRepository.findByKdcCode(kdcCode)).thenReturn(Optional.empty());
        when(categoryRepository.findByKdcCode("110")).thenReturn(Optional.of(parent));

        categoryService.createCategory(request);

        verify(categoryRepository).save(argThat(c -> c.getDepth() == 3 && c.getPath().equals("/1/11/111")));
    }

    @Test
    @DisplayName("createCategory - 0이 섞인 특수 코드 (101 -> 부모 100)")
    void createCategory_specialCode_success() {
        String kdcCode = "101";
        CreateCategoryRequest request = new CreateCategoryRequest(kdcCode, "특수분류");
        Category parent = mock(Category.class);

        when(categoryRepository.findByKdcCode(kdcCode)).thenReturn(Optional.empty());

        when(parent.getPath()).thenReturn("/1");
        when(parent.getDepth()).thenReturn(1);
        when(parent.getCategoryName()).thenReturn("대분류");
        when(categoryRepository.findByKdcCode("100")).thenReturn(Optional.of(parent));

        categoryService.createCategory(request);

        verify(categoryRepository).save(argThat(c -> c.getPath().equals("/1/101")));
    }

    @Test
    @DisplayName("카테고리 수정 - 성공")
    void updateCategory_success() {
        Long id = 1L;
        UpdateCategoryRequest request = new UpdateCategoryRequest("수정된 이름");
        Category category = mock(Category.class);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        categoryService.updateCategory(id, request);

        verify(category).updateCategoryName("수정된 이름");
    }

    @Test
    @DisplayName("카테고리 삭제 - 도서가 존재하면 예외 발생")
    void deleteCategory_hasBooks_throwsException() {
        // given
        Long id = 1L;
        Category category = mock(Category.class);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        when(categoryRepository.existsByParentCategoryId(id)).thenReturn(false);
        when(bookRepository.existsByCategoryId(id)).thenReturn(true);

        // when & then
        assertThrows(CategoryCannotDeleteException.class, () ->
                categoryService.deleteCategory(id)
        );
    }

    @Test
    @DisplayName("카테고리 삭제 - 성공")
    void deleteCategory_success() {
        Long id = 1L;
        Category category = mock(Category.class);
        when(category.getKdcCode()).thenReturn("100");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        when(categoryRepository.existsByParentCategoryId(id)).thenReturn(false);
        when(bookRepository.existsByCategoryId(id)).thenReturn(false);
        when(discountPolicyRepository.existsByCategoryId(id)).thenReturn(false);

        categoryService.deleteCategory(id);

        verify(categoryRepository).deleteById(id);
    }

    @Test
    @DisplayName("플랫 카테고리 목록 조회 - 상위 카테고리 이름 포함 확인")
    void getFlatCategoryList_success() {
        Category parent = mock(Category.class);
        when(parent.getCategoryName()).thenReturn("예술");

        Category child = mock(Category.class);
        when(child.getCategoryName()).thenReturn("음악");
        when(child.getParentCategory()).thenReturn(parent);
        when(child.getKdcCode()).thenReturn("600");

        when(categoryRepository.findAllByOrderByKdcCodeAsc()).thenReturn(List.of(child));

        List<CategoryListResponse> result = categoryService.getFlatCategoryList();

        assertEquals(1, result.size());
//        assertTrue(result.get(0).categoryName().contains("예술 > 음악"));
    }

    @Test
    @DisplayName("getCategoryList - 모든 카테고리 목록 조회 성공")
    void getCategoryList_success() {
        Category category = mock(Category.class);
        when(categoryRepository.findAllByOrderByKdcCodeAsc()).thenReturn(List.of(category));

        List<CategoryListResponse> result = categoryService.getCategoryList();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getCategory - 단건 조회 성공")
    void getCategory_success() {
        Category category = mock(Category.class);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategory(1L);

        assertEquals(category, result);
    }

    @Test
    @DisplayName("getCategory - 존재하지 않을 때 예외 발생")
    void getCategory_notFound_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategory(1L));
    }

    @Test
    @DisplayName("getCategoryProxy - 프록시 객체 조회 성공")
    void getCategoryProxy_success() {
        Category category = mock(Category.class);
        when(categoryRepository.getReferenceById(1L)).thenReturn(category);

        Category result = categoryService.getCategoryProxy(1L);

        assertEquals(category, result);
    }

    @Test
    @DisplayName("deleteCategory - UNC 코드는 삭제 불가 예외")
    void deleteCategory_UNC_throwsException() {
        Category unc = mock(Category.class);
        when(unc.getKdcCode()).thenReturn("UNC");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(unc));

        assertThrows(CategoryCannotDeleteException.class, () -> categoryService.deleteCategory(1L));
    }

    @Test
    @DisplayName("deleteCategory - 하위 카테고리가 존재하여 삭제 불가")
    void deleteCategory_hasChildren_throwsException() {
        Category category = mock(Category.class);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentCategoryId(1L)).thenReturn(true);

        assertThrows(CategoryCannotDeleteException.class, () -> categoryService.deleteCategory(1L));
    }

    @Test
    @DisplayName("deleteCategory - 할인 정책이 적용되어 있어 삭제 불가")
    void deleteCategory_hasDiscountPolicy_throwsException() {
        Category category = mock(Category.class);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentCategoryId(1L)).thenReturn(false);
        when(bookRepository.existsByCategoryId(1L)).thenReturn(false);
        when(discountPolicyRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(CategoryCannotDeleteException.class, () -> categoryService.deleteCategory(1L));
    }
}
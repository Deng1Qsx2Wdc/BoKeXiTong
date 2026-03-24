package com.example.demo.service.impl;

import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.enums.InteractionStatus;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.FollowsMapper;
import com.example.demo.pojo.entity.Follows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowsServiceTest {

    @Mock
    private FollowsMapper followsMapper;

    @Mock
    private AuthorService authorService;

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private FollowsService followsService;

    @Test
    void followShouldReactivateExistingInactiveRecordInsteadOfInsertingDuplicate() {
        Follows request = new Follows();
        request.setAuthorId(1L);
        request.setTargetId(2L);

        Follows existingFollow = new Follows();
        existingFollow.setId(100L);
        existingFollow.setAuthorId(1L);
        existingFollow.setTargetId(2L);
        existingFollow.setStatus(InteractionStatus.INACTIVE.getCode());

        when(followsMapper.selectOne(any())).thenReturn(existingFollow);
        when(followsMapper.updateById(existingFollow)).thenReturn(1);

        followsService.Follow(request);

        assertEquals(InteractionStatus.ACTIVE.getCode(), existingFollow.getStatus());
        verify(followsMapper).updateById(existingFollow);
        verify(followsMapper, never()).insert(any(Follows.class));
        verify(authorService).DeleteAuthorAllMessage(request.getAuthorId());
    }

    @Test
    void followShouldRejectWhenAlreadyActive() {
        Follows request = new Follows();
        request.setAuthorId(1L);
        request.setTargetId(2L);

        Follows existingFollow = new Follows();
        existingFollow.setId(100L);
        existingFollow.setAuthorId(1L);
        existingFollow.setTargetId(2L);
        existingFollow.setStatus(InteractionStatus.ACTIVE.getCode());

        when(followsMapper.selectOne(any())).thenReturn(existingFollow);

        BusinessException exception = assertThrows(BusinessException.class, () -> followsService.Follow(request));

        assertEquals(ErrorCode.FOLLOW_ALREADY_EXISTS.getCode(), exception.getCode());
        verify(followsMapper, never()).updateById(any(Follows.class));
        verify(followsMapper, never()).insert(any(Follows.class));
    }
}

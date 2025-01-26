package com.euiyeonlog.respository;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.domain.QPost;
import com.euiyeonlog.request.PostSearch;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    // 쿼리를 만드는 작업
    @Override
    public List<Post> getAll(int page) {
        return jpaQueryFactory.selectFrom(QPost.post)
                .limit(5)
                .offset((long)(page-1)*10)
                .orderBy(QPost.post.id.desc())
                .fetch();
    }

    @Override
    public List<Post> getAll(PostSearch postSearch) {
        return jpaQueryFactory.selectFrom(QPost.post)
                .limit(postSearch.getPageSize())
                // pageable 요구사항에 따라 달라질 수 있어서? getOffset() 정의?
//                .offset((long)(postSearch.getPage()-1) * postSearch.getPageSize())
                .offset(postSearch.getOffset())
                .orderBy(QPost.post.id.desc())
                .fetch();
    }
}

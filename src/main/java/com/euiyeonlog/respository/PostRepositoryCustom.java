package com.euiyeonlog.respository;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.request.PostSearch;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> getAll(int page);
    List<Post> getAll(PostSearch postSearch);
}

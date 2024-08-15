package org.dromakin.repository;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dromakin.exception.NotFoundException;
import org.dromakin.model.Post;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Repository
public class PostRepository {

    private static final Logger logger = LogManager.getLogger(PostRepository.class);

    private volatile ConcurrentHashMap<Long, Post> postsData = new ConcurrentHashMap<>();

    private AtomicLong idData = new AtomicLong(1L);

    public List<Post> all() {
        return postsData.values().parallelStream().collect(Collectors.toList());
    }

    public Optional<Post> getById(long id) {
        return Optional.ofNullable(postsData.get(id));
    }

    public Post saveUpdate(Post post) {
        Post result;
        long id = post.getId();

        if (id == 0) {
            post.setId(idData.getAndIncrement());
            result = save(post);

        } else {
            result = update(post);
        }

        return result;
    }

    public Post save(Post post) {
        postsData.put(post.getId(), post);
        return post;
    }

    public Post update(Post post) {
        Post result = postsData.get(post.getId());
        if (result != null) {
            postsData.put(post.getId(), post);
            result = postsData.get(post.getId());
        } else {
            throw new NotFoundException("Wrong post id to update!");
        }
        return result;
    }

    public void removeById(long id) {
        Post tmp = postsData.remove(id);
        if (tmp == null) {
            throw new NotFoundException(id + " not found");
        } else {
            idData.decrementAndGet();
        }
    }
}

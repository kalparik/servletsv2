package org.dromakin.servlet;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dromakin.controller.PostController;
import org.dromakin.repository.PostRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(PostRepository.class);
    private static final String API_PATH = "/api/posts";
    private static final String DIGITAL_PATTERN = "/\\d";
    private PostController controller;

    @Override
    public void init() {
        final var context = new AnnotationConfigApplicationContext("org.dromakin");
        controller = context.getBean(PostController.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        // если деплоились в root context, то достаточно этого
        try {
            final var path = req.getRequestURI();
            final var method = req.getMethod();
            // primitive routing
            if (method.equals("GET") && path.equals(API_PATH)) {
                controller.all(resp);
                logger.info("[GET] /api/posts - OK 200");
                return;
            }
            if (method.equals("GET") && path.matches(API_PATH + DIGITAL_PATTERN)) {
                // easy way
                final var id = getIdFromUrlPath(path);
                controller.getById(id, resp);
                logger.info("[GET] /api/posts/{id} - OK 200");
                return;
            }
            if (method.equals("POST") && path.equals(API_PATH)) {
                logger.info("[POST] /api/posts/ - OK 200");
                controller.save(req.getReader(), resp);
                return;
            }
            if (method.equals("DELETE") && path.matches(API_PATH + DIGITAL_PATTERN)) {
                // easy way
                final var id = getIdFromUrlPath(path);
                controller.removeById(id, resp);
                logger.info("[DELETE] /api/posts/{id} - OK 200");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            logger.error(e);
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected Long getIdFromUrlPath(String path) {
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }
}


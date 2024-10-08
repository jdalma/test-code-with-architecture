package com.example.demo.mock;

import com.example.demo.common.domain.ClockHolder;
import com.example.demo.common.domain.UuidHolder;
import com.example.demo.post.controller.PostController;
import com.example.demo.post.controller.PostCreateController;
import com.example.demo.post.controller.port.PostService;
import com.example.demo.post.service.PostServiceImpl;
import com.example.demo.post.service.port.PostRepository;
import com.example.demo.user.controller.MyInfoController;
import com.example.demo.user.controller.UserController;
import com.example.demo.user.controller.UserCreateController;
import com.example.demo.user.controller.port.AuthenticationService;
import com.example.demo.user.controller.port.UserCreateService;
import com.example.demo.user.controller.port.UserReadService;
import com.example.demo.user.controller.port.UserUpdateService;
import com.example.demo.user.service.CertificationService;
import com.example.demo.user.service.UserServiceImpl;
import com.example.demo.user.service.port.MailSender;
import com.example.demo.user.service.port.UserRepository;
import lombok.Builder;

public class TestContainer {

    public final MailSender mailSender;
    public final UserRepository userRepository;
    public final PostRepository postRepository;
    public final PostService postService;
    public final UserReadService userReadService;
    public final UserCreateService userCreateService;
    public final UserUpdateService userUpdateService;
    public final CertificationService certificationService;
    public final AuthenticationService authenticationService;
    public final UserController userController;
    public final MyInfoController myInfoController;
    public final UserCreateController userCreateController;
    public final PostController postController;
    public final PostCreateController postCreateController;

    @Builder
    public TestContainer(ClockHolder clockHolder, UuidHolder uuidHolder) {
        this.mailSender = new FakeMailSender();
        this.userRepository = new FakeUserRepository();
        this.postRepository = new FakePostRepository();
        this.postService = new PostServiceImpl(this.userRepository, this.postRepository, clockHolder);
        this.certificationService = new CertificationService(this.mailSender);
        UserServiceImpl userService = new UserServiceImpl(
            this.userRepository,
            this.certificationService,
            uuidHolder,
            clockHolder
        );
        this.authenticationService = userService;
        this.userReadService = userService;
        this.userCreateService = userService;
        this.userUpdateService = userService;
        this.userController = new UserController(userService, authenticationService);
        this.myInfoController = new MyInfoController(userService, userService, userService, authenticationService);
        this.userCreateController = new UserCreateController(userService);
        this.postController = new PostController(postService);
        this.postCreateController = new PostCreateController(postService);
    }
}

package com.qwerty.app;

import java.lang.reflect.Method;

import com.qwerty.app.images.AWSImageFaceDetectorAdapter;
import com.qwerty.app.images.ImageFaceBlur;
import com.qwerty.app.images.ImageFaceDetector;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebMvcRegistrationsAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class Application {

    @Value("${rest.api.base.path}")
    private String restApiBasePath;

//    public static void main(String[] args) {
//        SpringApplication.run(Application.class, args);
//    }

    private static final String FFMPEG_LIBRARY_NAME = "opencv_ffmpeg340_64";
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(FFMPEG_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        ImageFaceDetector detector = new AWSImageFaceDetectorAdapter();
        ImageFaceBlur imageFaceBlur = new ImageFaceBlur(detector);
        imageFaceBlur.processAndSave("backend/input.jpg", "backend/ouput.jpg");

        //OpenCVVideoDetection detection = new OpenCVVideoDetection(imageFaceBlur);
        //detection.process("video.avi", "ouput.avi");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("*").allowedOrigins("http://localhost:4200");
            }
        };
    }

    @Bean
    public WebMvcRegistrationsAdapter webMvcRegistrationsHandlerMapping() {
        Application application = this;
        return new WebMvcRegistrationsAdapter() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new RequestMappingHandlerMapping() {

                    @Override
                    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
                        Class<?> beanType = method.getDeclaringClass();
                        RestController restApiController = beanType.getAnnotation(RestController.class);
                        if (restApiController != null) {
                            PatternsRequestCondition apiPattern = new PatternsRequestCondition(application.restApiBasePath)
                                    .combine(mapping.getPatternsCondition());

                            mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                    mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                    mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                    mapping.getProducesCondition(), mapping.getCustomCondition());
                        }

                        super.registerHandlerMethod(handler, method, mapping);
                    }
                };
            }
        };
    }
}

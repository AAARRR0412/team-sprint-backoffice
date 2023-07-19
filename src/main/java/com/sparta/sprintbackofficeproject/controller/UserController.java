package com.sparta.sprintbackofficeproject.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.sprintbackofficeproject.dto.EmailRequestDto;
import com.sparta.sprintbackofficeproject.dto.SignupRequestDto;
import com.sparta.sprintbackofficeproject.exception.ApiException;
import com.sparta.sprintbackofficeproject.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiException> signup(@RequestBody @Valid SignupRequestDto requestDto, BindingResult bindingResult) throws MessagingException, JsonProcessingException {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if(fieldErrors.size()> 0 ){
            for (FieldError fieldError : fieldErrors){
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
                return ResponseEntity.badRequest().body(
                        new ApiException(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage(), HttpStatus.BAD_REQUEST.value())
                );
            }
        }
        userService.signup(requestDto);
        return ResponseEntity.ok().body(new ApiException("인증코드가 발송되었습니다.", HttpStatus.OK.value()));
    }

    @PostMapping("/signup/email-auth")
    public ResponseEntity<ApiException> verifyCode(@RequestBody EmailRequestDto requestDto) throws IOException {
        if (userService.verifyCode(requestDto.getEmail(), requestDto.getCode())) {

            return ResponseEntity.ok().body(new ApiException("회원가입이 완료되었습니다.", HttpStatus.OK.value()));
        }
        userService.saveUserAfterVerify(requestDto.getEmail());
        return ResponseEntity.badRequest().body(new ApiException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST.value()));
    }
}
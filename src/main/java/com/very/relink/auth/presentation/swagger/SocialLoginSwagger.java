package com.very.relink.auth.presentation.swagger;

import com.very.relink.auth.adapter.in.web.SocialLoginRequest;
import com.very.relink.auth.adapter.in.web.SocialLoginResponse;
import com.very.relink.core.presentation.ErrorResponse;
import com.very.relink.core.presentation.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Social Auth", description = "소셜 로그인 API")
public interface SocialLoginSwagger {

    @Operation(
            summary = "소셜 로그인",
            description = "프론트엔드 SDK에서 발급받은 provider token으로 로그인하고 서비스 JWT를 발급합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "소셜 로그인 성공",
                    content = @Content(schema = @Schema(implementation = SocialLoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "소셜 로그인 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<RestResponse<SocialLoginResponse>> login(
            @Valid @RequestBody SocialLoginRequest socialLoginRequest
    );
}

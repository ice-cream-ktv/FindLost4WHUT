package com.whut.lostandfoundforwhut.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whut.lostandfoundforwhut.common.enums.ResponseCode;
import com.whut.lostandfoundforwhut.common.exception.AppException;
import com.whut.lostandfoundforwhut.common.utils.security.jwt.JwtUtil;
import com.whut.lostandfoundforwhut.mapper.UserMapper; // 新增：Mock Controller 直接调用的 Mapper
import com.whut.lostandfoundforwhut.model.dto.UserNicknameUpdateDTO;
import com.whut.lostandfoundforwhut.model.dto.UserPasswordUpdateDTO;
import com.whut.lostandfoundforwhut.model.entity.User;
import com.whut.lostandfoundforwhut.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 新增：匹配 Mapper 的 QueryWrapper
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. Mock Controller 依赖的 Service
    @MockBean
    private IUserService userService;

    // 2. 新增：Mock Controller 直接调用的 UserMapper（核心，避免走真实数据库）
    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtUtil jwtUtil;

    // ==================== updatePassword 测试方法 ====================
    @Test
    void updatePassword_returnsNoPermissionWhenEmailMismatch() throws Exception {
        // 1. Mock JWT 解析
        when(jwtUtil.getEmail("token")).thenReturn("login@example.com");

        // 2. Mock Mapper 查询（如果 Controller 直接调 Mapper，必须加）
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setEmail("login@example.com");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(loginUser);

        // 3. Mock Service 方法
        doNothing().when(userService).requireUserByEmail("login@example.com");
        when(userService.getUserById(1L)).thenReturn(loginUser);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("other@example.com");
        when(userService.getUserById(2L)).thenReturn(targetUser);

        // 4. 构造请求参数
        UserPasswordUpdateDTO dto = new UserPasswordUpdateDTO();
        dto.setPassword("newPassword123");
        dto.setConfirmPassword("newPassword123");

        // 5. 执行请求并验证（核心修复：$.msg → $.info）
        mockMvc.perform(put("/api/users/2/password")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.NO_PERMISSION.getCode()))
                // ✅ 关键修复：响应体字段是 info 不是 msg
                .andExpect(jsonPath("$.info").value(ResponseCode.NO_PERMISSION.getInfo()));

        verify(userService, never()).updatePassword(anyLong(), any(UserPasswordUpdateDTO.class));
    }

    // ==================== updateNickname 测试方法 ====================
    @Test
    void updateNickname_returnsIllegalParameterWhenNicknameBlank() throws Exception {
        // 1. Mock JWT 解析
        when(jwtUtil.getEmail("token")).thenReturn("login@example.com");

        // 2. Mock Mapper 查询（避免用户不存在）
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setEmail("login@example.com");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(loginUser);

        // 3. Mock Service 方法
        doNothing().when(userService).requireUserByEmail("login@example.com");
        when(userService.getUserById(1L)).thenReturn(loginUser);

        // 4. Mock 异常抛出
        doThrow(new AppException(
                ResponseCode.ILLEGAL_PARAMETER.getCode(),
                ResponseCode.ILLEGAL_PARAMETER.getInfo()
        )).when(userService).updateNickname(anyLong(), any(UserNicknameUpdateDTO.class));

        // 5. 构造请求参数
        UserNicknameUpdateDTO dto = new UserNicknameUpdateDTO();
        dto.setNickname(" ");

        // 6. 执行请求并验证（核心修复：$.msg → $.info）
        mockMvc.perform(put("/api/users/1/nickname")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResponseCode.ILLEGAL_PARAMETER.getCode()))
                // ✅ 关键修复：响应体字段是 info 不是 msg
                .andExpect(jsonPath("$.info").value(ResponseCode.ILLEGAL_PARAMETER.getInfo()));

        verify(userService, times(1)).updateNickname(1L, dto);
    }
}
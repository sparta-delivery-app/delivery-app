package com.example.deliveryapp.client;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Operations s3Operations;

    @InjectMocks
    private S3Service s3Service;

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UploadImageTests {
        @Test
        @Order(1)
        void 이미지_업로드_빈파일_실패() {
            // given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(true);

            // when & then
            CustomException customException = assertThrows(CustomException.class,
                    () -> s3Service.uploadImage("menu", mockFile)
            );
            assertEquals(ErrorCode.EMPTY_FILE, customException.getErrorCode());
        }

        @Test
        @Order(2)
        void 이미지_업로드_이미지_외_타입_실패() {
            // given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getContentType()).thenReturn("test/plain");

            // when & then
            CustomException customException = assertThrows(CustomException.class,
                    () -> s3Service.uploadImage("menu", mockFile)
            );
            assertEquals(ErrorCode.INVALID_CONTENT_TYPE, customException.getErrorCode());
        }

        @Test
        @Order(3)
        void 이미지_업로드_성공() {
            // given
            MultipartFile mockFile = new MockMultipartFile(
                    "file",
                    "image.jpg",
                    "image/jpeg",
                    "contents".getBytes()
            );

            // when
            String result = s3Service.uploadImage("menu", mockFile);

            // then
            assertNotNull(result);
            verify(s3Operations, times(1)).upload(any(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteImageTests {
        @Test
        @Order(1)
        void 이미지_삭제_성공() {
            // given
            doNothing().when(s3Operations).deleteObject(any(), anyString());

            // when
            s3Service.deleteImage("menu", "image.jpg");

            // then
            verify(s3Operations, times(1)).deleteObject(any(), anyString());
        }
    }

    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateSignedUrlTests {
        @Test
        @Order(1)
        void 서명된_URL_생성_성공() throws MalformedURLException {
            // given
            when(s3Operations.createSignedGetURL(any(), anyString(), any(Duration.class))).thenReturn(new URL("https://signedUrl"));

            // when
            String result = s3Service.createSignedUrl("menu", "image.jpg");

            // then
            assertNotNull(result);
            verify(s3Operations, times(1)).createSignedGetURL(any(), anyString(), any(Duration.class));
        }
    }
}
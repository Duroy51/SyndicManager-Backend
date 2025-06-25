package com.enspy.syndicmanager.dto.response;


import lombok.*;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
     int status;
     String text;
     Object data;

     public HttpStatusCode statusCode() {
          return HttpStatusCode.valueOf(status);
     }
}

package com.example.CollabAuth.Email;



import com.example.grpc.EmailResponse;
import com.example.grpc.EmailServiceGrpc;
import com.example.grpc.User;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailClientService {

    @Autowired
    private EmailServiceGrpc.EmailServiceBlockingStub emailServiceBlockingStub;

    public boolean SendWelcomeMail(String userMail, String userName) {
        try {

            User request = User.newBuilder()
                    .setUserMail(userMail)
                    .setUserName(userName)
                    .build();

            EmailResponse response = emailServiceBlockingStub.sendWelcomeMail(request);
            if(response.getSuccess()) {
                return true;
            } else {
                return false;
            }

        } catch (StatusRuntimeException e) {
            log.info("Error While Sending Welcome Mail " + e.getMessage());
            return false;
        }
    }
}

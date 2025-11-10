package com.example.CollabAuth.gRPC;


import com.example.grpc.EmailServiceGrpc;
import io.grpc.ManagedChannel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @GrpcClient("email-service")
    private ManagedChannel managedChannel;

    @Bean
    private EmailServiceGrpc.EmailServiceBlockingStub emailServiceBlockingStub(
            @GrpcClient("email-service") ManagedChannel channel) {
        return EmailServiceGrpc.newBlockingStub(channel);
    }

}

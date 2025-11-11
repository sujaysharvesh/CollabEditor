package com.example.CollabAuth.gRPC;

import com.example.grpc.EmailServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {


    @Value("${gRpc.server.port}") private Integer port;
    @Value("${gRpc.server.host}") private String host;

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public EmailServiceGrpc.EmailServiceBlockingStub emailServiceBlockingStub(ManagedChannel channel) {
        return EmailServiceGrpc.newBlockingStub(channel);
    }
}
